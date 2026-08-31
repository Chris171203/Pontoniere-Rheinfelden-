# Google-Play-Vorbereitung

Der Quellstand ist technisch für einen Play-Store-Upload vorbereitet:

- Ziel-API 36
- Android App Bundle (`.aab`)
- getrennte Release-Signierung über GitHub-Secrets
- Datenschutzerklärung unter `docs/index.html`
- Entwürfe für Store-Eintrag, Data Safety und Prüfzugang

## Noch durch den Kontoinhaber zu erledigen

1. Offizielles Google-Play-Entwicklerkonto festlegen. Für eine Vereins-App ist ein Organisationskonto sachlich passender.
2. Dauerhaften Upload-Schlüssel erzeugen und sicher archivieren.
3. GitHub-Secrets gemäß `.github/workflows/android-release.yml` setzen.
4. GitHub Pages für `/docs` aktivieren oder die Datenschutzerklärung auf `pfvr.ch` veröffentlichen.
5. Hochauflösendes, freigegebenes Vereinslogo und finale Screenshots bereitstellen.
6. Store-Angaben und Data-Safety-Entwurf mit dem Verein bestätigen.
7. Für die Google-Prüfung einen zeitlich begrenzten Demo-Zugang zum internen Bereich bereitstellen, falls Google die Funktion vollständig prüfen soll.

Das vorhandene 96×96-Pixel-Ausgangslogo ist für den finalen Store-Eintrag zu klein. Entwürfe und Vorgaben für Store-Grafiken gehören in `playstore/assets/`.
