# Datenschutzerklärung PFVR Rheinfelden – ENTWURF FÜR GOOGLE PLAY

> **Vor Veröffentlichung zwingend ersetzen:** vollständiger Name des privaten App-Herausgebers, veröffentlichungspflichtige Kontaktangaben, Datenschutzkontakt und öffentliche URL. Die Angaben müssen mit dem tatsächlichen persönlichen Google-Play-Entwicklerkonto übereinstimmen. Die schriftliche Genehmigung des Pontonierfahrvereins Rheinfelden zur Nutzung von Vereinsname/Logo/Inhalten und zur Veröffentlichung der App ist separat zu archivieren. Diese Datei ist kein Ersatz für die öffentlich veröffentlichte Datenschutzerklärung.

## 1. Verantwortlicher für die App

**[Vollständiger Name des privaten App-Herausgebers]**  
**[von Google/rechtlich erforderliche Kontaktanschrift einsetzen]**  
E-Mail: **[Datenschutz-/Entwicklerkontakt einsetzen]**  
Projekt-/Datenschutzseite: **[öffentliche HTTPS-URL einsetzen]**

App: **PFVR Rheinfelden** (`ch.pfvr.app`)

Die App wird privat entwickelt und über ein persönliches Google-Play-Entwicklerkonto veröffentlicht. Die Verwendung des Vereinsnamens, der PFVR-Bezeichnung, des Vereinslogos und vereinbarter Vereinsinhalte erfolgt mit Genehmigung des Pontonierfahrvereins Rheinfelden. Der Verein ist dadurch nicht automatisch technischer Betreiber oder Herausgeber der Android-App.

Für Daten, die über die bestehenden Webseiten bzw. Systeme des Pontonierfahrvereins Rheinfelden verarbeitet werden, insbesondere `pfvr.ch` und `intern.pfvr.ch`, gelten zusätzlich die Verantwortlichkeiten und Datenschutzinformationen des jeweiligen Vereinsangebots.

## 2. Grundsatz

Die PFVR-Rheinfelden-App betreibt keinen eigenen Werbe-, Analyse- oder Trackingdienst und enthält nach aktuellem Stand kein Werbe-SDK. Sie überträgt keine Nutzungsstatistiken, Werbe-IDs, Crash-Telemetrie oder Gerätekennungen an einen eigenen App-Backendserver.

Die App verbindet sich für ihre Funktionen direkt mit bestehenden öffentlichen bzw. vereinsinternen Diensten. Dabei können die jeweiligen Server technisch erforderliche Verbindungsdaten verarbeiten.

## 3. Android-Berechtigungen und Geräteschnittstellen

Das eigene App-Manifest fordert Internetzugriff an. Eingebundene Android-Systembibliotheken können zusätzlich gewöhnliche technische Berechtigungen für Hintergrundarbeit in das finale Manifest einbringen. Die tatsächlich zusammengeführte Berechtigungsliste wird deshalb im Release-Build automatisiert geprüft und dokumentiert.

Die App fordert nach aktuellem Stand insbesondere **keine sensiblen Laufzeitberechtigungen** für Gerätestandort, Kamera, Mikrofon, Kontakte, SMS/Telefon, persönlichen Kalender oder allgemeinen Medien-/Speicherzugriff an. Sie verwendet außerdem nicht `QUERY_ALL_PACKAGES`.

Für die Auswahl einer Banking-App prüft die App lokal anhand begrenzter Paket-/Intent-Abfragen, welche kompatiblen Apps auf dem Gerät verfügbar sind. Diese Information wird ausschließlich auf dem Gerät verarbeitet und nicht an den App-Herausgeber, den Verein oder einen eigenen Server übertragen.

## 4. Lokal auf dem Gerät gespeicherte Daten

Je nach Nutzung speichert die App lokal unter anderem:

- gewählte Sprache und Darstellungsoptionen,
- Kachelreihenfolge und Sichtbarkeit,
- bevorzugte Banking-App,
- den vom Nutzer eingegebenen persönlichen Link zum internen PFVR-Bereich,
- den lokalen Freigabestatus der App,
- zwischengespeicherte öffentliche Kalender-, Wetter-, Rhein- und Vereinsdaten,
- lokale Zustände der internen Teilnehmerdarstellung,
- WebView-Cookies und Webdaten für aufgerufene PFVR-Webseiten,
- vorübergehend erzeugte Swiss-QR-Dateien im App-Cache bzw. vom Nutzer ausdrücklich gespeicherte Dateien.

