# Vereinsinhalte für die App — Übernahmeplan

Stand: 2026-09-14. Planungsstand; mit 0.12.8 wird nur die Symbolleiste umgesetzt. Grundlage sind die live abgerufenen öffentlichen WordPress-Seiten sowie der tatsächliche Android-Stand 0.12.7.

## Empfohlene Inhalte

Die Vereinsübersicht erhält einen kurzen Einstieg und wenige klar benannte Bereiche. Längere Texte werden innerhalb der App in aufklappbaren Abschnitten gezeigt. Der bereits vorhandene Kalender und die News bleiben die Ziele für aktuelle Termine und Meldungen.

| Reihenfolge | Bereich | Inhalt aus der Homepage | Darstellung in der App |
|---|---|---|---|
| 1 | Verein auf einen Blick | Gründung, Sport und Gemeinschaft; ein geeignetes Vereinsbild | Kurzer Einstieg mit einem Bild; keine als aktuell ausgegebenen alten Mitgliederzahlen. Quelle: [Verein/Sport](https://www.pfvr.ch/verein/). |
| 2 | Training | Sommer-/Wintertraining, Treffpunkte und Trainingsinhalte | Zwei übersichtliche Abschnitte, Treffpunkt anklickbar; für konkrete Anlässe in den vorhandenen Kalender wechseln. Die Homepage-Zeiten entsprechen derzeit dem App-Regelplan, ihr tatsächlicher Fortbestand wurde nicht beim Verein bestätigt. Quelle: [Training](https://www.pfvr.ch/verein/). |
| 3 | Unsere Boote | Beschreibung von Weidling und Übersetzboot | Zwei kompakte Bildkarten mit den Daten aus der Homepage. Quelle: [Boote](https://www.pfvr.ch/verein/). |
| 4 | Pontoniersport erklärt | Grundidee, Fahren zu zweit, typische Manöver und Sektionsfahren | Kurze Einführung; einzelne Manöver bei Interesse aufklappen, jeweils mit höchstens einem passenden Originalbild. Keine vollständige Bildergalerie in der Übersicht. Quelle: [Sport](https://www.pfvr.ch/verein/). |
| 5 | Jungpontoniere | Ausbildung im Weidling, Knoten, Prüfungen und Lager | Eigener nativer Abschnitt, Kontakt zum JP-Leiter aus der vorhandenen Vorstandsquelle. Alters-/Wettkampfkategorien erst nach aktueller fachlicher Prüfung als verbindliche Information darstellen. Quelle: [Jungpontoniere](https://www.pfvr.ch/verein/jungpontoniere/). |
| 6 | Vereinsleben | Gemeinsames Essen, Pfingstlager, Wanderung und weitere Vereinsaktivitäten | Kurzer Abschnitt mit einem Bild. Aktuelle Durchführungstermine aus dem Kalender, nicht aus allgemeinen Beschreibungen ableiten. Quelle: [Vereinsleben](https://www.pfvr.ch/verein/). |
| 7 | Geschichte | Einleitung, Jubiläumsbuch und vorhandene historische Verweise | Native Einleitung beibehalten; eine kurze Zeitleiste nur mit belegten Ereignissen ergänzen. Das komplette Jubiläumsbuch bleibt über einen deutlich benannten PDF-Link erreichbar. Quelle: [Geschichte](https://www.pfvr.ch/geschichte/). |
| 8 | Vorstand | Funktionen, Namen, öffentliche Kontaktdaten, passende Porträts | Bereits nativ vorhandene Texte zu übersichtlichen Kontaktkarten mit optionalem Porträt weiterentwickeln. Quelle: [Vorstand](https://www.pfvr.ch/verein/vorstand/). |

## Inhaltliche Grenzen

- Die allgemeine Vereinsseite enthält ausdrücklich Mitgliederzahlen mit Bezugsjahr **2021**. Ihr WordPress-Änderungsdatum ist 2022-01-24. Diese Zahlen gehören nicht als aktuelle Statistik in den neuen Einstieg; der unveränderte Quelltext kann in der vollständigen Quellenansicht bestehen bleiben.
- Die Nachwuchsseite wurde laut WordPress zuletzt 2022-02-06 geändert. Ausbildung und Vereinsangebot lassen sich daraus planen; Regelkategorien und Altersvoraussetzungen müssen vor einer aktuellen Kurzfassung gesondert geprüft werden.
- Die Geschichtsseite verweist auf ein Buch bis 1996 und beschreibt eine Erweiterung als Arbeit im Gang. Daraus folgt weder ein abgeschlossenes neues Buch noch eine belastbare Chronik späterer Jahre.
- WordPress-Änderungsdatum, letzter App-Abruf und tatsächliche Gültigkeit sind unterschiedliche Angaben. Der Abrufzeitpunkt darf keinen Eindruck einer frisch bestätigten Vereinsinformation erzeugen.

## Was als Link bleibt

Kontaktformulare, umfangreiche PDF-Dokumente, Bildarchive und die eigenständige Fischessen-Seite öffnen ihr jeweiliges Originalziel. Die wichtigsten Vereinsinformationen werden direkt in der App lesbar. Die bestehenden Telefon-, E-Mail-, Navigations- und Social-Aktionen bleiben in der Symbolleiste. Der eigene Einstieg für Schnuppertraining/Mitgliedschaft bleibt auf der öffentlichen Erstfreigabeseite; vorhandene Verweise innerhalb übernommener Originaltexte bleiben erhalten.

## Umsetzung in zwei Schritten

1. **Zuerst:** gegliederte Abschnitte für Training, Boote und Vereinsleben innerhalb der schon vorhandenen Vereinsansicht. Den aktuellen langen Fliesstext über geeignete Überschriften strukturieren. Vereinsbild und Bootsbilder aus denselben Quellen einbinden und sparsam cachen.
2. **Danach:** Jungpontoniere als weitere öffentliche Seite aufnehmen; Fahrtechnik, Vorstands-Porträts und geprüfte historische Meilensteine ergänzen.

Die vorhandene WordPress-Anbindung bleibt Grundlage. Inhalte werden nach Seite und Überschrift übernommen; keine parallele manuell gepflegte Faktenkopie. Bei geänderten Überschriften oder fehlenden Bildern bleibt der native bereinigte Quelltext als Rückfallansicht erreichbar. Für stabile Inhalte gilt weiter der lokale Tagescache mit manueller Aktualisierung. App-Beschriftungen erhalten Deutsch/Schweizerdeutsch; originale Namen und Homepage-Texte behalten ihre Sprache.

## Prüfkriterien für den späteren Ausbau

- Texte, Bildzuordnung und Kontaktziele anhand des tatsächlichen Quellinhalts vergleichen; keine veralteten Angaben aus dem Datumskontext lösen.
- Jeder übernommene Abschnitt bleibt nach erfolgreichem Abruf offline lesbar; fehlgeschlagene Aktualisierungen erhalten den Cache.
- Übersicht und Abschnitte bleiben bei 320 dp Breite, grosser Schrift sowie Hell-/Dunkelmodus lesbar.
- Reguläre Trainingsinformation und konkrete Kalendertermine werden erkennbar getrennt; keine widersprüchlichen App-Zeitpläne schaffen.
- Native Quelltexte bleiben auch im Schweizerdeutsch-Modus unverändert, während die App-Bedienung vollständig lokalisiert ist.
