# Status

Stand: Testversion `0.9.6` · aktualisiert 2026-09-03.

## Implementiert / im Test

- Native Vereinsnews werden über die öffentliche WordPress-REST-API geladen, lokal gecacht und auf Home sowie in einer nativen News-Liste angezeigt.
- Banking-Kompatibilität ist in `BankingAppRegistry` ausgelagert und damit unabhängig vom übrigen Kassen-/UI-Code pflegbar.
- Banking-Apps werden in vier Fähigkeiten eingeteilt: `DIRECT_SHARE`, `FILE_IMPORT`, `SCAN_ONLY` und `UNKNOWN`.
- Dokumentierte bzw. bestätigte Direktübergabe ist hinterlegt für Yuh, PostFinance, Raiffeisen CH, ZKB, BEKB, BLKB, AKB, SGKB, LUKB sowie Cler/Zak.
- neon ist konservativ als Dateiimport hinterlegt. Wenn neon auf einem konkreten Gerät zusätzlich einen Android-Bild-Share registriert, wird dieser Runtime-Nachweis automatisch höher gewichtet und die direkte Übergabe versucht.
- UBS und Revolut sind konservativ als Scanner/manueller Fallback hinterlegt. VR Banking wird ebenfalls so behandelt, solange kein erfolgreicher Swiss-QR-Dateiimport auf dem Gerät nachgewiesen ist.
- Weitere verbreitete Schweizer Apps wie Migros Bank, BCV, BKB, TKB, GKB, ZugerKB, Valiant, Swissquote, Alpian und radicant werden erkannt und zur Laufzeit auf Bildübergabe geprüft.
- Android Package Visibility enthält die bekannten Bankpakete sowie generische Launcher-, Bild- und PDF-Queries. `Alle Apps` bleibt als manueller Fallback verfügbar.
- Bank-spezifische TWINT-Apps werden nicht automatisch als Banking-App einsortiert; TWINT bleibt ein eigener Zahlungsweg.
- Beim Bezahlen richtet sich der Ablauf nach der erkannten Fähigkeit: direkte Bildübergabe, Dateiimport-Hilfe, Scanner/manuelle Übernahme oder dynamische Fallback-Kette.
- Bei direkter Übergabe werden `image/png`, Bild-Öffnen und `image/*` versucht. Bei unbekannten Apps folgt zusätzlich Textübergabe; als letzter Fallback werden Zahlungsdaten kopiert und die gewählte Banking-App geöffnet.
- Bei Dateiimport ohne registrierten Android-Share zeigt PFVR den QR mit den Aktionen `Banking-App öffnen` und `QR speichern`, damit der QR anschließend in der Banking-App hochgeladen werden kann.
- Bei Scanner-only-Apps wird auf demselben Gerät nicht so getan, als könne die Kamera den eigenen Bildschirm scannen: PFVR kopiert stattdessen die Zahlungsdaten und öffnet die App.
- Die Banking-Auswahl zeigt die ermittelte Fähigkeit direkt neben dem App-Namen und unter Einstellungen den aktuellen Kompatibilitätsstatus.
- Die bevorzugte Banking-App wird nur unter Einstellungen → Zahlung verwaltet. Nach der Auswahl entfällt die Zahlungsweg-Kachel in der Kasse; die Kasse beginnt direkt mit dem Warenkorb.
- Die Rhein-Kurzkarten zeigen den BAFU-Messdatenstand und kennzeichnen alte Cache-Daten. Beide Abfluss-Kurzkarten werden bei zwei Stationen auf dieselbe Höhe gesetzt.
- Pro aktiver Station gibt es ein gemeinsames Abfluss-/Pegel-Diagramm mit dynamischen Y-Achsen; Wassertemperatur bleibt eine separate Grafik.
- Live-Aktualisierung baut nur den Live-Bereich neu auf und stellt die Scrollposition nach dem Layout wieder her.
- `ch.pfvr.app.test` wird seit 0.9.5 mit einem reproduzierbaren festen Testschlüssel signiert. 0.9.6 verwendet denselben Zertifikats-Fingerprint und `versionCode 20`, sodass der Updatepfad 0.9.5 → 0.9.6 direkt testbar ist.
- Die Android-CI baut ausschließlich eingecheckte Quellen und prüft Unit-Tests, APK, Paket/Version sowie den festen Test-Zertifikatsfingerprint.
- Vereinsbeiz bleibt in Trinken, Essen und Feiern gegliedert; Warenkorb steht vor der Auswahl, zeigt Positionen und Total und erlaubt direktes Entfernen.
- Interner Bereich startet standardmäßig in der App-Ansicht und übernimmt Hell-/Dunkelmodus.
- Android 16 / API 36 als Target.

## Noch auf realen Geräten zu verifizieren

- 0.9.5 → 0.9.6 als Android-In-Place-Update ohne Deinstallation.
- Banking-Auswahlliste insbesondere mit neon, Revolut, Yuh und VR Banking auf demselben Gerät.
- Tatsächliche Zahlungsübernahme bei PostFinance, ZKB, Raiffeisen und weiteren Schweizer Banking-Apps. Ein registrierter Android-Share-Intent beweist nicht automatisch, dass die Bank den Swiss QR fachlich übernimmt.
- neon: prüfen, ob die aktuelle Android-Version direkte Bildübergabe akzeptiert oder der Dateiimport-Fallback verwendet wird.
- Revolut/UBS/VR Banking: prüfen, ob neuere App-Versionen inzwischen einen nutzbaren Datei-/Share-Import anbieten; die Registry kann dann ohne Umbau des Zahlungsflusses angepasst werden.
- Visuelle Prüfung der Einstellungs-Tabs, Bank-Capability-Texte und gleich hohen Rhein-Kurzkarten auf kleinen Geräten und im Dark Mode.

## Spätere Punkte

- Weitere Aufteilung der noch großen `MainActivity` in klar getrennte UI- und Service-Komponenten.
- Optionaler Ausbau der Banking-Registry mit real bestätigten Geräte-/Banktests statt nur Dokumentation und Android-Capability-Erkennung.
- Optionale Trainingsbenachrichtigungen und Homescreen-Widget.
- iOS-Implementierung.
