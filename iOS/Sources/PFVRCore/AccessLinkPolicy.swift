import Foundation

public enum AccessLinkPolicy {
    public static func isPFVRHost(_ host: String?) -> Bool {
        let value = host?.trimmingCharacters(in: .whitespacesAndNewlines).lowercased() ?? ""
        return value == "pfvr.ch" || value.hasSuffix(".pfvr.ch")
    }
    public static func isInternalPFVRHost(_ host: String?) -> Bool { host?.lowercased() == "intern.pfvr.ch" }
    public static func mayStayInPublicWebView(_ url: URL) -> Bool {
        guard safeHTTPS(url) else { return false }
        return isPFVRHost(url.host) || url.host?.lowercased() == "calendar.google.com"
    }
    public static func mayStayInInternalWebView(_ url: URL) -> Bool { safeHTTPS(url) && isInternalPFVRHost(url.host) }
    public static func mayOpenExternally(_ url: URL) -> Bool {
        guard let scheme = url.scheme?.lowercased() else { return false }
        if scheme == "https" || scheme == "http" { return url.host?.isEmpty == false && url.user == nil && url.password == nil }
        return ["mailto", "tel", "geo"].contains(scheme)
    }
    public static func validatedInternalURL(_ raw: String) -> URL? {
        let trimmed = raw.trimmingCharacters(in: .whitespacesAndNewlines)
        guard !trimmed.unicodeScalars.contains(where: { CharacterSet.controlCharacters.contains($0) }),
              let url = URL(string: trimmed), mayStayInInternalWebView(url),
              let components = URLComponents(url: url, resolvingAgainstBaseURL: false) else { return nil }
        let actions = components.queryItems?.filter { $0.name == "what" } ?? []
        guard actions.count == 1, actions.first?.value == "abmeldung" else { return nil }
        return url
    }
    private static func safeHTTPS(_ url: URL) -> Bool {
        url.scheme?.lowercased() == "https" && url.host?.isEmpty == false && url.user == nil && url.password == nil && (url.port == nil || url.port == 443)
    }
}
