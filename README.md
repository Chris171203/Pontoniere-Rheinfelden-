# Pontoniere Rheinfelden App

Mobile Vereins-App für den Pontonierfahrverein Rheinfelden mit Terminen, Trainingswetter, Rhein-Livedaten, interner An-/Abmeldung, Vereinsnews und Vereinsbeiz-Zahlung.

## Aktueller Entwicklungsstand

Android-Testversion `0.10.10` auf `main`.

Schwerpunkte des aktuellen Stands:

- nächstes Training aus dem öffentlichen Vereinskalender mit saisonalem Fallback;
- Wetter passend zum tatsächlichen Trainingszeitraum;
- BAFU-Rheindaten mit Abfluss, Pegel, Wassertemperatur und interaktiven Verläufen;
- native Termine und Vereinsnews mit lokalem Cache und sichtbarem Datenstand;
- interne An-/Abmeldung als mobile Matrix mit mehreren Personen, fixierter Kopfzeile und größeren Bedienflächen;
- lokale Personenverwaltung für die interne Ansicht, während die bestehende PFVR-Seite Quelle der Wahrheit bleibt;
- personalisierbare Kacheln für Home, Kasse und Verein;
- Vereinsbeiz mit Warenkorb, freiem Betrag, Swiss QR, Banking-App-Handoff und TWINT;
- Hell-/Dunkelmodus sowie für Android 16 / API 36 vorbereiteter Build;
- CI-geprüftes Debug-Testpaket mit stabilem öffentlichen Testschlüssel; Produktionssignierung bleibt getrennt.

## Struktur

- `Android/` – aktuelle native Android-App und Build-Konfiguration.
- `iOS/` – vorbereitet für eine spätere native iOS-App.
- `PlayStore/` – Unterlagen für eine spätere Veröffentlichung.
- `decisions/` – relevante Architekturentscheidungen.

## Datenschutz

Der persönliche `intern.pfvr.ch`-Link wird ausschließlich lokal auf dem Endgerät gespeichert und gehört nicht ins Repository. Öffentliche Vereins-, Kalender-, Wetter- und BAFU-Daten werden lokal gecacht.

## Versionierung

Während der Testphase werden Versionen unter `1.0.0` verwendet. `1.0.0` ist für den ersten offiziellen Release reserviert.
