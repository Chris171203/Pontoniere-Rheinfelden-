# Status

Stand: Testversion `0.10.13` · aktualisiert 2026-09-04.

## Aktueller Teststand

- Die interne An-/Abmeldung wird mobil als gemeinsam horizontal scrollende Matrix dargestellt: Termin- und Kochinformationen bleiben links, jede Person bildet über alle Tage eine feste Spalte. Auf üblichen Handybreiten bleiben mindestens zwei Personenspalten gleichzeitig sichtbar.
- Die Kopfzeile mit `Termin` und den Teilnehmernamen hat neben dem normalen CSS-Sticky-Verhalten jetzt einen Viewport-Fallback: Sobald ein Website-Wrapper `position: sticky` wirkungslos macht, wird eine nicht bedienbare, fixe Spiegel-Kopfzeile direkt am oberen Rand der WebView eingeblendet. Sie übernimmt die horizontale Position ausschließlich vom Tabellenkörper und bleibt dadurch auch tief in langen Terminlisten sichtbar.
- Die Kopfzeile besitzt weiterhin keine eigene Touch-/Scrollfläche. Horizontal gewischt wird nur bei Tagen und An-/Abmelde-Controls; Originalkopf und fixer Fallback werden programmgesteuert synchronisiert.
- Teilnehmernamen werden nur noch in der Kopfzeile angezeigt; die frühere Wiederholung über jedem An-/Abmelde-Control bleibt entfernt.
- Lange Koch-/Verantwortlichkeitsnamen in der linken Terminspalte werden nach dem Rendern anhand der tatsächlich verfügbaren Breite verkleinert. Datum, Zähler, Statusbadges und normaler Termintext werden davon nicht verändert.
- Die An-/Abmelde-Controls in den Personenspalten besitzen größere Touch-Flächen und größere Schrift. Auf sehr schmalen Geräten wird die Größe moderat reduziert.
- Teilnehmernamen werden aus tatsächlichen Tabellenzeilen, DOM-Textteilen, Personenattributen und – soweit vorhanden – stabilen Select-Werten rekonstruiert. Zusammengezogene Namen wie `NeugebauerChristoph` werden getrennt und als `Neugebauer, Christoph` angezeigt.
- Die Originaltabelle ist die Quelle der Wahrheit. Jede dort vorhandene Person wird in der App-Ansicht angezeigt, sofern sie nicht ausdrücklich lokal ausgeblendet wurde.
- `Personen` öffnet die Verwaltung auch dann, wenn die externe Seite keine verwertbare Teilnehmermatrix oder kein Hinzufügen-Control mehr liefert. In diesem Fall erzeugt die App eine lokale Fallback-Verwaltung statt mit einem Toast abzubrechen.
- Die Personenverwaltung enthält dauerhaft `Aus Initiallink neu aufbauen`. Die Aktion löscht ausschließlich lokale PFVR-Personen-, Restore- und Scrollzustände und lädt anschließend den in den App-Einstellungen gespeicherten persönlichen Initiallink neu. Der Initiallink selbst bleibt gespeichert.
- Die Zweitbestätigung für `Aus Initiallink neu aufbauen` wird jetzt zuverlässig zurückgesetzt: nach fünf Sekunden, beim Schließen der Personenverwaltung, beim erneuten Öffnen sowie sobald der Benutzer stattdessen eine andere Aktion in der Verwaltung ausführt.
- Website-Aktionen wie `Alle Personen anzeigen`, `Alle anzeigen`, `Alle hinzufügen` und entsprechende Einblend-/Hinzufügevarianten werden im App-Modus global ausgeblendet und zusätzlich per Capture-Handler blockiert. Ein MutationObserver unterdrückt auch dynamisch nachgeladene Varianten. Im Originalmodus bleibt die Website unverändert.
- Das sichtbare Personen-Select ist ein Proxy; das originale Website-Control bleibt in seinem Formular- und DOM-Kontext. Änderungen werden am Original ausgelöst und neue Zeilen über verzögerte Synchronisierung sowie MutationObserver übernommen.
- Entfernen baut die Website-Personenliste automatisch vom persönlichen Basislink neu auf und stellt nur die verbleibenden Zusatzpersonen wieder her.
- Pro Tag und Person werden ausschließlich die echten Website-Controls verwendet. Die App erfindet keine Essensoptionen. Statusfarben folgen dem aktuell gewählten Original-Control; es gibt keinen zusätzlich von der App erzwungenen Voll-Reload.
- Vertikale Scrollposition und gemeinsame horizontale Matrixposition werden über technisch notwendige Seitennavigationen hinweg gespeichert und wiederhergestellt.
- Home, Kasse und Verein verwenden eine gemeinsame, lokal personalisierbare Kachelarchitektur. Reihenfolge und Sichtbarkeit können unter Einstellungen → Ansicht & Kacheln angepasst werden; der Warenkorb bleibt in der Kasse fixiert.
- Vereinsnews werden nativ über die öffentliche WordPress-REST-API geladen und lokal gecacht. Rhein-, Wetter- und Kalenderdaten zeigen ihren Datenstand und besitzen lokale Fallbacks.
- Banking-Handoff bleibt Share-first. Gespeicherte Zahlungs-QRs verwenden wiederverwendbare Dateinamen wie `PFVR_12.50CHF.png`.
- Debug-Testpaket: `ch.pfvr.app.test`, fester öffentlicher Testschlüssel, Android 16 / API 36.

