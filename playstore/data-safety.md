# Data Safety – Arbeitsentwurf

Die endgültigen Antworten müssen im Play Console Formular anhand der dann gültigen Definitionen bestätigt werden.

## App-eigene Erhebung

- keine Werbung
- keine Analytics
- kein Crash-Reporting-SDK
- kein eigener Backend-Dienst
- kein Verkauf von Daten

## Datenübertragungen durch Funktionen

### Interner PFVR-Bereich

Beim Öffnen des persönlichen Links und bei An-/Abmeldungen können folgende Daten direkt an `intern.pfvr.ch` übertragen werden:

- Nutzerkennung bzw. personenbezogener Zugangslink
- Vereins-/Termininteraktionen
- Auswahl mit oder ohne Essen
- Cookies und technische Verbindungsdaten

Voraussichtliche Einstufung im Formular: funktional erforderlich; nicht für Werbung; Übertragung verschlüsselt per HTTPS. Ob Google dies als „vom Entwickler erhoben“ oder als eingebettete Webdienstfunktion einstuft, muss im konkreten Formular geprüft werden.

### Öffentliche Datenquellen

Kalender-, Wetter- und BAFU-Abfragen übermitteln technisch notwendige Verbindungsdaten wie IP-Adresse und User-Agent an die jeweiligen Anbieter. Die Inhaltsdaten werden lokal gecacht.

### Zahlungen

Die App erzeugt Zahlungsdaten lokal und öffnet externe Apps. Sie erhält keine Kontodaten und keine Zahlungsbestätigung.

## Löschung

Die App erstellt kein eigenes Benutzerkonto. Lokal gespeicherte Daten werden durch Löschen der App-Daten oder Deinstallation entfernt. Daten im internen Vereinssystem unterliegen den Prozessen des Vereins.
