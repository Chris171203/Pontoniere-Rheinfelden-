import XCTest
@testable import PFVRCore

final class RiverDomainTests: XCTestCase {
    func testOfficialThresholdBoundariesIncludingMetresConversion() throws {
        let expected: [(Double, RhineNavigation.Stage)] = [(699.9, .normal), (700, .hwmI), (789.9, .hwmI), (790, .hwmIIb), (819.9, .hwmIIb), (820, .hwmIIa)]
        for (gauge, stage) in expected { XCTAssertEqual(RhineNavigation.stage(gaugeCentimetres: gauge), stage) }
        for (metres, stage) in [(247.0, RhineNavigation.Stage.hwmI), (247.9, .hwmIIb), (248.2, .hwmIIa)] {
            let cm = try XCTUnwrap(RiverDisplay.gaugeCentimetres(station: .baselRheinhalle, metresAboveSea: metres))
            XCTAssertEqual(RhineNavigation.stage(gaugeCentimetres: cm), stage)
        }
        XCTAssertEqual(RhineNavigation.stage(gaugeCentimetres: 699.999), .normal)
        XCTAssertEqual(RhineNavigation.stage(gaugeCentimetres: 789.999), .hwmI)
        XCTAssertEqual(RhineNavigation.stage(gaugeCentimetres: 819.999), .hwmIIb)
        XCTAssertEqual(RhineNavigation.stage(gaugeCentimetres: .nan), .unknown)
        XCTAssertEqual(RhineNavigation.stage(gaugeCentimetres: .infinity), .unknown)
        XCTAssertEqual(RhineNavigation.stage(gaugeCentimetres: nil), .unknown)
    }

    func testFreshnessRequiresBothMeasurementAndCacheWithinSixtyMinutes() {
        let now = Date(timeIntervalSince1970: 2_000_000_000)
        let limit = now.addingTimeInterval(-3600)
        XCTAssertTrue(RhineNavigation.isCurrent(measurement: limit, cacheUpdated: limit, now: now))
        for (measurement, cache) in [(limit.addingTimeInterval(-0.001), now), (now, limit.addingTimeInterval(-0.001)), (now.addingTimeInterval(0.001), now), (now, now.addingTimeInterval(0.001)), (Date(timeIntervalSince1970: 0), now), (now, Date(timeIntervalSince1970: -1))] {
            XCTAssertFalse(RhineNavigation.isCurrent(measurement: measurement, cacheUpdated: cache, now: now))
            XCTAssertEqual(RhineNavigation.currentStage(gaugeCentimetres: 820, measurement: measurement, cacheUpdated: cache, now: now), .unknown)
        }
        XCTAssertFalse(RhineNavigation.isCurrent(measurement: nil, cacheUpdated: now, now: now))
        XCTAssertEqual(RhineNavigation.currentStage(gaugeCentimetres: 790, measurement: now, cacheUpdated: now, now: now), .hwmIIb)
    }

    func testOnlyBaselHasVerifiedCentimetreDatum() throws {
        XCTAssertEqual(try XCTUnwrap(RiverDisplay.gaugeCentimetres(station: .baselRheinhalle, metresAboveSea: 247.2)), 720, accuracy: 0.000001)
        XCTAssertNil(RiverDisplay.gaugeCentimetres(station: .rheinfelden, metresAboveSea: 268.2))
        XCTAssertEqual(RiverDisplay.graphLevelValue(station: .rheinfelden, metresAboveSea: 268.2, centimetres: true), 268.2)
        XCTAssertEqual(RiverDisplay.graphLevelUnit(station: .rheinfelden, centimetres: true), "m ü.M.")
        XCTAssertEqual(RiverDisplay.graphLevelUnit(station: .baselRheinhalle, centimetres: true), "cm")
        XCTAssertEqual(RhineNavigation.thresholdGraphValue(stage: .hwmIIa, centimetres: false), 248.2)
        XCTAssertEqual(RhineNavigation.thresholdGraphValue(stage: .hwmIIb, centimetres: true), 790)
        XCTAssertNil(RhineNavigation.thresholdGraphValue(stage: .normal, centimetres: true))
    }

    func testAxisAndStatisticsIgnoreGapsAndInvalidNumbers() throws {
        let axis = HydroMath.niceAxis([nil, .nan, 501, 899, .infinity])
        XCTAssertEqual(axis.min, 500)
        XCTAssertEqual(axis.max, 900)
        XCTAssertEqual(axis.step, 100)
        let flat = HydroMath.niceAxis([5, 5])
        XCTAssertLessThan(flat.min, 5)
        XCTAssertGreaterThan(flat.max, 5)
        XCTAssertEqual(HydroMath.niceAxis([nil, .nan]).min, 0)
        let stats = try XCTUnwrap(HydroMath.stats([nil, 3, .infinity, 6, .nan, 9]))
        XCTAssertEqual(stats.count, 3)
        XCTAssertEqual(stats.mean, 6)
        XCTAssertEqual(stats.change, 6)
        XCTAssertNil(HydroMath.stats([nil, .nan]))
    }

    func testNearestSampleBoundsAndTie() {
        let times = [10.0, 20, 30].map { Date(timeIntervalSince1970: $0) }
        XCTAssertNil(HydroMath.nearestIndex(times: [], target: times[0]))
        XCTAssertEqual(HydroMath.nearestIndex(times: times, target: Date(timeIntervalSince1970: 0)), 0)
        XCTAssertEqual(HydroMath.nearestIndex(times: times, target: Date(timeIntervalSince1970: 15)), 0)
        XCTAssertEqual(HydroMath.nearestIndex(times: times, target: Date(timeIntervalSince1970: 21)), 1)
        XCTAssertEqual(HydroMath.nearestIndex(times: times, target: Date(timeIntervalSince1970: 100)), 2)
        XCTAssertEqual(HydroMath.periodSeconds("7d"), 604800)
        XCTAssertEqual(HydroMath.periodSeconds("1h"), 3600)
    }
}
