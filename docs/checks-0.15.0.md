# Abnahme 0.15.0

Stand: 2026-09-14. Android und iOS abgeschlossen und nach `main` übernommen.

## Android

Freigegebener Funktionsstand 0.12.10 als **0.15.0 / Versionscode 68**, Paket `ch.pfvr.app.test`, festes Testzertifikat erhalten. [CI 34825587615 / Lauf 199](https://github.com/Chris171203/Pontoniere-Rheinfelden-/actions/runs/34825587615) erfolgreich: Android-Tests, vier Browser-Szenarien, Lint, Debug-APK, unsigned Release-AAB und Paket-/Signaturprüfung. PR #33 wurde nach main übernommen (`f8487c0965f377ff61db3822154a73d6a804dd71`).

[Android-Test-APK 0.15.0](https://github.com/Chris171203/Pontoniere-Rheinfelden-/actions/runs/34825587615/artifacts/10340042643). Die Funktions- und Screenshotnachweise des unveränderten Android-Produktcodes stehen in [checks-0.12.10.md](checks-0.12.10.md). Der integrierte iOS-Branch besteht zusätzlich [Android-CI 34869645363 / Lauf 204](https://github.com/Chris171203/Pontoniere-Rheinfelden-/actions/runs/34869645363); gegenüber dem freigegebenen Android-Stand ist nur dessen CHANGELOG ergänzt.

## iOS 0.15.0 / Build 2

Direkte Sommer-/Winterzeiten und Treffpunkte, App-Kalender, kompakte Themenzeilen und vollständig lesbare native Artikel entsprechen dem freigegebenen Vereinskonzept. Öffentliche WordPress-Inhalte und ausgewählte Bilder werden lokal gecacht. Originaltexte bleiben unverändert; app-eigene Beschriftungen sind auf Deutsch/Schweizerdeutsch gepflegt. Fünf zentrierte Kontakt-/Social-Symbole, früher Live-Refresh und der bytegleiche aktuelle Android-An-/Abmelderenderer sind integriert.

Vollständig bestanden: Quellstand `1a7dd5adf231cbbe1875ca4d35108f72327a7b47`, [iOS-CI 34869645361 / Lauf 36, Versuch 2](https://github.com/Chris171203/Pontoniere-Rheinfelden-/actions/runs/34869645361). Ausschliesslich die beiden fehlgeschlagenen iPhone-Jobs wurden einmal unverändert wiederholt; die drei erfolgreichen Jobs des ersten Versuchs blieben erhalten. Bereits [Lauf 34](https://github.com/Chris171203/Pontoniere-Rheinfelden-/actions/runs/34868088132) bestand vollständig. Die letzte Änderung verschärft allein den Footer-Sichtnachweis.

PR #32 ist mit `f938403b5f5ef925dc15a6436721c3701ed86b74` nach `main` übernommen. Quellstand, PR-Testmerge `85e2fabef9397bbe1da888383461fd0a563de754` und tatsächlicher Merge haben denselben Baum: `5287043a395ac2bc2a13d44799f31a86f0cc22af`. Anschliessende Bereinigungs- und Dokumentationscommits ändern keinen App-Produktcode.

| Prüfung | Tatsächlich bestanden in Lauf 36 |
|---|---|
| Foundation | 61 deterministische Tests; fünf Live-Tests separat ausgeführt |
| iPhone SE 3, hell | 61 Core + 22 Hosted + 14 UI = 97 Tests |
| iPhone 16 Pro Max, dunkel | 61 Core + 22 Hosted + 14 UI = 97 Tests |
| iPad Air 11 Zoll M2, hell | Alle 14 UI-Fälle, keine Skips |
| Gerätearchiv | Release für iPhoneOS/arm64, Version 0.15.0 / Build 2, AppIcon und Produktmetadaten |

Umgebung: Xcode 16.4 / iOS 18.5. Alle ausgewiesenen Tests ohne Fehler. Die fünf Live-Fälle werden im deterministischen Core-Schritt absichtlich übersprungen und zählen dort nicht als ausgeführt. Separat wurden Wetter, Kalender, News, beide BAFU-Stationen und fünf öffentliche Vereinsseiten tatsächlich erfolgreich gelesen. Compact besteht zusätzlich den Release-Build.

Neue Prüfungen decken Quellenparser, Layoutmigration, Cache-Persistenz und Löschbarrieren, native Bildaufbereitung sowie verzögerte Website-Farbänderungen im echten WKWebView ab. Die UI-Fälle prüfen direkt sichtbare Trainingszeiten, native Artikel ohne Aufklappen, Kalenderwechsel, frühen Refresh, Nachwuchskontakt, Schweizerdeutsch, grosse Schrift und zentrierte Kontakt-/Social-Symbole.

## Korrekturen und Sichtprüfung

Frühere UI-Läufe fanden zunächst verdeckte oder vererbte SwiftUI-Kennungen nicht. Sichtbare Beschriftungen behoben die Refresh-Abfrage. Die Sportnavigation erforderte zusätzlich eine geometrische Prüfung: SwiftUI meldet teilweise unter der Tab-Leiste liegende Elemente bereits als antippbar. Kurze gerichtete Scrollbewegungen bringen die vollständige Zeile zwischen Navigation und Tabs; sie können auch nach oben zurückscrollen. Die fachlichen Assertions wurden beibehalten.

Die Sichtprüfung von Lauf 34 zeigte dieselbe Schwäche noch bei der Footer-Aufnahme auf dem iPad. Der letzte Commit verwendet deshalb auch dort die vollständige Sichtbarkeitsprüfung. Er ändert genau eine Testzeile und keinen App-Produktcode.

Kontaktbögen aus tatsächlichen CI-Logs wurden für Training, native Sport-/Nachwuchsartikel, Schweizerdeutsch mit grosser Schrift, Home und Systemdialoge angesehen. Die abschliessenden Footer-Kontaktbögen aus Lauf 36 wurden auf allen drei Profilen angesehen: fünf vollständig sichtbare, zentrierte Symbole. Alle neuen Vereinsfälle bestehen. Im ersten Versuch von Lauf 36 überschritt auf beiden iPhones ein App-Start/Neustart das 60-Sekunden-Budget des Warenkorbtests; die Logs zeigen rund 42 Sekunden Verzögerung beim Start beziehungsweise bei der Einrichtung der Testautomatisierung. Auf dem grossen iPhone wurde zusätzlich die native Teilen-Ansicht erst nach Ablauf der Abfrage erkannt; das AX-Protokoll enthält danach die tatsächlichen Aktionen. Ausschliesslich die beiden fehlgeschlagenen iPhone-Jobs wurden einmal unverändert wiederholt und bestanden vollständig. Der Warenkorbtest benötigte dabei 31,4 Sekunden (compact) beziehungsweise 39,7 Sekunden (large); die Systemdialoge bestanden in 36,8 beziehungsweise 51,2 Sekunden. Diese Wiederholung ist ausdrücklich dokumentiert; es gibt keine automatische Wiederholung einzelner Testfälle und keine gelockerten Assertions oder Zeitlimits. Das ist keine vollständige Prüfung sämtlicher Original-PNGs oder aller Gerätekonfigurationen.

## Repository

48 abgeschlossene Branches wurden entfernt, drei abweichende historische Vorstufen vor dem Löschen als Archiv-Tags erhalten. Android PR #33 und iOS PR #32 sind integriert. Nach dem letzten erfolgreichen Bereinigungslauf bleibt ausschliesslich `main`, ohne offene Pull Requests. Die drei einmaligen Bereinigungsdateien sind entfernt; aktuelle Statusdateien sind gekürzt und historische Nachweise erhalten. [Bereinigungsnachweis](cleanup-0.15.0.md).

## Grenzen

Die iOS-Kompilierung und Laufzeitprüfung erfolgten in GitHub Actions. Die lokale Linux-Umgebung hat kein Xcode. Das Gerätearchiv ist unsigniert und keine installierbare IPA; Geräteinstallation/TestFlight benötigt Apple-Signierung und Provisionierung. Physische Geräte, iOS 17 zur Laufzeit, iPad-Querformat/Multitasking, persönliche produktive Intern-Aktionen und reale Zahlungen sind nicht Teil dieser Abnahme.
