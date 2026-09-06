# Sicherheits- und Betriebsreview 2026-09-06

Basis: Android `0.11.4` / Code 51 auf `main`, Quellcode, CI- und Play-Store-Unterlagen. Kein Laufzeitzugriff auf `intern.pfvr.ch`; serverseitige Wirkungen der Website-Events sind daher aus dem Clientcode abgeleitet und müssen mit einem Testzugang verifiziert werden.

## Gesamturteil

Die Grundarchitektur zeigt für ein privates Vereinsprojekt bereits ungewöhnlich gute Sicherheitsdisziplin:

- keine Werbung/Analytics-/Tracking-SDKs im aktuellen Build-Setup,
- keine sensiblen Android-Laufzeitberechtigungen; die gemergte Berechtigungsliste wird in CI geprüft,
- Cleartext deaktiviert,
- WebView-Datei-/Contentzugriff deaktiviert und Mixed Content blockiert,
- persönliche PFVR-Zugänge werden nicht eingecheckt und sind von Android-Backup/Device-Transfer ausgeschlossen,
- Test- und Produktionssignierung sind getrennt,
- CI blockiert bekannte persönliche Zugangsfragmente und rohe Keystore-Dateien.

Zwei Punkte sind vor `1.0.0` materiell wichtiger als weitere Features: Freshness/Failsafe der Schifffahrtslage und automatische Personen-Wiederherstellung im internen WebView.

---

## 1. Schifffahrtslage / veraltete Daten - bestätigt, hohe Priorität

### Codebefund

`MainActivity.currentHydroValue()` liest den neuesten Wert aus dem lokalen `data_live`-JSON, prüft aber kein Datenalter. `navigationStage()` verwendet diesen Wert direkt für Basel-Rheinhalle. `hydroSummary()` zeigt den Messzeitpunkt nur als `HH:mm` und ergänzt bei altem Abruf lediglich `· Cache`.

Damit kann ein alter niedriger Pegel weiterhin eine positive `Normal`-/`Unter HWM I`-Aussage erzeugen.

### Externe Einordnung

Die Schweizerischen Rheinhäfen nennen Basel-Rheinhalle als ausschlaggebenden Pegel und veröffentlichen 700/790/820 cm als Hochwassermarken. Das BAFU kennzeichnet aktuelle/Echtzeitdaten als ungeprüfte Rohdaten, die Fehler enthalten können. Die neue BAFU-Datenplattform weist für `data_live` zusätzlich `releaseStatus = 0` aus.

### Entscheidung

Release-Blocker für `1.0.0`. Umsetzung wird in Issue #14 verfolgt. Aktuelle Schifffahrtslage soll nach spätestens 60 Minuten Messdatenalter auf `UNKNOWN` wechseln. Historische Graphwerte bleiben davon unberührt. Zusätzlich sichtbarer Hinweis auf die Schweizerischen Rheinhäfen als massgebende Quelle.

### Rheinfelden-Farbgebung

Der Code setzt für Rheinfelden intern `Stage.NORMAL`, zeigt dort aber **kein Normal-/Freigabebadge**; die resultierenden Farben sind die normale Wasser-/Abfluss-Grundpalette. Der Review-Hinweis ist deshalb als semantische/robustheitsbezogene Verbesserung richtig, aber weniger gravierend als der stale Basel-Status. Ziel bleibt eine explizit stufenunabhängige Neutral-Palette, damit spätere Änderungen nicht versehentlich eine lokale Sperrlogik suggerieren.

---

## 2. Automatische Personen-Wiederherstellung - bestätigt, hohe Priorität

### Codebefund

`InternalAttendanceSkin.tryRestoreMissingPerson()` sucht einen lokal gewünschten, in der aktuellen Quelltabelle fehlenden Namen. Wird eine Option gefunden, setzt der Code `selectedIndex` am echten Website-Select und dispatcht `input` und `change`. `buildMobile()` ruft diesen Restore beim bloßen Aufbau der App-Ansicht auf.

