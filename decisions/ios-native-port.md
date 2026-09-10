# Native iOS-Portierung

Datum: 2026-09-10. Android-Basis: 0.12.6 (`c617bae`).

## Entscheidung

SwiftUI ab iOS 17 für die iPhone-/iPad-Oberfläche, ein lokales Swift-Package `PFVRCore` für Regeln, Datenmodelle, Abruf und Cache. Für die interne Website wird der bestehende Android-JavaScript-Renderer übernommen und über `WKWebView` angebunden. Android bleibt unverändert.

## Gründe und Alternativen

Ein gemeinsames Flutter-/React-Native-Projekt würde auch den funktionierenden Android-Client neu schreiben und seinen Regressionstestumfang vergrößern. Eine ausschließlich webbasierte Verpackung würde die native Navigation, Charts, dauerhafte lokale Zustände und Kalender-/Teilen-Integration nicht ohne zusätzliche Arbeit ersetzen. Die native Portierung hält die beiden Plattformen technisch getrennt; fachliche Regeln und Referenzdaten müssen dafür ausdrücklich abgeglichen werden.

Der vorhandene Intern-Renderer bedient echte Website-Controls und enthält zahlreiche bereits korrigierte Sonderfälle. Seine Wiederverwendung erhält diese Korrekturen; synthetische WebKit-Tests ersetzen dabei keinen Test am produktiven Vereinsserver.

## Plattformunterschiede

- Android-Paketnamen und Intent-Fallbacks werden nicht als angebliche iOS-Bank-APIs übernommen. iOS verwendet die vorhandenen Systemwege zum Teilen eines Swiss-QR-Bildes und die öffentliche TWINT-Seite.
- Der persönliche Intern-Link bleibt lokal. iOS-Schlüsselbund ohne Synchronisation und gerätegebundene Speicherung schützt den Zugriff; Website-Zustand und Backupverhalten werden separat behandelt.
- iOS 17 ist eine bewusste Untergrenze für SwiftUI/Charts, vereinfachte Kalenderübergabe und einen begrenzten Testumfang. Unterstützung älterer iOS-Versionen wäre ein separates Kompatibilitätspaket.
- Die in Android erst geplante Remote-Konfiguration wird nicht als fertig implementiert behandelt. Ein freigegebener versionierter HTTPS-Endpunkt ist Voraussetzung für die spätere plattformübergreifende Umsetzung.

## Nachweis

Fachregeln und Parser: XCTest mit expliziten Fehler-/Grenzfällen. App und interner Renderer: iOS-Simulator auf macOS, einschließlich lokaler HTML-Fixtures. Screenshots und xcresult als CI-Artefakte. Reale Banking-Übernahme, produktive Intern-Aktionen und signierte Geräteinstallation bleiben eigene Nachweise.

Aktueller Umsetzungs- und Teststatus: [`../iOS/PORTING_STATUS.md`](../iOS/PORTING_STATUS.md).
