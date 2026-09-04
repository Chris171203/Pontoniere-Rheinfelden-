# Status

Stand: Testversion `0.10.10` · aktualisiert 2026-09-04.

## Aktueller Teststand

- Die interne An-/Abmeldung wird mobil als gemeinsam horizontal scrollende Matrix dargestellt: Termin- und Kochinformationen bleiben links, jede Person bildet über alle Tage eine feste Spalte. Auf üblichen Handybreiten bleiben mindestens zwei Personenspalten gleichzeitig sichtbar.
- Die Kopfzeile der Matrix mit `Termin` und den Teilnehmernamen bleibt beim vertikalen Scrollen oben sichtbar. Die linke Terminspalte bleibt weiterhin beim horizontalen Scrollen fixiert.
- Teilnehmernamen werden nur noch in dieser Kopfzeile angezeigt; die bisherige Wiederholung über jedem An-/Abmelde-Control wurde entfernt. Dadurch steht in jeder Personen-/Termin-Zelle mehr Platz für die eigentliche Auswahl zur Verfügung.
- Die An-/Abmelde-Controls in den Personenspalten besitzen größere Touch-Flächen und größere Schrift. Auf sehr schmalen Geräten wird die Größe moderat reduziert, ohne auf den alten kompakten Stand zurückzufallen.
- Teilnehmernamen werden aus den tatsächlichen Tabellenzeilen, DOM-Textteilen, Personenattributen und – soweit vorhanden – stabilen Select-Werten rekonstruiert. Zusammengezogene Namen wie `NeugebauerChristoph` werden getrennt und als `Neugebauer, Christoph` angezeigt.
- Die Originaltabelle ist die Quelle der Wahrheit. Jede dort vorhandene Person wird in der App-Ansicht angezeigt, sofern sie nicht ausdrücklich lokal ausgeblendet wurde. Generische Namen wie `Person 1` sind nur noch letzter technischer Fallback.
- In der App-Ansicht öffnet `Personen` die Verwaltung zum Hinzufügen und Entfernen. Der Zurückpfeil neben dem Logo wird im internen Bereich nicht angezeigt; Android-Zurück führt direkt zu Home, damit WebView-Historie keine Personenänderung rückgängig macht.
- `Alle anzeigen` beziehungsweise `Alle hinzufügen` wird im App-Modus nicht mehr gespiegelt. Diese Website-Funktion bleibt ausschließlich in der unveränderten Originalansicht verfügbar.
- Das sichtbare Personen-Select ist ein Proxy; das originale Website-Control bleibt in seinem Formular- und DOM-Kontext. Änderungen werden am Original ausgelöst und neue Zeilen über verzögerte Synchronisierung sowie MutationObserver übernommen.
- Entfernen baut die Website-Personenliste automatisch vom persönlichen Basislink neu auf und stellt nur die verbleibenden Zusatzpersonen wieder her. Dadurch verschwindet die Person auch aus der Originalansicht, ohne dass der Benutzer manuell neu laden muss.
- Pro Tag und Person werden ausschließlich die echten Website-Controls verwendet. Die App erfindet keine Essensoptionen. Statusfarben folgen dem aktuell gewählten Original-Control; es gibt keinen zusätzlich von der App erzwungenen Voll-Reload.
- Vertikale Scrollposition und gemeinsame horizontale Matrixposition werden über technisch notwendige Seitennavigationen hinweg gespeichert und wiederhergestellt.
- Home, Kasse und Verein verwenden eine gemeinsame, lokal personalisierbare Kachelarchitektur. Reihenfolge und Sichtbarkeit können unter Einstellungen → Ansicht & Kacheln angepasst werden; der Warenkorb bleibt in der Kasse fixiert.
- Vereinsnews werden nativ über die öffentliche WordPress-REST-API geladen und lokal gecacht. Rhein-, Wetter- und Kalenderdaten zeigen ihren Datenstand und besitzen lokale Fallbacks.
- Banking-Handoff bleibt Share-first. Gespeicherte Zahlungs-QRs verwenden wiederverwendbare Dateinamen wie `PFVR_12.50CHF.png`.
- Debug-Testpaket: `ch.pfvr.app.test`, fester öffentlicher Testschlüssel, Android 16 / API 36.

