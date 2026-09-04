# Android

Aktuelle Android-Testversion: `0.10.7`.

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

## Gerätetest 0.10.7

- Ausgangslage für die Regression: Ist die zweite Person in `Original` bereits sichtbar, muss sie nach Wechsel zu `App` dauerhaft als zweite feste Personenspalte stehen bleiben und darf nicht mehr kurz aufblitzen und wieder verschwinden.
- Zusatzperson über den sichtbaren App-Select hinzufügen: jede danach in der Originaltabelle vorhandene Personenzeile muss in der App-Matrix sichtbar bleiben. Die Originaltabelle ist die Quelle der Wahrheit; nur ausdrücklich lokal entfernte Personen werden ausgeblendet.
- Unterschiedliche Reihenfolge oder Schreibweise von Auswahltext und Tabellenname darf die Person nicht mehr herausfiltern. Für die Wiederherstellung wird zusätzlich der Optionswert gespeichert und der Name tokenbasiert verglichen.
- Zwei oder mehr Zusatzpersonen einblenden, App komplett schließen und neu öffnen: die App-Ansicht soll die gespeicherte Personenliste über den Website-Select wiederherstellen.
- Unter `+ / − Person` eine Zusatzperson lokal entfernen: sie muss sofort aus der Matrix verschwinden und nach Neustart entfernt bleiben; die Standardperson darf nicht entfernbar sein.
- Auch weit unten in der Terminliste muss in jeder Personenzelle der Name sichtbar sein; lange Namen dürfen höchstens zwei Zeilen belegen und die Personenspalte nicht verbreitern.
- Die interne App-Ansicht ist eine gemeinsame Matrix: Termine/Kochinfo links, jede Person als feste Spalte rechts über alle Tage hinweg.
- Horizontal wird die komplette Personenmatrix gemeinsam gescrollt; die Terminspalte links bleibt dabei stehen. Mindestens zwei Personenspalten sollen gleichzeitig sichtbar sein.
- Nach einer Statusauswahl muss sich die Farbe des echten Buttons/Selects passend aktualisieren: grün `Mit Essen`, gelb `Ohne Essen`, rot `Komme nicht`, grau `Nicht gewählt`.
- Danach einmal `Neu laden` oder `Original` prüfen: bleibt der Status erhalten, ist die Auswahl serverseitig gespeichert.
- Unter `+ / − Person` ist nur der App-Proxy sichtbar. Das originale Website-Select bleibt unverändert an seinem ursprünglichen Formular-/DOM-Ort und wird dort ausgelöst; `Entfernen` nimmt Zusatzpersonen nur aus der lokalen Ansicht und verändert keine serverseitigen An-/Abmeldedaten.
- Prüfen, dass Köche/Termin-Metadaten nicht als Teilnehmer erscheinen und unterschiedliche zulässige Essens-/Anmeldeoptionen unverändert von der Website übernommen werden.
- Nach serverseitiger Navigation sollen vertikale Position und gemeinsame horizontale Matrixposition wiederhergestellt werden. Die App selbst erzwingt keinen zusätzlichen Voll-Reload.
- Kachelreihenfolge, Ein-/Ausblenden und Standard-Reset für Home, Kasse und Verein prüfen.

## Lokale Daten

Der persönliche `intern.pfvr.ch`-Link wird ausschließlich in den App-Einstellungen auf dem Endgerät gespeichert und darf nicht ins Repository eingecheckt werden. Auch Kachelreihenfolge, ausgeblendete Kacheln und die bevorzugte Banking-App bleiben lokal. Die gewünschte Teilnehmeransicht wird im WebView-Speicher des internen PFVR-Ursprungs gehalten und überlebt normale App-Neustarts; beim Löschen der App-/Website-Daten wird sie zurückgesetzt.

## Testpaket und Release

Debug-Testpakete verwenden `ch.pfvr.app.test` und seit 0.9.5 einen festen, bewusst öffentlichen Testschlüssel im Repository. Damit sind spätere Test-APKs bei steigendem `versionCode` überinstallierbar. 0.10.7 verwendet `versionCode 31` und denselben Test-Zertifikatsfingerprint.

Der Testschlüssel darf niemals für `ch.pfvr.app` oder einen Store-Release verwendet werden; die Produktionssignierung bleibt geheim und separat.

Bis zum offiziellen Release werden `0.x.y`-Versionen verwendet. `1.0.0` ist für den ersten offiziellen Release reserviert.