Der persönliche interne PFVR-Link kann eine personenbezogene Zugangskennung enthalten. Er wird ausschließlich lokal auf dem Gerät gespeichert und weder in das öffentliche Quellrepository noch an einen eigenen App-Backendserver übertragen.

## 5. Externe Datenquellen und Netzwerkverbindungen

Für die angezeigten Funktionen stellt die App direkte HTTPS-Verbindungen insbesondere zu folgenden Diensten her:

- `pfvr.ch` für öffentliche Vereinsinhalte, Vereinsmeldungen, Formulare und Vereinsbeiz-Seiten,
- `intern.pfvr.ch` für den zugangsgeschützten vereinsinternen An-/Abmeldebereich,
- Google Calendar für den **öffentlichen** Vereinskalender,
- Open-Meteo für Wetterdaten auf Basis fest hinterlegter Koordinaten in Rheinfelden; der Gerätestandort wird nicht verwendet,
- Bundesamt für Umwelt (BAFU) für öffentliche Rhein-Messdaten,
- von Nutzern ausdrücklich geöffnete externe Ziele wie Instagram, Facebook, Karten- oder Banking-/TWINT-Apps.

Bei direkten HTTP-/HTTPS-Verbindungen können die jeweiligen Anbieter technisch erforderliche Verbindungsdaten wie IP-Adresse, Zeitpunkt, User-Agent und Serverprotokolle verarbeiten. Für die Verarbeitung durch diese Dienste gelten zusätzlich deren jeweilige Datenschutzbestimmungen.

## 6. Öffentliche Vereinsinhalte

Öffentliche Kalender-, Wetter-, Rhein- und Vereinsdaten werden zur schnelleren Anzeige lokal zwischengespeichert. Diese Daten sind keine aus dem Gerät des Nutzers ausgelesenen persönlichen Kalender-, Standort- oder Kontaktdaten.

Die App kann öffentliche PFVR-Webseiten in einer WebView oder im externen Browser öffnen. Externe Links verlassen gegebenenfalls die App.

## 7. Interner PFVR-Bereich

Der interne Bereich basiert auf dem bestehenden `intern.pfvr.ch`-System und wird in einer WebView angezeigt. Der Nutzer hinterlegt seinen persönlichen PFVR-Link lokal auf dem Gerät.

Bei Nutzung des internen Bereichs können personenbezogene Daten bzw. personenbezogene Vereinsdaten sichtbar oder an das bestehende PFVR-System übertragen werden, insbesondere:

- Name bzw. Personenbezug der ausgewählten Teilnehmer,
- An-/Abmeldestatus,
- Essens-/Teilnahmestatus oder vergleichbare Auswahlwerte,
- technisch erforderliche Sitzungs-/Zugriffsinformationen des bestehenden PFVR-Systems.

Diese Daten werden direkt zwischen der App/WebView und dem bestehenden PFVR-System übertragen. Der private App-Herausgeber betreibt hierfür keinen eigenen Zwischenserver und verwendet diese Daten nicht für Werbung, Profiling oder Analysen.

Der gemeinsame App-Freigabecode ist lediglich eine lokale Zugangshürde vor der App-Oberfläche und ersetzt keine serverseitige Authentifizierung des vereinsinternen Systems.

## 8. Zahlungen und Banking-Apps

Die Vereinsbeiz-Funktion dient der Bezahlung physischer Konsumationen des Vereins.

Die App:

- berechnet Warenkorb bzw. freien Betrag lokal,
- erzeugt Swiss-QR-Zahlungsdaten lokal,
- verarbeitet keine Zugangsdaten zu Bankkonten,
- liest keine Kredit-/Debitkartendaten aus,
- führt selbst keine Banktransaktion aus.

Auf ausdrückliche Nutzeraktion können Zahlungsdaten oder ein QR-Bild an eine ausgewählte Banking-/TWINT-App bzw. über Androids Teilen-/Dateimechanismen übergeben werden. Ab diesem Zeitpunkt erfolgt die weitere Verarbeitung durch die vom Nutzer gewählte Drittanbieter-App nach deren Bedingungen und Datenschutzbestimmungen.

