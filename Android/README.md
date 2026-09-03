# Android

Aktuelle Android-Testversion: `0.10.2`.

## Entwicklung

- Projektordner `Android/` in Android Studio öffnen.
- Java 17.
- `minSdk 26`, `targetSdk 36`, `compileSdk 36`.
- CI verwendet Gradle 8.13 und führt Unit-Tests, APK-Build sowie Signatur-, Paket- und Versionsprüfung aus.
- Der allgemeine Android-CI-Lauf auf dem finalen Branch-Commit ist das verbindliche Build-Artefakt für Gerätetests.
- Gebaut wird ausschließlich der eingecheckte Quellstand; Build-Workflows dürfen den Anwendungscode nicht patchen.
- APK/AAB-Binärdateien werden nicht ins Repository eingecheckt, sondern ausschließlich als GitHub-Actions-Artefakte bereitgestellt.
- Banking-Kompatibilität wird zentral in `BankingAppRegistry.java` gepflegt.
- Kachelreihenfolge und Sichtbarkeit werden zentral in `TileLayoutStore.java` gepflegt. Stabile Tile-IDs erhalten bestehende Benutzerlayouts über App-Updates hinweg.
- Die Personalisierung wird ausschließlich unter Einstellungen → Ansicht & Kacheln geöffnet; Home, Kasse und Verein bleiben auf ihre eigentlichen Inhalte fokussiert.
- Die interne An-/Abmeldeseite wird ausschließlich per WebView-Skin aufbereitet; der Originalmodus bleibt unverändert.

## Gerätetest 0.10.2

- Interne App-Ansicht: Termine müssen vertikal untereinander erscheinen; Termin-/Kochinfo links, Teilnehmerkarten rechts.
- Bei mehreren hinzugefügten Teilnehmern muss die rechte Teilnehmerleiste pro Termin horizontal scrollbar bleiben.
- Prüfen, dass Köche/Termin-Metadaten nicht als Teilnehmer erscheinen.
- Termine mit unterschiedlichen angebotenen Optionen prüfen. Die App darf keine Auswahl anzeigen, die in der Originalseite für diesen Tag nicht vorhanden ist.
- `+ Person`, Personenauswahl und `Alle anzeigen` testen.
- Nach An-/Abmeldung bzw. einer serverseitigen Aktualisierung soll die vorherige Scrollposition wiederhergestellt werden. Die App selbst erzwingt keinen zusätzlichen Voll-Reload mehr.
- Kachelreihenfolge, Ein-/Ausblenden und Standard-Reset für Home, Kasse und Verein prüfen.

## Lokale Daten

Der persönliche `intern.pfvr.ch`-Link wird ausschließlich in den App-Einstellungen auf dem Endgerät gespeichert und darf nicht ins Repository eingecheckt werden. Auch Kachelreihenfolge, ausgeblendete Kacheln und die bevorzugte Banking-App bleiben lokal.

## Testpaket und Release

Debug-Testpakete verwenden `ch.pfvr.app.test` und seit 0.9.5 einen festen, bewusst öffentlichen Testschlüssel im Repository. Damit sind spätere Test-APKs bei steigendem `versionCode` überinstallierbar. 0.10.2 verwendet `versionCode 26` und denselben Test-Zertifikatsfingerprint.

Der Testschlüssel darf niemals für `ch.pfvr.app` oder einen Store-Release verwendet werden; die Produktionssignierung bleibt geheim und separat.

Bis zum offiziellen Release werden `0.x.y`-Versionen verwendet. `1.0.0` ist für den ersten offiziellen Release reserviert.
