import Foundation

public enum ClubPage: String, Codable, CaseIterable, Sendable {
    case about = "verein", youth = "jungpontoniere", board = "vorstand", history = "geschichte", contact = "kontakt"
    public var url: URL {
        let path: String
        switch self {
        case .youth: path = "verein/jungpontoniere"
        case .board: path = "verein/vorstand"
        default: path = rawValue
        }
        return URL(string: "https://www.pfvr.ch/\(path)/")!
    }
    public var endpoint: URL { URL(string: "https://www.pfvr.ch/wp-json/wp/v2/pages?slug=\(rawValue)&_fields=slug,link,title,content,modified")! }
    public static func matching(_ url: URL) -> ClubPage? {
        guard url.scheme == "https", url.host == "www.pfvr.ch", url.user == nil, url.port == nil else { return nil }
        return allCases.first { $0.url.path == url.path }
    }
}
public enum ClubDestination: String, CaseIterable, Identifiable, Sendable {
    case training, sport, life, youth, board, history, contact
    public var id: String { rawValue }
    public var page: ClubPage {
        switch self { case .training, .sport, .life: return .about; case .youth: return .youth
        case .board: return .board; case .history: return .history; case .contact: return .contact }
    }
    public var label: String {
        switch self { case .training: return "Trainingsinfos"; case .sport: return "Boote & Sport"
        case .life: return "Vereinsleben"; case .youth: return "Jungpontoniere"; case .board: return "Vorstand"
        case .history: return "Geschichte"; case .contact: return "Kontakt" }
    }
    public static func matching(_ url: URL) -> ClubDestination? {
        guard let page = ClubPage.matching(url) else { return nil }
        return page == .about ? .life : allCases.first { $0.page == page }
    }
    public func sections(in content: ClubContent) -> [ClubSection] {
        let ids: Set<String>
        switch self { case .training: ids = ["training"]; case .sport: ids = ["boats","sport"]; case .life: ids = ["life"]; default: ids = [] }
        return content.sections.filter { ids.isEmpty || ids.contains($0.id) }
    }
}
public struct ClubPhoto: Codable, Equatable, Sendable {
    public let url: URL
    public let caption: String
    public let width: Int
    public let height: Int
    public static func allowed(_ url: URL) -> Bool {
        url.scheme == "https" && url.host == "www.pfvr.ch" && url.user == nil && url.port == nil
        && url.path.hasPrefix("/wp-content/uploads/") && !url.path.contains("..")
        && ["jpg","jpeg","png","webp"].contains(url.pathExtension.lowercased())
    }
}
public struct ClubLink: Codable, Equatable, Identifiable, Sendable {
    public let label: String
    public let url: URL
    public var id: String { url.absoluteString }
}
public struct ClubSection: Codable, Equatable, Identifiable, Sendable {
    public let id: String
    public let title: String
    public var appTitle = true
    public var text = ""
    public var photo: ClubPhoto?
    public var children: [ClubSection] = []
    public var location = ""
    public var links: [ClubLink] = []
}
public struct ClubContent: Codable, Equatable, Sendable {
    public let title: String
    public let text: String
    public let modified: Date?
    public var intro = ""
    public var hero: ClubPhoto?
    public var sections: [ClubSection] = []
    public var milestones: [ClubSection] = []
    public var links: [ClubLink] = []
    public var overviewIntro: String {
        intro.isEmpty ? ClubTraining.firstSentence(text) : intro
    }
}
public struct ClubTraining: Equatable, Identifiable, Sendable {
    public let id: String
    public let label: String
    public let season: String
    public let days: String
    public let time: String
    public let location: String
    public let fallback: String
    public static func summaries(_ content: ClubContent) -> [ClubTraining] {
        ClubDestination.training.sections(in: content).flatMap { group in
            (group.children.isEmpty ? [group] : group.children).map { section in
                let text = section.text
                let season = match(#"\(([^()]+)\)"#, text)
                let days = match(#"\bam\s+(.+?)(?:,|\s+(?:von|um)\s+)"#, text)
                let regex = try! NSRegularExpression(pattern: #"\b([0-2]?\d:[0-5]\d)\s*Uhr"#)
                let times = regex.matches(in: text, range: NSRange(text.startIndex..., in: text)).prefix(2).compactMap {
                    Range($0.range(at: 1), in: text).map { String(text[$0]) }
                }
                let complete = !days.isEmpty && !times.isEmpty && !section.location.isEmpty
                return ClubTraining(id: section.id, label: section.title, season: season, days: days,
                                    time: times.joined(separator: "–"), location: section.location,
                                    fallback: complete ? "" : firstSentence(text))
            }
        }
    }
    static func match(_ pattern: String, _ text: String) -> String {
        guard let regex = try? NSRegularExpression(pattern: pattern),
              let match = regex.firstMatch(in: text, range: NSRange(text.startIndex..., in: text)),
              let range = Range(match.range(at: 1), in: text) else { return "" }
        return String(text[range])
    }
    static func firstSentence(_ text: String) -> String {
        guard let range = text.range(of: #"[.!?](?:\s|$)"#, options: .regularExpression) else { return text }
        return String(text[..<text.index(after: range.lowerBound)])
    }
}
