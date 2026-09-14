import Foundation
import SwiftSoup

/// Selects source sections; unfamiliar markup remains visible in the full native source.
public enum ClubContentParser {
    public static func parse(page: ClubPage, data: Data) throws -> ClubContent {
        guard data.count <= 1_000_000,
              let rows = try JSONSerialization.jsonObject(with: data) as? [[String: Any]],
              let row = rows.first(where: { $0["slug"] as? String == page.rawValue && $0["link"] as? String == page.url.absoluteString }),
              let titleHTML = (row["title"] as? [String: Any])?["rendered"] as? String,
              let html = (row["content"] as? [String: Any])?["rendered"] as? String else {
            throw PFVRDataError.invalidPayload("Vereinsinfos konnten gerade nicht geladen werden.")
        }
        let document = try SwiftSoup.parseBodyFragment(html, page.url.absoluteString)
        guard let body = document.body() else { throw PFVRDataError.invalidPayload("Leere Vereinsseite") }
        try body.select("script,style,form,iframe,object,embed,svg,nav,button,input,select,textarea").remove()
        let title = NewsParser.plain(titleHTML)
        let source = try sourceText(body)
        guard !title.isEmpty, !source.isEmpty else { throw PFVRDataError.invalidPayload("Leere Vereinsseite") }
        let modified = (row["modified"] as? String).flatMap { PFVRDate.parseLocal($0, format: "yyyy-MM-dd'T'HH:mm:ss") }
        var content = ClubContent(title: title, text: source, modified: modified)
        content.links = try links(body)
        switch page {
        case .about: try about(body, content: &content)
        case .youth: try youth(body, content: &content)
        case .board: try board(body, content: &content)
        case .history: try history(body, content: &content)
        case .contact: break
        }
        return content
    }
    private static func sourceText(_ element: Element) throws -> String {
        let blocks = try element.select("h1,h2,h3,h4,h5,h6,p,li,figcaption").array()
        let values = try blocks.map { try $0.text() }.filter { !$0.isEmpty }
        return values.isEmpty ? try element.text() : values.joined(separator: "\n\n")
    }
    static func links(_ element: Element) throws -> [ClubLink] {
        var seen: Set<URL> = []
        return try element.select("a[href]").array().compactMap { link in
            let href = try link.absUrl("href")
            guard let url = URL(string: href), ["https","http","mailto","tel"].contains(url.scheme?.lowercased() ?? ""),
                  !["http","https"].contains(url.scheme ?? "") || url.host != nil,
                  seen.insert(url).inserted else { return nil }
            let text = try link.text()
            return ClubLink(label: text.isEmpty ? url.absoluteString : text, url: url)
        }
    }
    private static func blocks(_ body: Element) throws -> [Element] { try body.select("h2,h3,p,img").array() }
    private static func heading(_ blocks: [Element], _ name: String) throws -> Int? {
        try blocks.firstIndex { block in
            guard ["h2","h3"].contains(block.tagName()) else { return false }
            return try block.text().lowercased() == name.lowercased()
        }
    }
    private static func firstPhoto(_ elements: [Element]) throws -> ClubPhoto? {
        for element in elements where element.tagName() == "img" {
            if let photo = try photo(element) { return photo }
        }
        return nil
    }
    private static func photo(_ image: Element) throws -> ClubPhoto? {
        var url = URL(string: try image.absUrl("src"))
        var selected = Int.max
        for candidate in try image.attr("srcset").split(separator: ",") {
            let parts = candidate.split(whereSeparator: \.isWhitespace)
            guard parts.count == 2, parts[1].hasSuffix("w"), let width = Int(parts[1].dropLast()),
                  width >= 640, width < selected,
                  let option = URL(string: String(parts[0]), relativeTo: URL(string: "https://www.pfvr.ch/")!)?.absoluteURL,
                  ClubPhoto.allowed(option) else { continue }
            selected = width; url = option
        }
        guard let url, ClubPhoto.allowed(url) else { return nil }
        var caption = try image.attr("alt")
        var ancestor = image.parent()
        while let current = ancestor {
            if current.tagName() == "figure", let label = try current.select("figcaption").first() {
                caption = try label.text(); break
            }
            ancestor = current.parent()
        }
        return ClubPhoto(url: url, caption: caption, width: Int(try image.attr("width")) ?? 0, height: Int(try image.attr("height")) ?? 0)
    }
    private static func section(_ id: String, _ title: String, _ html: String, appTitle: Bool = true, photo: ClubPhoto? = nil, location: String = "") throws -> ClubSection {
        let body = try SwiftSoup.parseBodyFragment(html, "https://www.pfvr.ch/").body()!
        var result = ClubSection(id: id, title: title, appTitle: appTitle)
        result.text = try sourceText(body); result.links = try links(body); result.photo = photo; result.location = location
        return result
    }
    private static func paragraphHTML(_ elements: [Element]) throws -> String {
        try elements.filter { $0.tagName() == "p" }.map { try $0.outerHtml() }.joined()
    }
    private static func about(_ body: Element, content: inout ClubContent) throws {
        let b = try blocks(body)
        guard let general = try heading(b, "Allgemeine Informationen"), let training = try heading(b, "Training"),
              let sport = try heading(b, "Sport"), general < training, training < sport else { return }
        var life = ""; var intro = ""
        for block in b[(general + 1)..<training] {
            let text = try block.text()
            if block.tagName() == "p", text.range(of: #"\b[12][0-9]{3} gegründet\."#, options: .regularExpression) != nil {
                intro = ClubTraining.firstSentence(text)
                content.milestones.append(try section("founding", ClubTraining.match(#"([12][0-9]{3}) gegründet"#, text), block.outerHtml(), appTitle: false))
            }
            if block.tagName() == "p", text.hasPrefix("Das gesellschaftliche Leben") || text.hasPrefix("Unter dem Jahr") { life += try block.outerHtml() }
            if block.tagName() == "img", let image = try photo(block), image.caption.contains("Einweihung") {
                let date = ClubTraining.match(#"(\b[0-9]{2}\.[0-9]{2}\.[12][0-9]{3}\b)"#, image.caption)
                if !date.isEmpty {
                    var milestone = ClubSection(id: "club-photo", title: date, appTitle: false)
                    milestone.text = image.caption; milestone.photo = image; content.milestones.append(milestone)
                }
            }
        }
        content.hero = try firstPhoto(Array(b[(general + 1)..<training]))
        let trainingBlocks = Array(b[(training + 1)..<sport])
        let html = try paragraphHTML(trainingBlocks)
        if let winter = html.range(of: "Während der Wintersaison"), html.contains("In der Sommersaison") {
            let summer = String(html[..<winter.lowerBound]), cold = "<p>" + html[winter.lowerBound...]
            var group = ClubSection(id: "training", title: "Training")
            group.children = [
                try section("summer", "Sommertraining", summer, photo: firstPhoto(trainingBlocks), location: summer.contains("Depot der Pontoniere Rheinfelden") ? "Depot der Pontoniere Rheinfelden" : ""),
                try section("winter", "Wintertraining", cold, location: cold.contains("Schützenturnhalle") ? "Schützenturnhalle Rheinfelden" : "")
            ]
            content.sections.append(group)
        } else if !html.isEmpty { content.sections.append(try section("training", "Training", html, photo: firstPhoto(trainingBlocks))) }
        var boats = ClubSection(id: "boats", title: "Unsere Boote")
        for name in ["Weidling","Boot"] {
            guard let h = try heading(b, name) else { continue }
            var i = h + 1
            while i < b.count && !["h2","h3"].contains(b[i].tagName()) {
                if b[i].tagName() == "p", !(try b[i].text()).isEmpty {
                    let imageEnd = min(b.count, i + 1 + (try b[i].select("img").size()))
                    boats.children.append(try section("boat-" + name.lowercased(), name, b[i].outerHtml(), appTitle: false,
                                                      photo: firstPhoto(Array(b[(h + 1)..<imageEnd]))))
                    break
                }
                i += 1
            }
        }
        if !boats.children.isEmpty { content.sections.append(boats) }
        var techniques = ClubSection(id: "sport", title: "Pontoniersport erklärt")
        var introHTML = ""
        for i in (sport + 1)..<b.count {
            let block = b[i], text = try block.text()
            guard block.tagName() == "p", !text.isEmpty else { continue }
            if text.hasPrefix("Pontonier ist") || text.hasPrefix("Das Schiff wird") {
                introHTML += try block.outerHtml()
                if text.hasPrefix("Pontonier ist") { intro += (intro.isEmpty ? "" : " ") + ClubTraining.firstSentence(text) }
            }
            let dash = text.range(of: "– ")
            if dash != nil || text.contains("Beim Sektionswettfahren") {
                var end = i + 1
                while end < b.count && !["p","h2","h3"].contains(b[end].tagName()) { end += 1 }
                while end < b.count && b[end].tagName() == "p" {
                    if !(try b[end].text()).isEmpty { break }
                    end += 1
                }
                while end < b.count && b[end].tagName() == "img" { end += 1 }
                let image = try firstPhoto(Array(b[(i + 1)..<end]))
                if let dash {
                    let rest = String(text[dash.upperBound...])
                    let title = rest.components(separatedBy: CharacterSet(charactersIn: "(:–")).first!.trimmingCharacters(in: .whitespacesAndNewlines)
                    let multiple = rest.contains("– ")
                    techniques.children.append(try section("tech-" + title, multiple ? "Weitere Disziplinen" : title,
                                                           block.outerHtml(), appTitle: multiple, photo: image))
                } else { techniques.children.append(try section("section-racing", "Sektionsfahren", block.outerHtml(), photo: image)) }
            }
        }
        let sportSource = try section("intro", "", introHTML)
        techniques.text = sportSource.text; techniques.links = sportSource.links
        if !techniques.text.isEmpty || !techniques.children.isEmpty { content.sections.append(techniques) }
        if !life.isEmpty { content.sections.append(try section("life", "Vereinsleben", life, photo: content.hero)) }
        content.intro = intro
    }
    private static func youth(_ body: Element, content: inout ClubContent) throws {
        for p in try body.select("p").array() {
            let text = try p.text(); var html = try p.html()
            if let start = html.range(of: "Jeweils im Herbst") {
                content.sections.append(try section("youth-training", "Fahren und Knoten", "<p>" + html[start.lowerBound...] + "</p>", photo: firstPhoto(blocks(body))))
            }
            if text.hasPrefix("Jeweils im Sommer veranstaltet") {
                if let age = html.range(of: "Wer schon ") {
                    guard let after = html.range(of: "Weitere Informationen", range: age.lowerBound..<html.endIndex) else { continue }
                    html = String(html[..<age.lowerBound]) + html[after.lowerBound...]
                }
                content.sections.append(try section("camp", "JP-Lager", "<p>" + html + "</p>"))
            }
        }
        for image in try body.select("img").array() {
            if let photo = try photo(image), photo.caption == "Schnüren" {
                content.sections.append(try section("knots", photo.caption, "", appTitle: false, photo: photo))
            }
        }
    }
    private static func board(_ body: Element, content: inout ClubContent) throws {
        for column in try body.select(".wp-block-column").array() {
            guard let role = try column.select("h3").first() else { continue }
            let paragraphs = try column.select("p").array().filter { !(try $0.text()).hasPrefix("Bild folgt") }
            let html = try paragraphHTML(paragraphs)
            if !html.isEmpty {
                let title = try role.text()
                content.sections.append(try section("board-" + title, title, html, appTitle: false, photo: firstPhoto(blocks(column))))
            }
        }
    }
    private static func history(_ body: Element, content: inout ClubContent) throws {
        for p in try body.select("p").array() {
            let text = try p.text()
            if text.contains("Jubiläumsbuch") && text.contains("1896") && text.contains("1996") {
                content.milestones.append(try section("book", "1996", p.outerHtml(), appTitle: false)); break
            }
        }
        for link in content.links where link.url.pathExtension.lowercased() == "pdf" || link.url.path.contains("/vorstandsarchiv-tabelle") {
            var item = ClubSection(id: link.id, title: link.url.pathExtension.lowercased() == "pdf" ? "Jubiläumsbuch (PDF)" : "Vorstandsarchiv")
            item.links = [link]; content.sections.append(item)
        }
    }
}
