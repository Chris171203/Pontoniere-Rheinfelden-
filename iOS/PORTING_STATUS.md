# iOS-Portierung – Arbeitsstand

Aktualisiert: 2026-09-10. Ausgangspunkt: Android 0.12.6, Commit `c617bae7c1f00fbb1136621166387ed814652794`.

## Ziel und Abnahme

Native iOS-App mit denselben fachlichen Kernfunktionen wie Android. SwiftUI ab iOS 17; gemeinsame Swift-Fachlogik in `PFVRCore`, ohne externe Laufzeitbibliotheken. Android-Quellen und -Build bleiben unverändert. Entwicklungsversion bleibt kleiner als 1.0.0.

Abnahme erfolgt getrennt nach implementierter Funktion, ausgeführtem automatischem Test und weiterhin fehlendem Nachweis. Ein erzeugter Test oder erfolgreicher Compilerlauf ist kein Nachweis für echte Banktransaktionen oder den produktiven Intern-Server.

## Arbeitspakete

| Paket | Umfang | Verantwortlich | Stand |
|---|---|---|---|
| AP1 | Warenkorb, Zahlungszustand, Swiss-QR, Pegelregeln, Kacheln, Freigabe, Sprache | Core-Agent | In Umsetzung |
| AP2 | Wetter, Rhein, Kalender, News, Cache und Fehlerfälle | Data-Agent | In Umsetzung |
| AP3 | Native Screens, Navigation, Einstellungen und iOS-Zahlungsübergabe | UI-Agent | In Umsetzung |
| AP4 | Interne App-/Originalansicht, lokale Zugangsdaten, Website-Controls | Internal-Agent | In Umsetzung |
| AP5 | Xcode-Projekt, CI, Unit-/WebKit-/Simulator-Tests, Screenshots | Build/Test-Agent | In Umsetzung |
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

## Externe Abhängigkeiten

Remote-Konfiguration und native Intern-API bleiben wie im Android-Stand geplant, da kein freigegebener Betriebsendpunkt vorliegt. Geräteinstallation/TestFlight benötigt später Apple-Signierung; für Simulator-Tests ist diese nicht erforderlich.

## Fortsetzung

Arbeitsbranch: `codex/ios-port-0.12.6`. Vor Fortsetzung Root-`AGENTS.md`, `PROJECT.md`, `STATUS.md`, diese Datei und `iOS/BUILD_TESTS.md` lesen; offene CI-Fehler anhand des konkreten Commitstands prüfen. Paketstatus und Nachweise werden vor Abschluss dieser Session aktualisiert.
