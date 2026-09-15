# Status

Stand: 2026-09-14 · Android **0.15.0 / Versionscode 68** · iOS **0.15.0 / Build 2**.

## Freigegebener Stand

- Der Nutzer hat Vereinsübersicht, direkte Trainingszeiten, native Themenseiten ohne fachliche Aufklappmenüs und Deutsch/Schweizerdeutsch freigegeben.
- 0.15.0 übernimmt die geprüfte Funktion von 0.12.10; Paket-ID und festes Testzertifikat bleiben erhalten. Der Versionscode steigt von 67 auf 68.
- Enthalten sind direkte An-/Abmeldefarben, Refresh beim ersten Live-Block sowie fünf zentrierte Kontakt-/Social-Symbole.
- Android ist über PR #33, iOS über PR #32 nach `main` übernommen. Die iOS-Vereinsansicht bietet dieselben Inhalte in nativer SwiftUI-Navigation, einschliesslich öffentlicher Quellen, lokalem Bild-/Inhaltscache und gepflegtem Schweizerdeutsch.

## Laufende Korrektur 0.15.1 · 2026-09-15

Alle noch laufenden oder kommenden Termine am nächsten relevanten Tag gemeinsam anzeigen; Wetter je Terminzeitraum statt nur zum ersten Termin. Android und iOS werden gemeinsam angepasst. Abnahmefälle: Mittag plus Abend, nur Abend, Absagen, beendete Termine, reguläres Training ohne Duplikate und keine Wetterwerte ausserhalb eines zeitlich begrenzten Anlasses.

## Abnahme und Bereinigung

- Android-Tests, Browser-Szenarien, Lint und Builds bestanden; feste Testidentität und Signatur erhalten. [Test-APK 0.15.0](https://github.com/Chris171203/Pontoniere-Rheinfelden-/actions/runs/34825587615/artifacts/10340042643).
- iOS: je iPhone 61 Core + 22 Hosted + 14 UI = 97 Tests; iPad alle 14 UI-Fälle; fünf separate Live-Quellenfälle und unsigniertes arm64-Gerätearchiv bestanden. Die zwei iPhone-Jobs benötigten im letzten Lauf eine einmalige unveränderte Wiederholung wegen Simulator-Verzögerungen. Die tatsächlichen Ergebnisse und Sichtprüfungen stehen in [Abnahme 0.15.0](docs/checks-0.15.0.md).
- 48 abgeschlossene Branches und drei einmalige Bereinigungsdateien entfernt; nur `main` bleibt, keine offenen Pull Requests. Drei Archiv-Tags sowie historische Status- und Prüfnachweise bleiben erhalten. [Bereinigungsnachweis](docs/cleanup-0.15.0.md).

Die Funktionsabnahme des freigegebenen Android-Vorgängers ist in [checks-0.12.10.md](docs/checks-0.12.10.md) commitgenau dokumentiert.

## Grenzen

- Testphase unter 1.0.0; keine Store-Veröffentlichung.
- iOS-Laufzeitprüfung unter Xcode 16.4 / iOS 18.5 in GitHub Actions; keine Prüfung auf physischen Geräten. Geräteinstallation/TestFlight benötigt Apple-Signierung; das unsignierte Archiv ist keine installierbare IPA.
- Persönliche produktive Intern-Aktionen und reale Zahlungen bleiben ausserhalb der synthetischen Prüfungen.

## Referenzen

- [Projektumfang](PROJECT.md)
- [Bisherige Status- und Prüfgeschichte](docs/status-history-before-0.15.0.md)
- [Vereinsinhalte und UX](decisions/native-club-content.md)
