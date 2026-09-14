import Foundation
import CoreFoundation

public enum WeatherForecast {
    public static func parse(_ data: Data) throws -> [WeatherHour] {
        guard let root = try JSONSerialization.jsonObject(with: data) as? [String: Any],
              let hourly = root["hourly"] as? [String: Any], let times = hourly["time"] as? [String], !times.isEmpty else { throw PFVRDataError.invalidPayload("Wetter: Stundenwerte fehlen") }
        func number(_ key: String, _ index: Int) -> Double? {
            guard let array = hourly[key] as? [Any], index < array.count, let number = array[index] as? NSNumber, CFGetTypeID(number) != CFBooleanGetTypeID() else { return nil }
            let value = number.doubleValue
            return value.isFinite ? value : nil
        }
        var result: [WeatherHour] = []
        for (index, timestamp) in times.enumerated() {
            guard let time = PFVRDate.parseLocal(timestamp) ?? PFVRDate.parseISO(timestamp) else { continue }
            result.append(WeatherHour(time: time, temperature: number("temperature_2m", index), precipitationProbability: number("precipitation_probability", index).map { Int(max(0, min(100, $0))) }, precipitation: number("precipitation", index), wind: number("wind_speed_10m", index), gust: number("wind_gusts_10m", index), uv: number("uv_index", index), weatherCode: number("weather_code", index).flatMap { $0 >= 0 && $0 <= 999 ? Int($0) : nil }))
        }
        guard result.contains(where: { $0.temperature != nil || $0.uv != nil }) else { throw PFVRDataError.invalidPayload("Wetter: keine gültigen Messwerte") }
        return result.sorted { $0.time < $1.time }
    }
    public static func supplementUV(_ hours: [WeatherHour], from supplement: [WeatherHour]) -> [WeatherHour] {
        var values: [Date: Double] = [:]
        for hour in supplement { if let uv = hour.uv, uv.isFinite { values[hour.time] = uv } }
        return hours.map { hour in var merged = hour; if merged.uv == nil { merged.uv = values[hour.time] }; return merged }
    }
    public static func days(hours: [WeatherHour], firstDay: Date = Date(), dayCount: Int = 3) -> [WeatherDay] {
        guard dayCount > 0 else { return [] }
        let calendar = PFVRDate.calendar
        let start = calendar.startOfDay(for: firstDay)
        return (0..<dayCount).compactMap { offset in
            guard let day = calendar.date(byAdding: .day, value: offset, to: start) else { return nil }
            let sameDay = hours.filter { calendar.isDate($0.time, inSameDayAs: day) }
            let targets = [6,12,18].compactMap { calendar.date(bySettingHour: $0, minute: 0, second: 0, of: day) }
            return summarize(sameDay, day: day, slots: slots(hours: sameDay, targets: targets))
        }
    }
    public static func slots(hours: [WeatherHour], targets: [Date]) -> [WeatherSlot] {
        targets.map { target in
            let sameDay = hours.filter { PFVRDate.calendar.isDate($0.time, inSameDayAs: target) }
            let closest = sameDay.min { abs($0.time.timeIntervalSince(target)) < abs($1.time.timeIntervalSince(target)) }
            return WeatherSlot(target: target, hour: closest.flatMap { abs($0.time.timeIntervalSince(target)) <= 90 * 60 ? $0 : nil })
        }
    }
    public static func event(hours: [WeatherHour], event: PFVREvent) -> WeatherEventForecast {
        let calendar = PFVRDate.calendar
        let multipleDays = !calendar.isDate(event.start, inSameDayAs: event.end)
        let threePoints = event.allDay || multipleDays || event.end.timeIntervalSince(event.start) >= 5 * 3600
        let matching = hours.filter { $0.time.addingTimeInterval(3600) > event.start && $0.time < event.end }
        let targets: [Date]
        if event.allDay || multipleDays {
            targets = [6,12,18].compactMap { calendar.date(bySettingHour: $0, minute: 0, second: 0, of: event.start) }
        } else if threePoints {
            // Android rounds start/middle/end to the containing hour for hourly forecasts.
            targets = [event.start, event.start.addingTimeInterval(event.end.timeIntervalSince(event.start) / 2), event.end].compactMap { calendar.dateInterval(of: .hour, for: $0)?.start }
        } else { targets = [] }
        let dayHours = hours.filter { calendar.isDate($0.time, inSameDayAs: event.start) }
        let values = slots(hours: dayHours, targets: targets)
        return WeatherEventForecast(event: event, slots: values, hours: matching, summary: summarize(threePoints ? dayHours : matching, day: calendar.startOfDay(for: event.start), slots: values))
    }
    private static func summarize(_ hours: [WeatherHour], day: Date, slots: [WeatherSlot]) -> WeatherDay {
        func finite(_ values: [Double?]) -> [Double] { values.compactMap { $0 }.filter { $0.isFinite } }
        let temperatures = finite(hours.map(\.temperature))
        let precipitation = finite(hours.map(\.precipitation)).map { max(0, $0) }
        let representative = PFVRDate.calendar.date(bySettingHour: 14, minute: 0, second: 0, of: day) ?? day
        let code = hours.filter { $0.weatherCode != nil }.min { abs($0.time.timeIntervalSince(representative)) < abs($1.time.timeIntervalSince(representative)) }?.weatherCode
        return WeatherDay(date: day, slots: slots, minTemperature: temperatures.min(), maxTemperature: temperatures.max(), precipitationProbabilityMax: hours.compactMap(\.precipitationProbability).max(), precipitationSum: precipitation.isEmpty ? nil : precipitation.reduce(0,+), windMax: finite(hours.map(\.wind)).map { max(0,$0) }.max(), gustMax: finite(hours.map(\.gust)).map { max(0,$0) }.max(), uvMax: finite(hours.map(\.uv)).map { max(0,$0) }.max(), weatherCode: code, count: hours.count)
    }
}
