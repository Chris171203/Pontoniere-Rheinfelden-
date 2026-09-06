# Bewertung der Opus-Analyse vom 06.09.2026

Basis: Quellstand 0.11.4, anschliessender Abgleich gegen den realen Android-Code und die offiziellen Quellen von BAFU und Schweizerischen Rheinhäfen.

## Gesamturteil

Die Analyse ist in den wesentlichen technischen Punkten belastbar. Besonders die beiden Punkte **veraltete Schifffahrtslage** und **automatische Wiederherstellung fehlender Personen** sind materiell und wurden für 0.11.5 unmittelbar adressiert. Einige Nebenpunkte sind korrekt, aber geringer zu gewichten; der Hinweis zur Aufbewahrungszeit des signierten Play-AAB war bereits veraltet, weil der Workflow zu diesem Zeitpunkt schon auf sieben Tage reduziert war.

## 1. Schifffahrtslage aus alten Daten – bestätigt, hohe Priorität

### Vorher

`navigationStage()` verwendete den letzten gespeicherten Basel-Wasserstand ohne eigene Altersgrenze. Ein Messwert konnte daher trotz längerer Offline-Phase weiter als `Normal`, `HWM I`, `Sperre IIb` oder `Sperre IIa` erscheinen. Der Datenstand wurde zwar dargestellt und ältere Caches markiert, die eigentliche Stufenermittlung berücksichtigte dieses Alter aber nicht.

Das BAFU bezeichnet aktuelle Messwerte ausdrücklich als **ungeprüfte Rohdaten**, die Fehler enthalten können. Die Schweizerischen Rheinhäfen benennen den Pegel Basel-Rheinhalle als massgebende Grundlage für die Hochwassermarken.

### 0.11.5

- aktuelle Schifffahrtsstufe nur noch, wenn sowohl Messzeitpunkt als auch Live-Cache höchstens 60 Minuten alt sind;
- sonst `Lage unklar` statt `Normal` oder Sperrstufe;
- Messdatenstand enthält jetzt Datum und Uhrzeit;
- Home zeigt ausdrücklich, dass BAFU-Aktuellwerte ungeprüfte Rohdaten sind und die angezeigte Lage keine amtliche Freigabe darstellt;
- Hinweis verlinkt auf die Schweizerischen Rheinhäfen;
- Rheinfelden erhält keine künstliche `NORMAL`-Stufe mehr. Pegel und Abfluss bleiben dort neutral stationsbezogen eingefärbt; die offizielle Schifffahrtslage wird weiterhin nur aus Basel abgeleitet.

Bewertung: **voll bestätigt und behoben**.

## 2. Stille Wiederherstellung von Personen – bestätigt, hohe Priorität

### Vorher

`tryRestoreMissingPerson()` konnte beim normalen Aufbau der mobilen internen Ansicht eine lokal gespeicherte Wunschperson automatisch im echten Website-Select auswählen und `input`/`change` auslösen. Damit konnte lokaler Zustand ohne neuen Nutzerklick einen serverseitigen Website-Schreibpfad anstossen.

Ob und wie das reale Backend auf jeden dieser Events reagiert, konnte ohne Zugriff auf `intern.pfvr.ch` nicht geprüft werden. Der Clientcode allein reicht aber aus, um den Mechanismus als unerwünscht einzustufen: Ein lokaler Darstellungswunsch darf einen veränderten Serverzustand nicht still zurückschreiben.

### 0.11.5

- automatische Restore-Ausführung beim normalen Seitenaufbau entfernt;
- fehlende gespeicherte Zusatzpersonen werden nur noch nach einer **expliziten Wiederherstellungsaktion in der Personenverwaltung** angestossen;
- die explizite Aktion darf danach die bereits vorhandene Restore-Sequenz fortsetzen, damit mehrere Personen nicht einzeln technisch rekonstruiert werden müssen;
- Reset/Neuaufbau löscht auch den expliziten Restore-Zustand;
- Regressionstests sichern ab, dass der alte bedingungslose Aufruf nicht wieder eingeführt wird.

Bewertung: **voll bestätigt und behoben**.

## 3. Öffentlicher Debug-Testschlüssel – korrektes Restrisiko

Der Schlüssel ist absichtlich öffentlich und ausschliesslich für `ch.pfvr.app.test`. Das verhindert keine absichtlich nachsignierte manipulierte Test-APK mit demselben Zertifikat. Der Mechanismus war als Testsignierung dokumentiert, das Social-Engineering-Risiko sollte aber deutlicher benannt werden.

Massnahme/Dokumentationsregel:

- Test-APKs nur aus dem eigenen, zugehörigen CI-Run installieren;
- bereitgestellte SHA-256-Prüfsumme verwenden;
- für reguläre Tester möglichst auf Google Play Internal Testing wechseln;
- Produktionspaket `ch.pfvr.app` nutzt einen getrennten geheimen Upload-Key/Play App Signing.

Bewertung: **korrekt, Dokumentations-/Verteilungsrisiko; kein Produktionsschlüsselleck**.

## 4. Persönlicher Intern-Link im JS-Kontext – teilweise bestätigt

Der persönliche Link musste schon wegen der bestehenden Website-Navigation im WebView-Kontext verfügbar sein. Zusätzlich wurde er aber als `window.__pfvrBaseInternalUrl` global gesetzt. Das war unnötig weit sichtbar.

