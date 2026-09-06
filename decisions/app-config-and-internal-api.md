# Architekturentscheidung: dynamische App-Konfiguration und interner API-Zugang

Stand: 2026-09-06

## Ausgangslage

In `0.11.4` liegen veränderliche Vereinsdaten teilweise noch fest im App-Paket:

- Zahlungsempfänger und IBAN in `MainActivity.java`,
- Vereinsbeiz-Preise in `assets/vereinsbeiz_prices.json`,
- TWINT-/Vereinslinks als feste URLs,
- der interne An-/Abmeldebereich wird über einen persönlichen `intern.pfvr.ch`-Link und die bestehende Website/WebView angebunden.

Das ist für die Testphase robust und offlinefähig, hat aber einen organisatorischen Nachteil: Eine Preis-, IBAN- oder Linkänderung erreicht alte Installationen erst mit einem App-Update. Für den internen Bereich ist die DOM-Aufbereitung zudem enger an die bestehende Website gekoppelt als ein dokumentierter API-Vertrag.

## Zielbild

Die App soll später zwei klar getrennte Datenkanäle verwenden:

1. **öffentliche, signierte App-Konfiguration** für nicht geheime, aber veränderliche Vereinsdaten,
2. **authentifizierte interne API** für personenbezogene bzw. schreibbare Vereinsfunktionen.

Wichtig: IBAN und Preise müssen dem Benutzer angezeigt werden und sind deshalb keine geheimen Daten. Eine zusätzliche Inhaltsverschlüsselung bringt hier kaum Vertraulichkeit. Entscheidend sind **Authentizität, Integrität und Aktualität**. Dafür werden HTTPS, digitale Signaturen, Gültigkeitszeiten und ein sicherer Fallback verwendet.

---

## 1. Öffentliche signierte App-Konfiguration

### Zweck

Dynamisch pflegbar, ohne dass für jede Änderung eine neue APK/AAB-Version nötig ist:

- Zahlungsempfänger,
- IBAN,
- Zahlungszweck,
- TWINT-/Zahlungslinks,
- Vereinsbeiz-Preise und Depotbeträge,
- Gültigkeitsstand der Preisliste,
- öffentliche Kontakt-/Supportziele,
- optional weitere ungefährliche Vereinsparameter.

### Beispielstruktur

```json
{
  "schemaVersion": 1,
  "configVersion": 17,
  "issuedAt": "2026-09-06T08:00:00Z",
  "validFrom": "2026-09-06T08:00:00Z",
  "expiresAt": "2026-10-06T08:00:00Z",
  "payment": {
    "payee": "Pontonierfahrverein Rheinfelden",
    "iban": "CH58 0076 9440 9013 1200 1",
    "note": "Konsumation Vereinsbeiz",
    "twintUrl": "https://www.pfvr.ch/vereinsbeiz-zahlung/"
  },
  "cashCatalog": {
    "validFrom": "2026",
    "categories": []
  }
}
```

Die eigentliche Signatur kann separat (`app-config.json.sig`) oder in einer signierten Hülle ausgeliefert werden.

### Integrität

Empfohlen:

- Transport ausschließlich über HTTPS,
- Konfiguration mit **Ed25519** serverseitig signieren,
- nur der öffentliche Verifikationsschlüssel liegt fest in der App,
- privater Signierschlüssel bleibt ausschließlich beim Betreiber,
- App akzeptiert eine neue Konfiguration nur bei gültiger Signatur und bekanntem Schema,
- `configVersion` muss monoton steigen; ältere Konfigurationen dürfen eine neuere gültige Version nicht überschreiben,
- `issuedAt`, `validFrom` und `expiresAt` werden geprüft.

Damit kann selbst ein falsch konfigurierter CDN-/Webserver nicht unbemerkt eine andere IBAN oder Preisliste an die App ausliefern, solange der Signierschlüssel geschützt bleibt.

### Cache- und Offlineverhalten

Priorität beim Start:

1. letzte lokal gespeicherte **gültig signierte** Konfiguration,
2. im App-Paket eingebauter Fallback,
3. im Hintergrund Aktualisierung vom Server.

Bei Ablauf (`expiresAt`) darf die App den Stand noch anzeigen, muss ihn jedoch klar als veraltet kennzeichnen. Für zahlungsrelevante Felder ist ein strengeres Verhalten sinnvoll: nach einer definierten maximalen Überziehungszeit entweder Warnung mit Bestätigung oder Zahlung über veraltete Konfiguration sperren und auf die offizielle Vereinsseite verweisen.

### Betrieb

Für den Einstieg reicht ein statischer, öffentlich erreichbarer Endpunkt, z. B.:

- `https://www.pfvr.ch/app/config-v1.json`
- `https://www.pfvr.ch/app/config-v1.json.sig`

Dafür ist kein eigener App-Backendserver erforderlich. Die Datei kann durch eine kleine geschützte Vereinsverwaltung erzeugt und signiert werden.

---

## 2. Authentifizierte interne API

### Warum

Die heutige interne App-Ansicht ist eine mobile Projektion der bestehenden Webseite. Sie verwendet echte Website-Controls und bleibt dadurch kompatibel, ist aber abhängig von DOM-Struktur und persönlicher Zugangs-URL.

Langfristig ist ein dokumentierter API-Vertrag stabiler und sicherer:

