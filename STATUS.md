# Status

Stand: 2026-09-14 · Android-Testversion `0.15.0` (`versionCode 68`).

## Freigegebener Stand

- Der Nutzer hat Vereinsübersicht, direkte Trainingszeiten, native Themenseiten ohne fachliche Aufklappmenüs und Deutsch/Schweizerdeutsch freigegeben.
- 0.15.0 übernimmt die geprüfte Funktion von 0.12.10; Paket-ID und festes Testzertifikat bleiben erhalten. Der Versionscode steigt von 67 auf 68.
- Enthalten sind direkte An-/Abmeldefarben, Refresh beim ersten Live-Block sowie fünf zentrierte Kontakt-/Social-Symbole.
- Die Funktionsabnahme von 0.12.10 ist in [checks-0.12.10.md](docs/checks-0.12.10.md) commitgenau dokumentiert. Android 0.15.0 ist mit [CI 34825587615](https://github.com/Chris171203/Pontoniere-Rheinfelden-/actions/runs/34825587615) erfolgreich geprüft und über PR #33 nach main übernommen.

## Aktuelle Arbeiten

1. Android 0.15.0 abgeschlossen; verbliebene abgeschlossene Branches prüfen und bereinigen.
2. iOS 0.15.0 / Build 2 im bestehenden PR #32: Quellenparser und Caches, direkte Trainingsübersicht, flache Themenartikel, Kontakt-/Social-Symbole, frühes Live-Refresh und gemeinsamer aktueller An-/Abmelderenderer implementiert; neue Prüfungen vorbereitet.
3. iOS-Core-, WebKit-, Simulator- und Gerätearchivprüfungen ausführen und den tatsächlichen Umfang dokumentieren.

Die lokale Ausführungsumgebung ist am 2026-09-14 nicht erreichbar. Quelländerungen erfolgen über GitHub; Build und Laufzeitprüfungen über die bestehenden GitHub-Actions-Workflows.

## Grenzen

- Testphase unter 1.0.0; keine Store-Veröffentlichung.
- iOS 0.15.0 ist in der Abnahme; bisherige 0.12.6-Nachweise sind historisch. Geräteinstallation/TestFlight benötigt weiterhin Apple-Signierung; das unsignierte Archiv ist keine installierbare IPA.
- Persönliche produktive Intern-Aktionen und reale Zahlungen bleiben ausserhalb der synthetischen Prüfungen.

## Referenzen

- [Projektumfang](PROJECT.md)
- [Bisherige Status- und Prüfgeschichte](docs/status-history-before-0.15.0.md)
- [Vereinsinhalte und UX](decisions/native-club-content.md)
