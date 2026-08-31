# Status

Stand: Testversion `0.5.0`.

## Implementiert / im Test

- Android-App mit Home, Terminen, Verein, Kasse, Einstellungen und internem Webbereich.
- Safe-area/Statusleisten-/Navigationsleisten-Korrekturen.
- Kalender-Cache.
- Wetter für das nächste reguläre Training.
  - April–September: Montag/Mittwoch, 18:30–20:00 Uhr.
  - Oktober–März: Donnerstag, 19:30–21:00 Uhr.
  - Einzelne Trainingsausfälle/Abweichungen werden von dieser festen Saisonregel noch nicht automatisch erkannt.
- BAFU Rhein-Daten inkl. Pegel-/Wassertemperatur-Verläufen.
  - Verläufe auf letzte 24 h begrenzt.
  - X-Achse mit Uhrzeit/24-h-Hinweis, Y-Achse mit Einheit und Wertebereich.
- Dark Mode: System / Hell / Dunkel für die native App.
  - Eingebettete öffentliche PFVR-Webseiten werden unabhängig davon kontrolliert hell dargestellt, weil das erzwungene Dark-Styling WordPress-/PDF-Inhalte unlesbar machen konnte.
- Persönlicher Intern-Link ausschließlich unter Einstellungen.
- Swiss QR mit Betrag; Banking-App vor TWINT.
- Banking-App-Auswahl mit Schweizer Priorisierung; Buttontext wird nach Auswahl sofort aktualisiert.
- Offizieller Vereinsbeiz-TWINT-QR von `pfvr.ch` ist aus der Kasse direkt als PDF erreichbar und kann über Browser/PDF-Viewer gespeichert werden.
- Redundanter Vereinsbeiz-Schnellzugriff auf Home entfernt; Kasse bleibt Hauptnavigation.
- News-Archiv unter Verein.
- Android 16 / API 36 als Target für Google Play vorbereitet.

## Verifiziert

- Öffentlicher PFVR-Kalender erreichbar.
- Wetterquelle erreichbar.
- BAFU Station 2091 liefert Q/W/WT.
- PFVR RSS und WordPress REST API sind verfügbar; damit kann ein nativer Bereich für aktuelle Beiträge später umgesetzt werden.
- PFVR WordPress-Medien enthalten einen expliziten Vereinsbeiz-TWINT-QR unter `vereinsbeiz_zahlung/Twint_QR.pdf`.
- Android 0.5.0 kompiliert mit API 36 als APK und AAB; APK-Signatur und Paket-/Versionsdaten wurden im CI geprüft.

## Google Play

Technisch machbar. Vorbereitet sind:

- API 36 / Android 16.
- App-Bundle-Build.
- Workflow für ein signiertes Release-AAB mit GitHub-Secrets.
- Store-Listing-Entwurf und Datenschutzentwurf unter `PlayStore/`.

Vor Veröffentlichung fehlen noch Entwicklerkonto/Verifizierung, Upload-Key, finale öffentlich erreichbare Datenschutzerklärung mit Link in der App, Data-Safety-/Content-Angaben, finale Store-Grafiken und ggf. die vorgeschriebene Testphase des gewählten Kontotyps.

## Interne An-/Abmeldung

- Aktuell nutzt die App bewusst die bestehende PFVR-Webseite in einer WebView.
- Es wurde keine öffentlich dokumentierte API für `kommen / nicht kommen / mit Essen` gefunden.
- Eine native Oberfläche ist wahrscheinlich möglich, wenn die bestehende Seite stabile HTML-Formulare bzw. HTTP-Requests verwendet; das muss anhand eines bereinigten Formular-/Request-Schemas geprüft werden.
- Persönliche URL-Keys oder Personen-IDs dürfen dafür nicht in Repository, Logs oder Diagnosedateien landen.

## Nächste Punkte

- 0.5.0 auf realem Android-Gerät testen, insbesondere Achsen, Trainingswetter, öffentliche Webseiten im Dark Mode, Banking-App-Button und TWINT-PDF.
- Internes An-/Abmeldeformular technisch untersuchen und entscheiden: native HTTP-Anbindung oder WebView/DOM-Integration.
- Preisliste fotografieren und Vereinsbeiz-Warenkorb ergänzen.
- Nativen Bereich `Aktuelles` aus WordPress REST/RSS entwickeln.
- Google-Play-Herausgeber festlegen und Release-Voraussetzungen abschließen.
- iOS-Implementierung später in `iOS/` starten.
