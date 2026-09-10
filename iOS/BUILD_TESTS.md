# iOS bauen und prüfen

Stand 2026-09-10. Die erste iOS-Portierung bleibt Testversion `0.12.6`, Bundle-ID `ch.pfvr.app.test`, Mindestversion iOS/iPadOS 17. Das Xcode-Projekt wird ohne Quellcodeänderungen aus `project.yml` erzeugt. SwiftPM verwendet Tools 5.9 und Swift-Sprachmodus 5.

## Ausführung

Voraussetzungen: macOS, vollständiges Xcode mit installierten iPhone-Simulatoren und XcodeGen ≥ 2.42. Ein Apple-Entwicklerkonto oder Signierschlüssel ist für diese Simulatorprüfungen nicht erforderlich.

```bash
swift test --package-path iOS --enable-code-coverage
xcodegen generate --spec iOS/project.yml
bash tools/ios-test.sh compact
bash tools/ios-test.sh large
```

Alternativ `iOS/PFVR.xcodeproj` nach der Generierung in Xcode öffnen, Scheme `PFVR`, iPhone-Simulator und Test wählen. Für Gerät/TestFlight sind ein eigenes Team, passende Bundle-ID, Signierung und ein gesonderter Freigabeprozess erforderlich; die CI erzeugt eine Simulator-App, keine installierbare iPhone-IPA.

`tools/ios-audit.py` prüft Plists und einige Build-Vorgaben. Diese Prüfung ist ausdrücklich kein Ersatz für Kompilierung oder Tests. `tools/ios-test.sh` bricht auf Linux verständlich ab, bevor es einen Build behauptet.

## Automatische Prüfpakete

| Paket | Laufzeit | Zweck |
|---|---|---|
| PFVRCoreTests | SwiftPM/macOS und iOS-Simulator | Fachlogik, Beträge/Swiss-QR, persistenter Warenkorb, Hydrologie, Kalender/Wetter, Parser/Cache und URL-Regeln |
| PFVRAppTests | Gehostet in der echten Simulator-App | WKWebView/JavaScript, Navigation und sichere Speicherung mit lokalen Prüfdaten |
| PFVRUITests | XCUITest auf zwei iPhone-Größen | Freigabe, Navigation, Warenkorb-Neustart, Zahlungsbestätigung, QR-Anzeige, Termine und Sprache |

Vor den Swift-Tests prüft die CI den JavaScript-Export bytegenau gegen den echten Android-Java-Generator und lässt V8 beide Sprachvarianten sowie das WebKit-Fixtureskript parsen. Das prüft Exportdrift und JavaScript-Syntax; DOM-Verhalten bleibt Aufgabe der WebKit-Tests.

Der Workflow `iOS CI` läuft auf `main`, `codex/ios-port-*`, Pull Requests nach `main` und manuell. PR-/Push-Läufe derselben Branch teilen eine Concurrency-Gruppe. Tests laufen ohne automatische Wiederholung fehlgeschlagener Fälle. Beide Simulatorgrößen werden auch dann unabhängig geprüft, wenn eine fehlschlägt. Compact kompiliert zusätzlich die Release-Konfiguration, damit auch die Grenzen der Debug-Testhilfen durch den Compiler geprüft werden.

Die Simulatorauswahl verwendet die neueste vorhandene iOS-Runtime mit zwei unterschiedlichen iPhone-Größen. `compact` bevorzugt SE/mini, sonst ein Standard-iPhone; `large` verwendet Max/Plus. Konkretes Gerät, Runtime und Toolchain werden pro Lauf protokolliert. Compact läuft hell, large dunkel. Das belegt keine Ausführung auf iOS 17, wenn diese Runtime im Runner fehlt.

Ein separater Core-Schritt führt mit `PFVR_LIVE_SMOKE=1` den `LiveSourceSmokeTests`-Vertragstest gegen die öffentlichen Datenquellen aus. Er prüft echte Antworten und verwendet keinen persönlichen Serverzugang. Ein externer Ausfall blockiert die deterministische Prüfung nicht (`continue-on-error`); das eigene Log `live-source-smoke.log` muss deshalb ausdrücklich bewertet werden.

## Nachweise und Grenzen

Jeder Simulatorlauf lädt `.xcresult`, Build-/Testlogs, Coverage-Bericht, exportierte Screenshot-PNGs sowie die unsignierte Simulator-App und das erzeugte Xcode-Projekt als GitHub-Artefakte hoch. Screenshots werden für alle Tabs, Terminansicht, Sprachwahl, QR und Zahlungsbestätigung aufbewahrt. Die Bilder benötigen eine tatsächliche Sichtprüfung; vorhandene Screenshots allein sind kein visueller Qualitätsnachweis.

UI-Tests verwenden `-ui-testing` mit eigener Preferences-Domain und deterministischen öffentlichen Beispieldaten. `-ui-test-reset`, `-ui-test-unlocked` und `-ui-test-pending-payment` sind ausschließlich im Debug-Build wirksam. Die Tests geben keine echte Zahlung frei und verwenden keinen persönlichen Intern-Link. Die QR-Anzeige darf weder Warenkorb noch Zahlungsbestätigungsstatus verändern. Ein extern gestarteter Zahlungsversuch wird für die Bestätigungsprüfung explizit nachgebildet.

Die vollständige reale Zahlung mit Banking-/TWINT-App, persönliche produktive An-/Abmeldung, Apple-Signierung/TestFlight und physische Geräte sind durch Simulatorprüfungen nicht abgedeckt. Bereits durchgeführte Läufe, Ergebnisse und offene Prüfpunkte werden in `PORTING_STATUS.md` dokumentiert; vorbereitete Tests gelten dort erst nach realer Ausführung als bestanden.

## Referenzen

- [Apple: Automatisierung mit xcodebuild, Ergebnissen und Coverage](https://developer.apple.com/videos/play/wwdc2019/413/)
- [XcodeGen: verbindliches Projektformat](https://github.com/yonaskolb/XcodeGen/blob/master/Docs/ProjectSpec.md)
- [GitHub: Runner-Images und installierte Software](https://github.com/actions/runner-images)
- [Apple: Verwendungsgründe für erforderliche APIs](https://developer.apple.com/documentation/bundleresources/describing-use-of-required-reason-api)
