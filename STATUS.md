# Status

Stand: Testversion `0.10.1` · aktualisiert 2026-09-03.

## Implementiert / im Test

- Die interne An-/Abmeldeansicht verwendet für Terminspalten eine feste lesbare Mindestbreite und legt breite Tabellen in einen horizontal scrollbaren Bereich. Wörter werden nicht mehr durch `overflow-wrap:anywhere` buchstabenweise zerlegt.
- Status wie `Mit Essen`, `Ohne Essen`, `Nicht gewählt` und `Komme nicht` bleiben als eigener farbiger Block vom folgenden Termintext getrennt. Die Originalansicht bleibt unverändert.
- Home, Kasse und Verein verwenden eine gemeinsame konfigurierbare Kachelarchitektur.
- Unter Einstellungen → Ansicht & Kacheln können die Kacheln je Bereich verschoben, ein- und ausgeblendet sowie auf den Standard zurückgesetzt werden.
- Reihenfolge und Sichtbarkeit werden nur lokal auf dem Gerät gespeichert. Unbekannte alte Tile-IDs werden entfernt; neu hinzugekommene Kacheln werden automatisch ergänzt.
- Der Warenkorb ist in der Kasse als zentrale Kachel immer sichtbar und an erster Stelle fixiert. Kategorien, freier Betrag, TWINT und Zahlungsdaten bleiben frei anordenbar und ausblendbar.
- Verein ist auf eine breite Über-uns-Kachel und kompakte Aktionskacheln für News, Jahresprogramm, Vorstand, Geschichte, Depot/Route, Telefon, E-Mail und Kontakt umgestellt.
- Home enthält separat anordenbare Kacheln für Trainingswetter, Rhein-Kurzwerte, Rhein-Grafiken, Termine und Vereinsnews. Hero und zentrale Schnellaktionen bleiben fest.
- Native Vereinsnews werden über die öffentliche WordPress-REST-API geladen, lokal gecacht und auf Home sowie in einer nativen News-Liste angezeigt.
- Banking-Handoff bleibt Share-first: jede gewählte Banking-App erhält zuerst dieselben QR-Bildversuche; die Registry steuert nur den Fallback.
- Gespeicherte Zahlungs-QRs verwenden wiederverwendbare Dateinamen wie `PFVR_12.50CHF.png`.
- Die Rhein-Kurzkarten zeigen den BAFU-Messdatenstand und kennzeichnen alte Cache-Daten. Pro aktiver Station gibt es ein gemeinsames Abfluss-/Pegel-Diagramm; Wassertemperatur bleibt separat.
- Live-Aktualisierung stellt die Scrollposition nach dem Neuaufbau der Home-Kacheln wieder her.
- `ch.pfvr.app.test` wird mit dem reproduzierbaren festen Testschlüssel signiert.
- Android 16 / API 36 als Target.

## Verifiziert

- Unit-Tests für Statusaufteilung, horizontalen Tabellen-Wrapper, feste Terminspaltenbreite und Vermeidung von buchstabenweisem Umbruch.
- Unit-Tests für Tile-Reihenfolge, unbekannte/neue IDs, fixierten Warenkorb und Sichtbarkeitssanitisierung.
- Android-Kompilierung, Unit-Tests, APK-Build, Paket/Version und fester Test-Zertifikatsfingerprint in CI.
- Finale Testidentität: `0.10.1`, `versionCode 25`, Paket `ch.pfvr.app.test`.
- APK/AAB-Dateien werden nicht im Git-Repository versioniert, sondern ausschließlich als CI-Artefakte erzeugt.

## Noch auf realen Geräten zu prüfen

- Interne Terminansicht: ungefähr zwei lesbare Terminspalten gleichzeitig; weitere Termine müssen horizontal scrollbar sein.
- Status-/Termintrennung auf mehreren Termintypen; DOM-Struktur der extern betriebenen Seite kann sich ändern.
- Kachelmenü auf kleiner Displaybreite, Hell-/Dunkelmodus und längere deutsche Bezeichnungen.
- Reihenfolge und Sichtbarkeit nach App-Neustart und nach einem weiteren Update.
- Warenkorbzustand bei ausgeblendeten Kategorien und frei angeordneter Kasse.
- Direkte Zahlungsübernahme weiterer Banking-Apps; Yuh ist bestätigt, Neon und Revolut übernahmen das Bild im bisherigen Test nicht.

## Nächste strukturelle Schritte

- Weitere Aufteilung der noch großen `MainActivity` in Screen-/Repository-Komponenten.
- Optional kompakte/breite Größenwahl nur für Kacheln, deren Inhalt beide Darstellungen sinnvoll unterstützt.
- Remote-Katalog mit Server-JSON, lokalem Cache und eingebautem Fallback vorbereiten, sobald ein pflegbarer Website-Endpunkt verfügbar ist.
- Optionaler Upload-Portal-Zugang für Vereinsbilder und Videos.
- Optionale Trainingsbenachrichtigungen und Homescreen-Widget.
- iOS-Implementierung.
