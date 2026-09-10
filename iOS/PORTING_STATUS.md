# iOS-Portierung – Arbeitsstand

Aktualisiert: 2026-09-10. Ausgangspunkt: Android 0.12.6, Commit `c617bae7c1f00fbb1136621166387ed814652794`.

## Ziel und Abnahme

Native iOS-App mit denselben fachlichen Kernfunktionen wie Android. SwiftUI ab iOS 17; gemeinsame Swift-Fachlogik in `PFVRCore`, ohne externe Laufzeitbibliotheken. Android-Quellen und -Build bleiben unverändert. Entwicklungsversion bleibt kleiner als 1.0.0.

Abnahme erfolgt getrennt nach implementierter Funktion, ausgeführtem automatischem Test und weiterhin fehlendem Nachweis. Ein erzeugter Test oder erfolgreicher Compilerlauf ist kein Nachweis für echte Banktransaktionen oder den produktiven Intern-Server.

## Arbeitspakete

| Paket | Umfang | Verantwortlich | Stand |
|---|---|---|---|
| AP1 | Warenkorb, Zahlungszustand, Swiss-QR, Pegelregeln, Kacheln, Freigabe, Sprache | Core-Agent | Implementiert; Fachtests bestanden |
| AP2 | Wetter, Rhein, Kalender, News, Cache und Fehlerfälle | Data-Agent | Implementiert; Fachtests und reale Quellen bestanden |
| AP3 | Native Screens, Navigation, Einstellungen und iOS-Zahlungsübergabe | UI-Agent | Implementiert; fünf von zehn ersten UI-Abläufen bestanden, Accessibility-Korrekturen in Arbeit |
| AP4 | Interne App-/Originalansicht, lokale Zugangsdaten, Website-Controls | Internal-Agent | Vier WebKit- und ein Navigationstest bestanden; Fixture-/Keychain-Korrektur in Arbeit |
| AP5 | Xcode-Projekt, CI, Unit-/WebKit-/Simulator-Tests, Screenshots | Build/Test-Agent | Debug- und Release-Build bestanden; tatsächliche Simulatorfehler werden korrigiert |
| AP6 | Unabhängiger Android-Abgleich und Review | Review-Agent | Sieben materielle Befunde korrigiert und im Quellcode nachgeprüft |
| AP1b | iOS-Hintergrundaktualisierung, Ablaufabbruch und Cache-Löschbarriere | Core-Agent + UI/Build | Acht native Lifecycle-Tests auf beiden Simulatoren bestanden |
| Integration | API-Abgleich, Review, Fehlerbehebung, Testnachweise, Übergabe | Hauptagent | In Bearbeitung |

## Verbindliche fachliche Prüfungen

- Erstfreigabe: öffentliche Landingpage verfügbar, vor Freigabe kein Live-Abruf und keine interne WebView.
- Warenkorb: Änderungen und offene Zahlungsbestätigung überstehen Neustart; Abbruch/Nein erhält den Warenkorb. Kein automatisch behaupteter Zahlungserfolg.
- Swiss QR: CHF-Betrag und Empfänger entsprechen Android; ungültige Beträge werden abgewiesen. iOS nutzt systemeigene Teilen-/Importwege.
- Rhein: nur Basel hat bestätigten cm-Bezug; Grenzwerte 700/790/820 cm; über 60 Minuten alte Messwerte oder Live-Caches ergeben keine aktuelle Freigabe.
- Wetter: nächster laufender/kommender, nicht abgesagter Termin oder früheres Regeltraining; Tagespunkte 06/12/18 aus demselben Cache, UV-Fallback und fehlende Werte korrekt behandeln.
- Kalender/News: Quelle und Datenalter sichtbar; Offline-Fallback; externe Texte unverändert.
- Kacheln: Reihenfolge/Sichtbarkeit persistent, unbekannte IDs bereinigen, neue IDs einordnen, Warenkorb fixieren.
- Intern: App-/Originalansicht, echte Website-Controls, personenbezogene Links nur lokal; Tests ausschließlich mit synthetischer lokaler Website.
- UI: kleine/große Geräte, Hell/Dunkel, Sprache, Navigation, Neustart, Zahlungsrückkehr und Fehlermeldungen automatisch prüfen.

