import Foundation

/// All calendar and forecast day boundaries use the club's timezone, independent of device locale.
public enum PFVRDate {
    public static let timeZone = TimeZone(identifier: "Europe/Zurich")!
    public static var calendar: Calendar {
        var calendar = Calendar(identifier: .gregorian)
        calendar.timeZone = timeZone
        calendar.firstWeekday = 2
        return calendar
    }
    public static func parseLocal(_ text: String, format: String = "yyyy-MM-dd'T'HH:mm", zone: TimeZone = timeZone) -> Date? {
        let formatter = DateFormatter()
        formatter.locale = Locale(identifier: "en_US_POSIX")
        formatter.timeZone = zone
        formatter.dateFormat = format
        formatter.isLenient = false
        return formatter.date(from: text)
    }
    public static func parseISO(_ text: String) -> Date? {
        let formatter = ISO8601DateFormatter()
        formatter.formatOptions = [.withInternetDateTime, .withFractionalSeconds]
        if let date = formatter.date(from: text) { return date }
        formatter.formatOptions = [.withInternetDateTime]
        return formatter.date(from: text)
    }
}

public struct PFVREvent: Codable, Equatable, Identifiable, Sendable {
    public let id: String
    public var title: String
    public var location: String
    public var details: String
    public var status: String
    public var start: Date
    public var end: Date
    public var allDay: Bool
    public var fromCalendar: Bool
    public init(id: String, title: String, location: String = "", details: String = "", status: String = "", start: Date, end: Date, allDay: Bool = false, fromCalendar: Bool = true) {
        self.id = id; self.title = title; self.location = location; self.details = details; self.status = status
        self.start = start; self.end = end; self.allDay = allDay; self.fromCalendar = fromCalendar
    }
    public var isCancelled: Bool { CalendarPolicy.isCancelled(status: status, title: title, details: details) }
}

public struct WeatherHour: Codable, Equatable, Identifiable, Sendable {
    public var id: Date { time }
    public let time: Date
    public var temperature: Double?
    public var precipitationProbability: Int?
    public var precipitation: Double?
    public var wind: Double?
    public var gust: Double?
    public var uv: Double?
    public var weatherCode: Int?
    public init(time: Date, temperature: Double? = nil, precipitationProbability: Int? = nil, precipitation: Double? = nil, wind: Double? = nil, gust: Double? = nil, uv: Double? = nil, weatherCode: Int? = nil) {
        self.time = time; self.temperature = temperature; self.precipitationProbability = precipitationProbability.map { max(0, min(100, $0)) }
        self.precipitation = precipitation; self.wind = wind; self.gust = gust; self.uv = uv; self.weatherCode = weatherCode
    }
}

public struct WeatherSlot: Equatable, Identifiable, Sendable {
    public var id: Date { target }
    public let target: Date
    public let hour: WeatherHour?
}
public struct WeatherDay: Equatable, Identifiable, Sendable {
    public var id: Date { date }
    public let date: Date
    public let slots: [WeatherSlot]
    public let minTemperature: Double?
    public let maxTemperature: Double?
    public let precipitationProbabilityMax: Int?
    public let precipitationSum: Double?
    public let windMax: Double?
    public let gustMax: Double?
    public let uvMax: Double?
    public let weatherCode: Int?
    public let count: Int
}
public struct WeatherEventForecast: Sendable {
    public let event: PFVREvent
    public let slots: [WeatherSlot]
    public let hours: [WeatherHour]
    public let summary: WeatherDay
}
public struct NewsArticle: Codable, Equatable, Identifiable, Sendable {
    public let id: Int
    public let publishedAt: Date?
    public let title: String
    public let excerpt: String
    public let url: URL
    public init(id: Int, publishedAt: Date? = nil, title: String, excerpt: String, url: URL) {
        self.id = id; self.publishedAt = publishedAt; self.title = title; self.excerpt = excerpt; self.url = url
    }
}
public struct HydroObservation: Codable, Equatable, Identifiable, Sendable {
    public var id: String { "\(parameter)|\(time.timeIntervalSince1970)" }
    public let time: Date
    public let value: Double
    public let parameter: String
    public init(time: Date, value: Double, parameter: String) { self.time = time; self.value = value; self.parameter = parameter }
}
public enum HydroRange: String, CaseIterable, Identifiable, Sendable {
    case hour, day, week
    public var id: String { rawValue }
    public var label: String { switch self { case .hour: return "1h"; case .day: return "24h"; case .week: return "7d" } }
    public var window: TimeInterval { switch self { case .hour: return 3600; case .day: return 86400; case .week: return 604800 } }
}
public struct HydroDataset {
    public let station: HydroStation
    public var live: Loaded<[HydroObservation]>?
    public var fine: Loaded<[HydroObservation]>?
    public var history: Loaded<[HydroObservation]>?
    public var failures: [String]
    public init(station: HydroStation, live: Loaded<[HydroObservation]>? = nil, fine: Loaded<[HydroObservation]>? = nil, history: Loaded<[HydroObservation]>? = nil, failures: [String] = []) {
        self.station = station; self.live = live; self.fine = fine; self.history = history; self.failures = failures
    }
    /// Current values always come from live data; history is never presented as a current reading.
    public func latest(parameter: String) -> HydroObservation? { live?.value.filter { $0.parameter == parameter }.max { $0.time < $1.time } }
    public func series(parameter: String, range: HydroRange) -> [HydroObservation] {
        func selected(_ values: Loaded<[HydroObservation]>?) -> [HydroObservation] { (values?.value ?? []).filter { $0.parameter == parameter }.sorted { $0.time < $1.time } }
        let current = selected(live)
        var points: [HydroObservation]
        switch range {
        case .hour: points = current
        case .day:
            points = selected(fine)
            let after = points.last?.time ?? .distantPast
            var last = after
            for point in current where point.time > after && point.time.timeIntervalSince(last) >= 9 * 60 {
                points.append(point); last = point.time
            }
            if let latest = current.last, latest.time > after { points.append(latest) }
            if points.count < 2 { points.append(contentsOf: selected(history)) }
        case .week:
            points = selected(history)
            if let latest = current.last { points.append(latest) }
        }
        if points.count < 2 { points.append(contentsOf: current) }
        var byTime: [Date: HydroObservation] = [:]
        for point in points { byTime[point.time] = point }
        let ordered = byTime.values.sorted { $0.time < $1.time }
        guard let newest = ordered.last?.time else { return [] }
        return ordered.filter { $0.time >= newest.addingTimeInterval(-range.window) }
    }
}
