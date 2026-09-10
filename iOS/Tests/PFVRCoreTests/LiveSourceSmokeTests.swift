import Foundation
import XCTest
@testable import PFVRCore

/// Opt-in reads of the actual public APIs. These never access the personal attendance website.
/// Kept separate from deterministic fixtures so an external outage cannot masquerade as a logic regression.
final class LiveSourceSmokeTests: XCTestCase {
    private var directory: URL!

    override func setUpWithError() throws {
        guard ProcessInfo.processInfo.environment["PFVR_LIVE_SMOKE"] == "1" else {
            throw XCTSkip("Live public source checks run only in the explicit PFVR_LIVE_SMOKE CI step.")
        }
        directory = FileManager.default.temporaryDirectory.appendingPathComponent("pfvr-live-" + UUID().uuidString)
    }

    override func tearDownWithError() throws {
        if let directory { try? FileManager.default.removeItem(at: directory) }
    }

    func testPublicWeatherProducesUsableHours() async throws {
        let service = PFVRDataService(cacheDirectory: directory)
        let result = try await service.weather(force: true)
        XCTAssertFalse(result.metadata.isStale)
        XCTAssertGreaterThan(result.value.filter { $0.temperature != nil }.count, 24)
        XCTAssertTrue(result.value.contains { $0.time > Date() }, "Live forecast must contain future hours")
        print("PUBLIC-SMOKE weather: \(result.value.count) hours; source=\(result.metadata.source)")
    }

    func testPublicCalendarCanBeParsed() async throws {
        let service = PFVRDataService(cacheDirectory: directory)
        let result = try await service.events(force: true)
        XCTAssertFalse(result.metadata.isStale)
        XCTAssertTrue(result.value.allSatisfy { $0.end >= $0.start })
        // A valid empty calendar is allowed; the parser must independently validate VCALENDAR.
        print("PUBLIC-SMOKE calendar: \(result.value.count) expanded events")
    }

    func testPublicNewsCanBeParsed() async throws {
        let service = PFVRDataService(cacheDirectory: directory)
        let result = try await service.news(force: true)
        XCTAssertFalse(result.metadata.isStale)
        XCTAssertTrue(result.value.allSatisfy { !$0.title.isEmpty && $0.url.scheme == "https" })
        print("PUBLIC-SMOKE news: \(result.value.count) articles")
    }

    func testBothBAFUStationsReturnCurrentAndHistoricalSeries() async throws {
        let service = PFVRDataService(cacheDirectory: directory)
        for station in HydroStation.allCases {
            let result = await service.hydro(station: station, force: true)
            XCTAssertNotNil(result.latest(parameter: "W"), "No level at \(station.rawValue): \(result.failures)")
            XCTAssertNotNil(result.latest(parameter: "Q"), "No flow at \(station.rawValue): \(result.failures)")
            XCTAssertGreaterThan(result.fine?.value.count ?? 0, 1, "No 10-minute series at \(station.rawValue)")
            XCTAssertGreaterThan(result.history?.value.count ?? 0, 1, "No hourly series at \(station.rawValue)")
            if station.supportsTemperature { XCTAssertNotNil(result.latest(parameter: "WT")) }
            print("PUBLIC-SMOKE BAFU \(station.rawValue): live=\(result.live?.value.count ?? 0), fine=\(result.fine?.value.count ?? 0), history=\(result.history?.value.count ?? 0)")
        }
    }
}
