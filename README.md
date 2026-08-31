# Pontoniere Rheinfelden App

Mobile App für den Pontonierfahrverein Rheinfelden.

## Struktur

- `Android/` – Android-App. Aktueller Entwicklungsstand: Testversion `0.5.0`.
- `iOS/` – vorbereitet für die spätere iOS-App.
- `Downloads/current/` – aktuell installierbare Test-APK.
- `Downloads/archive/` – ältere Test-APKs.
- `playstore/` – Vorbereitung für Google Play.
- `docs/` – öffentliche Datenschutzerklärung für GitHub Pages oder Übernahme auf `pfvr.ch`.

## Versionierung

Während der Testphase werden Versionen unter `1.0.0` verwendet. `1.0.0` ist für den ersten offiziellen Release reserviert.

## Datenquellen

Die App nutzt öffentliche Vereins- und Umweltdaten, unter anderem den öffentlichen PFVR-Kalender, PFVR-Webinhalte, Wetterdaten und BAFU-Hydrodaten. Persönliche Intern-Links werden lokal auf dem Endgerät gespeichert und gehören nicht ins Repository.

## Android-Build

Pull Requests und Änderungen an `Android/` werden durch GitHub Actions als Debug-APK gebaut und geprüft. Der manuelle Release-Workflow erzeugt nach Hinterlegung eines dauerhaften Upload-Schlüssels ein signiertes Android App Bundle für Google Play.
