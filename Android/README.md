# Android

Aktuelle Android-Testversion: `0.11.4` (`versionCode 51`).

## Entwicklung

- Projektordner `Android/` in Android Studio öffnen.
- Java 17.
- `minSdk 26`, `targetSdk 36`, `compileSdk 36`.
- CI verwendet Gradle 8.13 und führt Unit-Tests, `lintRelease`, Debug-APK-Build, Release-AAB-Kompilierung sowie Signatur-, Paket-, Versions- und Berechtigungsprüfung aus.
- CI exportiert die tatsächlich zusammengeführten APK-Berechtigungen und bricht bei unerwarteten sensiblen/hochwirksamen Berechtigungen ab.
- Gebaut wird ausschließlich der eingecheckte Quellstand; Build-Workflows dürfen den Anwendungscode nicht patchen.
- CI blockiert bekannte persönliche PFVR-Zugangsmuster und rohe Keystore-/PKCS-Schlüsseldateien im Repository.
- APK/AAB-Binärdateien werden nicht im Repository versioniert, sondern ausschließlich als CI-Artefakte erzeugt.
- Banking-Kompatibilität wird zentral in `BankingAppRegistry.java` gepflegt.
- Kachelreihenfolge und Sichtbarkeit werden zentral in `TileLayoutStore.java` gepflegt. Stabile Tile-IDs erhalten bestehende Benutzerlayouts über App-Updates hinweg.
- Die interne An-/Abmeldeseite wird ausschließlich per WebView-Skin aufbereitet; der Originalmodus bleibt unverändert.

## Play-Store-Readiness 0.11.4

- Produktions-Paket: `ch.pfvr.app`; Debug/Test bleibt `ch.pfvr.app.test`.
- Der Play-Bundle-Workflow darf ausschließlich manuell von `main` laufen und verwendet die geschützte GitHub-Environment `play-store`.
- Produktions-/Upload-Signierung benötigt einen separaten Upload-Key über CI-Secrets; der feste öffentliche Testschlüssel darf niemals für den Store verwendet werden.
- `lintRelease` ist verpflichtender Teil der CI. Der in der Vorbereitung gefundene API-27-only Wert `windowLightNavigationBar` liegt nun ausschließlich in `values-v27`, sodass `minSdk 26` tatsächlich kompatibel bleibt.
- `android:allowBackup="false"` wird durch explizite Backup-/Data-Extraction-Regeln ergänzt. App-Dateien, Datenbanken, Shared Preferences und externe App-Dateien sind aus Cloud-Backup und Device-to-Device-Transfer ausgeschlossen, damit insbesondere persönliche PFVR-Zugänge nicht auf andere Geräte migriert werden.
- Das kleine `pfvr_logo.jpg` ist weiterhin nur ein Laufzeit-Asset. Für Google Play wird ein vom Verein freigegebenes hochauflösendes bzw. vektorbasiertes Masterlogo benötigt.
- Store-, Datenschutz-, Data-Safety-, Review-, Asset- und Upload-Key-Unterlagen liegen in `PlayStore/`; Release-Sicherheitsentscheidungen in `decisions/play-store-security.md`.

## Sprache und Erstfreigabe