0.11.5 übergibt den Wert nur noch als lokale Variable an die erzeugte Skin-Funktion. Das ändert nicht die grundlegende Tatsache, dass die aktuelle Website selbst den persönlichen Link/Token kennt; es reduziert aber die unnötige globale Exposition im injizierten JavaScript.

Bewertung: **korrekt, kleiner Hardening-Punkt; behoben**.

## 5. Beliebige URI-Schemata in `external(url)` – bestätigt

Der zentrale externe Link-Öffner akzeptierte vorher jedes URI-Schema, das Android per `ACTION_VIEW` auflösen konnte. Für die aktuellen fest hinterlegten App-Links war das kaum ausnutzbar, bei kompromittierten oder künftig dynamischeren Webinhalten wäre die Oberfläche unnötig breit.

0.11.5 erlaubt nur noch `http`, `https`, `mailto`, `tel` und `geo`. `intent:`, `file:`, `javascript:` und unbekannte Schemata werden abgewiesen. Dazu existieren Unit-Tests.

Bewertung: **korrekt, behoben**.

## 6. IBAN und Preise im App-Paket – bestätigt, Architekturthema

Aktuell sind Empfänger/IBAN Android-Konstanten und die Vereinsbeiz-Preise werden aus dem gebündelten Asset `vereinsbeiz_prices.json` geladen. Das ist absichtlich offlinefähig, aber bei Bank- oder Preisänderungen organisatorisch nicht ideal: alte Installationen aktualisieren sich erst mit einem App-Update.

Dafür wurde das Zielbild `decisions/runtime-config-and-internal-api.md` erstellt:

- öffentliche, versionierte HTTPS-Laufzeitkonfiguration für IBAN, Zahlungsempfänger, Preise und weitere nicht geheime Betriebsdaten;
- sichtbarer Datenstand + lokaler Cache;
- zeitlich begrenzte Gültigkeit und Fail-safe bei abgelaufenen Zahlungsstammdaten;
- später eigene authentifizierte JSON-API für personenbezogene interne Funktionen.

Bewertung: **korrekt; serverseitige Abhängigkeit, deshalb geplant statt im Client vorgetäuscht**.

## 7. AccessGate / SHA-256 – korrekt eingeordnet

Der Gate-Code ist kein Login und wird im Code auch nicht als solcher behandelt. Der Klartextcode wird nicht gespeichert; im APK liegt nur der SHA-256-Prüfwert. Bei einem schwachen menschlichen Kennwort wäre Offline-Raten möglich. Der aktuelle Freigabecode ist jedoch ein zufälliger 16-stelliger A-Z/0-9-Code und damit für den vorgesehenen Zweck ausreichend.

Der eigentliche Sinn des Gates ist nicht kryptografische Serverauthentifizierung, sondern:

- zufällige Play-/APK-Installationen sollen nicht unmittelbar die komplette Vereins-App erschliessen;
- vor Freigabe werden eigentliche App-Screens, Live-Daten und interne WebViews noch nicht aufgebaut;
- Interessierte können trotzdem die öffentliche Landingpage, Schnuppertraining/Formulare und Social-Links sehen;
- der persönliche interne PFVR-Zugang bleibt eine **zweite, getrennte Ebene**.

Bewertung: **kein Fehler; Grenze muss klar erklärt bleiben**.

## 8. Signiertes Play-AAB als GitHub-Artefakt – Hinweis teilweise veraltet

Der analysierte Text nennt 30 Tage. Im aktuellen Workflow war die Retention bereits auf **7 Tage** reduziert. Ein signiertes AAB enthält den Upload-Schlüssel nicht; es ist trotzdem unnötig, Vorab-Binärdateien länger als nötig öffentlich verfügbar zu halten.

Bewertung: **kein Schlüsselleck; Resthygiene. Sieben Tage sind für den manuellen Releaseprozess vertretbar.** Eine weitere Reduktion auf einen Tag ist optional, sobald der Store-Uploadprozess eingespielt ist.

## 9. Rechtliche Freigabe – bestätigt und bereits als Release-Blocker geführt

Die App soll als privates Hobbyprojekt über ein persönliches Google-Play-Entwicklerkonto veröffentlicht werden. Vor öffentlicher Veröffentlichung werden schriftliche PFVR-Freigaben für Name, Logo, Inhalte und Store-Nutzung eingeholt. Die vorbereitete Datenschutzerklärung enthält absichtlich noch Herausgeber-/Kontaktplatzhalter.

Bewertung: **bestätigt; kein Codefehler, aber zwingender Release-Schritt**.

## 10. Nicht prüfbare Punkte bleiben offen

Ohne serverseitigen Zugang können weiterhin nicht belastbar beurteilt werden:

- welche konkrete Servermutation `intern.pfvr.ch` bei jedem Website-Control ausführt;
- Session-/Token-Lebensdauer und serverseitige Berechtigungsprüfung;
- welche zusätzlichen Felder das Backend speichert;
- Eignung eines künftigen API-/Review-Zugangs.

Diese Punkte werden nicht aus Clientcode erraten. Vor einer eigenen internen API muss das reale Backend dokumentiert bzw. getestet werden.

## Offizielle Quellen

- BAFU FAQ Datenqualität und Quellenangabe: https://www.hydrodaten.admin.ch/de/fragen
- BAFU Portal / Haftungshinweis: https://www.hydrodaten.admin.ch/de/ueber-das-portal
- Schweizerische Rheinhäfen, Pegel/Hochwassermarken: https://port-of-switzerland.ch/hafenservice/pegel/
