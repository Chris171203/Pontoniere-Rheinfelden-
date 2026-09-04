# Google Play Store-Assets

## Pflichtformate

### Store-App-Icon

- 512 × 512 px
- 32-Bit PNG mit Alpha
- maximal 1.024 KB
- kein Google-Play-Badge, kein Ranking-/Preistext

Das Store-Icon ist **nicht** dasselbe wie das Launcher-Icon im APK. Das aktuelle App-Ressourcenlogo `Android/app/src/main/res/drawable/pfvr_logo.jpg` ist nur ein kleines Laufzeit-Asset und kein geeigneter hochauflösender Store-Master.

**Benötigt vom Verein:** möglichst Vektorlogo (SVG/PDF/AI) oder mindestens ein sauberes hochauflösendes PNG. Keine künstliche Hochskalierung des kleinen JPEG als finalen Store-Master.

Zieldatei: `PlayStore/assets/icon-512.png`

### Feature Graphic

- 1024 × 500 px
- JPEG oder 24-Bit PNG, ohne Alpha

Empfohlene Gestaltung für PFVR:

- Rhein/Weidling/Pontonier-Szene als Hauptmotiv,
- ruhige PFVR-Farbwelt,
- Logo nur ergänzend, nicht als riesige Wiederholung des App-Icons,
- wenig Text; falls Text, z. B. `PFVR Rheinfelden · Uf em Rhy dihei`.

Zieldatei: `PlayStore/assets/feature-graphic-1024x500.png`

### Phone-Screenshots

Google verlangt mindestens zwei Screenshots. Für den PFVR-Eintrag sind sechs sinnvoll:

1. **Home** – Hero + Trainingswetter + Einstieg in Rhein/Termine.
2. **Rhein aktuell** – Basel/Rheinfelden, Pegel, Schifffahrtslage.
3. **Rhein-Grafiken** – Verlauf mit Pegel/Abfluss und Hochwassermarken.
4. **Termine** – Jahresprogramm / nächste Termine.
5. **Interne An-/Abmeldung** – ausschließlich mit Demo-/Testpersonen, niemals produktiven Mitgliederdaten.
6. **Verein / Schwiizerdütsch** – Vereinsinfos oder Sprachumschaltung als lokales Merkmal.

Optional siebter Screenshot: Vereinsbeiz/Warenkorb/Swiss-QR, aber ohne echte Banking-App-Oberfläche und ohne unnötige Zahlungs-/Kontodetails über das ohnehin öffentliche Vereins-Zahlungsziel hinaus.

Google-Anforderungen für Screenshots:

- JPEG oder 24-Bit PNG ohne Alpha,
- Mindestabmessung 320 px,
- Maximalabmessung 3.840 px,
- längste Seite höchstens doppelt so lang wie die kürzeste.

## Datenschutz bei Screenshots

Vor Upload jeden Screenshot prüfen auf:

- persönliche `intern.pfvr.ch`-URL / Token,
- Namen realer Mitglieder, sofern nicht ausdrücklich für Veröffentlichung freigegeben,
- interne Status-/Teilnahmedaten,
- Benachrichtigungen oder Statusleiste mit privaten Inhalten,
- Banking-App-Daten,
- E-Mail-/Telefoninformationen, die nicht ohnehin als öffentliche Vereinskontakte vorgesehen sind.

Für den internen Screen einen dedizierten Demo-Datensatz verwenden.

## Empfohlene Dateistruktur

```text
PlayStore/assets/
  icon-512.png
  feature-graphic-1024x500.png
  screenshots/
    phone-01-home.png
    phone-02-rhein-aktuell.png
    phone-03-rhein-grafik.png
    phone-04-termine.png
    phone-05-intern-demo.png
    phone-06-verein-schwiizerduetsch.png
```

Binärdateien erst committen, wenn der Verein die Nutzung der Motive/Logos freigegeben hat und die Dateien tatsächlich final sind.

Offizielle Referenz: https://support.google.com/googleplay/android-developer/answer/9866151
