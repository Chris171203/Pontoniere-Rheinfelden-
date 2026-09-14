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
    public static func nextWeatherEvent(events: [PFVREvent], now: Date = Date()) -> PFVREvent? {
        let limit = PFVRDate.calendar.date(byAdding: .day, value: 21, to: now)!
        let scheduled = events.filter { !$0.isCancelled && $0.end > now && $0.start <= limit }.min { $0.start < $1.start }
        let regular = nextRegularTraining(events: events, now: now)
        switch (scheduled, regular) {
        case let (event?, training?): return event.start <= training.start ? event : training
        case let (event?, nil): return event
        case let (nil, training?): return training
        default: return nil
        }
    }
    public static func nextRegularTraining(events: [PFVREvent], now: Date = Date()) -> PFVREvent? {
        let calendar = PFVRDate.calendar
        let first = calendar.startOfDay(for: now)
        for offset in 0..<21 {
            guard let day = calendar.date(byAdding: .day, value: offset, to: first) else { continue }
            let summer = (4...9).contains(calendar.component(.month, from: day))
            let weekday = calendar.component(.weekday, from: day)
            guard summer ? [2,4].contains(weekday) : weekday == 5 else { continue }
            let relevant = events.filter { calendar.isDate($0.start, inSameDayAs: day) && trainingScore($0) >= 4 }
            if relevant.contains(where: \.isCancelled) { continue }
            let start = calendar.date(bySettingHour: summer ? 18 : 19, minute: 30, second: 0, of: day)!
            let end = calendar.date(bySettingHour: summer ? 20 : 21, minute: 0, second: 0, of: day)!
            if var replacement = relevant.filter({ !$0.isCancelled }).sorted(by: {
                let a = trainingScore($0), b = trainingScore($1)
                return a == b ? $0.start < $1.start : a > b
            }).first {
                if replacement.allDay { replacement.start = start; replacement.end = end; replacement.allDay = false }
                if replacement.end > now { return replacement }
            } else if end > now {
                return PFVREvent(id: "regular-\(start.timeIntervalSince1970)", title: "Regelmässiges Training", start: start, end: end, fromCalendar: false)
            }
        }
        // Do not resurrect a cancelled date when the whole search window is cancelled.
        return nil
    }
}
