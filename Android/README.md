# Android

Aktuelle Android-Testversion: `0.10.19` (`versionCode 43`).

## Entwicklung

- Projektordner `Android/` in Android Studio öffnen.
- Java 17.
- `minSdk 26`, `targetSdk 36`, `compileSdk 36`.
- CI verwendet Gradle 8.13 und führt Unit-Tests, APK-Build sowie Signatur-, Paket- und Versionsprüfung aus.
- Gebaut wird ausschließlich der eingecheckte Quellstand; Build-Workflows dürfen den Anwendungscode nicht patchen.
- APK/AAB-Binärdateien werden nicht ins Repository eingecheckt, sondern ausschließlich als GitHub-Actions-Artefakte bereitgestellt.
- Banking-Kompatibilität wird zentral in `BankingAppRegistry.java` gepflegt.
- Kachelreihenfolge und Sichtbarkeit werden zentral in `TileLayoutStore.java` gepflegt. Stabile Tile-IDs erhalten bestehende Benutzerlayouts über App-Updates hinweg.
- Die interne An-/Abmeldeseite wird ausschließlich per WebView-Skin aufbereitet; der Originalmodus bleibt unverändert.

## Rheinwerte

- BAFU-Wasserstände werden für Basel-Rheinhalle und Rheinfelden primär in `m ü.M.` dargestellt.
- Ein zusätzlicher relativer Pegel in `cm` wird ausschließlich verwendet, wenn der dafür erforderliche Bezug belastbar bestätigt ist.
- Aktuell besitzt Basel-Rheinhalle einen solchen bestätigten cm-Bezug. Rheinfelden wird nicht auf Basis eines vermuteten oder historischen Pegelnullpunkts umgerechnet und bleibt daher in `m ü.M.`.
- In `Rhein aktuell` erscheint der zusätzliche cm-Wert folglich nur bei Basel-Rheinhalle.
- In `Rhein-Grafiken` wird der Umschalter `m ü.M.` / `cm` ebenfalls nur bei Stationen mit verifiziertem cm-Bezug angeboten. Bei nicht unterstützten Stationen erzwingt die Darstellungslogik `m ü.M.`, auch wenn aus einer älteren App-Version noch eine cm-Einstellung gespeichert sein sollte.

## Interne An-/Abmeldung

- Die App-Ansicht verwendet eine gemeinsame Matrix: Termine und Kochinformationen links, jede Person als feste Spalte rechts über alle Tage hinweg.
- `Termin` und Teilnehmernamen bleiben beim vertikalen Scrollen als Kopf sichtbar; horizontal bewegt ausschließlich der Tabellenkörper die synchronisierte Kopfposition.
- Teilnehmernamen werden aus der tatsächlichen Originaltabelle einschließlich Bearbeiten-/Namens-Controls rekonstruiert. Nach `Original → Alle anzeigen → App-Ansicht` bleibt die Originalzeile die Quelle für Name, Reihenfolge und Status.
- Der Werkzeugleistenbutton `Personen` öffnet Hinzufügen/Entfernen und bleibt auch bei einem unvollständigen Website-Zustand über eine lokale Fallback-Verwaltung nutzbar.
- `Aus Initiallink neu aufbauen` löscht nur lokale Personen-/Restore-Zustände und lädt den gespeicherten persönlichen Initiallink erneut.
- Website-Bulk-Aktionen wie `Alle anzeigen` sind im App-Modus blockiert; im unveränderten Originalmodus können sie bewusst verwendet werden.
- Statusentscheidungen werden ausschließlich über die echten Website-Controls vorgenommen.

## Gerätetest 0.10.19

- `Rhein aktuell`: Basel-Rheinhalle und Rheinfelden müssen beide den Wasserstand in `m ü.M.` zeigen. Nur Basel darf darunter zusätzlich einen cm-Pegel anzeigen.
- `Rhein-Grafiken`: Basel muss zwischen `m ü.M.` und `cm` umschaltbar sein; Rheinfelden darf keinen cm-Umschalter erhalten. Aktueller Wert, rechte Achse, Kurve, Hinweis und Tooltip müssen jeweils dieselbe Einheit verwenden.
- Die beiden Zeitstempel unter `Rhein aktuell` müssen am unteren Kartenrand auf derselben Höhe stehen.
- Kacheln verschieben/ein- oder ausblenden: Die Kachelverwaltung darf danach nicht mehr an den Seitenanfang springen.
- `Original → Alle anzeigen → App-Ansicht`: alle Originalpersonen müssen mit echtem Namen, korrekter Reihenfolge und ihrem eigenen Status übernommen werden.
- `Personen`: Hinzufügen, Entfernen und Neuaufbau aus Initiallink auch nach App-Neustart prüfen.
- Beim langen vertikalen Scrollen müssen `Termin` und die aktuell sichtbaren Teilnehmernamen stehen bleiben; horizontales Wischen muss Kopf und Personenspalten exakt synchron halten.
- Lange Koch-/Verantwortlichkeitsnamen müssen sich in der Terminspalte verkleinern, ohne Datum oder übrigen Termintext mitzuskaliert.

## Lokale Daten

Der persönliche `intern.pfvr.ch`-Link wird ausschließlich in den App-Einstellungen auf dem Endgerät gespeichert und darf nicht ins Repository eingecheckt werden. Kachelreihenfolge, bevorzugte Banking-App, Graphdarstellung und gewünschte Teilnehmerliste bleiben ebenfalls lokal.

## Testpaket und Release

Debug-Testpakete verwenden `ch.pfvr.app.test` und seit 0.9.5 einen festen, bewusst öffentlichen Testschlüssel im Repository. Damit sind spätere Test-APKs bei steigendem `versionCode` überinstallierbar. Der Testschlüssel darf niemals für `ch.pfvr.app` oder einen Store-Release verwendet werden; die Produktionssignierung bleibt geheim und separat.

Bis zum offiziellen Release werden `0.x.y`-Versionen verwendet. `1.0.0` ist für den ersten offiziellen Release reserviert.
