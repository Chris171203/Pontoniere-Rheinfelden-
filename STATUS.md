# Status

Stand: Testversion `0.8.0` · aktualisiert 2026-09-01.

## Implementiert / im Test

- Android-App mit Home, Terminen, Verein, Kasse, Einstellungen und internem Webbereich.
- Kalendervorrang für das nächste Training:
  - passender Trainings- oder Fahrübungstermin aus dem Vereinskalender wird verwendet;
  - dessen tatsächliche Anfangs- und Endzeit steuert die Wetterauswertung;
  - abgesagte Termine werden erkannt;
  - saisonaler Mo/Mi- beziehungsweise Do-Plan bleibt als Fallback.
- Termine mit moderner Detailansicht, Beschreibung, Endzeit, Ort, Route, Teilen und Übergabe an die persönliche Kalender-App.
- Rhein-Bereich neu gestaltet:
  - kompakter Status-Pill statt grosser Farbfläche;
  - Grenzbereiche als übersichtliche Chips statt langer Textlegende;
  - Statistik für Mittelwert, Min/Max und Entwicklung je ausgewähltem Messwert;
  - auswählbare Bereiche `1h`, `24h` und `7d` sowie Messwerte Abfluss, Pegel und Temperatur;
  - `1h` aus BAFU-Livewerten, `24h` aus 10-Minuten-Mitteln und `7d` aus Stundenmitteln;
  - dynamische, lesbar gerundete Achsen und interaktive Einzelwertanzeige;
  - relevante Niedrig-, Warn- und Alarmgrenzen als farbige gestrichelte Linien auf der Abflussskala.
- Datenalter von Kalender, Wetter und Rhein in den Einstellungen sichtbar.
- Persistenter Offline-Cache für Kalender, Training-Wetter und BAFU-Messwerte.
- Dark Mode: System / Hell / Dunkel für die native App.
- Swiss QR mit Betrag und Banking-App-Auswahl.
- TWINT-Code-Zahlungsweg sowie offizieller Vereins-TWINT-QR.
- Persönlicher Intern-Link ausschliesslich lokal gespeichert.
- WebView-Hostprüfung auf exakte PFVR-Domain beziehungsweise echte Subdomains gehärtet; Drittanbieter-Cookies deaktiviert.
- Hydrologie-, Kalender-Matching- und Linkregeln durch lokale Unit-Tests abgedeckt.
- Android 16 / API 36 als Target.

## Noch zu verifizieren

- Visuelle Prüfung von 0.8.0 auf einem realen Android-Gerät, insbesondere:
  - Rheinstatus und Grenz-Chips in Hell/Dunkel;
  - Diagramme in allen drei Zeitbereichen;
  - enge und sehr breite Wertebereiche;
  - Termin-Details und Kalenderübergabe;
  - kalendergesteuerte Trainingsprognose bei realen Terminbezeichnungen und Absagen.
- Installation über einer älteren Test-APK. GitHub-Debug-Builds können unterschiedliche Testsignaturen besitzen; dann ist eine Neuinstallation erforderlich.

## Spätere Punkte

- Technische Prüfung einer stabilen nativen An-/Abmeldung anhand eines bereinigten Request-Schemas.
- Vereinsbeiz-Warenkorb nach Vorliegen der offiziellen Preisliste.
- Native Vereinsnews aus WordPress REST/RSS.
- Optionale Trainingsbenachrichtigungen und Homescreen-Widget.
- Stabile Test- und Release-Signierung.
- iOS-Implementierung.
