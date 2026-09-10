# PFVRCore shared interface — AP1

Foundation, Swift 5.9. Names below are public; implementation is owned by AP1.

- `CashCatalog.loadBundled() throws -> CashCatalog`, `init(data: Data) throws`; `.currency`, `.title`, `.validFrom`, `.categories: [CashCategory]`; `.item(id:) -> CashItem?`, `.total(quantities: [String:Int]) -> CashMoney`.
- `CashCategory`: Identifiable/Codable/Equatable, `.id`, `.label`, `.items`. `CashItem`: `.id`, `.name`, `.variant`, `.price: CashMoney`, `.deposit`, `.displayName`.
- `CashMoney`: Codable/Equatable/Comparable, `.cents: Int64`, `.decimalString` (POSIX two decimals), `.formatted` (`CHF 12.50`); `init(cents:)`.
- `CashCartStore(defaults: UserDefaults = .standard)`: synchronous, `.state: CashCartState` (`.quantities`, `.paymentConfirmationPending`), `setQuantity(_:for:)`, `clear()`, `markExternalHandoff(fromCart:launched:)`, `confirmPayment(successful:)`. Each mutation writes the entire state atomically to a single UserDefaults data value. Call handoff ONLY after OS accepts a cart-origin external open/share. QR display/save/copy/free-amount actions must not arm confirmation. User confirmation is never a verified payment result.
- `CashCartState(quantities: [String:Int] = [:], paymentConfirmationPending: Bool = false)`, Codable/Equatable; `.itemCount`, `.isEmpty`.
- `PaymentAmount(raw: String) throws`: empty/zero = open amount; rejects negative, nonnumeric/nonfinite, > CHF 100000; rounds positive decimal input to cents, `.money: CashMoney?`, `.qrField`, `.fileName`; `init(money: CashMoney?) throws`.
- `PaymentDetails`: static `.iban`, `.payee`, `.note`, `.twintURL`, `.twintQRURL`; `shareText(amount:)`. `PaymentQR.payload(amount:) -> String` produces the Android-compatible Swiss QR CRLF payload with structured creditor address.
- `HydroStation`: String/Codable/CaseIterable/Identifiable, `.baselRheinhalle = "2289"`, `.rheinfelden = "2091"`; `.id`, `.label`, `.supportsTemperature`, `.stationURL`.
- `RiverDisplay.gaugeCentimetres(station:metresAboveSea:) -> Double?`; `graphLevelValue(station:metresAboveSea:centimetres:) -> Double`; `graphLevelUnit(station:centimetres:) -> String`; `hasVerifiedGaugeCentimetres(_:) -> Bool`. Only Basel has a verified centimetre datum (240 m).
- `RhineNavigation.Stage`: `.unknown`, `.normal`, `.hwmI`, `.hwmIIb`, `.hwmIIa`; `.shortLabel`, `.detail`; `stage(gaugeCentimetres: Double?)`, `currentStage(gaugeCentimetres:measurement:cacheUpdated:now:)` (`Date?`, default now), `isCurrent(measurement:cacheUpdated:now:)`; `thresholdGraphValue(stage:centimetres:) -> Double?`; static `.officialThresholdStages`, `.maximumCurrentAge` (3600 s).
- `HydroMath.niceAxis(_ values: [Double?]) -> AxisScale` (`.min,.max,.step`), `.stats(_:) -> Stats?` (`.count,.first,.last,.min,.max,.mean,.change`), `.nearestIndex(times:[Date], target:Date) -> Int?`, `.periodSeconds(_ code:String) -> TimeInterval`.
- `TileArea`: `.home`, `.cash`, `.club`; `.label`. `TileSpec`: `.id`, `.area`, `.label`, `.width` (`.wide`/`.compact`), `.pinned`. `TileLayoutStore(defaults:)`: `ordered(_ area:)`, `isVisible(_ spec:)`, `setVisible(_:for:)`, `move(area:id:delta:)`, `reset(_ area:)`; static `specs(_:)`, `normalizeOrder(area:requested:)`, `sanitizeHidden(area:requested:)`, `moveOrder(area:current:id:delta:)`.
- `AccessGate.normalize(_:)`, `.matches(_ candidate:String) -> Bool`, `.matchesDigest(_:expectedSHA256:)`, `.sha256Hex(_:)`. Same offline first-use digest as Android; no server authorization implied.
- `PublicLinks`: URL constants `.site`, `.join`, `.facebook`, `.instagram`, `.board`, `.history`, `.program`, `.contact`, `.news`, `.riverNavigation`.
- `AccessLinkPolicy.isPFVRHost(_:)`, `.isInternalPFVRHost(_:)`, `.mayStayInPublicWebView(_ url:URL)`, `.mayStayInInternalWebView(_:)`, `.mayOpenExternally(_:)`, `.validatedInternalURL(_ raw:String) -> URL?`. Internal link requires exact HTTPS intern.pfvr.ch, no credentials/unexpected port, and `what=abmeldung`.
- `AppLanguage`: `.german="de"`, `.swissGerman="gsw"`. `Language.translate(_ appOwnedText:String, mode:AppLanguage) -> String`; call ONLY for app-owned labels. Calendar/news/person/source content must bypass localization.

AP2 owns all calendar/weather/news/HTTP/cache data types and policies; AP1 supplies only station identity, display maths and navigation thresholds.
