import XCTest
@testable import PFVRCore

final class TrainingSeasonTests: XCTestCase {
    private func date(_ text: String) -> Date { PFVRDate.parseLocal(text)! }
    private func event(_ title: String, _ day: String, status: String = "") -> PFVREvent {
        let start = date(day + "T00:00")
        return PFVREvent(id: title, title: title, status: status, start: start,
                         end: PFVRDate.calendar.date(byAdding: .day, value: 1, to: start)!, allDay: true)
    }
    func testAutumnTriggersAndUnrelatedSummerEvents() {
        for title in ["Schiffe verladen", "Schiffe reinigen", "Abfahren", "Saisonabschluss", "Einwintern"] {
            XCTAssertEqual(CalendarPolicy.trainingSeason(events: [event(title,"2026-09-26")], day: date("2026-09-28T12:00")), .pause)
            XCTAssertEqual(CalendarPolicy.trainingSeason(events: [event(title,"2026-07-26")], day: date("2026-09-28T12:00")), .summer)
        }
        XCTAssertFalse(CalendarPolicy.isSummerEnd(event("Depot reinigen","2026-09-26")))
        XCTAssertEqual(CalendarPolicy.trainingSeason(events: [event("Schiffe verladen","2026-09-26")], day: date("2026-09-25T12:00")), .summer)
        XCTAssertEqual(CalendarPolicy.trainingSeason(events: [event("Schiffe verladen","2026-09-26", status: "CANCELLED")], day: date("2026-09-28T12:00")), .summer)
    }
    func testWinterAnchorYearBoundaryAndNextSeason() {
        let events = [event("Schiffe reinigen","2026-09-26"), event("Start Wintertraining","2026-10-22")]
        for (day, expected) in [("2026-10-01",CalendarPolicy.TrainingSeason.pause), ("2026-10-22",.winter), ("2027-01-07",.winter), ("2027-04-01",.summer), ("2027-09-28",.summer), ("2027-10-01",.pause)] {
            XCTAssertEqual(CalendarPolicy.trainingSeason(events: events, day: date(day+"T12:00")), expected)
        }
        XCTAssertNil(CalendarPolicy.nextRegularTraining(events: [], now: date("2026-10-01T12:00")))
        XCTAssertNil(CalendarPolicy.nextRegularTraining(events: [event("Start Wintertraining","2026-10-22",status:"CANCELLED")], now: date("2026-10-23T12:00")))
        XCTAssertEqual(CalendarPolicy.nextRegularTraining(events: events, now: date("2026-10-23T12:00"))?.start, date("2026-10-29T19:30"))
    }
    func testExplicitTrainingWinsDuringPause() {
        let events = [event("Schiffe verladen","2026-09-26"),event("Zusatztraining","2026-09-29")]
        XCTAssertEqual(CalendarPolicy.nextWeatherEvents(events: events, now: date("2026-09-28T12:00")).map(\.title), ["Zusatztraining"])
        XCTAssertEqual(CalendarPolicy.nextRegularTraining(events: events, now: date("2026-09-28T12:00"))?.title, "Zusatztraining")
    }
    func testPastAnchorsSurviveParsingAndCacheRoundTrip() throws {
        let raw = """
        BEGIN:VCALENDAR
        BEGIN:VEVENT
        UID:end
        SUMMARY:Schiffe verladen
        DTSTART;VALUE=DATE:20260926
        DTEND;VALUE=DATE:20260927
        END:VEVENT
        BEGIN:VEVENT
        UID:start
        SUMMARY:Start Wintertraining
        DTSTART;TZID=Europe/Zurich:20261022T193000
        DTEND;TZID=Europe/Zurich:20261022T210000
        END:VEVENT
        END:VCALENDAR
        """
        let events = try CalendarParser.parse(Data(raw.utf8), now: date("2026-09-28T12:00"))
        XCTAssertEqual(events.count,2)
        XCTAssertTrue(CalendarPolicy.nextWeatherEvents(events: events, now: date("2026-09-28T12:00")).isEmpty)
        let january = try CalendarParser.parse(Data(raw.utf8), now: date("2027-01-04T12:00"))
        let restored = try JSONDecoder().decode([PFVREvent].self, from: JSONEncoder().encode(january))
        XCTAssertEqual(CalendarPolicy.nextRegularTraining(events: restored, now: date("2027-01-04T12:00"))?.start,date("2027-01-07T19:30"))
    }
}
