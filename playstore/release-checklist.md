# Release-Checkliste

## Recht und Konto

- [ ] Veröffentlichung und Nutzung von Name, Logo, Website-Inhalten und interner Plattform durch den Verein freigegeben
- [ ] Play-Entwicklerkonto festgelegt und verifiziert
- [ ] Kontakt- und Supportadresse bestätigt
- [ ] öffentliche Datenschutzerklärung erreichbar

## Schlüssel und Build

- [ ] dauerhafter Upload-Key erzeugt und offline gesichert
- [ ] GitHub-Secrets gesetzt: `PFVR_UPLOAD_KEYSTORE_BASE64`, `PFVR_KEYSTORE_PASSWORD`, `PFVR_KEY_ALIAS`, `PFVR_KEY_PASSWORD`
- [ ] Release-Workflow erzeugt signiertes AAB
- [ ] Play App Signing aktiviert
- [ ] Versionscode eindeutig erhöht

## Store

- [ ] finales 512×512-App-Icon aus hochauflösendem Original
- [ ] Feature-Grafik 1024×500 bestätigt
- [ ] mindestens zwei bereinigte Smartphone-Screenshots
- [ ] Kurz- und Langbeschreibung bestätigt
- [ ] Kategorie, Altersfreigabe und Zielgruppe bestätigt
- [ ] Data-Safety-Formular bestätigt
- [ ] App-Zugriff: Demo-Link oder nachvollziehbare Prüfanweisung für den internen Bereich

## Funktionstest

- [ ] Startseite online und offline
- [ ] Kalendercache und Trainingserkennung
- [ ] Wetterzeit entspricht dem Kalendertermin
- [ ] BAFU-Daten und Achsenbeschriftungen
- [ ] heller/dunkler App-Modus
- [ ] öffentliche Webseiten im WebView lesbar
- [ ] interner Bereich, Cookies und An-/Abmeldung
- [ ] Swiss QR mit Testscanner validiert
- [ ] Banking-/TWINT-App-Auswahl
