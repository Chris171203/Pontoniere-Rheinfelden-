# PFVR Rheinfelden - App-Vorstellung und Betriebskonzept

Stand der Beschreibung: Android `0.11.4` / Code 51, geprüft gegen `main` am 06.09.2026.

Dieses Dokument ist bewusst ausführlicher als eine spätere Endnutzer-Kurzanleitung. Es erklärt Zweck, Funktionen, Datenquellen, Zugangsmodell, Sicherheitsgrenzen und die geplante Weiterentwicklung.

## 1. Ziel der App

Die App bündelt für Mitglieder und Interessierte die wichtigsten PFVR-Funktionen auf dem Smartphone:

- Trainingswetter,
- Rheinwerte und Schifffahrtslage,
- öffentliches Jahresprogramm,
- Vereinsmeldungen und Vereinsinformationen,
- Schnuppertraining/Formulare und Social-Media-Links,
- Vereinsbeiz/Warenkorb/Swiss QR/TWINT,
- persönliche interne An-/Abmeldung,
- anpassbare Kacheln, Hell-/Dunkelmodus sowie Deutsch/Schwiizerdütsch.

Die App ist keine reine Kopie der Website. Öffentliche Daten werden soweit sinnvoll nativ geladen, strukturiert, lokal gecacht und mit sichtbarem Datenstand dargestellt. Bestehende PFVR-Webseiten bleiben gleichzeitig als Originalquelle erreichbar.

## 2. Erstfreigabe - warum ein Code verwendet wird

Beim ersten Start wird ein gemeinsamer 16-stelliger Freigabecode verlangt. Der Klartextcode ist nicht in der App bzw. im Repository hinterlegt; gespeichert ist nur ein SHA-256-Prüfwert. Nach erfolgreicher Prüfung wird lokal ein Freigabe-Flag gesetzt.

### Zweck

Die App soll über Google Play auffindbar sein, enthält nach der Freigabe aber auch vereinsnahe Funktionen, Kasse und den Einstieg in den internen Bereich. Der Code schafft deshalb eine bewusste zweite Schwelle zwischen einer öffentlichen Store-Installation und der vollständigen Vereins-App.

Der Code ist ausdrücklich **keine echte Server-Authentifizierung**. Ein technisch versierter Angreifer könnte eine APK analysieren oder verändern. Personenbezogene interne Daten bleiben deshalb zusätzlich durch den bestehenden persönlichen PFVR-Zugang geschützt.

### Geplanter Ablauf

1. App aus Google Play installieren.
2. Ohne Code ist nur die öffentliche Landingpage aktiv: Sprachwahl, Schnuppertraining/Mitgliedschaft, Instagram und Facebook.
3. Mitglied erhält den Freigabecode über einen vorgesehenen Vereinskanal und gibt ihn einmal ein.
4. Das Gerät wird lokal freigeschaltet; Live-Daten und die eigentlichen App-Bereiche werden initialisiert.
5. Für die interne An-/Abmeldung wird zusätzlich der persönliche `intern.pfvr.ch`-Link unter Einstellungen hinterlegt.
6. App-Daten sind von Cloud-Backup und Device-to-Device-Transfer ausgeschlossen; auf einem neuen Gerät ist die Freigabe deshalb erneut erforderlich.

Später kann der gemeinsame Code bei Bedarf durch serverseitiges Geräte-Enrollment mit individuellen, widerrufbaren Zugängen ersetzt werden.

## 3. Transparenz - Links zu den Originalquellen

Die App soll Informationen aufbereiten, die Originalquellen aber nicht verstecken.

Beispiele im aktuellen Stand:

- Kopfbereich/PFVR-Verweise öffnen die Vereinswebsite.
- Termine besitzen `Originalkalender` für den öffentlichen Google-Vereinskalender.
- Rhein-Karten und Diagramme verlinken auf die jeweilige BAFU-Messstation.
- Die Schifffahrtslage verlinkt auf die Schweizerischen Rheinhäfen.
- Vereinsbereich öffnet Website-Seiten zu Vorstand, Geschichte, Kontakt usw.
- Schnuppertraining, Instagram und Facebook führen zu den offiziellen öffentlichen Zielen.
- TWINT kann die bestehende PFVR-Zahlungsseite bzw. den Vereins-QR öffnen.
- Im internen Bereich kann jederzeit zwischen der optimierten **App-Ansicht** und der unveränderten **Original-Ansicht** der PFVR-Seite gewechselt werden.

Die Originalansicht ist insbesondere für Sonderfälle, Fehlersuche und Funktionsabgleich wichtig.

## 4. Home

Home ist die tägliche Übersicht. Der aktuelle Stand enthält unter anderem:

- nächstes Training und direkten Einstieg in die An-/Abmeldung,
- Trainingswetter für den tatsächlichen relevanten Trainingszeitraum,
- `Rhein aktuell` mit Basel-Rheinhalle und Rheinfelden,
- Rhein-Grafiken für 1 h / 24 h / 7 d,
- nächste öffentliche Termine,
- aktuelle Vereinsmeldungen,
- direkte Einstiege zu weiteren Funktionen.

