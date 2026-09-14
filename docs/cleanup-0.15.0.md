# Repository-Bereinigung 0.15.0

Stand: 2026-09-14.

[Prüflauf 34827342457](https://github.com/Chris171203/Pontoniere-Rheinfelden-/actions/runs/34827342457) erfolgreich: 47 alte Entwicklungsbranches gelöscht. Nach dem Merge von PR #32 hat [Abschlusslauf 34873367525](https://github.com/Chris171203/Pontoniere-Rheinfelden-/actions/runs/34873367525) auch `codex/ios-port-0.12.6` beim geprüften Head `1a7dd5adf231cbbe1875ca4d35108f72327a7b47` entfernt. Insgesamt sind **48 Branches gelöscht**. Die anschliessende API-Prüfung zeigt ausschliesslich `main` und keine offenen Pull Requests.

Von den zuerst entfernten 47 Ständen sind 44 entweder Vorfahren von main oder über einen abgeschlossenen PR mit identischem Head belegt. Drei abweichende Vorstufen wurden vor dem Löschen als Tags erhalten:

| Archiv-Tag | Grund |
|---|---|
| `archive/dev-0.10.16` | Temporäre Datei hinzugefügt und wieder entfernt; keine verbleibende Dateidifferenz. |
| `archive/dev-0.10.17` | Überholter Pegel-Versuch mit unbestätigter Rheinfelden-cm-Umrechnung; durch die akzeptierten stationsbezogenen Regeln ersetzt. |
| `archive/dev-0.11.3` | Einmaliger Workflow zum Export des damaligen Quellstands. |

Die Prüfung schützt main, offene PR-Branches und geschützte Branches. Jede Löschung verwendet den protokollierten alten Commit als atomare Bedingung; ein inzwischen verschobener Branch bleibt erhalten. Die freigegebenen Commit-IDs und Gründe stehen im [ersten archivierten Bereinigungsmanifest](https://github.com/Chris171203/Pontoniere-Rheinfelden-/blob/a9c216d71f89d8fd5fd0184ea833959927f0326c/tools/cleanup-0.15.0.json) und im [abschliessenden iOS-Manifest](https://github.com/Chris171203/Pontoniere-Rheinfelden-/blob/377879236d7e776bb8158fcc10bee6f56ac69ef6/tools/cleanup-0.15.0.json). Die vollständigen Ausführungsprotokolle liegen in den jeweiligen CI-Artefakten.

Die drei einmaligen Bereinigungsdateien (`.github/workflows/cleanup-0.15.0.yml`, `tools/cleanup-0.15.0.py`, `tools/cleanup-0.15.0.json`) sind nach erfolgreichem Abschluss entfernt; ihre geprüfte Fassung bleibt über die Commit-Links erhalten. Aktuelle README-, Status- und Abnahmedateien führen 0.15.0. Die bisherige Statusgeschichte steht unverändert im [Statusarchiv](status-history-before-0.15.0.md); die frühere iOS-Portierung ist unverändert in [PORTING_HISTORY_0.12.6.md](../iOS/PORTING_HISTORY_0.12.6.md) erhalten.
