# Android Changelog

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
