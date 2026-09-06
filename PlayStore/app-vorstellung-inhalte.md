# PFVR Rheinfelden - Inhalte für die ausführliche App-Vorstellung

Stand: Android `0.11.4`, Quellstand `main`, fachlich erneut abgeglichen am 06.09.2026.

Dieses Dokument ist die fachliche Basis für eine ausführliche Vorstellung der realen App. Eine spätere Kurzanleitung soll daraus nur die tatsächlich nötigen Bedienhandlungen übernehmen.

## Ziel und Einordnung

Die App bündelt öffentliche Vereinsinformationen, Kalender, Trainingswetter, Rheinwerte, Vereinsnews, Vereinsbeiz-Zahlung und den bestehenden internen An-/Abmeldebereich in einer mobilen Oberfläche. Sie ersetzt die PFVR-Website nicht. Wo sinnvoll, bleiben die Originalquellen direkt erreichbar.

Der interne Bereich bleibt technisch das bestehende `intern.pfvr.ch`. Die App bietet dafür zwei Darstellungen: die für Handys neu angeordnete **App-Ansicht** und die unveränderte **Original-Ansicht** der Website.

## Erstfreigabecode - warum er verwendet wird

Die APK bzw. ein späterer Store-Build kann grundsätzlich weitergegeben, gefunden oder auf Geräten installiert werden, die nicht unmittelbar zum vorgesehenen Nutzerkreis gehören. Der Erstfreigabecode ist deshalb eine **bewusste organisatorische Verteilungshürde**.

Was er tatsächlich bewirkt:

- Vor erfolgreicher Freigabe werden App-Shell, Live-Daten und interne WebViews nicht initialisiert.
- Öffentlich bleiben Landingpage, Sprachwahl und freigegebene Vereinslinks wie Schnuppertraining, Instagram und Facebook.
- Der Code wird offline geprüft. Im Quellcode steht nur ein SHA-256-Prüfwert, nicht der Klartextcode.
- Der aktuelle Code muss nach Normalisierung genau 16 alphanumerische Zeichen lang sein.
- Nach erfolgreicher Eingabe wird nur ein lokales Freigabe-Flag gespeichert.
- Bei Löschen der App-Daten bzw. einer frischen Installation ist die Erstfreigabe erneut erforderlich.

Was er **nicht** ist:

- keine Benutzeranmeldung;
- keine Absicherung persönlicher Vereinsdaten;
- kein Ersatz für den persönlichen Zugang zu `intern.pfvr.ch`;
- keine Sicherheitsgrenze gegen einen technisch motivierten Angreifer, der APK und Hash analysiert.

Der Code ist damit sinnvoll als niedrige Hürde gegen zufällige/ungeplante Nutzung eines öffentlich verteilbaren Builds, darf aber in der Präsentation nicht als Authentifizierung dargestellt werden.

### Geplanter Verteilungsablauf

1. Vor Release zunächst Google Play Internal/Closed Testing.
2. App über den vorgesehenen Play-Kanal verteilen; keine normalen Test-Updates aus Chats oder fremden Mirrors.
3. Freigabecode getrennt und gezielt an Mitglieder/berechtigte Tester kommunizieren.
4. Code einmalig in der App eingeben; danach startet die normale App-Oberfläche.
5. Wer den internen Bereich nutzt, hinterlegt zusätzlich unter Einstellungen seinen persönlichen PFVR-HTTPS-Link.
6. Der persönliche Link bleibt gerätelokal und ist vom Erstfreigabecode getrennt.
7. Bei bekannt gewordenem Freigabecode kann der Hash in einer neuen App-Version rotiert werden.

## Originalquellen bleiben erreichbar

Die native App soll Informationen besser aufbereiten, aber Herkunft und Originalsystem nicht verstecken:

- Der Pfeil `↗` im Kopf öffnet die PFVR-Hauptseite.
- Das Jahresprogramm bietet `Originalkalender`.
- Rhein-Karten und Diagramme verlinken auf die jeweilige BAFU-Station.
- Die Schifffahrtslage verlinkt auf die Schweizerischen Rheinhäfen.
- Vereinsbereiche verlinken auf PFVR-Webseiten, Formulare sowie offizielle Social-Media-Auftritte.
- Im internen Bereich kann jederzeit zwischen **App-Ansicht** und **Original** gewechselt werden.

Damit bleibt die App eine komfortable Oberfläche mit nachvollziehbarer Quelle statt eines abgeschlossenen Informationssilos.

