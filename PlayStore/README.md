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

1. **Herausgeberkonto**: für eine offizielle Vereins-App ist ein Organisationskonto des Pontonierfahrvereins Rheinfelden die sauberste Variante. Google verlangt dafür einen D-U-N-S-Eintrag und Organisationsnachweise. Alternativ ist ein privates Entwicklerkonto möglich; dann erscheint der private Herausgeber entsprechend bei Google Play und bei neuen privaten Konten gilt vor Produktion die Testanforderung unten.
2. **Upload-Key** erzeugen und sicher offline sichern. Nur die Base64-Kopie und Passwörter als GitHub-Secrets hinterlegen.
3. **Play App Signing** beim ersten Upload verwenden.
4. **Datenschutzerklärung** finalisieren. Der Entwurf liegt in `PlayStore/privacy-policy-draft.md`; Herausgeber/Datenschutzkontakt müssen zum tatsächlichen Play-Konto passen. Die finale Erklärung muss öffentlich erreichbar sein und zusätzlich aus der App verlinkt werden.
5. **Store-Grafiken**: hochauflösendes App-Icon, Feature Graphic und Screenshots erzeugen. Das aktuelle kleine Vereinslogo ist für einen finalen Store-Auftritt qualitativ noch nicht ideal.
6. **Data-Safety-Formular** anhand der finalen App und des tatsächlichen Herausgebers ausfüllen.
7. **Inhaltsklassifizierung** und sonstige App-Content-Angaben in der Play Console ausfüllen.
8. **App-Zugriff für Google-Review**: Der interne PFVR-Bereich ist zugangsbeschränkt. Für eine Store-Prüfung sollte der Verein einen separaten Test-/Demo-Zugang bereitstellen, der ausschließlich in der Play Console unter den Review-Zugangsdaten hinterlegt wird. Keinesfalls einen persönlichen Vereinslink ins Repository oder in den Store-Text übernehmen.
9. Falls ein **neues privates Entwicklerkonto** verwendet wird: geschlossenen Test mit mindestens 12 Testern über 14 aufeinanderfolgende Tage durchführen, bevor Produktionszugriff beantragt werden kann. Der interne Test-Track kann bereits vorher für die Verteilung an Tester genutzt werden.

## GitHub-Secrets für den Release-Workflow

- `ANDROID_UPLOAD_KEYSTORE_B64`
- `ANDROID_UPLOAD_STORE_PASSWORD`
- `ANDROID_UPLOAD_KEY_ALIAS`
- `ANDROID_UPLOAD_KEY_PASSWORD`

Keystore und Passwörter niemals committen.

## Store-Positionierung

Die App ist keine reine WebView-Hülle: Kalender-Cache, Trainingswetter, BAFU-Livedaten/Verläufe, Kasse/Swiss-QR, Einstellungen und lokale Integrationen sind nativ. Für eingebettete PFVR-Seiten sollte vor Veröffentlichung geklärt sein, dass der Verein bzw. Websitebetreiber die App und die Darstellung seiner Inhalte erlaubt. Wird die App direkt vom Verein herausgegeben, ist diese Zuordnung deutlich sauberer.

## Praktischer Testweg

Ein Google-Play-**interner Test** wäre bereits vor dem öffentlichen Release sinnvoll: Tester installieren über Google Play statt per APK-Sideload. Damit entfällt auf den Testgeräten der normale Android-Dialog für Installationen aus unbekannten Quellen. `1.0.0` bleibt trotzdem für den ersten offiziellen Produktionsrelease reserviert.
