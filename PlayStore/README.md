# Google Play Vorbereitung

Technisch ist die Android-App für Google Play geeignet. Vor dem ersten Store-Release bleiben organisatorische und Store-spezifische Schritte.

## Technischer Stand

- Paketname: `ch.pfvr.app`
- Testversion: `0.5.0`
- `targetSdk` / `compileSdk`: API 36 (Android 16)
- Mindestversion: Android 8 / API 26
- Debug-APK wird über `.github/workflows/android.yml` gebaut.
- Ein signiertes Release-AAB kann über `.github/workflows/play-release.yml` erzeugt werden, sobald der Upload-Key als GitHub-Secrets hinterlegt ist.

## Noch vor Veröffentlichung festlegen

1. **Herausgeberkonto**: idealerweise Organisationskonto des Pontonierfahrvereins Rheinfelden, falls der Verein die App offiziell veröffentlicht. Alternativ privates Entwicklerkonto.
2. **Upload-Key** erzeugen und sicher offline sichern. Nur die Base64-Kopie und Passwörter als GitHub-Secrets hinterlegen.
3. **Play App Signing** beim ersten Upload aktivieren.
4. **Datenschutzerklärung** finalisieren. Der Entwurf liegt in `PlayStore/privacy-policy-draft.md`; Herausgeber/Datenschutzkontakt müssen zum tatsächlichen Play-Konto passen.
5. **Store-Grafiken**: hochauflösendes App-Icon, Feature Graphic und Screenshots erzeugen. Das aktuelle kleine Vereinslogo ist für einen finalen Store-Auftritt qualitativ noch nicht ideal.
6. **Data-Safety-Formular** anhand der finalen App ausfüllen.
7. **Inhaltsklassifizierung** und sonstige App-Content-Angaben in der Play Console ausfüllen.
8. Falls ein **neues privates Entwicklerkonto** verwendet wird: geschlossenen Test mit mindestens 12 Testern über 14 aufeinanderfolgende Tage durchführen, bevor Produktionszugriff beantragt werden kann.

## GitHub-Secrets für den Release-Workflow

- `ANDROID_UPLOAD_KEYSTORE_B64`
- `ANDROID_UPLOAD_STORE_PASSWORD`
- `ANDROID_UPLOAD_KEY_ALIAS`
- `ANDROID_UPLOAD_KEY_PASSWORD`

Keystore und Passwörter niemals committen.

## Store-Positionierung

Die App ist keine reine WebView-Hülle: Kalender-Cache, Trainingswetter, BAFU-Livedaten/Verläufe, Kasse/Swiss-QR, Einstellungen und lokale Integrationen sind nativ. Für eingebettete PFVR-Seiten sollte vor Veröffentlichung geklärt sein, dass der Verein bzw. Websitebetreiber die App und die Darstellung seiner Inhalte erlaubt.