## Interner Bereich - App-Ansicht und Original

### App-Ansicht

Die dunkle mobile Matrix ist die von der App erzeugte Projektion der bestehenden Website-Tabelle. Termine/Kochinformationen stehen links, ausgewählte Personen bilden feste Spalten. Die Statusschaltflächen bleiben die echten Controls der bestehenden PFVR-Seite; die App ordnet sie mobil neu an und passt Darstellung/Touch-Flächen an.

Werkzeugleiste: `Personen | Original | Neu laden`.

- `Personen`: lokale Personenverwaltung bzw. bewusste Interaktion mit dem Website-Control.
- `Original`: Wechsel zur unveränderten Website-Darstellung.
- `Neu laden`: aktuellen Stand erneut von der Website laden.

### Original-Ansicht

Die helle Tabellenansicht ist die ursprüngliche Darstellung von `intern.pfvr.ch` innerhalb derselben App-WebView. Sie dient als Referenz, Alternative und für Website-Funktionen, die die mobile Projektion bewusst nicht abbildet.

Werkzeugleiste: `App-Ansicht | Neu laden`.

### Zahlen und Statusfarben - exakt zum Code

Die kleinen farbigen Zahlen in den Termin-Köpfen werden **nicht von der App neu berechnet**. Die App verschiebt die vorhandenen Elemente der Originaltabelle in die mobile Terminspalte. Damit bleiben die von der Website gelieferten Summen die Quelle der Wahrheit.

Die app-eigene Statusfarblogik für die An-/Abmelde-Controls ist eindeutig:

- Grün `#16863A` = `Mit Essen`.
- Gelb `#F2C94C` = `Ohne Essen`.
- Grau `#6D7880` = `Nicht gewählt`.
- Rot `#C83737` = `Komme nicht`.

Für die Erklärung der Zahlen soll deshalb ebenfalls `Rot = Komme nicht` verwendet werden. Die Formulierung `abgemeldet/nicht anwesend` ist unnötig unpräzise. Grau ist ein Personenstatus; in den gezeigten Terminsummen gibt es keinen zusätzlichen grauen Zähler.

## Lokale Daten und Caching

Öffentliche Termine, Wetter, Rheinwerte und Vereinsnews werden lokal zwischengespeichert. Ziel sind schnelle Anzeige und begrenzte Offline-Fähigkeit. Der letzte erfolgreiche Stand erscheint zuerst, anschließend versucht die App eine Aktualisierung.

Persönliche PFVR-Links, Kachelreihenfolge, Sprache, Theme, Banking-App-Auswahl und interne Darstellungszustände bleiben lokal. Android-Cloud-Backup und Device-to-Device-Transfer sind für App-Daten explizit ausgeschlossen.

## Rheinwerte, Hochwassermarken und Farblogik

Die App zeigt BAFU-Messwerte für Basel-Rheinhalle und Rheinfelden. Beide Stationen zeigen den Wasserstand primär in `m ü.M.`. Nur für Basel-Rheinhalle gibt es zusätzlich den verifizierten relativen Pegel in `cm`; der verwendete Bezug ist `247.20 m ü.M. = 720 cm`, entsprechend Pegelnull `240.00 m ü.M.`.

Für die Schifffahrtslage ist ausschließlich Basel-Rheinhalle maßgeblich:

- 700 cm = HWM I / Voralarm;
- 790 cm = HWM IIb / Sperrung Kleinschifffahrt und Fähren Basel-Rheinfelden;
- 820 cm = HWM IIa / Sperrung Schifffahrt Rheinfelden-Kembs.

Der Abfluss ist ein zusätzlicher hydrologischer Messwert und steuert diese Stufe **nicht**.

### Kurven- und Zahlenfarben im aktuellen Code

Basel-Pegel und Abfluss verwenden dieselbe Stufenfamilie, bleiben aber durch verschiedene Farbtöne unterscheidbar:

- Normal: Pegel türkis, Abfluss neutral stahlblau.
- HWM I: gelbe Farbfamilie.
- HWM IIb: orange Farbfamilie.
- HWM IIa: rote Farbfamilie.
- `UNKNOWN`: neutral/grau.

