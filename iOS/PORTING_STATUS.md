# iOS-Portierung

Stand: 2026-09-14 · **0.15.0 / Build 2** · iPhone und iPad ab iOS 17.

Die freigegebene Android-Vereinsansicht ist in SwiftUI umgesetzt: direkt sichtbare Sommer-/Winterzeiten und Treffpunkte, App-Kalender, kompakte Themenzeilen und vollständig lesbare native Unterseiten. Nur der vollständige Originaltext bleibt optional aufklappbar.

Öffentliche WordPress-Inhalte und ausgewählte Bilder werden lokal gecacht. Quellenänderung und Abrufdatum bleiben getrennt; Fehler ersetzen keinen erfolgreichen Datenstand. Die Originaltexte bleiben unverändert, app-eigene Beschriftungen sind auf Deutsch/Schweizerdeutsch gepflegt. Telefon, Navigation, E-Mail, Instagram und Facebook stehen als fünf zentrierte Symbole unten. Der Refresh sitzt beim ersten sichtbaren Live-Block. Die interne An-/Abmeldung verwendet den bytegleichen aktuellen Android-Renderer, einschliesslich verzögerter Website-Farbänderungen.

## Abnahme

PR #32 ist nach `main` übernommen. [CI 36](https://github.com/Chris171203/Pontoniere-Rheinfelden-/actions/runs/34869645361) besteht: je iPhone 61 Core + 22 Hosted + 14 UI = 97 Tests, iPad alle 14 UI-Fälle, fünf separate Live-Quellenfälle und das Gerätearchiv. Beide iPhone-Jobs benötigten wegen Simulator-Verzögerungen eine einmalige unveränderte Wiederholung. Tatsächliche Ergebnisse, Commits und Sichtprüfungen stehen in [Abnahme 0.15.0](../docs/checks-0.15.0.md). Reproduktion: [BUILD_TESTS.md](BUILD_TESTS.md).

Die frühere Portierungsarbeit ist unverändert in [PORTING_HISTORY_0.12.6.md](PORTING_HISTORY_0.12.6.md) erhalten. Ihre Ergebnisse beschreiben den damaligen Stand. Weitere historische Laufprotokolle: [TEST_RESULTS.md](TEST_RESULTS.md).

## Plattformgrenzen

Das unsignierte arm64-Gerätearchiv prüft iPhoneOS-Kompilierung, Version, AppIcon und Produktmetadaten. Es ist keine installierbare IPA; Geräteinstallation oder TestFlight benötigt Apple-Team, Signierung und Provisionierung.

Die Laufzeitprüfungen verwenden Xcode 16.4 / iOS 18.5 und synthetische Bedienfälle. Öffentliche Quellen werden zusätzlich live gelesen. Physische Geräte, iOS 17 zur Laufzeit, iPad-Querformat/Multitasking sowie persönliche produktive Intern-Aktionen und reale Zahlungsübergaben sind damit nicht belegt.

Bei einer Fortsetzung zuerst Root-AGENTS, PROJECT, STATUS und die iOS-Buildanleitung lesen. Testversionen bleiben unter 1.0.0.
