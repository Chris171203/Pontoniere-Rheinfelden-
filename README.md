# Pontoniere Rheinfelden App

Mobile App für den Pontonierfahrverein Rheinfelden.

## Aktueller Entwicklungsstand

Android-Testversion `0.8.0` auf Branch `dev-0.8.0`.

Schwerpunkte der Version:

- nächstes Training aus dem Vereinskalender mit saisonalem Fallback;
- Wetter passend zur tatsächlichen Trainingszeit;
- moderne Termin-Detailansicht und Kalenderaktionen;
- neu gestalteter Rhein-Bereich mit auswählbaren Messwerten und `1h`, `24h` oder `7d`;
- interaktive Diagramme, gerundete Achsen und kompakte Grenzbereiche;
- lokale Caches und sichtbares Datenalter;
- Swiss QR und TWINT-Zahlungswege.

## Struktur

- `Android/` – native Android-App.
- `iOS/` – vorbereitet für eine spätere native iOS-App.

## Datenschutz

Der persönliche `intern.pfvr.ch`-Link wird ausschliesslich lokal auf dem Endgerät gespeichert und gehört nicht ins Repository. Öffentliche Vereins-, Kalender-, Wetter- und BAFU-Daten werden lokal gecacht.

## Versionierung

Während der Testphase werden Versionen unter `1.0.0` verwendet. `1.0.0` ist für den ersten offiziellen Release reserviert.
