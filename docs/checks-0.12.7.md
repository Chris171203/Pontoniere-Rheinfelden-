# Prüfung 0.12.7 — 2026-09-14

## Geprüfter Stand

- Android 0.12.7 / Code 64 / `ch.pfvr.app.test`.
- Quellcommit `4154c1e92fcd1e193ff34b91d6ca77c8b21c7e1a`.
- Tatsächlicher APK-Build vom PR-Merge `5df4945b49a335e1850288a49e71a2c75f89d47a`.
- Beide haben Quellbaum `48baefeb65260a52d864e5f54d7fc3b5bbc844e7`; nach Download mit lokalem Baum und GitHub-Gitobjekten verglichen.
- [Erfolgreicher CI-Lauf 34811629510](https://github.com/Chris171203/Pontoniere-Rheinfelden-/actions/runs/34811629510).
- Nach diesem Stand nur Abschlussdokumentation in STATUS.md und diesem Bericht.

## Ergebnisse

| Prüfung | Ergebnis |
|---|---|
| JUnit, einschliesslich nativer UI-Tests | 106 Tests in 28 Klassen; 0 Fehler, 0 Skips; heruntergeladene XML-Ergebnisse ausgewertet |
| Native Vereinsansicht | Quelltext aus Cache bleibt in Schweizerdeutsch unverändert; kein WebView; Logo-Fusszeile zentriert |
| Native Detailansicht | Gespeicherter Quellinhalt und altes Abrufdatum bleiben bei Abruffehler sichtbar |
| Native Home-Ansicht | Genau ein Refresh am ersten sichtbaren Live-Block; Standardlayout und zwei Layouts mit ausgeblendeten Wetter-/Rheinkacheln |
| Browser mit tatsächlich aus Java exportiertem Skin | Vier Kombinationen Deutsch/Schweizerdeutsch und hell/dunkel erfolgreich |
| Originale Website-Controls | Identität, Werte, delegierte Ereignisse und Klassen-/Inline-Farbänderungen erhalten; Antworten erst nach 650/700 ms; kein Reload; alte Pseudobeschriftung nach Textwechsel entfernt |
| Visuelle Prüfung | Originalaufnahmen der synthetischen Anmeldematrix in Hell/Dunkel angesehen |
| Öffentliche Quellen | Verein, Vorstand, Geschichte und Kontakt live über WordPress abgerufen und mit ClubPageRepository verarbeitet; keine personenbezogenen Quellkopien eingecheckt |
| Parser | Überschriften, Text und Kontaktlinks erhalten; aktive Inhalte/Formulare entfernt; ungültige/leere/falsche Antwort abgelehnt |
| Android-Build | `testDebugUnitTest lintRelease assembleDebug bundleRelease` erfolgreich |
| Paket, Signatur, Berechtigungen | CI erfolgreich; APK-Hash und Testzertifikat nach Download mit Manifest/Repository verglichen; ZIP CRC fehlerfrei |

## Testdatei

`PFVR-Rheinfelden-test-0.12.7-debug.apk`, 3’533’022 Byte.

SHA-256: `b0f1f01cac5efbc5b6b34d96f7c980a7a324d3c84f9fe4d69925d274ca6665ce`.

Testzertifikat SHA-256: `0521e6bc43e2868177609dea69d074ed14bb594bad28adee98293e586d3d46bf`.

## Befund und Grenzen

Die App leitete Farben aus dem Aktionstext ab und setzte sie inline mit `!important`. Damit konnte sie Zustandsfarben der originalen Website überdecken. Jetzt steuert die Website die echten Controls; die App passt das Layout und bei Bedarf die rein visuelle Beschriftung an. Die Browserprüfung verwendet ausschliesslich synthetische Personen und lokale Antworten. Es wurden keine produktiven An-/Abmeldungen ausgeführt.

Die nativen UI-Tests laufen mit Robolectric/API 28. Ein physisches Android-Gerät, ein Android-WebView mit persönlichem Vereinszugang und externe Kontaktformular-/PDF-Apps waren nicht Teil der Abnahme. Die verwendeten öffentlichen Homepage-Texte bleiben unverändert, einschliesslich darin genannter älterer Daten; das angezeigte Datum ist der erfolgreiche App-Abrufzeitpunkt.

Der separate iOS-Entwurf #32 basiert weiterhin auf 0.12.6. Bei seiner nächsten Synchronisierung müssen die Skin-Änderung und die neuen nativen Vereins-/Refresh-/Sprachfunktionen übernommen und mit den dortigen iOS-Gates erneut geprüft werden.

## Korrigierte Prüfhürden

- Eine neue Fixture-Hilfsmethode brauchte unter Android eine deklarierte JSONException/Exception; die lokale JVM-JSON-Bibliothek hatte dies nicht verlangt.
- Zwei vorhandene Quelltexttests erwarteten noch die alte Refresh-Aufrufstelle bzw. das bisher doppeldeutige Morgen-Label. Erwartungen auf die angeforderte neue Darstellung umgestellt; zusätzliche native Tests prüfen die tatsächliche View-Struktur.
