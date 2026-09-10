import XCTest
@testable import PFVRCore

final class DataParserTests: XCTestCase {
    private func date(_ text: String) -> Date { PFVRDate.parseLocal(text)! }
    private func data(_ value: String) -> Data { Data(value.utf8) }
    private func ics(_ events: String) -> Data { data("BEGIN:VCALENDAR\nVERSION:2.0\n\(events)\nEND:VCALENDAR") }
    func testCalendarFoldingEscapesAndUTC() throws {
        let parsed = try CalendarParser.parse(ics("""
        BEGIN:VEVENT
        UID:one
        DTSTART:20260910T160000Z
        DTEND:20260910T173000Z
        SUMMARY:Wettfahren\\, Rheinfel
         den
        DESCRIPTION:Zeile 1\\nZeile 2\\; bereit
        LOCATION:Rheinweg 42
        END:VEVENT
        """), now: date("2026-09-10T10:00"))
        XCTAssertEqual(parsed.count, 1)
        XCTAssertEqual(parsed[0].title, "Wettfahren, Rheinfelden")
        XCTAssertEqual(parsed[0].details, "Zeile 1\nZeile 2; bereit")
        XCTAssertEqual(parsed[0].start, date("2026-09-10T18:00"))
        XCTAssertEqual(parsed[0].end, date("2026-09-10T19:30"))
    }
    func testRecurrenceExcludedMovedAndCancelled() throws {
        let parsed = try CalendarParser.parse(ics("""
        BEGIN:VEVENT
        UID:weekly
        DTSTART;TZID=Europe/Zurich:20260907T183000
        DTEND;TZID=Europe/Zurich:20260907T200000
        SUMMARY:Training
        RRULE:FREQ=WEEKLY;BYDAY=MO,WE;COUNT=6
        EXDATE;TZID=Europe/Zurich:20260909T183000
        END:VEVENT
        BEGIN:VEVENT
        UID:weekly
        RECURRENCE-ID;TZID=Europe/Zurich:20260914T183000
        DTSTART;TZID=Europe/Zurich:20260915T190000
        SUMMARY:Verschobenes Training
        END:VEVENT
        BEGIN:VEVENT
        UID:weekly
        RECURRENCE-ID;TZID=Europe/Zurich:20260916T183000
        STATUS:CANCELLED
        END:VEVENT
        """), now: date("2026-09-07T00:00"))
        XCTAssertEqual(parsed.count, 5)
        XCTAssertFalse(parsed.contains { $0.start == date("2026-09-09T18:30") || $0.start == date("2026-09-14T18:30") })
        let moved = try XCTUnwrap(parsed.first { $0.title == "Verschobenes Training" })
        XCTAssertEqual(moved.start, date("2026-09-15T19:00"))
        XCTAssertEqual(moved.end, date("2026-09-15T20:30"))
        XCTAssertTrue(try XCTUnwrap(parsed.first { $0.start == date("2026-09-16T18:30") }).isCancelled)
    }
    func testExplicitDateTimeValueIsNotAllDay() throws {
        let parsed = try CalendarParser.parse(ics("""
        BEGIN:VEVENT
        UID:timed
        DTSTART;VALUE=DATE-TIME;TZID=Europe/Zurich:20260910T183000
        SUMMARY:Training
        END:VEVENT
        """), now: date("2026-09-10T12:00"))
        XCTAssertFalse(parsed[0].allDay)
        XCTAssertEqual(parsed[0].start, date("2026-09-10T18:30"))
    }
    func testRecurrenceKeepsLocalHourAcrossDST() throws {
        let parsed = try CalendarParser.parse(ics("""
        BEGIN:VEVENT
        UID:dst
        DTSTART;TZID=Europe/Zurich:20261022T193000
        DTEND;TZID=Europe/Zurich:20261022T210000
        SUMMARY:Wintertraining
        RRULE:FREQ=WEEKLY;COUNT=2
        END:VEVENT
        """), now: date("2026-10-22T12:00"))
        XCTAssertEqual(parsed.map { PFVRDate.calendar.component(.hour, from: $0.start) }, [19,19])
        XCTAssertEqual(parsed[1].start.timeIntervalSince(parsed[0].start), 7 * 86400 + 3600)
    }
    func testAllDayExclusiveEndAcrossDST() throws {
        let parsed = try CalendarParser.parse(ics("""
        BEGIN:VEVENT
        UID:day
        DTSTART;VALUE=DATE:20261025
        DTEND;VALUE=DATE:20261026
        SUMMARY:Wanderung
        END:VEVENT
        """), now: date("2026-10-25T00:00"))
        XCTAssertTrue(parsed[0].allDay)
        XCTAssertEqual(parsed[0].end, date("2026-10-26T00:00"))
        XCTAssertEqual(parsed[0].end.timeIntervalSince(parsed[0].start), 25 * 3600)
    }
    func testMonthlyOrdinalAndAdditionalDate() throws {
        let parsed = try CalendarParser.parse(ics("""
        BEGIN:VEVENT
        UID:monthly
        DTSTART;TZID=Europe/Zurich:20260907T183000
        SUMMARY:Material
        RRULE:FREQ=MONTHLY;BYDAY=1MO;COUNT=3
        RDATE;TZID=Europe/Zurich:20260908T183000
        END:VEVENT
        """), now: date("2026-09-01T00:00"))
        XCTAssertEqual(parsed.map(\.start), [date("2026-09-07T18:30"),date("2026-09-08T18:30"),date("2026-10-05T18:30"),date("2026-11-02T18:30")])
    }
    func testYearlySelectorsWithoutMonthExpandAcrossMonths() throws {
        let parsed = try CalendarParser.parse(ics("""
        BEGIN:VEVENT
        UID:monthly-in-year
        DTSTART;TZID=Europe/Zurich:20260910T183000
        SUMMARY:Treffen
        RRULE:FREQ=YEARLY;BYMONTHDAY=10;COUNT=3
        END:VEVENT
        """), now: date("2026-09-01T00:00"))
        XCTAssertEqual(parsed.map(\.start), [date("2026-09-10T18:30"), date("2026-10-10T18:30"), date("2026-11-10T18:30")])
    }
    func testMalformedCalendarRejectedButRealEmptyAccepted() throws {
        XCTAssertEqual(try CalendarParser.parse(ics("")), [])
        for malformed in [
            "BEGIN:VEVENT\nDTSTART:not-a-date\nEND:VEVENT",
            "BEGIN:VEVENT\nDTSTART:20260910T120000Z",
            "BEGIN:VEVENT\nDTSTART;TZID=Imaginary/Moon:20260910T120000\nEND:VEVENT",
            "BEGIN:VEVENT\nDTSTART:20260910T120000Z\nRRULE:FREQ=WEEKLY;INTERVAL=oops\nEND:VEVENT",
            "BEGIN:VEVENT\nDTSTART:20260910T120000Z\nRRULE:FREQ=MONTHLY;BYSETPOS=1\nEND:VEVENT",
            "BEGIN:VEVENT\nDTSTART:20260910T120000Z\nRRULE:FREQ=MONTHLY;BYMONTHDAY=-9223372036854775808\nEND:VEVENT",
            "BEGIN:VEVENT\nDTSTART:20260910T120000Z\nRRULE:FREQ=MONTHLY;BYDAY=-9223372036854775808MO\nEND:VEVENT"
        ] { XCTAssertThrowsError(try CalendarParser.parse(ics(malformed), now: date("2026-09-01T00:00")), malformed) }
        XCTAssertThrowsError(try CalendarParser.parse(data("<html>maintenance</html>")))
    }
    func testCalendarEventPrecedesTrainingRegardlessOfTitle() throws {
        let now = date("2026-09-10T12:00")
        let event = PFVREvent(id: "exam", title: "JP-Prüfung", start: date("2026-09-12T08:00"), end: date("2026-09-12T18:00"))
        XCTAssertEqual(CalendarPolicy.nextWeatherEvent(events: [event], now: now)?.id, "exam")
        let later = PFVREvent(id: "later", title: "Wanderung", start: date("2026-09-20T08:00"), end: date("2026-09-20T18:00"))
        XCTAssertEqual(CalendarPolicy.nextWeatherEvent(events: [later], now: now)?.start, date("2026-09-14T18:30"))
    }
    func testRunningCalendarAndCancelledTrainingPolicy() throws {
        let running = PFVREvent(id: "run", title: "Wettfahren", start: date("2026-09-10T08:00"), end: date("2026-09-10T18:00"))
        XCTAssertEqual(CalendarPolicy.nextWeatherEvent(events: [running], now: date("2026-09-10T12:00"))?.id, "run")
        let cancelled = PFVREvent(id: "cancel", title: "Training fällt aus", start: date("2026-09-14T18:30"), end: date("2026-09-14T20:00"))
        XCTAssertEqual(CalendarPolicy.nextRegularTraining(events: [cancelled], now: date("2026-09-14T12:00"))?.start, date("2026-09-16T18:30"))
        XCTAssertEqual(CalendarPolicy.nextRegularTraining(events: [], now: date("2026-10-01T12:00"))?.start, date("2026-10-01T19:30"))
    }
    func testForecastDaysUse06Noon18AndNullStaysMissing() throws {
        let hours = try WeatherForecast.parse(data("""
        {"hourly":{"time":["2026-09-10T06:00","2026-09-10T12:00","2026-09-10T18:00"],"temperature_2m":[10,22,17],"precipitation_probability":[-1,70,150],"precipitation":[0,2.5,null],"wind_speed_10m":[5,8,3],"wind_gusts_10m":[10,15,6],"uv_index":[null,null,null],"weather_code":[0,3,2]}}
        """))
        let days = WeatherForecast.days(hours: hours, firstDay: date("2026-09-10T23:00"))
        XCTAssertEqual(days.count, 3)
        XCTAssertEqual(days[0].slots.map { PFVRDate.calendar.component(.hour, from: $0.target) }, [6,12,18])
        XCTAssertEqual(days[0].slots.compactMap { $0.hour?.temperature }, [10,22,17])
        XCTAssertEqual(days[0].minTemperature, 10); XCTAssertEqual(days[0].maxTemperature, 22)
        XCTAssertEqual(days[0].precipitationProbabilityMax, 100); XCTAssertEqual(days[0].precipitationSum, 2.5)
        XCTAssertNil(days[0].uvMax)
        XCTAssertEqual(days[1].count, 0); XCTAssertNil(days[1].minTemperature); XCTAssertNil(days[1].precipitationSum)
        XCTAssertTrue(days[1].slots.allSatisfy { $0.hour == nil })
    }
    func testForecastShortIntervalAndLongThreePoints() {
        let hours = (0..<24).map { WeatherHour(time: date("2026-09-10T00:00").addingTimeInterval(Double($0)*3600), temperature: Double($0)) }
        let short = PFVREvent(id: "short", title: "Training", start: date("2026-09-10T18:30"), end: date("2026-09-10T20:00"))
        let forecast = WeatherForecast.event(hours: hours, event: short)
        XCTAssertEqual(forecast.hours.compactMap(\.temperature), [18,19]); XCTAssertTrue(forecast.slots.isEmpty)
        let long = PFVREvent(id: "long", title: "Fest", start: date("2026-09-10T08:30"), end: date("2026-09-10T18:00"))
        XCTAssertEqual(WeatherForecast.event(hours: hours, event: long).slots.map { PFVRDate.calendar.component(.hour, from: $0.target) }, [8,13,18])
        let allDay = PFVREvent(id: "all", title: "Wanderung", start: date("2026-09-10T00:00"), end: date("2026-09-11T00:00"), allDay: true)
        XCTAssertEqual(WeatherForecast.event(hours: hours, event: allDay).slots.map { PFVRDate.calendar.component(.hour, from: $0.target) }, [6,12,18])
    }
    func testForecastMissingSlotAndUVAlignment() throws {
        let first = WeatherHour(time: date("2026-09-10T06:00"), temperature: 15)
        let second = WeatherHour(time: date("2026-09-10T12:00"), temperature: 22, uv: 2)
        let merged = WeatherForecast.supplementUV([first,second], from: [WeatherHour(time: second.time, uv: 8), WeatherHour(time: first.time, uv: 1.5)])
        XCTAssertEqual(merged.map(\.uv), [1.5,2])
        XCTAssertNil(WeatherForecast.slots(hours: [first], targets: [date("2026-09-10T12:00")])[0].hour)
        XCTAssertThrowsError(try WeatherForecast.parse(data("{\"hourly\":{\"time\":[\"2026-09-10T12:00\"],\"temperature_2m\":[true]}}")))
    }
    func testNewsEntityDecodingAndInvalidInput() throws {
        let parsed = try NewsParser.parse(data("""
        [{"id":4,"date":"2026-09-10T12:00:00","link":"https://www.pfvr.ch/news/","title":{"rendered":"Wettfahren &amp; &#xDC;bung"},"excerpt":{"rendered":"<p>Original&nbsp;Text &#8211; gut.</p><script>bad()</script>"}}]
        """))
        XCTAssertEqual(parsed[0].title,"Wettfahren & Übung")
        XCTAssertEqual(parsed[0].excerpt,"Original Text – gut.")
        XCTAssertEqual(parsed[0].publishedAt,date("2026-09-10T12:00"))
        XCTAssertThrowsError(try NewsParser.parse(data("[{}]")))
        XCTAssertEqual(try NewsParser.parse(data("[]")),[])
    }
    func testHydroFiltersWrongStationAndPreservesRawUnits() throws {
        let parsed = try LiveHydroParser.parse(data("""
        {"data":{"water":{"observations":{"data_live":[{"stationNo":"2289","parameterName":"W","timestamp":"2026-09-10T12:00:00Z","value":247.3},{"stationNo":"2091","parameterName":"Q","timestamp":"2026-09-10T12:00:00Z","value":1200},{"stationNo":"2289","parameterName":"Q","timestamp":"2026-09-10T12:00:00Z","value":true}]}}}}
        """), arrayName: "data_live", station: .baselRheinhalle)
        XCTAssertEqual(parsed.count,1); XCTAssertEqual(parsed[0].value,247.3); XCTAssertEqual(parsed[0].parameter,"W")
        XCTAssertThrowsError(try LiveHydroParser.parse(data("{\"errors\":[{\"message\":\"bad\"}]}"), arrayName:"data_live"))
    }
    func testHydroSeriesMergesTailDeduplicatesAndNeverUsesHistoryAsCurrent() {
        let start = date("2026-09-10T00:00")
        let historical = [HydroObservation(time: start, value: 1000, parameter: "Q"),HydroObservation(time: start.addingTimeInterval(3600), value: 1010, parameter: "Q")]
        let live = [HydroObservation(time: start.addingTimeInterval(3600), value: 1012, parameter: "Q"),HydroObservation(time: start.addingTimeInterval(7200), value: 1020, parameter: "Q")]
        let meta = CacheMetadata(source:"BAFU 2091",updatedAt:start)
        var dataset = HydroDataset(station: .rheinfelden, fine: Loaded(value:historical,metadata:meta),history:Loaded(value:historical,metadata:meta))
        XCTAssertNil(dataset.latest(parameter:"Q"))
        dataset.live = Loaded(value:live,metadata:meta)
        XCTAssertEqual(dataset.latest(parameter:"Q")?.value,1020)
        XCTAssertEqual(dataset.series(parameter:"Q",range:.day).count,3)
        XCTAssertEqual(dataset.series(parameter:"Q",range:.week).last?.value,1020)
    }
}
