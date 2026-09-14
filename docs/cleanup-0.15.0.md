# Repository-Bereinigung 0.15.0

Stand: 2026-09-14.

[Prüflauf 34827342457](https://github.com/Chris171203/Pontoniere-Rheinfelden-/actions/runs/34827342457) erfolgreich: 47 alte Entwicklungsbranches gelöscht. Danach bleiben `main` und der laufende iOS-Port.

44 Stände sind entweder Vorfahren von main oder über einen abgeschlossenen PR mit identischem Head belegt. Drei abweichende Vorstufen wurden vor dem Löschen als Tags erhalten:

| Archiv-Tag | Grund |
|---|---|
| `archive/dev-0.10.16` | Temporäre Datei hinzugefügt und wieder entfernt; keine verbleibende Dateidifferenz. |
| `archive/dev-0.10.17` | Überholter Pegel-Versuch mit unbestätigter Rheinfelden-cm-Umrechnung; durch die akzeptierten stationsbezogenen Regeln ersetzt. |
| `archive/dev-0.11.3` | Einmaliger Workflow zum Export des damaligen Quellstands. |

Die Prüfung schützt main, offene PR-Branches und geschützte Branches. Jede Löschung verwendet den protokollierten alten Commit als atomare Bedingung; ein inzwischen verschobener Branch bleibt erhalten. Die freigegebenen Commit-IDs und Gründe stehen in [tools/cleanup-0.15.0.json](../tools/cleanup-0.15.0.json). Das vollständige Ausführungsprotokoll liegt im CI-Artefakt.

Aktuelle README und STATUS.md wurden von veralteten Versionsangaben bereinigt. Die bisherige Statusgeschichte steht unverändert im [Statusarchiv](status-history-before-0.15.0.md).
