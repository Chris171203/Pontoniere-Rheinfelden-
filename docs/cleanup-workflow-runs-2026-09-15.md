# Bereinigung alter Workflow-Läufe

Stand: 2026-09-15.

## Ergebnis

Von 481 vorhandenen Läufen wurden **451 gelöscht**. **30 geschützte Läufe** blieben erhalten; zusammen mit dem erfolgreichen Bereinigungslauf sind anschliessend **31 Läufe** vorhanden. Der vollständige Bestand wurde per GitHub-API mit der Schutzliste abgeglichen.

Behalten wurden alle in den vorhandenen Markdown-Dokumenten verlinkten Prüfnachweise, laufende Prüfungen sowie die neuesten Läufe und jeweils bis zu zwei erfolgreiche Builds der aktiven CI-Workflows. Zum Prüfzeitpunkt gab es keine offenen PRs. Die Löschliste enthielt nur konkrete Run-IDs; Status, Quellcommit und Workflow-Pfad wurden unmittelbar vor der Löschung erneut geprüft. Fehlgeschlagene Löschungen hätten die Verarbeitung gestoppt.

Entfernt wurden überholte Android-/iOS-Zwischenläufe und alte einmalige Import-, Export-, Patch- und Entwicklungsabläufe samt ihren Logs und Artefakten. Aktuelle APKs, verlinkte historische Prüfberichte und die drei benötigten Workflow-Definitionen bleiben erhalten.

## Nachweise

- [Erfolgreicher Bereinigungslauf](https://github.com/Chris171203/Pontoniere-Rheinfelden-/actions/runs/34961669994)
- [Löschbericht](https://github.com/Chris171203/Pontoniere-Rheinfelden-/actions/runs/34961669994/artifacts/10393213246)
- [Geprüfte vollständige Lösch- und Schutzliste](https://github.com/Chris171203/Pontoniere-Rheinfelden-/blob/ef4bb52bbb73724283fc0d381e191b270a7bf119/tools/cleanup-actions-2026-09-15.json)

Die einmalige Workflow-Datei, das Ausführungsskript und die Arbeitsliste wurden nach Abschluss aus dem aktuellen Quellstand entfernt. Die obigen Commit-Links bewahren die genaue Ausführung nachvollziehbar auf. Kein wiederkehrender Löschauftrag eingerichtet.
