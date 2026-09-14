import XCTest
@testable import PFVRCore

final class TileLayoutTests: XCTestCase {
    func testUpdateMigrationDropsRemovedJoinTileAndInsertsThreeDayWeatherOnce() {
        let home = TileLayoutStore.normalizeOrder(area: .home, requested: ["home_events", "home_weather", "home_weather", "old_id"])
        XCTAssertEqual(Array(home.prefix(3)), ["home_events", "home_weather", "home_weather_3day"])
        XCTAssertEqual(Set(home).count, home.count)
        XCTAssertFalse(home.contains("old_id"))
        let reordered = TileLayoutStore.normalizeOrder(area: .home, requested: ["home_weather_3day", "home_events", "home_weather"])
        XCTAssertEqual(Array(reordered.prefix(3)), ["home_weather_3day", "home_events", "home_weather"])
        XCTAssertFalse(TileLayoutStore.normalizeOrder(area: .club, requested: ["club_join", "club_news"]).contains("club_join"))
    }
    func testCartCannotBeHiddenMovedOrDisplaced() {
        let order = TileLayoutStore.normalizeOrder(area: .cash, requested: ["cash_food", "cash_cart", "cash_drinks"])
        XCTAssertEqual(order.first, "cash_cart")
        XCTAssertEqual(TileLayoutStore.moveOrder(area: .cash, current: order, id: "cash_cart", delta: 1), order)
        XCTAssertEqual(TileLayoutStore.moveOrder(area: .cash, current: order, id: "cash_food", delta: -1), order)
        XCTAssertEqual(TileLayoutStore.sanitizeHidden(area: .cash, requested: ["cash_cart", "cash_food", "old"]), ["cash_food"])
        XCTAssertEqual(TileLayoutStore.moveOrder(area: .cash, current: order, id: "cash_drinks", delta: -99)[1], "cash_drinks")
    }
    func testVisibilityAndReorderPersistAndResetOnlyRequestedArea() throws {
        let suite = "PFVR.tests.tiles.\(UUID().uuidString)"
        let defaults = try XCTUnwrap(UserDefaults(suiteName: suite))
        defer { defaults.removePersistentDomain(forName: suite) }
        let first = TileLayoutStore(defaults: defaults)
        let weather = try XCTUnwrap(first.ordered(.home).first)
        let food = try XCTUnwrap(first.ordered(.cash).first { $0.id == "cash_food" })
        first.setVisible(false, for: weather)
        first.setVisible(false, for: food)
        first.move(area: .home, id: "home_weather", delta: 1)
        let restored = TileLayoutStore(defaults: defaults)
        XCTAssertFalse(restored.isVisible(weather))
        XCTAssertEqual(restored.ordered(.home).first?.id, "home_weather_3day")
        restored.reset(.home)
        XCTAssertTrue(restored.isVisible(weather))
        XCTAssertFalse(restored.isVisible(food))
        XCTAssertEqual(restored.ordered(.home).first?.id, "home_weather")
    }
}