Im Basel-Diagramm wird jedes historische Segment anhand der zum jeweiligen Zeitpunkt vorliegenden **Pegelstufe** eingefärbt. Auch die Abflusskurve erhält ihre Stufenfarbe aus dem zeitgleichen/nächstliegenden Basel-Pegelwert, nicht aus dem Abflusswert selbst. Die offiziellen HWM-Linien werden auf der Pegelachse eingezeichnet.

Für Rheinfelden sind die Farben visuell aktuell neutral (Pegel türkis, Abfluss stahlblau). Technisch wird dafür jedoch noch `Stage.NORMAL` verwendet. Das ist als P0-Hardeningpunkt festgehalten und wird auf einen wirklich stufenunabhängigen Pfad umgestellt, damit später keine Schifffahrtssemantik versehentlich auf Rheinfelden übertragen wird.

### Offener P0: Datenalter

`navigationStage()` nutzt derzeit den neuesten lokal verfügbaren Basel-Wasserstand ohne harte Altersgrenze. Deshalb kann ein alter Cache weiterhin `Normal` oder eine HWM-Stufe liefern. Vor `1.0.0` gilt als Release-Gate:

- spätestens ab 60 Minuten Messwertalter `UNKNOWN`;
- Messdatum + Uhrzeit sichtbar;
- BAFU-Hinweis auf ungeprüfte Echtzeit-Rohdaten;
- klare Trennung zur amtlichen Schifffahrtsauskunft der Schweizerischen Rheinhäfen.

## Vereinsbeiz / Zahlung - aktueller Stand

Der aktuelle Code verwendet einen lokal eingebauten strukturierten Preiskatalog (`vereinsbeiz_prices.json`) und lokal definierte Zahlungsdaten. Die App berechnet Warenkorb/freien Betrag lokal, erzeugt Swiss-QR-Daten lokal und kann QR-Bild/Zahlungsinformationen an eine ausgewählte Banking-/TWINT-App übergeben.

Die PFVR-App verarbeitet keine Bankzugangsdaten und führt selbst keine Banktransaktion aus.

Die aktuelle IBAN ist im Code hinterlegt. Genau darin liegt das organisatorische Wartungsrisiko: Bei einem späteren Bankwechsel kann eine alte Installation weiterhin einen formal korrekten QR für das alte Konto erzeugen.

## Geplante aktuelle Konfiguration ohne App-Update

Preise, IBAN und öffentliche Vereinslinks sind keine Geheimnisse. Das Sicherheitsziel lautet deshalb nicht `Inhalt vor dem App-Nutzer verstecken`, sondern:

- Transport verschlüsseln;
- Herkunft und Integrität beweisen;
- alte/rollback-konfigurierte Daten erkennen;
- fachlich ungültige Werte ablehnen;
- Offline-Fähigkeit erhalten.

Vorgesehene Architektur:

1. nur lesender PFVR-HTTPS-Endpunkt, z. B. `https://www.pfvr.ch/app/config/v1.json`;
2. TLS/HTTPS für verschlüsselte Übertragung;
3. digital signierter Payload, bevorzugt Ed25519;
4. `keyId` und vorbereiteter Schlüsselrotationspfad;
5. monoton steigende `revision`, damit eine bereits akzeptierte Konfiguration nicht auf einen älteren signierten Stand zurückgerollt werden kann;
6. Schema-, Zeit-, IBAN-, Preis- und URL-Validierung;
7. atomarer Last-known-good-Cache plus eingebauter Fallback;
8. `ETag`/`If-None-Match` für effiziente Aktualisierung;
9. sichtbare Quelle, Revision und Stand in der Kasse;
10. bei deutlich veralteten Zahlungsdaten keine stille QR-Erzeugung mit möglicherweise alter IBAN.

Eine zusätzliche Payload-Verschlüsselung mit einem in der APK eingebetteten symmetrischen Geheimschlüssel wäre hier Scheinsicherheit: Der Schlüssel wäre aus der App extrahierbar und Preise/IBAN sind ohnehin nicht geheim.

## Privater interner Bereich - langfristiges Zielbild

Der persönliche Bereich `intern.pfvr.ch` ist von der öffentlichen Remote-Konfiguration getrennt. Aktuell verwendet die App einen lokal gespeicherten persönlichen HTTPS-Link und eine WebView.

Langfristig ist ein dokumentierter authentifizierter API-Endpunkt besser als DOM-Aufbereitung:

