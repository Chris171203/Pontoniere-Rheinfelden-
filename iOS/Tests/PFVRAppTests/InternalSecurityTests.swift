import XCTest
@testable import PFVR

final class InternalSecurityTests: XCTestCase {
    func testExactHostHTTPSPolicyAndInitialLinkNormalization() throws {
        XCTAssertTrue(InternalNavigationPolicy.permits(try XCTUnwrap(URL(string: "https://intern.pfvr.ch/"))))
        let blocked = ["http://intern.pfvr.ch/", "https://intern.pfvr.ch.evil.example/", "https://evil.example/intern.pfvr.ch",
                       "https://user@intern.pfvr.ch/", "https://intern.pfvr.ch:8443/", "javascript:alert(1)", "file:///tmp/test",
                       "mailto:fixture@example.invalid", "https://pfvr.ch/?fixture=private"]
        for raw in blocked { XCTAssertFalse(InternalNavigationPolicy.permits(try XCTUnwrap(URL(string: raw)))) }
        var parts = URLComponents()
        parts.scheme = "https"; parts.host = "intern.pfvr.ch"; parts.path = "/fixture"
        parts.queryItems = [URLQueryItem(name: "what", value: "abmeldung_ics_feed"), URLQueryItem(name: "fixture", value: "local-only")]
        let normalized = try XCTUnwrap(InternalNavigationPolicy.normalizedInitialURL(parts.url!.absoluteString))
        XCTAssertEqual(URLComponents(url: normalized, resolvingAgainstBaseURL: false)?.queryItems?.first?.value, "abmeldung")
        parts.queryItems?.append(URLQueryItem(name: "what", value: "abmeldung"))
        XCTAssertNil(InternalNavigationPolicy.normalizedInitialURL(parts.url!.absoluteString))
        XCTAssertNil(InternalNavigationPolicy.normalizedInitialURL("https://intern.pfvr.ch/"))
    }

    func testDeviceLocalStoreRoundTripIdentityChangeAndDeletion() throws {
        let store = SecureInternalStore(service: "ch.pfvr.tests.internal." + UUID().uuidString)
        defer { try? store.removeURL() }
        var parts = URLComponents()
        parts.scheme = "https"; parts.host = "intern.pfvr.ch"; parts.path = "/fixture"
        parts.queryItems = [URLQueryItem(name: "what", value: "abmeldung")]
        try store.saveURL(parts.url!.absoluteString)
        XCTAssertEqual(store.loadURL(), parts.url)
        let state = #"{"version":4,"desired":["Muster Alex"],"restoreValues":{},"hidden":[],"rowNames":["Muster Alex"]}"#
        try store.savePeopleState(state)
        XCTAssertEqual(store.loadPeopleState(), state)
        try store.savePeopleState("not-json")
        XCTAssertEqual(store.loadPeopleState(), state)
        parts.queryItems?.append(URLQueryItem(name: "fixture", value: "another-identity"))
        try store.saveURL(parts.url!.absoluteString)
        XCTAssertNil(store.loadPeopleState())
        try store.removeURL()
        XCTAssertNil(store.loadURL())
        XCTAssertThrowsError(try store.saveURL("https://example.invalid/"))
    }
}
