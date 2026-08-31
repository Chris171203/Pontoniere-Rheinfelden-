# Projekt

## Ziel

Mobile PFVR-App mit schnellem Zugriff auf Vereinsinformationen, Termine, Training/Wetter, Rhein-/Pegeldaten, internen Bereich und Vereinsbeiz-Zahlung.

## Kernfunktionen

- Home mit nächstem Training und Wetter (Mo/Mi, 18–20 Uhr).
- Rhein Rheinfelden: BAFU-Abfluss, Pegel und Wassertemperatur inkl. Verlauf und Messzeit.
- Termine aus dem öffentlichen PFVR-Google-Kalender mit persistentem Cache.
- Verein/Kontakt inkl. News-Archiv-Verlinkung.
- Interner Bereich über persönlichen `intern.pfvr.ch`-Link; Konfiguration nur in Einstellungen und nur lokal gespeichert.
- Vereinsbeiz: freier Betrag, Swiss-QR-Zahlung, Banking-App als Priorität vor TWINT; später Warenkorb aus Preisliste.
- Theme: System / Hell / Dunkel.

## Datenquellen

- PFVR-Website / WordPress-REST/RSS für öffentliche Vereinsinhalte.
- Öffentlicher PFVR-Google-Kalender.
- Wetter: MeteoSwiss ICON via Open-Meteo.
- Hydrologie: BAFU Station 2091 Rhein–Rheinfelden.

## Release

Entwicklung/Test: `0.x.y`. Erster offizieller Release: `1.0.0`.
