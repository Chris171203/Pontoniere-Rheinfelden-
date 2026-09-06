# Remote App-Konfiguration für PFVR

Stand: 2026-09-06

## Motivation

Einige Vereinsdaten sind aktuell Bestandteil der installierten App und ändern sich nur mit einem App-Update:

- IBAN, Zahlungsempfänger und Zahlungszweck der Vereinsbeiz;
- Preiskatalog der Vereinsbeiz;
- TWINT-/Zahlungslinks;
- einzelne öffentliche Vereinslinks.

Das ist technisch robust und offline-fähig, aber organisatorisch ungünstig: Bei Preis-, Konto- oder Linkänderungen können ältere Installationen veraltete Werte verwenden.

## Grundentscheidung

Für **öffentliche bzw. nicht geheime Vereinskonfiguration** wird kein persönlicher Mitgliederzugang benötigt. Vorgesehen ist ein dedizierter, nur lesender HTTPS-Endpunkt des Vereins, z. B.:

`https://www.pfvr.ch/app/config/v1.json`

HTTPS/TLS verschlüsselt die Übertragung. Für Preise und IBAN ist aber vor allem **Integrität, Herkunft und Aktualität** wichtig, nicht Geheimhaltung. Deshalb wird der Payload zusätzlich digital signiert und gegen Rollback geschützt.

Eine zusätzliche Inhaltsverschlüsselung mit einem in der App eingebetteten symmetrischen Geheimschlüssel ist ausdrücklich nicht vorgesehen: Ein solcher Schlüssel wäre aus der APK extrahierbar und würde für öffentliche Zahlungs-/Preisdaten kein sinnvolles zusätzliches Sicherheitsziel erfüllen.

## Zielarchitektur

1. Die App enthält weiterhin einen eingebauten, getesteten Fallback-Stand.
2. Beim Start bzw. in sinnvollen Intervallen lädt sie die aktuelle Konfiguration per HTTPS.
3. HTTP-Caching (`ETag`/`If-None-Match`) vermeidet unnötige vollständige Downloads.
4. Der Server liefert Payload + digitale Signatur.
5. Die App prüft die Signatur mit einem fest eingebetteten öffentlichen Verifikationsschlüssel (bevorzugt Ed25519).
6. Danach werden Schema, `keyId`, Zeitstempel, monoton steigende `revision` und fachliche Werte validiert.
7. Eine bereits akzeptierte höhere Revision darf nicht durch eine ältere, aber formal korrekt signierte Konfiguration ersetzt werden.
8. Nur eine vollständig gültige Konfiguration ersetzt atomar den letzten bekannten gültigen Stand.
9. Bei Netzwerk-, Signatur-, Schema- oder Rollbackfehlern bleibt der letzte gültige Stand aktiv; fehlt dieser, gilt der eingebettete Fallback.
10. Die Oberfläche zeigt Quelle, Revision, Stand und ggf. einen Hinweis auf einen älteren Fallback.

Der private Signierschlüssel liegt ausschließlich beim Betreiber/Deployment und niemals in App oder öffentlichem Repository.

## Schlüsselrotation

Der Payload enthält eine `keyId`. Die App kennt mindestens den aktuellen öffentlichen Verifikationsschlüssel und kann optional bereits einen nächsten Schlüssel mitführen. Dadurch kann der Betreiber einen geplanten Schlüsselwechsel überlappend vorbereiten.

Ein kompromittierter privater Signierschlüssel lässt sich nicht vollständig ohne App-Update aus einer bereits installierten App widerrufen, wenn diese nur genau diesen Schlüssel kennt. Deshalb soll die Implementierung einen dokumentierten Rotationspfad und mindestens zwei bekannte Key-Slots vorsehen. Produktions- und Testsignierschlüssel für die Remote Config bleiben getrennt.

## Beispiel-Schema

```json
{
  "schemaVersion": 1,
  "revision": 17,
  "keyId": "pfvr-config-2026-a",
  "publishedAt": "2026-09-06T10:00:00+02:00",
  "payment": {
    "validUntil": "2026-12-31T23:59:59+01:00",
    "payee": "Pontonierfahrverein Rheinfelden",
    "iban": "CH58 0076 9440 9013 1200 1",
    "note": "Konsumation Vereinsbeiz",
    "twintQrPdf": "https://www.pfvr.ch/.../Twint_QR.pdf",
    "twintPage": "https://www.pfvr.ch/vereinsbeiz-zahlung/"
  },
  "cashCatalog": {
    "currency": "CHF",
    "validFrom": "2026",
    "categories": []
  },
  "links": {
    "website": "https://www.pfvr.ch/",
    "join": "https://www.pfvr.ch/schnuppertraining-mitglied-werden-formulare/"
  }
}
```

Die Signatur kann separat (`v1.json.sig`) oder in einem klar definierten Envelope übertragen werden. Signiert werden die kanonisch definierten Payload-Bytes; Signaturformat und JSON-Kanonisierung müssen vor der Implementierung festgelegt werden.

## Fachliche Validierung in der App

Zusätzlich zur Signatur:

