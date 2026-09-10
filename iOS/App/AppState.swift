import SwiftUI
import PFVRCore

@MainActor
final class AppState: ObservableObject {
    let defaults: UserDefaults
    let testing: Bool
    @Published private(set) var unlocked: Bool
    @Published var tab: AppTab = .home
    @Published var language: AppLanguage { didSet { defaults.set(language.rawValue, forKey: "ui_language") } }
    @Published var theme: String { didSet { defaults.set(theme, forKey: "theme_mode") } }
    @Published var firstStation: HydroStation { didSet { defaults.set(firstStation.rawValue, forKey: "river_station_1") } }
    @Published var secondStation: HydroStation { didSet { defaults.set(secondStation.rawValue, forKey: "river_station_2") } }
    @Published var secondStationEnabled: Bool { didSet { defaults.set(secondStationEnabled, forKey: "river_second_enabled") } }
    @Published var range: HydroRange { didSet { defaults.set(range.rawValue, forKey: "river_range") } }
    @Published var baselCentimetres: Bool { didSet { defaults.set(baselCentimetres, forKey: "river_basel_cm") } }
    @Published var backgroundRefresh: Bool {
        didSet {
            defaults.set(backgroundRefresh, forKey: "background_refresh")
            scheduleBackgroundRefresh()
        }
    }
    @Published private(set) var weather: Loaded<[WeatherHour]>?
    @Published private(set) var events: Loaded<[PFVREvent]>?
    @Published private(set) var news: Loaded<[NewsArticle]>?
    @Published private(set) var rivers: [HydroStation: HydroDataset] = [:]
    @Published private(set) var loading = false
    @Published private(set) var failures: [String] = []
    @Published private(set) var catalog: CashCatalog?
    @Published private(set) var cart = CashCartState()
    @Published private(set) var layoutRevision = 0
    @Published var showPaymentConfirmation = false
    private var service: PFVRDataService?
    private var cartStore: CashCartStore?
    private var tiles: TileLayoutStore?
    private var activated = false

    init() {
        #if DEBUG
        let args = ProcessInfo.processInfo.arguments
        testing = args.contains("-ui-testing")
        defaults = testing ? UserDefaults(suiteName: "pfvr.ui.tests")! : .standard
        if testing && args.contains("-ui-test-reset") { defaults.removePersistentDomain(forName: "pfvr.ui.tests") }
        if testing && args.contains("-ui-test-unlocked") { defaults.set(true, forKey: "access_unlocked_v1") }
        #else
        testing = false
        defaults = .standard
        #endif
        unlocked = defaults.bool(forKey: "access_unlocked_v1")
        language = AppLanguage(rawValue: defaults.string(forKey: "ui_language") ?? "de") ?? .german
        theme = defaults.string(forKey: "theme_mode") ?? "system"
        firstStation = HydroStation(rawValue: defaults.string(forKey: "river_station_1") ?? "2091") ?? .rheinfelden
        secondStation = HydroStation(rawValue: defaults.string(forKey: "river_station_2") ?? "2289") ?? .baselRheinhalle
        secondStationEnabled = defaults.object(forKey: "river_second_enabled") as? Bool ?? true
        range = HydroRange(rawValue: defaults.string(forKey: "river_range") ?? "day") ?? .day
        baselCentimetres = defaults.bool(forKey: "river_basel_cm")
        backgroundRefresh = defaults.object(forKey: "background_refresh") as? Bool ?? true
    }

    var colorScheme: ColorScheme? { theme == "dark" ? .dark : theme == "light" ? .light : nil }
    var now: Date {
        #if DEBUG
        if testing { return PFVRDate.parseLocal("2026-09-10T12:00")! }
        #endif
        return Date()
    }
    var activeStations: [HydroStation] {
        secondStationEnabled && firstStation != secondStation ? [firstStation, secondStation] : [firstStation]
    }
    var total: CashMoney { catalog?.total(quantities: cart.quantities) ?? CashMoney(cents: 0) }
    var upcoming: [PFVREvent] { (events?.value ?? []).filter { $0.end > now }.sorted { $0.start < $1.start } }
    var nextWeatherEvent: PFVREvent? { CalendarPolicy.nextWeatherEvent(events: events?.value ?? [], now: now) }
    func ui(_ label: String) -> String { Language.translate(label, mode: language) }

