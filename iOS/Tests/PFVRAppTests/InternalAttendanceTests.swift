import XCTest
import WebKit
@testable import PFVR

@MainActor
final class InternalAttendanceTests: XCTestCase {
    private var model: InternalAttendanceModel!
    private var webView: WKWebView!
    private var testStore: SecureInternalStore!
    private var window: UIWindow!
    private weak var previousKeyWindow: UIWindow?

    override func setUpWithError() throws {
        try super.setUpWithError()
        testStore = SecureInternalStore(service: "ch.pfvr.tests.internal." + UUID().uuidString)
        model = InternalAttendanceModel(store: testStore)
        // A detached UIWindow can run DOM tests while WebKit returns entirely
        // transparent snapshots. Use the hosted app's actual foreground scene.
        let scene = try XCTUnwrap(UIApplication.shared.connectedScenes.compactMap { $0 as? UIWindowScene }
            .first { $0.activationState == .foregroundActive }, "Hosted app needs a foreground scene for rendered evidence")
        previousKeyWindow = scene.windows.first { $0.isKeyWindow }
        window = UIWindow(windowScene: scene)
        window.frame = CGRect(x: 0, y: 0, width: 390, height: 844)
        window.rootViewController = UIViewController()
        window.makeKeyAndVisible()
    }

    override func tearDown() {
        model.detach()
        webView = nil
        try? testStore.removeURL()
        window?.isHidden = true
        window = nil
        previousKeyWindow?.makeKeyAndVisible()
        previousKeyWindow = nil
        model = nil
        testStore = nil
        super.tearDown()
    }

    private var initialURL: URL {
        var parts = URLComponents()
        parts.scheme = "https"; parts.host = "intern.pfvr.ch"; parts.path = "/fixture"
        parts.queryItems = [URLQueryItem(name: "what", value: "abmeldung"), URLQueryItem(name: "fixture", value: "local-only")]
        return parts.url!
    }

    private func load(_ html: String? = nil, appView: Bool = true, language: String = "de", dark: Bool = false) throws {
        let resource = Bundle(for: Self.self).url(forResource: "internal-fixture", withExtension: "html")
        let fixture = try html ?? String(contentsOf: XCTUnwrap(resource), encoding: .utf8)
        webView = model.makeWebView(initialURL: initialURL, appView: appView, language: language, dark: dark, startLoading: false)
        webView.frame = window.bounds
        webView.autoresizingMask = [.flexibleWidth, .flexibleHeight]
        window.rootViewController!.view.addSubview(webView)
        window.layoutIfNeeded()
        model.loadFixture(fixture)
    }

    private func waitFor(_ predicate: @escaping () async throws -> Bool, timeout: TimeInterval = 8,
                         file: StaticString = #filePath, line: UInt = #line) async throws {
        let deadline = Date().addingTimeInterval(timeout)
        while Date() < deadline {
            if try await predicate() { return }
            try await Task.sleep(nanoseconds: 50_000_000)
        }
        let keychain = testStore.lastFailureStatus.map { " (Keychain OSStatus: \($0))" } ?? ""
        XCTFail("Local WebKit fixture condition timed out\(keychain)", file: file, line: line)
        throw NSError(domain: "PFVRFixture", code: 1)
    }

    private func bool(_ script: String) async throws -> Bool {
        try await webView.evaluateJavaScript(script) as? Bool ?? false
    }

    private func ready() async throws {
        try await waitFor { !self.model.loading }
        XCTAssertFalse(model.usedOriginalFallback)
        let built = try await bool(InternalAttendanceRenderer.readyScript)
        XCTAssertTrue(built)
    }

    private func captureMatrix(_ name: String) async throws {
        XCTAssertNotNil(webView.window?.windowScene)
        XCTAssertFalse(webView.isHidden)
        webView.layoutIfNeeded()
        // DOM-ready is earlier than the first painted frame after reveal.
        _ = try await webView.evaluateJavaScript("window.__pfvrSnapshotPainted=false;requestAnimationFrame(()=>requestAnimationFrame(()=>{window.__pfvrSnapshotPainted=true}));null;")
        try await waitFor { try await self.bool("window.__pfvrSnapshotPainted===true") }
        let configuration = WKSnapshotConfiguration()
        configuration.rect = webView.bounds
        configuration.afterScreenUpdates = true
        let screenshot = try await webView.takeSnapshot(configuration: configuration)
        let attachment = XCTAttachment(image: screenshot)
        attachment.name = name
        attachment.lifetime = .keepAlways
        add(attachment)

        // Reject the observed all-transparent result, or a single background
        // color, instead of recording another apparently successful blank PNG.
        let cgImage = try XCTUnwrap(screenshot.cgImage)
        var pixels = [UInt8](repeating: 0, count: 16 * 16 * 4)
        try pixels.withUnsafeMutableBytes { bytes in
            let context = try XCTUnwrap(CGContext(data: bytes.baseAddress, width: 16, height: 16,
                bitsPerComponent: 8, bytesPerRow: 16 * 4, space: CGColorSpaceCreateDeviceRGB(),
                bitmapInfo: CGImageAlphaInfo.premultipliedLast.rawValue))
            context.draw(cgImage, in: CGRect(x: 0, y: 0, width: 16, height: 16))
        }
        let colors = Set(stride(from: 0, to: pixels.count, by: 4).map { index in
            UInt32(pixels[index]) << 24 | UInt32(pixels[index + 1]) << 16 |
                UInt32(pixels[index + 2]) << 8 | UInt32(pixels[index + 3])
        })
        XCTAssertTrue(stride(from: 3, to: pixels.count, by: 4).contains { pixels[$0] > 0 }, "Matrix snapshot must contain painted pixels")
        XCTAssertGreaterThan(colors.count, 1, "Matrix snapshot must contain visible content, not one background color")
    }