- Die native Oberfläche unterstützt `Deutsch` und `Schwiizerdütsch`. Die Dialektfassung umfasst Landingpage, Home, Rhein, Termine, News, Einstellungen, Kachelverwaltung, Verein, Kasse, häufige App-Dialoge/Statusmeldungen sowie die von der App erzeugte interne Personenverwaltung und ihre Statusbeschriftungen.
- Externe beziehungsweise quellenseitige Texte, Kalendereinträge, News, Termin-/Personennamen und die originale PFVR-Seite werden absichtlich nicht verändert. Sie werden technisch über eine rohe Textausgabe an der Dialekt-Mapping-Schicht vorbeigeführt; gemischte Anzeigen lokalisieren nur ihre App-Beschriftungen.
- Die Schwiizerdütsch-Fassung ist bewusst gut lesbar und sprachlich konsistent gehalten, statt eine maximal enge Ortsmundart zu erzwingen.
- Beim ersten Start dieses App-Datenstands wird ein Freigabecode verlangt. Nach erfolgreicher Prüfung wird nur ein lokales Freigabe-Flag gespeichert; der Klartextcode ist nicht im Repository enthalten.
- Der Erstfreigabe-Screen dient zugleich als öffentliche Landingpage: Sprachwahl, der Hinweis auf Schnuppertraining bereits vor der Mitgliedschaft, die offizielle Einstiegs-/Formularseite sowie Instagram/Facebook sind ohne Freigabe erreichbar. Die eigentlichen App-Screens, Live-Daten und internen WebViews werden weiterhin erst nach erfolgreicher Freigabe aufgebaut.
- Schnuppertraining/Mitgliedschaft sowie Instagram und Facebook sind für bereits freigeschaltete Installationen zusätzlich über Home bzw. den Bereich Verein erreichbar.
- Die Erstfreigabe ist eine lokale Zugangshürde und ersetzt keine serverseitige Authentifizierung. Persönliche `intern.pfvr.ch`-Links bleiben weiterhin ausschließlich auf dem Endgerät.

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
- Koch-/Verantwortlichkeitsnamen in der linken Terminspalte sind in der App-Ansicht reine Anzeigetexte: nicht fokussierbar, nicht editierbar und ohne Tastatur. Das ursprüngliche Website-Feld bleibt nur technisch verborgen erhalten; lange Namen werden automatisch kleiner skaliert und Datumsangaben fett hervorgehoben.

## Gerätetest 0.11.4

- Frische Installation/App-Daten: Die Landingpage muss sichtbar sein, zwischen Deutsch und Schwiizerdütsch umschalten und die hinterlegten PFVR-Ziele für Schnuppertraining/Formulare, Instagram sowie Facebook ohne Freigabe öffnen. Home, Live-Daten und interner WebView dürfen vor Codeeingabe nicht initialisiert werden.
- Schwiizerdütsch: Home, Rhein, Termine, News, Einstellungen, Kachelverwaltung, Verein, Kasse sowie die app-erzeugte Personenverwaltung/Anmeldestatus auf verbliebene hochdeutsche App-Texte prüfen. Inhalte aus Kalender, News, Personennamen und Original-PFVR-Seite müssen unverändert bleiben.
- Verein: neue Kacheln für Schnuppertraining/Mitgliedschaft, Instagram und Facebook prüfen; bestehende benutzerdefinierte Kachelreihenfolgen müssen die neuen IDs automatisch ergänzen.
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
- Koch-/Verantwortlichkeitsnamen in der Terminspalte dürfen nicht fokussierbar oder editierbar sein und beim Antippen keine Tastatur öffnen. Lange Namen müssen sich verkleinern; das Datum soll fett bleiben, ohne Zähler oder Termintext mitzuskalieren.
- Android 8/API 26: App-Start und Navigation testen; die API-27-spezifische helle Navigationsleiste darf dort keinen Ressourcenzugriffsfehler verursachen.
- Play-Vorbereitung: finale zusammengeführte Berechtigungsliste im CI-Artefakt prüfen; keine sensiblen Laufzeitberechtigungen dürfen auftauchen.

## Lokale Daten

Der persönliche `intern.pfvr.ch`-Link wird ausschließlich in den App-Einstellungen auf dem Endgerät gespeichert und darf nicht ins Repository eingecheckt werden. Kachelreihenfolge, bevorzugte Banking-App, Graphdarstellung und gewünschte Teilnehmerliste bleiben ebenfalls lokal.

Cloud-Backup und Android-Device-to-Device-Transfer für die App-Daten sind bewusst deaktiviert bzw. explizit ausgeschlossen, damit persönliche Intern-Links und Teilnehmerzustände nicht automatisch auf ein anderes Gerät übernommen werden.

## Testpaket und Release

Debug-Testpakete verwenden `ch.pfvr.app.test` und seit 0.9.5 einen festen, bewusst öffentlichen Testschlüssel im Repository. Damit sind spätere Test-APKs bei steigendem `versionCode` überinstallierbar. Der Testschlüssel darf niemals für `ch.pfvr.app` oder einen Store-Release verwendet werden; die Produktionssignierung bleibt geheim und separat.

Ein signiertes Play-AAB wird ausschließlich über `.github/workflows/play-release.yml` aus `main` und mit einem separaten Upload-Key erzeugt. `1.0.0` bleibt für den ersten offiziellen Produktionsrelease reserviert.
