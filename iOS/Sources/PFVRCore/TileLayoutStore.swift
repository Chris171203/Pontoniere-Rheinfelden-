import Foundation

public enum TileArea: String, CaseIterable, Codable, Identifiable, Sendable {
    case home, cash, club
    public var id: String { rawValue }
    public var label: String { self == .home ? "Home" : self == .cash ? "Kasse" : "Verein" }
}
public struct TileSpec: Equatable, Identifiable, Sendable {
    public enum Width: String, Sendable { case wide, compact }
    public let id: String
    public let area: TileArea
    public let label: String
    public let width: Width
    public let pinned: Bool
}

public final class TileLayoutStore {
    private let defaults: UserDefaults
    public init(defaults: UserDefaults = .standard) { self.defaults = defaults }
    public static func specs(_ area: TileArea) -> [TileSpec] {
        let rows: [(String, String)]
        switch area {
        case .home: rows = [("home_weather", "Trainingswetter"), ("home_weather_3day", "3-Tage-Wetter"),
                           ("home_river_summary", "Rhein aktuell"), ("home_river_charts", "Rhein-Grafiken"),
                           ("home_events", "Nächste Termine"), ("home_news", "Vereinsnews")]
        case .cash: rows = [("cash_cart", "Warenkorb"), ("cash_drinks", "Trinken"), ("cash_food", "Essen"),
                           ("cash_celebrations", "Feiern"), ("cash_free_amount", "Freier Betrag"),
                           ("cash_twint", "TWINT"), ("cash_payment_details", "Zahlungsdaten")]
        case .club: rows = [("club_about", "Über den Verein"), ("club_news", "Vereinsnews"),
                           ("club_program", "Jahresprogramm"), ("club_board", "Vorstand"), ("club_history", "Geschichte"),
                           ("club_depot", "Depot & Route"), ("club_phone", "Telefon"), ("club_email", "E-Mail"),
                           ("club_contact", "Kontaktseite"), ("club_instagram", "Instagram"), ("club_facebook", "Facebook")]
        }
        return rows.map { TileSpec(id: $0.0, area: area, label: $0.1, width: area == .club && $0.0 != "club_about" ? .compact : .wide, pinned: $0.0 == "cash_cart") }
    }
    public static func normalizeOrder(area: TileArea, requested: [String]) -> [String] {
        let catalog = specs(area)
        let known = Dictionary(uniqueKeysWithValues: catalog.map { ($0.id, $0) })
        let supplied = Set(requested)
        var order = catalog.filter(\.pinned).map(\.id)
        func append(_ id: String) { if !order.contains(id) { order.append(id) } }
        for id in requested {
            guard let spec = known[id], !spec.pinned else { continue }
            append(id)
            if area == .home && id == "home_weather" && !supplied.contains("home_weather_3day") { append("home_weather_3day") }
        }
        for spec in catalog where !spec.pinned { append(spec.id) }
        return order
    }
    public static func sanitizeHidden(area: TileArea, requested: Set<String>) -> Set<String> {
        let optional = Set(specs(area).filter { !$0.pinned }.map(\.id))
        return requested.intersection(optional)
    }
    public static func moveOrder(area: TileArea, current: [String], id: String, delta: Int) -> [String] {
        var order = normalizeOrder(area: area, requested: current)
        let catalog = specs(area)
        guard delta != 0, let spec = catalog.first(where: { $0.id == id }), !spec.pinned,
              let index = order.firstIndex(of: id) else { return order }
        let pinnedCount = catalog.filter(\.pinned).count
        let target = max(pinnedCount, min(order.count - 1, index + (delta < 0 ? -1 : 1)))
        if index != target { order.swapAt(index, target) }
        return order
    }
    public func ordered(_ area: TileArea) -> [TileSpec] {
        let ids = Self.normalizeOrder(area: area, requested: defaults.stringArray(forKey: orderKey(area)) ?? [])
        defaults.set(ids, forKey: orderKey(area))
        let catalog = Dictionary(uniqueKeysWithValues: Self.specs(area).map { ($0.id, $0) })
        return ids.compactMap { catalog[$0] }
    }
    public func isVisible(_ spec: TileSpec) -> Bool { spec.pinned || !hidden(spec.area).contains(spec.id) }
    public func setVisible(_ visible: Bool, for spec: TileSpec) {
        guard !spec.pinned, Self.specs(spec.area).contains(spec) else { return }
        var ids = hidden(spec.area)
        if visible { ids.remove(spec.id) } else { ids.insert(spec.id) }
        defaults.set(ids.sorted(), forKey: hiddenKey(spec.area))
    }
    public func move(area: TileArea, id: String, delta: Int) {
        defaults.set(Self.moveOrder(area: area, current: ordered(area).map(\.id), id: id, delta: delta), forKey: orderKey(area))
    }
    public func reset(_ area: TileArea) {
        defaults.removeObject(forKey: orderKey(area))
        defaults.removeObject(forKey: hiddenKey(area))
    }
    private func hidden(_ area: TileArea) -> Set<String> {
        let ids = Self.sanitizeHidden(area: area, requested: Set(defaults.stringArray(forKey: hiddenKey(area)) ?? []))
        defaults.set(ids.sorted(), forKey: hiddenKey(area))
        return ids
    }
    private func orderKey(_ area: TileArea) -> String { "pfvr.tiles.order.\(area.rawValue)" }
    private func hiddenKey(_ area: TileArea) -> String { "pfvr.tiles.hidden.\(area.rawValue)" }
}