## 9. Werbung, Analytics und Profiling

Die App enthält nach aktuellem Stand:

- keine Werbung,
- kein Ad-SDK,
- kein Analytics-SDK,
- kein eigenes Nutzertracking,
- keine Profilbildung zu Werbe- oder Marketingzwecken.

Falls sich dies künftig ändert, müssen App, Play-Console-Angaben und diese Datenschutzerklärung vor Veröffentlichung der Änderung angepasst werden.

## 10. Datensicherheit und Android-Backup

Netzwerkzugriffe der App erfolgen über HTTPS; unverschlüsselter Cleartext-Verkehr ist in der Android-Konfiguration deaktiviert. Persönliche Intern-Links und App-Einstellungen werden im privaten App-Speicher des jeweiligen Android-Geräts gehalten.

Cloud-Backup ist deaktiviert. Zusätzlich schließen die App-Regeln Dateien, Datenbanken, Shared Preferences und externe App-Dateien ausdrücklich von Android-Backup und Device-to-Device-Transfer aus. Damit sollen insbesondere der persönliche PFVR-Link, Freigabezustände und interne Teilnehmerzustände nicht automatisch auf ein anderes Gerät übertragen werden.

Die Sicherheit und Aufbewahrung serverseitiger Daten des bestehenden PFVR-Systems sowie externer Dienste liegt zusätzlich in deren jeweiligem Verantwortungsbereich.

## 11. Speicherdauer, Löschung und Anfragen

Öffentliche Cache-Daten können innerhalb der App gelöscht werden. Lokale App-Daten einschließlich Einstellungen und persönlichem Intern-Link können über die Android-Systemeinstellungen bzw. durch Löschen der App-Daten oder Deinstallation entfernt werden.

Personenbezogene Daten, die bereits an das bestehende PFVR-System übertragen wurden, werden dadurch nicht automatisch auf dem PFVR-Server gelöscht. Für Auskunft, Berichtigung oder Löschung dieser Vereinsdaten ist der für das jeweilige PFVR-System verantwortliche Verein zu kontaktieren. Der App-Herausgeber kann nur Auskunft über die von der App selbst verantworteten lokalen bzw. eigenen Datenflüsse geben.

Die App selbst bietet keine eigenständige Kontoerstellung an. Ein bestehender PFVR-Zugang wird außerhalb der App verwaltet.

## 12. Kinder und Jugendliche

Die App ist eine Vereins-/Sportanwendung. Das tatsächlich in Google Play deklarierte Zielalter muss vor Veröffentlichung vom Herausgeber festgelegt und mit dem vorgesehenen Vereinsgebrauch abgestimmt werden. Sofern die App für Minderjährige angeboten wird, gelten die entsprechenden gesetzlichen und Google-Play-Anforderungen zusätzlich.

## 13. Änderungen

Diese Datenschutzerklärung wird angepasst, wenn sich Funktionen, Datenflüsse, externe Dienste oder der Herausgeber wesentlich ändern. Maßgeblich ist die unter der in Google Play angegebenen öffentlichen URL veröffentlichte Fassung.

---

## Release-Prüfung vor Veröffentlichung

- `[ ]` Vollständiger Name/Kontakt des privaten Herausgebers entspricht dem verifizierten Play-Developer-Profil und den rechtlichen Anforderungen.
- `[ ]` Vereinsfreigabe für Name/Logo/Inhalte/Store-Veröffentlichung ist schriftlich archiviert.
- `[ ]` Öffentliche HTTPS-URL funktioniert ohne Login oder Geoblocking.
- `[ ]` Gleiche Privacy-Policy bzw. Link ist in der App zugänglich.
- `[ ]` Verantwortlichkeit der App und Verantwortlichkeit des PFVR-Backends sind klar getrennt.
- `[ ]` `data-safety-draft.md` wurde gegen die finale App und das reale PFVR-Backend geprüft.
- `[ ]` Finale CI-Berechtigungsliste wurde geprüft.
- `[ ]` Änderungen an Banking, WebView, Analytics, Werbung oder externen SDKs wurden nachgezogen.
