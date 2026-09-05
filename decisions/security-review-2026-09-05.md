# Sicherheits- und Robustheitsreview 2026-09-05

Stand der Bewertung: Android `0.11.4`, Paket `ch.pfvr.app`.

## Ausgangslage

Ein vollständiger Quellcode-Review hat keine Hinweise auf Analytics-/Werbe-SDKs, Telemetrie-Endpunkte, JavaScript-Bridges (`addJavascriptInterface`), dynamisches Nachladen von Code, Shell-Aufrufe, Reflection-basierte Umgehungen, sensible Android-Laufzeitberechtigungen oder eingecheckte produktive Geheimnisse ergeben. Die bestehende CI prüft den finalen Permission-Merge, blockiert bekannte persönliche PFVR-Zugangsmuster und trennt Test- und Produktionssignierung.

Die materiellen Risiken betreffen nicht verstecktes Tracking, sondern Datenfrische, Seiteneffekte der WebView-Aufbereitung und die langfristige Pflege von Vereinskonfiguration.

## P0 - Schifffahrtslage darf bei alten Daten nicht weiter als Normal/Warnstufe erscheinen

### Ist-Zustand

`navigationStage()` leitet die Schifffahrtsstufe aus dem zuletzt lokal gespeicherten Basel-Rheinhalle-Wasserstand ab. `currentHydroValue()` wählt zwar den neuesten Messpunkt aus dem Cache, prüft dessen Alter aber nicht. Die Anzeige kennzeichnet einen älteren Cache in einer Nebenzeile, die Stufenlogik selbst bleibt jedoch aktiv.

Bei Rheinfelden wird für die Farblogik aktuell technisch `Stage.NORMAL` eingesetzt, obwohl für die Schifffahrtslage ausdrücklich Basel-Rheinhalle maßgeblich ist.

### Entscheidung

Vor einem öffentlichen Release muss die operative Schifffahrtsanzeige fail-safe werden:

- Stufe nur bei ausreichend aktuellem Basel-Messwert bilden.
- Harte Obergrenze: spätestens ab 60 Minuten Messwertalter `UNKNOWN` statt `NORMAL`/HWM-Stufe.
- Messzeitpunkt mit Datum und Uhrzeit anzeigen, nicht nur Uhrzeit.
- Rheinfelden erhält eine neutrale, stufenunabhängige Messwertpalette; keine technisch gesetzte `NORMAL`-Stufe.
- In der App sichtbar: Echtzeitwerte des BAFU sind ungeprüfte Rohdaten und können Fehler enthalten.
- Sichtbarer Hinweis: keine amtliche Freigabe; für Sperrungen/Hochwassermarken sind die Schweizerischen Rheinhäfen maßgebend.
- Quellenangabe für BAFU-Daten an die BAFU-Empfehlung annähern: `Daten Oberflächengewässer: Abteilung Hydrologie, Bundesamt für Umwelt BAFU` plus Bezugs-/Messzeitpunkt.

Offizielle Grundlagen:

- BAFU FAQ Datenqualität: https://www.hydrodaten.admin.ch/de/fragen
- Schweizerische Rheinhäfen, Pegel/Hochwassermarken: https://port-of-switzerland.ch/hafenservice/pegel/

## P0 - Keine stille Wiederherstellung von Personen über die Website

### Ist-Zustand

Die mobile WebView-Aufbereitung hält eine gerätelokale gewünschte Personenliste. Fehlt eine gewünschte Person in der aktuell geladenen Originaltabelle, kann `tryRestoreMissingPerson()` derzeit selbstständig das echte Website-Select auf diese Person setzen und `input`/`change` auslösen. Das kann - abhängig vom tatsächlichen Verhalten von `intern.pfvr.ch` - serverseitige Änderungen verursachen, ohne dass der Nutzer diese konkrete Wiederherstellung angefordert hat.

### Entscheidung

Automatische schreibende Wiederherstellung wird vor Release entfernt. Zulässig bleibt:

