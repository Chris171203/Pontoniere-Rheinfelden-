import XCTest
@testable import PFVRCore
#if canImport(FoundationNetworking)
import FoundationNetworking
#endif

private actor ClubTransport: HTTPTransport {
    var calls = 0
    let payload: Data
    var fails = false
    var blocked = false
    var started: CheckedContinuation<Void, Never>?
    var resume: CheckedContinuation<Void, Never>?
    init(_ payload: Data) { self.payload = payload }
    func configure(fails: Bool = false, blocked: Bool = false) { self.fails = fails; self.blocked = blocked }
    func waitForStart() async {
        if calls > 0 { return }
        await withCheckedContinuation { started = $0 }
    }
    func release() { blocked = false; resume?.resume(); resume = nil }
    func data(for request: URLRequest) async throws -> (Data, HTTPURLResponse) {
        calls += 1; started?.resume(); started = nil
        if blocked { await withCheckedContinuation { resume = $0 } }
        if fails { throw URLError(.notConnectedToInternet) }
        return (payload, HTTPURLResponse(url: request.url!, statusCode: 200, httpVersion: nil, headerFields: nil)!)
    }
}
final class ClubRepositoryTests: XCTestCase {
    func testCachePersistsAcrossRepositoryAndForceFailureKeepsSuccessfulSource() async throws {
        let directory = FileManager.default.temporaryDirectory.appendingPathComponent(UUID().uuidString)
        defer { try? FileManager.default.removeItem(at: directory) }
        let transport = ClubTransport(try ClubTestData.data())
        let repository = ClubRepository(cacheDirectory: directory, transport: transport)
        let first = try await repository.load(.about)
        let restored = ClubRepository(cacheDirectory: directory, transport: transport)
        let cached = try await restored.load(.about)
        XCTAssertEqual(cached, first)
        let calls = await transport.calls; XCTAssertEqual(calls, 1)
        await transport.configure(fails: true)
        let failed = try await restored.load(.about, force: true)
        XCTAssertEqual(failed.value, first.value)
        XCTAssertEqual(failed.metadata.updatedAt, first.metadata.updatedAt)
        XCTAssertTrue(failed.metadata.isStale)
        XCTAssertNotNil(failed.metadata.failure)
    }
    func testExpiredCacheReloadsAndWrongPayloadDoesNotReplaceSnapshot() async throws {
        let directory = FileManager.default.temporaryDirectory.appendingPathComponent(UUID().uuidString)
        defer { try? FileManager.default.removeItem(at: directory) }
        let now = Date(timeIntervalSince1970: 1_700_000_000)
        let source = try ClubContentParser.parse(page: .about, data: ClubTestData.data())
        try CacheStore(directory: directory).write(Loaded(value: source, metadata: CacheMetadata(source: "pfvr.ch", updatedAt: now.addingTimeInterval(-86401))), key: ClubPage.about.rawValue)
        let transport = ClubTransport(Data("[]".utf8))
        let repository = ClubRepository(cacheDirectory: directory, transport: transport, clock: { now })
        let result = try await repository.load(.about)
        let count = await transport.calls; XCTAssertEqual(count, 1)
        XCTAssertTrue(result.metadata.isStale)
        XCTAssertEqual(result.value, source)
    }
    func testClearPreventsLateRepopulation() async throws {
        let directory = FileManager.default.temporaryDirectory.appendingPathComponent(UUID().uuidString)
        defer { try? FileManager.default.removeItem(at: directory) }
        let transport = ClubTransport(try ClubTestData.data())
        await transport.configure(blocked: true)
        let repository = ClubRepository(cacheDirectory: directory, transport: transport)
        let first = Task { try await repository.load(.about) }
        await transport.waitForStart()
        let count = await transport.calls; XCTAssertEqual(count, 1)
        try await repository.clear()
        await transport.release()
        do { _ = try await first.value; XCTFail("Cleared request must not complete") } catch is CancellationError {} catch { XCTFail("\(error)") }
        let cached = await repository.cached(.about); XCTAssertNil(cached)
        let reloaded = try await repository.load(.about)
        XCTAssertFalse(reloaded.value.text.isEmpty)
    }
    func testConcurrentLoadsCoalesce() async throws {
        let directory = FileManager.default.temporaryDirectory.appendingPathComponent(UUID().uuidString)
        defer { try? FileManager.default.removeItem(at: directory) }
        let transport = ClubTransport(try ClubTestData.data())
        await transport.configure(blocked: true)
        let repository = ClubRepository(cacheDirectory: directory, transport: transport)
        let first = Task { try await repository.load(.about) }
        await transport.waitForStart()
        let second = Task { try await repository.load(.about) }
        await transport.release()
        let a = try await first.value, b = try await second.value
        XCTAssertEqual(a, b)
        let count = await transport.calls; XCTAssertEqual(count, 1)
    }
}
