import Foundation

public enum AppLanguage: String, CaseIterable, Codable, Identifiable, Sendable {
    case german = "de"
    case swissGerman = "gsw"
    public var id: String { rawValue }
    public var label: String { self == .german ? "Deutsch" : "Schwiizerdütsch" }
}

/// Only pass app-owned labels. Public calendar/news titles, person names and website text remain verbatim.
public enum Language {
    private static let swiss: [String: String] = {
        guard let url = Bundle.module.url(forResource: "swiss_german", withExtension: "json"),
              let data = try? Data(contentsOf: url), let values = try? JSONDecoder().decode([String: String].self, from: data) else { return [:] }
        return values
    }()
    public static func translate(_ appOwnedText: String, mode: AppLanguage) -> String {
        guard mode == .swissGerman else { return appOwnedText }
        if let direct = swiss[appOwnedText] { return direct }
        let arrow = "  →"
        if appOwnedText.hasSuffix(arrow), let value = swiss[String(appOwnedText.dropLast(arrow.count))] { return value + arrow }
        let background = "Hintergrundaktualisierung: "
        if appOwnedText.hasPrefix(background) {
            let state = String(appOwnedText.dropFirst(background.count))
            return "Hintergrund-Aktualisierig: " + (state == "Ein" ? "Aa" : state == "Aus" ? "Us" : state)
        }
        for (prefix, replacement) in [("Schifffahrtslage · ", "Schifffahrtslag · "), ("Rhein-Kachel ", "Rhy-Kachle ")] {
            if appOwnedText.hasPrefix(prefix) { return replacement + appOwnedText.dropFirst(prefix.count) }
        }
        return appOwnedText
    }
}
