# Projekt

## Ziel

Mobile PFVR-App mit schnellem Zugriff auf Training, Rhein- und Wetterdaten, Vereinstermine, interne An-/Abmeldung und Vereinsbeiz-Zahlung.

## Kernfunktionen

- Personalisierbare Kachelbereiche auf Home, in der Kasse und im Verein. Reihenfolge und Sichtbarkeit werden lokal gespeichert; zentrale Funktionen können als fixierte Kacheln vor Ausblenden geschützt werden. Die Vereinsübersicht zeigt Einstieg und Trainingszeiten fest; ihre weiteren Ziele sind kompakte, anordenbare Navigationszeilen.
- Home mit allen laufenden oder kommenden Anlässen am nächsten relevanten Vereinstag und einer gemeinsamen Wettervorhersage für ihre Zeiträume. Ein dezenter Refresh-Tap direkt beim ersten sichtbaren Live-Block (standardmässig `Wetter zum nächsten Termin`) erzwingt bei Bedarf einen neuen Abruf der Live-Wetter- und Rheindaten, ohne den Umweg über Einstellungen.
  - Gleiche und überlappende Terminzeiten werden für die Vorhersage zusammengeführt. Die Terminliste bleibt vollständig; Wetterpunkte, Regenmenge und weitere Summen erscheinen einmal. Getrennte Mittag-/Abendzeiten erhalten Punkte in derselben Vorhersage, Stunden aus der Lücke werden ausgeschlossen.
  - Alle noch laufenden oder kommenden, nicht abgesagten Termine desselben nächsten Tages aus dem öffentlichen Vereinskalender werden chronologisch und unabhängig vom Termin-Titel berücksichtigt, damit z. B. Wettfahren, JP-Prüfungen, Endfahren, Wanderungen und weitere gemeinsame Anlässe Wetter erhalten.
  - Ein früher liegendes reguläres Training aus dem saisonalen Trainingsplan bleibt als Fallback/Ergänzung berücksichtigt, wenn es im Kalender nicht explizit geführt wird.
  - Mittag und Abend erscheinen bei zwei Terminen als Zeitpunkte derselben Vorhersage; bei nur einem Abendtermin erscheinen keine Mittagswerte. Ein ausdrücklich eingetragenes Training wird nicht als Regeltraining dupliziert. Laufende mehrtägige Anlässe verwenden den ausgewählten Tag in Europe/Zurich.
  - Kurze Termine zeigen weiterhin die Prognose über den tatsächlichen Terminzeitraum. Ganztägige oder mindestens fünfstündige Termine zeigen drei Prognosepunkte; ganztägig `06/12/18 Uhr`, lange zeitlich begrenzte Termine anhand Start/Mitte/letzter betroffener Stunde. Zusammenfassungen verwenden ausschliesslich Stunden, die den Terminzeitraum schneiden; das Terminende ist exklusiv.
  - Zusätzlich gibt es eine anordenbare 3-Tage-Wetterkachel für Rheinfelden. Pro Tag zeigt sie die gleichmässig verteilten Prognosepunkte `Morgen · 06 Uhr`, `Mittag · 12 Uhr` und `Abend · 18 Uhr` mit Wetter-Symbol, Temperatur und Regenwahrscheinlichkeit; Tages-Min/Max, Regenmenge, Wind/Böen und UV-Maximum bleiben als kompakte Tageszusammenfassung erhalten. Sie nutzt denselben lokal gecachten Wetterabruf wie das Trainingswetter und erzeugt keinen zusätzlichen API-Request.
