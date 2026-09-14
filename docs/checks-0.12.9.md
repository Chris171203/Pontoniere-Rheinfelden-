# Prüfung 0.12.9 — 2026-09-14

Android 0.12.9 / Code 66 / `ch.pfvr.app.test`.

## Ergebnis

[CI 34816581029, Lauf 191](https://github.com/Chris171203/Pontoniere-Rheinfelden-/actions/runs/34816581029) erfolgreich. 119 Tests in 31 Klassen, keine Fehler oder übersprungenen Tests. Dazu vier erfolgreiche Browser-Szenarien für die verzögerten Website-Farbwechsel (Deutsch/Schweizerdeutsch, Hell/Dunkel). Release-Lint, Debug-APK, unsigned Release-AAB sowie Paket-, Testzertifikat- und Berechtigungsprüfung bestanden.

Quellcommit: `2c5435cf7c0dc4e07b47bdf4bd7c54e6d0cce43b`.
Getesteter PR-Merge / APK: `3657ebf43fe849085e0f60da2929586fa58f5bef`.
Identischer Quellbaum beider Commits: `fb917d4f5ca14cc7b3b71a5da283d5a4895073e5`.
Danach wurden nur STATUS.md und dieser Prüfbericht ergänzt.

| Bereich | Prüfnachweis |
|---|---|
| Quellübernahme | Reale WordPress-Antworten für Verein, Jungpontoniere, Vorstand, Geschichte und Kontakt erfolgreich durch den aktuellen Parser verarbeitet. Vier Vereinsgruppen, sieben Vorstands-Karten; Quellenänderungsdaten bleiben erhalten. Keine öffentliche Personenliste als Testfixture eingecheckt. |
| Training / Boote / Sport | Sommer/Winter samt jeweiligem Treffpunkt getrennt; Kontaktlink erhalten. Die beiden Bootsbeschreibungen enthalten keine nachfolgenden Manöver. Manöverbilder werden korrekt zugeordnet und auf eines je Abschnitt begrenzt. |
| Nachwuchs / Geschichte | Alte Altersregeln aus den ausgewählten Nachwuchs-Ausschnitten herausgehalten, im datierten Quelltext erhalten. Keine erfundenen historischen Stationen; doppelte PDF-Links zusammengeführt. JP-Kontakt aus der Vorstandsquelle, E-Mail-Ziel und externes PDF per tatsächlich ausgelöstem Intent geprüft. |
| Rückfall | Geänderte Überschriften ergeben weiterhin lesbaren bereinigten Quelltext. Unbekannte Saisonformulierungen behalten den gesamten Trainingsabsatz. Unvollständige Änderungsdaten führen nicht zum Verlust des Inhalts. |
| Bilder | Quelle, erlaubter Upload-Pfad, vorhandene mittlere srcset-Grösse und Original-Bildunterschrift geprüft. Vereinsbild, Weidling, Fahrtechnikbild, Nachwuchsbild und ein Vorstands-Porträt live mit HTTP 200 abgerufen (7’550 bis 327’636 Byte). |
| Offline / Cache | Verkleinertes Bild nach Loader-Neustart wiederverwendet; bei Netzfehler und ungültigem Download alter Stand erhalten. Fremde Bild-URLs starten keinen Abruf. Cache-Leeren während eines Downloads verhindert nachträgliches Wiederbefüllen. |
| Native Bildanzeige | Regressionstest für Bilder beim anfänglichen Anfügen und beim nachträglichen Aufklappen eines bereits angefügten Abschnitts; beide erzeugen ein natives Bild. |
| Layout / Sprache | Native Trainingsansicht mit synthetischer Quelle bei 320 dp / 150 Prozent Schrift in beiden Themen und Sprachen. Textgrenzen ohne Abschneiden geprüft, Aufklappzustand nach Neuaufbau erhalten, Kartenaktion geprüft. Vier Screenshots angesehen; Thema und Sprache werden im Test vor Activity-Start gesetzt und explizit geprüft. Originaltext bleibt sprachlich unverändert. |
| Bestehende Funktionen | Bisherige Tests zu Erstfreigabe, Kachelmigration, Kontakt-/Social-Footer, erstem Live-Refresh, Zahlungen, Kalender, Wetter und An-/Abmeldung weiterhin erfolgreich. |

Bei der Umsetzung wurde ein Fehler beim späten Einblenden von Bildern behoben. Die Textmessung berücksichtigt nun sichtbare Zeilenbreite ohne nachlaufende Leerzeichen; der Screenshot-Test setzt das Thema vor onCreate, um tatsächlich die gewünschte Variante aufzunehmen. Die native Textansicht entfernt zusätzliche abschliessende HTML-Absatzumbrüche.

## Testpaket

`PFVR-Rheinfelden-test-0.12.9-debug.apk`, 3’553’028 Byte.

SHA-256: `73e81da466c03e0a1cece040650fbd1a6ea213fabfa5f77ca9a5e5feed759142`.

Testzertifikat SHA-256: `0521e6bc43e2868177609dea69d074ed14bb594bad28adee98293e586d3d46bf`.

Nach Download gegen CI-Hash, intakten APK-ZIP-Inhalt und vorhandenes Testzertifikat geprüft. APK und detaillierte Prüfberichte liegen als CI-Artefakte vor; keine Binärdateien im Repository.

## Grenzen

Keine physische Android-Geräteprüfung. Screenshots verwenden synthetische Textquellen; Bildanzeige und echter Quellenabruf wurden separat geprüft. Keine realen Anrufe, E-Mails, Navigationsfahrten oder produktiven persönlichen An-/Abmeldungen ausgelöst. WordPress-Änderungsdaten bestätigen nicht die heutige Gültigkeit älterer Trainings- oder Regelangaben. iOS bleibt im separaten Entwurf #32.
