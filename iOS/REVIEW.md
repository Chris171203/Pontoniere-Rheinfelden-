# iOS-Port – unabhängiges Review AP6

Stand: 2026-09-10, Quellprüfung einschließlich nachgereichter Hintergrundaktualisierung abgeschlossen. Referenz: Android 0.12.6 (`c617bae`).

## Vorgehen und Grenzen

Quellcodevergleich von `AGENTS.md`, `PROJECT.md`, `STATUS.md`, Android-Fachlogik und iOS-Implementierung. Automatische Rechenschritte werden gesondert bezeichnet. Dieses Review ist kein Nachweis für einen ausgeführten iOS-Simulator, produktive Intern-Anmeldungen oder echte Bankzahlungen; tatsächliche Build-/Testnachweise stehen in `BUILD_TESTS.md`.

## Materielle Befunde

| ID | Priorität | Befund und Nutzerwirkung | Nachweis / Status |
|---|---|---|---|
| R1 | P1 | Die direkte Double-Umrechnung von `248.20 m ü.M.` ergibt `819.9999999999989 cm`; der Vergleich `>= 820` klassifiziert den exakten Grenzwert als Sperre IIb statt IIa. Aus Android übernommener Fehler, den reine Tests mit dem Literal `820.0` übersehen. | Rechnung am 10.09.2026 mit Python ausgeführt. Im iOS-Code durch `1e-9 cm` reine Rechentoleranz korrigiert; kombinierte Umrechnungs-/Stufentests und knapp unterhalb liegende Grenzfälle in `RiverDomainTests` ergänzt. Android bleibt unverändert; der Fall ist für eine separate Korrektur reproduzierbar dokumentiert. Testausführung siehe `BUILD_TESTS.md`. |
| R2 | P2 | Ungültige `DTSTART`-Felder bzw. unvollständige `VEVENT`-Blöcke wurden im Kalenderparser still übersprungen. Ein kaputter Feed konnte dadurch wie ein erfolgreicher leerer Kalender wirken und den letzten brauchbaren Cache ersetzen; ungültige Wiederholungszahlen wurden teilweise durch gültige Vorgaben ersetzt. Analog konnten vollständig ungültige News-Zeilen als erfolgreiche leere News-Liste gespeichert werden. | Parser korrigiert: ungültige Zeitfelder/Zeitzonen, unvollständige Termine und fehlerhafte Regeln führen zum Fehlerpfad; echte leere Feeds bleiben erlaubt. News braucht mindestens einen gültigen Artikel bei nichtleerer Antwort. `VALUE=DATE-TIME` wird zudem korrekt als Zeitangabe behandelt. Quellprüfung und ergänzte Parser-/Cachetests, Ausführung siehe `BUILD_TESTS.md`. |
| R3 | P2 | Die Wetterkacheln nannten fest `MeteoSwiss/Open-Meteo`, auch wenn der Datenservice vollständig auf `Open-Meteo Best Match` zurückfiel. Dadurch war die Herkunft des angezeigten Wetters falsch bezeichnet. | Im Quellcode korrigiert: Beide Wetterkacheln verwenden die tatsächliche `metadata.source`; der Datenservice erhält auch die gesonderte UV-Herkunft. |
| R4 | P2 | Der App-Start nutzte die vorhandenen Cache-Lese-APIs nicht vor dem Netzabruf. Bei veraltetem Cache und fehlender Verbindung blieben bereits gespeicherte Daten bis zum Ablauf mehrerer Abruf-Timeouts unsichtbar. | Im Quellcode korrigiert: `startIfNeeded` liest Wetter, Termine, News und beide Rhein-Caches vor dem eigenen Live-Refresh ein. Die Datenservice-Tests decken den erhaltenen alten Datenstand und unveränderte Zeitstempel beim fehlgeschlagenen Refresh ab. |
| R5 | P2 | Die Termin-Detailansicht zeigte bei mehrtägigen Terminen mit Uhrzeit lediglich das Startdatum und die Enduhrzeit; das Enddatum fehlte. Eine Prüfung der Ganztagsdauer in Sekunden behandelte zusätzlich einen einzelnen 25-Stunden-Tag bei Zeitumstellung wie mehrere Tage. | Im Quellcode korrigiert: mehrtägige Uhrzeit-Termine zeigen beide Datums-/Zeitangaben. Ganztagszeiträume verwenden Zürcher Kalendertage und berücksichtigen das exklusive Enddatum. |
| R6 | P2 | Android aktualisiert den öffentlichen Cache auch bei geschlossener App über einen aktivierbaren WorkManager-Job; iOS besass zunächst nur den Timer der aktiven App. Das ist eine tatsächliche Funktionslücke: Vor einem Offline-Start konnte kein neuerer Hintergrund-Cache entstehen. | Im Quellcode ergänzt und geprüft: `BGAppRefresh` mit Schalter, Registrierung beim Start, erneuter Freigabe-/Schalterprüfung bei Zustellung, frühestens 30 Minuten und erneutem Einplanen. Hintergrundarbeit nutzt denselben öffentlichen Cache, eine eigene Session ohne persistente Cookies sowie Abbruch und genau einmaligen Abschluss. Ablauf-/Schaltertests ergänzt. OS-Ausführung bleibt best effort; keine garantierte 30-Minuten-Ausführung. |
| R7 | P2 | Die Android-Einstellung `Daten-Cache leeren` fehlte in iOS, obwohl die Datenservice-Funktion existiert. Damit fehlte der manuelle Wiederherstellungsweg bei unbrauchbaren öffentlichen Daten. | Im Quellcode ergänzt: bestätigte Löschung ausschließlich der öffentlichen Caches, anschliessender erzwungener Abruf. Warenkorb, Erstfreigabe, Kacheln und persönliche Intern-Daten bleiben getrennt. Ein im Review erkannter Timer-/Löschkonflikt wurde durch Setzen des Busy-Status vor dem ersten `await` verhindert. UI-Regression für erhaltenen Warenkorb ergänzt; ausgeführtes Ergebnis siehe Testnachweise. |

