# Play Console Checkliste – PFVR Rheinfelden

Arbeitsstand für `ch.pfvr.app`. Veröffentlichungsmodell: **privat entwickeltes Hobbyprojekt über ein persönliches Google-Play-Entwicklerkonto, mit schriftlicher Genehmigung des Pontonierfahrvereins Rheinfelden für Vereinsname/Logo/Inhalte und Veröffentlichung.**

## 1. Entwicklerkonto / Herausgeber / Vereinsfreigabe

- `[x]` Veröffentlichungsmodell festgelegt: persönliches Google-Play-Entwicklerkonto.
- `[ ]` Persönliches Entwicklerkonto vollständig verifizieren; rechtlicher Name und von Google verlangte Kontaktdaten aktuell halten.
- `[ ]` Öffentlichen Entwicklernamen festlegen. Empfehlung nach Vereinsfreigabe: neutraler Entwicklername oder persönliche Entwicklerbezeichnung; nicht so formulieren, als wäre das Konto selbst der Verein.
- `[ ]` Dauerhaft erreichbare Entwickler-/Support-E-Mail festlegen und verifizieren.
- `[ ]` Vereinsfreigabe gemäß `vereinsfreigabe-anfrage.md` einholen.
- `[ ]` Vom Verein bestätigte Freigabe gemäß `vereinsfreigabe-bestaetigung.md` archivieren.
- `[ ]` Freigabe deckt mindestens ab: `PFVR Rheinfelden`, ausgeschriebenen Vereinsnamen, Vereinslogo, öffentliche Vereinsinformationen/Links, Store-Screenshots/-Grafiken sowie Veröffentlichung und Updates über das persönliche Entwicklerkonto.
- `[ ]` Nur dann Formulierungen wie `offizielle App` verwenden, wenn der Verein genau diese Außendarstellung ausdrücklich freigibt. Standardformulierung: `Privat entwickelt mit Genehmigung des Pontonierfahrvereins Rheinfelden`.

Google: https://support.google.com/googleplay/android-developer/answer/13628312

## 2. Neue App in Play Console anlegen

- `[ ]` Standard-Sprache: Deutsch; wenn die Console eine Schweizer Variante anbietet und sie zum Listing passt, Deutsch (Schweiz) verwenden.
- `[ ]` App-Name: `PFVR Rheinfelden`.
- `[ ]` Paketname: **`ch.pfvr.app`**.
- `[ ]` Paketname-Verfügbarkeit prüfen und danach nicht mehr ändern.
- `[ ]` Typ: **App**.
- `[ ]` Preis: **Kostenlos**.
- `[ ]` Play App Signing akzeptieren/aktivieren.
- `[ ]` erforderliche Export-/Programmerklärungen wahrheitsgemäß bestätigen.
- `[ ]` Kategorie später im Store-Eintrag: `Sport`.

## 3. App-Inhalte / Policy

### Datenschutz

- `[ ]` Privaten Herausgeber/Verantwortlichen und Datenschutzkontakt in `privacy-policy-draft.md` final einsetzen.
- `[ ]` Rolle klar beschreiben: App-Herausgeber ist der private Entwickler; PFVR betreibt weiterhin seine eigenen Webseiten/internen Systeme.
- `[ ]` Erklärung als normale öffentliche HTTPS-Webseite veröffentlichen.
- `[ ]` Bei Veröffentlichung auf `pfvr.ch` muss die Vereinsfreigabe dies abdecken; alternativ eigene dauerhaft kontrollierte HTTPS-Seite verwenden.
- `[ ]` URL ohne Login/Geoblocking erreichbar.
- `[ ]` gleiche Erklärung oder Link innerhalb der Produktions-App zugänglich machen, bevor der Store-Release eingereicht wird.

Google: https://support.google.com/googleplay/android-developer/answer/10144311

### Werbung

- `[ ]` `Enthält Werbung?` → **Nein**, solange kein Werbe-SDK / keine Anzeigen eingebaut werden.

