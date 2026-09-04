# Google Play Vorbereitung

Stand: Android-Testversion `0.11.4` (`versionCode 51`), Paket `ch.pfvr.app`, `targetSdk 36` / `compileSdk 36`, `minSdk 26`.

Die App ist technisch weit genug für die Google-Play-Vorbereitung und einen ersten internen Play-Test. `1.0.0` bleibt für den ersten öffentlichen Produktionsrelease reserviert.

## Bereits erfüllt

- Android 16 / API 36 als Target; damit wird die seit 31.08.2026 geltende Ziel-API-Anforderung für neue Apps und Updates erfüllt.
- Produktions-Paketname ist fest als `ch.pfvr.app`; Debug/Test bleibt durch `.test` getrennt.
- Das eigene Manifest fordert nur Internetzugriff an. Die final zusammengeführten APK-Berechtigungen werden in CI exportiert und gegen eine Sperrliste für Standort, Kamera, Mikrofon, Kontakte, Kalender, Telefon/SMS, Medien-/Speicherzugriff, `QUERY_ALL_PACKAGES`, Benachrichtigungen, App-Installation/Overlay und Exact Alarms geprüft.
- Cleartext-Verkehr ist deaktiviert.
- App-Daten mit persönlichem PFVR-Zugang sind von Cloud-Backup und Device-to-Device-Transfer explizit ausgeschlossen.
- `lintRelease` ist Teil der CI; Release-AAB und Debug-APK werden in jeder relevanten CI-Ausführung kompiliert.
- Testsignierung und Produktions-/Upload-Signierung sind getrennt. Der eingecheckte Testschlüssel ist niemals für Google Play zu verwenden.
- `.github/workflows/play-release.yml` erzeugt nach Hinterlegung eines separaten Upload-Keys ein signiertes Release-AAB. Der Job läuft nur manuell von `main` und ist an die GitHub-Environment `play-store` gebunden.
- Store-Text, Datenschutzentwurf, Data-Safety-Arbeitsblatt, Review-Zugang, Asset-Anforderungen, Upload-Key-Anleitung und Play-Console-Checkliste liegen in diesem Ordner.
- Die App ist keine reine WebView-Hülle: Kalender, Wetter, Rheinwerte/Diagramme, Kacheln, Kasse/Swiss-QR, Sprache und große Teile der Vereinsdarstellung sind nativ.

## Externe Release-Blocker

1. **Herausgeber festlegen.** Für eine offizielle Vereins-App ist ein Organisationskonto des Pontonierfahrvereins Rheinfelden vorzuziehen. Google verlangt für Organisationskonten grundsätzlich eine D-U-N-S-Nummer sowie verifizierbare Organisations- und Kontaktdaten. Falls noch keine D-U-N-S-Nummer existiert, früh beantragen; Google weist auf eine mögliche Bearbeitungszeit von bis zu 30 Tagen hin.
2. **Öffentliche Datenschutzerklärung veröffentlichen.** Google verlangt für jede App eine aktive, öffentlich zugängliche Privacy-Policy-URL und einen Link innerhalb der App. Der Entwurf in `privacy-policy-draft.md` und die statische Vorlage `privacy-policy-web-template.html` sind fachlich vorbereitet, benötigen aber den endgültigen Herausgeber/Datenschutzkontakt und eine reale öffentliche URL, vorzugsweise auf `pfvr.ch`.
3. **Separaten Review-Zugang bereitstellen.** Der gemeinsame Erstfreigabecode gehört ausschließlich in die Play Console, nicht ins Repository. Für den internen PFVR-Bereich braucht Google zusätzlich einen dedizierten, nicht personenbezogenen Demo-/Review-Link oder einen entsprechend isolierten Testzugang.
4. **Upload-Key auf einem vertrauenswürdigen Vereins-/Admin-Gerät erzeugen und offline sichern.** Keystore und Passwörter niemals in Git committen. Anschließend die vier Release-Secrets in der GitHub-Environment `play-store` hinterlegen.
5. **Store-Assets erstellen.** Benötigt werden insbesondere ein 512×512-PNG-App-Icon, eine 1024×500-Feature-Grafik und mindestens zwei geeignete Screenshots. Das derzeitige kleine Launcher-JPEG ist kein ausreichendes Store-Master-Asset.
6. **Play Console vollständig ausfüllen:** App-Zugriff, Werbung, Zielgruppe, IARC-Altersfreigabe, Data Safety, Financial Features und sonstige App-Content-Erklärungen.