Die tatsächliche serverseitige Wirkung hängt vom Verhalten von `intern.pfvr.ch` ab und ist ohne Testzugang nicht direkt verifiziert. Clientseitig ist die Mutation jedoch eindeutig vorhanden.

### Entscheidung

Automatisches Lesen/Skinning bleibt erlaubt; schreibende Aktionen sollen eine klare Nutzeraktion erfordern. Issue #15 verfolgt die Entfernung des automatischen Restore-Pfads. Eine fehlende lokal gemerkte Person soll nur angezeigt werden; Wiederhinzufügen erfolgt über `Personen` nach expliziter Auswahl.

---

## 3. Öffentlicher Testschlüssel - bewusstes Restrisiko

Der feste Testschlüssel ist absichtlich öffentlich und ausschließlich für `ch.pfvr.app.test`. Dadurch sind Test-APKs überinstallierbar. Gleichzeitig kann grundsätzlich auch ein Dritter eine andere APK mit demselben Testzertifikat signieren.

Das ist für Produktion unkritisch, weil `ch.pfvr.app` einen separaten privaten Upload-Key/Play-App-Signing-Pfad verwendet. Für Sideload-Tests gilt trotzdem:

- nur CI-Artefakte aus dem eigenen Repository/Run installieren,
- Version/Commit und `SHA256SUMS.txt` prüfen,
- keine als „Update“ zugesandte Fremd-APK installieren,
- reguläre Tests nach Möglichkeit auf Google Play Internal Testing umstellen.

Dieser Hinweis gehört in die Entwickler-/Tester-Dokumentation.

---

## 4. JavaScript-Kontext des persönlichen Intern-Links - bestätigt, mittlere/niedrige Priorität

`MainActivity.internalSkin()` setzt die vollständige persönliche Basis-URL in `window.__pfvrBaseInternalUrl`. Sie wird für `Aus Initiallink neu aufbauen` verwendet.

Der Link ist der Seite ohnehin als aktuelle URL bekannt; dies ist daher kein neuer Geheimnisverlust gegenüber der eigenen `intern.pfvr.ch`-Seite. Trotzdem wird der Zugriff unnötig verlängert und für jedes gleich-origin-Skript als globale Variable bequem verfügbar.

Ziel beim internen API-/Adapter-Umbau: keine langlebigen persönlichen URLs im JavaScript-Kontext; Recovery durch nativen Callback/Navigation oder serverseitig widerrufbare Gerätezertifikate/Tokens ersetzen.

---

## 5. Externe URL-Schemes - bestätigt, Hardening sinnvoll

`external(String url)` reicht derzeit jede parsebare URI direkt an `ACTION_VIEW` weiter. Die WebView-Regeln halten nur erlaubte HTTPS-Hosts innerhalb der App; alle anderen Ziele werden an `external()` delegiert.

Für die von der App selbst fest hinterlegten Links ist das kontrolliert. Bei einem kompromittierten/ungewöhnlichen externen Link aus Webinhalt ist eine Scheme-Whitelist trotzdem sinnvoll.

Ziel:

- erlauben: `https`, `http`, `mailto`, `tel`, `geo`,
- weitere Schemes nur explizit und fallbezogen,
- `intent:` und unbekannte Schemes standardmässig ablehnen bzw. nach Rückfrage extern öffnen.

---

## 6. IBAN/Preise - organisatorisches Aktualitätsrisiko bestätigt

In `0.11.4` sind Zahlungsempfänger/IBAN im Java-Code und die Preisliste als lokales JSON-Asset gebündelt. Der Swiss-QR wird daraus lokal korrekt aufgebaut. Das Risiko ist nicht die Vertraulichkeit, sondern veraltete Konfiguration auf alten Installationen.

Dafür wurde `decisions/app-config-and-internal-api.md` angelegt. Ziel ist zunächst eine **öffentlich erreichbare, digital signierte Konfiguration** mit lokalem Last-known-good und eingebautem Fallback. IBAN und Preise benötigen keine Inhaltsverschlüsselung, weil sie dem Nutzer angezeigt werden; digitale Signatur + HTTPS schützen stattdessen Integrität und Herkunft.

---

