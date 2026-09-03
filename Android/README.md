# Android

Aktuelle Android-Testversion: `0.9.6`.

## Entwicklung

- Projektordner `Android/` in Android Studio öffnen.
- Java 17.
- `minSdk 26`, `targetSdk 36`, `compileSdk 36`.
- CI verwendet Gradle 8.13 und führt Unit-Tests, APK-Build sowie Signatur-, Paket- und Versionsprüfung aus.
- Gebaut wird ausschließlich der eingecheckte Quellstand; Build-Workflows dürfen den Anwendungscode nicht patchen.
- Banking-Kompatibilität wird zentral in `BankingAppRegistry.java` gepflegt. Neue Banken oder bestätigte Fähigkeiten sollen dort ergänzt werden, statt Sonderfälle in `MainActivity` zu verteilen.

## Lokale Daten

Der persönliche `intern.pfvr.ch`-Link wird ausschließlich in den App-Einstellungen auf dem Endgerät gespeichert und darf nicht ins Repository eingecheckt werden.

## Testpaket und Release

Debug-Testpakete verwenden `ch.pfvr.app.test` und seit 0.9.5 einen festen, bewusst öffentlichen Testschlüssel im Repository. Damit sind spätere Test-APKs bei steigendem `versionCode` überinstallierbar. 0.9.6 verwendet `versionCode 20` und ist damit der erste direkte Update-Test auf Basis dieses stabilen Schlüssels.

Der Testschlüssel darf niemals für `ch.pfvr.app` oder einen Store-Release verwendet werden; die Produktionssignierung bleibt geheim und separat.

Bis zum offiziellen Release werden `0.x.y`-Versionen verwendet. `1.0.0` ist für den ersten offiziellen Release reserviert.
