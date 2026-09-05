# Google Play Vorbereitung

Stand: Android-Testversion `0.11.4` (`versionCode 51`), Paket `ch.pfvr.app`, `targetSdk 36` / `compileSdk 36`, `minSdk 26`.

Die App ist technisch weit genug für die Google-Play-Vorbereitung und einen ersten internen Play-Test. `1.0.0` bleibt für den ersten öffentlichen Produktionsrelease reserviert.

## Veröffentlichungsmodell

Die App wird als **privat entwickeltes Hobbyprojekt über ein persönliches Google-Play-Entwicklerkonto** veröffentlicht. Der Pontonierfahrverein Rheinfelden ist nicht automatisch Herausgeber oder technischer Betreiber der Android-App.

Vor einer öffentlichen Veröffentlichung wird eine schriftliche Vereinsfreigabe eingeholt. Sie soll mindestens die Nutzung von Vereinsname/PFVR-Bezeichnung, Vereinslogo, öffentlichen Vereinsinformationen und -links sowie die Veröffentlichung der App und ihrer Store-Materialien unter dem persönlichen Entwicklerkonto erlauben. Vorlagen liegen in `vereinsfreigabe-anfrage.md` und `vereinsfreigabe-bestaetigung.md`.

Store-Listing, Datenschutzerklärung und Supportangaben müssen dieses Rollenmodell klar abbilden: **privater Herausgeber/Entwickler**, mit Genehmigung und für den Einsatz beim PFVR; keine unbelegte Behauptung, der Verein selbst veröffentliche oder betreibe die App.

## Bereits erfüllt

- Android 16 / API 36 als Target; damit wird die seit 31.08.2026 geltende Ziel-API-Anforderung für neue Apps und Updates erfüllt.
- Produktions-Paketname ist fest als `ch.pfvr.app`; Debug/Test bleibt durch `.test` getrennt.
- Das eigene Manifest fordert nur Internetzugriff an. Die final zusammengeführten APK-Berechtigungen werden in CI exportiert und gegen eine Sperrliste für Standort, Kamera, Mikrofon, Kontakte, Kalender, Telefon/SMS, Medien-/Speicherzugriff, `QUERY_ALL_PACKAGES`, Benachrichtigungen, App-Installation/Overlay und Exact Alarms geprüft.
- Cleartext-Verkehr ist deaktiviert.
- App-Daten mit persönlichem PFVR-Zugang sind von Cloud-Backup und Device-to-Device-Transfer explizit ausgeschlossen.
- `lintRelease` ist Teil der CI; Release-AAB und Debug-APK werden in jeder relevanten CI-Ausführung kompiliert.
- Testsignierung und Produktions-/Upload-Signierung sind getrennt. Der eingecheckte Testschlüssel ist niemals für Google Play zu verwenden.
- `.github/workflows/play-release.yml` erzeugt nach Hinterlegung eines separaten Upload-Keys ein signiertes Release-AAB. Der Job läuft nur manuell von `main` und ist an die GitHub-Environment `play-store` gebunden.
- Store-Text, Datenschutzentwurf, Data-Safety-Arbeitsblatt, Review-Zugang, Asset-Anforderungen, Upload-Key-Anleitung, Vereinsfreigabe-Vorlagen und Play-Console-Checkliste liegen in diesem Ordner.
- Die App ist keine reine WebView-Hülle: Kalender, Wetter, Rheinwerte/Diagramme, Kacheln, Kasse/Swiss-QR, Sprache und große Teile der Vereinsdarstellung sind nativ.

## Externe Release-Blocker

