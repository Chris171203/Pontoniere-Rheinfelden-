# iOS-Port – unabhängiges Review AP6

Stand: 2026-09-10, laufende Implementierungsprüfung. Referenz: Android 0.12.6 (`c617bae`).

## Vorgehen und Grenzen

Quellcodevergleich von `AGENTS.md`, `PROJECT.md`, `STATUS.md`, Android-Fachlogik und iOS-Implementierung. Automatische Rechenschritte werden gesondert bezeichnet. Dieses Review ist kein Nachweis für einen ausgeführten iOS-Simulator, produktive Intern-Anmeldungen oder echte Bankzahlungen; tatsächliche Build-/Testnachweise stehen in `BUILD_TESTS.md`.

## Materielle Befunde

| ID | Priorität | Befund und Nutzerwirkung | Nachweis / Status |
|---|---|---|---|
| R1 | P1 | Die direkte Double-Umrechnung von `248.20 m ü.M.` ergibt `819.9999999999989 cm`; der Vergleich `>= 820` klassifiziert den exakten Grenzwert als Sperre IIb statt IIa. Aus Android übernommener Fehler, den reine Tests mit dem Literal `820.0` übersehen. | Rechnung am 10.09.2026 mit Python ausgeführt; iOS-Quellpfad `RiverDisplay.gaugeCentimetres` → `RhineNavigation.stage` geprüft. Korrektur und kombinierter Grenzwerttest bei AP1 angefordert. |
| R2 | P2 | Ungültige `DTSTART`-Felder bzw. unvollständige `VEVENT`-Blöcke wurden im Kalenderparser still übersprungen. Ein kaputter Feed konnte dadurch wie ein erfolgreicher leerer Kalender wirken und den letzten brauchbaren Cache ersetzen; ungültige Wiederholungszahlen wurden teilweise durch gültige Vorgaben ersetzt. Analog konnten vollständig ungültige News-Zeilen als erfolgreiche leere News-Liste gespeichert werden. | Kontrollfluss in `CalendarParser.parse`/`occurrences` und `NewsParser.parse` geprüft. Strikte Unterscheidung zwischen echtem leerem Feed und defekten Datensätzen sowie Regressionstests bei AP2 angefordert. |
| R3 | P2 | Die Wetterkacheln nannten fest `MeteoSwiss/Open-Meteo`, auch wenn der Datenservice vollständig auf `Open-Meteo Best Match` zurückfiel. Dadurch war die Herkunft des angezeigten Wetters falsch bezeichnet. | Vergleich `PFVRDataService.loadWeather` mit `HomeView`/`SourceStamp`; AP3 auf die tatsächlich gespeicherte `metadata.source` verwiesen. |

## Verbindliche Prüfpunkte

- Vor erfolgreicher Erstfreigabe keine Live-Requests, internen WebViews oder personenbezogenen Dateninitialisierung.
- Fehlerhafte/fehlende Feedwerte werden nicht als erfundene aktuelle Messwerte ausgegeben; erfolgreicher leerer Kalender darf alte Termine entfernen, Netzwerkfehler erhält gekennzeichneten Cache.
- Aktuelle Basel-Stufe verlangt sowohl höchstens 60 Minuten alten Messwert als auch Cache. Zukünftige Zeitstempel ergeben keine aktuelle Stufe. Rheinfelden hat keinen erfundenen cm-Bezug oder Sperrstatus.
- Mengenänderungen und offene Zahlungsbestätigung bleiben über Neustarts erhalten; nur bestätigter Erfolg oder manuelles Leeren löscht den Warenkorb. Freie Beträge, QR-Anzeige, Kopieren und Speichern lösen keine Zahlungsbestätigung aus.
- Persönlicher Intern-Link wird ausschließlich lokal gespeichert und weder als globales JS-Datum noch in Logs, über fremde WebView-Ziele oder ungeschützte Backups offengelegt. Native Anpassung verwendet die echten Website-Controls.
- Externe Kalender-, News-, Personen- und Quelltexte bleiben von der App-Sprachübersetzung ausgenommen.
- Fachliche Android-Parität wird anhand tatsächlich vorhandener Screens und Aktionen überprüft; vorbereitete APIs allein gelten nicht als fertige Funktion.

## Fortsetzung

Die Implementierung trifft paketweise ein. Die final geprüften Bereiche, behobenen Befunde und verbleibenden Lücken werden vor Abschluss hier ergänzt.
