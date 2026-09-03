# Android Changelog

## 0.9.8
- Gespeicherte und temporär geteilte Swiss-QR-Bilder verwenden ein einheitliches, suchbares Dateinamensschema.
- Betragsbezogene QR-Dateien heißen z. B. `PFVR_12.50CHF.png`; offene Beträge `PFVR_offenCHF.png`.
- Dateinamenlogik in `PaymentQrFileName` ausgelagert und mit Unit-Tests abgesichert.

## 0.9.7
- Banking-Handoff auf Share-first umgestellt: jede Banking-App erhält zuerst die Bildübergabeversuche, unabhängig von der statischen Capability-Einstufung.

## 0.9.6
- Banking-Kompatibilität in `BankingAppRegistry` ausgelagert und um Capability-basierte Fallbacks erweitert.

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
