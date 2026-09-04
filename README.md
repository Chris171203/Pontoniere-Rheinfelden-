# Pontoniere Rheinfelden App

Mobile Vereins-App für den Pontonierfahrverein Rheinfelden mit Terminen, Trainingswetter, Rhein-Livedaten, interner An-/Abmeldung, Vereinsnews und Vereinsbeiz-Zahlung.

## Aktueller Entwicklungsstand

Aktueller Android-Teststand: `0.11.4` (`versionCode 51`).

Schwerpunkte des aktuellen Stands:

- nächstes Training aus dem öffentlichen Vereinskalender mit saisonalem Fallback;
- Wetter passend zum tatsächlichen Trainingszeitraum;
- BAFU-Rheindaten mit Abfluss, Wasserstand, Wassertemperatur und interaktiven Verläufen; beide Stationen zeigen den Wasserstand in m ü.M., eine zusätzliche cm-Darstellung wird nur bei Messpunkten mit verifiziertem Bezug angeboten;
- native Termine und Vereinsnews mit lokalem Cache und sichtbarem Datenstand;
- interne An-/Abmeldung als mobile Matrix mit mehreren Personen, dauerhaft sichtbarer synchronisierter Kopfzeile und größeren Bedienflächen;
- vollständige Übernahme großer Original-Personenlisten inklusive Namen aus Bearbeiten-/Namens-Controls und zeilengerecht zugeordneten Statuswerten;
- lange Koch-/Verantwortlichkeitsnamen werden innerhalb der Terminspalte automatisch passend verkleinert;
- robuste Personenverwaltung mit blockierten Website-Bulk-Aktionen und Recovery aus dem lokal gespeicherten persönlichen Initiallink;
- lokale Personenverwaltung für die interne Ansicht, während die bestehende PFVR-Seite Quelle der Wahrheit bleibt;
- personalisierbare Kacheln für Home, Kasse und Verein; die Kachelverwaltung behält beim Umordnen ihre Scrollposition;
- Vereinsbeiz mit Warenkorb, freiem Betrag, Swiss QR, Banking-App-Handoff und TWINT;
- Deutsch/Schwiizerdütsch-Umschaltung mit durchgängig nachgezogenen App-eigenen UI-Texten bis in die app-erzeugte interne Personenverwaltung; externe Vereinsinhalte und Namen bleiben unverändert;
- öffentliche Erstfreigabe-/Landingpage mit Hinweis auf Schnuppertraining vor der Mitgliedschaft sowie verifizierten PFVR-Links zu Einstieg/Formularen, Instagram und Facebook, ohne vor der Freigabe interne oder Live-Bereiche zu initialisieren;
- Hell-/Dunkelmodus sowie Android 16 / API 36;
- Play-Readiness-Prüfung mit `lintRelease`, Release-AAB-Kompilierung, finalem Permission-Audit und getrenntem, geschütztem Upload-Key-Workflow;
- lokale PFVR-Zugänge sind ausdrücklich von Android-Backup und Device-to-Device-Transfer ausgeschlossen.

## Struktur

- `Android/` – aktuelle native Android-App und Build-Konfiguration.
- `iOS/` – vorbereitet für eine spätere native iOS-App.
- `PlayStore/` – aktuelle Store-, Datenschutz-, Data-Safety-, Review-, Asset- und Release-Unterlagen.
- `decisions/` – relevante Architektur- und Sicherheitsentscheidungen.

## Datenschutz

Der persönliche `intern.pfvr.ch`-Link wird ausschließlich lokal auf dem Endgerät gespeichert und gehört nicht ins Repository. Öffentliche Vereins-, Kalender-, Wetter- und BAFU-Daten werden lokal gecacht. Persönliche App-Daten werden nicht über Android-Backup oder Device-to-Device-Transfer migriert.

## Versionierung

Während der Testphase werden Versionen unter `1.0.0` verwendet. `1.0.0` ist für den ersten offiziellen Produktionsrelease reserviert.
