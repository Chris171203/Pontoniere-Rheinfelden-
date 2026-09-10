import XCTest
@testable import PFVRCore
#if canImport(FoundationNetworking)
import FoundationNetworking
#endif

private actor DataMockTransport: HTTPTransport {
    enum Reply: Sendable { case json(String, Int = 200), failure }
    var replies: [Reply]
    var requests: [URLRequest] = []
    init(_ replies: [Reply]) { self.replies = replies }
    func data(for request: URLRequest) async throws -> (Data, HTTPURLResponse) {
        requests.append(request)
        guard !replies.isEmpty else { throw URLError(.notConnectedToInternet) }
        let reply = replies.removeFirst()
        // Suspension makes concurrent-load request coalescing observable.
        await Task.yield()
        switch reply {
        case .failure: throw URLError(.notConnectedToInternet)
        case let .json(text, status): return (Data(text.utf8), HTTPURLResponse(url: request.url!, statusCode: status, httpVersion: "HTTP/1.1", headerFields: ["Content-Type":"application/json"])!)
        }
    }
    func count() -> Int { requests.count }
    func recorded() -> [URLRequest] { requests }
}

final class DataServiceTests: XCTestCase {
    private let moment = PFVRDate.parseLocal("2026-09-10T12:00")!
    private let weather = """
    {"hourly":{"time":["2026-09-10T06:00","2026-09-10T12:00","2026-09-10T18:00"],"temperature_2m":[10,22,17],"uv_index":[0,4,0]}}
    """
    private let calendar = """
    BEGIN:VCALENDAR
    VERSION:2.0
    BEGIN:VEVENT
    UID:one
    DTSTART;TZID=Europe/Zurich:20260912T090000
    DTEND;TZID=Europe/Zurich:20260912T180000
    SUMMARY:JP-Prüfung
    END:VEVENT
    END:VCALENDAR
    """
    private let news = """
    [{"id":1,"date":"2026-09-10T12:00:00","link":"https://www.pfvr.ch/post","title":{"rendered":"Bericht"},"excerpt":{"rendered":"Text"}}]
    """
    private func directory() throws -> URL {
        let url = FileManager.default.temporaryDirectory.appendingPathComponent("PFVR-data-tests-\(UUID().uuidString)")
        try FileManager.default.createDirectory(at: url, withIntermediateDirectories: true)
        addTeardownBlock { try? FileManager.default.removeItem(at: url) }
        return url
    }
    private func hydro(_ array: String, station: String = "2091") -> String {
        "{\"data\":{\"water\":{\"observations\":{\"\(array)\":[{\"stationNo\":\"\(station)\",\"parameterName\":\"Q\",\"timestamp\":\"2026-09-10T09:50:00Z\",\"value\":1000}]}}}}"
    }
    func testWeatherSingleFetchForThreeDaysAndConcurrentRefresh() async throws {
        let transport = DataMockTransport([.json(weather)])
        let now = moment
        let service = PFVRDataService(cacheDirectory:try directory(),transport:transport,clock:{now})
        async let first = service.weather()
        async let second = service.weather()
        let (a,b) = try await (first,second)
        XCTAssertEqual(a.value,b.value)
        XCTAssertEqual(WeatherForecast.days(hours:a.value,firstDay:now).count,3)
        _ = try await service.weather()
        let count = await transport.count()
        XCTAssertEqual(count,1)
    }
    func testWeatherFallbackUVSupplementMatchesTimestamps() async throws {
        let noUV = "{\"hourly\":{\"time\":[\"2026-09-10T06:00\",\"2026-09-10T12:00\"],\"temperature_2m\":[10,22],\"uv_index\":[null,null]}}"
        let uv = "{\"hourly\":{\"time\":[\"2026-09-10T12:00\",\"2026-09-10T06:00\"],\"uv_index\":[4,0]}}"
        let transport = DataMockTransport([.json("unavailable",503),.json(noUV),.json(uv)])
        let now = moment
        let service = PFVRDataService(cacheDirectory:try directory(),transport:transport,clock:{now})
        let result = try await service.weather()
        XCTAssertEqual(result.value.map(\.uv),[0,4])
        XCTAssertEqual(result.metadata.source,"Open-Meteo Best Match · UV Open-Meteo Best Match")
        let requests = await transport.recorded()
        XCTAssertEqual(requests.count,3)
        XCTAssertTrue(requests[0].url!.absoluteString.contains("models=meteoswiss_icon_seamless"))
        XCTAssertFalse(requests[1].url!.absoluteString.contains("models="))
    }
    func testWeatherUVFailureRetainsValidForecastAndFreshness() async throws {
        let transport = DataMockTransport([.json("{\"hourly\":{\"time\":[\"2026-09-10T12:00\"],\"temperature_2m\":[22]}}"),.failure])
        let now = moment
        let result = try await PFVRDataService(cacheDirectory:try directory(),transport:transport,clock:{now}).weather()
        XCTAssertEqual(result.value[0].temperature,22); XCTAssertNil(result.value[0].uv)
        XCTAssertFalse(result.metadata.isStale)
    }
    func testDiskCacheSurvivesNewServiceAndFailedRefreshDoesNotResetAge() async throws {
        let dir = try directory(), now = moment
        let first = PFVRDataService(cacheDirectory:dir,transport:DataMockTransport([.json(weather)]),clock:{now})
        let initial = try await first.weather()
        let later = now.addingTimeInterval(7200)
        let transport = DataMockTransport([.failure,.failure])
        let restarted = PFVRDataService(cacheDirectory:dir,transport:transport,clock:{later})
        let disk = await restarted.cachedWeather()
        XCTAssertEqual(disk?.value,initial.value); XCTAssertTrue(disk?.metadata.isStale == true)
        let result = try await restarted.weather()
        XCTAssertEqual(result.value,initial.value); XCTAssertEqual(result.metadata.updatedAt,now)
        XCTAssertEqual(result.metadata.age(now:later),7200)
        XCTAssertTrue(result.metadata.isStale); XCTAssertNotNil(result.metadata.failure)
        let stored: Loaded<[WeatherHour]>? = CacheStore(directory:dir).read("weather")
        XCTAssertEqual(stored?.metadata.updatedAt,now)
    }
    func testMalformedCalendarRetainsGoodCacheButEmptyCalendarIsValid() async throws {
        let dir = try directory(), now = moment
        let transport = DataMockTransport([.json(calendar),.json("BEGIN:VCALENDAR\nBEGIN:VEVENT\nDTSTART:not-a-date\nEND:VEVENT\nEND:VCALENDAR"),.json("BEGIN:VCALENDAR\nEND:VCALENDAR")])
        let service = PFVRDataService(cacheDirectory:dir,transport:transport,clock:{now})
        let initial = try await service.events()
        XCTAssertEqual(initial.value.count,1)
        let failed = try await service.events(force:true)
        XCTAssertEqual(failed.value,initial.value); XCTAssertTrue(failed.metadata.isStale)
        let empty = try await service.events(force:true)
        XCTAssertTrue(empty.value.isEmpty); XCTAssertFalse(empty.metadata.isStale)
    }
    func testMalformedNewsRetainsGoodCache() async throws {
        let now = moment
        let service = PFVRDataService(cacheDirectory:try directory(),transport:DataMockTransport([.json(news),.json("[{}]")]),clock:{now})
        let first = try await service.news()
        let invalid = try await service.news(force:true)
        XCTAssertEqual(first.value,invalid.value); XCTAssertTrue(invalid.metadata.isStale)
    }
    func testNoCacheFailureThrowsInsteadOfFabricatingData() async throws {
        let now = moment
        let service = PFVRDataService(cacheDirectory:try directory(),transport:DataMockTransport([.failure]),clock:{now})
        do { _ = try await service.news(); XCTFail("Expected unavailable data") } catch { XCTAssertTrue(error is URLError) }
    }
    func testHydroBothStationsRealQueriesAndPartialFailures() async throws {
        let now = moment
        let transport = DataMockTransport([
            .json(hydro("data_live")),.json(hydro("data_10min_mean")),.json(hydro("data_1hour_mean")),
            .failure,.json(hydro("data_10min_mean")),.json("{\"errors\":[{\"message\":\"fail\"}]}"),
            .json(hydro("data_live",station:"2289")),.json(hydro("data_10min_mean",station:"2289")),.json(hydro("data_1hour_mean",station:"2289"))
        ])
        let service = PFVRDataService(cacheDirectory:try directory(),transport:transport,clock:{now})
        let first = await service.hydro(station:.rheinfelden)
        XCTAssertEqual(first.latest(parameter:"Q")?.value,1000)
        let failed = await service.hydro(station:.rheinfelden,force:true)
        XCTAssertEqual(failed.latest(parameter:"Q")?.value,1000)
        XCTAssertTrue(failed.live?.metadata.isStale == true)
        XCTAssertTrue(failed.history?.metadata.isStale == true)
        XCTAssertFalse(failed.fine?.metadata.isStale ?? true)
        XCTAssertEqual(failed.failures.count,2)
        let basel = await service.hydro(station:.baselRheinhalle)
        XCTAssertEqual(basel.live?.metadata.source,"BAFU 2289")
        let requests = await transport.recorded()
        XCTAssertEqual(requests.count,9)
        XCTAssertTrue(requests.allSatisfy { $0.httpMethod == "POST" && $0.url == PFVRDataSources.hydro })
        let bodies = try requests.map { try XCTUnwrap($0.httpBody) }.map { String(decoding:$0,as:UTF8.self) }
        XCTAssertTrue(bodies[0].contains("stationNo")); XCTAssertTrue(bodies[0].contains("2091")); XCTAssertTrue(bodies[6].contains("2289"))
        XCTAssertTrue(bodies[1].contains("data_10min_mean")); XCTAssertTrue(bodies[2].contains("data_1hour_mean"))
    }
    func testCorruptDiskCacheIsNotPresentedAndCanRecover() async throws {
        let dir = try directory(), now = moment
        try Data("not json".utf8).write(to:dir.appendingPathComponent("weather.json"))
        let service = PFVRDataService(cacheDirectory:dir,transport:DataMockTransport([.json(weather)]),clock:{now})
        let disk = await service.cachedWeather(); XCTAssertNil(disk)
        let result = try await service.weather(); XCTAssertEqual(result.value.count,3)
    }
}

