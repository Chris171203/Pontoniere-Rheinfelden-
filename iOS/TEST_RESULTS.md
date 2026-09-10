# iOS-Testnachweise

Aktualisiert: 2026-09-10. Ergebnisse beziehen sich immer auf den angegebenen Commit, nicht automatisch auf spätere lokale Änderungen.

## Abschließend bestandener Quellstand

Geprüfter Produkt- und Testcode: **`2bf0749f490ffe0aee5f050aec7ee6583e79717d`**, [GitHub Actions 34513741048](https://github.com/Chris171203/Pontoniere-Rheinfelden-/actions/runs/34513741048). Alle vier Jobs erfolgreich; Logs, echte XCTest-Abschlussmarken und exportierte `.xcresult`-Zusammenfassungen wurden geprüft. Nach diesem Stand wurden ausschließlich Dokumentationen geändert.

Umgebung: macOS 15.7.9, Xcode 16.4, Apple Swift 6.1.2, Simulator-SDK und Runtime iOS 18.5 (22F77), arm64. Swift-Sprachmodus 5, Deployment-Ziel iOS 17.

| Paket / Gerät | Tatsächlich bestanden | Job | Artefakt-ID |
|---|---|---|---|
| Foundation/macOS | 49 deterministische Fälle und separat vier echte Live-Quellenprüfungen | `102993933226` | `10166892146` |
| iPhone SE (3. Generation), hell | 49 Core + 19 Hosted + 11 UI = **79**, 0 Fehler | `102993932973` | `10167388772` |
| iPhone 16 Pro Max, dunkel | 49 Core + 19 Hosted + 11 UI = **79**, 0 Fehler | `102993933067` | `10167257495` |
| iPad Air 11 Zoll (M2), hell | Ein gezielter nativer Teilen-/Kalendereditor-Test, 0 Fehler | `102993933205` | `10167068246` |

Vier opt-in Live-Fälle werden in jeder Offline-Suite bewusst übersprungen und im Foundation-Job tatsächlich separat ausgeführt. Es gab keine automatische Wiederholung fehlgeschlagener Testfälle. Der kompakte Lauf bestand zusätzlich den Release-Build; alle drei Simulatorziele bestanden Debug-Testbuild, Ad-hoc-Signaturprüfung und Prüfung der tatsächlich eingebetteten eigenen Keychain-Rechte.

Die 19 Hosted-Fälle umfassen acht Hintergrund-Lifecycle-Fälle, sechs echte WebKit-/Formularfälle, zwei Sicherheits-/Keychain-Fälle und drei Zahlungs-/QR-Fälle. Der Swiss-QR-PNG wurde mit Vision vollständig zurückgelesen: offener Betrag, CHF 0.01, 12.50 und 99999.99, einschließlich Empfänger, IBAN, CHF und 34 Payload-Feldern. Ein unabhängiges Kontrollbild prüft den Reader.

Die elf UI-Fälle prüfen Freigabefehler, alle Tabs, beide Rhein-Diagramme mit tatsächlichen Fixturewerten, Termindetails, Sprache und unveränderte Quelltitel, Warenkorb über Neustart/manuelles Leeren, explizite Zahlungsbestätigung mit Nein/Ja, QR-Ansehen ohne Zahlungsfrage, native Systemdialoge, Kacheln und Cache-Löschung ohne Verlust anderer Einstellungen.

### Tatsächliche Bildprüfung

Original-PNGs wurden aus den beiden finalen iPhone-Artefakten gelesen. Beide Pegel-/Abflussgrafiken sind vollständig erfasst, die Temperatur-Zeitachse ist auch auf SE lesbar. Home, Kasse, QR, Zahlungsfrage, native Teilen-/Kalenderdialoge und weitere zuvor geprüfte unveränderte Ansichten zeigen keine erkennbaren Überlappungen oder abgeschnittenen Bedienelemente.

Die internen Matrizen enthalten nach der Scene-Korrektur tatsächliche Inhalte. Deutsch/hell und Schweizerdeutsch/dunkel zeigen die originalen Website-Controls, zwei sichtbare Personenspalten, Namen, Statusfarben und lesbare Beschriftungen. Eine zunächst verdächtige Vorschau ohne Buttontexte wurde direkt anhand der unveränderten PNG-Datei und ihrer Pixel überprüft: Alle Texte sind vorhanden. Archiv und Datei haben identischen SHA-256 `0d22d52e7899c0eac561594700768b0c91c439eab3761ddd2c1aded47f6cc53c`; daraus folgt kein weiterer Produktfehler. Vorsorgliche, noch nicht integrierte Teständerungen wurden verworfen, der grün geprüfte Quellstand blieb unverändert.

Die nachstehenden älteren Läufe dokumentieren die gefundenen und behobenen Fehler; ihre Zwischenstände ersetzen nicht die abschließende Tabelle. GitHub-Testartefakte werden gemäß Workflow 14 Tage aufbewahrt; Quellcode, Testergebnisse und Reproduktionsbefehle bleiben im Repository erhalten.

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

## Signaturnachweis und erkannter Scheinerfolg

- Commit: `533aee2e98ecda6534806aff6fd631c232a67d56`; [Lauf 34509075123](https://github.com/Chris171203/Pontoniere-Rheinfelden-/actions/runs/34509075123).
- Die tatsächlich gebauten Simulator-Executables bestanden die Ad-hoc-Signaturprüfung und enthielten die benötigten Simulator-Keychain-Rechte im Mach-O. Debug-Builds sowie der kompakte Release-Build waren erfolgreich.
- Die iPhone-Jobs meldeten nominal Erfolg, hatten jedoch **keinen einzigen Laufzeittest ausgeführt**: Bash 3.2 brach bei einem leeren optionalen Array unter `set -u` ab, der EXIT-Trap lieferte irrtümlich Status 0. Diese Jobs zählen ausdrücklich nicht als bestandene Tests.
- Der Folgecommit beseitigt das leere Array und erzwingt einen abgeschlossenen Testaufruf, ein echtes `tests.xcresult` und erfolgreich abgeschlossene Core-/App-/UI-Testbundles mit tatsächlich bestandenen Fällen. Auf iPad muss der gezielt ausgewählte Systemeditor-Test selbst bestanden sein.
- Der iPad-Test lief tatsächlich und scheiterte an der nicht exponierten Container-ID `system.share`. Original-Screenshot und UIKit-Accessibility-Baum belegen einen korrekt geöffneten nativen Teilen-Popover mit PNG `PFVR_3.00CHF`, Kopieren und Schließen. Die Prüfung wurde auf diese tatsächlichen nativen Controls umgestellt.

## Tatsächlicher Lauf nach den App-/Simulator-Korrekturen

- Commit: `e57a286558d7edfcf5bf6b64b856c129e1c7d709`; [Lauf 34510327390](https://github.com/Chris171203/Pontoniere-Rheinfelden-/actions/runs/34510327390).
- iPhone SE (3. Generation), iOS 18.5, hell, Job `102982567649`: Debug-Testbuild und Release-Build bestanden. 49 Core-Tests bestanden, vier Live-Fälle bewusst ausgelassen; **alle 19 Hosted-App-Tests bestanden**; neun von elf UI-Tests bestanden.
- iPhone 16 Pro Max, iOS 18.5, dunkel, Job `102982567748`: dieselben 49 Core-, 19 Hosted- und neun von elf UI-Testfälle bestanden. Identische zwei Dialogadapterfehler, kein zusätzlicher Fehler.
- Damit tatsächlich nachgewiesen: alle acht Hintergrund-Lifecycle-Fälle; alle sechs internen WKWebView-Fälle einschließlich Wiederherstellung von Personen, Original-/App-Modus und echten Formular-Controls; beide Sicherheits-/Keychain-Fälle; alle drei Zahlungsfälle einschließlich vollständigem Swiss-QR-PNG-Roundtrip über Vision für offenen Betrag, CHF 0.01, 12.50 und 99999.99 sowie unabhängigem Reader-Kontrollbild.
- UI tatsächlich bestanden: Freigabefehler, sechs Tabs/Mindest-Tapflächen, beide Rhein-Diagramme, Termindetails, persistente Sprache, Warenkorb-Neustart/manuelles Leeren, Kachelanordnung/-sichtbarkeit, Cache-Löschen ohne Warenkorbverlust und QR-Ansehen ohne Zahlungsbestätigung.
- Zwei UI-Adapterfehler blieben: die bekannte Teilen-Container-ID und die von UIKit nicht exponierten Ja-/Nein-Button-IDs. Der originale Zahlungs-Screenshot samt AX-Baum zeigt die echte Frage „War die Zahlung erfolgreich?“ mit Nein/Ja. Der folgende Testadapter fragt deshalb genau diesen Alert und dessen native Antworten ab; auch sämtliche negativen Prüfungen wurden auf das echte Alert-Element umgestellt. Warenkorb- und Neustartassertions wurden beibehalten.

## Native Systemdialoge auf iPad

- Commit: `c04d95a9778dc45c527946b901a48f930f48540f`; [Lauf 34511181146](https://github.com/Chris171203/Pontoniere-Rheinfelden-/actions/runs/34511181146).
- Foundation-Job `102986088657`: 49 deterministische Tests und anschließend **alle vier tatsächlichen Live-Quellentests** bestanden; Live-Dauer 24,678 Sekunden.
- iPad Air 11 Zoll (M2), iOS 18.5, hell, Job `102986088728`: Debug-Testbuild und gezielter UI-Test bestanden (61,980 Sekunden). Apples echter Teilen-Dialog mit bedienbarer Kopieren-Aktion wurde geöffnet und geschlossen, anschließend der echte EventKit-Editor mit editierbarem Quelltitel geöffnet und abgebrochen. Warenkorb blieb nach Prozessneustart erhalten; keine Zahlungsfrage durch Abbruch.
- Die beiden iPhone-Jobs `102986088569` (SE) und `102986088754` (Pro Max) bestanden je 49 Core-, 19 Hosted- und zehn von elf UI-Testfällen. Teilen und Kalender bestanden auch auf beiden iPhones. Einzig die Zahlungsalert-Kennung war noch fehlerhaft; der Folgecommit `3c0aca0` korrigiert diesen Adapter.
- Dieser einzelne gezielte iPad-Test ersetzt keine vollständige iPad-Testmatrix.

## Vollständige Bedienabläufe und WebKit-Nachprüfung

- Commit `3c0aca08227c5f8785669929d5a03ffe2ca0a677`; [Lauf 34512395375](https://github.com/Chris171203/Pontoniere-Rheinfelden-/actions/runs/34512395375).
- Auf iPhone 16 Pro Max dunkel, Job `102989474775`, sind erstmals **alle elf UI-Tests tatsächlich bestanden**, einschließlich Zahlungsfrage, Nein/Ja und Warenkorbzustand über Neustarts. 49 Core-Tests ebenfalls bestanden. Ein Hosted-Fall überschritt beim Laden der Original-WebKit-Fixture sein Zeitlimit (`testOriginalModeAndAutomaticFallbackKeepUntouchedPageAvailable`, bisheriges Testfenster ohne aktive Scene); 18 von 19 Hosted-Fällen bestanden. Das ist kein vollständig erfolgreicher Job.
- iPhone SE, Job `102989474823`: ebenfalls alle elf UI-Tests und 49 Core-Tests bestanden. Der Hosted-Fall `testActualControlsFormPayloadAndListenersSurviveMatrixProjection` überschritt beim Warten auf die lokale Fixture sein Zeitlimit; 18 von 19 Hosted-Fällen bestanden. Diese zwei Scene-losen Läufe sind trotz erfolgreicher Bedienungstests keine vollständige Abnahme.
- Core-Job `102989474574`: 49 Offlinefälle sowie alle vier tatsächlichen Live-Quellenprüfungen bestanden. iPad-Job `102989474834`: gezielter nativer Systemdialogtest erneut bestanden, jetzt auch mit der stärkeren negativen Prüfung des echten Zahlungsalerts.
- Nachprüfstand `2bf0749f490ffe0aee5f050aec7ee6583e79717d`, [Lauf 34513741048](https://github.com/Chris171203/Pontoniere-Rheinfelden-/actions/runs/34513741048): Szene-/Bildkorrekturen und lesbare Temperaturachse; Ergebnis erst nach tatsächlichem Abschluss bewerten.

## Visuelles Review und letzte Korrekturen

- 18 exportierte Originalbilder aus `e57a286` / iPhone SE unabhängig angesehen. Home, Rheinwerte, Termine/Detail, Kasse, QR, Verein, Einstellungen, Kachelanpassung und Intern-Leerzustand zeigten keine erkennbaren Überlappungen oder abgeschnittenen Bedienelemente. Zusätzlich den neun Ansichten umfassenden Kontaktbogen aus `c04d95a` / iPhone 16 Pro Max dunkel angesehen; keine sichtbaren Überlappungen.
- Kleiner tatsächlicher Darstellungsfehler: Die automatische Temperatur-X-Achse kürzte auf SE mehrere Uhrzeiten zu identischen „9. Sept., 1…“. `RiverTemperatureCard` erhält kurze, zur Auswahl passende `HH:mm`-/`dd.MM.`-Labels.
- Zwei Aufnahmelücken: Beide gehosteten internen Matrixbilder waren vollständig transparent; die DOM-/Formulartests waren trotzdem echt bestanden. Das Testfenster wird nun an eine aktive `UIWindowScene` gebunden, auf tatsächliche gezeichnete Frames gewartet und leere/einfarbige Bilder werden zurückgewiesen. Die zwei kleinen Pegel-/Abflussaufnahmen zeigten nur Graphanfänge; der UI-Test verlangt jetzt vollständige Sichtbarkeit zwischen Navigation und Tabs und erfasst zusätzlich die Temperaturkurve.
- Die korrigierten Tests wurden im abschließenden Lauf tatsächlich bestanden; die Originalbilder wurden anschließend geprüft, siehe Tabelle und Bildprüfung oben.

## Lokal ausgeführte Prüfungen

- Der aktuelle Android-Referenzbaum wurde mit GitHub verglichen; Baumhash identisch.
- `python3 tools/ios-audit.py`: Plist-/Konfigurationsaudit bestanden, ausdrücklich keine Kompilierung.
- `bash -n tools/ios-test.sh`: Shell-Syntax bestanden.
- `python3 tools/internal-export-renderer.py --check`: Deutsch und Schweizerdeutsch stimmen mit der Ausgabe des tatsächlichen Android-Java-Generators überein.

## Grenzen der Abnahme

Die Entwicklungsversion ist für die oben benannten Simulatorfälle abgenommen. Nicht nachgewiesen sind physische Geräte, die Mindest-Runtime iOS 17, sämtliche iPad-Abläufe, produktive persönliche Intern-Aktionen, reale Banking-/TWINT-Übernahmen und -Zahlungen sowie die tatsächliche Häufigkeit von iOS-Hintergrundzustellungen. Store-AppIcon und Apple-Produktionssignierung/TestFlight bleiben Release-Aufgaben. Der Simulator-Build ist keine auf einem iPhone installierbare IPA.
