# Projekt

## Ziel

Mobile PFVR-App mit schnellem Zugriff auf Training, Rhein- und Wetterdaten, Vereinstermine, interne An-/Abmeldung und Vereinsbeiz-Zahlung.

## Kernfunktionen

- Personalisierbare Kachelbereiche auf Home, in der Kasse und im Verein. Reihenfolge und Sichtbarkeit werden lokal gespeichert; zentrale Funktionen können als fixierte Kacheln vor Ausblenden geschützt werden.
- Home mit nächstem Training und Wetter für den tatsächlichen Zeitraum.
  - Bevorzugt wird ein passender Termin aus dem öffentlichen Vereinskalender.
  - Der saisonale Trainingsplan dient als Fallback, wenn kein Kalendereintrag vorhanden ist.
- Rhein: zwei Stationskacheln, davon die zweite optional. Aktuelle Abflüsse stehen kompakt nebeneinander; Rheinfelden zeigt oben zusätzlich die Wassertemperatur. Pro aktiver Station kombiniert ein `1h`/`24h`/`7d`-Diagramm Abfluss und Pegel mit zwei dynamischen Y-Achsen; vorhandene Temperaturdaten werden darunter separat dargestellt.
- Termine aus dem öffentlichen PFVR-Google-Kalender mit persistentem Cache, Detailansicht, Teilen, Route und Übergabe an die persönliche Kalender-App.
- Verein und Kontakt mit nativen News, Jahresprogramm, Vorstand, Geschichte und direkten Kontaktaktionen.
- Interner Bereich über persönlichen `intern.pfvr.ch`-Link; Konfiguration nur in den Einstellungen und nur lokal gespeichert. App-Ansicht ist Standard, folgt dem nativen Hell-/Dunkelmodus und darf fehlerhafte Inline-Darstellung der externen Seite rein visuell korrigieren. Der Originalmodus bleibt verfügbar.
- Vereinsbeiz: fixierter Warenkorb, anordenbare Kategorien Trinken/Essen/Feiern, freier Betrag, Swiss-QR-Zahlung, direkte Android-Übergabe an eine unter Einstellungen → Zahlung gewählte Banking-App und TWINT-Zahlungsweg.
- Einstellungen: Bereiche Allgemein, Rhein und Zahlung sowie ein eigenes Menü `Ansicht & Kacheln` für Home, Kasse und Verein.

## Datenquellen

- PFVR-Website / WordPress-REST/RSS für öffentliche Vereinsinhalte.
- Öffentlicher PFVR-Google-Kalender.
- Wetter: MeteoSwiss ICON via Open-Meteo.
- Hydrologie: BAFU Stationen 2091 Rhein–Rheinfelden und 2289 Basel–Rheinhalle.

## Qualitätsziele

- Letzten erfolgreichen Datenstand lokal anzeigen und Datenalter sichtbar machen.
- Live-Aktualisierungen dürfen die aktuelle Scrollposition nicht verändern.
- Benutzerdefinierte Kachellayouts müssen App-Updates mit neuen oder entfernten Tile-IDs robust überstehen.
- Keine persönlichen Zugangsparameter, Schlüssel oder Personen-IDs im Repository oder in Diagnosedaten.
- Test-APK reproduzierbar aus dem eingecheckten Quellstand bauen; keine verdeckten Build-Patches.
- Dauerhafte Android-Updates setzen eine unveränderte Paket-ID und dieselbe geschützte Signatur voraus.
- Android und spätere iOS-App sollen dieselben fachlichen Kernfunktionen bieten.

## Release

Entwicklung/Test: `0.x.y`. Erster offizieller Release: `1.0.0`.
