# Projekt

## Ziel

Mobile PFVR-App mit schnellem Zugriff auf Vereinsinformationen, Termine, Training/Wetter, Rhein-/Pegeldaten, internen Bereich und Vereinsbeiz-Zahlung.

## Kernfunktionen

- Home mit Wetter zum nächsten tatsächlich im öffentlichen Kalender eingetragenen Training; Datum und Uhrzeit werden nicht aus festen Wochentagen abgeleitet.
- Rhein Rheinfelden: BAFU-Abfluss, Pegel und Wassertemperatur inkl. Verlauf, Achsenbeschriftung und Messzeit.
- Termine aus dem öffentlichen PFVR-Google-Kalender mit persistentem Cache.
- Verein/Kontakt inkl. News-Archiv-Verlinkung.
- Interner Bereich über persönlichen `intern.pfvr.ch`-Link; Konfiguration nur in Einstellungen und nur lokal gespeichert.
- Vereinsbeiz: freier Betrag, Swiss-QR-Zahlung, Banking-App als Priorität vor TWINT; später Warenkorb aus Preisliste.
- Theme: System / Hell / Dunkel. Eingebettete öffentliche Webseiten und der interne Bereich werden unabhängig davon in einem lesbaren hellen Webmodus dargestellt.

## Datenquellen

- PFVR-Website / WordPress-REST/RSS für öffentliche Vereinsinhalte.
- Öffentlicher PFVR-Google-Kalender.
- Wetter: MeteoSwiss ICON via Open-Meteo.
- Hydrologie: BAFU Station 2091 Rhein–Rheinfelden.

## Interner Bereich

Eine dokumentierte, stabile API für An-/Abmeldung wurde bisher nicht nachgewiesen. Bis eine solche Schnittstelle vom Betreiber bestätigt oder der konkrete Request-Vertrag belastbar analysiert ist, bleibt die echte Seite im WebView maßgeblich. Das Styling darf verbessert werden; Form-Requests dürfen nicht durch ungesicherte DOM-Annahmen ersetzt werden.

## Release

Entwicklung/Test: `0.x.y`. Erster offizieller Release: `1.0.0`. Android wird für Ziel-API 36 und Google-Play-Ausgabe als signiertes App Bundle vorbereitet.
