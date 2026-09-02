# Status

Stand: Testversion `0.9.2` · aktualisiert 2026-09-02.

## Implementiert / im Test

- Rhein-Übersicht: aktuelle Abflüsse der aktiven Stationen kompakt nebeneinander; Status und Wert folgen Niedrig/Gut/Warn/Alarm.
- Rheinfelden zeigt die aktuelle Wassertemperatur direkt in der oberen Stationskarte; ein eigener Temperaturgraph entfällt.
- Pro aktiver Station ein gemeinsames Diagramm für Abfluss und Pegel mit zwei dynamischen Y-Achsen; Basel nutzt für Pegel cm, Rheinfelden m ü.M.
- Zweite Rhein-Kachel bleibt in den Einstellungen ein- und ausblendbar.
- Live-Aktualisierung baut nur den Live-Bereich neu auf und stellt die Scrollposition zusätzlich nach dem Layout nochmals her.
- Banking-App wird oben in der Kasse festgelegt. Die Auswahl speichert nur die App und öffnet sie nicht. Erst der Zahlungsbutton übergibt den temporären Swiss QR.
- Vereinsbeiz bleibt in Trinken, Essen und Feiern gegliedert; Warenkorb steht vor der Auswahl, zeigt Positionen und Total und erlaubt direktes Entfernen.
- Direkte Swiss-QR-Übergabe wurde mit Yuh real erfolgreich geprüft; weitere Banken bleiben geräteabhängig.
- Interner Bereich startet standardmäßig in der App-Ansicht und übernimmt Hell-/Dunkelmodus.
- Android 16 / API 36 als Target.

## Noch zu verifizieren

- Visuelle Prüfung der kombinierten Doppelachsen-Diagramme auf realem Android-Gerät, insbesondere kleine Displays und Dark Mode.
- Direkte QR-Übergabe mit weiteren Banking-Apps.
- Updatepfad: 0.9.1 wurde mangels geschützter Signing-Secrets nur als getrenntes Debug-Testpaket gebaut. Für dauerhaft installierbare Updates muss einmalig eine stabile private Test-/Release-Signatur eingerichtet werden; ein wechselnder GitHub-Runner-Debug-Key ist nicht updatefähig.

## Spätere Punkte

- Native Vereinsnews aus WordPress REST/RSS.
- Optionale Trainingsbenachrichtigungen und Homescreen-Widget.
- iOS-Implementierung.