- Rhein: zwei Stationskacheln, davon die zweite optional. Aktuelle Abflüsse stehen kompakt nebeneinander; Rheinfelden zeigt oben zusätzlich die Wassertemperatur. Pro aktiver Station kombiniert ein `1h`/`24h`/`7d`-Diagramm Abfluss und Pegel mit zwei dynamischen Y-Achsen; vorhandene Temperaturdaten werden darunter separat dargestellt.
- Termine aus dem öffentlichen PFVR-Google-Kalender mit persistentem Cache, Detailansicht, Teilen, Route und Übergabe an die persönliche Kalender-App.
- Verein und Kontakt mit nativen News, App-Jahresprogramm sowie aus der öffentlichen WordPress-API geladenen nativen Abschnitten für Training, Boote, Fahrtechnik, Vereinsleben, Jungpontoniere, Vorstands-Kontaktkarten, belegte historische Stationen und Kontakt. Die Vereinsübersicht zeigt Vereinsbild, kurzen Einstieg und Sommer-/Winterzeiten mit Treffpunkten. Boote & Sport, Nachwuchs, Vorstand, Geschichte, Vereinsleben und vollständige Trainingsinfos öffnen als normale Unterseiten ohne fachliche Aufklappmenüs. Nur der vollständige Originaltext bleibt optional aufklappbar. Rücknavigation erhält die vorherige Seite und Scrollposition. Öffentliche Seiten werden einzeln für 24 Stunden lokal gecacht; Fehler überschreiben keinen erfolgreichen Stand. Ausgewählte Originalbilder werden nativ angezeigt und auf 16 MiB / 64 Dateien begrenzt gecacht. Quellenänderung und App-Abruf stehen getrennt; die vollständige bereinigte Textquelle bleibt nativ erreichbar. Kontaktformulare, PDFs und grosse Bildgalerien werden bei Bedarf extern geöffnet. Telefon, Navigation zum Depot, E-Mail, Instagram und Facebook stehen als fünf Symbole zentriert unter den Vereins-Kacheln; Kontaktaktionen öffnen direkt die jeweilige Gerätefunktion. Der eigene Schnuppertraining-/Mitgliedschaftseinstieg bleibt auf der öffentlichen Landingpage vor der Erstfreigabe; aus der Website übernommene Texte und Links bleiben als Quellinhalt erhalten.
- Interner Bereich über persönlichen `intern.pfvr.ch`-Link; Konfiguration nur in den Einstellungen und nur lokal gespeichert. App-Ansicht ist Standard und folgt dem nativen Hell-/Dunkelmodus. Die originalen Website-Controls, Werte, Ereignisse und Zustandsfarben bleiben erhalten; die App korrigiert nur Layout und Beschriftungsumbruch. Der Originalmodus bleibt verfügbar.
- Vereinsbeiz: fixierter, dauerhaft lokal gespeicherter Warenkorb, der App-/Prozessneustarts übersteht. Nach einer aus dem Warenkorb gestarteten externen Bank-/TWINT-Zahlung fragt die App beim Zurückkehren ausdrücklich nach dem Erfolg; `Ja` leert den Warenkorb, `Nein` lässt ihn unverändert. Ohne diese Bestätigung wird der Warenkorb nur durch `Warenkorb leeren` zurückgesetzt. Dazu kommen anordenbare Kategorien Trinken/Essen/Feiern, freier Betrag, Swiss-QR-Zahlung, direkte Android-Übergabe an eine unter Einstellungen → Zahlung gewählte Banking-App und TWINT-Zahlungsweg.
- Einstellungen: Bereiche Allgemein, Rhein und Zahlung sowie ein eigenes Menü `Ansicht & Kacheln` für Home, Kasse und Verein.
- App-eigene Oberfläche wahlweise auf Deutsch oder in gut lesbarem `Schwiizerdütsch`; externe/source-seitige Inhalte bleiben in ihrer Originalsprache. Die Erstfreigabe darf öffentliche Vereinsinfos und Social-Links zeigen, initialisiert aber vor erfolgreichem Code keine internen oder Live-App-Bereiche.

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
- Android und iOS-App sollen dieselben fachlichen Kernfunktionen bieten.

## Release

Entwicklung/Test: `0.x.y`. Erster offizieller Release: `1.0.0`.
