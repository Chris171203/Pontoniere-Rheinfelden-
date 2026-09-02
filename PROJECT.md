# Projekt

## Ziel

Mobile PFVR-App mit schnellem Zugriff auf Training, Rhein- und Wetterdaten, Vereinstermine, interne An-/Abmeldung und Vereinsbeiz-Zahlung.

## Kernfunktionen

- Home mit nächstem Training und Wetter für den tatsächlichen Zeitraum.
  - Bevorzugt wird ein passender Termin aus dem öffentlichen Vereinskalender.
  - Der saisonale Trainingsplan dient als Fallback, wenn kein Kalendereintrag vorhanden ist.
- Rhein: zwei Stationskacheln, davon die zweite optional. Jede aktive Kachel zeigt Abfluss und Pegel; Rheinfelden zusätzlich Wassertemperatur. Verläufe für `1h`, `24h` und `7d` werden untereinander dargestellt.
- Termine aus dem öffentlichen PFVR-Google-Kalender mit persistentem Cache, Detailansicht, Teilen, Route und Übergabe an die persönliche Kalender-App.
- Verein und Kontakt inklusive News-Archiv-Verlinkung.
- Interner Bereich über persönlichen `intern.pfvr.ch`-Link; Konfiguration nur in den Einstellungen und nur lokal gespeichert. App-Ansicht ist Standard und folgt dem nativen Hell-/Dunkelmodus.
- Vereinsbeiz: Warenkorb in den Gruppen Trinken, Essen und Feiern, freier Betrag, Swiss-QR-Zahlung, direkte Android-Übergabe an kompatible Banking-Apps und TWINT-Zahlungsweg.
- Darstellung: System / Hell / Dunkel.

## Datenquellen

- PFVR-Website / WordPress-REST/RSS für öffentliche Vereinsinhalte.
- Öffentlicher PFVR-Google-Kalender.
- Wetter: MeteoSwiss ICON via Open-Meteo.
- Hydrologie: BAFU Stationen 2091 Rhein–Rheinfelden und 2289 Basel–Rheinhalle.

## Qualitätsziele

- Letzten erfolgreichen Datenstand lokal anzeigen und Datenalter sichtbar machen.
- Live-Aktualisierungen dürfen die aktuelle Scrollposition nicht verändern.
- Keine persönlichen Zugangsparameter, Schlüssel oder Personen-IDs im Repository oder in Diagnosedaten.
- Test-APK reproduzierbar aus dem eingecheckten Quellstand bauen; keine verdeckten Build-Patches.
- Dauerhafte Android-Updates setzen eine unveränderte Paket-ID und dieselbe geschützte Signatur voraus.
- Android und spätere iOS-App sollen dieselben fachlichen Kernfunktionen bieten.

## Release

Entwicklung/Test: `0.x.y`. Erster offizieller Release: `1.0.0`.
