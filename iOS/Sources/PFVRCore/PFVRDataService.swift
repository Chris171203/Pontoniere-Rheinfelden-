import Foundation
#if canImport(FoundationNetworking)
import FoundationNetworking
#endif

public enum PFVRDataError: Error, LocalizedError, Equatable {
    case invalidPayload(String)
    case httpStatus(Int)
    case insecureURL
    public var errorDescription: String? {
        switch self {
        case .invalidPayload(let message): return message
        case .httpStatus(let code): return "Datenabruf: HTTP \(code)"
        case .insecureURL: return "Datenquelle benötigt HTTPS"
        }
    }
}
public protocol HTTPTransport: Sendable {
    func data(for request: URLRequest) async throws -> (Data, HTTPURLResponse)
}
public struct URLSessionTransport: HTTPTransport, @unchecked Sendable {
    private let session: URLSession
    public init(session: URLSession = .shared) { self.session = session }
    public func data(for request: URLRequest) async throws -> (Data, HTTPURLResponse) {
        guard request.url?.scheme?.lowercased() == "https" else { throw PFVRDataError.insecureURL }
        let (data, response) = try await session.data(for: request)
        guard let response = response as? HTTPURLResponse else { throw PFVRDataError.invalidPayload("HTTP-Antwort fehlt") }
        guard response.url?.scheme?.lowercased() == "https" else { throw PFVRDataError.insecureURL }
        return (data, response)
    }
}
public enum PFVRDataSources {
    public static let calendar = URL(string: "https://calendar.google.com/calendar/ical/a8mtko83nd27vsvp4i1cnpt3gs%40group.calendar.google.com/public/basic.ics")!
    public static let news = URL(string: "https://www.pfvr.ch/wp-json/wp/v2/posts?per_page=20&_fields=id,date,link,title,excerpt")!
    public static let hydro = URL(string: "https://data.bafu.admin.ch/api")!
    public static let weatherBase = "https://api.open-meteo.com/v1/forecast?latitude=47.5544&longitude=7.7940&hourly=temperature_2m,precipitation_probability,precipitation,weather_code,wind_speed_10m,wind_gusts_10m,uv_index&timezone=Europe%2FZurich&forecast_days=8"
    public static let weather = URL(string: weatherBase + "&models=meteoswiss_icon_seamless")!
    public static let weatherFallback = URL(string: weatherBase)!
    public static let uv = URL(string: "https://api.open-meteo.com/v1/forecast?latitude=47.5544&longitude=7.7940&hourly=uv_index&timezone=Europe%2FZurich&forecast_days=8")!
}

