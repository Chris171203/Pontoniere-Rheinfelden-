# iOS-Portierung

Stand: 2026-09-15 · **0.15.2 / Build 4** · iPhone und iPad ab iOS 17.

Die freigegebene Android-Vereinsansicht ist in SwiftUI umgesetzt: direkt sichtbare Sommer-/Winterzeiten und Treffpunkte, App-Kalender, kompakte Themenzeilen und vollständig lesbare native Unterseiten. Nur der vollständige Originaltext bleibt optional aufklappbar.

Öffentliche WordPress-Inhalte und ausgewählte Bilder werden lokal gecacht. Quellenänderung und Abrufdatum bleiben getrennt; Fehler ersetzen keinen erfolgreichen Datenstand. Die Originaltexte bleiben unverändert, app-eigene Beschriftungen sind auf Deutsch/Schweizerdeutsch gepflegt. Telefon, Navigation, E-Mail, Instagram und Facebook stehen als fünf zentrierte Symbole unten. Der Refresh sitzt beim ersten sichtbaren Live-Block. Die interne An-/Abmeldung verwendet den bytegleichen aktuellen Android-Renderer, einschliesslich verzögerter Website-Farbänderungen.

Korrektur 0.15.2: eine gemeinsame Vorhersage pro Termintag mit vollständiger Terminliste. Gleiche/überlappende Zeiten werden einmal ausgewertet; Mittag und Abend bleiben innerhalb derselben Vorhersage. Abnahme für diesen Stand noch ausstehend.

## Abnahme

PR #34 ist nach `main` übernommen. Alle laufenden oder kommenden Termine am nächsten relevanten Tag erhalten eigenes Wetter für ihre Uhrzeiten; Mittag und Abend werden getrennt dargestellt. Absagen, beendete Termine und doppelte Regeltrainings werden ausgeschlossen. Zeitlich begrenzte lange Anlässe verwenden nur ihre betroffenen Stunden.

[CI 39](https://github.com/Chris171203/Pontoniere-Rheinfelden-/actions/runs/34943348505) besteht: je iPhone 67 Core + 22 Hosted + 16 UI = 105 Tests, iPad alle 16 UI-Fälle, fünf separate Live-Quellenfälle und das Gerätearchiv. Der vollständige iOS-Baum ist identisch zum integrierten Quellstand; spätere Änderungen betrafen Android und die Auswahl der Vorschauaufnahmen. Originale Wetter-Screenshots beider Varianten wurden auf allen drei Profilen angesehen. Tatsächliche Ergebnisse und Commitbezug: [Abnahme 0.15.1](../docs/checks-0.15.1.md). Vorheriger Stand: [Abnahme 0.15.0](../docs/checks-0.15.0.md). Reproduktion: [BUILD_TESTS.md](BUILD_TESTS.md).

Die frühere Portierungsarbeit ist unverändert in [PORTING_HISTORY_0.12.6.md](PORTING_HISTORY_0.12.6.md) erhalten. Ihre Ergebnisse beschreiben den damaligen Stand. Weitere historische Laufprotokolle: [TEST_RESULTS.md](TEST_RESULTS.md).

## Plattformgrenzen

Das unsignierte arm64-Gerätearchiv prüft iPhoneOS-Kompilierung, Version, AppIcon und Produktmetadaten. Es ist keine installierbare IPA; Geräteinstallation oder TestFlight benötigt Apple-Team, Signierung und Provisionierung.

Die Laufzeitprüfungen verwenden Xcode 16.4 / iOS 18.5 und synthetische Bedienfälle. Öffentliche Quellen werden zusätzlich live gelesen. Physische Geräte, iOS 17 zur Laufzeit, iPad-Querformat/Multitasking sowie persönliche produktive Intern-Aktionen und reale Zahlungsübergaben sind damit nicht belegt.

Bei einer Fortsetzung zuerst Root-AGENTS, PROJECT, STATUS und die iOS-Buildanleitung lesen. Testversionen bleiben unter 1.0.0.
