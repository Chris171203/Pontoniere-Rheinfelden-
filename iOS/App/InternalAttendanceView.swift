import SwiftUI
import WebKit

struct InternalAttendanceView: View {
    var language: String = "de"
    var openSettings: (() -> Void)? = nil
    @Environment(\.colorScheme) private var colorScheme
    @AppStorage("internal_app_view", store: Self.preferenceStore) private var appView = true
    @State private var initialURL = Self.configuredURL()
    @State private var generation = UUID()
    @StateObject private var model = InternalAttendanceModel()

    private static var preferenceStore: UserDefaults {
        #if DEBUG
        if ProcessInfo.processInfo.arguments.contains("-ui-testing") { return UserDefaults(suiteName: "pfvr.ui.tests")! }
        #endif
        return .standard
    }

    private static func configuredURL() -> URL? {
        #if DEBUG
        if ProcessInfo.processInfo.arguments.contains("-ui-testing") { return nil }
        #endif
        return SecureInternalStore.shared.loadURL()
    }

    private func label(_ de: String, _ gsw: String) -> String { language == "gsw" ? gsw : de }

    var body: some View {
        VStack(spacing: 0) {
            if let initialURL {
                HStack(spacing: 8) {
                    if appView {
                        Button(label("Personen", "Persone")) { model.openPeople() }
                            .accessibilityLabel(label("Personen hinzufügen oder entfernen", "Persone dezuefüege oder entferne"))
                            .disabled(model.usedOriginalFallback)
                    }
                    Button(appView ? "Original" : label("App-Ansicht", "App-Aasicht")) { appView.toggle() }
                    Button { model.reload() } label: { Image(systemName: "arrow.clockwise") }
                        .accessibilityLabel(label("Neu laden", "Neu lade"))
                }
                .buttonStyle(.bordered).controlSize(.large).padding(8)
                if let message = model.message {
                    Text(message).font(.footnote).foregroundStyle(.secondary)
                        .padding(.horizontal).padding(.bottom, 6).accessibilityIdentifier("internalStatus")
                }
                ZStack {
                    InternalWebSurface(model: model, initialURL: initialURL, appView: appView,
                                       language: language, dark: colorScheme == .dark)
                        .id(generation)
                    if model.loading { ProgressView().accessibilityLabel(label("Wird geladen", "Wird glade")) }
                }
            } else {
                ContentUnavailableView {
                    Label(label("Kein Intern-Zugang eingerichtet", "Kein Intern-Zuegang iigrichtet"), systemImage: "person.crop.rectangle")
                } description: {
                    Text(label("Der persönliche PFVR-Link wird unter Einstellungen verwaltet.", "De persönlich PFVR-Link wird under Iistellige verwaltet."))
                } actions: {
                    if let openSettings { Button(label("Zu den Einstellungen", "Zu de Iistellige"), action: openSettings) }
                }
                .accessibilityIdentifier("internal.link.missing")
            }
        }
        .accessibilityIdentifier("screen.internal")
        .onReceive(NotificationCenter.default.publisher(for: .internalSettingsDidChange)) { _ in
            // A changed identity gets a new ephemeral WebView, including fresh cookies and storage.
            model.detach()
            initialURL = Self.configuredURL()
            generation = UUID()
        }
    }
}

private struct InternalWebSurface: UIViewRepresentable {
    let model: InternalAttendanceModel
    let initialURL: URL
    let appView: Bool
    let language: String
    let dark: Bool

    func makeUIView(context: Context) -> WKWebView {
        model.makeWebView(initialURL: initialURL, appView: appView, language: language, dark: dark)
    }

    func updateUIView(_ webView: WKWebView, context: Context) {
        model.update(appView: appView, language: language, dark: dark)
    }

    static func dismantleUIView(_ webView: WKWebView, coordinator: ()) {
        webView.stopLoading()
        webView.navigationDelegate = nil
        webView.configuration.userContentController.removeScriptMessageHandler(forName: "pfvrPeople")
    }
}

@MainActor
final class InternalAttendanceModel: NSObject, ObservableObject, WKNavigationDelegate, WKScriptMessageHandler {
    @Published private(set) var loading = false
    @Published private(set) var message: String?
    @Published private(set) var usedOriginalFallback = false
    private(set) weak var webView: WKWebView?
    private var initialURL: URL?
    private var appView = true
    private var language = "de"
    private var dark = false
    private var originalFallback = false
    private var loadGeneration = UUID()
    private let store: SecureInternalStore
    #if DEBUG
    private var fixtureHTML: String?
    private var fixtureLoadPending = false
    #endif

