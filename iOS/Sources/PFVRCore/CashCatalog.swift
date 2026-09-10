import Foundation

/// Exact cent arithmetic; no binary floating point enters the payment total.
public struct CashMoney: Codable, Equatable, Comparable, Sendable {
    public let cents: Int64
    public init(cents: Int64) { self.cents = cents }
    public var decimalString: String {
        let magnitude = cents.magnitude
        let fractional = magnitude % 100
        return "\(cents < 0 ? "-" : "")\(magnitude / 100).\(fractional < 10 ? "0" : "")\(fractional)"
    }
    public var formatted: String { "CHF \(decimalString)" }
    public static func < (lhs: CashMoney, rhs: CashMoney) -> Bool { lhs.cents < rhs.cents }
}

public struct CashItem: Codable, Equatable, Identifiable, Sendable {
    public let id: String
    public let name: String
    public let variant: String
    public let price: CashMoney
    public let deposit: Bool
    public var displayName: String { variant.isEmpty ? name : "\(name) · \(variant)" }
}

public struct CashCategory: Codable, Equatable, Identifiable, Sendable {
    public let id: String
    public let label: String
    public let items: [CashItem]
}

public struct CashCatalog: Equatable, Sendable {
    public enum CatalogError: Error, Equatable { case missingResource, unsupportedSchema, invalidPrice, duplicateID, invalidID, invalidCurrency }
    public let currency: String
    public let title: String
    public let validFrom: String
    public let categories: [CashCategory]

    public static func loadBundled() throws -> CashCatalog {
        guard let url = Bundle.module.url(forResource: "vereinsbeiz_prices", withExtension: "json") else {
            throw CatalogError.missingResource
        }
        return try CashCatalog(data: Data(contentsOf: url))
    }

    public init(data: Data) throws {
        struct Source: Decodable {
            struct Category: Decodable {
                struct Item: Decodable { let id: String; let name: String; let variant: String?; let price: Decimal; let deposit: Bool? }
                let id: String; let label: String; let items: [Item]
            }
            let schemaVersion: Int; let currency: String; let title: String; let validFrom: String; let categories: [Category]
        }
        let source = try JSONDecoder().decode(Source.self, from: data)
        guard source.schemaVersion == 1 else { throw CatalogError.unsupportedSchema }
        guard source.currency == "CHF" else { throw CatalogError.invalidCurrency }
        var categoryIDs = Set<String>()
        var itemIDs = Set<String>()
        var mapped: [CashCategory] = []
        for category in source.categories {
            guard CashCartState.cleanID(category.id) == category.id, !category.id.isEmpty else { throw CatalogError.invalidID }
            guard categoryIDs.insert(category.id).inserted else { throw CatalogError.duplicateID }
            let items = try category.items.map { item -> CashItem in
                guard CashCartState.cleanID(item.id) == item.id, !item.id.isEmpty else { throw CatalogError.invalidID }
                guard itemIDs.insert(item.id).inserted else { throw CatalogError.duplicateID }
                guard item.price >= 0, item.price <= 100_000 else { throw CatalogError.invalidPrice }
                var scaled = item.price * 100
                var rounded = Decimal()
                NSDecimalRound(&rounded, &scaled, 0, .plain)
                // A price catalog must specify cents; silently rounding server prices would conceal a data error.
                guard rounded == scaled else { throw CatalogError.invalidPrice }
                return CashItem(id: item.id, name: item.name, variant: item.variant ?? "", price: CashMoney(cents: NSDecimalNumber(decimal: rounded).int64Value), deposit: item.deposit ?? false)
            }
            mapped.append(CashCategory(id: category.id, label: category.label, items: items))
        }
        currency = source.currency
        title = source.title
        validFrom = source.validFrom
        categories = mapped
    }

    public func item(id: String) -> CashItem? { categories.lazy.flatMap(\.items).first { $0.id == id } }
    public func total(quantities: [String: Int]) -> CashMoney {
        let cents = categories.flatMap(\.items).reduce(Int64(0)) { subtotal, item in
            subtotal + item.price.cents * Int64(max(0, min(99, quantities[item.id] ?? 0)))
        }
        return CashMoney(cents: cents)
    }
}