## Automatisch geprüft

- Unit-Tests prüfen Namensformatierung, mobile Matrix, blockierte Bulk-Personenaktionen, Fallback-Personenverwaltung, Initiallink-Recovery, die neue Viewport-Kopfzeile, horizontale Synchronisierung, automatische Verkleinerung langer Kochtexte und das Zurücksetzen der Recovery-Bestätigung.
- Die Android-CI kompiliert mit Java 17 / Gradle 8.13, führt die Unit-Tests aus, baut das APK und prüft Paketname, Versionsdaten sowie den festen Test-Zertifikatsfingerprint.
- APK- und AAB-Dateien werden nicht im Repository versioniert, sondern ausschließlich als CI-Artefakte erzeugt.

## Noch auf realen Geräten zu prüfen

- Beim vertikalen Scrollen durch lange Terminlisten müssen `Termin` und die aktuell sichtbaren Teilnehmernamen dauerhaft direkt unter der nativen Werkzeugleiste sichtbar bleiben.
- Beim horizontalen Wischen im Tabellenkörper müssen Originalkopf beziehungsweise fixer Kopf exakt dieselbe Personenspalte anzeigen. Die Kopfzeile selbst darf sich nicht per Finger verschieben lassen.
- Lange Koch-/Verantwortlichkeitsnamen müssen sich innerhalb der linken Karte verkleinern, ohne Datum, Zähler oder Terminbeschreibung zu beeinflussen.
- Nach einmaligem Antippen von `Aus Initiallink neu aufbauen` muss die Bestätigung nach fünf Sekunden, nach Schließen/Öffnen oder nach einer anderen Aktion wieder im neutralen Zustand stehen.
- `Alle Personen anzeigen` und entsprechende Bulk-Aktionen dürfen im App-Modus weder sichtbar noch auslösbar sein.
- Hinzufügen, Entfernen, Wieder-Einblenden und Neuaufbau aus Initiallink müssen auch nach einem vollständigen App-Neustart konsistent bleiben.
- Nach Statusänderungen müssen Farbe, serverseitige Speicherung und Scrollposition stimmen.
- Direkte Zahlungsübernahme weiterer Banking-Apps; Yuh ist bestätigt, Neon und Revolut übernahmen das Bild im bisherigen Test nicht.

## Nächste strukturelle Schritte

- Die noch große `MainActivity` weiter in Screen-, Repository- und WebView-Adapter-Komponenten aufteilen.
- Für die private An-/Abmeldeseite langfristig einen klaren Adapter beziehungsweise dokumentierten Backend-Endpunkt verwenden, sobald Zugriff darauf besteht. Weitere DOM-Sonderfälle nicht unkontrolliert in die Hauptlogik einbauen.
- Remote-Katalog mit Server-JSON, lokalem Cache und eingebautem Fallback vorbereiten, sobald ein pflegbarer Website-Endpunkt verfügbar ist.
- Optionaler Upload-Portal-Zugang für Vereinsbilder und Videos.
- Optionale Trainingsbenachrichtigungen, Homescreen-Widget und spätere iOS-Implementierung.