    init(store: SecureInternalStore = .shared) { self.store = store; super.init() }

    private func label(_ de: String, _ gsw: String) -> String { language == "gsw" ? gsw : de }

    func makeWebView(initialURL: URL, appView: Bool, language: String, dark: Bool, startLoading: Bool = true) -> WKWebView {
        self.initialURL = initialURL
        self.appView = appView
        self.language = language
        self.dark = dark
        let configuration = WKWebViewConfiguration()
        configuration.websiteDataStore = .nonPersistent()
        configuration.defaultWebpagePreferences.allowsContentJavaScript = true
        configuration.preferences.javaScriptCanOpenWindowsAutomatically = false
        configuration.userContentController.add(self, name: "pfvrPeople")
        configuration.userContentController.addUserScript(WKUserScript(source: InternalAttendanceRenderer.bootstrap(peopleState: store.loadPeopleState()),
                                                                        injectionTime: .atDocumentStart, forMainFrameOnly: true))
        let webView = WKWebView(frame: .zero, configuration: configuration)
        webView.navigationDelegate = self
        webView.allowsBackForwardNavigationGestures = false
        webView.isOpaque = false
        webView.accessibilityIdentifier = "internalAttendanceWebView"
        self.webView = webView
        if startLoading { reload() }
        return webView
    }

    func detach() {
        loadGeneration = UUID()
        webView?.stopLoading()
        webView?.navigationDelegate = nil
        webView?.configuration.userContentController.removeScriptMessageHandler(forName: "pfvrPeople")
        webView = nil
        loading = false
    }

    func update(appView: Bool, language: String, dark: Bool) {
        guard self.appView != appView || self.language != language || self.dark != dark else { return }
        self.appView = appView
        self.language = language
        self.dark = dark
        reload()
    }

    func reload() {
        originalFallback = false
        loadCurrentPage()
    }

    private func loadCurrentPage() {
        guard let webView, let initialURL else { return }
        beginLoad(webView)
        #if DEBUG
        if let fixtureHTML {
            fixtureLoadPending = true
            webView.loadHTMLString(fixtureHTML, baseURL: initialURL)
            return
        }
        #endif
        // Reload the current internal page, except after an error or an invalid navigation.
        let url = webView.url.flatMap { InternalNavigationPolicy.permits($0) ? $0 : nil } ?? initialURL
        webView.load(URLRequest(url: url, cachePolicy: .reloadIgnoringLocalCacheData, timeoutInterval: 30))
    }

    #if DEBUG
    /// Local deterministic test path. Navigation out of the fixture is always blocked.
    func loadFixture(_ html: String) { fixtureHTML = html; reload() }
    #endif

    private func beginLoad(_ webView: WKWebView) {
        loadGeneration = UUID()
        loading = true
        if !originalFallback { message = nil }
        usedOriginalFallback = originalFallback
        webView.isHidden = appView && !originalFallback
        webView.overrideUserInterfaceStyle = dark ? .dark : .light
    }

    func openPeople(attempt: Int = 0) {
        guard appView, !originalFallback, let webView else { return }
        webView.evaluateJavaScript(InternalAttendanceRenderer.peopleScript) { [weak self] result, _ in
            guard let self, self.webView === webView else { return }
            if result as? Bool == true { return }
            if attempt >= 2 {
                self.message = self.label("Personenverwaltung ist auf dieser Seite nicht verfügbar.", "Personeverwaltig isch uf dere Siite nöd verfüegbar.")
                return
            }
            if attempt == 0 { self.applyRenderer(webView) }
            DispatchQueue.main.asyncAfter(deadline: .now() + 0.35) { [weak self] in self?.openPeople(attempt: attempt + 1) }
        }
    }

    private func applyRenderer(_ webView: WKWebView) {
        guard let initialURL else { return }
        do {
            let script = try InternalAttendanceRenderer.script(language: language, dark: dark, initialURL: initialURL)
            webView.evaluateJavaScript(script) { [weak self] _, error in
                if error != nil { self?.revealOriginal(webView) }
            }
        } catch { revealOriginal(webView) }
    }

    private func revealOriginal(_ webView: WKWebView) {
        guard self.webView === webView, !originalFallback else { return }
        originalFallback = true
        usedOriginalFallback = true
        message = label("Die App-Ansicht ist hier nicht verfügbar. Die Originalseite wird angezeigt.", "D App-Aasicht isch da nöd verfüegbar. D Originalsiite wird aazeigt.")
        // Reload once without the renderer so a partial DOM rewrite cannot damage the fallback.
        loadCurrentPage()
    }

