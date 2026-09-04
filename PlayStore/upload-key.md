# Android Upload-Key für Google Play

Der bestehende eingecheckte PFVR-Testschlüssel ist **nur** für `ch.pfvr.app.test`. Für `ch.pfvr.app` muss ein separater Upload-Key verwendet werden.

## Sicherheitsregel

Den Produktions-/Upload-Key auf einem vertrauenswürdigen Rechner des Vereins bzw. des verantwortlichen Admins erzeugen. Nicht in Chat, GitHub, Tickets oder Cloud-Notizen kopieren. Das Original mindestens zweimal getrennt offline sichern.

Der Upload-Key ist nicht der endgültige App-Signing-Key, wenn **Play App Signing** verwendet wird. Google verwaltet dann den App-Signing-Key; der eigene Key dient zum authentifizierten Upload neuer Bundles und kann im Notfall über den vorgesehenen Google-Prozess ersetzt werden.

## Erzeugen

Mit einem aktuellen JDK:

```bash
keytool -genkeypair \
  -keystore pfvr-upload.jks \
  -alias pfvr-upload \
  -keyalg RSA \
  -keysize 4096 \
  -validity 10000 \
  -v
```

Passwörter nicht in Shell-History oder Skripten hinterlegen. Bei den Zertifikatsdaten die tatsächlichen Vereins-/Herausgeberangaben verwenden.

## Zertifikat dokumentieren

```bash
keytool -list -v -keystore pfvr-upload.jks -alias pfvr-upload
```

SHA-256-Fingerprint separat dokumentieren. Den Keystore selbst niemals committen.

## Base64-Kopie für GitHub Actions

Linux:

```bash
base64 -w 0 pfvr-upload.jks > pfvr-upload.jks.b64
```

macOS:

```bash
base64 < pfvr-upload.jks | tr -d '\n' > pfvr-upload.jks.b64
```

Die Base64-Datei nur zum Eintragen des Secrets verwenden und anschließend vom Arbeitsrechner löschen, sofern sie nicht bewusst verschlüsselt archiviert wird.

## GitHub-Secrets

Empfohlen in der GitHub-Environment `play-store`:

- `ANDROID_UPLOAD_KEYSTORE_B64` = kompletter Base64-Inhalt
- `ANDROID_UPLOAD_STORE_PASSWORD`
- `ANDROID_UPLOAD_KEY_ALIAS` = `pfvr-upload`
- `ANDROID_UPLOAD_KEY_PASSWORD`

Environment nach Möglichkeit mit erforderlichen Reviewern/Schutzregeln versehen.

## Erster Play-Upload

1. `main` auf den gewünschten Teststand bringen.
2. `.github/workflows/play-release.yml` manuell ausführen.
3. erzeugtes versioniertes `.aab`, `SHA256SUMS.txt` und `build-info.txt` herunterladen/archivieren.
4. AAB in den **Internal testing**-Track der Play Console hochladen.
5. Beim Einrichten **Play App Signing** aktivieren.
6. Von Google angezeigte App-Signing- und Upload-Zertifikatsfingerprints dokumentieren.
7. Testinstallation ausschließlich aus dem Play-Testtrack durchführen.

## Nicht tun

- `pfvr-upload.jks` committen,
- Passwort oder Keystore in `gradle.properties` im Repository speichern,
- denselben Schlüssel wie für `ch.pfvr.app.test` verwenden,
- den Play-App-Signing-Key selbst in GitHub hinterlegen, wenn Google ihn verwaltet,
- den einzigen Keystore nur auf einem einzelnen Laptop aufbewahren.
