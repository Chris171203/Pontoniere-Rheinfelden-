import SwiftUI
import PFVRCore

enum PFVRTheme {
    static let navy = Color(red: 12 / 255, green: 45 / 255, blue: 72 / 255)
    static let water = Color(red: 43 / 255, green: 142 / 255, blue: 166 / 255)
    static let background = Color(uiColor: .systemGroupedBackground)
    static let card = Color(uiColor: .secondarySystemGroupedBackground)
}

struct PFVRCard<Content: View>: View {
    let title: String?
    @ViewBuilder let content: Content
    init(_ title: String? = nil, @ViewBuilder content: () -> Content) {
        self.title = title
        self.content = content()
    }
    var body: some View {
        VStack(alignment: .leading, spacing: 14) {
            if let title { Text(title).font(.headline) }
            content
        }
        .frame(maxWidth: .infinity, alignment: .leading)
        .padding(16)
        .background(PFVRTheme.card, in: RoundedRectangle(cornerRadius: 18))
    }
}

struct PageScroll<Content: View>: View {
    @ViewBuilder let content: Content
    init(@ViewBuilder content: () -> Content) { self.content = content() }
    var body: some View {
        ScrollView {
            LazyVStack(alignment: .leading, spacing: 14) { content }
                .padding(16)
                .frame(maxWidth: 860)
                .frame(maxWidth: .infinity)
        }
        .background(PFVRTheme.background)
    }
}

struct SourceStamp: View {
    @EnvironmentObject private var state: AppState
    let source: String
    let updated: Date?
    var stale = false
    var body: some View {
        VStack(alignment: .leading, spacing: 3) {
            Text(source).font(.caption2)
            if let updated {
                Text(state.ui(stale ? "Gespeicherter Stand" : "Stand") + " · " + AppDates.stamp(updated))
                    .font(.caption2)
            } else { Text(state.ui("Noch keine gespeicherten Daten.")).font(.caption2) }
        }.foregroundStyle(.secondary)
    }
}

enum AppDates {
    static var zurich: Calendar {
        var calendar = Calendar(identifier: .gregorian)
        calendar.timeZone = TimeZone(identifier: "Europe/Zurich")!
        return calendar
    }
    static func format(_ date: Date, _ pattern: String) -> String {
        let formatter = DateFormatter()
        formatter.locale = Locale(identifier: "de_CH")
        formatter.timeZone = zurich.timeZone
        formatter.dateFormat = pattern
        return formatter.string(from: date)
    }
    static func stamp(_ date: Date) -> String { format(date, "dd.MM.yyyy HH:mm") }
    static func day(_ date: Date, language: AppLanguage = .german) -> String {
        Language.translate(format(date, "EEEE"), mode: language) + ", " + format(date, "d. MMMM")
    }
    static func time(_ date: Date) -> String { format(date, "HH:mm") }
    static func number(_ value: Double?, digits: Int = 0) -> String {
        guard let value, value.isFinite else { return "–" }
        return String(format: "%.*f", digits, value)
    }
}