    private func revealWhenReady(_ webView: WKWebView, generation: UUID, attempt: Int = 0) {
        guard generation == loadGeneration, self.webView === webView else { return }
        if !appView { webView.isHidden = false; loading = false; return }
        webView.evaluateJavaScript(InternalAttendanceRenderer.readyScript) { [weak self] result, _ in
            guard let self, self.loadGeneration == generation, self.webView === webView else { return }
            if result as? Bool == true {
                webView.isHidden = false
                self.loading = false
            } else if attempt >= 24 {
                self.revealOriginal(webView)
            } else {
                DispatchQueue.main.asyncAfter(deadline: .now() + 0.1) { [weak self] in
                    self?.revealWhenReady(webView, generation: generation, attempt: attempt + 1)
                }
            }
        }
    }

    func webView(_ webView: WKWebView, didStartProvisionalNavigation navigation: WKNavigation!) { beginLoad(webView) }

    func webView(_ webView: WKWebView, didFinish navigation: WKNavigation!) {
        guard self.webView === webView else { return }
        if appView && !originalFallback { applyRenderer(webView); revealWhenReady(webView, generation: loadGeneration) }
        else { webView.isHidden = false; loading = false }
    }

    func webView(_ webView: WKWebView, decidePolicyFor action: WKNavigationAction, decisionHandler: @escaping (WKNavigationActionPolicy) -> Void) {
        guard let url = action.request.url else { decisionHandler(.cancel); return }
        #if DEBUG
        if fixtureHTML != nil {
            // loadHTMLString does not fetch its HTTPS base URL. All fixture link/form actions are denied.
            let allow = fixtureLoadPending && action.navigationType == .other && (url.scheme == "about" || url == initialURL)
            fixtureLoadPending = false
            decisionHandler(allow ? .allow : .cancel)
            return
        }
        #endif
        guard InternalNavigationPolicy.permits(url) else { decisionHandler(.cancel); return }
        if action.targetFrame == nil {
            decisionHandler(.cancel)
            webView.load(action.request)
        } else { decisionHandler(.allow) }
    }

    func webView(_ webView: WKWebView, decidePolicyFor response: WKNavigationResponse, decisionHandler: @escaping (WKNavigationResponsePolicy) -> Void) {
        #if DEBUG
        let isFixture = fixtureHTML != nil
        #else
        let isFixture = false
        #endif
        guard isFixture || response.response.url.map(InternalNavigationPolicy.permits) == true else {
            decisionHandler(.cancel)
            return
        }
        if let response = response.response as? HTTPURLResponse, response.statusCode >= 400 {
            decisionHandler(.cancel)
            showLoadError(webView)
        } else { decisionHandler(.allow) }
    }

    func webView(_ webView: WKWebView, didFailProvisionalNavigation navigation: WKNavigation!, withError error: Error) {
        if (error as NSError).code != NSURLErrorCancelled { showLoadError(webView) }
    }
    func webView(_ webView: WKWebView, didFail navigation: WKNavigation!, withError error: Error) {
        if (error as NSError).code != NSURLErrorCancelled { showLoadError(webView) }
    }
    func webViewWebContentProcessDidTerminate(_ webView: WKWebView) { showLoadError(webView) }

    private func showLoadError(_ webView: WKWebView) {
        guard self.webView === webView else { return }
        loadGeneration = UUID()
        loading = false
        webView.isHidden = true
        // NSError's description/userInfo can include the complete personal URL; do not show or log it.
        message = label("Interner Bereich konnte nicht geladen werden. Prüfe den persönlichen Link unter Einstellungen oder tippe auf Neu laden.",
                        "De intern Bereich het nöd chönne glade werde. Prüef de persönlich Link under Iistellige oder tipp uf Neu lade.")
    }

    func userContentController(_ userContentController: WKUserContentController, didReceive message: WKScriptMessage) {
        guard let webView, userContentController === webView.configuration.userContentController,
              message.name == "pfvrPeople", message.frameInfo.isMainFrame,
              message.frameInfo.securityOrigin.protocol == "https",
              message.frameInfo.securityOrigin.host.lowercased() == "intern.pfvr.ch",
              [0, 443].contains(message.frameInfo.securityOrigin.port),
              let body = message.body as? [String: Any], let value = body["value"] else { return }
        do {
            if value is NSNull { try store.savePeopleState(nil) }
            else if let raw = value as? String { try store.savePeopleState(raw) }
        } catch {
            self.message = label("Die Personenansicht konnte nicht lokal gespeichert werden.", "D Personeaasicht het nöd chönne lokal gspeicheret werde.")
        }
    }
}
