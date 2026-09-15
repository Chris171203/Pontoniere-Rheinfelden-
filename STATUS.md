# Status

Stand: 2026-09-15 · Android **0.15.1 / Versionscode 69** · iOS **0.15.1 / Build 3**.

## Korrektur 0.15.2 in Arbeit

Eine gemeinsame Wettervorhersage pro ausgewähltem Tag. Alle Termine bleiben sichtbar; gleiche oder überlappende Zeiträume werden zusammengeführt. Getrennte Zeiträume liefern Punkte innerhalb derselben Vorhersage, ohne Wetter aus der Lücke oder doppelt gezählte Regenmengen. Android und iOS werden gemeinsam korrigiert.

## Aktueller Stand

- PR #34 ist nach `main` übernommen. Die Wetterkachel zeigt alle noch laufenden oder kommenden Termine am nächsten relevanten Tag chronologisch und mit Wetter je Terminzeitraum.
- Mittag plus Abend erscheinen getrennt; bei einem einzelnen Abendtermin werden dort keine Mittagswerte angezeigt. Abgesagte und beendete Termine entfallen, explizite Trainings werden nicht verdoppelt. Laufende mehrtägige Anlässe verwenden den ausgewählten Tag in Europe/Zurich.
- Auch längere Termine verwenden ausschliesslich passende Stundenwerte. Deutsch/Schweizerdeutsch gepflegt; öffentliche Termintitel bleiben unverändert. Kein zusätzlicher Wetterabruf je Termin.
- Der freigegebene Vereinsstand aus 0.15.0 bleibt enthalten: direkte Trainingsübersicht, native Themenartikel, Kontakt-/Social-Symbole, früher Refresh und unmittelbare An-/Abmeldefarben.

## Abnahme

- Android: 133 Tests ohne Fehler/Skips, vier Browser-Szenarien, Lint, Builds und Paket-/Versions-/Signaturprüfung bestanden. [Test-APK 0.15.1](https://github.com/Chris171203/Pontoniere-Rheinfelden-/actions/runs/34943818340/artifacts/10386039139).
- iOS: je iPhone 105 Tests, iPad alle 16 UI-Fälle, fünf separate Live-Quellenfälle, Compact-Release-Build und unsigniertes arm64-Gerätearchiv bestanden. Die geprüften iOS-Dateien sind identisch zum integrierten Stand. Wetter-Screenshots beider Varianten auf allen drei Simulatorprofilen angesehen.
- Details, Commitbezug und Build-Korrekturen: [Abnahme 0.15.1](docs/checks-0.15.1.md). Die bisherigen [0.15.0-Nachweise](docs/checks-0.15.0.md) bleiben historisch erhalten.

## Historie und Grenzen

- Zur Bereinigung von 0.15.0 wurden 48 abgeschlossene Branches und drei einmalige Hilfsdateien entfernt; drei Archiv-Tags und historische Nachweise blieben erhalten. [Bereinigungsnachweis](docs/cleanup-0.15.0.md).
- Entwicklungsversion unter 1.0.0; keine Store-Veröffentlichung. iOS-Geräteinstallation/TestFlight benötigt Apple-Signierung und Provisionierung. Keine Prüfung auf physischen Geräten; persönliche produktive Intern-Aktionen und reale Zahlungen sind nicht durch die synthetischen Tests belegt.

## Referenzen

- [Projektumfang](PROJECT.md)
- [Bisherige Statusgeschichte](docs/status-history-before-0.15.0.md)
- [Vereinsinhalte und UX](decisions/native-club-content.md)
