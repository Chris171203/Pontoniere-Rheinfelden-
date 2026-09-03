# Android Changelog

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
