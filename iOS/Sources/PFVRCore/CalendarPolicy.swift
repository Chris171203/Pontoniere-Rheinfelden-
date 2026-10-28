import Foundation

public enum CalendarPolicy {
    public static func normalize(_ value: String) -> String {
        value.folding(options: [.diacriticInsensitive, .caseInsensitive], locale: Locale(identifier: "de_CH"))
            .split(whereSeparator: { $0.isWhitespace }).joined(separator: " ")
    }
    public static func isCancelled(status: String, title: String, details: String = "") -> Bool {
        if status.uppercased() == "CANCELLED" { return true }
        let text = normalize(title + " " + details)
        return ["abgesagt", "kein training", "fallt aus", "entfallt", "annulliert"].contains { text.contains($0) }
    }
    public static func trainingScore(_ event: PFVREvent) -> Int {
        let text = normalize(event.title + " " + event.details)
        var score = 0
        if ["training", "fahrubung", "jungpontonier", "pontonierkurs", "wintertraining"].contains(where: text.contains) { score += 8 }
        if ["schiff", "hindernis", "parcours", "auswasser", "einwasser", "reinig", "material", "depot"].contains(where: text.contains) { score += 4 }
        if !event.allDay && (16...22).contains(PFVRDate.calendar.component(.hour, from: event.start)) { score += 2 }
        if ["pensionar", "versammlung", "sitzung", "vorstand", "jass", "jubilar"].contains(where: text.contains) { score -= 10 }
        return score
    }
    public enum TrainingSeason { case summer, winter, pause }
    public static func isSummerEnd(_ event: PFVREvent) -> Bool {
        guard (9...10).contains(PFVRDate.calendar.component(.month, from: event.start)) else { return false }
        let text = normalize(event.title)
        return ["abfahren", "saisonabschluss", "einwintern"].contains(where: text.contains)
            || (text.contains("schiff") && ["verlad", "reinig"].contains(where: text.contains))
    }
    public static func isWinterStart(_ event: PFVREvent) -> Bool {
        let text = normalize(event.title)
        return text.contains("wintertraining") && ["start", "beginn", "auftakt", "erstes", "1. wintertraining"].contains(where: text.contains)
    }
    public static func isSeasonAnchor(_ event: PFVREvent) -> Bool { isSummerEnd(event) || isWinterStart(event) }
    public static func trainingSeason(events: [PFVREvent], day: Date) -> TrainingSeason {
        let calendar = PFVRDate.calendar
        let month = calendar.component(.month, from: day), year = calendar.component(.year, from: day)
        let date = calendar.startOfDay(for: day)
        let winterFloor = calendar.date(from: DateComponents(year: month <= 3 ? year - 1 : year, month: 9, day: 1))!
        let effective = events.filter { !$0.isCancelled && calendar.startOfDay(for: $0.start) <= date }
        if (month >= 9 || month <= 3), effective.contains(where: { isWinterStart($0) && $0.start >= winterFloor }) { return .winter }
        if (4...9).contains(month), !effective.contains(where: { calendar.component(.year, from: $0.start) == year && isSummerEnd($0) }) { return .summer }
        return .pause
    }
    public static func nextWeatherEvent(events: [PFVREvent], now: Date = Date()) -> PFVREvent? {
        nextWeatherEvents(events: events, now: now).first
    }
    public static func nextWeatherEvents(events: [PFVREvent], now: Date = Date()) -> [PFVREvent] {
        let calendar = PFVRDate.calendar
        let limit = calendar.date(byAdding: .day, value: 21, to: now)!
        var candidates = events.filter { !$0.isCancelled && $0.end > now && $0.start <= limit }
        // Calendar replacements are already included; never duplicate an explicit training.
        if let regular = nextRegularTraining(events: events, now: now), !regular.fromCalendar {
            candidates.append(regular)
        }
        candidates.sort { $0.start == $1.start ? $0.id < $1.id : $0.start < $1.start }
        guard let first = candidates.first else { return [] }
        let dayStart = calendar.startOfDay(for: max(now, first.start))
        let dayEnd = calendar.date(byAdding: .day, value: 1, to: dayStart)!
        return candidates.filter { $0.start < dayEnd && $0.end > dayStart }.map { event in
            var selected = event
            selected.start = max(event.start, dayStart)
            selected.end = min(event.end, dayEnd)
            return selected
        }
    }
    public static func nextRegularTraining(events: [PFVREvent], now: Date = Date()) -> PFVREvent? {
        let calendar = PFVRDate.calendar
        let first = calendar.startOfDay(for: now)
        for offset in 0..<21 {
            guard let day = calendar.date(byAdding: .day, value: offset, to: first) else { continue }
            let season = trainingSeason(events: events, day: day)
            let summer = season == .summer || (season == .pause && (4...9).contains(calendar.component(.month, from: day)))
            let weekday = calendar.component(.weekday, from: day)
            let regularDay = summer ? [2,4].contains(weekday) : weekday == 5
            let relevant = events.filter { calendar.isDate($0.start, inSameDayAs: day) && trainingScore($0) >= 4 }
            if relevant.contains(where: \.isCancelled) { continue }
            let start = calendar.date(bySettingHour: summer ? 18 : 19, minute: 30, second: 0, of: day)!
            let end = calendar.date(bySettingHour: summer ? 20 : 21, minute: 0, second: 0, of: day)!
            if var replacement = relevant.filter({ !$0.isCancelled && (trainingScore($0) >= 8 || (season != .pause && regularDay)) }).sorted(by: {
                let a = trainingScore($0), b = trainingScore($1)
                return a == b ? $0.start < $1.start : a > b
            }).first {
                if replacement.allDay { replacement.start = start; replacement.end = end; replacement.allDay = false }
                if replacement.end > now { return replacement }
            } else if season != .pause && regularDay && end > now {
                return PFVREvent(id: "regular-\(start.timeIntervalSince1970)", title: "Regelmässiges Training", start: start, end: end, fromCalendar: false)
            }
        }
        // Do not resurrect a cancelled date when the whole search window is cancelled.
        return nil
    }
}
