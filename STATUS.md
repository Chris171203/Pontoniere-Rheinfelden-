# Status

Stand: Testversion `0.10.0` · aktualisiert 2026-09-03.

## Implementiert / im Test

- Home, Kasse und Verein verwenden eine gemeinsame konfigurierbare Kachelarchitektur.
- Unter Einstellungen → Ansicht & Kacheln können die Kacheln je Bereich verschoben, ein- und ausgeblendet sowie auf den Standard zurückgesetzt werden.
- Reihenfolge und Sichtbarkeit werden nur lokal auf dem Gerät gespeichert. Unbekannte alte Tile-IDs werden entfernt; neu hinzugekommene Kacheln werden automatisch ergänzt.
- Der Warenkorb ist in der Kasse als zentrale Kachel immer sichtbar und an erster Stelle fixiert. Kategorien, freier Betrag, TWINT und Zahlungsdaten bleiben frei anordenbar und ausblendbar.
- Verein ist auf eine breite Über-uns-Kachel und kompakte Aktionskacheln für News, Jahresprogramm, Vorstand, Geschichte, Depot/Route, Telefon, E-Mail und Kontakt umgestellt.
- Home enthält separat anordenbare Kacheln für Trainingswetter, Rhein-Kurzwerte, Rhein-Grafiken, Termine und Vereinsnews. Hero und zentrale Schnellaktionen bleiben fest.
- Der interne App-Modus trennt vorangestellte Anmeldestatus wie `Ohne Essen` robust vom folgenden Termintext. Das konkrete Problem `Ohne EssenSchiffe ...` wird zu Statuschip plus eigener Detailzeile aufgeteilt. Die Originalansicht bleibt unverändert.
- Native Vereinsnews werden über die öffentliche WordPress-REST-API geladen, lokal gecacht und auf Home sowie in einer nativen News-Liste angezeigt.
- Banking-Handoff bleibt Share-first: jede gewählte Banking-App erhält zuerst dieselben QR-Bildversuche; die Registry steuert nur den Fallback.
- Gespeicherte Zahlungs-QRs verwenden wiederverwendbare Dateinamen wie `PFVR_12.50CHF.png`.
- Die Rhein-Kurzkarten zeigen den BAFU-Messdatenstand und kennzeichnen alte Cache-Daten. Pro aktiver Station gibt es ein gemeinsames Abfluss-/Pegel-Diagramm; Wassertemperatur bleibt separat.
- Live-Aktualisierung stellt die Scrollposition nach dem Neuaufbau der Home-Kacheln wieder her.
- `ch.pfvr.app.test` wird mit dem reproduzierbaren festen Testschlüssel signiert.
- Android 16 / API 36 als Target.

## Verifiziert

- Unit-Tests für Tile-Reihenfolge, unbekannte/neue IDs, fixierten Warenkorb, Sichtbarkeitssanitisierung und interne Statusaufteilung.
- Android-Kompilierung, Unit-Tests, APK-Build, Paket/Version und fester Test-Zertifikatsfingerprint in CI.

## Noch auf realen Geräten zu prüfen

- Kachelmenü auf kleiner Displaybreite, Hell-/Dunkelmodus und längere deutsche Bezeichnungen.
- Reihenfolge und Sichtbarkeit nach App-Neustart und nach einem weiteren Update.
- Interne Terminaufbereitung an mehreren Termintypen; DOM-Struktur der extern betriebenen Seite kann sich ändern.
- Warenkorbzustand bei ausgeblendeten Kategorien und frei angeordneter Kasse.
- Direkte Zahlungsübernahme weiterer Banking-Apps; Yuh ist bestätigt, Neon und Revolut übernahmen das Bild im bisherigen Test nicht.

## Nächste strukturelle Schritte

- Weitere Aufteilung der noch großen `MainActivity` in Screen-/Repository-Komponenten.
- Optional kompakte/breite Größenwahl nur für Kacheln, deren Inhalt beide Darstellungen sinnvoll unterstützt.
- Optionale Trainingsbenachrichtigungen und Homescreen-Widget.
- iOS-Implementierung.