## Bereits kontrollierte Zusammenhänge

- Der Freigabebildschirm konstruiert keine Datenservices oder interne WebViews. `startIfNeeded` prüft die Freigabe und initialisiert danach Cache, Warenkorb und Kacheln.
- Die beiden initialen SwiftUI-Tasks können gleichzeitig anlaufen; der MainActor-`loading`-Schutz verhindert doppelte Refreshs. Zusätzlich bündelt der Datenservice parallele Anfragen pro Ressource und Rhein-Station.
- Warenkorb und offene Rückfrage liegen gemeinsam in einem lokal gespeicherten Datenobjekt. Mengen/Preise werden in Cent gerechnet; Rundung erfolgt mit `Decimal`, negative/ungültige Zahlungsbeträge werden abgewiesen.
- Die Intern-Ansicht verwendet einen nicht persistenten WebKit-Datenspeicher. Initiallink und explizite Personenpräferenzen liegen im nicht synchronisierten Keychain mit `WhenUnlockedThisDeviceOnly`. Die native Nachrichtenbrücke prüft Hauptrahmen und exakten HTTPS-Ursprung; rohe Ladefehler werden wegen möglicher persönlicher URL-Inhalte nicht ausgegeben.
- Die Intern-Navigationssperre gilt für Dokumentnavigationen und neue Fenster. Eine Referrer-Meta-Regel ist zusätzlicher Schutz für Ressourcenabrufe; sie ist keine Kontrolle über beliebige Datenübertragung durch vertrauenswürdige Website-Skripte oder den Server selbst.
- Bewusste Android/iOS-Abweichung: Innerhalb der Intern-WebView blockiert iOS auch im Originalmodus externe Ziele einschließlich `mailto`, `tel` und `geo`. Android konnte diese kontrolliert extern öffnen. Die separaten nativen Vereinskontakte bleiben in iOS verfügbar; der Originalmodus bedeutet keine uneingeschränkte externe Navigation.
- Ein früher Abgleich fand unterschiedliche Rhein-Parameternamen in UI-Testdaten und produktivem Parser. Die Fixtures und die Oberfläche verwenden inzwischen einheitlich die BAFU-Codes `W`, `Q`, `WT`; Bildschirmtests sollen zusätzlich tatsächliche Messwerte prüfen.
- Diagrammcode verwendet zwei getrennte dynamische Zahlenskalen für Pegel und Abfluss; die Temperaturkurve ist getrennt. Quellcodekontrolle ersetzt keine Prüfung gerenderter Achsen/Screenshots.
- Die Home-News-Kachel verwendet tatsächlich den gemeinsamen News-Datenstand einschließlich Herkunft und Stand; sie ist kein reiner Platzhalter. Der 44-Punkte-Refresh an `Rhein aktuell` lädt Wetter und Rhein neu. Basel bewertet das Datenalter inzwischen zusätzlich in einer 30-Sekunden-`TimelineView`, unabhängig vom 5-Minuten-Abrufrhythmus.
- Ein erneuter automatischer Abgleich aller statisch erkennbaren `state.ui("…")`-Beschriftungen gegen die Schwiizerdütsch-Ressource fand nach den Korrekturen keine fehlenden Einträge. Dynamische Quelltexte sind hiervon bewusst nicht erfasst; die Kollisionsfälle werden separat getestet.