- rein lokale Darstellung/Filterung ohne Schreibwirkung;
- Hinzufügen/Entfernen ausschließlich nach expliziter Nutzeraktion;
- Recovery `Aus Initiallink neu aufbauen` ausschließlich nach ausdrücklicher Bestätigung;
- nach einer serverseitigen Abweichung höchstens ein sichtbarer Hinweis mit einer bewussten Aktion `Personenliste abgleichen`.

Solange das reale Backend nicht dokumentiert ist, gilt: **Serverzustand schlägt lokalen Wunschzustand.**

## P1 - Persönlichen Intern-Link nicht als globale JavaScript-Variable halten

Der Basislink wird derzeit für Recovery-Funktionen als `window.__pfvrBaseInternalUrl` in den Seitenkontext geschrieben. Das ist durch korrektes Quoting vor Injektion geschützt, erweitert aber unnötig den Zugriff anderer Skripte derselben Seite.

Geplant: Basislink nicht als globale Window-Property exponieren. Besser ist eine eng begrenzte, in die App-Skin eingeschlossene Variable bzw. eine native Navigation, die den Basislink nur im Java-Code kennt.

## P1 - Externe URI-Schemata explizit erlauben

`external(url)` startet derzeit `ACTION_VIEW` für die übergebene URI. Die WebView selbst besitzt Host-Regeln, aber der allgemeine externe Übergang soll zusätzlich nur bekannte Schemata akzeptieren.

Zulässige Schemata: `https`, `http`, `mailto`, `tel`, `geo`. Andere Schemata wie `intent:` werden standardmäßig abgewiesen, sofern später kein konkret begründeter und getesteter Anwendungsfall hinzukommt.

## P1 - Öffentlicher Debug-Testschlüssel: Betriebsregel

Der feste Testschlüssel für `ch.pfvr.app.test` ist bewusst öffentlich und darf niemals für Produktion verwendet werden. Daraus folgt: Dritte können technisch eigene APKs mit demselben Testzertifikat signieren.

Bis die Testverteilung vollständig über Google Play Internal Testing erfolgt:

- Test-APK nur aus dem eigenen, nachvollziehbaren GitHub-Actions-Run installieren;
- Commit/Version und SHA-256 gegen das Build-Artefakt prüfen;
- keine Test-APK aus Chats, Dateihostern oder fremden Mirrors als Update installieren.

Produktivpaket `ch.pfvr.app` verwendet einen davon vollständig getrennten Upload-/Play-Signing-Schlüssel.

## P2 - Release-Artefakte

Ein signiertes Produktions-AAB ist kein Signierschlüssel und kann den Schlüssel nicht offenlegen. In einem öffentlichen Repository soll die Artefakt-Retention trotzdem kurz gehalten werden, damit ein noch nicht über Play verteilter Build nicht unnötig lange öffentlich abrufbar bleibt.

## AccessGate / Erstfreigabecode

Der Erstfreigabecode ist ausdrücklich **keine Benutzeranmeldung**. Er verhindert bei zufälliger Weitergabe/Installation der App, dass direkt die für Vereinsmitglieder gedachte App-Oberfläche zugänglich ist. Die Prüfung erfolgt offline gegen einen eingebetteten SHA-256-Prüfwert; nach Erfolg wird nur ein lokales Freigabe-Flag gespeichert.

Der aktuell verwendete Code besteht aus 16 zufällig erzeugten alphanumerischen Zeichen. Für diesen Einsatzzweck ist ein Offline-Dictionary-Angriff auf einen leicht merkbaren Code daher nicht der relevante Fall. Sollte der Code öffentlich werden, muss er in einer neuen App-Version rotiert werden.

## Release-Gate

P0-Punkte müssen vor dem ersten öffentlichen Produktionsrelease behoben und auf realen Geräten geprüft sein. P1-Punkte sollen möglichst im selben Hardening-Schritt umgesetzt werden. `1.0.0` bleibt bis dahin gesperrt.
