# Android Changelog

## 0.10.18
- In `Rhein-Grafiken` kann die Pegelachse global zwischen `m ü.M.` und `cm` umgeschaltet werden. Die Auswahl wird lokal gespeichert und gilt für beide Messstationen; Standard bleibt `m ü.M.`.
- Aktueller Pegelwert im Diagrammkopf, rechte Pegelachse, Kurve, Tooltip und Hinweistext verwenden gemeinsam die ausgewählte Einheit.
- `Rhein aktuell` bleibt davon unabhängig: Beide Stationen zeigen dort primär den absoluten Wasserstand in `m ü.M.` und darunter klein den relativen Pegel in `cm`.

## 0.10.17
- Beide Rhein-Messpunkte zeigen den Pegel wieder primär als absoluten BAFU-Wasserstand in `m ü.M.` mit zwei Nachkommastellen.
- Direkt darunter wird in der kompakten Rhein-Übersicht zusätzlich der relative Pegelstand in `cm` klein dargestellt. Basel verwendet den Pegelnullpunkt 240.00 m ü.M.; Rheinfelden den historischen BAFU-Pegelnullpunkt 260.00 m ü.M.
- Die Zeitstempel bleiben durch die gleich hohen Karten und den flexiblen Restbereich am unteren Kartenrand auf gleicher Höhe.

## 0.10.16
- `Rhein aktuell` zeigt bei beiden Messpunkten Abfluss und Pegel als eigene, klar beschriftete Werte. Basel-Rheinhalle verwendet für den Pegel weiterhin `cm`, Rheinfelden `m ü.M.`.
- Rheinfelden zeigt die Wassertemperatur weiterhin zusätzlich unter den beiden Hauptwerten.
- Die Messdaten-Zeitstempel werden in den gleich hohen Rhein-Kurzkarten an den unteren Kartenrand gedrückt und liegen dadurch auf derselben Höhe.

## 0.10.15
- Der Pegel Basel-Rheinhalle wird in der App konsequent als operativer Schifffahrtspegel in Zentimetern dargestellt. Der BAFU-Rohwert `W` in m ü.M. wird mit dem offiziellen Bezug `247.20 m ü.M. = 720 cm` auf den Pegel umgerechnet (`(W - 240.00 m) × 100`).
- Die stationsabhängige Pegel-Darstellung ist zentralisiert: Basel nutzt `cm` ohne Nachkommastellen, Rheinfelden bleibt beim absoluten Wasserstand in `m ü.M.` mit zwei Nachkommastellen. Kurzkarte, Detailwerte, Verlauf und Tooltips verwenden dieselbe Logik.
- Beim Verschieben, Ein-/Ausblenden oder Zurücksetzen von Kacheln wird die aktuelle vertikale Position der Kachelverwaltung nach dem Neuaufbau wiederhergestellt. Die Ansicht springt dadurch nicht mehr nach jeder Änderung an den Anfang.

## 0.10.14
- Die App liest Teilnehmernamen in der Originaltabelle jetzt auch direkt aus Bearbeiten-/Namens-Controls wie `✎ Kougionis Eleni` oder entsprechenden `input`-/Button-Beschriftungen. Dadurch entstehen nach `Alle anzeigen` nicht mehr massenhaft generische `Teilnehmer N`-Spalten, wenn die Originalzeile einen echten Namen enthält.
- Alte lokal gespeicherte Zeilennamen werden nur noch indexbasiert verwendet, wenn die aktuelle Quelltabelle exakt gleich viele Personenzeilen besitzt. Eine stark veränderte Originaltabelle kann damit keine alten Namen auf falsche Statuszeilen verschieben.
- Große beziehungsweise deutlich erweiterte Personenlisten aus der Originalansicht werden als neue Quelle der Wahrheit übernommen. Insbesondere nach `Alle anzeigen` werden lokale Ausblendungen verworfen und alle aktuell vorhandenen Originalzeilen in derselben Reihenfolge in die App-Matrix übertragen.
- Statuswerte bleiben an der jeweiligen Original-Personenzeile gekoppelt; die mobile Transponierung ändert nur die Darstellung, nicht die Zuordnung von Person, Termin und Auswahl.

## 0.10.13
- Tabellenkopf erhält einen Viewport-Fallback: Falls ein Website-Wrapper das CSS-Sticky-Verhalten verhindert, bleibt eine fixe Spiegel-Kopfzeile mit `Termin` und Teilnehmernamen beim vertikalen Scrollen sichtbar.
- Der fixe Kopf übernimmt die horizontale Position ausschließlich vom Tabellenkörper; die Kopfzeile selbst bleibt nicht per Finger scrollbar.
- Lange Koch-/Verantwortlichkeitsnamen in der linken Terminspalte werden auf die verfügbare Breite herunter skaliert, ohne Datum, Zähler oder Terminbeschreibung zu verkleinern.
- Die Zweitbestätigung von `Aus Initiallink neu aufbauen` wird nach fünf Sekunden sowie beim Schließen, erneuten Öffnen oder einer anderen Aktion in der Personenverwaltung zuverlässig zurückgesetzt.

