import Foundation

enum InternalNavigationPolicy {
    /// Do not hand rejected URLs to Safari: query strings can contain personal access data.
    static func permits(_ url: URL) -> Bool {
        guard let parts = URLComponents(url: url, resolvingAgainstBaseURL: false) else { return false }
        return parts.scheme?.lowercased() == "https"
            && parts.host?.lowercased() == "intern.pfvr.ch"
            && parts.user == nil && parts.password == nil
            && (parts.port == nil || parts.port == 443)
    }

    static func normalizedInitialURL(_ rawValue: String) -> URL? {
        let trimmed = rawValue.trimmingCharacters(in: .whitespacesAndNewlines)
        guard var parts = URLComponents(string: trimmed), let original = parts.url, permits(original) else { return nil }
        var items = parts.queryItems ?? []
        let indices = items.indices.filter { items[$0].name == "what" }
        guard indices.count == 1, let index = indices.first else { return nil }
        if items[index].value == "abmeldung_ics_feed" { items[index].value = "abmeldung" }
        guard items[index].value == "abmeldung" else { return nil }
        parts.queryItems = items
        // A fragment is not part of the personal server link and must not become a script channel.
        parts.fragment = nil
        return parts.url
    }
}