Die Kacheln werden aus lokal gespeicherten Layoutangaben aufgebaut und können in den Einstellungen angeordnet/ausgeblendet werden.

## 5. Wetter

Die App verwendet feste Koordinaten für Rheinfelden und fragt Open-Meteo ab. Bevorzugt wird MeteoSwiss ICON über Open-Meteo; bei Fehlern wird auf Open-Meteo Best Match zurückgefallen.

Die App liest **nicht** den Gerätestandort. Angezeigt werden für den Trainingszeitraum unter anderem Temperatur, Niederschlagswahrscheinlichkeit/-menge, Wind, Böen, UV-Wert und Wetterzustand.

Der erfolgreiche Stand wird lokal gespeichert. Die Datenquelle und das Alter werden angezeigt.

## 6. Rheinwerte und Schifffahrtslage

### Daten

Die App verwendet die offizielle BAFU-Datenplattform per GraphQL:

- Basel-Rheinhalle: Station 2289,
- Rheinfelden: Station 2091,
- Livewerte,
- 10-Minuten-Mittel für 24 h,
- Stundenmittel für 7 d.

Wasserstand wird primär in `m ü.M.` dargestellt. Nur Basel-Rheinhalle besitzt zusätzlich den verifizierten relativen cm-Bezug. Rheinfelden wird bewusst nicht aus einem vermuteten Pegelnullpunkt in cm umgerechnet.

### Schifffahrtslage

Die offiziellen Marken sind an Basel-Rheinhalle gekoppelt:

- 700 cm: HWM I / Voralarm,
- 790 cm: HWM IIb / Sperrung Kleinschifffahrt und Fähren Basel-Rheinfelden,
- 820 cm: HWM IIa / Sperrung Rheinfelden-Kembs.

Der Abfluss ist ein eigener hydrologischer Messwert und steuert die Sperrstufe nicht.

### Sicherheitsreview 06.09.2026

Die BAFU-Livewerte sind ungeprüfte Rohdaten. Zusätzlich liest `0.11.4` die aktuelle Stufe aus dem letzten lokalen Livewert, ohne den Messzeitpunkt als harte Gültigkeitsbedingung zu verwenden. Deshalb ist vor `1.0.0` ein Fail-safe vorgesehen: veraltete Basel-Daten -> `Keine aktuelle Lage`; zusätzlich sichtbarer Hinweis, dass die App keine amtliche Freigabe ersetzt und die Schweizerischen Rheinhäfen massgebend sind. Umsetzung: Issue #14.

## 7. Jahresprogramm / Termine

Der öffentliche Vereinskalender wird als ICS geladen, geparst und lokal gespeichert. Wiederkehrende Termine, Ausnahmen und abgesagte Termine werden berücksichtigt.

Die App bietet:

- Monats-/Terminliste,
- Detailansicht,
- Standort/Route,
- Teilen,
- Übergabe an die persönliche Kalender-App,
- `Aktualisieren`,
- `Originalkalender`.

Der lokale Stand bleibt bei fehlender Verbindung sichtbar; der Zeitpunkt der letzten Aktualisierung wird angezeigt.

## 8. Vereinsbeiz / Kasse

### Aktueller Stand 0.11.4

Die lokale Preisliste liegt strukturiert in `assets/vereinsbeiz_prices.json` (Stand 2026). Die App berechnet den Warenkorb lokal.

Zahlungsdaten sind derzeit im Quellcode hinterlegt:

- Empfänger: Pontonierfahrverein Rheinfelden,
- IBAN: `CH58 0076 9440 9013 1200 1`,
- Zweck: `Konsumation Vereinsbeiz`.

Die App erzeugt den Swiss-QR lokal. Sie liest keine Bankzugangsdaten, Karten oder Kontosalden und führt selbst keine Banktransaktion aus.

### Banking-App

Unter Einstellungen -> Zahlung kann eine installierte Banking-App gewählt werden. Die App prüft bekannte Apps und tatsächlich verfügbare Android-Schnittstellen. Je nach App wird zuerst eine direkte QR-Bildübergabe versucht, danach folgen Datei-/Text-Fallbacks. TWINT bleibt separat über die PFVR-Lösung verfügbar.

### Aktualitätsproblem

IBAN, Preise und Zahlungslinks sind nicht geheim, können sich aber ändern. Alte App-Versionen würden dann weiter ihren eingebauten Stand verwenden.

Daher ist eine signierte Remote-Konfiguration geplant: HTTPS + digitale Signatur + Versions-/Gültigkeitsdaten, lokaler Last-known-good und eingebauter Fallback. Details: `decisions/app-config-and-internal-api.md` / Issue #17.

## 9. Verein

Der Vereinsbereich bündelt:

- Kurzvorstellung,
- Vereinsnews,
- Jahresprogramm,
- Vorstand,
- Geschichte,
- Depot/Route,
- Telefon,
- E-Mail,
- Kontaktseite,
- Instagram,
- Facebook,
- Schnuppertraining/Mitgliedschaft.

