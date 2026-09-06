# ADR – Laufzeitkonfiguration und künftige interne App-Schnittstelle

Stand: 2026-09-06 · Zielbild nach Android 0.11.5

## Ausgangslage

Die aktuelle App bezieht öffentliche Termine, Wetter, Rheinwerte und Vereinsnews bereits zur Laufzeit aus externen Quellen. Zwei für den Betrieb wichtige Bereiche sind dagegen noch im App-Paket gebündelt:

- Zahlungsstammdaten wie Empfänger und IBAN sind Konstanten im Android-Code.
- Die Vereinsbeiz-Preisliste liegt als `assets/vereinsbeiz_prices.json` im App-Paket.

Der interne An-/Abmeldebereich wird über die bestehende Seite `intern.pfvr.ch` in einer gehärteten WebView geöffnet. Die mobile App-Ansicht ist eine Darstellungsschicht über den realen Website-Controls; sie ist noch keine eigene serverseitige API.

Das ist für den aktuellen Teststand brauchbar, führt aber bei IBAN-, Preis- oder Prozessänderungen zu einem organisatorischen Problem: Bereits installierte App-Versionen kennen Änderungen erst nach einem App-Update.

## Entscheidung

Langfristig werden zwei technisch getrennte Schnittstellen vorgesehen:

1. **Signierte/vertrauenswürdige Laufzeitkonfiguration für nicht geheime Betriebsdaten** wie Preise, IBAN, Zahlungsempfänger, TWINT-Ziele und weitere zentral pflegbare Parameter.
2. **Authentifizierte interne JSON-API** für personenbezogene Vereinsfunktionen wie An-/Abmeldung und Personenlisten.

Der gemeinsame Erstfreigabecode wird für keine dieser Schnittstellen als Server-Authentifizierung verwendet. Er bleibt ausschließlich eine lokale Zugangshürde für die App-Oberfläche.

## 1. Öffentliche Laufzeitkonfiguration

### Geeignete Inhalte

Beispielhafte Struktur:

```json
{
  "schemaVersion": 1,
  "revision": "2026-09-06T10:00:00+02:00",
  "validFrom": "2026-09-06T00:00:00+02:00",
  "validUntil": "2026-12-31T23:59:59+01:00",
  "payments": {
    "currency": "CHF",
    "payee": "Pontonierfahrverein Rheinfelden",
    "iban": "...",
    "message": "Konsumation Vereinsbeiz"
  },
  "cashCatalog": {
    "title": "Preisliste Vereinsbeiz",
    "validFrom": "2026",
    "categories": []
  }
}
```

Der Endpoint sollte auf einer vom Verein kontrollierten HTTPS-Domain liegen, z. B. unter `pfvr.ch` oder einem dafür vorgesehenen API-Host. Eine konkrete URL wird erst festgelegt, wenn der Serverpfad tatsächlich eingerichtet ist.

### Sicherheitsmodell

- Transport ausschließlich per HTTPS mit normaler Android-Zertifikatsprüfung.
- Antwort enthält `schemaVersion`, `revision`, `validFrom` und `validUntil`.
- Optional zusätzlich eine digitale Signatur der Nutzdaten. Das schützt vor versehentlichen oder kompromittierten Zwischenständen zusätzlich zur TLS-Verbindung.
- Client akzeptiert nur bekannte Schemas und validiert IBAN, Währung, Preisbereiche und Pflichtfelder.
- `ETag`/`If-None-Match` oder Revisionsnummern vermeiden unnötige Downloads.
- Letzte gültige Konfiguration wird lokal gecacht und der Datenstand sichtbar angezeigt.

### Fail-safe für Zahlungsdaten

Zahlungsstammdaten werden strenger behandelt als normale Inhaltsdaten:

- Solange die gecachte Konfiguration innerhalb ihrer Gültigkeit liegt, darf sie offline verwendet werden.
- Ist eine Zahlungs-Konfiguration abgelaufen und kann nicht aktualisiert werden, wird die automatische QR-/Direktzahlungsfunktion deaktiviert oder deutlich als nicht verifiziert markiert.
- Es darf nicht unbegrenzt eine alte IBAN weiterverwendet werden.
- Der freie Betrag und Warenkorb können lokal weiter funktionieren; die eigentliche Zahlungsübergabe wartet auf gültige Stammdaten.

Damit kann ein Bankwechsel zentral wirksam werden, ohne darauf zu hoffen, dass alle Mitglieder sofort ein App-Update installieren.

### Fallback

Die App darf weiterhin einen gebündelten Fallback-Katalog enthalten, damit eine Neuinstallation nicht völlig leer startet. Dieser Fallback bekommt ebenfalls einen sichtbaren Versions-/Gültigkeitsstand. Für Zahlungsstammdaten darf der Fallback nicht unbegrenzt als dauerhaft gültige Wahrheit behandelt werden.