- klare Lese-/Schreiboperationen,
- kein DOM-Scraping für zentrale Funktionen,
- explizite Nutzeraktionen für Änderungen,
- Gerätezugänge können einzeln widerrufen werden,
- Tokens müssen nicht als lang lebende URL in der App stehen.

### Enrollment / Gerätekopplung

Vorgeschlagener Ablauf:

1. Mitglied meldet sich auf der bestehenden PFVR-Seite an bzw. öffnet seinen bisherigen persönlichen Zugang.
2. Dort wird einmalig **„App verbinden“** gewählt.
3. Server erzeugt einen kurzlebigen Einmalcode oder QR/Deep-Link (z. B. 5 Minuten gültig, nur einmal nutzbar).
4. Die App erzeugt lokal ein Schlüsselpaar im **Android Keystore**; der private Geräteschlüssel verlässt das Gerät nicht.
5. Einmalcode + öffentlicher Geräteschlüssel werden über HTTPS an den Server übermittelt.
6. Server registriert das Gerät und gibt ein kurzlebiges Access-Token sowie einen rotierenden Refresh-Mechanismus aus.
7. Einmalcode wird sofort ungültig.

### Tokens

- Access-Token kurzlebig, z. B. 5-15 Minuten.
- Refresh-Token rotierend; ein altes Refresh-Token wird nach erfolgreicher Rotation ungültig.
- Refresh-Material lokal nur Keystore-geschützt speichern.
- Gerät/Token serverseitig einzeln widerrufbar.
- Keine geheimen API-Schlüssel fest in der APK.
- Optional Requests mit dem gerätegebundenen Schlüssel signieren (Proof-of-Possession), damit ein kopiertes Token allein nicht genügt.

### API-Schnittstellen - mögliches V1

Nur als fachliches Ziel, endgültig mit dem Betreiber von `intern.pfvr.ch` festlegen:

```text
POST /api/app/v1/enroll
POST /api/app/v1/token/refresh
GET  /api/app/v1/me
GET  /api/app/v1/attendance/events
GET  /api/app/v1/attendance/people
PUT  /api/app/v1/attendance/events/{eventId}/status
DELETE /api/app/v1/devices/{deviceId}
```

### Schreibregeln

- **Keine automatische serverseitige Mutation beim Laden.**
- Jede Status-/Personenänderung benötigt eine eindeutige Nutzeraktion.
- Schreibrequest enthält erwartete Ausgangsversion/ETag oder Revisionsnummer, um konkurrierende Änderungen zu erkennen.
- Requests sind idempotent bzw. besitzen eine `requestId`, damit ein Netzwerkwiederholungsversuch nicht doppelt schreibt.
- Serverantwort liefert den danach tatsächlich gültigen Zustand; UI übernimmt diesen als Quelle der Wahrheit.
- Optional serverseitiges Änderungsprotokoll für Support/Fehlersuche.

### Offline

- zuletzt geladene interne Daten dürfen für einen kurzen Zeitraum **read-only** angezeigt werden, sofern dies datenschutzrechtlich akzeptiert ist,
- keine Offline-Schreibwarteschlange für An-/Abmeldungen in V1; bei fehlender Verbindung muss der Benutzer informiert werden,
- besonders sensible oder überholte Daten erhalten ein sichtbares Alter.

---

## 3. Transport- und App-Härtung

- ausschließlich HTTPS/TLS,
- Cleartext bleibt deaktiviert,
- externe App-Links nur über erlaubte Schemes (`https`, `http`, `mailto`, `tel`, `geo`) und bewusst behandelte Ziele,
- keine JavaScript-Bridge mit nativen Geheimnissen,
- persönliche Tokens nicht in öffentlich lesbarem JavaScript-Kontext ablegen,
- optional Certificate Pinning erst nach sauberem Rotations-/Recovery-Konzept; falsch betriebenes Pinning kann die App sonst selbst aussperren,
- Server- und Config-Schlüsselrotation dokumentieren.

## 4. Migration aus 0.11.4

### Phase A - kurzfristig

- bestehende eingebettete Preise/IBAN bleiben Fallback,
- Herkunft und Stand sichtbar machen,
- Änderungen weiterhin über App-Release.

### Phase B - signierte öffentliche Konfiguration

- `AppConfigRepository` einführen,
- Zahlung und `CashCatalog` aus derselben signierten Konfiguration beziehen,
- Last-known-good + eingebauten Fallback behalten,
- sichtbaren `Stand` und `Quelle` anzeigen.

### Phase C - interner API-Adapter

- API parallel zur bestehenden WebView anbieten,
- zunächst read-only vergleichen,
- danach einzelne Schreibfunktionen hinter expliziten Nutzeraktionen migrieren,
- WebView-Originalansicht als Übergangs-/Supportpfad behalten,
- erst nach nachgewiesener Funktionsparität DOM-Schreiblogik entfernen.

## 5. Sicherheitsprinzip

**Öffentliche Werte werden nicht künstlich geheim gemacht; sie werden nachweisbar echt und aktuell gemacht. Interne/personenbezogene Funktionen erhalten echte serverseitige Authentifizierung und explizite Schreiboperationen.**

Dieses Zielbild ersetzt nicht die kurzfristigen Sicherheitskorrekturen der bestehenden WebView-Integration und der Pegel-Freshness-Logik.