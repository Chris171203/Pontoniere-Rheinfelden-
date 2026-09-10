import Foundation
import Security

extension Notification.Name {
    static let internalSettingsDidChange = Notification.Name("PFVRInternalSettingsDidChange")
}

/// Personal links and explicitly selected participants stay on this device.
/// The WebView uses an ephemeral store; only this allowlisted participant state persists.
final class SecureInternalStore {
    static let shared = SecureInternalStore()
    private let service: String

    init(service: String = "ch.pfvr.app.internal") { self.service = service }

    enum StoreError: LocalizedError {
        case invalidURL
        case unavailable
        var errorDescription: String? {
            switch self {
            case .invalidURL: return "Bitte den persönlichen HTTPS-An-/Abmelde-Link (what=abmeldung) verwenden."
            case .unavailable: return "Der lokale Zugang konnte nicht sicher gespeichert werden."
            }
        }
    }

    func loadURL() -> URL? {
        guard let data = read(account: "initial-url"), let raw = String(data: data, encoding: .utf8) else { return nil }
        return InternalNavigationPolicy.normalizedInitialURL(raw)
    }

    func saveURL(_ rawValue: String) throws {
        guard let url = InternalNavigationPolicy.normalizedInitialURL(rawValue) else { throw StoreError.invalidURL }
        if url != loadURL() { try delete(account: "people-v4") }
        try write(Data(url.absoluteString.utf8), account: "initial-url")
        NotificationCenter.default.post(name: .internalSettingsDidChange, object: nil)
    }

    func removeURL() throws {
        try delete(account: "initial-url")
        try delete(account: "people-v4")
        NotificationCenter.default.post(name: .internalSettingsDidChange, object: nil)
    }

    func loadPeopleState() -> String? {
        guard let data = read(account: "people-v4"), data.count <= 200_000 else { return nil }
        return String(data: data, encoding: .utf8)
    }

    func savePeopleState(_ raw: String?) throws {
        guard let raw else { try delete(account: "people-v4"); return }
        let data = Data(raw.utf8)
        guard data.count <= 200_000,
              let state = try? JSONSerialization.jsonObject(with: data) as? [String: Any],
              state["version"] as? Int == 4,
              state["desired"] is [String] else { return }
        try write(data, account: "people-v4")
    }

    private func query(account: String) -> [String: Any] {
        [kSecClass as String: kSecClassGenericPassword,
         kSecAttrService as String: service,
         kSecAttrAccount as String: account,
         kSecAttrSynchronizable as String: false]
    }

    private func read(account: String) -> Data? {
        var query = query(account: account)
        query[kSecReturnData as String] = true
        query[kSecMatchLimit as String] = kSecMatchLimitOne
        var result: CFTypeRef?
        guard SecItemCopyMatching(query as CFDictionary, &result) == errSecSuccess else { return nil }
        return result as? Data
    }

    private func write(_ data: Data, account: String) throws {
        let query = query(account: account)
        let attributes: [String: Any] = [kSecValueData as String: data,
                                       kSecAttrAccessible as String: kSecAttrAccessibleWhenUnlockedThisDeviceOnly]
        let status = SecItemUpdate(query as CFDictionary, attributes as CFDictionary)
        if status == errSecItemNotFound {
            var item = query
            attributes.forEach { item[$0.key] = $0.value }
            guard SecItemAdd(item as CFDictionary, nil) == errSecSuccess else { throw StoreError.unavailable }
        } else if status != errSecSuccess {
            throw StoreError.unavailable
        }
    }

    private func delete(account: String) throws {
        let status = SecItemDelete(query(account: account) as CFDictionary)
        guard status == errSecSuccess || status == errSecItemNotFound else { throw StoreError.unavailable }
    }
}