### App-Zugriff

- `[ ]` App als **teilweise eingeschränkt** deklarieren.
- `[ ]` Gemeinsamen Erstfreigabecode ausschließlich in der Play Console hinterlegen.
- `[ ]` Separaten PFVR-Review-/Demo-Link bereitstellen, der keine persönlichen Mitgliedsdaten enthält.
- `[ ]` Review-Anleitung aus `review-access.md` in die Console übertragen.
- `[ ]` Review-Zugang vor jeder Einreichung selbst auf einem frischen Gerät testen.

Google: https://support.google.com/googleplay/android-developer/answer/9859455

### Zielgruppe

- `[ ]` Mit dem Verein tatsächliche vorgesehene Zielgruppe/Mindestalter abstimmen.
- `[ ]` Zielaltersgruppen wahrheitsgemäß auswählen.
- `[ ]` Falls Kinder unter 13 zur Zielgruppe gehören, Families-Policy separat vollständig prüfen; nicht ohne fachliche Entscheidung aktivieren.

Google: https://support.google.com/googleplay/android-developer/answer/9867159

### Inhaltsklassifizierung (IARC)

Vorläufige Erwartung: Sport-/Vereins-App ohne Gewalt, Glücksspiel, sexuelle Inhalte, Drogen, nutzergenerierte öffentliche Kommunikation oder Werbung. Der Fragebogen ist trotzdem vollständig und wahrheitsgemäß auszufüllen.

- `[ ]` IARC-Kontakt-E-Mail festlegen.
- `[ ]` Fragebogen abschließen und Zertifikat prüfen.

Google: https://support.google.com/googleplay/android-developer/answer/9859655

### Financial Features

Die Erklärung ist für veröffentlichte Apps auszufüllen, auch wenn keine Finanzdienstleistung angeboten wird.

PFVR-Sachverhalt:

- Vereinsbeiz = physische Konsumationen.
- App führt keine Banktransaktion aus.
- App liest/speichert keine Bankzugangsdaten oder Karteninformationen des Nutzers.
- App erzeugt Händler-Swiss-QR und startet/teilt auf Nutzeraktion mit einer externen Banking-/TWINT-App.

- `[ ]` Aktuellen Formularwortlaut in der Console prüfen.
- `[ ]` Nicht als `Banking` deklarieren.
- `[ ]` Entscheiden, ob der reine nutzerinitiierte externe Zahlungshandoff als `Mobile payments and digital wallets` verlangt wird; Googles öffentliche Hilfeseite definiert nur Kategorien, nicht jeden Merchant-Handoff-Fall eindeutig.
- `[ ]` Falls unsicher: vor Produktion Play-Support mit genau obigem Sachverhalt schriftlich fragen und Antwort dokumentieren.

Google Financial Features: https://support.google.com/googleplay/android-developer/answer/13849271

Google Payments / physische Waren: https://support.google.com/googleplay/android-developer/answer/9858738

### Data Safety

- `[ ]` `data-safety-draft.md` mit dem finalen Review-/Demo-Zugang validieren.
- `[ ]` Interner PFVR-Datenfluss als optionalen personenbezogenen Funktionsbereich korrekt abbilden.
- `[ ]` Installierte Banking-Apps nur als lokale Verarbeitung behandeln, solange kein Upload stattfindet.
- `[ ]` Keine Standorterhebung angeben; Wetter nutzt feste Koordinaten.
- `[ ]` Keine Ads/Analytics/Crash-SDK-Erhebung angeben, solange dies technisch unverändert bleibt.
- `[ ]` Privacy Policy und Data Safety auf Widersprüche prüfen.

Google: https://support.google.com/googleplay/android-developer/answer/10787469

### News & Magazine

Die App ist primär eine Sport-/Vereins-App. Vereinsmeldungen sind nur eine Teilfunktion. Nicht in `News & Magazines` kategorisieren und die Store-Metadaten nicht als Nachrichtenprodukt formulieren.

