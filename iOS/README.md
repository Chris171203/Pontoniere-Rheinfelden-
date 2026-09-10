# PFVR für iOS

Native SwiftUI-Portierung des Android-Stands **0.12.6** für iPhone und iPad ab iOS 17. Entwicklungsversion; aktueller Funktions- und Teststatus steht in [`PORTING_STATUS.md`](PORTING_STATUS.md).

## Aufbau

- `Sources/PFVRCore/`: Fachregeln, Zahlungsdaten, Kalender/Wetter/Rhein/News und persistente öffentliche Caches.
- `App/`: SwiftUI-Screens, iOS-Systemintegration und interne WKWebView mit dem übernommenen Android-Renderer.
- `Tests/PFVRCoreTests/`: Fachlogik und Daten-/Fehlerfälle.
- `Tests/PFVRAppTests/`: gehostete App-/WebKit-Tests mit lokaler synthetischer Website.
- `Tests/PFVRUITests/`: Bedienabläufe im iOS-Simulator.
- `project.yml`: XcodeGen-Projektbeschreibung; `Package.swift`: unabhängig testbares Swift-Package.

## Bauen und testen

Benötigt einen Mac mit Xcode und XcodeGen. Die CI führt die gleichen Schritte auf macOS aus. Simulator-Builds benötigen kein Apple-Signierzertifikat.

```sh
swift test --package-path iOS
xcodegen generate --spec iOS/project.yml
bash tools/ios-test.sh compact
bash tools/ios-test.sh large
```

Das erzeugte `iOS/PFVR.xcodeproj` kann anschließend in Xcode geöffnet werden. Eine Installation auf einem echten Gerät benötigt eine passende Apple-Entwicklersignierung. Details und tatsächlich ausgeführte Prüfungen: [`BUILD_TESTS.md`](BUILD_TESTS.md).

## Fortsetzen

Zuerst Root-`AGENTS.md`, `PROJECT.md`, `STATUS.md` und [`PORTING_STATUS.md`](PORTING_STATUS.md) lesen. Die Architekturentscheidung steht in [`../decisions/ios-native-port.md`](../decisions/ios-native-port.md). Persönliche Intern-Links oder Signierschlüssel gehören nicht in Testdaten, Logs oder das Repository.
