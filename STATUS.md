# Status

Stand: 2026-09-15 · Android **0.15.2 / Versionscode 70** · iOS **0.15.2 / Build 4**.

## Aktueller Stand

- PR #35 ist nach `main` übernommen. Eine gemeinsame Wettervorhersage zeigt alle laufenden oder kommenden Termine des nächsten relevanten Tages mit ihren Titeln und Uhrzeiten.
- Gleiche und überlappende Zeiträume werden einmal ausgewertet. Mittag und Abend erscheinen als Punkte innerhalb derselben Vorhersage; Lückenstunden und doppelte Regenmengen entfallen. Bei einem einzelnen Abendtermin werden keine Mittagswerte angezeigt.
- Kalenderauswahl, Absagefilter und Regeltraining-Ergänzung bleiben bestehen. Deutsch/Schweizerdeutsch gepflegt; öffentliche Titel unverändert. Kein zusätzlicher Wetterabruf je Termin.
- Die freigegebene Vereinsansicht aus 0.15.0 bleibt enthalten: direktes Training, native Themenartikel, Kontakt-/Social-Symbole, früher Refresh und unmittelbare An-/Abmeldefarben.

## Workflow-Bereinigung 2026-09-15

- Alte Actions-Historie bereinigt: **451 von 481 Läufen gelöscht**, 30 geschützte Prüf-/Buildläufe plus erfolgreicher Löschbericht verbleiben. Bestand per API gegen Schutzliste geprüft; temporäre Bereinigungsdateien entfernt. [Nachweis](docs/cleanup-workflow-runs-2026-09-15.md).

- Zusätzliche Push-Auslöser für `dev-*` und `codex/ios-port-*` entfernt. Entwicklungsänderungen werden über Pull Requests geprüft; `main` und manuelle Starts bleiben erhalten. Damit entfallen doppelte Push-/PR-Läufe.
- Android CI, iOS CI und der manuelle Play-Store-Bundle-Workflow bleiben als benötigte Prüf-/Releasewege erhalten. Einmalige Bereinigungsworkflows waren bereits entfernt.
- Neueste erfolgreiche Android-APK: [Build 217](https://github.com/Chris171203/Pontoniere-Rheinfelden-/actions/runs/34956753704), 0.15.2 / 70, Quellstand `75fb0f6a46bd2be971eb63284f5b6da2a30f2708`. App-Code und Version bleiben bei dieser Bereinigung unverändert.
- Prüfung der Bereinigung: YAML eingelesen; Trigger und sämtliche Jobdefinitionen gegen den vorherigen Stand verglichen. Keine Tests oder Buildschritte entfernt.

## Prüfstand

- Android: 135 Tests ohne Fehler/Skips, vier Browserfälle, Lint, Builds und Paket-/Versions-/Signaturprüfung bestanden. [Test-APK 0.15.2](https://github.com/Chris171203/Pontoniere-Rheinfelden-/actions/runs/34952967257/artifacts/10390386143).
- iOS: Wettertests auf beiden iPhone-Grössen und iPad bestanden; gemeinsame Ansichten auf allen drei Profilen angesehen. Compact: 107 bestanden; iPad: alle 17 UI-Fälle bestanden. Core, fünf separate Live-Fälle, Compact-Release-Build und Gerätearchiv bestanden.
- Der grosse iPhone-Gesamtlauf bleibt wegen eines Timeouts im bestehenden Warenkorb-Neustarttest nach langsamem Simulatorstart rot (106 bestanden, ein Timeout). Derselbe Fall besteht auf den anderen Profilen und im vorherigen Large-Lauf. Keine Änderung der Warenkorblogik in diesem Fix. Details und genaue Nachweise: [Prüfbericht 0.15.2](docs/checks-0.15.2.md).
- Historisch: [0.15.1](docs/checks-0.15.1.md), [0.15.0](docs/checks-0.15.0.md). Die in 0.15.1 getrennten Wetteranzeigen waren eine Fehlinterpretation und sind in 0.15.2 zusammengeführt.

## Historie und Grenzen

- Zur Bereinigung von 0.15.0 wurden 48 abgeschlossene Branches und drei einmalige Hilfsdateien entfernt; drei Archiv-Tags und historische Nachweise blieben erhalten. [Bereinigungsnachweis](docs/cleanup-0.15.0.md).
- Entwicklungsversion unter 1.0.0; keine Store-Veröffentlichung. iOS-Geräteinstallation/TestFlight benötigt Apple-Signierung und Provisionierung. Keine Prüfung auf physischen Geräten; persönliche produktive Intern-Aktionen und reale Zahlungen sind nicht durch die synthetischen Tests belegt.

## Referenzen

- [Projektumfang](PROJECT.md)
- [Bisherige Statusgeschichte](docs/status-history-before-0.15.0.md)
- [Vereinsinhalte und UX](decisions/native-club-content.md)
