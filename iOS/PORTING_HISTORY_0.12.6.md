# iOS-Portierung – Arbeitsstand

Aktualisiert: 2026-09-10. Android-Referenz: 0.12.6, Commit `c617bae7c1f00fbb1136621166387ed814652794`.

## Ergebnis

Native SwiftUI-Entwicklungsversion für iPhone und iPad ab iOS 17 umgesetzt. Der aktuelle geprüfte Quellstand `9da62951bfae021324df5b9c56c968e9465a0893` hat die erweiterte Abnahme bestanden: [CI 34518227820](https://github.com/Chris171203/Pontoniere-Rheinfelden-/actions/runs/34518227820), fünf erfolgreiche Jobs einschließlich vollständiger iPad-UI-Suite und unsigniertem Geräte-Release-Archiv. Danach folgen nur Dokumentationsänderungen. Arbeitsbranch: `codex/ios-port-0.12.6`, [Draft-PR #32](https://github.com/Chris171203/Pontoniere-Rheinfelden-/pull/32). Main und Android wurden nicht geändert.

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

Xcode 16.4 / iOS 18.5: iPhone SE 3 hell und iPhone 16 Pro Max dunkel bestanden jeweils **49 Core + 19 Hosted + 11 UI = 79 Tests**, ohne Fehler. Auf iPad Air 11 Zoll M2 bestanden alle elf UI-Szenarien, einschließlich echter Teilen-/Kalenderdialoge mit Abbruch und erhaltenem Warenkorb. Vier Live-Quellenprüfungen wurden separat tatsächlich ausgeführt. Debug-Testbuilds, kompakter Release-Build, echte Simulator-Keychain-Rechte und QR-PNG-/Vision-Roundtrip sind bestanden.

Original-Screenshots wurden tatsächlich angesehen. Die letzte Korrektur beseitigt abgeschnittene Temperatur-Zeitangaben; beide Graphen und die internen hellen/dunklen Matrizen sind im finalen Stand sichtbar und lesbar. Detaillierte Commit-, Job-, Artefakt- und Fehlerhistorie: [`TEST_RESULTS.md`](TEST_RESULTS.md). Reproduktionsbefehle: [`BUILD_TESTS.md`](BUILD_TESTS.md).

Der Android-Ausgangsbaum wurde identisch aus dem Remote-Commit rekonstruiert (`6e9808b2357a0e584a1ecc63f706176632cdf0e8`). Der zuletzt getestete lokale iOS-Baum stimmt exakt mit Remote-Branch und geprüftem PR-Merge überein (`090b30f6bd6bfb90b577c6c8f3a8ff6fc19c5881`). Lokale Plist-, Shell- und Java-Renderer-Prüfungen sind ergänzende Prüfungen, keine behauptete lokale Xcode-Ausführung.

## Plattformunterschiede und fehlende Release-Nachweise

- Geräteinstallation/TestFlight benötigt Apple-Team, geeignete Bundle-ID und Produktionssignierung. Die CI erzeugt eine ad-hoc-signierte Simulator-App und ein unsigniertes iPhoneOS-Release-Archiv, keine installierbare Geräte-IPA.
- Das vorhandene 96×96-Vereinslogo wird auf Nutzerwunsch als AppIcon verwendet; XcodeGen erzeugt die benötigte Asset-Größe beim Build. Ein höher aufgelöstes Original ist optional für bessere Schärfe. Diese Icon-Ergänzung wird separat im Gerätearchiv geprüft.
- Physische Geräte und iOS 17 sind nicht zur Laufzeit geprüft. Das iPad besteht die elf vorhandenen UI-Szenarien im Hochformat; Querformat, Multitasking und reale Geräte sind gesondert offen.
- Reale Banking-/TWINT-Übernahmen und Zahlungen sowie persönliche produktive Intern-Aktionen sind nicht durch synthetische Simulatorfälle belegt.
- iOS-Teilen ersetzt Android-Paketwahl. Eine erfolgreiche Übergabe ist keine automatisch verifizierte Zahlung.
- Hintergrundaktualisierung ist standardmäßig eingeschaltet; 30 Minuten sind eine früheste Anforderung, kein garantiertes Intervall. Die Tests prüfen Bedingungen, Abbruch und Zustand, nicht die Zustellhäufigkeit durch iOS.
- Die interne WebView erlaubt denselben HTTPS-Host. Auch im Originalmodus werden externe Dokumentnavigationen und `mailto`/`tel`/`geo` nicht weitergegeben; native öffentliche Vereinskontakte bleiben verfügbar. Website-Skripte und Serverlogik sind dadurch nicht vollständig kontrolliert.
- Remote-Konfiguration und native Intern-API bleiben wie im Android-Stand geplant, solange kein freigegebener Betriebsendpunkt vorliegt.

## Fortsetzung

Zuerst Root-`AGENTS.md`, `PROJECT.md`, `STATUS.md`, diese Datei und `iOS/BUILD_TESTS.md` lesen. Keine offenen Simulatorfehler im geprüften Produkt-/Testcode. Für eine Fortsetzung zuerst den aktuellen Branch mit dem geprüften Commit vergleichen; reine Dokumentationsänderungen verändern die getestete App nicht.

Nächste konkrete Arbeiten für eine Geräte-/Store-Version:

1. Vereinslogo als AppIcon im Gerätearchiv abnehmen; Store-Texte/-Screenshots erst für eine entsprechende Veröffentlichung ergänzen.
2. Apple-Team, Produktions-Bundle-ID und geschützte Signierung/TestFlight-Verteilung einrichten.
3. Verfügbare physische Geräte/iOS-17-Runtime sowie iPad-Querformat/Multitasking prüfen; tatsächliche Banking-/Intern-Integrationen mit dafür vorgesehenen Zugängen gesondert abnehmen.
4. Erst danach Verteilungs-/Release-Freigabe behandeln. Der bestehende PR ist nicht gemergt und nichts wurde veröffentlicht.

Ein aus Android übernommener Gleitkommafehler an der exakten 820-cm-Grenze wurde nur in iOS behoben und in [`REVIEW.md`](REVIEW.md) für eine separate Android-Korrektur dokumentiert.

## Fortsetzung: breitere iPad-Prüfung und Geräte-Kompilierung

Stand 2026-09-10, abgeschlossen. Ausgangspunkt Remote `739d71d`; auch dessen CI-Lauf `34516084257` ist erfolgreich. Die folgenden Bedingungen wurden anhand der neuen CI abgenommen:

| Arbeitspaket | Abnahmebedingung | Status |
|---|---|---|
| iPad (Terra-Agent) | Alle 11 benannten UI-Fälle genau einmal bestanden, vollständiger XCTest-Abschluss; kein Core/App-Duplikat | Bestanden in CI 34518227820 |
| Gerätearchiv (Integration) | Release-Archiv für echtes iPhoneOS/arm64; Produktmetadaten/Privacy/Entitlement-Trennung geprüft | Bestanden in CI 34518227820 |
| Release-Inventar (Luna-Agent) | Tatsächliche Icon-Abmessungen, App-Identität und Signierkonfiguration geprüft | Erledigt: nur identisches 96×96-JPEG; kein AppIcon/Team/Exportprofil im Repository |
| Review | Keine aufgeweichten UI-Assertions, absichtlich übersprungene Core-Live-Tests bleiben zulässig | Bestanden; sechs Regressionstests, abschließendes Review ohne weitere materielle Befunde |

Ein Simulatorlauf oder unsigniertes Archiv ersetzt weiterhin keinen physischen Geräte-/TestFlight-Nachweis. Die vorhandenen Quelllogos werden nicht als hochauflösende Originale ausgegeben.

## AppIcon-Ergänzung

Nutzerentscheidung: vorhandenes Vereinslogo verwenden. Icon-Generator und Geräteprodukt-Prüfung ergänzt. Gerätearchiv auf `f405a48` in CI `34520320085`, Job `103015917220`, bestanden: AppIcon für iPhone/iPad kompiliert und im Produkt geprüft. Die übrigen Simulatorjobs dieses Icon-Stands laufen separat; die vollständige vorherige Funktionsabnahme bleibt oben commitgenau dokumentiert. Der technische Installationsblocker bleibt Apple-Signierung/Provisionierung, nicht ein neues Logo oder weitere Simulatorfälle.