## 7. AccessGate - Einordnung bestätigt

`AccessGate` normalisiert einen 16-stelligen A-Z/0-9-Code und vergleicht einen SHA-256-Digest konstantzeitnah. Der Klartext steht nicht im Repository.

Das ist ausdrücklich nur eine lokale Erstfreigabe, keine serverseitige Authentifizierung. Bei einem zufälligen 16-Zeichen-Code ist ein Offline-Bruteforce trotz ungesalzener Einzelrunde praktisch nicht das relevante Risiko. Ein merkbares Wort oder kurzer Code wäre dagegen ungeeignet.

Geplanter Zweck:

- unbeabsichtigten Vollzugriff auf die App nach öffentlicher Installation vermeiden,
- Mitglieder erhalten den Code über einen Vereinskanal,
- nur die öffentliche Landingpage/Links stehen vorher zur Verfügung,
- neuer Geräte-/App-Datenstand muss erneut freigeschaltet werden (Backups sind deaktiviert),
- der interne Bereich benötigt zusätzlich den persönlichen PFVR-Zugang.

Später kann der Shared Code durch serverseitiges Enrollment pro Gerät ersetzt werden.

---

## 8. CI-/Release-Artefakte - Opus-Angabe teilweise veraltet

Der signierte Play-AAB wird als GitHub-Actions-Artefakt erzeugt. Im **aktuellen** `play-release.yml` ist die Retention auf **7 Tage**, nicht 30 Tage, gesetzt. Das Artefakt enthält keinen Signierschlüssel; die Veröffentlichung des AAB vor dem Play-Upload ist bei einem öffentlichen Repository dennoch unnötige Sichtbarkeit.

Vor Produktion prüfen:

- ob der Repository-/Actions-Zugriff wie erwartet konfiguriert ist,
- ob der AAB überhaupt als Actions-Artefakt aufbewahrt werden muss oder direkt kontrolliert an Play übertragen/sofort gelöscht werden kann,
- SHA256/Signaturbericht getrennt archivieren.

---

## 9. Datenquellen / Attribution

### BAFU

Die verwendete GraphQL-API `https://data.bafu.admin.ch/api` ist die offizielle Datenplattform des BAFU. Umweltdaten sind dort öffentlich als Open Data verfügbar. Livewerte sind Rohdaten; dies soll sichtbar kommuniziert werden.

### Open-Meteo

Die App verwendet den kostenlosen Open-Meteo-Dienst mit MeteoSwiss-ICON-Auswahl und fest hinterlegten Rheinfelden-Koordinaten. Der Free-Tier ist für nichtkommerzielle Nutzung vorgesehen und die Daten stehen unter CC BY 4.0; Attribution ist erforderlich. Die heutige UI nennt `MeteoSwiss ICON via Open-Meteo`, was bereits in die richtige Richtung geht. Für Store-/Info-Dokumente soll Open-Meteo als Daten-/API-Anbieter ausdrücklich genannt werden.

---

## 10. Priorisierte Maßnahmen

### Vor 1.0.0

1. Issue #14 - stale Schifffahrtslage fail-safe + Rohdaten-/Amtshinweis.
2. Issue #15 - automatische Personenmutation entfernen.
3. Vereinsfreigabe/Privacy/Review-Zugang finalisieren.
4. External-Scheme-Allowlist ergänzen.
5. Test-Signing-Risiko in Testerhinweisen dokumentieren.

### Danach / Architektur

6. Signierte Remote-Konfiguration für IBAN/Preise/Links.
7. Authentifizierte interne API mit Geräte-Enrollment und expliziten Writes.
8. Persönliche Basis-URL aus globalem JS-Kontext entfernen, sobald der neue Adapter verfügbar ist.

## Quellen

- Schweizerische Rheinhäfen: https://port-of-switzerland.ch/hafenservice/pegel/
- BAFU Datenqualität: https://www.hydrodaten.admin.ch/de/fragen
- BAFU Datenplattform: https://data.bafu.admin.ch/dataproduct-water-observations
- Open-Meteo Terms: https://open-meteo.com/en/terms
