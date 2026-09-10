import Foundation

public enum NewsParser {
    public static func parse(_ data: Data) throws -> [NewsArticle] {
        guard let rows = try JSONSerialization.jsonObject(with: data) as? [[String: Any]] else { throw PFVRDataError.invalidPayload("News: ungültige Antwort") }
        var seen: Set<Int> = []
        let articles: [NewsArticle] = rows.compactMap { row in
            let title = plain((row["title"] as? [String: Any])?["rendered"] as? String ?? "")
            let excerpt = plain((row["excerpt"] as? [String: Any])?["rendered"] as? String ?? "")
            guard let number = row["id"] as? NSNumber, number.intValue > 0, !seen.contains(number.intValue), !title.isEmpty, let link = row["link"] as? String, let url = URL(string: link), ["http","https"].contains(url.scheme?.lowercased() ?? ""), url.host != nil else { return nil }
            seen.insert(number.intValue)
            let date = (row["date"] as? String).flatMap { PFVRDate.parseLocal($0, format: "yyyy-MM-dd'T'HH:mm:ss") }
            return NewsArticle(id: number.intValue, publishedAt: date, title: title, excerpt: excerpt, url: url)
        }
        guard rows.isEmpty || !articles.isEmpty else { throw PFVRDataError.invalidPayload("News: keine gültigen Artikel") }
        return articles
    }
    public static func plain(_ html: String) -> String {
        var value = html.replacingOccurrences(of: "(?is)<(script|style)\\b[^>]*>.*?</\\1\\s*>", with: "", options: .regularExpression)
        value = value.replacingOccurrences(of: "<[^>]+>", with: " ", options: .regularExpression)
        let entities = ["amp":"&", "lt":"<", "gt":">", "quot":"\"", "apos":"'", "nbsp":" ", "ndash":"–", "mdash":"—", "hellip":"…", "auml":"ä", "ouml":"ö", "uuml":"ü", "Auml":"Ä", "Ouml":"Ö", "Uuml":"Ü", "szlig":"ß", "lsquo":"‘", "rsquo":"’", "ldquo":"“", "rdquo":"”", "eacute":"é"]
        let expression = try! NSRegularExpression(pattern: "&(#x[0-9A-Fa-f]+|#[0-9]+|[A-Za-z]+);")
        for match in expression.matches(in: value, range: NSRange(value.startIndex..., in: value)).reversed() {
            guard let full = Range(match.range, in: value), let keyRange = Range(match.range(at: 1), in: value) else { continue }
            let key = String(value[keyRange])
            var decoded = entities[key]
            if key.hasPrefix("#x"), let code = UInt32(key.dropFirst(2), radix: 16), let scalar = UnicodeScalar(code) { decoded = String(scalar) }
            else if key.hasPrefix("#"), let code = UInt32(key.dropFirst()), let scalar = UnicodeScalar(code) { decoded = String(scalar) }
            if let decoded { value.replaceSubrange(full, with: decoded) }
        }
        return value.split(whereSeparator: { $0.isWhitespace }).joined(separator: " ")
    }
}
