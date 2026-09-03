# Pontoniere Rheinfelden app guidance

## Struktur

- `Android/`: aktuelle Android-App und Android-Build.
- `iOS/`: spätere iOS-Implementierung.
- Plattformübergreifende Anforderungen gehören in `PROJECT.md` / `STATUS.md`, nicht nur in Plattformcode.

## Arbeitsregeln

- Reale App, Quellcode, Build und Tests sind maßgeblich.
- Testversionen bleiben `< 1.0.0`; `1.0.0` ist für den ersten offiziellen Release reserviert.
- Persönliche Intern-Links, produktive Signierschlüssel, Tokens und andere personenbezogene Zugangsdaten nie committen. Der persönliche `intern.pfvr.ch`-Link wird ausschließlich lokal in den App-Einstellungen gespeichert.
- Öffentliche Daten möglichst aus stabilen Quellen/API/Feeds beziehen und lokal cachen. Alter/Quelle von Live-Daten sichtbar machen.
- Android und iOS dürfen technisch unterschiedlich umgesetzt werden, sollen aber dieselben fachlichen Kernfunktionen bieten.
- Neue Funktionen zuerst in `STATUS.md` einordnen; relevante Architekturentscheidungen kurz dokumentieren.

- Ausnahme: Der feste öffentliche Testschlüssel für `ch.pfvr.app.test` darf eingecheckt werden; er ist ausdrücklich nicht für Produktion oder Store-Releases bestimmt.