1. **Vereinsfreigabe einholen.** Schriftlich bestätigen lassen, dass die privat entwickelte App unter dem persönlichen Entwicklerkonto veröffentlicht werden darf und Vereinsname/PFVR-Bezeichnung, Logo sowie die vereinbarten öffentlichen Inhalte/Links und Store-Materialien verwendet werden dürfen.
2. **Herausgeber-/Kontaktangaben finalisieren.** Rechtlicher Name und die von Google verlangten Kontaktdaten des persönlichen Entwicklerkontos müssen stimmen. Für Store-Support und Datenschutz möglichst eine passende, dauerhaft erreichbare Projekt-/Entwickleradresse festlegen.
3. **Öffentliche Datenschutzerklärung veröffentlichen.** Google verlangt für jede App eine aktive, öffentlich zugängliche Privacy-Policy-URL und einen Link innerhalb der App. Der Entwurf in `privacy-policy-draft.md` und die statische Vorlage `privacy-policy-web-template.html` sind fachlich vorbereitet, benötigen aber die endgültigen Herausgeber-/Kontaktangaben und eine reale öffentliche URL. Eine Seite auf `pfvr.ch` ist nur mit Vereinsfreigabe zu verwenden; alternativ kann der private Herausgeber eine eigene dauerhaft kontrollierte HTTPS-Seite nutzen.
4. **Separaten Review-Zugang bereitstellen.** Der gemeinsame Erstfreigabecode gehört ausschließlich in die Play Console, nicht ins Repository. Für den internen PFVR-Bereich braucht Google zusätzlich einen dedizierten, nicht personenbezogenen Demo-/Review-Link oder einen entsprechend isolierten Testzugang.
5. **Upload-Key auf einem vertrauenswürdigen privaten/Admin-Gerät erzeugen und offline sichern.** Keystore und Passwörter niemals in Git committen. Anschließend die vier Release-Secrets in der GitHub-Environment `play-store` hinterlegen.
6. **Store-Assets erstellen.** Benötigt werden insbesondere ein 512×512-PNG-App-Icon, eine 1024×500-Feature-Grafik und mindestens zwei geeignete Screenshots. Das derzeitige kleine Launcher-JPEG ist kein ausreichendes Store-Master-Asset. Vereinslogo nur im Umfang der erteilten Freigabe verwenden.
7. **Play Console vollständig ausfüllen:** App-Zugriff, Werbung, Zielgruppe, IARC-Altersfreigabe, Data Safety, Financial Features und sonstige App-Content-Erklärungen.
8. **Persönliches Konto – Produktionszugang:** Falls das persönliche Entwicklerkonto unter die aktuelle Closed-Test-Pflicht fällt, vor Produktion mindestens 12 Tester über 14 aufeinanderfolgende Tage im geschlossenen Test halten und anschließend Produktionszugang beantragen.

Die externen Punkte werden zusätzlich in GitHub-Issue **#4** nachverfolgt.

## Wichtige Policy-Einordnung

- **Zahlungen:** Die Vereinsbeiz betrifft physische Waren/Konsumationen. Google Play Billing ist für den Kauf physischer Waren nicht zu verwenden. Die App verarbeitet keine Bankzugangsdaten und führt selbst keine Banktransaktion aus; sie erzeugt Zahlungsdaten/Swiss-QR und übergibt diese auf Nutzeraktion an externe Banking-/TWINT-Apps. Die Financial-Features-Erklärung ist trotzdem für jede veröffentlichte App auszufüllen; die konkrete Auswahl muss bei Einreichung anhand des dann sichtbaren Formularwortlauts bestätigt werden.
- **Vereinsmeldungen:** Die App wird in der Kategorie `Sport` positioniert und ist keine News-/Magazine-App. Vereinsmeldungen sind nur eine Teilfunktion; Store-Metadaten sollen die App nicht als Nachrichtenprodukt beschreiben.
- **Kinder/Zielgruppe:** Vor Einreichung muss die tatsächliche Zielgruppe festgelegt und mit dem Verein abgestimmt werden. Unter-13-Zielgruppen dürfen nicht ohne Prüfung ein- oder ausgeschlossen werden, weil davon zusätzliche Families-Anforderungen abhängen.
- **App-Zugriff:** Die Erstfreigabe und der interne Bereich müssen in der Play Console als eingeschränkter Zugriff angegeben werden. Reviewer müssen ohne persönliche Vereinszugänge alles Wesentliche prüfen können.
- **Rollen/Markenbezug:** Der Store-Eintrag muss transparent machen, dass die App privat entwickelt/veröffentlicht wird und Vereinsname/Logo mit Genehmigung verwendet werden. `Offizielle App` nur schreiben, wenn der Verein genau diese Bezeichnung schriftlich freigibt.

