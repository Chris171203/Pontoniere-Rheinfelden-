import XCTest
import UIKit
import PFVRCore
@testable import PFVR

private actor ClubImageTransport: HTTPTransport {
    let payload: Data
    var calls = 0
    var fails = false
    var blocked = false
    var started: CheckedContinuation<Void, Never>?
    var releaseWaiter: CheckedContinuation<Void, Never>?
    init(_ payload: Data) { self.payload = payload }
    func configure(fails: Bool = false, blocked: Bool = false) { self.fails = fails; self.blocked = blocked }
    func waitForStart() async { if calls == 0 { await withCheckedContinuation { started = $0 } } }
    func release() { blocked = false; releaseWaiter?.resume(); releaseWaiter = nil }
    func data(for request: URLRequest) async throws -> (Data, HTTPURLResponse) {
        calls += 1; started?.resume(); started = nil
        if blocked { await withCheckedContinuation { releaseWaiter = $0 } }
        if fails { throw URLError(.notConnectedToInternet) }
        return (payload, HTTPURLResponse(url: request.url!, statusCode: 200, httpVersion: nil, headerFields: nil)!)
    }
}
@MainActor
final class ClubImageTests: XCTestCase {
    private func fixture() throws -> Data {
        let format = UIGraphicsImageRendererFormat(); format.scale = 1
        let image = UIGraphicsImageRenderer(size: CGSize(width: 2000, height: 1000), format: format).image { context in
            UIColor.blue.setFill(); context.fill(CGRect(x: 0, y: 0, width: 2000, height: 1000))
            UIColor.white.setFill(); context.fill(CGRect(x: 500, y: 200, width: 800, height: 400))
        }
        return try XCTUnwrap(image.pngData())
    }
    func testNativeImageIsDownsampledPersistedAndAvailableOffline() async throws {
        let directory = FileManager.default.temporaryDirectory.appendingPathComponent(UUID().uuidString)
        defer { try? FileManager.default.removeItem(at: directory) }
        let transport = ClubImageTransport(try fixture())
        let url = URL(string: "https://www.pfvr.ch/wp-content/uploads/test.jpg")!
        let store = ClubImageStore(directory: directory, transport: transport)
        let first = try await store.image(url)
        let image = try XCTUnwrap(UIImage(data: first))
        XCTAssertLessThanOrEqual(max(image.size.width, image.size.height), 1400)
        let restored = ClubImageStore(directory: directory, transport: transport)
        let cached = try await restored.image(url); XCTAssertEqual(cached, first)
        let initialCalls = await transport.calls; XCTAssertEqual(initialCalls, 1)
        for file in try FileManager.default.contentsOfDirectory(at: directory, includingPropertiesForKeys: nil) {
            try FileManager.default.setAttributes([.modificationDate: Date().addingTimeInterval(-90000)], ofItemAtPath: file.path)
        }
        await transport.configure(fails: true)
        let offline = try await restored.image(url); XCTAssertEqual(offline, first)
        do { _ = try await store.image(URL(string: "https://example.org/test.jpg")!); XCTFail("Unexpected external photo") }
        catch {}
        let finalCalls = await transport.calls; XCTAssertEqual(finalCalls, 2)
    }
    func testClearingImageCacheRejectsLateDownload() async throws {
        let directory = FileManager.default.temporaryDirectory.appendingPathComponent(UUID().uuidString)
        defer { try? FileManager.default.removeItem(at: directory) }
        let transport = ClubImageTransport(try fixture()); await transport.configure(blocked: true)
        let store = ClubImageStore(directory: directory, transport: transport)
        let task = Task { try await store.image(URL(string: "https://www.pfvr.ch/wp-content/uploads/test.jpg")!) }
        await transport.waitForStart()
        try await store.clear(); await transport.release()
        do { _ = try await task.value; XCTFail("Cleared image must not repopulate cache") }
        catch is CancellationError {} catch { XCTFail("\(error)") }
        XCTAssertFalse(FileManager.default.fileExists(atPath: directory.path))
    }
}
