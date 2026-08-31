# Status

Stand: Testversion `0.5.0`.

## Implementiert / im Test

- Android-App mit Home, Terminen, Verein, Kasse, Einstellungen und internem Webbereich.
- Safe-area/Statusleisten-/Navigationsleisten-Korrekturen.
- Kalender-Cache.
- Trainingserkennung anhand realer Kalendereinträge statt fixer Montag-/Mittwoch-Annahme.
- Wetterzeit aus Beginn und Ende des Kalendereintrags; damit insbesondere 18:30–20:00 Uhr bei entsprechendem Sommertraining.
- BAFU Rhein-Daten inkl. Pegel-/Wassertemperatur-Verläufen mit X-/Y-Achsenbeschriftung.
- Dark Mode: System / Hell / Dunkel; eingebettete Websites werden zur Vermeidung unlesbarer Farbkombinationen in hellem Modus dargestellt.
- Persönlicher Intern-Link ausschließlich unter Einstellungen.
- Swiss QR mit Betrag; Banking-App vor TWINT.
- Banking-App-Auswahl mit Schweizer Priorisierung und sofortiger Aktualisierung der Schaltflächenbeschriftung.
- News-Archiv unter Verein.
- Schnellzugriff auf Vereinsbeiz entfernt, weil `Kasse` bereits Hauptnavigation ist.
- Datenschutzerklärung und Google-Play-Unterlagen vorbereitet.

## Verifiziert

- Öffentlicher PFVR-Kalender erreichbar.
- Wetterquelle erreichbar.
- BAFU Station 2091 liefert Q/W/WT.
- PFVR RSS und WordPress REST API sind verfügbar; damit kann ein nativer Bereich für aktuelle Beiträge später umgesetzt werden.
- Android CI baut und prüft die App.

## Offen / externe Voraussetzung

- Ein echter Vereins-TWINT-QR kann erst eingebettet und zum Download angeboten werden, wenn die originale QR-Grafik oder ein offizieller TWINT-Zahlungslink vorliegt. Der vorhandene Swiss QR ist kein TWINT QR.
- Für eine native An-/Abmeldung fehlt eine dokumentierte API. Aktuell wird die Originalseite mit angepasstem Styling verwendet.
- Für Google Play fehlen noch das bestätigte Entwicklerkonto, Vereinsfreigaben, ein dauerhaft archivierter Upload-Schlüssel, eine veröffentlichte Datenschutz-URL und finale hochauflösende Store-Grafiken.
- Das vorhandene 96×96-Vereinslogo ist für den finalen Store-Eintrag zu klein.

## Nächste Punkte

- 0.5.0 auf realem Android-Gerät testen.
- Originalen TWINT-Vereins-QR oder offiziellen Zahlungslink beschaffen.
- Preisliste fotografieren und Vereinsbeiz-Warenkorb ergänzen.
- Native Schnittstelle zum internen System nur nach belastbarer Request-/API-Analyse entwickeln.
- Play-Store-Konto und Release-Signierung einrichten.
- Nativen Bereich `Aktuelles` aus WordPress REST/RSS entwickeln.
- iOS-Implementierung später in `iOS/` starten.