Die meisten Inhalte öffnen bewusst die entsprechende PFVR-Originalseite oder einen externen offiziellen Kanal.

## 10. Einstellungen

### Allgemein

- System/Hell/Dunkel,
- Deutsch/Schwiizerdütsch,
- persönlicher Intern-Link,
- Kachelreihenfolge/-sichtbarkeit,
- Cache-/Datenstände,
- manuelle Aktualisierung,
- Hintergrundaktualisierung.

### Rhein

- Messstationen der beiden Rhein-Kacheln,
- Kachel 2 ein-/ausblenden,
- offizielle Hochwassermarken und Quellenlink.

### Zahlung

- Banking-App wählen/ändern/entfernen,
- dokumentierte oder am Gerät erkannte Übergabemöglichkeit.

## 11. Interner Bereich - zwei Ansichten

### App-Ansicht

Die **App-Ansicht ist die dunkle, für Handys optimierte Matrix**. Termine stehen zeilenweise links, Personen als Spalten rechts. Kopfzeile und horizontale Position werden synchronisiert; die Statusflächen sind gross und farblich erkennbar.

Die App erfindet keine eigenen Teilnahmestatus. Die echten Controls der bestehenden PFVR-Webseite werden in die mobile Darstellung übernommen.

`Personen` öffnet eine Verwaltung zum Hinzufügen/Entfernen. `Aus Initiallink neu aufbauen` besitzt eine Zweitbestätigung und lädt die persönliche Basisansicht neu.

### Original-Ansicht

Die **Original-Ansicht ist die helle, ursprüngliche PFVR-Webseite**. Sie bleibt technisch unverändert und ist über die Schaltfläche `Original` bzw. zurück über `App-Ansicht` erreichbar. Sie dient als Referenz und für Funktionen, die in der mobilen Projektion bewusst nicht nachgebaut werden.

### Security-Review

In `0.11.4` versucht die App-Ansicht beim Aufbau automatisch, lokal gewünschte, aktuell fehlende Personen über das echte Website-Select wiederherzustellen (`input`/`change`). Abhängig von der Website kann dies serverseitig schreiben. Das soll vor `1.0.0` entfernt werden: automatische Darstellung ja, Mutation nur nach explizitem Tap. Umsetzung: Issue #15.

## 12. Datenschutz und Sicherheitsmodell

Aktuell:

- Manifest fordert nur Internetzugriff; CI prüft die gemergten Permissions zusätzlich,
- keine Werbung, kein Analytics-/Tracking-SDK,
- kein eigener Telemetrie-Backendserver,
- HTTPS, Cleartext deaktiviert,
- WebView-Datei-/Contentzugriff deaktiviert,
- Mixed Content blockiert, Safe Browsing aktiv,
- persönliche PFVR-URL nur lokal,
- Cloud-Backup/Device-Transfer für App-Daten ausgeschlossen,
- keine produktiven Signierschlüssel im Repository,
- separates Produktionspaket und Upload-Key für Google Play.

Der öffentliche Testschlüssel von `ch.pfvr.app.test` ist absichtlich öffentlich. Deshalb dürfen Sideload-Tester nur bekannte eigene CI-Artefakte installieren und den SHA-256 prüfen. Produktion nutzt diesen Schlüssel nicht.

## 13. Geplante sichere interne Verbindung

Langfristig soll der persönliche Langzeit-Link durch echtes Geräte-Enrollment ersetzt werden:

1. Auf PFVR-Webseite `App verbinden` auswählen.
2. Kurzlebigen Einmalcode/QR erzeugen.
3. App erzeugt Geräteschlüssel im Android Keystore.
4. Einmalcode + öffentlicher Geräteschlüssel über HTTPS austauschen.
5. Kurzlebiges Access-Token + rotierende, widerrufbare Refresh-Berechtigung.
6. Dokumentierte API für Termine/Personen/Status.
7. Jeder Schreibvorgang benötigt einen ausdrücklichen Nutzerbefehl.

Die bestehende Original-Webansicht kann während der Migration als Support-/Fallbackpfad erhalten bleiben.

## 14. Veröffentlichung

Geplant ist eine private, nichtkommerzielle Veröffentlichung über das persönliche Google-Play-Entwicklerkonto des Entwicklers, mit schriftlicher Genehmigung des PFVR für Name, Logo, Inhalte und Store-Veröffentlichung.

Vor öffentlichem Release offen:

- Vereinsfreigabe,
- finale Datenschutzerklärung/Supportdaten,
- Play-Review-/Demo-Zugang,
- Store-Grafiken aus freigegebenem Masterlogo,
- Issues #14/#15/#16,
- signierter Produktions-Upload-Key / Play App Signing,
- erforderlicher Google-Play-Testtrack des persönlichen Entwicklerkontos.

`1.0.0` bleibt der erste öffentliche Produktionsrelease.