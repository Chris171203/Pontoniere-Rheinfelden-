# Status

Stand: Testversion `0.10.6` · aktualisiert 2026-09-04.

## Implementiert / im Test

- Personen-Hinzufügen lässt das originale Website-Select in seinem Formular/DOM-Kontext und verwendet in der App nur einen Proxy. Änderungen werden am echten Control ausgelöst; neue Personenzeilen werden zusätzlich über verzögerte Synchronisierung und MutationObserver in die mobile Matrix übernommen.
- Die gewünschte Personenliste der internen App-Ansicht wird dauerhaft gespeichert. Fehlende Zusatzpersonen werden beim nächsten Laden über den originalen Website-Select wieder eingeblendet; zusätzliche Personen können lokal aus der Übersicht entfernt werden, ohne An-/Abmeldedaten auf dem Server zu löschen.
- Teilnehmernamen stehen zusätzlich in jeder Statuszelle; lange Namen sind auf zwei Zeilen begrenzt und werden innerhalb der gedeckelten Personenspalte moderat verkleinert.
- Die mobile interne Terminansicht verwendet eine gemeinsam horizontal scrollende Matrix: Termin-/Kochinfo bleibt links sticky, jede Person ist über alle Tage dieselbe feste Spalte und mindestens zwei Personenspalten sind gleichzeitig sichtbar. Unter `+ / − Person` werden vorhandene Website-Aktionen aus der Personen-Spalte wieder sichtbar, sodass zusätzliche Personen über die serverseitig vorhandene Funktion entfernt werden können.
- Die Statusfarbe in der mobilen An-/Abmeldeansicht folgt dem aktuell ausgewählten Original-Control (Button oder Select) und wird nach Änderungen ohne zusätzlichen Seiten-Reload neu berechnet.
- Formulierungen wie `Ich komme, mit Essen` / `Ich komme, ohne Essen` werden zusätzlich zur kurzen Form erkannt. Bei Selects wird die tatsächlich ausgewählte Option ausgewertet.
- Die interne An-/Abmeldeansicht wird mobil aus der vorhandenen Terminmatrix projiziert: Termine stehen untereinander, die Termin-Metadaten links und die tatsächlich hinzugefügten Teilnehmer rechts in horizontal scrollbaren Personenkarten.
- Namen in den Termin-Metadaten werden nicht als Teilnehmer interpretiert; Koch/Termininfo bleibt vollständig beim jeweiligen Tag. Teilnehmernamen stammen ausschließlich aus der linken Personen-Spalte der Originalmatrix.
- Pro Tag/Person werden ausschließlich die echten Website-Controls verschoben. Die App erfindet keine Essensoptionen: wenn `Mit Essen` serverseitig nicht angeboten wird, erscheint diese Auswahl auch in der App-Ansicht nicht.
- `Person zur Liste hinzufügen` wird als globale Teilnehmerverwaltung oberhalb der Termine zusammengefasst und über `+ Person` aufgeklappt. Der originale Select und vorhandene Aktionen wie `Alle anzeigen` bleiben funktional dieselben Website-Controls.
- Der bisherige von der App erzwungene `window.location.reload()` nach einer Statusänderung wurde entfernt. Vor serverseitig notwendigen Navigationen werden vertikale Scrollposition und horizontale Teilnehmerpositionen gespeichert und danach wiederhergestellt.
- Status wie `Mit Essen`, `Ohne Essen`, `Nicht gewählt` und `Komme nicht` bleiben als eigener farbiger Block vom folgenden Termintext getrennt. Die Originalansicht bleibt unverändert.
- Home, Kasse und Verein verwenden eine gemeinsame konfigurierbare Kachelarchitektur.
- Unter Einstellungen → Ansicht & Kacheln können die Kacheln je Bereich verschoben, ein- und ausgeblendet sowie auf den Standard zurückgesetzt werden.
- Reihenfolge und Sichtbarkeit werden nur lokal auf dem Gerät gespeichert. Unbekannte alte Tile-IDs werden entfernt; neu hinzugekommene Kacheln werden automatisch ergänzt.
- Der Warenkorb ist in der Kasse als zentrale Kachel immer sichtbar und an erster Stelle fixiert. Kategorien, freier Betrag, TWINT und Zahlungsdaten bleiben frei anordenbar und ausblendbar.
- Verein ist auf eine breite Über-uns-Kachel und kompakte Aktionskacheln für News, Jahresprogramm, Vorstand, Geschichte, Depot/Route, Telefon, E-Mail und Kontakt umgestellt.
- Home enthält separat anordenbare Kacheln für Trainingswetter, Rhein-Kurzwerte, Rhein-Grafiken, Termine und Vereinsnews. Hero und zentrale Schnellaktionen bleiben fest.
- Native Vereinsnews werden über die öffentliche WordPress-REST-API geladen, lokal gecacht und auf Home sowie in einer nativen News-Liste angezeigt.
- Banking-Handoff bleibt Share-first: jede gewählte Banking-App erhält zuerst dieselben QR-Bildversuche; die Registry steuert nur den Fallback.
- Gespeicherte Zahlungs-QRs verwenden wiederverwendbare Dateinamen wie `PFVR_12.50CHF.png`.
- `ch.pfvr.app.test` wird mit dem reproduzierbaren festen Testschlüssel signiert.
- Android 16 / API 36 als Target.

