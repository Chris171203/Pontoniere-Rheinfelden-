# Abnahme 0.15.1

Stand: 2026-09-15. Android 0.15.1 / Versionscode 69 und iOS 0.15.1 / Build 3 abgeschlossen; PR #34 ist mit `177693cf9fb5ab8b239411b65175cfed6a14459c` nach `main` übernommen.

## Korrektur

Die Wetterkachel wählte zuvor nur einen einzelnen Termin aus. Jetzt erscheinen alle noch laufenden oder kommenden Termine am nächsten relevanten Tag chronologisch, jeweils mit Wetter über ihren eigenen Zeitraum. Mittag und Abend werden getrennt dargestellt; bei nur einem Abendtermin erscheinen keine Mittagswerte. Abgesagte und beendete Termine entfallen. Ein explizites Kalendertraining wird nicht als Regeltraining verdoppelt; ein nicht eingetragenes reguläres Training ergänzt weiterhin denselben Tag.

Der ausgewählte Tag richtet sich nach Europe/Zurich. Laufende mehrtägige Anlässe verwenden diesen Tag, nicht ihren vergangenen Starttag. Stundenwerte werden nur berücksichtigt, wenn ihr Stundenintervall den Termin schneidet. Das Terminende ist exklusiv; z. B. 18–24 Uhr verwendet keine Mittagswerte und keine Stunde des Folgetags. Ganztägige Termine behalten 06/12/18 Uhr. Zeitlich begrenzte lange Termine zeigen Start/Mitte/letzte betroffene Stunde und eine auf ihren Zeitraum begrenzte Zusammenfassung.

Android und iOS verwenden denselben öffentlichen Wettercache wie bisher; keine zusätzlichen Wetterabrufe pro Termin. Schweizerdeutsch für die Mehrzahlüberschrift und die Zeitraum-Beschriftung ist gepflegt. Externe Termintitel bleiben unverändert.

## Prüfungen

- Lokal: neun bestehende Java-Policy-/Quellprüfungen, iOS-Plist-/Konfigurationsaudit, sechs Prüfungen des XCTest-Ausführungsnachweises und `git diff --check` bestanden. Lokal gibt es kein Xcode und keinen vollständigen Android-SDK-Build.
- Neu: fünf Android-Robolectric-Fälle für tatsächliche Auswahl und gerenderte Texte; sechs iOS-Core-Fälle; zwei iOS-UI-Fälle für Mittag plus Abend auf Schweizerdeutsch und nur Abend auf Deutsch. Unterschiedliche Temperaturen sowie nicht relevante Stundenwerte prüfen die Zuordnung.
- Android [CI 34943818340 / Lauf 209](https://github.com/Chris171203/Pontoniere-Rheinfelden-/actions/runs/34943818340), Quellstand `26fca6e04c7f3c98dd6433c6720e5c515850017d`: vollständig bestanden. Die heruntergeladenen JUnit-XML-Berichte bestätigen **133 Tests, null Fehler und null Skips**, darunter alle fünf neuen Robolectric-Fälle. Vier Browser-Szenarien, Release-Lint, Debug-APK, unsigned Release-AAB sowie Paket-/Versions-/Signaturprüfung ebenfalls bestanden. [Test-APK 0.15.1](https://github.com/Chris171203/Pontoniere-Rheinfelden-/actions/runs/34943818340/artifacts/10386039139).
- iOS [CI 34943348505 / Lauf 39](https://github.com/Chris171203/Pontoniere-Rheinfelden-/actions/runs/34943348505) auf `4c2defad80cd6f4633ce165b2af7f64d02808813`: alle fünf Jobs erfolgreich. Je iPhone **67 Core + 22 Hosted + 16 UI = 105 Tests**, iPad **16 UI-Fälle**, ohne Fehler und ohne UI-Skips. Fünf Live-Quellenfälle separat ausgeführt und bestanden; sie werden im deterministischen Core-Schritt absichtlich übersprungen und dort nicht als ausgeführt gezählt. Compact besteht zusätzlich den Release-Build. Unsigniertes arm64-iPhoneOS-Archiv mit Version **0.15.1 / Build 3**, AppIcon und Produktmetadaten bestanden. Umgebung: Xcode 16.4 / iOS 18.5.
- Der komplette iOS-Baum ist zwischen diesem geprüften Stand und dem integrierten PR-Head `26fca6e04c7f3c98dd6433c6720e5c515850017d` identisch: `e3639690a6ce1016b47b13ab2c72bdf49055f45d`. Die späteren Commits betreffen Android, dessen CI/Quelltextprüfung und die Auswahl der Screenshot-Vorschaubilder, nicht iOS-App oder iOS-Tests. Weitere automatisch gestartete Läufe sind zusätzliche Wiederholungen; die dokumentierte iOS-Abnahme bezieht sich auf den vollständig erfolgreichen Lauf 39.

## Sichtprüfung

Original-PNGs aus den Simulatorartefakten wurden tatsächlich heruntergeladen und angesehen: Mittag plus Abend auf Schweizerdeutsch und nur Abend auf Deutsch, auf iPhone SE 3 (hell), iPhone 16 Pro Max (dunkel) und iPad Air 11 M2 (hell). Beide Termine zeigen getrennte Uhrzeiten und die zugeordneten synthetischen Temperaturen 28 °C beziehungsweise 14 °C. Beim einzelnen Abendtermin fehlt der Mittagsblock in der Terminwetter-Kachel. Die eigenständige 3-Tage-Wetterkachel bleibt eine Tagesübersicht. Alle neuen UI-Fälle bestanden ohne Wiederholung einzelner Tests; geprüft wurden diese gezielten Originalaufnahmen, nicht sämtliche Screenshots oder Gerätekonfigurationen.

## Build-Konfiguration

Die ersten Android-Läufe scheiterten vor Kompilierung im bestehenden `android-actions/setup-android@v3`: dessen Standardpaket `tools` war im SDK-Repository nicht mehr verfügbar. Der Workflow fordert deshalb explizit `platform-tools` an. Command-Line-Tools, Plattform 36 und Build-Tools 35.0.0 bleiben vorgesehen. Keine Testassertions wurden dafür entfernt. Danach meldete eine ältere Quelltextprüfung die bisher ausschliesslich erwartete Einzahl der Wetterüberschrift; sie wurde auf die nun erforderliche Einzahl/Mehrzahl angepasst. Die tatsächlichen neuen UI-/Fachprüfungen bestanden bereits in diesem Lauf.

## Grenzen

Die Abnahme verwendet synthetische Termine und Wetterwerte, ergänzt durch die bestehenden separaten öffentlichen Quellenprüfungen. Sie bestätigt nicht die konkreten Termine auf einem privaten Gerät. iOS-Geräteinstallation/TestFlight benötigt weiterhin Apple-Signierung; ein unsigniertes Gerätearchiv belegt Kompilierung, keine Installation auf einem physischen Gerät.
