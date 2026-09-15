# Abnahme 0.15.2 – gemeinsame Wettervorhersage

Stand: 2026-09-15. Android **0.15.2 / 70**, iOS **0.15.2 / 4**. Entwicklungsversion unter 1.0.0. [PR #35](https://github.com/Chris171203/Pontoniere-Rheinfelden-/pull/35) wurde mit Merge `2efd0224405e50a734076d670f806ab3a288649a` nach `main` übernommen.

## Korrektur

0.15.1 hatte die Anforderung falsch als eigene Wetterdarstellung je Termin umgesetzt. 0.15.2 zeigt alle relevanten Termintitel und Uhrzeiten innerhalb einer einzigen Wetterkachel. Gleiche oder überlappende Intervalle werden für die Wetterauswertung zusammengeführt. Mittag und Abend ergeben Punkte innerhalb derselben Vorhersage. Die gemeinsame Zusammenfassung zählt betroffene Stunden einmal und lässt Lücken zwischen Terminen aus. Keine zusätzlichen Wetterabrufe.

Die Auswahl laufender und kommender Termine, Absagefilter, Regeltraining-Ergänzung und Begrenzung mehrtägiger Anlässe auf den ausgewählten Zürcher Kalendertag bleiben bestehen. App-eigene Texte verwenden die bestehenden deutschen/schweizerdeutschen Übersetzungen; öffentliche Termintitel bleiben unverändert.

## Android

[CI 213](https://github.com/Chris171203/Pontoniere-Rheinfelden-/actions/runs/34952967257) auf Quellstand `6678d093c4e31a0babd1b8e205a112e70acfc2f5` bestanden. Der tatsächliche synthetische PR-Merge-Build ist `de1b75ffa5fff9cdde7528071b2bbb6c255a152c`.

- 135 Testfälle aus heruntergeladenen XML-Berichten, keine Fehler und keine Skips. Darunter tatsächliche Robolectric-Ansichtstests mit beiden Titeln, einer gemeinsamen Wetterkarte und einmal gezählter Regenmenge bei identischen Terminen.
- Policy-Tests für identische/überlappende Intervalle, getrennte Mittag-/Abendzeiten ohne Lückenstunden und Ganztagesanlässe. Schweizerdeutsche Überschrift und unveränderte öffentliche Titel geprüft.
- Vier Browserfälle sowie `testDebugUnitTest`, `lintRelease`, `assembleDebug` und `bundleRelease` bestanden. Paket, Version und öffentliche Testsignatur bestätigt.
- Ein älterer Quelltexttest erwartete noch das entfernte Wetterlayout. Er prüft nun die unveränderten öffentlichen Titel in der gemeinsamen Karte; die neue Funktionsprüfung bestand bereits im ersten Lauf.

[Test-APK-Artefakt](https://github.com/Chris171203/Pontoniere-Rheinfelden-/actions/runs/34952967257/artifacts/10390386143): 3 560 288 Byte. SHA-256 `ef218e11bfca3cc5304b584c7040fe84540c2550282bb13978b0b33e825980f9`. Download gegen CI-Hash und APK-ZIP geprüft. Bestehendes Testzertifikat: `0521e6bc43e2868177609dea69d074ed14bb594bad28adee98293e586d3d46bf`.

Der Android-Verzeichnisbaum `0c6281cb7e10594f1b6b08d26d30317df6495c59` ist auch im späteren Quellstand `16c1cd35e02facffd64f3f9bfb561573fb3f630e` identisch; dessen erneute [Android-CI](https://github.com/Chris171203/Pontoniere-Rheinfelden-/actions/runs/34953315662) besteht ebenfalls.

## iOS

[CI 51](https://github.com/Chris171203/Pontoniere-Rheinfelden-/actions/runs/34954762045) auf `05c6f00e56c05f099e11c7a3a9abaef8d74423db`:

- 68 deterministische Swift-Package-Tests bestanden. Fünf Live-Quellenfälle werden im regulären Package-Lauf absichtlich ausgelassen und bestanden separat mit Live-Zugriff.
- Unsigniertes arm64-Gerätearchiv mit Version 0.15.2 / Build 4, Produktmetadaten und AppIcon-Prüfung bestanden.
- Compact: 68 Core + 22 Hosted + 17 UI = 107 bestanden, 0 Fehler. Die fünf separaten Live-Fälle erscheinen zusätzlich als Skips. Release-Simulator-Build bestanden.
- iPad: alle 17 UI-Fälle ohne Fehler/Skips bestanden.
- Large: 106 bestanden, ein Timeout im bestehenden Warenkorb-Neustarttest, fünf separate Live-Skips. Alle drei Wetter-UI-Fälle bestanden; insgesamt bestanden 16 von 17 UI-Fällen; der Large-Gesamtlauf ist damit **nicht grün**.
- Originalaufnahmen für identische Terminzeiten und Mittag/Abend auf allen drei Simulatorprofilen angesehen: beide Titel stehen über einer gemeinsamen Prognose, Schweizerdeutsch und Hell/Dunkel sind lesbar.

Der Large-Timeout trat nach einem langsamen Simulator-/Automation-Start auf: 44,6 Sekunden bis zum ersten Home-Control, während das Testbudget insgesamt 60 Sekunden beträgt. Beim anschliessenden App-Neustart wurde dieses Budget überschritten. Derselbe unveränderte Warenkorb-Fall bestand im vorherigen Large-Lauf in 29,5 Sekunden sowie im aktuellen Compact-/iPad-Lauf. Der Wetterfix ändert keine Warenkorb- oder Neustartlogik. Der Befund wird als verbleibende Einschränkung der Gesamtabnahme geführt; Zeitlimits und erforderliche Prüfungen wurden nicht herabgesetzt oder umgangen.

Artefakte: [Compact](https://github.com/Chris171203/Pontoniere-Rheinfelden-/actions/runs/34954762045/artifacts/10391064314), [iPad](https://github.com/Chris171203/Pontoniere-Rheinfelden-/actions/runs/34954762045/artifacts/10390094799), [Large mit Timeout-Protokoll](https://github.com/Chris171203/Pontoniere-Rheinfelden-/actions/runs/34954762045/artifacts/10390874496).

Eine zusätzliche Testkennung der gemeinsamen Zusammenfassung war im Accessibility-Baum nicht auffindbar. Die aufgenommenen Ansichten zeigten die richtige gemeinsame Darstellung. Die Tests prüfen stattdessen die sichtbare einzelne Temperaturanzeige bei identischen Zeiten und die gemeinsamen Mittag-/Abendpunkte ohne getrennte Intervallanzeigen.

Die ersten Simulator-Builds scheiterten an `.count` auf einem `XCUIElement` in der neuen Testabfrage. Die korrigierte Abfrage zählt über `XCUIElementQuery`. Diese fehlgeschlagenen Läufe sind kein UI-Nachweis.

## Grenzen

Keine physische Geräteprüfung. Das iOS-Gerätearchiv ist keine installierbare IPA; Apple-Signierung/Provisionierung bleibt erforderlich. Synthetische Intern- und Zahlungsfälle belegen keine persönlichen produktiven Aktionen. Historische Nachweise: [0.15.1](checks-0.15.1.md), [0.15.0](checks-0.15.0.md).
