import Foundation

public struct CashCartState: Codable, Equatable, Sendable {
    public private(set) var quantities: [String: Int]
    public private(set) var paymentConfirmationPending: Bool
    public var itemCount: Int { quantities.values.reduce(0, +) }
    public var isEmpty: Bool { quantities.isEmpty }

    public init(quantities: [String: Int] = [:], paymentConfirmationPending: Bool = false) {
        self.quantities = Self.sanitize(quantities)
        self.paymentConfirmationPending = paymentConfirmationPending && !self.quantities.isEmpty
    }

    private enum CodingKeys: CodingKey { case quantities, paymentConfirmationPending }
    public init(from decoder: Decoder) throws {
        let values = try decoder.container(keyedBy: CodingKeys.self)
        self.init(quantities: try values.decodeIfPresent([String: Int].self, forKey: .quantities) ?? [:],
                  paymentConfirmationPending: try values.decodeIfPresent(Bool.self, forKey: .paymentConfirmationPending) ?? false)
    }

    static func cleanID(_ id: String) -> String {
        let clean = id.trimmingCharacters(in: .whitespacesAndNewlines)
        guard !clean.isEmpty, clean.count <= 120, !clean.contains("="), !clean.contains("\n"), !clean.contains("\r") else { return "" }
        return clean
    }

    public static func sanitize(_ values: [String: Int]) -> [String: Int] {
        var result: [String: Int] = [:]
        for key in values.keys.sorted() {
            let id = cleanID(key)
            if !id.isEmpty, let quantity = values[key], quantity > 0 { result[id] = min(99, quantity) }
        }
        return result
    }

    public mutating func setQuantity(_ quantity: Int, for id: String) {
        let clean = Self.cleanID(id)
        guard !clean.isEmpty else { return }
        if quantity <= 0 { quantities.removeValue(forKey: clean) }
        else { quantities[clean] = min(99, quantity) }
        if isEmpty { paymentConfirmationPending = false }
    }

    public mutating func clear() { quantities.removeAll(); paymentConfirmationPending = false }
    public mutating func markExternalHandoff(fromCart: Bool, launched: Bool) {
        if fromCart && launched && !isEmpty { paymentConfirmationPending = true }
    }
    public mutating func confirmPayment(successful: Bool) {
        // Prevent a stray confirmation callback from clearing a cart with no outstanding handoff.
        guard paymentConfirmationPending else { return }
        if successful { clear() }
        else { paymentConfirmationPending = false }
    }
}

/// The app owns this on the main actor. A single encoded value keeps cart and confirmation consistent.
public final class CashCartStore {
    public private(set) var state: CashCartState
    private let defaults: UserDefaults
    private let key: String
    public init(defaults: UserDefaults = .standard, key: String = "pfvr.cash.state.v1") {
        self.defaults = defaults
        self.key = key
        if let data = defaults.data(forKey: key), let saved = try? JSONDecoder().decode(CashCartState.self, from: data) {
            state = saved
        } else { state = CashCartState() }
    }
    public func setQuantity(_ quantity: Int, for id: String) { state.setQuantity(quantity, for: id); save() }
    public func clear() { state.clear(); save() }
    public func markExternalHandoff(fromCart: Bool, launched: Bool) { state.markExternalHandoff(fromCart: fromCart, launched: launched); save() }
    public func confirmPayment(successful: Bool) { state.confirmPayment(successful: successful); save() }
    private func save() {
        // The state contains only bounded strings, integers and booleans; encoding cannot encounter nonfinite values.
        guard let data = try? JSONEncoder().encode(state) else { return }
        defaults.set(data, forKey: key)
    }
}
