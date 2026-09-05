# PFVR Rheinfelden - Inhalte für die ausführliche App-Vorstellung

Stand: `0.11.4` mit Sicherheitsreview vom 05.09.2026.

Dieses Dokument ist die fachliche Basis für eine ausführliche Vorstellung der App. Eine spätere Kurzanleitung soll daraus nur die tatsächlich nötigen Bedienhandlungen übernehmen.

## Ziel und Einordnung

Die App bündelt öffentliche Vereinsinformationen und mehrere native Zusatzfunktionen in einer mobilen Oberfläche. Sie ersetzt die PFVR-Website nicht. Wo sinnvoll, bleiben die Originalquellen direkt erreichbar. Der interne An-/Abmeldebereich bleibt technisch das bestehende `intern.pfvr.ch`; die App stellt dafür zusätzlich eine für Mobilgeräte optimierte Ansicht bereit.

## Erstfreigabecode - warum er existiert

Die APK bzw. ein späterer Store-Build kann grundsätzlich weitergegeben oder auf Geräten installiert werden, die nicht unmittelbar zum vorgesehenen Nutzerkreis gehören. Gleichzeitig enthält die App nach der Freigabe Funktionen, die auf einen vereinsinternen Bereich verweisen und einen persönlichen PFVR-Zugang aufnehmen können.

Der Erstfreigabecode ist deshalb eine **bewusste Verteilungshürde**:

- Vor erfolgreicher Freigabe werden App-Shell, Live-Daten und interne WebViews nicht initialisiert.
- Öffentlich bleiben nur die Landingpage, Sprachwahl und freigegebene Vereinslinks wie Schnuppertraining, Instagram und Facebook.
- Der Code wird offline geprüft. Im Quellcode steht nur sein SHA-256-Prüfwert, nicht der Klartextcode.
- Nach erfolgreicher Eingabe wird nur ein lokales Freigabe-Flag gespeichert.
- Der Code ist **keine Benutzeranmeldung** und ersetzt keinen persönlichen Zugang zu `intern.pfvr.ch`.
- Bei Löschen der App-Daten bzw. einer frischen Installation ist die Erstfreigabe erneut erforderlich.

Geplanter Ablauf bei Verteilung: App über Google Play bereitstellen -> Freigabecode gezielt an Mitglieder/berechtigte Tester kommunizieren -> Erstfreigabe einmalig durchführen -> optional persönlichen PFVR-Link unter Einstellungen hinterlegen.

## Originalquellen bleiben erreichbar

Die native App soll Informationen besser aufbereiten, aber die Herkunft nicht verstecken:

- Der Pfeil `↗` oben rechts öffnet die PFVR-Hauptseite.
- Das Jahresprogramm bietet zusätzlich `Originalkalender`.
- Rhein-Karten und Diagramme verlinken auf die jeweilige BAFU-Station.
- Die Schifffahrtslage verlinkt auf die Schweizerischen Rheinhäfen.
- Vereinsbereiche verlinken auf die entsprechenden PFVR-Webseiten, Formulare und Social-Media-Auftritte.
- Im internen Bereich kann jederzeit zwischen **App-Ansicht** und **Original** gewechselt werden.

Damit bleibt die App eine komfortable Oberfläche mit nachvollziehbarer Quelle statt eines abgeschlossenen Informationssilos.

## Interner Bereich - App-Ansicht und Original

### App-Ansicht

Die dunkle mobile Matrix ist die von der App erzeugte Darstellung. Termine/Kochinformationen stehen links, ausgewählte Personen bilden feste Spalten. Die Statusschaltflächen bleiben die echten Controls der bestehenden PFVR-Seite; die App ordnet sie nur mobil neu an und passt Darstellung/Bedienflächen an.

Werkzeugleiste: `Personen | Original | Neu laden`.

`Personen` öffnet die lokale Personenverwaltung. `Original` wechselt zur unveränderten Website-Darstellung. `Neu laden` lädt den aktuellen Stand erneut von der Website.

### Original-Ansicht

Die helle Tabellenansicht ist die ursprüngliche Darstellung von `intern.pfvr.ch` innerhalb der App. Sie dient als Referenz, Alternative und für Funktionen, die die mobile Projektion nicht abbildet.

Werkzeugleiste: `App-Ansicht | Neu laden`.

