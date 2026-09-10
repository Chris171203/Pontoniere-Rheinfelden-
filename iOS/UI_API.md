# iOS native UI — AP3

Native SwiftUI, iOS 17+, Swift 5.9. App UI files live in `App/`; public business/data models are imported from `PFVRCore`. UI package does not replace the Android source or introduce an online account system.

## App lifecycle and persistence

`AppState` is a main-actor observable object. The only objects constructed before first-use acceptance are local presentation preferences and the public landing page. `AccessGate.matches` accepts the Android-compatible local code; only after acceptance does `startIfNeeded()` construct the cash/tile stores and public-data service. The internal WKWebView is instantiated only when its tab is selected after acceptance.

`startIfNeeded()` hydrates all public snapshots from disk before any network load. Refresh fetches weather, calendar, news and both hydrology stations, while stable tile IDs preserve scroll/layout identity. The active scene retries according to service TTLs every five minutes; a pull-to-refresh and the 44-point button at Rhein aktuell force refresh. Basel freshness is independently re-evaluated every 30 seconds so an open screen stops showing a current stage after the 60-minute limit. BackgroundRefresh registers the native task at app start but schedules/runs public work only after unlock and while the local background toggle is enabled. iOS chooses actual delivery times. Settings can explicitly clear only the public cache and reload it; cart, preferences and private access remain intact.

Theme, language, station selection, graph range and Basel units use local preferences. Cash quantities and pending payment confirmation use `CashCartStore`; every change persists. Tile order and visibility use `TileLayoutStore`; the cart remains pinned. The personal internal URL is stored via `SecureInternalStore` in Keychain, only from Settings.

## Screens and platform adapters

- `AppShellView`: direct six-tab navigation (Home, Rhein, Termine, Intern, Kasse, Verein), with Settings always available. Each bottom navigation target is at least 44 points tall. A compact label keeps all six destinations directly reachable on an iPhone.
- `LandingView`: public joining/social information, German/Swiss German selector, secure code field and local rejection state. Joining appears only before unlock.
- `HomeView`: source-aware next-event weather, 06/12/18 three-day forecast, river summary/graphs, upcoming calendar and native news. Forecast and calendar dates use Europe/Zurich. External titles and details bypass app localization; app-generated regular-training titles and weekdays are localized.
- `RiverView`: real two-axis flow/level Canvas graph with independent scales, historical Basel stage colors, thresholds when in range, 1h/24h/7d range, selection tooltip, separate Swift Charts temperature plot. BAFU parameters are `Q`, `W`, `WT`; W stays metres above sea internally. Only Basel permits the verified centimetre conversion. Measurement/cache freshness and authoritative navigation link remain visible.
- `CalendarView`: native event list/details, native share, map route URL, `EKEventEditViewController` for explicit user review/save. Presenting the editor does not silently create an event. Multi-day/timed and exclusive-end all-day dates use Zurich calendar boundaries, including DST.
- `ClubView`: native news headlines/excerpts, club/about/contact actions and `SFSafariViewController` for public program/board/history/source articles. Original Android PFVR logo is reused as the `PFVRLogo` asset.
- `CashView`: identical bundled catalog, capped 0–99 item quantities, persistent cart, free/open amount, native Swiss QR image/share/export and official TWINT page. Product source names remain unchanged.

## Payment rendering and handoff

`PaymentQRCode.image(amount: PaymentAmount) throws -> UIImage` uses Core Image QR generation with correction M, an integer scale, a four-module white quiet zone and centred Swiss cross. `PaymentQRCode.file(image:amount:)` writes a reusable named temporary PNG for native share/export.

`PaymentSharePolicy.shouldConfirm(activityType: UIActivity.ActivityType?, completed: Bool) -> Bool` rejects cancellation, missing target, local copy/save/print/markup utilities. A completed external share accepted from the cart arms a persistent confirmation only; it never implies bank payment success. Explicit export, QR display and free amount do not arm it. Payment sheet dismissal checks pending state even when a share extension returns without a scene transition. Opening the official TWINT page arms only after iOS accepts the external open. App activation and restart restore the pending yes/no question. Only explicit yes or manual cart clear empties the cart; no preserves it.

iOS does not reproduce Android package discovery or promise that a banking app accepts images. The system share sheet is the integration point; installed-bank end-to-end acceptance remains a real-device validation item.

## Deterministic UI tests

All launch hooks are enclosed in `#if DEBUG`; Release ignores them.

- `-ui-testing`: isolated `pfvr.ui.tests` UserDefaults suite; deterministic 2026-09-10 fixture time; no public network service; internal view shows the missing-link state without loading real credentials.
- `-ui-test-reset`: clears only that suite before launch, for independent cases.
- `-ui-test-unlocked`: seeds the local gate state for tests of unlocked functions.
- `-ui-test-pending-payment`: if the restored cart is nonempty, seeds the persistent pending state using the ordinary cart API.

Fixtures include 192 hourly forecast values, three calendar events (including external title `Warenkorb` for localization regression), native news and Q/W/WT values for both stations. They are never used in Release.

Stable accessibility identifiers:

| Area | IDs |
| --- | --- |
| Gate | `screen.landing`, `gate.code`, `gate.unlock`, `gate.error` |
| Navigation | `tab.home`, `tab.river`, `tab.events`, `tab.internal`, `tab.cash`, `tab.club`, `screen.{tab}` |
| Settings | `settings.open`, `settings.done`, `settings.language.de`, `settings.language.gsw`, `settings.theme`, `settings.tiles`, `settings.tiles.detail`, `settings.backgroundRefresh`, `settings.cache.clear`, `settings.cache.clear.confirm` |
| Tiles | `tiles.area.home/cash/club`, `tiles.toggle.{id}`, `tiles.up.{id}`, `tiles.down.{id}` |
| Internal config | `settings.internal.url`, `settings.internal.save`, `settings.internal.result` |
| Weather/River | `home.weather`, `home.weather.threeDays`, `river.refresh`, `river.range`, `river.basel.unit`, `river.station.{stationID}`, `river.graph.{stationID}` |
| Events | `event.{id}`, `event.detail`, `event.share`, `event.calendar`, `event.route` |
| Cart | `cart.add.{itemId}`, `cart.remove.{itemId}`, `cart.total`, `cart.clear`, `cart.clear.confirm`, `cash.cart` |
| Payment | `payment.qr`, `payment.twint`, `payment.qr.image`, `payment.share`, `payment.done`, `payment.free.amount`, `payment.confirm.yes`, `payment.confirm.no` |

## Deliberate platform differences / remaining device checks

Native Safari and EventKit replace Android intent/WebView adapters. Foreground and opportunistic native background refresh are implemented; iOS determines background execution times. The app uses a native canvas graph with equivalent metrics/scales rather than pixel-identical Android rendering. Store AppIcon variants and production signing/entitlements are separate release packaging work. A simulator can verify QR image decode, UI, persistence and local flows but cannot certify a third-party banking app or live personal attendance mutation. See the root iOS port/testing status for actually executed checks; this document describes interfaces, not test results.