## Verifiziert

- Unit-Tests prüfen die mobile Tag/Teilnehmer-Projektion, dynamische Breiten, Wiederverwendung echter Website-Controls, globale Personenverwaltung und das Entfernen des erzwungenen Reloads.
- Zusätzliche Unit-Tests prüfen Live-Restyling aus Button-/Select-Werten, verzögerte Neuberechnung und DOM-Mutationserkennung ohne Voll-Reload.
- Unit-Tests prüfen weiterhin Statusaufteilung und die Kachel-Layoutmigration.
- Android-Kompilierung, Unit-Tests, APK-Build, Paket/Version und fester Test-Zertifikatsfingerprint werden in CI geprüft.
- Finale Testidentität: `0.10.6`, `versionCode 30`, Paket `ch.pfvr.app.test`.
- APK/AAB-Dateien werden nicht im Git-Repository versioniert, sondern ausschließlich als CI-Artefakte erzeugt.

## Noch auf realen Geräten zu prüfen

- Zusatzperson über `+ / − Person` auswählen: die echte Website-Aktion muss ausgelöst werden und die neue Person danach ohne manuelles Neuladen in der mobilen Matrix erscheinen.
- Danach App vollständig schließen und erneut öffnen: die hinzugefügte Person muss aus der gespeicherten Ansicht wiederhergestellt werden.
- Nach Statusauswahl muss sich die Farbe sofort bzw. nach kurzer Server-/DOM-Aktualisierung anpassen. Anschließend durch `Neu laden` oder Originalansicht prüfen, ob der Status serverseitig erhalten bleibt.
- Ob die reale interne Matrix exakt mit `erste Zeile = Termine` und `erste Spalte = Teilnehmer` strukturiert ist; der Screenshot spricht dafür, aber die Seite ist nicht öffentlich prüfbar.
- Termine mit unterschiedlichen zulässigen Essens-/Anmeldeoptionen: die mobilen Karten müssen exakt die serverseitig vorhandenen Controls zeigen.
- `Alle anzeigen` muss über das originale Website-Control funktionieren und neu auftauchende Personen direkt in die Matrix übernehmen.
- Scrollposition nach Statusänderung bzw. serverseitigem Aktualisieren; ein technisch notwendiger Seitenwechsel darf den Benutzer nicht mehr an den Seitenanfang zurückwerfen.
- Kachelmenü auf kleiner Displaybreite, Hell-/Dunkelmodus und längere deutsche Bezeichnungen.
- Direkte Zahlungsübernahme weiterer Banking-Apps; Yuh ist bestätigt, Neon und Revolut übernahmen das Bild im bisherigen Test nicht.

## Nächste strukturelle Schritte

- Wenn die reale Seite einen stabilen Hintergrund-Endpunkt für An-/Abmeldungen bereitstellt, kann eine echte partielle Datenaktualisierung ohne Seitennavigation ergänzt werden. Bis dahin bleibt die vorhandene Website-Logik maßgeblich und die App erhält den View-State über Navigationen hinweg.
- Weitere Aufteilung der noch großen `MainActivity` in Screen-/Repository-Komponenten.
- Remote-Katalog mit Server-JSON, lokalem Cache und eingebautem Fallback vorbereiten, sobald ein pflegbarer Website-Endpunkt verfügbar ist.
- Optionaler Upload-Portal-Zugang für Vereinsbilder und Videos.
- Optionale Trainingsbenachrichtigungen und Homescreen-Widget.
- iOS-Implementierung.