private final class DataFixtureURLProtocol: URLProtocol {
    override class func canInit(with request: URLRequest) -> Bool { request.url?.host == "fixture.pfvr.invalid" }
    override class func canonicalRequest(for request: URLRequest) -> URLRequest { request }
    override func startLoading() {
        let response = HTTPURLResponse(url:request.url!,statusCode:200,httpVersion:"HTTP/1.1",headerFields:["Content-Type":"application/json"])!
        client?.urlProtocol(self,didReceive:response,cacheStoragePolicy:.notAllowed)
        client?.urlProtocol(self,didLoad:Data("{\"fixture\":true}".utf8))
        client?.urlProtocolDidFinishLoading(self)
    }
    override func stopLoading() {}
}

final class URLSessionTransportTests: XCTestCase {
    func testInjectableURLSessionExecutesProtocolFixture() async throws {
        let configuration = URLSessionConfiguration.ephemeral
        configuration.protocolClasses = [DataFixtureURLProtocol.self]
        let session = URLSession(configuration:configuration)
        defer { session.invalidateAndCancel() }
        let transport = URLSessionTransport(session:session)
        let (body,response) = try await transport.data(for:URLRequest(url:URL(string:"https://fixture.pfvr.invalid/test")!))
        XCTAssertEqual(response.statusCode,200)
        XCTAssertEqual(String(decoding:body,as:UTF8.self),"{\"fixture\":true}")
        do {
            _ = try await transport.data(for:URLRequest(url:URL(string:"http://fixture.pfvr.invalid/test")!))
            XCTFail("Unencrypted requests must be rejected")
        } catch { XCTAssertEqual(error as? PFVRDataError,.insecureURL) }
    }
}
