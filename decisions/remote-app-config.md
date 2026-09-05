# Remote App-Konfiguration für PFVR

Stand: 2026-09-05

## Motivation

Einige Vereinsdaten sind aktuell Bestandteil der installierten App und ändern sich nur mit einem App-Update:

- IBAN, Zahlungsempfänger und Zahlungszweck der Vereinsbeiz;
- Preiskatalog der Vereinsbeiz;
- TWINT-/Zahlungslinks;
- einzelne öffentliche Vereinslinks.

Das ist technisch robust offline-fähig, aber organisatorisch ungünstig: Bei Preis-, Konto- oder Linkänderungen können ältere Installationen veraltete Werte verwenden.

## Grundentscheidung

Für **öffentliche bzw. nicht geheime Vereinskonfiguration** wird kein persönlicher Mitgliederzugang benötigt. Vorgesehen ist ein dedizierter, nur lesender HTTPS-Endpunkt des Vereins, z. B.:

`https://www.pfvr.ch/app/config/v1.json`

HTTPS verschlüsselt die Übertragung. Für Preise und IBAN ist aber vor allem **Integrität und Aktualität** wichtig, nicht Geheimhaltung. Deshalb soll der Inhalt zusätzlich digital signiert werden.

## Zielarchitektur

1. Die App enthält weiterhin einen eingebauten, getesteten Fallback-Stand.
2. Beim Start bzw. in sinnvollen Intervallen lädt sie die aktuelle Konfiguration per HTTPS.
3. Der Server liefert Payload + Signatur.
4. Die App prüft die Signatur mit einem fest eingebetteten öffentlichen Verifikationsschlüssel (bevorzugt Ed25519).
5. Danach werden Schema, Zeitstempel und fachliche Werte validiert.
6. Nur eine vollständig gültige Konfiguration ersetzt atomar den letzten bekannten gültigen Stand.
7. Bei Netzwerk-/Signatur-/Schemafehlern bleibt der letzte gültige Stand aktiv; fehlt dieser, gilt der eingebettete Fallback.
8. Die Oberfläche zeigt `Stand`, Quelle und ggf. einen Hinweis auf einen älteren Fallback.

Der private Signierschlüssel für die Konfiguration liegt ausschließlich beim Betreiber/Deployment und niemals in App oder öffentlichem Repository.

## Beispiel-Schema

```json
{
  "schemaVersion": 1,
  "publishedAt": "2026-09-05T09:00:00+02:00",
  "validUntil": "2026-10-05T23:59:59+02:00",
  "payment": {
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

Die Signatur kann separat (`v1.json.sig`) oder in einem klar definierten Envelope übertragen werden.

## Fachliche Validierung in der App

Zusätzlich zur Signatur:

- bekannte `schemaVersion`;
- `publishedAt` nicht unplausibel in der Zukunft;
- `validUntil` nicht abgelaufen;
- IBAN syntaktisch und Mod-97 gültig;
- nur `CHF`, solange die Kasse nicht explizit Mehrwährung unterstützt;
- Preise endlich, >= 0 und innerhalb eines plausiblen Maximalwerts;
- eindeutige Artikel-IDs;
- Zahlungs-/Vereinslinks nur `https` und auf erwarteten Hosts bzw. expliziter Allowlist;
- keine ausführbaren Inhalte, Skripte oder dynamisch nachzuladenden Klassen in der Konfiguration.

## Caching / Offline

Die App ist weiterhin offline-tolerant:

- `remote verified` = bevorzugt;
- `last known good` = bei temporärem Ausfall;
- `bundled fallback` = letzter Notfallstand.

Die Kasse muss sichtbar anzeigen, aus welchem Stand Preise und Zahlungsdaten stammen. Bei einer abgelaufenen oder sehr alten Konfiguration kann die App den freien Betrag weiterhin erlauben, aber katalogbasierte Zahlungen bzw. QR-Erzeugung mit veralteter IBAN deutlich blockieren oder bestätigen lassen.

## Privater interner Bereich: getrennte Architektur

Der persönliche Bereich `intern.pfvr.ch` ist etwas anderes als die Remote-Konfiguration. Für ihn gilt langfristig:

- eigener authentifizierter API-Endpunkt statt DOM-Automation, sobald der Verein dies bereitstellen kann;
- kurzlebige Zugriffstokens statt dauerhaftem Token in einer URL;
- Refresh-Credential lokal über Android Keystore/verschlüsselten App-Speicher schützen;
- TLS/HTTPS für Transport;
- serverseitige Rollen und minimal notwendige Rechte;
- Schreiboperationen nur nach expliziter Nutzeraktion;
- nachvollziehbare Serverantworten und Fehlercodes;
- keine persönlichen Tokens im JavaScript-Seitenkontext;
- keine dynamische Remote-Code-Ausführung.

Der lokale Erstfreigabecode der App bleibt davon getrennt und ist keine Authentifizierung für diesen API-Bereich.

## Warum nicht einfach alles verschlüsseln?

Preise, IBAN und öffentliche Links sind keine Geheimnisse. Eine zusätzliche Ende-zu-Ende-Verschlüsselung mit einem in der App eingebetteten geheimen Schlüssel würde kaum Schutz bringen, weil der Schlüssel aus der App extrahierbar wäre. **HTTPS + digitale Signatur + strenge Validierung** löst hier das eigentliche Problem: Manipulation und veraltete Konfiguration.

Für personenbezogene interne Daten ist Authentifizierung/Autorisierung entscheidend; deren Transport erfolgt ohnehin verschlüsselt über HTTPS.

## Einführung

Phase 1: Server-Endpunkt und Signaturformat festlegen, Testschlüssel erzeugen, Beispielpayload publizieren.

Phase 2: Android-Repository für Remote Config mit Signatur-/Schema-/IBAN-/Preisprüfung und Last-known-good-Cache implementieren. Ohne gültigen Remote-Stand bleibt die bestehende lokale Konfiguration maßgeblich.

Phase 3: Nach realem Gerätetest IBAN, Preise und Zahlungslinks aus der Remote-Konfiguration beziehen. Quelle/Stand in der Kasse anzeigen.

Phase 4: iOS verwendet später dasselbe Datenformat und dieselbe Signaturprüfung.