- kurzlebige Zugriffstokens;
- Refresh-Credential lokal über Android Keystore/verschlüsselten App-Speicher schützen;
- serverseitige Rollen und minimal notwendige Rechte;
- explizite POST/PUT-Schreibaktionen nur nach Nutzeraktion;
- nachvollziehbare Fehlercodes/Serverantworten;
- optional Version/ETag gegen konkurrierende Änderungen;
- keine persönlichen Tokens im JavaScript-Seitenkontext;
- keine dynamische Remote-Code-Ausführung.

Ein späterer Pairing-/Login-Ablauf kann den heutigen persönlichen Link einmalig in dieses Tokenmodell überführen. Das setzt Backend-Unterstützung des PFVR voraus und ist deshalb kein kurzfristiger App-only-Fix.

## Sicherheitsreview / Opus-Analyse vom 05.09.2026

Die Analyse ist in den wesentlichen technischen Punkten belastbar und wurde erneut gegen `main` geprüft.

Bestätigt:

- keine Werbung/Analytics/Telemetrie im geprüften Code;
- keine sensiblen Android-Laufzeitberechtigungen;
- keine JavaScript-Bridge und kein dynamisches Nachladen von Code;
- keine produktiven Secrets im Repository;
- P0 Datenalter der Schifffahrtsstufe bestätigt;
- P0 automatische Personen-Wiederherstellung bestätigt: `tryRestoreMissingPerson()` kann ohne expliziten Nutzerklick das echte Website-Select setzen und `input`/`change` auslösen; die konkrete serverseitige Wirkung bleibt ohne Backend-/Gerätetest offen;
- persönlicher Basislink liegt aktuell als `window.__pfvrBaseInternalUrl` im Seitenkontext;
- `external(url)` verwendet derzeit ohne Scheme-Allowlist `ACTION_VIEW`;
- öffentlicher Testschlüssel erfordert eine klare Sideloading-Betriebsregel.

Präzisierung zur Analyse:

- `AppLinkPolicy` enthält bereits Host-Allowlisting für WebViews. Der Opus-Punkt betrifft deshalb **nicht** fehlende Host-Regeln in der WebView, sondern den allgemeinen externen `ACTION_VIEW`-Übergang.
- Die Release-AAB-Retention wurde nach dem Review bereits von 30 auf 7 Tage reduziert. Dieser Punkt ist damit erledigt.
- Die Rhein-Kurvenfarblogik ist abgesehen vom Datenalter/Rheinfelden-`Stage.NORMAL` fachlich stimmig: Abfluss steuert keine Sperrstufe; historische Farben folgen dem Basel-Pegel.

## Eingeleitete Schritte

Kanonische Release-Blocker/Follow-ups:

- Issue #10: Schifffahrtslage fail-safe + Farblogik-Regressionen.
- Issue #11: automatische Personen-Wiederherstellung entfernen.
- Issue #12: WebView-/Link-Hardening und Test-APK-Vertrauensmodell.
- Issue #8: signierte Remote Config für Preise/IBAN/Links inkl. Rollback-Schutz und Schlüsselrotation.

Ältere doppelte Issues #6, #7 und #9 wurden am 06.09.2026 als Duplikate geschlossen.

Die P0-Codeänderungen werden nicht nur dokumentarisch `weggepatcht`: Vor Schließen der Release-Blocker sind Unit-/Regressionstests und ein realer Gerätetest gegen die echte Website bzw. absichtlich alten Rhein-Cache erforderlich.

## Präsentationsregel für Screenshots

Screenshots werden in der Vorstellung als einzelne saubere Bildflächen eingesetzt. Keine gestapelten alten Screenshot-Ebenen, keine sichtbaren Ränder eines vorherigen Bildes im Hintergrund. App-Ansicht und Original-Ansicht werden getrennt dargestellt und klar beschriftet.

## Maßgebliche Quellen im Repository

- `AGENTS.md`
- `PROJECT.md`
- `STATUS.md`
- `Android/app/src/main/java/ch/pfvr/internapp/MainActivity.java`
- `Android/app/src/main/java/ch/pfvr/internapp/InternalAttendanceSkin.java`
- `Android/app/src/main/java/ch/pfvr/internapp/RhineNavigation.java`
- `Android/app/src/main/java/ch/pfvr/internapp/RiverDisplay.java`
- `Android/app/src/main/java/ch/pfvr/internapp/AccessGate.java`
- `Android/app/src/main/assets/vereinsbeiz_prices.json`
- `decisions/security-review-2026-09-05.md`
- `decisions/remote-app-config.md`