    func unlock(code: String) -> Bool {
        guard AccessGate.matches(code) else { return false }
        defaults.set(true, forKey: "access_unlocked_v1")
        unlocked = true
        return true
    }

    /// No service, internal view, cache load or refresh exists before local first-use acceptance.
    func startIfNeeded() async {
        guard unlocked, !activated else { return }
        activated = true
        scheduleBackgroundRefresh()
        tiles = TileLayoutStore(defaults: defaults)
        cartStore = CashCartStore(defaults: defaults)
        cart = cartStore!.state
        do { catalog = try CashCatalog.loadBundled() }
        catch { failures = [ui("Die Preisliste konnte nicht geladen werden.")] }
        #if DEBUG
        if testing {
            seedFixtures()
            if ProcessInfo.processInfo.arguments.contains("-ui-test-pending-payment"), !cart.isEmpty {
                cartStore?.markExternalHandoff(fromCart: true, launched: true)
                cart = cartStore!.state
            }
            checkPaymentConfirmation()
            return
        }
        #endif
        let directory = FileManager.default.urls(for: .applicationSupportDirectory, in: .userDomainMask)[0]
            .appendingPathComponent("PFVR/PublicCache", isDirectory: true)
        service = PFVRDataService(cacheDirectory: directory)
        if let service {
            weather = await service.cachedWeather()
            events = await service.cachedEvents()
            news = await service.cachedNews()
            rivers[.rheinfelden] = await service.cachedHydro(station: .rheinfelden)
            rivers[.baselRheinhalle] = await service.cachedHydro(station: .baselRheinhalle)
        }
        checkPaymentConfirmation()
        await refresh()
    }

    func refresh(force: Bool = false) async {
        guard unlocked, let service, !loading else { return }
        loading = true
        failures = []
        defer { loading = false }
        async let weatherRequest = service.weather(force: force)
        async let eventRequest = service.events(force: force)
        async let newsRequest = service.news(force: force)
        async let rheinfeldenRequest = service.hydro(station: .rheinfelden, force: force)
        async let baselRequest = service.hydro(station: .baselRheinhalle, force: force)
        do { weather = try await weatherRequest } catch { failures.append(ui("Wetter konnte nicht aktualisiert werden.")) }
        do { events = try await eventRequest } catch { failures.append(ui("Kalender konnte nicht aktualisiert werden.")) }
        do { news = try await newsRequest } catch { failures.append(ui("News konnten nicht aktualisiert werden.")) }
        rivers[.rheinfelden] = await rheinfeldenRequest
        rivers[.baselRheinhalle] = await baselRequest
    }

    func clearPublicCache() async {
        guard unlocked, !loading else { return }
        loading = true
        await BackgroundRefresh.shared.cancelAndWait()
        do {
            if let service { try await service.clearCache() }
            weather = nil; events = nil; news = nil; rivers = [:]; failures = []
            #if DEBUG
            if testing { seedFixtures(); loading = false; return }
            #endif
            loading = false
            await refresh(force: true)
        } catch {
            loading = false
            failures = [ui("Der Daten-Cache konnte nicht gelöscht werden.")]
        }
    }

    func scheduleBackgroundRefresh() {
        BackgroundRefresh.shared.schedule(unlocked: unlocked, enabled: backgroundRefresh, testing: testing)
    }