Die externen Punkte werden zusätzlich in GitHub-Issue **#4** nachverfolgt.

## Wichtige Policy-Einordnung

- **Zahlungen:** Die Vereinsbeiz betrifft physische Waren/Konsumationen. Google Play Billing ist für den Kauf physischer Waren nicht zu verwenden. Die App verarbeitet keine Bankzugangsdaten und führt selbst keine Banktransaktion aus; sie erzeugt Zahlungsdaten/Swiss-QR und übergibt diese auf Nutzeraktion an externe Banking-/TWINT-Apps. Die Financial-Features-Erklärung ist trotzdem für jede veröffentlichte App auszufüllen; die konkrete Auswahl muss bei Einreichung anhand des dann sichtbaren Formularwortlauts bestätigt werden.
- **Vereinsmeldungen:** Die App wird in der Kategorie `Sport` positioniert und ist keine News-/Magazine-App. Vereinsmeldungen sind nur eine Teilfunktion; Store-Metadaten sollen die App nicht als Nachrichtenprodukt beschreiben.
- **Kinder/Zielgruppe:** Vor Einreichung muss der Verein das tatsächlich vorgesehene Mindestalter bestätigen. Unter-13-Zielgruppen dürfen nicht einfach aus Bequemlichkeit ausgeschlossen oder eingeschlossen werden, weil davon zusätzliche Families-Anforderungen abhängen.
- **App-Zugriff:** Die Erstfreigabe und der interne Bereich müssen in der Play Console als eingeschränkter Zugriff angegeben werden. Reviewer müssen ohne persönliche Vereinszugänge alles Wesentliche prüfen können.

## GitHub-Secrets für den Release-Workflow

- `ANDROID_UPLOAD_KEYSTORE_B64`
- `ANDROID_UPLOAD_STORE_PASSWORD`
- `ANDROID_UPLOAD_KEY_ALIAS`
- `ANDROID_UPLOAD_KEY_PASSWORD`

Die Secrets gehören in die GitHub-Environment `play-store`; dort sollten Freigaberegeln/Reviewer aktiviert werden.

## Empfohlener Releaseweg

1. Organisationskonto/D-U-N-S und Privacy-URL klären.
2. Upload-Key erzeugen, Play App Signing beim ersten AAB-Upload aktivieren.
3. `0.11.4` bzw. einen daraus abgeleiteten `0.11.x`-Stand als **Internal testing** über Google Play verteilen.
4. Store-Eintrag, Data Safety und Review-Zugang mit echten Angaben abschließen.
5. Reale Gerätetests und Rückmeldungen sammeln; keine neue Funktionswelle kurz vor Release.
6. Erst nach abgeschlossenem Store-/Review-Test auf `1.0.0` gehen.

Falls stattdessen ein neues persönliches Entwicklerkonto verwendet wird, gilt für Produktionszugriff derzeit ein geschlossener Test mit mindestens 12 dauerhaft angemeldeten Testern über 14 aufeinanderfolgende Tage. Diese Hürde gilt nicht in gleicher Form für ein Organisationskonto.

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

## Offizielle Referenzen

- Target API: https://support.google.com/googleplay/android-developer/answer/11926878
- Organisationskonto / D-U-N-S: https://support.google.com/googleplay/android-developer/answer/13628312
- App-Inhalte / Review: https://support.google.com/googleplay/android-developer/answer/9859455
- User Data / Privacy Policy: https://support.google.com/googleplay/android-developer/answer/10144311
- Data Safety: https://support.google.com/googleplay/android-developer/answer/10787469
- Financial Features: https://support.google.com/googleplay/android-developer/answer/13849271
- Zahlungen / physische Waren: https://support.google.com/googleplay/android-developer/answer/9858738
- Store-Assets: https://support.google.com/googleplay/android-developer/answer/9866151