/// Coalesces concurrent loads, validates before replacing atomic cache, preserves independent BAFU slices.
public actor PFVRDataService {
    private let cache: CacheStore
    private let transport: any HTTPTransport
    private let clock: @Sendable () -> Date
    private var weatherTask: Task<Loaded<[WeatherHour]>, Error>?
    private var eventTask: Task<Loaded<[PFVREvent]>, Error>?
    private var newsTask: Task<Loaded<[NewsArticle]>, Error>?
    private var hydroTasks: [String: Task<HydroDataset, Never>] = [:]
    public init(cacheDirectory: URL, transport: any HTTPTransport = URLSessionTransport(), clock: @escaping @Sendable () -> Date = { Date() }) {
        cache = CacheStore(directory: cacheDirectory); self.transport = transport; self.clock = clock
    }
    public func clearCache() throws { try cache.clear() }
    public func cachedWeather() -> Loaded<[WeatherHour]>? { cached("weather", ttl: 1800) }
    public func cachedEvents() -> Loaded<[PFVREvent]>? { cached("calendar", ttl: 3600) }
    public func cachedNews() -> Loaded<[NewsArticle]>? { cached("news", ttl: 3600) }
    public func cachedHydro(station: HydroStation) -> HydroDataset {
        HydroDataset(station: station, live: cached("hydro_\(station.rawValue)_data_live", ttl: 600), fine: cached("hydro_\(station.rawValue)_data_10min_mean", ttl: 1800), history: cached("hydro_\(station.rawValue)_data_1hour_mean", ttl: 3600))
    }
    public func weather(force: Bool = false) async throws -> Loaded<[WeatherHour]> {
        if let task = weatherTask { return try await task.value }
        let task = Task { try await self.loadWeather(force: force) }; weatherTask = task
        defer { weatherTask = nil }
        return try await task.value
    }
    public func events(force: Bool = false) async throws -> Loaded<[PFVREvent]> {
        if let task = eventTask { return try await task.value }
        let task = Task { try await self.loadEvents(force: force) }; eventTask = task
        defer { eventTask = nil }
        return try await task.value
    }
    public func news(force: Bool = false) async throws -> Loaded<[NewsArticle]> {
        if let task = newsTask { return try await task.value }
        let task = Task { try await self.loadNews(force: force) }; newsTask = task
        defer { newsTask = nil }
        return try await task.value
    }
    public func hydro(station: HydroStation, force: Bool = false) async -> HydroDataset {
        if let task = hydroTasks[station.rawValue] { return await task.value }
        let task = Task { await self.loadHydro(station: station, force: force) }; hydroTasks[station.rawValue] = task
        defer { hydroTasks[station.rawValue] = nil }
        return await task.value
    }
    private func cached<Value: Codable>(_ key: String, ttl: TimeInterval) -> Loaded<Value>? {
        guard var result: Loaded<Value> = cache.read(key) else { return nil }
        let elapsed = clock().timeIntervalSince(result.metadata.updatedAt)
        result.metadata.isStale = elapsed < 0 || elapsed >= ttl
        return result
    }
    private func save<Value: Codable>(_ value: Value, key: String, source: String) -> Loaded<Value> {
        var result = Loaded(value: value, metadata: CacheMetadata(source: source, updatedAt: clock()))
        do { try cache.write(result, key: key) }
        catch { result.metadata.failure = "Datenstand konnte lokal nicht gespeichert werden." }
        return result
    }
    private func fallback<Value: Codable>(_ saved: Loaded<Value>?, error: Error) throws -> Loaded<Value> {
        guard var saved else { throw error }
        saved.metadata.isStale = true
        // Do not expose URLs/transport error details that might later contain query secrets.
        saved.metadata.failure = (error as? PFVRDataError)?.errorDescription ?? "Keine Verbindung – gespeicherter Stand."
        return saved
    }
    private func get(_ url: URL, body: Data? = nil, accept: String = "application/json") async throws -> Data {
        var request = URLRequest(url: url, cachePolicy: .reloadIgnoringLocalCacheData, timeoutInterval: 15)
        request.setValue(accept, forHTTPHeaderField: "Accept")
        request.setValue("PFVR-iOS/0.12.6", forHTTPHeaderField: "User-Agent")
        if let body { request.httpMethod = "POST"; request.httpBody = body; request.setValue("application/json", forHTTPHeaderField: "Content-Type") }
        let (data, response) = try await transport.data(for: request)
        guard (200..<300).contains(response.statusCode) else { throw PFVRDataError.httpStatus(response.statusCode) }
        return data
    }
    private func loadWeather(force: Bool) async throws -> Loaded<[WeatherHour]> {
        let saved: Loaded<[WeatherHour]>? = cached("weather", ttl: 1800)
        if let saved, !force && !saved.metadata.isStale { return saved }
        do {
            var source = "MeteoSwiss ICON via Open-Meteo"
            var hours: [WeatherHour]
            do { hours = try WeatherForecast.parse(await get(PFVRDataSources.weather)) }
            catch { hours = try WeatherForecast.parse(await get(PFVRDataSources.weatherFallback)); source = "Open-Meteo Best Match" }
            if !hours.contains(where: { $0.uv != nil }) {
                if let supplement = try? WeatherForecast.parse(await get(PFVRDataSources.uv)) {
                    hours = WeatherForecast.supplementUV(hours, from: supplement)
                    if hours.contains(where: { $0.uv != nil }) { source += " · UV Open-Meteo Best Match" }
                }
            }
            return save(hours, key: "weather", source: source)
        } catch { return try fallback(saved, error: error) }
    }
    private func loadEvents(force: Bool) async throws -> Loaded<[PFVREvent]> {
        let saved: Loaded<[PFVREvent]>? = cached("calendar", ttl: 3600)
        if let saved, !force && !saved.metadata.isStale { return saved }
        do {
            let data = try await get(PFVRDataSources.calendar, accept: "text/calendar")
            let events = try CalendarParser.parse(data, now: clock())
            // A valid empty calendar is accepted; malformed responses never replace cache.
            return save(events, key: "calendar", source: "PFVR Google-Kalender")
        } catch { return try fallback(saved, error: error) }
    }
    private func loadNews(force: Bool) async throws -> Loaded<[NewsArticle]> {
        let saved: Loaded<[NewsArticle]>? = cached("news", ttl: 3600)
        if let saved, !force && !saved.metadata.isStale { return saved }
        do { return save(try NewsParser.parse(await get(PFVRDataSources.news)), key: "news", source: "PFVR WordPress") }
        catch { return try fallback(saved, error: error) }
    }
    private func loadHydro(station: HydroStation, force: Bool) async -> HydroDataset {
        var dataset = HydroDataset(station: station)
        for (array, ttl, lookback) in [("data_live",600.0,0.0),("data_10min_mean",1800.0,26.0*3600),("data_1hour_mean",3600.0,8.0*86400)] {
            let key = "hydro_\(station.rawValue)_\(array)"
            let saved: Loaded<[HydroObservation]>? = cached(key, ttl: ttl)
            var result = saved
            if force || saved == nil || saved?.metadata.isStale == true {
                do {
                    let query: String
                    if array == "data_live" {
                        query = "{ water { observations { data_live(where:{stationNo:{_eq:\"\(station.rawValue)\"}}) { stationNo parameterName timestamp value releaseStatus } } } }"
                    } else {
                        let from = ISO8601DateFormatter().string(from: clock().addingTimeInterval(-lookback))
                        query = "{ water { observations { \(array)(where:{station:{no:{_eq:\"\(station.rawValue)\"}},timestamp:{_gte:\"\(from)\"}}) { parameterName timestamp value } } } }"
                    }
                    let body = try JSONSerialization.data(withJSONObject: ["query":query])
                    let values = try LiveHydroParser.parse(await get(PFVRDataSources.hydro, body: body), arrayName: array, station: station)
                    result = save(values, key: key, source: "BAFU \(station.rawValue)")
                } catch {
                    result = try? fallback(saved, error: error)
                    dataset.failures.append("BAFU \(station.rawValue) · \(array): " + ((error as? PFVRDataError)?.errorDescription ?? "Abruf fehlgeschlagen"))
                }
            }
            switch array { case "data_live": dataset.live = result; case "data_10min_mean": dataset.fine = result; default: dataset.history = result }
        }
        return dataset
    }
}
