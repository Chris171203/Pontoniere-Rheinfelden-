# Android

Aktuelle Android-Testversion: `0.10.22` (`versionCode 46`).

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
- Die offizielle Schifffahrtslage richtet sich ausschließlich nach dem Pegel Basel-Rheinhalle: 700 cm = HWM I/Voralarm, 790 cm = HWM IIb/Sperrung Kleinschifffahrt und Fähren Basel–Rheinfelden, 820 cm = HWM IIa/Sperrung Rheinfelden–Kembs.
- Der Abfluss bleibt ein eigener hydrologischer Messwert und steuert keine offizielle Sperrstufe.
- Im Basel-Diagramm werden die drei Hochwassermarken auf der Pegelachse eingezeichnet. Pegel- und Abflusskurve folgen je Abschnitt der zeitgleichen Pegelstufe, verwenden innerhalb derselben Stufe aber unterschiedliche Farbtöne.
- Im Normalzustand ist der Abfluss neutral stahlblau. Warnfarben dürfen erst mit einer tatsächlichen Basel-Hochwasserstufe erscheinen.

## Interne An-/Abmeldung

- Die App-Ansicht verwendet eine gemeinsame Matrix: Termine und Kochinformationen links, jede Person als feste Spalte rechts über alle Tage hinweg.
- `Termin` und Teilnehmernamen bleiben beim vertikalen Scrollen als Kopf sichtbar; horizontal bewegt ausschließlich der Tabellenkörper die synchronisierte Kopfposition.
- Teilnehmernamen werden aus der tatsächlichen Originaltabelle einschließlich Bearbeiten-/Namens-Controls rekonstruiert. Nach `Original → Alle anzeigen → App-Ansicht` bleibt die Originalzeile die Quelle für Name, Reihenfolge und Status.
- Der Werkzeugleistenbutton `Personen` öffnet Hinzufügen/Entfernen und bleibt auch bei einem unvollständigen Website-Zustand über eine lokale Fallback-Verwaltung nutzbar.
- `Aus Initiallink neu aufbauen` löscht nur lokale Personen-/Restore-Zustände und lädt den gespeicherten persönlichen Initiallink erneut.
- Website-Bulk-Aktionen wie `Alle anzeigen` sind im App-Modus blockiert; im unveränderten Originalmodus können sie bewusst verwendet werden.
- Statusentscheidungen werden ausschließlich über die echten Website-Controls vorgenommen.
- Koch-/Verantwortlichkeitsnamen in der linken Terminspalte sind in der App-Ansicht reine Anzeigetexte: nicht fokussierbar, nicht editierbar und ohne Tastatur. Lange Namen werden automatisch kleiner skaliert; Datumsangaben werden fett hervorgehoben.

## Gerätetest 0.10.22

- `Rhein aktuell`: Der Pegel ist wieder die größere Hauptgröße. Basel muss abhängig von seiner aktuellen Hochwasserstufe Pegelwert und Lage-Badge passend einfärben; der Abfluss bleibt kleiner und farblich unterscheidbar. Rheinfelden bleibt ohne erfundene lokale Sperrstufe neutral.
- Im Normalzustand müssen Abflusswert und Abflusskurve neutral stahlblau wirken und dürfen nicht wie eine Warnung aussehen. Gelb/Orange/Rot dürfen erst bei einer tatsächlichen Basel-Hochwasserstufe erscheinen.
- `Rhein aktuell`: Basel-Rheinhalle und Rheinfelden müssen beide den Wasserstand in `m ü.M.` zeigen. Nur Basel darf darunter zusätzlich einen cm-Pegel anzeigen. Die beiden Zeitstempel müssen am unteren Kartenrand auf derselben Höhe stehen.
- `Rhein-Grafiken`: Basel muss zwischen `m ü.M.` und `cm` umschaltbar sein; Rheinfelden darf keinen cm-Umschalter erhalten. Aktueller Wert, rechte Achse, Kurve, Hinweis und Tooltip müssen jeweils dieselbe Einheit verwenden.
- Im Basel-Graphen müssen HWM I/IIb/IIa an 700/790/820 cm beziehungsweise 247.00/247.90/248.20 m ü.M. an der Pegelachse liegen. Historische Pegel- und Abflussabschnitte müssen dieselbe Stufe ausdrücken, aber als unterschiedliche Kurven erkennbar bleiben.
- Bei einem ausgewählten Basel-Diagrammpunkt müssen die beiden Punkte zur dortigen historischen Stufe passen und der Tooltip zusätzlich die damalige Stufe nennen.
- Kacheln verschieben/ein- oder ausblenden: Die Kachelverwaltung darf danach nicht mehr an den Seitenanfang springen.
- `Original → Alle anzeigen → App-Ansicht`: alle Originalpersonen müssen mit echtem Namen, korrekter Reihenfolge und ihrem eigenen Status übernommen werden.
- `Personen`: Hinzufügen, Entfernen und Neuaufbau aus Initiallink auch nach App-Neustart prüfen.
- Beim langen vertikalen Scrollen müssen `Termin` und die aktuell sichtbaren Teilnehmernamen stehen bleiben; horizontales Wischen muss Kopf und Personenspalten exakt synchron halten.
- Koch-/Verantwortlichkeitsnamen in der Terminspalte dürfen nicht fokussierbar oder editierbar sein und beim Antippen keine Tastatur öffnen. Lange Namen müssen sich verkleinern; das Datum soll fett bleiben, ohne Zähler oder Termintext mitzuskaliert.

## Lokale Daten

Der persönliche `intern.pfvr.ch`-Link wird ausschließlich in den App-Einstellungen auf dem Endgerät gespeichert und darf nicht ins Repository eingecheckt werden. Kachelreihenfolge, bevorzugte Banking-App, Graphdarstellung und gewünschte Teilnehmerliste bleiben ebenfalls lokal.

## Testpaket und Release

Debug-Testpakete verwenden `ch.pfvr.app.test` und seit 0.9.5 einen festen, bewusst öffentlichen Testschlüssel im Repository. Damit sind spätere Test-APKs bei steigendem `versionCode` überinstallierbar. Der Testschlüssel darf niemals für `ch.pfvr.app` oder einen Store-Release verwendet werden; die Produktionssignierung bleibt geheim und separat.

Bis zum offiziellen Release werden `0.x.y`-Versionen verwendet. `1.0.0` ist für den ersten offiziellen Release reserviert.