    func testActualControlsFormPayloadAndListenersSurviveMatrixProjection() async throws {
        try load()
        try await ready()
        try await captureMatrix("internal-matrix-390pt-de-light")
        XCTAssertFalse(webView.configuration.websiteDataStore.isPersistent)
        let actualNodes = try await bool("document.querySelector('.pfvr-person-control #original-submit')===fixtureOriginalSubmit && document.querySelector('.pfvr-person-control #original-input')===fixtureOriginalInput && document.querySelector('.pfvr-person-control #original-status')===fixtureOriginalStatus")
        XCTAssertTrue(actualNodes)
        let names = try await webView.evaluateJavaScript("Array.from(document.querySelectorAll('.pfvr-attendance-mobile .pfvr-person-header')).map(n=>n.textContent)") as? [String]
        XCTAssertEqual(names, ["Muster,\nAlex", "Beispiel,\nBea", "Test,\nCarlo"])
        let cells = try await webView.evaluateJavaScript("document.querySelectorAll('.pfvr-person-cell').length") as? Int
        XCTAssertEqual(cells, 9)
        let unchanged = try await bool("fixtureOriginalSubmit.value==='attend-with-food' && fixtureOriginalSubmit.textContent==='Ich komme, mit Essen' && fixtureOriginalSubmit.dataset.pfvrDisplayLabel==='Ich komme,\\nmit Essen'")
        XCTAssertTrue(unchanged)
        _ = try await webView.evaluateJavaScript("fixtureOriginalSubmit.click();fixtureOriginalInput.click();fixtureOriginalStatus.value='no';fixtureOriginalStatus.dispatchEvent(new Event('change',{bubbles:true}));")
        let payload = try await bool("fixture.submissions.length===1 && fixture.submissions[0].name==='choice' && fixture.submissions[0].value==='attend-with-food' && fixture.submissions[0].cook==='Beispiel Sehrlangerkochname' && fixture.inputClicks===1 && fixture.statusChanges[0]==='no'")
        XCTAssertTrue(payload)
        let cookReadOnly = try await bool("getComputedStyle(document.getElementById('cook')).display==='none' && !!document.querySelector('.pfvr-day-display-value')")
        XCTAssertTrue(cookReadOnly)
        _ = try await webView.evaluateJavaScript("document.getElementById('bulk-action').click()")
        let bulkBlocked = try await bool("fixture.bulkClicks===0")
        XCTAssertTrue(bulkBlocked)
    }

    func testPersonProxyUsesRealSelectAndPersistsOnlyExplicitRestoreValue() async throws {
        try load()
        try await ready()
        let noAutomaticWrites = try await bool("fixture.personChanges.length===0 && Object.keys(JSON.parse(localStorage.getItem('pfvr-attendance-people-v4')).restoreValues).length===0")
        XCTAssertTrue(noAutomaticWrites)
        model.openPeople()
        try await waitFor { try await self.bool("!!document.querySelector('.pfvr-person-tools.open')") }
        _ = try await webView.evaluateJavaScript("var proxy=document.querySelector('[data-pfvr-proxy]');proxy.value='fixture-dora';proxy.dispatchEvent(new Event('change',{bubbles:true}));")
        try await waitFor { try await self.bool("document.querySelectorAll('.pfvr-attendance-mobile .pfvr-person-header').length===4") }
        let forwarded = try await bool("fixture.personInputs[0]==='fixture-dora' && fixture.personChanges[0]==='fixture-dora' && JSON.parse(localStorage.getItem('pfvr-attendance-people-v4')).restoreValues['probe dora']==='fixture-dora'")
        XCTAssertTrue(forwarded)
        try await waitFor { self.model.acceptedPeopleMessages > 0 }
        try await waitFor { self.testStore.loadPeopleState()?.contains("fixture-dora") == true }
        // Reload the initial fixture: only an explicitly saved website option may be restored.
        model.reload()
        try await ready()
        let restored = try await bool("fixture.personChanges.length===1 && fixture.personChanges[0]==='fixture-dora' && document.querySelectorAll('.pfvr-attendance-mobile .pfvr-person-header').length===4")
        XCTAssertTrue(restored)
        // A completely new ephemeral WebView must restore via the device-local native store.
        model.detach()
        webView.removeFromSuperview()
        model = InternalAttendanceModel(store: testStore)
        try load()
        try await ready()
        let afterRestart = try await bool("fixture.personChanges.length===1 && fixture.personChanges[0]==='fixture-dora'")
        XCTAssertTrue(afterRestart)
    }

