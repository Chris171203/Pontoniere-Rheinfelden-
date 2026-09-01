# Projekt

## Ziel

Mobile PFVR-App mit schnellem Zugriff auf Training, Rhein- und Wetterdaten, Vereinstermine, interne An-/Abmeldung und Vereinsbeiz-Zahlung.

## Kernfunktionen

- Home mit nächstem Training und Wetter für den tatsächlichen Zeitraum.
  - Bevorzugt wird ein passender Termin aus dem öffentlichen Vereinskalender.
  - Der saisonale Trainingsplan dient als Fallback, wenn kein Kalendereintrag vorhanden ist.
- Rhein Rheinfelden: BAFU-Abfluss, Pegel und Wassertemperatur mit Messzeit, Statistik und wählbarem Verlauf für `1h`, `24h` und `7d`.
- Termine aus dem öffentlichen PFVR-Google-Kalender mit persistentem Cache, Detailansicht, Teilen, Route und Übergabe an die persönliche Kalender-App.
- Verein und Kontakt inklusive News-Archiv-Verlinkung.
- Interner Bereich über persönlichen `intern.pfvr.ch`-Link; Konfiguration nur in den Einstellungen und nur lokal gespeichert.
- Vereinsbeiz: freier Betrag, Swiss-QR-Zahlung und TWINT-Zahlungsweg; später Warenkorb aus der offiziellen Preisliste.
- Darstellung: System / Hell / Dunkel.

## Datenquellen

- PFVR-Website / WordPress-REST/RSS für öffentliche Vereinsinhalte.
- Öffentlicher PFVR-Google-Kalender.
- Wetter: MeteoSwiss ICON via Open-Meteo.
- Hydrologie: BAFU Station 2091 Rhein–Rheinfelden.

## Qualitätsziele

- Letzten erfolgreichen Datenstand lokal anzeigen und Datenalter sichtbar machen.
- Keine persönlichen Zugangsparameter, Schlüssel oder Personen-IDs im Repository oder in Diagnosedaten.
- Test-APK reproduzierbar aus dem eingecheckten Quellstand bauen; keine verdeckten Build-Patches.
- Android und spätere iOS-App sollen dieselben fachlichen Kernfunktionen bieten.

## Release

Entwicklung/Test: `0.x.y`. Erster offizieller Release: `1.0.0`.