Google: https://support.google.com/googleplay/android-developer/answer/10523915

## 4. Store-Haupteintrag

- `[ ]` Text aus `store-listing-de.md` übernehmen.
- `[ ]` Transparenzsatz nach Freigabe verwenden: `Privat entwickelt mit Genehmigung des Pontonierfahrvereins Rheinfelden.`
- `[ ]` Website `https://www.pfvr.ch/` nur als Vereinswebsite angeben, sofern der Verein dies für den Store-Eintrag freigibt; ansonsten geeignete Projekt-/Supportseite verwenden.
- `[ ]` Support-E-Mail des privaten Herausgebers final einsetzen; `info@pfvr.ch` nur mit ausdrücklicher Zustimmung als Supportadresse verwenden.
- `[ ]` Datenschutz-URL eintragen.
- `[ ]` 512×512 Store-Icon hochladen.
- `[ ]` 1024×500 Feature Graphic hochladen.
- `[ ]` mindestens zwei Phone-Screenshots; empfohlen sechs gemäß `store-assets.md`.
- `[ ]` Keine echten Intern-Tokens oder unnötigen Teilnehmerdaten auf Screenshots.
- `[ ]` Vereinslogo/Name nur im schriftlich genehmigten Umfang verwenden.

Google: https://support.google.com/googleplay/android-developer/answer/9866151

## 5. Signierung / AAB

- `[ ]` Upload-Key lokal auf einem vertrauenswürdigen privaten/Admin-Gerät gemäß `upload-key.md` erzeugen.
- `[ ]` Original-Keystore + Zugangsdaten mindestens zweimal getrennt offline sichern.
- `[ ]` GitHub-Secrets setzen.
- `[ ]` GitHub-Environment `play-store` mit erforderlichen Schutzregeln anlegen.
- `[ ]` `.github/workflows/play-release.yml` auf `main` manuell starten.
- `[ ]` AAB-SHA256 und Signaturbericht archivieren.
- `[ ]` Beim ersten Upload **Play App Signing** aktivieren.

## 6. Testtracks beim persönlichen Entwicklerkonto

Zuerst `Internal testing` mit einer `0.11.x`-Version.

- `[ ]` Kleine Testgruppe im Verein definieren.
- `[ ]` Play-Install statt Sideload prüfen.
- `[ ]` Update über Play testen.
- `[ ]` Erststart/Landingpage + Freigabecode testen.
- `[ ]` Intern-Link einrichten und Matrix testen.
- `[ ]` Banking-Handoff auf mehreren Geräten/Banking-Apps testen.
- `[ ]` Datenschutzlink aus der Produktions-App testen.

Falls das persönliche Entwicklerkonto unter Googles Closed-Test-Pflicht für neue persönliche Konten fällt:

- `[ ]` Closed-Test-Track einrichten.
- `[ ]` mindestens **12 Tester** dauerhaft angemeldet halten.
- `[ ]` **14 aufeinanderfolgende Tage** vollständig absolvieren.
- `[ ]` Testerfeedback und reale Nutzung dokumentieren.
- `[ ]` anschließend Produktionszugang in der Play Console beantragen.

Google: https://support.google.com/googleplay/android-developer/answer/14151465

## 7. Vor 1.0.0

- `[ ]` Vereinsfreigabe schriftlich archiviert.
- `[ ]` Alle Gerätetests aus `Android/README.md` abgearbeitet.
- `[ ]` Keine bekannten kritischen Fehler.
- `[ ]` Review-Zugang funktioniert.
- `[ ]` Privacy/Data Safety final.
- `[ ]` Store-Assets final und vom Verein hinsichtlich Logo/Name freigegeben.
- `[ ]` Persönlicher Herausgeber/Support und Vereinsrolle im Store widerspruchsfrei dargestellt.
- `[ ]` ggf. Closed-Test-/Produktionszugang abgeschlossen.
- `[ ]` Version erst dann auf `1.0.0` setzen.
