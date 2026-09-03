# Status

Stand: Testversion `0.9.7` · aktualisiert 2026-09-03.

## Implementiert / im Test

- Native Vereinsnews werden über die öffentliche WordPress-REST-API geladen, lokal gecacht und auf Home sowie in einer nativen News-Liste angezeigt.
- Banking-Kompatibilität ist in `BankingAppRegistry` ausgelagert und damit unabhängig vom übrigen Kassen-/UI-Code pflegbar.
- Banking-Apps werden in vier Fallback-Profile eingeteilt: `DIRECT_SHARE`, `FILE_IMPORT`, `SCAN_ONLY` und `UNKNOWN`.
- Wichtig ab 0.9.7: die Profile entscheiden nicht mehr darüber, ob Share versucht wird. Für jede ausgewählte Banking-App wird immer zuerst die QR-Bildübergabe probiert: `ACTION_SEND image/png`, danach `ACTION_VIEW image/png`, danach `ACTION_SEND image/*`.
- Erst wenn alle QR-Bildwege auf dem konkreten Gerät nicht angeboten werden, greift das Profil: Dateiimport-Hilfe, Scanner/manuelle Übernahme oder dynamische Text-/App-Fallbacks.
- Dokumentierte bzw. bestätigte Direktübergabe ist hinterlegt für Yuh, PostFinance, Raiffeisen CH, ZKB, BEKB, BLKB, AKB, SGKB, LUKB sowie Cler/Zak.
- neon ist als Dateiimport-Fallback hinterlegt; trotzdem wird auch bei neon immer zuerst die direkte Bildübergabe versucht.
- UBS, Revolut und VR Banking verwenden den Scanner/manuellen Fallback nur dann, wenn die installierte App-Version keinen der drei Bild-Handoff-Wege anbietet.
- Weitere verbreitete Schweizer Apps wie Migros Bank, BCV, BKB, TKB, GKB, ZugerKB, Valiant, Swissquote, Alpian und radicant werden erkannt und erhalten dieselben Share-first-Versuche.
- Android Package Visibility enthält die bekannten Bankpakete sowie generische Launcher-, Bild- und PDF-Queries. `Alle Apps` bleibt als manueller Fallback verfügbar.
- Bank-spezifische TWINT-Apps werden nicht automatisch als Banking-App einsortiert; TWINT bleibt ein eigener Zahlungsweg.
- Bei Dateiimport ohne registrierten Android-Share zeigt PFVR den QR mit den Aktionen `Banking-App öffnen` und `QR speichern`, damit der QR anschließend in der Banking-App hochgeladen werden kann.
- Bei Scanner-Fallbacks werden Zahlungsdaten kopiert und die App geöffnet, aber erst nachdem die Bildübergabe tatsächlich versucht wurde.
- Die Banking-Auswahl zeigt das jeweilige Fallback-Profil; ein tatsächlich registrierter Android-Bild-Share auf dem Gerät wird weiterhin höher gewichtet als die statische Registry.
- Die bevorzugte Banking-App wird nur unter Einstellungen → Zahlung verwaltet. Nach der Auswahl entfällt die Zahlungsweg-Kachel in der Kasse; die Kasse beginnt direkt mit dem Warenkorb.
- Die Rhein-Kurzkarten zeigen den BAFU-Messdatenstand und kennzeichnen alte Cache-Daten. Beide Abfluss-Kurzkarten werden bei zwei Stationen auf dieselbe Höhe gesetzt.
- Pro aktiver Station gibt es ein gemeinsames Abfluss-/Pegel-Diagramm mit dynamischen Y-Achsen; Wassertemperatur bleibt eine separate Grafik.
- Live-Aktualisierung baut nur den Live-Bereich neu auf und stellt die Scrollposition nach dem Layout wieder her.
- `ch.pfvr.app.test` wird seit 0.9.5 mit einem reproduzierbaren festen Testschlüssel signiert. 0.9.7 verwendet denselben Zertifikats-Fingerprint und `versionCode 21`.
- Die Android-CI baut ausschließlich eingecheckte Quellen und prüft Unit-Tests, APK, Paket/Version sowie den festen Test-Zertifikatsfingerprint.
- Vereinsbeiz bleibt in Trinken, Essen und Feiern gegliedert; Warenkorb steht vor der Auswahl, zeigt Positionen und Total und erlaubt direktes Entfernen.
- Interner Bereich startet standardmäßig in der App-Ansicht und übernimmt Hell-/Dunkelmodus.
- Android 16 / API 36 als Target.

## Noch auf realen Geräten zu verifizieren

- 0.9.6 → 0.9.7 als Android-In-Place-Update ohne Deinstallation.
- Banking-Auswahlliste insbesondere mit neon, Revolut, Yuh und VR Banking auf demselben Gerät.
- Tatsächliche Zahlungsübernahme bei Yuh, neon, Revolut, PostFinance, ZKB, Raiffeisen und weiteren Apps. Ein registrierter Android-Share-Intent beweist nicht automatisch, dass die Bank den Swiss QR fachlich übernimmt.
- neon: prüfen, ob die aktuelle Android-Version den direkten Bild-Handoff akzeptiert oder der Dateiimport-Fallback erscheint.
- Revolut/UBS/VR Banking: prüfen, ob die aktuelle App-Version einen der Share-first-Wege akzeptiert; nur wenn alle drei fehlen, wird der Scanner/manuelle Fallback genutzt.
- Visuelle Prüfung der Einstellungs-Tabs, Bank-Capability-Texte und gleich hohen Rhein-Kurzkarten auf kleinen Geräten und im Dark Mode.

## Spätere Punkte

- Weitere Aufteilung der noch großen `MainActivity` in klar getrennte UI- und Service-Komponenten.
- Optionaler Ausbau der Banking-Registry mit real bestätigten Geräte-/Banktests statt nur Dokumentation und Android-Capability-Erkennung.
- Optionale Trainingsbenachrichtigungen und Homescreen-Widget.
- iOS-Implementierung.
