# iOS-Testnachweise

Aktualisiert: 2026-09-10. Ergebnisse beziehen sich immer auf den angegebenen Commit, nicht automatisch auf spätere lokale Änderungen.

## Erster Compiler- und Datenquellentest

- Commit: `126997b8a69a2107d127f4064b9a02ddd72052b2`.
- [GitHub Actions 34503310212](https://github.com/Chris171203/Pontoniere-Rheinfelden-/actions/runs/34503310212), Job `Foundation unit tests` (`102959157086`).
- Tatsächliche Umgebung laut Log: Xcode 16.4, Apple Swift 6.1.2, macOS-15-Runner; Swift-Sprachmodus 5.
- Deterministisch: 22 Fachtests bestanden, 0 Fehler. Vier opt-in Netzwerktests wurden in diesem Schritt ausdrücklich übersprungen.
- Anschließend: dieselben vier Netzwerktests mit `PFVR_LIVE_SMOKE=1` tatsächlich ausgeführt; alle bestanden (23,43 Sekunden).

| Öffentliche Quelle | Tatsächliches Parserergebnis |
|---|---|
| BAFU Basel 2289 | 274 Live-, 238 Zehnminuten-, 371 Stundenwerte |
| BAFU Rheinfelden 2091 | 343 Live-, 360 Zehnminuten-, 557 Stundenwerte |
| Vereinskalender | 26 expandierte Termine |
| PFVR WordPress | 20 Artikel |
| Wetter | 192 Stunden; MeteoSwiss ICON, UV über Open-Meteo Best Match |

Diese Zahlen beschreiben den konkreten Abruf und sind keine dauerhaft erwarteten Zählwerte.

Die zwei Simulatorjobs dieses frühen Zwischenstands endeten vor der App-Kompilierung: XcodeGen fand den noch nicht enthaltenen Ordner `Tests/PFVRAppTests` nicht. Daraus folgt kein bestandenes App-/Simulatorergebnis. Der Ordner samt Tests wurde danach ergänzt; erneute Ausführung erforderlich.

## Integrierter Zwischenstand

- Commit: `9f7b81a965a2411f05e4518dde55ee6adce74df8`.
- [GitHub Actions 34503980734](https://github.com/Chris171203/Pontoniere-Rheinfelden-/actions/runs/34503980734), Foundation-Job `102961465938`.
- 49 Fachtests bestanden, 0 Fehler; vier Netzwerktests in diesem Schritt planmäßig übersprungen. Anschließend alle vier Live-Netzwerktests tatsächlich bestanden.
- Android-Renderer-Bytevergleich und JavaScript-Syntaxprüfung jetzt auch auf dem macOS-Runner bestanden.
- Die Simulator-Builds scheiterten an der XcodeGen-Vorgabe `AppIcon`, obwohl bisher nur das Vereinslogo als Bildressource vorlag. Zusätzlich hatte die Auswahl einen iOS-26.2-Simulator mit Xcode 16.4 kombiniert; dieser Lauf ist kein iOS-Funktionstestnachweis.
- Korrekturen danach: fehlende AppIcon-Vorgabe für den Testbuild entfernt; Simulatorauswahl auf die vom aktiven Xcode-SDK unterstützten Versionen begrenzt. Store-Icon bleibt ein eigener offener Release-Punkt.

## Kompilierung des vollständigen App-Ziels

- Commit: `245088a29a785c2a7d7ed905caf4ad6d04a4cccb`.
- [GitHub Actions 34505001223](https://github.com/Chris171203/Pontoniere-Rheinfelden-/actions/runs/34505001223).
- Fachtests und reale Quellen erneut bestanden. Die Simulatorauswahl verwendete nun nachweislich iPhone SE (3. Generation), iOS 18.5 und Xcode-SDK 18.5; der vorherige Asset-Katalogfehler war behoben.
- Der Compiler meldete zwei zu komplexe Swift-Ausdrücke in `AppState.seedFixtures` und `WeatherSummary.body`. Beide wurden in kleinere, explizit typisierte Schritte zerlegt. Die große Variante wurde nach dem ersten eindeutigen Fehlerbericht zugunsten des korrigierten Laufs abgebrochen.

## Lauf mit Compilerkorrekturen

- Commit: `2c3b0a90b41db74d323f1cc47e884444589fe3a4`.
- [GitHub Actions 34505470755](https://github.com/Chris171203/Pontoniere-Rheinfelden-/actions/runs/34505470755).
- Foundation-Job `102966775198`: 49 deterministische Tests, vier separate Live-Quellenprüfungen, drei Simulatorauswahl-Regressionsfälle und Java-/JavaScript-Prüfungen bestanden.
- Simulatorjobs `102966775479` (kompakt) und `102966774915` (groß): Debug-Testbuild auf beiden Geräten bestanden; kompakter Release-Build ebenfalls bestanden. App, Core-, Hosted- und UI-Testziele sind damit tatsächlich unter Xcode kompiliert und im Simulator ausgeführt.
- Je Simulator: 49 deterministische Core-Tests bestanden, vier Netzwerkfälle planmäßig übersprungen; 14 von 18 Hosted-Testfällen bestanden, vier Testfälle mit insgesamt fünf Assertions-/Fehlermeldungen fehlgeschlagen; fünf von zehn Bedienungstests bestanden.
- Bestanden: sämtliche acht Hintergrund-Lebenszyklustests; echte interne Form-Controls, Entfernen, Original-/Fallbackmodus, Schweizerdeutsch-/Dunkel-Renderer; HTTPS-Navigationsregeln; Zahlungs-Share-Regeln. UI: alle sechs Tabs mit Mindest-Tapflächen, ungültige Erstfreigabe, Termindetails, Sprachpersistenz samt unverändertem Quelltext und beide Rhein-Diagramme.
- Konkrete Fehler: `cart.total` war für vier UI-Abläufe nicht als eigenständiges Accessibility-Element auffindbar; der Kachelschalter änderte im Test seinen Wert nicht; ein WebKit-Test gab einen nicht serialisierbaren DOM-Knoten zurück; die Personen-Fixture wartete vergeblich auf einen Zustand; der Keychain-Roundtrip meldete `unavailable`; Vision lieferte nach einem `e5rt ... OPERATION ERROR` keine QR-Erkennung.
- Diese Ergebnisse sind keine vollständige Abnahme. Die Fachassertions bleiben erhalten; Accessibility/Testbedienung, Fixture und Simulator-Signierung werden gezielt korrigiert. Der QR-Test erhält eine explizite CPU-/Revisionskonfiguration und ein separates Kontrollbild, um Readerfehler von tatsächlich unlesbaren QR-Bildern zu unterscheiden.

## Folgelauf nach Simulator-Korrekturen

- Commit: `99e3dbec1fb4e4466a415b7911ae1a6f24f73fad`.
- [GitHub Actions 34508226404](https://github.com/Chris171203/Pontoniere-Rheinfelden-/actions/runs/34508226404).
- Foundation-Job `102975560599`: deterministische Fachtests und die vier separat ausgeführten Live-Quellenprüfungen erneut bestanden. Vier Simulatorauswahl-Regressionsfälle decken jetzt auch iPad und den Ausschluss einer zu neuen Runtime ab.
- Simulatorjobs: `102975560372` (kompakt), `102975560831` (groß), `102975560662` (iPad). Die iPhone-Läufe enthalten sämtliche Core-/Hosted-/UI-Tests; iPad führt gezielt den echten System-Teilen-/Kalendereditor-Test mit Abbruch aus. Der kompakte Lauf baut zusätzlich Release.
- Die drei Simulatorjobs schlugen fehl. Der kompakte Log belegt einen erfolgreichen Debug-Testbuild und danach einen Fehler im zusätzlichen Signaturvalidator: Er suchte Simulator-Rechte in der macOS-Codesign-Plist. Xcode 16.4 bindet diese separat in den Mach-O-Abschnitt `__TEXT,__entitlements` ein; die normale Codesign-Plist darf leer sein. In diesem Job begannen weder Release-Build noch Laufzeittests.
- Der Validator wurde daraufhin gezielt korrigiert: echte Ad-hoc-Signatur weiterhin prüfen und die Simulator-Rechte aus dem tatsächlich gebauten Executable lesen. Der SecItem-Roundtrip bleibt als unabhängiger harter Laufzeitnachweis erhalten. Dies ist kein Gerätezertifikat und keine Produktionssignierung.

## Lokal ausgeführte Prüfungen

- Der aktuelle Android-Referenzbaum wurde mit GitHub verglichen; Baumhash identisch.
- `python3 tools/ios-audit.py`: Plist-/Konfigurationsaudit bestanden, ausdrücklich keine Kompilierung.
- `bash -n tools/ios-test.sh`: Shell-Syntax bestanden.
- `python3 tools/internal-export-renderer.py --check`: Deutsch und Schweizerdeutsch stimmen mit der Ausgabe des tatsächlichen Android-Java-Generators überein.

## Noch ausstehend

Vollständiger integrierter Simulatorlauf einschließlich App-/WebKit-/UI-Tests, Release-Build, QR-PNG-Decodierung und Sichtprüfung der exportierten Screenshots. Reale Bankzahlungen, produktive Intern-Aktionen, physische Geräte und signierte Verteilung sind separate Nachweise.
