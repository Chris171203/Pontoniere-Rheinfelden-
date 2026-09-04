# Play Console Checkliste – PFVR Rheinfelden

Arbeitsstand für `ch.pfvr.app`. Die Reihenfolge ist so gewählt, dass externe Blocker früh auffallen.

## 1. Entwicklerkonto / Herausgeber

- `[ ]` Entscheiden: Organisationskonto des Pontonierfahrvereins Rheinfelden.
- `[ ]` Prüfen, ob der Verein bereits eine D-U-N-S-Nummer besitzt.
- `[ ]` Falls nein: D-U-N-S beantragen. Google weist auf bis zu 30 Tage Bearbeitungszeit hin.
- `[ ]` Organisationsname, Anschrift, Telefonnummer, Website und Kontaktperson exakt mit den offiziellen Vereinsdaten abgleichen.
- `[ ]` Öffentliche Entwickler-E-Mail und -Telefonnummer festlegen und verifizieren.
- `[ ]` Entwicklername festlegen, empfohlen: `Pontonierfahrverein Rheinfelden` oder eine vom Verein freigegebene Kurzform.

Google: https://support.google.com/googleplay/android-developer/answer/13628312

## 2. Neue App in Play Console anlegen

- `[ ]` Standard-Sprache: Deutsch (Schweiz), sofern verfügbar; ansonsten Deutsch.
- `[ ]` App-Name: `PFVR Rheinfelden`.
- `[ ]` App: keine kostenpflichtige Download-App.
- `[ ]` Kategorie: `Sport`.
- `[ ]` Paketname beim ersten AAB kontrollieren: `ch.pfvr.app`.
- `[ ]` Paketname danach nicht mehr ändern.

## 3. App-Inhalte / Policy

### Datenschutz

- `[ ]` Herausgeber + Datenschutzkontakt in `privacy-policy-draft.md` final einsetzen.
- `[ ]` Erklärung als normale öffentliche HTTPS-Webseite veröffentlichen, vorzugsweise auf `pfvr.ch`.
- `[ ]` URL ohne Login/Geoblocking erreichbar; kein PDF.
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

- `[ ]` Verein bestätigt tatsächliches Mindestalter der vorgesehenen Nutzer.
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
- `[ ]` Website `https://www.pfvr.ch/`.
- `[ ]` Support-E-Mail final vom Verein bestätigen.
- `[ ]` Datenschutz-URL eintragen.
- `[ ]` 512×512 Store-Icon hochladen.
- `[ ]` 1024×500 Feature Graphic hochladen.
- `[ ]` mindestens zwei Phone-Screenshots; empfohlen sechs gemäß `store-assets.md`.
- `[ ]` Keine echten Intern-Tokens oder unnötigen Teilnehmerdaten auf Screenshots.

Google: https://support.google.com/googleplay/android-developer/answer/9866151

## 5. Signierung / AAB

- `[ ]` Upload-Key lokal auf einem vertrauenswürdigen Admin-Gerät gemäß `upload-key.md` erzeugen.
- `[ ]` Original-Keystore + Zugangsdaten mindestens zweimal getrennt offline sichern.
- `[ ]` GitHub-Secrets setzen.
- `[ ]` GitHub-Environment `play-store` mit erforderlichen Reviewern/Schutzregeln anlegen.
- `[ ]` `.github/workflows/play-release.yml` auf `main` manuell starten.
- `[ ]` AAB-SHA256 und Signaturbericht archivieren.
- `[ ]` Beim ersten Upload **Play App Signing** aktivieren.

## 6. Testtrack

Empfohlen zuerst `Internal testing` mit einer `0.11.x`-Version.

- `[ ]` Kleine Testgruppe im Verein definieren.
- `[ ]` Play-Install statt Sideload prüfen.
- `[ ]` Update über Play testen.
- `[ ]` Erststart/Landingpage + Freigabecode testen.
- `[ ]` Intern-Link einrichten und Matrix testen.
- `[ ]` Banking-Handoff auf mehreren Geräten/Banking-Apps testen.
- `[ ]` Datenschutzlink aus der Produktions-App testen.

Falls entgegen der Empfehlung ein **neues persönliches Entwicklerkonto** verwendet wird, verlangt Google aktuell vor Produktionszugriff einen Closed Test mit mindestens 12 Testern, die 14 Tage kontinuierlich angemeldet sind.

Google: https://support.google.com/googleplay/android-developer/answer/14151465

## 7. Vor 1.0.0

- `[ ]` Alle Gerätetests aus `Android/README.md` abgearbeitet.
- `[ ]` Keine bekannten kritischen Fehler.
- `[ ]` Review-Zugang funktioniert.
- `[ ]` Privacy/Data Safety final.
- `[ ]` Store-Assets final.
- `[ ]` Vereinsfreigabe für Herausgeber, Logo, Texte, Screenshots und Veröffentlichung dokumentiert.
- `[ ]` Version erst dann auf `1.0.0` setzen.
