# Pontoniere Rheinfelden App

Mobile Vereins-App für den Pontonierfahrverein Rheinfelden: Vereinstermine, Wetter, Rhein-Livedaten, interne An-/Abmeldung und Vereinsbeiz.

## Aktueller Stand

Android-Testversion **0.15.0**, Versionscode **68**. Der freigegebene Funktionsstand wird in [STATUS.md](STATUS.md) mit den Prüfnachweisen geführt. iOS-Testversion **0.15.0**, Build **2**, übernimmt dieselben Vereinsinhalte mit nativer Navigation. Plattformnachweise stehen in der [Abnahme 0.15.0](docs/checks-0.15.0.md).

- Vereinsübersicht mit kurzem Einstieg, direkt sichtbarem Sommer-/Wintertraining, Treffpunkten und Kalenderzugang.
- Native Themenseiten für Boote, Fahrtechnik, Nachwuchs, Vorstand, Geschichte, Vereinsleben und Kontakt; Texte und Bilder aus der öffentlichen Homepage mit lokalem Cache.
- Telefon, Navigation, E-Mail, Instagram und Facebook als zentrierte Symbole am Seitenende.
- Wetter zum nächsten relevanten Vereinsanlass, 3-Tage-Prognose und BAFU-Rheinwerte mit Verläufen und sichtbarem Datenstand.
- Native Termine und News; interne An-/Abmeldung mit den echten Website-Controls und deren Zustandsfarben.
- Persistenter Vereinsbeiz-Warenkorb, Swiss QR, Zahlungsübergabe und ausdrückliche Rückkehrbestätigung.
- Deutsch/Schwiizerdütsch, Hell/Dunkel und anpassbare Navigation. Externe Vereinsinhalte und Namen bleiben unverändert.

## Struktur

- [Android](Android/README.md): native App, Build und Testsignierung.
- [iOS](iOS/README.md): native SwiftUI-App, Simulatorprüfungen und Gerätearchiv.
- [Projekt](PROJECT.md): plattformübergreifende Anforderungen.
- [Status](STATUS.md): aktuelle Arbeiten, Nachweise und Grenzen.
- [Statusarchiv](docs/status-history-before-0.15.0.md): bisherige Versionen und Entscheidungen.
- [PlayStore](PlayStore/README.md): Store-, Datenschutz- und Release-Unterlagen.
- `decisions/`: Architektur- und Sicherheitsentscheidungen.

## Datenschutz und Versionierung

Persönliche Intern-Links und Zugänge werden ausschliesslich lokal gespeichert und gehören nicht ins Repository. Öffentliche Daten werden lokal gecacht. Testversionen bleiben unter `1.0.0`; diese Version ist für den ersten offiziellen Produktionsrelease reserviert. Die Testsignierung ist von der Produktionssignierung getrennt.
