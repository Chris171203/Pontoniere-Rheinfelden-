import XCTest
@testable import PFVRCore

final class AccessLanguageTests: XCTestCase {
    func testGateNormalizationAndInvalidActivationCodes() {
        XCTAssertEqual(AccessGate.normalize(" abcd-2345 efgh-6789 "), "ABCD2345EFGH6789")
        for invalid in ["", "PFVR", "0000-0000-0000-0000"] { XCTAssertFalse(AccessGate.matches(invalid)) }
        XCTAssertFalse(AccessGate.matchesDigest("test", expectedSHA256: "nothex"))
        XCTAssertFalse(AccessGate.matchesDigest("test", expectedSHA256: String(repeating: "z", count: 64)))
    }
    #if canImport(CryptoKit)
    func testSHA256KnownVectorsAndDigestComparison() {
        let empty = "e3b0c44298fc1c149afbf4c8996fb92427ae41e4649b934ca495991b7852b855"
        let abc = "ba7816bf8f01cfea414140de5dae2223b00361a396177a9cb410ff61f20015ad"
        XCTAssertEqual(AccessGate.sha256Hex(""), empty)
        XCTAssertEqual(AccessGate.sha256Hex("abc"), abc)
        XCTAssertTrue(AccessGate.matchesDigest("abc", expectedSHA256: abc.uppercased()))
        XCTAssertFalse(AccessGate.matchesDigest("abcd", expectedSHA256: abc))
    }
    func testAndroidSwissQRGoldenDigest() throws {
        // Golden payload independently assembled from Android 0.12.6 makeSwissQr field layout.
        let payload = PaymentQR.payload(amount: try PaymentAmount(raw: "12.50"))
        XCTAssertEqual(AccessGate.sha256Hex(payload), "7a64a06850b7bca16e497a563f3d37c45dc338edfaca2f7b79ecc5be72ddeac0")
    }
    #endif
    func testExactHostAndSafeSchemePolicies() throws {
        XCTAssertTrue(AccessLinkPolicy.isPFVRHost("www.pfvr.ch"))
        XCTAssertFalse(AccessLinkPolicy.isPFVRHost("pfvr.ch.example.org"))
        XCTAssertFalse(AccessLinkPolicy.isPFVRHost("evilpfvr.ch"))
        XCTAssertTrue(AccessLinkPolicy.mayStayInPublicWebView(PublicLinks.site))
        XCTAssertFalse(AccessLinkPolicy.mayStayInPublicWebView(try XCTUnwrap(URL(string: "http://www.pfvr.ch/"))))
        for raw in ["javascript:alert(1)", "data:text/html,test", "file:///etc/hosts", "intent://test", "custombank://transfer"] {
            XCTAssertFalse(AccessLinkPolicy.mayOpenExternally(try XCTUnwrap(URL(string: raw))))
        }
        for raw in ["https://www.pfvr.ch/", "mailto:public@example.org", "tel:+41000000000", "geo:47.5,7.8"] {
            XCTAssertTrue(AccessLinkPolicy.mayOpenExternally(try XCTUnwrap(URL(string: raw))))
        }
    }
    func testInternalLinkValidationRejectsCredentialHostAndActionConfusion() throws {
        var components = URLComponents()
        components.scheme = "https"
        components.host = "intern.pfvr.ch"
        components.path = "/index.php"
        components.queryItems = [URLQueryItem(name: "what", value: "abmeldung")]
        let valid = try XCTUnwrap(components.url)
        XCTAssertEqual(AccessLinkPolicy.validatedInternalURL(valid.absoluteString), valid)
        components.queryItems?.append(URLQueryItem(name: "what", value: "other"))
        XCTAssertNil(AccessLinkPolicy.validatedInternalURL(try XCTUnwrap(components.url).absoluteString))
        components.queryItems = [URLQueryItem(name: "what", value: "abmeldung")]
        components.user = "untrusted"
        XCTAssertNil(AccessLinkPolicy.validatedInternalURL(try XCTUnwrap(components.url).absoluteString))
        components.user = nil
        components.port = 8443
        XCTAssertNil(AccessLinkPolicy.validatedInternalURL(try XCTUnwrap(components.url).absoluteString))
        components.port = nil
        components.host = "intern.pfvr.ch.example.org"
        XCTAssertNil(AccessLinkPolicy.validatedInternalURL(try XCTUnwrap(components.url).absoluteString))
        components.host = "intern.pfvr.ch"
        components.scheme = "http"
        XCTAssertNil(AccessLinkPolicy.validatedInternalURL(try XCTUnwrap(components.url).absoluteString))
    }
    func testBundledLanguageMappingAndExternalContentBoundary() {
        XCTAssertEqual(Language.translate("Warenkorb", mode: .swissGerman), "Warenchorb")
        XCTAssertEqual(Language.translate("Warenkorb", mode: .german), "Warenkorb")
        XCTAssertEqual(Language.translate("Alle Termine anzeigen  →", mode: .swissGerman), "Alli Termin aazeige  →")
        XCTAssertEqual(Language.translate("Rhein-Kachel 2", mode: .swissGerman), "Rhy-Kachle 2")
        XCTAssertEqual(Language.translate("Hintergrundaktualisierung: Ein", mode: .swissGerman), "Hintergrund-Aktualisierig: Aa")
        XCTAssertEqual(Language.translate("Nachrichten von Beispiel mit Essen", mode: .swissGerman), "Nachrichten von Beispiel mit Essen")
        XCTAssertEqual(Language.translate("Wetter zum nächsten Termin", mode: .swissGerman), "Wätter zum nöchschte Termin")
        XCTAssertEqual(Language.translate("War die Zahlung erfolgreich?", mode: .swissGerman), "Isch d Zahlig erfolgreich gsi?")
    }
}