- bekannte `schemaVersion`;
- bekannte `keyId`;
- `revision` positiv und nicht kleiner als die höchste bereits akzeptierte Revision;
- `publishedAt` nicht unplausibel in der Zukunft;
- Zahlungsdaten nicht über ihre definierte Gültigkeit hinaus verwenden;
- IBAN syntaktisch und Mod-97 gültig;
- nur `CHF`, solange die Kasse nicht explizit Mehrwährung unterstützt;
- Preise endlich, >= 0 und innerhalb eines plausiblen Maximalwerts;
- eindeutige Artikel-IDs;
- Zahlungs-/Vereinslinks nur `https` und auf erwarteten Hosts bzw. expliziter Allowlist;
- keine ausführbaren Inhalte, Skripte oder dynamisch nachzuladenden Klassen in der Konfiguration.

Die digitale Signatur ersetzt diese fachlichen Prüfungen nicht: Auch ein authentischer Payload kann durch einen Bedien- oder Deploymentfehler fachlich falsch sein.

## Caching / Offline

Die App bleibt offline-tolerant:

- `remote verified` = bevorzugt;
- `last known good` = bei temporärem Ausfall;
- `bundled fallback` = letzter Notfallstand.

Der Austausch des aktiven Standes erfolgt atomar erst nach vollständiger Prüfung. Die Kasse zeigt sichtbar, aus welchem Stand Preise und Zahlungsdaten stammen.

Bei einer abgelaufenen oder deutlich zu alten Zahlungsinformation darf die App nicht still einen Swiss-QR mit möglicherweise alter IBAN erzeugen. Funktionen, die keine Kontodaten benötigen (z. B. freier Betrag oder reine Preisansicht), können separat weiter verfügbar bleiben. Für den Katalog kann eine weniger strenge Stale-Regel sinnvoll sein als für die IBAN; diese Regeln werden fachbereichsspezifisch definiert statt mit einem einzigen globalen Ablaufdatum.

## Aktualisierungsrhythmus

Für Preise und Zahlungsdaten reicht ein moderater Refresh, z. B. beim App-Start plus periodisch mit mehreren Stunden Abstand. `ETag`/`If-None-Match` erlaubt dabei eine günstige `304 Not Modified`-Antwort. Ein schneller Polling-Mechanismus bringt für diese Daten keinen Mehrwert.

## Privater interner Bereich: getrennte Architektur

Der persönliche Bereich `intern.pfvr.ch` ist etwas anderes als die Remote-Konfiguration. Für ihn gilt langfristig:

- eigener authentifizierter API-Endpunkt statt DOM-Automation, sobald der Verein dies bereitstellen kann;
- kurzlebige Zugriffstokens statt dauerhaftem Token in einer URL;
- Refresh-Credential lokal über Android Keystore/verschlüsselten App-Speicher schützen;
- TLS/HTTPS für Transport;
- serverseitige Rollen und minimal notwendige Rechte;
- Schreiboperationen nur nach expliziter Nutzeraktion;
- nachvollziehbare Serverantworten und Fehlercodes;
- optional ETag/Versionsnummern für konkurrierende Änderungen statt blindem Überschreiben;
- keine persönlichen Tokens im JavaScript-Seitenkontext;
- keine dynamische Remote-Code-Ausführung.

Ein späterer Pairing-/Login-Ablauf kann den persönlichen Zugang einmalig in ein kurzlebiges API-Tokenmodell überführen. Bis das Backend dies unterstützt, bleibt die heutige WebView mit lokal gespeichertem persönlichen HTTPS-Link das Übergangsmodell.

Der lokale Erstfreigabecode der App bleibt davon getrennt und ist keine Authentifizierung für diesen API-Bereich.

## Sicherheitsziele getrennt betrachtet

- **Vertraulichkeit der Übertragung:** TLS/HTTPS.
- **Herkunft und Integrität der öffentlichen Konfiguration:** Ed25519-Signatur.
- **Schutz gegen Replay/Rollback:** monotone `revision` plus Gültigkeitsregeln.
- **Fehlkonfiguration:** Schema- und Fachvalidierung.
- **Offline-Betrieb:** Last-known-good plus eingebetteter Fallback.
- **Personenbezogene interne Daten:** Authentifizierung, Autorisierung und Keystore-geschützte Credentials - nicht öffentliche Remote Config.

## Einführung

Phase 1: Server-Endpunkt, kanonisches Signaturformat, `revision`/`keyId` und Rotationsregeln festlegen; Testschlüssel erzeugen und Beispielpayload publizieren.

Phase 2: Android-Repository für Remote Config mit Signatur-, Schema-, Zeit-, Revision-, IBAN-, Preis- und URL-Prüfung sowie Last-known-good-Cache implementieren. Ohne gültigen Remote-Stand bleibt die bestehende lokale Konfiguration maßgeblich.

Phase 3: Tests für manipulierte Signatur, unbekannte `keyId`, Rollback, abgelaufene Zahlungsdaten, ungültige IBAN, unplausible Preise, falsche Hosts und Offline-Fallback; anschließend realer Gerätetest.

Phase 4: Nach erfolgreichem Gerätetest IBAN, Preise und Zahlungslinks aus der Remote-Konfiguration beziehen und Quelle/Stand in der Kasse anzeigen.

Phase 5: iOS verwendet später dasselbe Datenformat und dieselbe Signaturprüfung.