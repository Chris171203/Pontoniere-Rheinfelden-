# Status

Stand: 2026-09-14 · Android-Testversion `0.15.0` (`versionCode 68`).

## Freigegebener Stand

- Der Nutzer hat Vereinsübersicht, direkte Trainingszeiten, native Themenseiten ohne fachliche Aufklappmenüs und Deutsch/Schweizerdeutsch freigegeben.
- 0.15.0 übernimmt die geprüfte Funktion von 0.12.10; Paket-ID und festes Testzertifikat bleiben erhalten. Der Versionscode steigt von 67 auf 68.
- Enthalten sind direkte An-/Abmeldefarben, Refresh beim ersten Live-Block sowie fünf zentrierte Kontakt-/Social-Symbole.
- Die Funktionsabnahme von 0.12.10 ist in [checks-0.12.10.md](docs/checks-0.12.10.md) commitgenau dokumentiert. Der neue 0.15.0-Build wird vor Übernahme geprüft.

## Aktuelle Arbeiten

1. Android 0.15.0 bauen, PR #33 übernehmen, abgeschlossene Branches prüfen und bereinigen.
2. iOS aus PR #32 auf die freigegebenen Vereinsinhalte, Navigation und Schweizerdeutsch nachziehen.
3. iOS-Core-, WebKit-, Simulator- und Gerätearchivprüfungen ausführen und den tatsächlichen Umfang dokumentieren.

Die lokale Ausführungsumgebung ist am 2026-09-14 nicht erreichbar. Quelländerungen erfolgen über GitHub; Build und Laufzeitprüfungen über die bestehenden GitHub-Actions-Workflows.

## Grenzen

- Testphase unter 1.0.0; keine Store-Veröffentlichung.
- iOS ist bisher auf Android 0.12.6 basierend geprüft. Geräteinstallation/TestFlight benötigt weiterhin Apple-Signierung; das unsignierte Archiv ist keine installierbare IPA.
- Persönliche produktive Intern-Aktionen und reale Zahlungen bleiben ausserhalb der synthetischen Prüfungen.

## Referenzen

- [Projektumfang](PROJECT.md)
- [Bisherige Status- und Prüfgeschichte](docs/status-history-before-0.15.0.md)
- [Vereinsinhalte und UX](decisions/native-club-content.md)