## GitHub-Secrets für den Release-Workflow

- `ANDROID_UPLOAD_KEYSTORE_B64`
- `ANDROID_UPLOAD_STORE_PASSWORD`
- `ANDROID_UPLOAD_KEY_ALIAS`
- `ANDROID_UPLOAD_KEY_PASSWORD`

Die Secrets gehören in die GitHub-Environment `play-store`; dort sollten Freigaberegeln/Reviewer aktiviert werden.

## Empfohlener Releaseweg

1. Persönliches Play-Entwicklerkonto verifizieren und App `PFVR Rheinfelden` mit Paket `ch.pfvr.app` anlegen.
2. Vereinsfreigabe schriftlich einholen und archivieren.
3. Privacy-/Supportangaben finalisieren und öffentliche Datenschutz-URL bereitstellen.
4. Upload-Key erzeugen, Play App Signing beim ersten AAB-Upload aktivieren.
5. `0.11.4` bzw. einen daraus abgeleiteten `0.11.x`-Stand als **Internal testing** über Google Play verteilen.
6. Store-Eintrag, Data Safety und Review-Zugang mit echten Angaben abschließen.
7. Falls für das persönliche Konto erforderlich: Closed Test mit 12 Testern / 14 Tagen absolvieren und Produktionszugang beantragen.
8. Reale Gerätetests und Rückmeldungen sammeln; keine neue Funktionswelle kurz vor Release.
9. Erst nach abgeschlossenem Store-/Review-Test auf `1.0.0` gehen.

## Arbeitsdateien

- `store-listing-de.md` – Store-Haupteintrag (Deutsch/Schweiz).
- `release-notes-de.md` – Text für den ersten internen Play-Test.
- `privacy-policy-draft.md` – fachlich vorbereiteter Datenschutztext mit offenen Herausgeberdaten.
- `privacy-policy-web-template.html` – veröffentlichbare HTML-Basis nach Ersetzen der rechtlichen Platzhalter.
- `data-safety-draft.md` – technische Datensicherheits-/Datenfluss-Matrix für die Play Console.
- `play-console-checklist.md` – konkrete Console-Schritte und noch offene Entscheidungen.
- `review-access.md` – Review-Zugang ohne persönliche Vereinsdaten.
- `store-assets.md` – erforderliche Grafiken und Screenshotplan.
- `upload-key.md` – sichere Erzeugung/Verwaltung des Upload-Keys.
- `vereinsfreigabe-anfrage.md` – versandfertige Anfrage an den PFVR-Vorstand.
- `vereinsfreigabe-bestaetigung.md` – kurze schriftliche Freigabevorlage für die Vereinsablage.

## Offizielle Referenzen

- Target API: https://support.google.com/googleplay/android-developer/answer/11926878
- Entwicklerkonto / veröffentlichte Kontaktdaten: https://support.google.com/googleplay/android-developer/answer/13628312
- App-Inhalte / Review: https://support.google.com/googleplay/android-developer/answer/9859455
- User Data / Privacy Policy: https://support.google.com/googleplay/android-developer/answer/10144311
- Data Safety: https://support.google.com/googleplay/android-developer/answer/10787469
- Financial Features: https://support.google.com/googleplay/android-developer/answer/13849271
- Zahlungen / physische Waren: https://support.google.com/googleplay/android-developer/answer/9858738
- Store-Assets: https://support.google.com/googleplay/android-developer/answer/9866151
- Closed Test für neue persönliche Konten: https://support.google.com/googleplay/android-developer/answer/14151465
