# Android

Aktuelle Android-Testversion: `0.5.0`.

## Entwicklung

- Projektordner `Android/` in Android Studio öffnen.
- Java 17.
- `minSdk 26`, `targetSdk 36`, `compileSdk 36`.
- Android Gradle Plugin 8.11.1.
- CI-Build mit Gradle 8.13.

## Lokale Daten

Der persönliche `intern.pfvr.ch`-Link wird ausschließlich in den App-Einstellungen auf dem Endgerät gespeichert und darf nicht ins Repository eingecheckt werden.

## Debug-Build

```bash
gradle assembleDebug
```

Die GitHub-Action `.github/workflows/android.yml` erzeugt eine geprüfte Debug-APK. Nach erfolgreichem Test wird die aktuelle APK zusätzlich unter `Downloads/current/` abgelegt; ältere Teststände kommen nach `Downloads/archive/`.

## Google-Play-Build

Der manuelle Workflow `.github/workflows/android-release.yml` benötigt folgende Repository-Secrets:

- `PFVR_UPLOAD_KEYSTORE_BASE64`
- `PFVR_KEYSTORE_PASSWORD`
- `PFVR_KEY_ALIAS`
- `PFVR_KEY_PASSWORD`

Er erzeugt ein signiertes `.aab` und zusätzlich eine signierte Release-APK für interne Tests. Der Schlüssel darf nie im Repository liegen.

## Release

Bis zum offiziellen Release werden `0.x.y`-Versionen verwendet. `1.0.0` ist für den ersten offiziellen Release reserviert.
