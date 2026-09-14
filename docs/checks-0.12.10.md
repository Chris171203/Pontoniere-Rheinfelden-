# Prüfung 0.12.10 — 2026-09-14

Android 0.12.10 / Code 67 / `ch.pfvr.app.test`.

## Ergebnis

[CI 34822553841, Lauf 196](https://github.com/Chris171203/Pontoniere-Rheinfelden-/actions/runs/34822553841) erfolgreich. 128 Tests in 32 Klassen ohne Fehler oder übersprungene Tests. Vier Browser-Szenarien für verzögerte Website-Farbwechsel in Deutsch/Schweizerdeutsch und Hell/Dunkel ebenfalls erfolgreich. Release-Lint, Debug-APK, unsigned Release-AAB sowie Paket-, Testzertifikat- und Berechtigungsprüfung bestanden.

Quellcommit: `491523ddc5f59d221c3e50cc51915e5ff1e86ec4`.
Getesteter PR-Merge / APK: `a01acf8f19b4b8f64021b1df1df3aa85406b539f`.
Identischer Quellbaum beider Commits: `197490f401d8b771c7d73c0d11af81431830de24`.
Danach wurden nur STATUS.md und dieser Prüfbericht ergänzt.

| Bereich | Prüfnachweis |
|---|---|
| Vereinsübersicht | Kurzer Quelleneinstieg, Sommer-/Winterzeiten, Treffpunkte und Kalender direkt sichtbar; kein Aufklappelement auf der Übersicht. Alte Mitgliederzahlen erscheinen nicht im Einstieg. |
| Themen-Unterseiten | Boote und Fahrtechnik mit vollständig sichtbaren Fachabschnitten; nur der vollständige Originaltext lässt sich zusätzlich öffnen. Ein Tap auf die Navigationszeile öffnet die native Sportseite. |
| Navigation | Rückkehr von einer verlinkten Unterseite zur vorherigen Fachseite und danach zur Übersicht erhält jeweils die Scrollposition. Karten-, E-Mail- und PDF-Aktionen per Ziel-Intent geprüft. |
| Training / Quelle | Zeiten, Saison und Wochentage werden aus dem vorhandenen Quelltext gelesen. Eine geänderte Testzeit verändert die Übersicht; unvollständige Angaben behalten den Quellsatz ohne erfundene Uhrzeit. Neues Überschriftenschema und gescheiterter Refresh lassen den Quelltext direkt lesbar. |
| Tatsächlicher Quelltext | Der neue Präsentationsparser wurde zusätzlich mit der bereits in diesem Arbeitsgang gespeicherten öffentlichen Vereinsseite ausgeführt: Sommer April–September, Montag-/Mittwochabend 18:30–20:00 am Depot; Winter Oktober–März, Donnerstag 19:30 bei der Schützenturnhalle. Kein erneuter Live-Abruf für diesen UX-Schritt; die Angaben werden als Homepage-Stand mit Kalenderverweis angezeigt. |
| Sprache / Darstellung | Übersicht und Sportseite bei 320 dp und 150 Prozent Schrift in Deutsch/Schweizerdeutsch sowie Hell/Dunkel. Textgrenzen ohne Abschneiden geprüft und alle acht nativen Screenshots angesehen. App-Beschriftungen und erzeugte Wochentage lokalisiert; Originaltexte unverändert. |
| Pixeldichte | Zusätzlicher nativer Test bei xhdpi / Faktor 2 prüft Abstände und Textpolster in tatsächlichen Gerätepixeln. Doppelte dp-Umrechnung der Vereinsabstände korrigiert. |
| Layoutmigration | Frühere Standardanordnung erhält kompakte Navigationszeilen; eigene Reihenfolgen bleiben erhalten. Das Jahresprogramm steht fest beim Training, die alte Programmkachel entfällt. |
| Regression | Bestehende Tests für Quellen-/Bildcache, Kontakt-/Social-Footer, ersten Live-Refresh, Erstfreigabe, Zahlungen, Kalender, Wetter und An-/Abmeldung weiterhin erfolgreich. |

## Testpaket

`PFVR-Rheinfelden-test-0.12.10-debug.apk`, 3’558’524 Byte.

SHA-256: `ce596c7111c211587983a0e4add41c4354e8dcfb6854304b9c7a1add0746d43f`.

Testzertifikat SHA-256: `0521e6bc43e2868177609dea69d074ed14bb594bad28adee98293e586d3d46bf`.

Nach Download gegen CI-Hash, intakten APK-ZIP-Inhalt, Build-Metadaten und vorhandenes Testzertifikat geprüft. APK und detaillierte Prüfberichte liegen als CI-Artefakte vor; keine Binärdateien im Repository.

## Grenzen

Keine physische Android-Geräteprüfung. Die acht Layoutaufnahmen verwenden synthetische Texte und das vorhandene Vereinslogo als Bild-Fallback; echte Quellbilder werden durch die bereits vorhandenen separaten Bildtests abgedeckt. Keine realen Anrufe, E-Mails, Navigationsfahrten oder produktiven persönlichen An-/Abmeldungen ausgelöst. iOS bleibt im separaten Entwurf #32.