    func quantity(_ item: CashItem) -> Int { cart.quantities[item.id] ?? 0 }
    func setQuantity(_ amount: Int, for item: CashItem) {
        cartStore?.setQuantity(max(0, min(99, amount)), for: item.id)
        if let cartStore { cart = cartStore.state }
    }
    func clearCart() {
        cartStore?.clear()
        if let cartStore { cart = cartStore.state }
        showPaymentConfirmation = false
    }
    func paymentHandoff(fromCart: Bool, launched: Bool) {
        cartStore?.markExternalHandoff(fromCart: fromCart, launched: launched)
        if let cartStore { cart = cartStore.state }
    }
    func checkPaymentConfirmation() {
        guard unlocked, activated else { return }
        showPaymentConfirmation = cart.paymentConfirmationPending
    }
    func confirmPayment(_ successful: Bool) {
        cartStore?.confirmPayment(successful: successful)
        if let cartStore { cart = cartStore.state }
        showPaymentConfirmation = false
    }
    func orderedTiles(_ area: TileArea) -> [TileSpec] { tiles?.ordered(area) ?? TileLayoutStore.specs(area) }
    func visibleTiles(_ area: TileArea) -> [TileSpec] { orderedTiles(area).filter { tiles?.isVisible($0) ?? true } }
    func tileVisible(_ tile: TileSpec) -> Bool { tiles?.isVisible(tile) ?? true }
    func setTileVisible(_ visible: Bool, tile: TileSpec) { tiles?.setVisible(visible, for: tile); layoutRevision += 1 }
    func moveTile(_ tile: TileSpec, by delta: Int) { tiles?.move(area: tile.area, id: tile.id, delta: delta); layoutRevision += 1 }
    func resetTiles(_ area: TileArea) { tiles?.reset(area); layoutRevision += 1 }

    #if DEBUG
    private func seedFixtures() {
        let start = AppDates.zurich.startOfDay(for: now)
        let weatherHours = (0..<192).map { index in
            WeatherHour(time: start.addingTimeInterval(Double(index) * 3600), temperature: 17 + 5 * sin(Double(index % 24) / 24 * .pi), precipitationProbability: index % 8 == 0 ? 40 : 10, precipitation: index % 8 == 0 ? 0.4 : 0, wind: 9, gust: 18, uv: 4, weatherCode: index % 8 == 0 ? 61 : 2)
        }
        weather = Loaded(value: weatherHours, metadata: CacheMetadata(source: "MeteoSwiss/Open-Meteo · Testdaten", updatedAt: now))
        events = Loaded(value: [
            PFVREvent(id: "ui-training", title: "Vereinstraining", location: "Rheinweg 42, 4310 Rheinfelden", details: "Gemeinsames Training auf dem Rhein.", start: PFVRDate.parseLocal("2026-09-10T18:30")!, end: PFVRDate.parseLocal("2026-09-10T20:00")!),
            PFVREvent(id: "ui-event", title: "Endfahren", location: "Depot PFVR, Rheinfelden", details: "Vereinsanlass am Rhein.", start: PFVRDate.parseLocal("2026-09-12T00:00")!, end: PFVRDate.parseLocal("2026-09-13T00:00")!, allDay: true),
            PFVREvent(id: "ui-source-text", title: "Warenkorb", details: "Unveränderter externer Quelltext für den Sprachtest.", start: PFVRDate.parseLocal("2026-09-13T18:00")!, end: PFVRDate.parseLocal("2026-09-13T20:00")!)
        ], metadata: CacheMetadata(source: "PFVR Vereinskalender · Testdaten", updatedAt: now))
        news = Loaded(value: [NewsArticle(id: 1001, publishedAt: now, title: "Gemeinsam auf dem Rhein", excerpt: "Ein Rückblick auf das Vereinsleben und die nächste gemeinsame Ausfahrt.", url: PublicLinks.news)], metadata: CacheMetadata(source: "PFVR WordPress · Testdaten", updatedAt: now))
        for station in HydroStation.allCases {
            var samples: [HydroObservation] = []
            for index in 0..<169 {
                let date = now.addingTimeInterval(Double(index - 168) * 3600)
                samples.append(HydroObservation(time: date, value: station == .rheinfelden ? 268.4 + sin(Double(index) / 15) * 0.1 : 245.2 + sin(Double(index) / 15) * 0.12, parameter: "W"))
                samples.append(HydroObservation(time: date, value: 780 + sin(Double(index) / 15) * 30, parameter: "Q"))
                if station.supportsTemperature { samples.append(HydroObservation(time: date, value: 20.5 + sin(Double(index) / 12), parameter: "WT")) }
            }
            let loaded = Loaded(value: samples, metadata: CacheMetadata(source: "BAFU · Testdaten", updatedAt: now))
            rivers[station] = HydroDataset(station: station, live: loaded, fine: loaded, history: loaded)
        }
    }
    #endif
}
