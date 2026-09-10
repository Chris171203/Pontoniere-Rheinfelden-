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

## Lokal ausgeführte Prüfungen

- Der aktuelle Android-Referenzbaum wurde mit GitHub verglichen; Baumhash identisch.
- `python3 tools/ios-audit.py`: Plist-/Konfigurationsaudit bestanden, ausdrücklich keine Kompilierung.
- `bash -n tools/ios-test.sh`: Shell-Syntax bestanden.
- `python3 tools/internal-export-renderer.py --check`: Deutsch und Schweizerdeutsch stimmen mit der Ausgabe des tatsächlichen Android-Java-Generators überein.

## Noch ausstehend

Vollständiger integrierter Simulatorlauf einschließlich App-/WebKit-/UI-Tests, Release-Build, QR-PNG-Decodierung und Sichtprüfung der exportierten Screenshots. Reale Bankzahlungen, produktive Intern-Aktionen, physische Geräte und signierte Verteilung sind separate Nachweise.
