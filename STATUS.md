# Status

Stand: Testversion `0.9.3` · aktualisiert 2026-09-02.

## Implementiert / im Test

- Einstellungen sind in die Bereiche Allgemein, Rhein und Zahlung gegliedert.
- Die bevorzugte Banking-App wird nur noch unter Einstellungen → Zahlung verwaltet. Die Auswahl speichert die App, startet sie aber nicht.
- Die Kasse zeigt den gewählten Zahlungsweg kompakt an. Ohne Auswahl erscheint ein deutlicher Hinweis mit direktem Sprung zur passenden Einstellung.
- Der Banking-Dialog bietet nur installierte, als Banking-App erkennbare Apps an, die PNG-Bilder über Androids Teilen-Funktion annehmen.
- Die beiden Abfluss-Kurzkarten auf Home werden bei aktiver zweiter Station auf dieselbe Höhe gesetzt; maßgeblich ist die höhere Karte mit allen Zusatzinformationen.
- Pro aktiver Station bleibt ein gemeinsames Diagramm für Abfluss und Pegel mit zwei dynamischen Y-Achsen erhalten; Stationen mit Temperaturdaten erhalten darunter eine separate Wassertemperaturgrafik.
- Live-Aktualisierung baut nur den Live-Bereich neu auf und stellt die Scrollposition nach dem Layout wieder her.
- Nicht mehr verwendete Einzelgrafik-, Metrik- und generische App-Startlogik wurde entfernt.
- Die Android-CI baut ausschließlich eingecheckte Quellen; historische versionsgebundene Patch-Workflows und Migrationsskripte wurden entfernt.
- Vereinsbeiz bleibt in Trinken, Essen und Feiern gegliedert; Warenkorb steht vor der Auswahl, zeigt Positionen und Total und erlaubt direktes Entfernen.
- Interner Bereich startet standardmäßig in der App-Ansicht und übernimmt Hell-/Dunkelmodus.
- Android 16 / API 36 als Target.

## Noch zu verifizieren

- Visuelle Prüfung der gleich hohen Kurzkarten und der Einstellungs-Tabs auf kleinen realen Android-Geräten und im Dark Mode.
- Direkte QR-Übergabe mit weiteren Banking-Apps; Yuh ist real bestätigt. Nicht mehr installierte oder nicht mehr kompatible bevorzugte Apps werden als nicht verfügbar behandelt.
- Updatepfad: Dauerhaft installierbare Updates benötigen einmalig eine stabile private Test-/Release-Signatur. Ein wechselnder GitHub-Runner-Debug-Key ist nicht updatefähig.

## Spätere Punkte

- Weitere Aufteilung der noch großen `MainActivity` in klar getrennte UI- und Service-Komponenten.
- Native Vereinsnews aus WordPress REST/RSS.
- Optionale Trainingsbenachrichtigungen und Homescreen-Widget.
- iOS-Implementierung.