## 2. Authentifizierte interne API

### Ziel

Die heutige WebView bleibt zunächst kompatibler Fallback. Für die eigentliche App-Ansicht soll langfristig eine dokumentierte API bereitstehen, damit DOM-Parsing und Website-Steuerung entfallen können.

### Vorgesehene Funktionen

- aktuelle Termine und zulässige Statusoptionen lesen,
- eigene Person und ausdrücklich hinzugefügte weitere Personen lesen,
- An-/Abmeldestatus lesen und bewusst ändern,
- serverseitige Änderungen als Quelle der Wahrheit übernehmen,
- ggf. Rollen-/Berechtigungsinformationen lesen,
- keine administrativen Funktionen ohne eigene serverseitige Berechtigung.

### Authentifizierung

Die aktuelle persönliche `intern.pfvr.ch`-URL darf nicht dauerhaft zur allgemeinen App-API-Berechtigung hochgestuft werden. Ein geeigneter Migrationspfad wäre:

1. Nutzer authentifiziert sich einmal über den bestehenden Vereinsmechanismus bzw. bestätigt seinen persönlichen Zugang.
2. Der Server tauscht diesen Nachweis gegen ein **kurzlebiges Zugriffstoken** und ggf. ein rotierendes Refresh-Token aus.
3. Tokens werden im privaten App-Speicher unter Nutzung des Android Keystore geschützt.
4. Zugriffstokens besitzen kurze Laufzeiten; Refresh-Tokens sind widerrufbar und werden bei Erneuerung rotiert.
5. Logout/Entzug auf Serverseite macht einen Zugang ungültig, ohne dass ein App-Update nötig ist.

Bis ein solcher Servermechanismus real existiert, wird er nicht im Client vorgetäuscht.

### Transport und API-Sicherheit

- HTTPS/TLS 1.2+ bzw. aktueller Plattformstandard.
- Keine selbstgebauten Verschlüsselungsprotokolle.
- Server prüft Autorisierung für jede schreibende Operation.
- Schreibzugriffe nur nach expliziter Nutzeraktion; lokale Wunschlisten dürfen Serverzustände nicht still überschreiben.
- Idempotente bzw. eindeutig referenzierte Mutationen, damit Wiederholungen bei Netzfehlern keinen Doppelzustand erzeugen.
- Revisions-/Versionsfelder zur Erkennung von Konflikten (`If-Match`/optimistische Sperre) sind sinnvoll.
- Rate-Limits und serverseitiges Audit der Änderungen, soweit der Verein dies fachlich benötigt.
- Certificate Pinning nur, wenn der Verein einen belastbaren Schlüssel-/Zertifikats-Rotationsprozess inklusive Backup-Pin betreiben kann. Andernfalls ist das normale Android Trust-Model robuster.

## 3. Verhältnis zur Originalseite

Die Originalseite bleibt während der Migration bewusst erhalten:

- `Original` öffnet weiterhin die unveränderte PFVR-Darstellung als Referenz/Fallback.
- Neue API-Funktionen werden schrittweise gegen die Originalseite abgeglichen.
- Erst wenn die API alle benötigten Schreib- und Lesefälle belastbar abbildet, kann die App-Ansicht unabhängig vom Website-DOM werden.

So gibt es jederzeit einen Vergleichspfad und keine Big-Bang-Migration.

## 4. Geplanter Ausbau

### Phase A – ohne Backend-Änderung

- Sicherheitskorrekturen 0.11.5 abschliessen.
- aktuelle Preis-/IBAN-Quelle fachlich festlegen und Verantwortlichen benennen.
- JSON-Schema und Validierungsregeln finalisieren.

### Phase B – öffentliche Laufzeitkonfiguration

- read-only HTTPS-Endpoint auf PFVR-Infrastruktur bereitstellen,
- Cache/ETag/Gültigkeit im Client implementieren,
- sichtbaren Datenstand für Preisliste und Zahlungsstammdaten ergänzen,
- abgelaufene Zahlungsdaten fail-safe behandeln.

### Phase C – interne API

- vorhandene `intern.pfvr.ch`-Schreibpfade serverseitig dokumentieren,
- dedizierte API mit Authentifizierung und Berechtigungsprüfung schaffen,
- App-Ansicht auf API umstellen,
- Original-WebView als Fallback beibehalten und später neu bewerten.

## Nicht entschieden / externe Abhängigkeiten

- konkrete Servertechnologie und API-URL,
- Token-/Identity-System des bestehenden PFVR-Backends,
- wer im Verein Preis-/Zahlungsdaten pflegen darf,
- wie ein separater Google-Review-/Demozugang serverseitig umgesetzt wird.

Diese Punkte benötigen Zugriff bzw. Entscheidungen auf der PFVR-Serverseite und werden nicht durch Clientcode erfunden.
