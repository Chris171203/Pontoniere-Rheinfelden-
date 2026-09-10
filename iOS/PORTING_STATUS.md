# iOS-Portierung – Arbeitsstand

Aktualisiert: 2026-09-10. Android-Referenz: 0.12.6, Commit `c617bae7c1f00fbb1136621166387ed814652794`.

## Ergebnis

Native SwiftUI-Entwicklungsversion für iPhone und iPad ab iOS 17 umgesetzt. Der Produkt- und Testcode `2bf0749f490ffe0aee5f050aec7ee6583e79717d` hat die vollständige konfigurierte Simulator-Abnahme bestanden: [CI 34513741048](https://github.com/Chris171203/Pontoniere-Rheinfelden-/actions/runs/34513741048). Nach diesem Stand folgen nur Dokumentationsänderungen. Arbeitsbranch: `codex/ios-port-0.12.6`, [Draft-PR #32](https://github.com/Chris171203/Pontoniere-Rheinfelden-/pull/32). Main und Android wurden nicht geändert.

## Arbeitspakete

| Paket | Umfang | Verantwortlich | Ergebnis |
|---|---|---|---|
| AP1 | Warenkorb, Zahlungszustand, Swiss-QR, Pegelregeln, Kacheln, Freigabe, Sprache | Core-Agent | Implementiert; Fach- und QR-Tests bestanden |
| AP2 | Wetter, Rhein, Kalender, News, Cache und Fehlerfälle | Data-Agent | Implementiert; Fachtests und vier echte Live-Quellenprüfungen bestanden |
| AP3 | Native Screens, Navigation, Einstellungen und Zahlungsübergabe | UI-Agent | Implementiert; alle elf Bedienabläufe auf beiden iPhones bestanden |
| AP4 | Interne App-/Originalansicht, Keychain, echte Website-Controls | Internal-Agent | Sechs WebKit- und zwei Sicherheits-/Keychain-Fälle auf beiden iPhones bestanden; Originalbilder geprüft |
| AP5 | Xcode-Projekt, CI, Unit-/WebKit-/Simulator-Tests, Screenshots | Build/Test-Agent | Debug/Release und komplette konfigurierte Testmatrix bestanden |
| AP6 | Unabhängiger Android-Abgleich und Review | Review-Agent | Sieben materielle Befunde sowie ein visueller Achsenbefund korrigiert; Bildnachprüfung durchgeführt |
| AP1b | Hintergrundaktualisierung, Ablaufabbruch und Cache-Löschbarriere | Core-Agent + UI/Build | Acht native Lifecycle-Fälle je iPhone bestanden |
| Integration | API-Abgleich, Fehlerbehebung, Testnachweise und Dokumentation | Hauptagent + gezielte Nachprüfungsagents | Für diese Entwicklungsversion abgeschlossen |

## Umgesetzter Funktionsumfang

- Freigabe/Landingpage vor öffentlichen Datenservices und interner WebView.
- Home, Wetter zum nächsten Termin, 3-Tage-Wetter und native News.
- Beide Rhein-Stationen mit getrennten Pegel-/Abflussskalen und Temperatur. Nur Basel hat einen bestätigten cm-Bezug. Grenzwerte 700/790/820 cm; Messwert und Cache müssen höchstens 60 Minuten alt sein, zukünftige Zeitstempel gelten nicht als aktuell.
- Öffentlicher Kalender mit Wiederholungen/Zeitzonen/Ausnahmen, Details, Kartenübergabe, Teilen und EventKit-Editor; externe Texte bleiben unverändert.
- Exakter Android-Vereinsbeizkatalog, Rechnung in CHF-Cent, persistenter Warenkorb und offene Zahlungsfrage. Nur ausdrückliches Ja oder manuelles Leeren löscht den Warenkorb; Nein/Abbruch erhält ihn. QR-Ansehen, Kopieren oder Speichern behauptet keinen Zahlungserfolg.
- Swiss-QR-PNG, natives Teilen und TWINT-Übergabe; Deutsch/Schweizerdeutsch, anpassbare Kacheln und Einstellungen.
- Tatsächlicher Android-JavaScript-Renderer mit Bytevergleich; Intern-App-/Originalansicht, Personenverwaltung und lokal gespeicherte private Präferenzen im nicht synchronisierten Keychain.
- Lokale öffentliche Caches, unmittelbares Laden vorhandener Daten, Fehler-/Offline-Fallback, bestätigtes Cache-Leeren sowie aktivierbare Hintergrundaktualisierung.

## Ausgeführte Abnahme

Xcode 16.4 / iOS 18.5: iPhone SE 3 hell und iPhone 16 Pro Max dunkel bestanden jeweils **49 Core + 19 Hosted + 11 UI = 79 Tests**, ohne Fehler. Auf iPad Air 11 Zoll M2 bestand zusätzlich der gezielte Test für echte Teilen-/Kalenderdialoge mit Abbruch und erhaltenem Warenkorb. Vier Live-Quellenprüfungen wurden separat tatsächlich ausgeführt. Debug-Testbuilds, kompakter Release-Build, echte Simulator-Keychain-Rechte und QR-PNG-/Vision-Roundtrip sind bestanden.

Original-Screenshots wurden tatsächlich angesehen. Die letzte Korrektur beseitigt abgeschnittene Temperatur-Zeitangaben; beide Graphen und die internen hellen/dunklen Matrizen sind im finalen Stand sichtbar und lesbar. Detaillierte Commit-, Job-, Artefakt- und Fehlerhistorie: [`TEST_RESULTS.md`](TEST_RESULTS.md). Reproduktionsbefehle: [`BUILD_TESTS.md`](BUILD_TESTS.md).

Der Android-Ausgangsbaum wurde identisch aus dem Remote-Commit rekonstruiert (`6e9808b2357a0e584a1ecc63f706176632cdf0e8`). Der abschließend getestete lokale iOS-Baum stimmt exakt mit Remote überein (`117ebb91a2d1e8bdde46fe782587ac0e40e2800d`). Lokale Plist-, Shell- und Java-Renderer-Prüfungen sind ergänzende Prüfungen, keine behauptete lokale Xcode-Ausführung.

## Plattformunterschiede und fehlende Release-Nachweise

- Geräteinstallation/TestFlight benötigt Apple-Team, geeignete Bundle-ID und Produktionssignierung. Die CI erzeugt eine ad-hoc-signierte Simulator-App, keine installierbare Geräte-IPA.
- Ein ausreichend hoch aufgelöstes offizielles AppIcon und vollständige Store-Assets fehlen. Das vorhandene 96×96-Vereinslogo ist in der App enthalten.
- Physische Geräte und iOS 17 sind nicht zur Laufzeit geprüft. Der iPad-Test deckt gezielt Systemdialoge ab, nicht alle App-Abläufe.
- Reale Banking-/TWINT-Übernahmen und Zahlungen sowie persönliche produktive Intern-Aktionen sind nicht durch synthetische Simulatorfälle belegt.
- iOS-Teilen ersetzt Android-Paketwahl. Eine erfolgreiche Übergabe ist keine automatisch verifizierte Zahlung.
- Hintergrundaktualisierung ist standardmäßig eingeschaltet; 30 Minuten sind eine früheste Anforderung, kein garantiertes Intervall. Die Tests prüfen Bedingungen, Abbruch und Zustand, nicht die Zustellhäufigkeit durch iOS.
- Die interne WebView erlaubt denselben HTTPS-Host. Auch im Originalmodus werden externe Dokumentnavigationen und `mailto`/`tel`/`geo` nicht weitergegeben; native öffentliche Vereinskontakte bleiben verfügbar. Website-Skripte und Serverlogik sind dadurch nicht vollständig kontrolliert.
- Remote-Konfiguration und native Intern-API bleiben wie im Android-Stand geplant, solange kein freigegebener Betriebsendpunkt vorliegt.

## Fortsetzung

Zuerst Root-`AGENTS.md`, `PROJECT.md`, `STATUS.md`, diese Datei und `iOS/BUILD_TESTS.md` lesen. Keine offenen Simulatorfehler im geprüften Produkt-/Testcode. Für eine Fortsetzung zuerst den aktuellen Branch mit dem geprüften Commit vergleichen; reine Dokumentationsänderungen verändern die getestete App nicht.

Nächste konkrete Arbeiten für eine Geräte-/Store-Version:

1. Offizielles hochauflösendes AppIcon und Store-Assets ergänzen.
2. Apple-Team, Produktions-Bundle-ID und geschützte Signierung/TestFlight-Verteilung einrichten.
3. Verfügbare physische Geräte/iOS-17-Runtime und breitere iPad-Abläufe prüfen; tatsächliche Banking-/Intern-Integrationen mit dafür vorgesehenen Zugängen gesondert abnehmen.
4. Erst danach Verteilungs-/Release-Freigabe behandeln. Der bestehende PR ist nicht gemergt und nichts wurde veröffentlicht.

Ein aus Android übernommener Gleitkommafehler an der exakten 820-cm-Grenze wurde nur in iOS behoben und in [`REVIEW.md`](REVIEW.md) für eine separate Android-Korrektur dokumentiert.
