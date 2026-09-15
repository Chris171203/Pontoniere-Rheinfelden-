import XCTest
@testable import PFVRCore

final class EventWeatherDayTests: XCTestCase {
    private func date(_ value: String) -> Date { PFVRDate.parseLocal(value)! }
    private func event(_ id: String, _ start: String, _ end: String, title: String = "Anlass", allDay: Bool = false) -> PFVREvent {
        PFVREvent(id: id, title: title, start: date(start), end: date(end), allDay: allDay)
    }
    func testMiddayAndEveningBothAppearChronologically() {
        let noon = event("noon", "2026-09-15T12:30", "2026-09-15T14:00")
        let evening = event("evening", "2026-09-15T18:30", "2026-09-15T20:00")
        let tomorrow = event("tomorrow", "2026-09-16T08:00", "2026-09-16T10:00")
        XCTAssertEqual(CalendarPolicy.nextWeatherEvents(events: [tomorrow,evening,noon], now: date("2026-09-15T09:00")).map(\.id), ["noon","evening"])
        XCTAssertEqual(CalendarPolicy.nextWeatherEvents(events: [noon,evening], now: date("2026-09-15T13:00")).map(\.id), ["noon","evening"])
        XCTAssertEqual(CalendarPolicy.nextWeatherEvents(events: [noon,evening], now: date("2026-09-15T14:00")).map(\.id), ["evening"])
    }
    func testOnlyEveningAndCancelledMidday() {
        let evening = event("evening", "2026-09-15T18:30", "2026-09-15T20:00")
        let cancelled = event("cancel", "2026-09-15T12:00", "2026-09-15T14:00", title: "Anlass abgesagt")
        XCTAssertEqual(CalendarPolicy.nextWeatherEvents(events: [evening], now: date("2026-09-15T09:00")).map(\.id), ["evening"])
        XCTAssertEqual(CalendarPolicy.nextWeatherEvents(events: [cancelled,evening], now: date("2026-09-15T09:00")).map(\.id), ["evening"])
    }
    func testRegularTrainingAddedOnceAndExplicitTrainingNotDuplicated() {
        let noon = event("noon", "2026-09-16T12:30", "2026-09-16T14:00")
        let explicit = event("training", "2026-09-16T18:30", "2026-09-16T20:00", title: "Vereinstraining")
        let fallback = CalendarPolicy.nextWeatherEvents(events: [noon], now: date("2026-09-16T09:00"))
        XCTAssertEqual(fallback.count, 2); XCTAssertFalse(fallback[1].fromCalendar)
        XCTAssertEqual(CalendarPolicy.nextWeatherEvents(events: [explicit,noon], now: date("2026-09-16T09:00")).map(\.id), ["noon","training"])
        let allDay = event("all", "2026-09-16T00:00", "2026-09-17T00:00", title: "Vereinstraining", allDay: true)
        let selected = CalendarPolicy.nextWeatherEvents(events: [allDay], now: date("2026-09-16T09:00"))
        XCTAssertEqual(selected.count, 1); XCTAssertTrue(selected[0].allDay)
    }
    func testRunningMultiDayEventUsesTodayAndIncludesTodaysOtherEvents() {
        let running = event("running", "2026-09-14T00:00", "2026-09-17T00:00", allDay: true)
        let evening = event("evening", "2026-09-15T18:30", "2026-09-15T20:00")
        let selected = CalendarPolicy.nextWeatherEvents(events: [running,evening], now: date("2026-09-15T09:00"))
        XCTAssertEqual(selected.map(\.id), ["running","evening"])
        XCTAssertEqual(selected[0].start, date("2026-09-15T00:00"))
        XCTAssertEqual(selected[0].end, date("2026-09-16T00:00"))
    }
    func testSimultaneousDistinctEventsRemainAndCompletedDayAdvances() {
        let a = event("a", "2026-09-15T18:00", "2026-09-15T20:00")
        let b = event("b", "2026-09-15T18:00", "2026-09-15T21:00")
        XCTAssertEqual(CalendarPolicy.nextWeatherEvents(events: [b,a], now: date("2026-09-15T09:00")).map(\.id), ["a","b"])
        XCTAssertEqual(CalendarPolicy.nextWeatherEvents(events: [a,b], now: date("2026-09-15T21:00")).first?.start, date("2026-09-16T18:30"))
    }
    func testEachForecastUsesOnlyItsOwnHoursIncludingLongEvening() {
        let start = date("2026-09-15T00:00")
        let hours = (0..<24).map { WeatherHour(time: start.addingTimeInterval(Double($0)*3600), temperature: Double($0), precipitation: Double($0), wind: Double($0), uv: Double($0)) }
        let noon = WeatherForecast.event(hours: hours, event: event("noon", "2026-09-15T12:30", "2026-09-15T14:00"))
        let evening = WeatherForecast.event(hours: hours, event: event("evening", "2026-09-15T18:30", "2026-09-15T20:00"))
        XCTAssertEqual(noon.hours.compactMap(\.temperature), [12,13]); XCTAssertEqual(noon.summary.precipitationSum, 25)
        XCTAssertEqual(evening.hours.compactMap(\.temperature), [18,19]); XCTAssertEqual(evening.summary.precipitationSum, 37)
        let long = WeatherForecast.event(hours: hours, event: event("long", "2026-09-15T18:00", "2026-09-16T00:00"))
        XCTAssertEqual(long.slots.map { PFVRDate.calendar.component(.hour, from: $0.target) }, [18,21,23])
        XCTAssertEqual(long.summary.minTemperature, 18); XCTAssertEqual(long.summary.maxTemperature, 23)
        XCTAssertEqual(long.summary.precipitationSum, 123)
        let missing = WeatherForecast.event(hours: Array(hours.prefix(14)), event: event("missing", "2026-09-15T18:30", "2026-09-15T20:00"))
        XCTAssertEqual(missing.summary.count, 0)
    }
    func testSharedForecastDeduplicatesSameTimeAndOverlapWithoutIncludingGap() throws {
        let start = date("2026-09-15T00:00")
        let hours = (0..<24).map { WeatherHour(time: start.addingTimeInterval(Double($0)*3600), temperature: Double($0), precipitation: 1) }
        let noon = event("noon", "2026-09-15T12:30", "2026-09-15T14:00")
        let evening = event("a", "2026-09-15T18:30", "2026-09-15T20:00")
        let same = event("b", "2026-09-15T18:30", "2026-09-15T20:00")
        let identical = try XCTUnwrap(WeatherForecast.events(hours: hours, events: [evening,same]))
        XCTAssertEqual(identical.count, 2); XCTAssertEqual(identical.precipitationSum, 2)
        XCTAssertTrue(identical.slots.isEmpty)
        let overlap = event("c", "2026-09-15T19:00", "2026-09-15T21:00")
        let shared = try XCTUnwrap(WeatherForecast.events(hours: hours, events: [same,noon,overlap,evening]))
        XCTAssertEqual(shared.count, 5); XCTAssertEqual(shared.precipitationSum, 5)
        XCTAssertEqual(shared.slots.map { PFVRDate.calendar.component(.hour, from: $0.target) }, [12,18])
        XCTAssertEqual(shared.minTemperature, 12); XCTAssertEqual(shared.maxTemperature, 20)
        let all = event("all", "2026-09-15T00:00", "2026-09-16T00:00", allDay: true)
        let wholeDay = try XCTUnwrap(WeatherForecast.events(hours: hours, events: [all,evening]))
        XCTAssertEqual(wholeDay.count, 24); XCTAssertEqual(wholeDay.precipitationSum, 24)
        XCTAssertEqual(wholeDay.slots.count, 3)
        XCTAssertNil(WeatherForecast.events(hours: hours, events: []))
    }
}
