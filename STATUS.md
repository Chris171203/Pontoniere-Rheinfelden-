# Status

Stand: Testversion `0.9.5` · aktualisiert 2026-09-03.

## Implementiert / im Test

- Native Vereinsnews werden über die öffentliche WordPress-REST-API geladen, lokal gecacht und auf Home sowie in einer nativen News-Liste angezeigt.
- Die Banking-Erkennung enthält explizite Paket-Sichtbarkeit für verbreitete Schweizer Banken und Finanzapps, darunter neon und Revolut, plus generischen App-Fallback.
- Bank-spezifische TWINT-Apps werden nicht mehr automatisch als Banking-App einsortiert; TWINT bleibt ein eigener Zahlungsweg.
- `ch.pfvr.app.test` wird ab 0.9.5 mit einem festen öffentlichen Testschlüssel signiert, damit spätere Testversionen mit höherem `versionCode` überinstalliert werden können. Produktionssignierung bleibt separat.
- Einstellungen sind in die Bereiche Allgemein, Rhein und Zahlung gegliedert.
- Die bevorzugte Banking-App wird nur noch unter Einstellungen → Zahlung verwaltet. Die Auswahl speichert die App, startet sie aber nicht.
- Ohne gewählte Banking-App zeigt die Kasse einen deutlichen Hinweis mit direktem Sprung zur passenden Einstellung; nach der Auswahl entfällt diese Zahlungsweg-Kachel und die Kasse beginnt direkt mit dem Warenkorb.
- Die Banking-Auswahl berücksichtigt installierte Banking-Apps unabhängig davon, ob sie einen PNG-Share-Import registrieren; nicht automatisch erkannte Apps können über „Alle Apps“ manuell gewählt werden.
- Beim Bezahlen werden nacheinander direkter Swiss-QR-Bildimport, Bild-Öffnen, generisches Bild-Teilen und Textübergabe an die gewählte App versucht; als letzter Fallback wird die Banking-App geöffnet und die Zahlungsdaten werden kopiert.
- Die beiden Abfluss-Kurzkarten auf Home werden bei aktiver zweiter Station auf dieselbe Höhe gesetzt; maßgeblich ist die höhere Karte mit allen Zusatzinformationen.
- Die Rhein-Kurzkarten zeigen wieder direkt den BAFU-Messdatenstand (Uhrzeit) und kennzeichnen alte Cache-Daten.
- Pro aktiver Station bleibt ein gemeinsames Diagramm für Abfluss und Pegel mit zwei dynamischen Y-Achsen erhalten; Stationen mit Temperaturdaten erhalten darunter eine separate Wassertemperaturgrafik.
- Live-Aktualisierung baut nur den Live-Bereich neu auf und stellt die Scrollposition nach dem Layout wieder her.
- Nicht mehr verwendete Einzelgrafik-, Metrik- und generische App-Startlogik wurde entfernt.
- Die Android-CI baut ausschließlich eingecheckte Quellen; historische versionsgebundene Patch-Workflows und Migrationsskripte wurden entfernt.
- Vereinsbeiz bleibt in Trinken, Essen und Feiern gegliedert; Warenkorb steht vor der Auswahl, zeigt Positionen und Total und erlaubt direktes Entfernen.
- Interner Bereich startet standardmäßig in der App-Ansicht und übernimmt Hell-/Dunkelmodus.
- Android 16 / API 36 als Target.

## Geprüft / nächste Integration

- Öffentliche Vereinsnews sind nativ abrufbar. `https://www.pfvr.ch/wp-json/wp/v2/posts` liefert WordPress-REST-JSON mit ID, Datum, Titel, Kurztext und Artikel-Link; `https://www.pfvr.ch/feed/` steht zusätzlich als RSS-Fallback zur Verfügung.
- Für die App ist WordPress REST die bevorzugte News-Quelle: JSON lokal cachen, Datenalter sichtbar machen, auf Home die neuesten 1–3 Beiträge kompakt zeigen und unter Verein eine vollständige native News-Liste anbieten. Ein Tipp auf einen Beitrag öffnet den Originalartikel in der bestehenden PFVR-App-Webansicht.
- Bilder dürfen optional bleiben: die zuletzt geprüften Beiträge hatten kein `featured_media`; das News-Layout darf daher nicht von Titelbildern abhängen.

## Noch zu verifizieren

- Visuelle Prüfung der gleich hohen Kurzkarten und der Einstellungs-Tabs auf kleinen realen Android-Geräten und im Dark Mode.
- Direkte QR-Übergabe mit weiteren Banking-Apps; Yuh ist real bestätigt. Bei Apps ohne unterstützten Import kann Android nur die App öffnen und die Zahlungsdaten als Fallback bereitstellen.
- Updatepfad: Dauerhaft installierbare Updates benötigen einmalig eine stabile private Test-/Release-Signatur. Ein wechselnder GitHub-Runner-Debug-Key ist nicht updatefähig.

## Spätere Punkte

- Weitere Aufteilung der noch großen `MainActivity` in klar getrennte UI- und Service-Komponenten.
- Native Vereinsnews nach dem oben geprüften REST-Konzept umsetzen.
- Optionale Trainingsbenachrichtigungen und Homescreen-Widget.
- iOS-Implementierung.