## 0.10.12
- Die Personenverwaltung öffnet auch dann eine Fallback-Ansicht, wenn die externe PFVR-Seite keine verwertbare Teilnehmermatrix oder kein Hinzufügen-Control liefert.
- `Aus Initiallink neu aufbauen` löscht gezielt die lokal gespeicherten Personen- und Wiederherstellungszustände und lädt anschließend den gespeicherten persönlichen Basis-/Initiallink neu. Der Initiallink selbst bleibt erhalten.
- Der Neuaufbau erfordert eine zweite Betätigung innerhalb von fünf Sekunden, damit die lokale Personenansicht nicht versehentlich zurückgesetzt wird.

## 0.10.11
- Website-Bulk-Aktionen wie `Alle Personen anzeigen`, `Alle anzeigen` und entsprechende Hinzufügen-/Einblenden-Varianten werden in der App-Ansicht global ausgeblendet und zusätzlich gegen Auslösung blockiert.
- Die Tabellenkopfzeile mit `Termin` und Teilnehmernamen bleibt vertikal stehen, besitzt keine eigene Touch-Scrollfläche und wird horizontal ausschließlich vom scrollbaren Tabellenkörper nachgeführt.
- Die Personenverwaltung bleibt bei fehlendem Website-Hinzufügen-Control für vorhandene Personen nutzbar.

## 0.10.10
- `Termin` und Teilnehmernamen wurden in eine feste Kopfzeile verschoben; redundante Teilnehmernamen über jedem Status-Control entfallen.
- An-/Abmelde-Controls besitzen größere Touch-Flächen und größere Schrift.
- Horizontale Kopf-/Tabellenposition und Scrollzustand werden gemeinsam wiederhergestellt.

## 0.10.9
- `Entfernen` ist keine reine lokale Ausblendung mehr: Die App setzt die interne Seite automatisch auf den persönlichen Basislink zurück und stellt anschließend nur die verbleibenden Zusatzpersonen über die echten Website-Controls wieder her.
- Dadurch wird die entfernte Person auch aus der Originalansicht genommen und der fehlerhafte Grid-Zwischenzustand nach lokalem Ausblenden vermieden; kein manueller Reload ist nötig.
- Der Zurückpfeil neben dem Logo ist im internen Bereich entfernt. Android-Zurück navigiert dort direkt zu Home und kann nicht mehr durch WebView-Historie eine Personenänderung rückgängig machen.
- Im Originalmodus entfällt zusätzlich der redundante innere Zurück-Button; `Personen` ist ausschließlich in der App-Ansicht sichtbar.

## 0.10.8
- Teilnehmernamen werden robust aus getrennten DOM-Textteilen, CamelCase und vorhandenen Personen-IDs gelesen und als `Nachname, Vorname` dargestellt.
- Der App-Modus verwendet den linken Werkzeugleisten-Button als direkt erreichbare modale Personenverwaltung; Hinzufügen, Entfernen und Wieder-Einblenden liegen an einer Stelle.
- `Alle anzeigen/hinzufügen` wird im App-Modus nicht mehr gespiegelt. Die Funktion bleibt ausschließlich in der unveränderten Originalansicht verfügbar.
- Lokales Entfernen blendet Personenspalten nur aus und löscht die darin enthaltenen echten Website-Controls nicht mehr aus dem DOM.
- Alte generische Namen aus 0.10.7 werden bei der Migration verworfen; letzte erkannte Zeilennamen dienen nur noch als Fallback.

## 0.10.7
- Aktuell in der Originaltabelle vorhandene Personenzeilen sind die Quelle der Wahrheit und werden nicht mehr durch eine exakte lokale Namens-Whitelist entfernt.
- Auswahltext und Tabellenname werden über gespeicherten Optionswert sowie tokenbasierte Namensnormalisierung zusammengeführt; Vorname/Nachname-Reihenfolge darf abweichen.
- Lokales Entfernen wird separat als Ausblenden gespeichert, damit nur bewusst entfernte Personen unterdrückt werden.
- Regression behoben: Eine hinzugefügte Person blitzte kurz auf, blieb in der Originalansicht vorhanden und verschwand dennoch wieder aus der mobilen Matrix.

## 0.10.6
- Personen-Hinzufügen verschiebt den originalen Website-Select nicht mehr aus seinem Formular. Ein App-Proxy steuert das echte Control am ursprünglichen DOM-Ort.
- Server-/DOM-seitig neu auftauchende Teilnehmerzeilen werden ohne erzwungenen Voll-Reload in die vorhandene mobile Matrix synchronisiert; auch `Alle anzeigen` wird über das originale Website-Control ausgelöst.

## 0.10.5
- Gewünschte Personenliste der internen App-Ansicht wird dauerhaft im WebView-Origin gespeichert und nach App-Neustart über den echten Website-Select schrittweise wiederhergestellt.
- Zusätzliche Personen können lokal aus der eigenen Übersicht entfernt werden; die Standard-/eigene Person bleibt geschützt. Das verändert keine serverseitigen An-/Abmeldedaten.
- Teilnehmername steht zusätzlich in jeder Statuszelle. Lange Namen werden auf maximal zwei Zeilen begrenzt und moderat verkleinert; die gedeckelte Spaltenbreite für mindestens zwei sichtbare Personen bleibt bestehen.
- Gemeinsames horizontales Scrollen der festen Personenspalten bleibt erhalten.

