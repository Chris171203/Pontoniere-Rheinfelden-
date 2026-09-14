# Abnahme 0.15.0

Stand: 2026-09-14. Android abgeschlossen; iOS-Abnahme noch offen.

## Android und Repository

Der freigegebene Funktionsstand 0.12.10 wurde als **0.15.0 / Versionscode 68** mit erhaltener Test-ID und Signatur gebaut. [CI 34825587615](https://github.com/Chris171203/Pontoniere-Rheinfelden-/actions/runs/34825587615) ist erfolgreich; PR #33 ist in main integriert. [Test-APK](https://github.com/Chris171203/Pontoniere-Rheinfelden-/actions/runs/34825587615/artifacts/10340042643). Der Android-Lauf [34828648912](https://github.com/Chris171203/Pontoniere-Rheinfelden-/actions/runs/34828648912) ist auch auf dem aktuellen integrierten iOS-Branch erfolgreich.

47 alte Branches wurden entfernt; drei abweichende Vorstufen vorher als Archiv-Tags erhalten. [Bereinigungsnachweis](cleanup-0.15.0.md). Remote bleiben main und der noch offene iOS-PR #32.

## iOS 0.15.0 / Build 2

Implementiert: direkte Trainingsübersicht, kompakte Themenzeilen, vollständig lesbare native Artikel, öffentliche WordPress-Inhalte mit Tagescache, begrenzter Bildcache, zentrierte Kontakt-/Social-Symbole, früher Live-Refresh, Deutsch/Schweizerdeutsch und bytegleicher aktueller Android-An-/Abmelderenderer. Originaltexte bleiben unverändert.

Geprüfter Remote-Quellstand: `5c229139a57d7f661aba505096578ddfa836f4c0`, [CI 34828648798, Lauf 32](https://github.com/Chris171203/Pontoniere-Rheinfelden-/actions/runs/34828648798). PR-Testmerge und Quellbranch haben denselben Baum `d82edfdca90b8dba8a4371dffa77f717150f1ac7`.

| Prüfung | Tatsächliches Ergebnis |
|---|---|
| Foundation | 61 deterministische Tests bestanden; fünf Live-Tests separat ausgeführt und bestanden |
| Öffentliche Quellen | Wetter, Kalender, News, beide BAFU-Stationen und fünf Vereinsseiten erfolgreich gelesen |
| iPhone compact | 61 Core + 22 Hosted bestanden; 13 von 14 UI-Fällen bestanden |
| iPhone large | 61 Core + 22 Hosted bestanden; 13 von 14 UI-Fällen bestanden |
| iPad tablet | Alle 14 UI-Fälle bestanden, keine Skips |
| Gerätearchiv | Unsigniertes arm64-iPhoneOS-Release-Archiv inklusive Version 0.15.0 / Build 2 und AppIcon bestanden |

Die fünf Live-Fälle werden im deterministischen Core-Schritt absichtlich übersprungen und zählen dort nicht als bestanden. Xcode 16.4 / iOS 18.5. Kontaktbögen der drei Profile aus den echten CI-Logs wurden angesehen; das ist keine vollständige Prüfung aller Original-PNGs.

## Verbleibende Korrektur und Fortsetzung

Large scheitert beim Öffnen der Sportseite: Der bisherige Helfer akzeptiert die Zeile als antippbar, obwohl die vorherige Aufnahme sie unterhalb des sichtbaren Inhalts zeigt. Compact scheitert mit grosser Schweizerdeutsch-Schrift beim Erreichen derselben Zeile; der Helfer wischt ausschliesslich nach oben und kann eine übersprungene Zeile nicht zurückholen. Die vorherige reine Selektorkorrektur reicht damit nicht aus.

Lokaler Commit `50059cfc97e8141883ddb767b2ce5f2684d299b0` ergänzt für Vereinsaktionen kurze gerichtete Scrollbewegungen und verlangt die vollständige Zeile zwischen Navigation und Tabs. Alle fachlichen Assertions bleiben erhalten. `git diff --check` und der lokale Plist-/Konfigurationsaudit sind bestanden; der neue XCUITest-Code ist noch nicht kompiliert oder ausgeführt.

Der Push wurde durch die automatische Freigabeprüfung abgelehnt. Origin wurde danach als `https://github.com/Chris171203/Pontoniere-Rheinfelden-.git` verifiziert und mit dem bestehenden PR #32 abgeglichen; auch der erneute Versuch wurde mangels ausdrücklicher Push-Freigabe abgelehnt. Keine Umgehung oder alternative Remote-Schreiboperation durchgeführt.

Der Nutzer hat danach Push und Bereinigung ausdrücklich freigegeben. Der lokale Git-Client hat keine Schreibanmeldung; die vorbereiteten Änderungen werden über die verbundene GitHub-Integration übertragen. Danach tatsächliche Simulatorergebnisse prüfen und verbleibende Fehler beheben. Erst nach erfolgreicher Abnahme PR #32 integrieren, den dann abgeschlossenen Branch entfernen und die einmaligen Bereinigungsdateien aus dem aktuellen Baum nehmen. Diesen Bericht und STATUS anschliessend auf den endgültigen Stand aktualisieren.

## Grenzen

Das Gerätearchiv ist keine installierbare IPA; Geräteinstallation/TestFlight braucht Apple-Signierung und Provisionierung. Keine physischen Geräte, iOS-17-Runtime, persönlichen produktiven Intern-Aktionen oder realen Zahlungen geprüft.
