import Foundation

/// Parses public iCalendar, including recurrence exclusions and moved/cancelled occurrences.
/// Supported RRULEs: DAILY/WEEKLY/MONTHLY/YEARLY; INTERVAL/COUNT/UNTIL; BYDAY
/// (including monthly/yearly ordinal weekdays), BYMONTHDAY, BYMONTH and WKST.
/// Unsupported rule parts fail explicitly, so a cached correct calendar remains available.
public enum CalendarParser {
    private struct RawEvent {
        var uid = "", title = "", location = "", details = "", status = ""
        var start: Date?, end: Date?, recurrence: Date?
        var allDay = false
        var zone = PFVRDate.timeZone
        var rule = ""
        var excluded: Set<Date> = []
        var additional: [Date] = []
        func materialize(_ occurrence: Date, master: RawEvent? = nil) -> PFVREvent {
            let original = start ?? recurrence ?? occurrence
            let base = master ?? self
            var calendar = Calendar(identifier: .gregorian); calendar.timeZone = base.zone
            let dayEvent = master?.allDay ?? allDay
            let duration = end.map { $0.timeIntervalSince(original) } ?? base.end.flatMap { ending in base.start.map { ending.timeIntervalSince($0) } }
            let finish: Date
            if let duration, duration > 0 {
                // All-day durations are calendar days, not 24-hour blocks across DST.
                if dayEvent, let baseStart = base.start, let baseEnd = base.end {
                    let days = max(1, calendar.dateComponents([.day], from: baseStart, to: baseEnd).day ?? 1)
                    finish = calendar.date(byAdding: .day, value: days, to: occurrence)!
                } else { finish = occurrence.addingTimeInterval(duration) }
            } else { finish = dayEvent ? calendar.date(byAdding: .day, value: 1, to: occurrence)! : occurrence.addingTimeInterval(3600) }
            let label = title.isEmpty ? (master?.title ?? "") : title
            return PFVREvent(id: "\(uid.isEmpty ? label : uid)|\(occurrence.timeIntervalSince1970)", title: label.isEmpty ? "Vereinstermin" : label, location: location.isEmpty ? (master?.location ?? "") : location, details: details.isEmpty ? (master?.details ?? "") : details, status: status.isEmpty ? (master?.status ?? "") : status, start: occurrence, end: finish, allDay: dayEvent)
        }
    }
    public static func parse(_ data: Data, now: Date = Date(), months: Int = 14) throws -> [PFVREvent] {
        guard let raw = String(data: data, encoding: .utf8), raw.contains("BEGIN:VCALENDAR"), raw.contains("END:VCALENDAR") else { throw PFVRDataError.invalidPayload("Kalender: ungültiges iCalendar-Dokument") }
        var lines: [String] = []
        for line in raw.replacingOccurrences(of: "\r\n", with: "\n").replacingOccurrences(of: "\r", with: "\n").components(separatedBy: "\n") {
            if (line.hasPrefix(" ") || line.hasPrefix("\t")), !lines.isEmpty { lines[lines.count - 1] += line.dropFirst() }
            else { lines.append(line) }
        }
        var events: [RawEvent] = [], current: RawEvent?
        var nested = 0
        for line in lines {
            if line == "BEGIN:VEVENT" {
                guard current == nil else { throw PFVRDataError.invalidPayload("Kalender: verschachtelter VEVENT") }
                current = RawEvent(); nested = 0; continue
            }
            if line == "END:VEVENT" {
                guard let event = current, nested == 0, event.start != nil || event.recurrence != nil else { throw PFVRDataError.invalidPayload("Kalender: unvollständiger VEVENT") }
                events.append(event); current = nil; continue
            }
            guard current != nil else { continue }
            if line.hasPrefix("BEGIN:") { nested += 1; continue }
            if line.hasPrefix("END:") { nested = max(0, nested - 1); continue }
            guard nested == 0, let colon = line.firstIndex(of: ":") else { continue }
            let key = String(line[..<colon]), value = String(line[line.index(after: colon)...])
            let name = key.components(separatedBy: ";")[0].uppercased()
            switch name {
            case "UID": current?.uid = unescape(value)
            case "SUMMARY": current?.title = unescape(value)
            case "DESCRIPTION": current?.details = unescape(value)
            case "LOCATION": current?.location = unescape(value)
            case "STATUS": current?.status = value
            case "RRULE": current?.rule = value
            case "DTSTART":
                guard let parsed = parseDate(key: key, value: value) else { throw PFVRDataError.invalidPayload("Kalender: ungültiger Beginn oder Zeitzone") }
                current?.start = parsed.date; current?.allDay = parsed.allDay; current?.zone = parsed.zone
            case "DTEND":
                guard let parsed = parseDate(key: key, value: value) else { throw PFVRDataError.invalidPayload("Kalender: ungültiges Ende") }
                current?.end = parsed.date
            case "RECURRENCE-ID":
                if key.uppercased().contains("RANGE=THISANDFUTURE") { throw PFVRDataError.invalidPayload("Kalender: RANGE=THISANDFUTURE wird nicht unterstützt") }
                guard let parsed = parseDate(key: key, value: value) else { throw PFVRDataError.invalidPayload("Kalender: ungültige Wiederholungsausnahme") }
                current?.recurrence = parsed.date; if current?.start == nil { current?.allDay = parsed.allDay }
            case "EXDATE": for date in value.components(separatedBy: ",") {
                guard let parsed = parseDate(key: key, value: date) else { throw PFVRDataError.invalidPayload("Kalender: ungültiges EXDATE") }
                current?.excluded.insert(parsed.date)
            }
            case "RDATE": for date in value.components(separatedBy: ",") {
                guard let parsed = parseDate(key: key, value: date) else { throw PFVRDataError.invalidPayload("Kalender: ungültiges RDATE") }
                current?.additional.append(parsed.date)
            }
            default: break
            }
        }
        guard current == nil else { throw PFVRDataError.invalidPayload("Kalender: VEVENT nicht geschlossen") }
        let earliest = now.addingTimeInterval(-6 * 3600)
        let limit = PFVRDate.calendar.date(byAdding: .month, value: max(1,months), to: now)!
        var masters: [String: RawEvent] = [:], result: [String: PFVREvent] = [:]
        for event in events where event.recurrence == nil {
            guard let start = event.start else { continue }
            if !event.uid.isEmpty { masters[event.uid] = event }
            let dates = try occurrences(event, start: start, limit: limit) + event.additional
            for occurrence in dates where !event.excluded.contains(occurrence) {
                let entry = event.materialize(occurrence)
                result[entry.id] = entry
            }
        }
        for event in events where event.recurrence != nil {
            guard let original = event.recurrence else { continue }
            let master = masters[event.uid]
            // UID is required to avoid an exception removing an unrelated event at the same time.
            if !event.uid.isEmpty {
                let id = "\(event.uid)|\(original.timeIntervalSince1970)"
                result.removeValue(forKey: id)
            }
            let replacement = event.materialize(event.start ?? original, master: master)
            result[replacement.id] = replacement
        }
        return result.values.filter { $0.start <= limit && $0.end >= earliest }.sorted { $0.start == $1.start ? $0.id < $1.id : $0.start < $1.start }
    }
    private static func parseDate(key: String, value: String) -> (date: Date, allDay: Bool, zone: TimeZone)? {
        let allDay = key.uppercased().contains("VALUE=DATE") || value.count == 8
        var zone = PFVRDate.timeZone
        for parameter in key.components(separatedBy: ";").dropFirst() where parameter.uppercased().hasPrefix("TZID=") {
            guard let parsed = TimeZone(identifier: String(parameter.dropFirst(5)).replacingOccurrences(of: "\"", with: "")) else { return nil }
            zone = parsed
        }
        if value.hasSuffix("Z") { zone = TimeZone(secondsFromGMT: 0)! }
        let text = value.hasSuffix("Z") ? String(value.dropLast()) : value
        let format = allDay ? "yyyyMMdd" : text.count == 13 ? "yyyyMMdd'T'HHmm" : "yyyyMMdd'T'HHmmss"
        guard let date = PFVRDate.parseLocal(text, format: format, zone: zone) else { return nil }
        return (date, allDay, zone)
    }
    private static func unescape(_ value: String) -> String {
        var output = "", escaped = false
        for character in value {
            if escaped { output.append(character == "n" || character == "N" ? "\n" : character); escaped = false }
            else if character == "\\" { escaped = true }
            else { output.append(character) }
        }
        if escaped { output.append("\\") }
        return output
    }
    private static func occurrences(_ event: RawEvent, start: Date, limit: Date) throws -> [Date] {
        guard !event.rule.isEmpty else { return [start] }
        var rule: [String: String] = [:]
        for part in event.rule.uppercased().components(separatedBy: ";") {
            let pair = part.split(separator: "=", maxSplits: 1).map(String.init)
            guard pair.count == 2, rule[pair[0]] == nil else { throw PFVRDataError.invalidPayload("Kalender: ungültige RRULE") }
            rule[pair[0]] = pair[1]
        }
        let supported: Set<String> = ["FREQ","INTERVAL","COUNT","UNTIL","BYDAY","BYMONTHDAY","BYMONTH","WKST"]
        guard Set(rule.keys).isSubset(of: supported), let frequency = rule["FREQ"], ["DAILY","WEEKLY","MONTHLY","YEARLY"].contains(frequency) else { throw PFVRDataError.invalidPayload("Kalender: nicht unterstützte Wiederholungsregel") }
        guard let interval = Int(rule["INTERVAL"] ?? "1"), (1...10000).contains(interval),
              let count = Int(rule["COUNT"] ?? "100000"), (1...100000).contains(count) else { throw PFVRDataError.invalidPayload("Kalender: ungültiges Wiederholungsintervall oder COUNT") }
        var until = limit
        if let text = rule["UNTIL"] {
            guard let parsed = parseDate(key: "DTSTART", value: text) else { throw PFVRDataError.invalidPayload("Kalender: ungültiges UNTIL") }
            until = min(parsed.date,limit)
        }
        var calendar = Calendar(identifier: .gregorian); calendar.timeZone = event.zone
        let anchor = calendar.startOfDay(for: start)
        let original = calendar.dateComponents([.year,.month,.day,.hour,.minute,.second,.weekday], from: start)
        let weekdayNames = ["SU":1,"MO":2,"TU":3,"WE":4,"TH":5,"FR":6,"SA":7]
        guard let weekStart = weekdayNames[rule["WKST"] ?? "MO"] else { throw PFVRDataError.invalidPayload("Kalender: ungültiges WKST") }
        let anchorWeek = calendar.date(byAdding: .day, value: -((original.weekday! - weekStart + 7) % 7), to: anchor)!
        let byDays = (rule["BYDAY"] ?? "").split(separator: ",").map(String.init)
        let byMonth = (rule["BYMONTH"] ?? "").split(separator: ",").compactMap { Int($0) }
        let byMonthDay = (rule["BYMONTHDAY"] ?? "").split(separator: ",").compactMap { Int($0) }
        if let text = rule["BYMONTH"], text.split(separator: ",", omittingEmptySubsequences: false).count != byMonth.count || byMonth.contains(where: { !(1...12).contains($0) }) { throw PFVRDataError.invalidPayload("Kalender: ungültiges BYMONTH") }
        if let text = rule["BYMONTHDAY"], text.split(separator: ",", omittingEmptySubsequences: false).count != byMonthDay.count || byMonthDay.contains(where: { $0 == 0 || abs($0) > 31 }) { throw PFVRDataError.invalidPayload("Kalender: ungültiges BYMONTHDAY") }
        for token in byDays {
            guard token.count >= 2, weekdayNames[String(token.suffix(2))] != nil else { throw PFVRDataError.invalidPayload("Kalender: ungültiges BYDAY") }
            let prefix = token.dropLast(2)
            if !prefix.isEmpty {
                guard let ordinal = Int(prefix), ordinal != 0, abs(ordinal) <= 5, ["MONTHLY","YEARLY"].contains(frequency), frequency != "YEARLY" || !byMonth.isEmpty else { throw PFVRDataError.invalidPayload("Kalender: nicht unterstütztes ordinales BYDAY") }
            }
        }
        var output: [Date] = [], made = 0, day = anchor, iterations = 0
        while day <= until && made < count && iterations < 100000 {
            iterations += 1
            let parts = calendar.dateComponents([.year,.month,.day,.weekday], from: day)
            let elapsedDays = calendar.dateComponents([.day], from: anchor, to: day).day ?? 0
            let elapsedMonths = (parts.year! - original.year!) * 12 + parts.month! - original.month!
            let elapsedWeeks = (calendar.dateComponents([.day], from: anchorWeek, to: day).day ?? 0) / 7
            var matches: Bool
            switch frequency {
            case "DAILY": matches = elapsedDays % interval == 0
            case "WEEKLY": matches = elapsedWeeks % interval == 0 && (byDays.isEmpty ? parts.weekday == original.weekday : true)
            case "MONTHLY": matches = elapsedMonths % interval == 0 && (byDays.isEmpty && byMonthDay.isEmpty ? parts.day == original.day : true)
            default: matches = (parts.year! - original.year!) % interval == 0 && (byMonth.isEmpty ? parts.month == original.month : true) && (byDays.isEmpty && byMonthDay.isEmpty ? parts.day == original.day : true)
            }
            if !byMonth.isEmpty { matches = matches && byMonth.contains(parts.month!) }
            let daysInMonth = calendar.range(of: .day, in: .month, for: day)!.count
            if !byMonthDay.isEmpty { matches = matches && byMonthDay.contains { $0 > 0 ? $0 == parts.day! : daysInMonth + $0 + 1 == parts.day! } }
            if !byDays.isEmpty {
                matches = matches && byDays.contains { token in
                    guard token.count >= 2, weekdayNames[String(token.suffix(2))] == parts.weekday else { return false }
                    guard let ordinal = Int(token.dropLast(2)), ordinal != 0 else { return true }
                    return ordinal > 0 ? ((parts.day! - 1) / 7 + 1) == ordinal : -((daysInMonth - parts.day!) / 7 + 1) == ordinal
                }
            }
            if matches, let occurrence = calendar.date(bySettingHour: original.hour!, minute: original.minute!, second: original.second!, of: day), occurrence >= start, occurrence <= until {
                made += 1; output.append(occurrence)
            }
            guard let next = calendar.date(byAdding: .day, value: 1, to: day), next > day else { break }
            day = next
        }
        if iterations >= 100000 && day <= until { throw PFVRDataError.invalidPayload("Kalender: Wiederholungsregel überschreitet das Expansionslimit") }
        return output
    }
}
