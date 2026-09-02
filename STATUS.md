# Status

Stand: Testversion `0.9.1` · aktualisiert 2026-09-02.

## Implementiert / im Test

- Zwei BAFU-Stationskacheln auf Home; Kachel 2 ist in den Einstellungen ein- und ausblendbar.
- Jede aktive Stationskachel zeigt die verfügbaren Diagramme untereinander: Abfluss, Pegel und bei Rheinfelden Wassertemperatur.
- Abflusswert, Status und Abflusskurve verwenden die aktuelle Grenzfarbe; Grenzlinien bleiben auf der Abflussskala sichtbar.
- Live-Aktualisierung ersetzt nur den Live-Bereich und stellt die bisherige Scrollposition wieder her.
- Vereinsbeiz in Trinken, Essen und Feiern gegliedert; Warenkorb steht vor der Auswahl, zeigt Positionen und Total und erlaubt das direkte Entfernen einzelner Positionen.
- Direkte Swiss-QR-Übergabe als temporäres PNG wurde mit Yuh auf einem realen Android-Gerät erfolgreich geprüft.
- Interner Bereich startet standardmäßig in der App-Ansicht und übernimmt den nativen Hell-/Dunkelmodus.
- Persönlicher Intern-Link ausschließlich lokal gespeichert; CI weist bekannte Zugangsmuster zurück.
- Der Build erzeugt immer ein getrenntes Debug-Testpaket und zusätzlich eine release-signierte APK, sofern die geschützten Android-Signing-Secrets im Repository eingerichtet sind.
- Android 16 / API 36 als Target.

## Noch zu verifizieren

- Visuelle Prüfung auf realem Android-Gerät: drei gestapelte Rheinfelden-Diagramme, zwei Basel-Diagramme, Hell/Dunkel und zweite Kachel aus/ein.
- Update von `0.9.1` auf die nächste Testversion mit unveränderter Paket-ID und derselben geschützten Signatur. Der Wechsel von bisherigen wechselnden Debug-Signaturen erfordert einmalig eine Neuinstallation.
- Direkte Swiss-QR-Übergabe mit weiteren Banking-Apps; Android-Intent-Unterstützung allein garantiert noch keine QR-Auswertung durch die Bank.

## Spätere Punkte

- Native Vereinsnews aus WordPress REST/RSS.
- Optionale Trainingsbenachrichtigungen und Homescreen-Widget.
- iOS-Implementierung.
