# Schnuppertraining-Link nur vor der Erstfreigabe

Stand: 2026-09-07

## Entscheidung

Der Link `Schnuppertraining & Mitglied werden` ist ausschließlich Teil der öffentlichen Start-/Landingpage vor erfolgreicher Eingabe des Freigabecodes.

Nach erfolgreicher Erstfreigabe wird der Link in der normalen App nicht mehr angeboten. Das betrifft insbesondere:

- den Home-Hero unter `An-/Abmelden` und `Bezahlen`;
- die bisherige Kachel `club_join` im Bereich `Verein` und in dessen Kachelverwaltung.

Die öffentliche Landingpage vor dem Code behält den Hinweis `Neu beim PFVR?`, den Schnuppertraining-/Mitgliedschaftslink und die freigegebenen Social-Links.

## Begründung

Die Landingpage erfüllt zwei Aufgaben: Sie bietet vor der Freigabe einen sinnvollen öffentlichen Einstieg und erklärt Interessierten, wie sie zum Verein finden. Nach der Freigabe richtet sich die App an den vorgesehenen Nutzerkreis; dort ist der wiederholte Mitgliederwerbungs-Link redundant und nimmt auf der Home-Seite Platz von den eigentlichen Vereinsfunktionen weg.

## Abnahme

- Vor Freigabecode: `Schnuppertraining & Mitglied werden` bleibt sichtbar und öffnet die bestehende PFVR-Seite.
- Nach Freigabecode: kein Schnuppertraining-/Mitgliedschaftslink auf Home.
- Nach Freigabecode: keine `club_join`-Kachel im Bereich `Verein` oder in `Ansicht & Kacheln`.
- Bestehende gespeicherte Kachellayouts mit `club_join` werden durch die vorhandene Tile-ID-Normalisierung automatisch bereinigt.
- Regressionstest stellt sicher, dass der Link in `showFirstUseGate()` erhalten bleibt, aber im freigeschalteten App-Bereich nicht mehr vorkommt.