## Verbindliche Prüfpunkte

- Vor erfolgreicher Erstfreigabe keine Live-Requests, internen WebViews oder personenbezogenen Dateninitialisierung.
- Fehlerhafte/fehlende Feedwerte werden nicht als erfundene aktuelle Messwerte ausgegeben; erfolgreicher leerer Kalender darf alte Termine entfernen, Netzwerkfehler erhält gekennzeichneten Cache.
- Aktuelle Basel-Stufe verlangt sowohl höchstens 60 Minuten alten Messwert als auch Cache. Zukünftige Zeitstempel ergeben keine aktuelle Stufe. Rheinfelden hat keinen erfundenen cm-Bezug oder Sperrstatus.
- Mengenänderungen und offene Zahlungsbestätigung bleiben über Neustarts erhalten; nur bestätigter Erfolg oder manuelles Leeren löscht den Warenkorb. Freie Beträge, QR-Anzeige, Kopieren und Speichern lösen keine Zahlungsbestätigung aus.
- Persönlicher Intern-Link wird ausschließlich lokal gespeichert und weder als globales JS-Datum noch in Logs, über fremde WebView-Ziele oder ungeschützte Backups offengelegt. Native Anpassung verwendet die echten Website-Controls.
- Externe Kalender-, News-, Personen- und Quelltexte bleiben von der App-Sprachübersetzung ausgenommen.
- Fachliche Android-Parität wird anhand tatsächlich vorhandener Screens und Aktionen überprüft; vorbereitete APIs allein gelten nicht als fertige Funktion.

## Abschluss und verbleibende Nachweise

Die sieben aufgeführten materiellen Befunde sind im integrierten Quellcode adressiert. Die Quellprüfung fand danach keinen weiteren offenen P1-/P2-Befund in den geprüften Kernabläufen. Das ist keine pauschale Fehlerfreiheitserklärung: Ausschlaggebend bleiben der vollständige Compiler-/Simulatorlauf des jeweiligen Commitstands und die Sichtprüfung seiner Screenshots.

Die tatsächlichen Läufe und ihre Commitzuordnung stehen in `TEST_RESULTS.md` und `PORTING_STATUS.md`; die verwendeten Verfahren und nicht abgedeckten Gerätefälle in `BUILD_TESTS.md`. Dieses Review übernimmt keine Testzahlen früherer Zwischenstände als Nachweis für spätere Änderungen.

Reale Banking-/TWINT-Übernahme, persönliche produktive Intern-Aktionen, vom Betriebssystem tatsächlich ausgelöste Hintergrundintervalle, physische Geräte, eine gegebenenfalls nicht installierte iOS-17-Runtime sowie Signierung/TestFlight bleiben getrennte Nachweise. Der BG-Lebenszyklus lässt sich mit injiziertem Scheduler/Taskhandle testen; daraus folgt keine bestimmte Zustellhäufigkeit auf einem iPhone.

Der geerbte Android-Grenzwertfehler R1 bleibt dort bewusst unverändert und ist separat korrigierbar. Die strengere externe Navigation im iOS-Internbereich und das native iOS-Teilen anstelle der Android-Bankpaket-Erkennung sind dokumentierte Plattformunterschiede.