## Automatisch geprüft

- Unit-Tests prüfen Namensformatierung, CamelCase-Trennung, mobile Matrix, zwei sichtbare Personenspalten, fixierte Matrix-Kopfzeile, vergrößerte An-/Abmelde-Controls, fehlende redundante Namenslabels in den Zellen, Original-Control-Kontext, Personen-Synchronisierung, lokale Ausblendung, unterdrückte Bulk-Aktion, Statusfarben und das Fehlen eines erzwungenen Reloads.
- Eingebettete JavaScript-RegExp-Escapes sind Java-textblockkompatibel eingecheckt; der finale Branch wird unverändert durch die normale Android-CI gebaut.
- Die Android-CI kompiliert mit Java 17 / Gradle 8.13, führt die Unit-Tests aus, baut das APK und prüft Paketname, Versionsdaten sowie den festen Test-Zertifikatsfingerprint.
- APK- und AAB-Dateien werden nicht im Repository versioniert, sondern ausschließlich als CI-Artefakte erzeugt.

## Noch auf realen Geräten zu prüfen

- Beim vertikalen Scrollen müssen `Termin` und alle sichtbaren Teilnehmernamen stehen bleiben; beim horizontalen Scrollen müssen Kopfzeile und Matrix weiterhin exakt zusammenlaufen.
- Die größeren An-/Abmelde-Controls dürfen auch bei langen Beschriftungen wie `Ich komme, ohne Essen` nicht abgeschnitten werden und sollen die Zellen nicht unnötig hochziehen.
- Teilnehmernamen dürfen innerhalb der Tageszeilen nicht mehr zusätzlich über den Controls erscheinen.
- Namen müssen als `Nachname, Vorname` erscheinen; insbesondere `NeugebauerChristoph` und `WiekertStephan` dürfen weder zusammengezogen noch als `Person 1/2` angezeigt werden.
- Der obere Button `Personen` muss die Verwaltung zuverlässig öffnen. Hinzufügen, lokales Entfernen und Wieder-Einblenden müssen ohne Missbrauch des Zurück-Buttons möglich sein.
- Eine bereits in der Originalansicht vorhandene zweite Person muss beim Wechsel in die App-Ansicht dauerhaft bestehen bleiben.
- `Alle anzeigen/hinzufügen` darf im App-Modus nicht erscheinen, muss in der Originalansicht aber unverändert verfügbar bleiben.
- Nach einem vollständigen App-Neustart müssen die gewünschte Personenansicht, ausgeblendete Personen und Statusauswahlen konsistent bleiben.
- Nach Statusänderungen müssen Farbe, serverseitige Speicherung und Scrollposition stimmen.
- Direkte Zahlungsübernahme weiterer Banking-Apps; Yuh ist bestätigt, Neon und Revolut übernahmen das Bild im bisherigen Test nicht.

## Nächste strukturelle Schritte

- Die noch große `MainActivity` weiter in Screen-, Repository- und WebView-Adapter-Komponenten aufteilen.
- Für die private An-/Abmeldeseite langfristig einen klaren Adapter beziehungsweise dokumentierten Backend-Endpunkt verwenden, sobald Zugriff darauf besteht. Weitere DOM-Sonderfälle nicht unkontrolliert in die Hauptlogik einbauen.
- Remote-Katalog mit Server-JSON, lokalem Cache und eingebautem Fallback vorbereiten, sobald ein pflegbarer Website-Endpunkt verfügbar ist.
- Optionaler Upload-Portal-Zugang für Vereinsbilder und Videos.
- Optionale Trainingsbenachrichtigungen, Homescreen-Widget und spätere iOS-Implementierung.