## Lokale Daten und Caching

Öffentliche Termine, Wetter, Rheinwerte und Vereinsnews werden lokal zwischengespeichert. Ziel ist eine schnelle Anzeige und begrenzte Offline-Fähigkeit. Der jeweils letzte erfolgreiche Stand erscheint zuerst, danach versucht die App eine Aktualisierung.

Persönliche PFVR-Links, Kachelreihenfolge, Sprache, Theme, Banking-App-Auswahl und interne Darstellungszustände bleiben lokal. Android-Cloud-Backup und Device-to-Device-Transfer sind für App-Daten explizit ausgeschlossen.

## Rheinwerte und Schifffahrtslage

Die App zeigt BAFU-Messwerte für Basel-Rheinhalle und Rheinfelden. Basel-Rheinhalle ist für die offiziellen Hochwassermarken der Schifffahrt maßgeblich. Die App bildet 700/790/820 cm als HWM I/HWM IIb/HWM IIa ab; der Abfluss ist ein ergänzender Messwert und steuert die Sperrstufe nicht.

Der Sicherheitsreview vom 05.09.2026 hat hierzu einen P0-Punkt ergeben: Eine Schifffahrtsstufe darf nicht aus beliebig altem Cache weitergeführt werden. Vor `1.0.0` wird deshalb ein harter `UNKNOWN`-Zustand bei veralteten Daten umgesetzt, das Messdatum klarer angezeigt und ein Rohdaten-/Amtshinweis ergänzt.

## Vereinsbeiz / Zahlung

Der aktuelle Stand verwendet einen lokal eingebauten strukturierten Preiskatalog und lokal definierte Zahlungsdaten. Die App berechnet Warenkorb/freien Betrag lokal, erzeugt Swiss-QR-Daten lokal und kann das QR-Bild bzw. Zahlungsinformationen an eine ausgewählte Banking-/TWINT-App übergeben. Die PFVR-App verarbeitet keine Bankzugangsdaten und führt selbst keine Banktransaktion aus.

### Geplante Aktualisierung ohne App-Update

Preise, IBAN und Zahlungslinks sollen langfristig aus einer **signierten Remote-Konfiguration** des Vereins kommen. Vorgesehen ist:

- HTTPS für verschlüsselte Übertragung;
- digital signierter JSON-Payload (z. B. Ed25519) für Integrität;
- eingebetteter öffentlicher Verifikationsschlüssel in der App;
- Schema-, Zeit-, IBAN- und Preisvalidierung;
- atomarer Last-known-good-Cache;
- eingebauter Fallback für Offline-/Serverfehler;
- sichtbarer Stand/Quelle in der Kasse.

Preise und IBAN sind keine Geheimnisse. Deshalb ist Signatur/Integrität wichtiger als zusätzliche Inhaltsverschlüsselung mit einem in der App eingebetteten Geheimschlüssel.

## Langfristige interne API

Für `intern.pfvr.ch` ist langfristig ein dokumentierter authentifizierter API-Endpunkt besser als DOM-Aufbereitung. Zielbild: HTTPS, kurzlebige Zugriffstokens, lokale Absicherung über Android Keystore, serverseitige Rollen, explizite Schreibaktionen und keine persönlichen Tokens im JavaScript-Seitenkontext.

## Sicherheitsreview 05.09.2026

Positiv bestätigt wurden unter anderem: keine Werbung/Analytics/Telemetrie, keine sensiblen Android-Laufzeitberechtigungen, keine JavaScript-Bridge, kein dynamisches Nachladen von Code, keine produktiven Secrets im Repository, getrennte Test-/Produktionssignierung, Permission-Audit und Backup-Ausschluss.

Vor öffentlichem Release müssen insbesondere zwei P0-Punkte geschlossen werden:

1. Fail-safe Schifffahrtslage bei alten BAFU-Daten.
2. Keine automatische schreibende Wiederherstellung fehlender Personen aus einem gerätelokalen Wunschzustand.

Weitere Hardening-Punkte: persönlicher Basislink nicht als globale JS-Variable, externe URI-Schemata allowlisten, Test-APK-Vertrauensmodell klar dokumentieren und Release-Artefakt-Retention reduzieren.

Details: `decisions/security-review-2026-09-05.md` und `decisions/remote-app-config.md`.
