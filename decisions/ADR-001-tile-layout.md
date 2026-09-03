# ADR-001: Stabile, lokal gespeicherte Kachellayouts

Datum: 2026-09-03  
Status: angenommen

## Kontext

Home, Kasse und Verein enthielten zunehmend viele unterschiedlich wichtige Inhalte. Eine starre Reihenfolge erzeugt auf kleinen Geräten lange Seiten und passt nicht zu den verschiedenen Nutzungsprofilen im Verein. Gleichzeitig darf Personalisierung zentrale Funktionen wie den Warenkorb nicht unsichtbar oder unbrauchbar machen.

## Entscheidung

- Jede konfigurierbare Kachel besitzt eine stabile technische ID, einen Anzeigenamen, eine feste sinnvolle Breite und optional den Status `pinned`.
- Reihenfolge und ausgeblendete IDs werden je Bereich ausschließlich lokal in `SharedPreferences` gespeichert.
- Beim Laden werden unbekannte IDs entfernt und neue Kacheln in Standardreihenfolge ergänzt.
- `pinned`-Kacheln bleiben sichtbar und vor allen frei anordenbaren Kacheln; zunächst gilt dies für den Kassen-Warenkorb.
- Die erste Ausbaustufe erlaubt Reihenfolge und Sichtbarkeit, aber keine freie Größenänderung. Kompakt/breit wird fachlich pro Kachel festgelegt.
- Home-Hero und globale Schnellaktionen bleiben außerhalb des anordenbaren Bereichs, damit Einstieg und Navigation stabil bleiben.

## Folgen

- Neue Kacheln können ergänzt werden, ohne bestehende Benutzerlayouts zu zerstören.
- Entfernte Kacheln hinterlassen keine dauerhaft ungültigen Einstellungen.
- Die UI bleibt kontrolliert und auf kleinen Displays testbar.
- Eine spätere Größenwahl ist möglich, benötigt aber eigene Render-Varianten pro unterstützter Kachel.