    func testHeaderFollowsBodyScrollAndReferrerPolicySurvivesParser() async throws {
        try load()
        try await ready()
        _ = try await webView.evaluateJavaScript("var body=document.querySelector('.pfvr-matrix-scroll');body.scrollLeft=100;body.dispatchEvent(new Event('scroll'));")
        try await waitFor { try await self.bool("document.querySelector('.pfvr-matrix-head-scroll').scrollLeft===document.querySelector('.pfvr-matrix-scroll').scrollLeft") }
        let headerInert = try await bool("getComputedStyle(document.querySelector('.pfvr-matrix-head-scroll')).pointerEvents==='none'")
        XCTAssertTrue(headerInert)
        let policy = try await bool("!!document.head.querySelector('meta[name=referrer][content=no-referrer]')")
        XCTAssertTrue(policy)
        _ = try await webView.evaluateJavaScript("var meta=document.createElement('meta');meta.name='referrer';meta.content='unsafe-url';document.head.appendChild(meta);null;")
        try await waitFor { try await self.bool("Array.from(document.querySelectorAll('meta[name=referrer]')).every(n=>n.content==='no-referrer')") }
    }

    func testRemovingExtraPersonPreservesPrimaryAndDoesNotSubmitAttendance() async throws {
        try load()
        try await ready()
        model.openPeople()
        try await waitFor { try await self.bool("!!document.querySelector('.pfvr-person-tools.open')") }
        let primaryProtected = try await bool("!document.querySelector('[data-pfvr-managed-person=\"muster alex\"] .pfvr-local-remove')")
        XCTAssertTrue(primaryProtected)
        _ = try await webView.evaluateJavaScript("document.querySelector('[data-pfvr-managed-person=\"beispiel bea\"] .pfvr-local-remove').click()")
        try await waitFor { try await self.bool("JSON.parse(localStorage.getItem('pfvr-attendance-people-v4')).hidden.includes('Beispiel Bea')") }
        // The fixture rejects renderer navigation; explicitly reload only its local HTML.
        model.reload()
        try await ready()
        let removed = try await bool("document.querySelectorAll('.pfvr-attendance-mobile .pfvr-person-header').length===2 && fixture.submissions.length===0 && fixture.personChanges.length===0")
        XCTAssertTrue(removed)
    }

    func testOriginalModeAndAutomaticFallbackKeepUntouchedPageAvailable() async throws {
        try load(appView: false)
        try await waitFor { !self.model.loading }
        let original = try await bool("!document.querySelector('.pfvr-attendance-mobile') && getComputedStyle(document.getElementById('original-header')).display!=='none'")
        XCTAssertTrue(original)
        model.detach()
        webView.removeFromSuperview()
        try load("<!doctype html><html><head></head><body><header id='fallback-original'>Unbekannter Seitenaufbau</header><button id='fallback-control' onclick='window.fixtureClicked=true'>Weiter</button></body></html>")
        try await waitFor { !self.model.loading && self.model.usedOriginalFallback }
        XCTAssertFalse(webView.isHidden)
        let noProjection = try await bool("!document.querySelector('#pfvr-internal-style') && getComputedStyle(document.getElementById('fallback-original')).display!=='none'")
        XCTAssertTrue(noProjection)
        _ = try await webView.evaluateJavaScript("document.getElementById('fallback-control').click()")
        let clickable = try await bool("window.fixtureClicked===true")
        XCTAssertTrue(clickable)
    }

    func testSwissGermanDarkRendererAndSafelyEncodedPrivateLink() async throws {
        try load(language: "gsw", dark: true)
        try await ready()
        try await captureMatrix("internal-matrix-390pt-gsw-dark")
        let dark = try await bool("getComputedStyle(document.body).backgroundColor==='rgb(17, 23, 28)' && getComputedStyle(document.documentElement).colorScheme==='dark'")
        XCTAssertTrue(dark)
        let originalUntranslated = try await bool("fixtureOriginalSubmit.textContent==='Ich komme, mit Essen'")
        XCTAssertTrue(originalUntranslated)
        var parts = URLComponents(url: initialURL, resolvingAgainstBaseURL: false)!
        parts.queryItems?.append(URLQueryItem(name: "fixture", value: "'\\\n;window.fixtureInjected=true;//"))
        let script = try InternalAttendanceRenderer.script(language: "de", dark: false, initialURL: parts.url!)
        XCTAssertFalse(script.contains("__BASE_INTERNAL_URL__"))
        _ = try await webView.evaluateJavaScript("delete window.__pfvrAttendanceMobileV2;")
        _ = try await webView.evaluateJavaScript(script)
        let injected = try await bool("window.fixtureInjected===true")
        XCTAssertFalse(injected)
    }
}