## Ausgangsnachweise

- Android-Referenz: [CI 34444682421](https://github.com/Chris171203/Pontoniere-Rheinfelden-/actions/runs/34444682421), Ergebnis `success` für `c617bae` vor Beginn der Portierung.
- Der lokale rekonstruierte Quellbaum wurde per `git write-tree` mit dem GitHub-Commitbaum verglichen: identisch (`6e9808b2357a0e584a1ecc63f706176632cdf0e8`).
- Lokale Ausführungsumgebung: Linux, kein `swift`, `swiftc` oder `xcodebuild` gefunden. Apple-Build/Simulator werden über macOS-CI ausgeführt; Resultate stehen erst nach tatsächlicher Ausführung fest.

## Testergebnisse

Die konkreten Commit-/Laufzuordnungen stehen in [`TEST_RESULTS.md`](TEST_RESULTS.md). Bislang sind 49 Fachtests, vier tatsächliche öffentliche API-Prüfungen sowie der Java-Generatorvergleich und JavaScript-Syntaxprüfung auf macOS bestanden. Debug-Testbuilds auf iPhone SE (3. Generation) und iPhone 16 Pro Max mit iOS 18.5 sowie der Release-Build sind erfolgreich. Die erste echte Simulator-Ausführung bestand je Gerät 14/18 Hosted- und 5/10 UI-Testfälle. Ihre konkreten Fehler werden korrigiert; bis zum erfolgreichen Folgelauf liegt keine vollständige Simulator-Abnahme vor.

## Plattformunterschiede und externe Abhängigkeiten

Remote-Konfiguration und native Intern-API bleiben wie im Android-Stand geplant, da kein freigegebener Betriebsendpunkt vorliegt. Geräteinstallation/TestFlight benötigt später Apple-Signierung; für Simulator-Tests ist diese nicht erforderlich.

- iOS-Zahlungsübergabe nutzt die System-Teilen-Funktion statt Android-Paketwahl. Ein erfolgreicher Bildimport ist keine automatische Bestätigung einer Zahlung.
- Hintergrundaktualisierung ist vorhanden und standardmäßig eingeschaltet. Die 30 Minuten sind nur eine früheste Anforderung; Ausführungszeit und Verfügbarkeit bestimmt iOS. Die Tests prüfen Bedingungen, Abbruch und Zustand, nicht eine garantierte OS-Zustellung.
- Die interne WebView erlaubt ausschließlich denselben HTTPS-Host. Auch im Originalmodus werden externe Navigation und `mailto`/`tel`/`geo` nicht weitergegeben. Der Inhalt und die echten internen Website-Controls bleiben erhalten. Diese Begrenzung ist keine Kontrolle der internen Serverlogik oder beliebiger Requests des vertrauenswürdigen Website-Skripts.
- Ein Store-taugliches AppIcon-Set und Produktionssignierung fehlen noch. Das vorhandene 96×96-Vereinslogo wird in der App verwendet; der Simulator-Testbuild fordert kein fehlendes Icon-Set an.
- Tests laufen auf tatsächlich vorhandenen, zum Xcode-SDK passenden iPhone-Simulatoren. Mindestversion iOS 17, iPad und echte Geräte sind damit nicht automatisch zur Laufzeit geprüft.

## Fortsetzung

Arbeitsbranch: `codex/ios-port-0.12.6`. Vor Fortsetzung Root-`AGENTS.md`, `PROJECT.md`, `STATUS.md`, diese Datei und `iOS/BUILD_TESTS.md` lesen; offene CI-Fehler anhand des konkreten Commitstands prüfen. Paketstatus und Nachweise werden vor Abschluss dieser Session aktualisiert.