## 0.10.4
- Interne Terminansicht als gemeinsam horizontal scrollende Matrix: Termine/Kochinfo links, jede Person als feste Spalte über alle Tage. Mindestens zwei Personenspalten sind gleichzeitig sichtbar.
- `+ / − Person` zeigt neben dem Hinzufügen auch vorhandene originale Website-Aktionen aus der Personen-Spalte; vorhandene Entfernen-Aktionen werden dadurch wieder erreichbar.
- Keine synthetische Löschfunktion: Entfernen wird nur angeboten, wenn die interne Website dafür selbst ein Control liefert.

## 0.10.3
- Statusfarben der mobilen An-/Abmeldeansicht werden nach Button-/Select-Änderungen aus dem aktuellen echten Control-Wert neu berechnet.
- Formulierungen wie `Ich komme, mit Essen` und `Ich komme, ohne Essen` werden ebenfalls korrekt erkannt.
- Selects werden anhand der tatsächlich gewählten Option gefärbt; MutationObserver und verzögerte Restyles aktualisieren die Darstellung ohne erzwungenen Seiten-Reload.

## 0.10.2
- Interne An-/Abmeldematrix mobil transponiert: Termine untereinander, Termin-/Kochinfo links und Teilnehmer rechts.
- Teilnehmer werden ausschließlich aus der Personen-Spalte übernommen; Namen in Termin-Metadaten bleiben Termin-/Kochinfo.
- Pro Tag/Person werden die realen Website-Controls verwendet; keine synthetischen `Mit Essen`/`Ohne Essen`-Optionen.
- `Person zur Liste hinzufügen` als globale, einklappbare `+ Person`-Verwaltung oberhalb der Termine.
- Von der App erzwungenen Voll-Reload nach Statusklick entfernt; Scroll- und horizontale Position werden über serverseitige Navigationen hinweg wiederhergestellt.

## 0.10.1
- Interne Terminspalten erhalten eine feste lesbare Breite statt auf wenige Zeichen zusammenzuschrumpfen.
- Breite An-/Abmeldetabellen liegen in einem horizontal scrollbaren Container.
- Wortumbruch erfolgt wieder an Wortgrenzen; `overflow-wrap:anywhere` wurde entfernt.
- Statusblöcke wie `Ohne Essen` bleiben vom Termintext getrennt.

## 0.10.0
- Personalisierbare Kachelreihenfolge und Sichtbarkeit für Home, Kasse und Verein.
- Eigenes Einstellungsmenü mit Bereichsauswahl, Hoch/Runter, Ein-/Ausblenden und Standard-Reset.
- Warenkorb als fixierte, immer sichtbare erste Kassenkachel; Beiz-Kategorien und Zahlungsblöcke frei anordenbar.
- Verein auf kompakte Aktionskacheln umgestellt; Geschichte öffnet eine eigene Detailseite statt dauerhaft alle Meilensteine anzuzeigen.
- Interne An-/Abmeldeseite trennt Statuspräfixe wie `Ohne Essen` vom folgenden Termintext und behebt dadurch Darstellungen wie `Ohne EssenSchiffe`.
- Tile-IDs und Layoutmigration in `TileLayoutStore`, WebView-Aufbereitung in `InternalAttendanceSkin` ausgelagert.

## 0.9.8
- Wiederverwendbare Zahlungs-QR-Dateinamen im Format `PFVR_12.50CHF.png`.

## 0.9.7
- Share-first-Zahlungsübergabe für jede ausgewählte Banking-App; statische Fähigkeiten steuern nur noch Fallbacks.

## 0.9.6
- Banking-Kompatibilität in eine zentrale Registry ausgelagert.
- Capability-basierte Fallbacks für direkten Share, Dateiimport, Scanner und unbekannte Apps.

## 0.9.5
- Native Vereinsnews via WordPress REST mit Cache.
- Explizite Erkennung verbreiteter Schweizer Banking-Apps, darunter neon und Revolut.
- TWINT aus der automatischen Banking-App-Liste getrennt.
- Stabiler Test-Signierschlüssel für künftig überinstallierbare Test-Updates.
- Historische Entwicklungsbranches bereinigt; 0.9.4 bleibt als Rückfallstand.

## 0.9.4
- Banking-Auswahl auf installierte Banking-Apps erweitert; nicht automatisch erkannte Apps können über „Alle Apps“ gewählt werden.
- Zahlungsübergabe versucht mehrere Android-Importwege und öffnet als Fallback die ausgewählte Banking-App mit kopierten Zahlungsdaten.
- Zahlungsweg-Kachel in der Kasse wird nach erfolgter App-Auswahl nicht mehr angezeigt.
- BAFU-Messdatenstand ist wieder direkt in den Rhein-Kurzkarten sichtbar.
- Öffentliche PFVR-Newsquellen über WordPress REST und RSS als Basis für die nächste native News-Integration verifiziert.
