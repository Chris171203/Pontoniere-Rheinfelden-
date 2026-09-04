# Google-Play-Review-Zugang

Der Produktionsbuild enthält zwei getrennte Zugangsebenen:

1. lokale **Erstfreigabe** der App per gemeinsamem Freigabecode,
2. optionaler persönlicher Zugang zum bestehenden `intern.pfvr.ch`-Bereich.

Google muss eingeschränkte Funktionen prüfen können. Persönliche Vereinszugänge dürfen dafür nicht verwendet oder ins Repository übernommen werden.

## In Play Console hinterlegen

Unter **Policy / App content / App access** die App als teilweise zugangsbeschränkt angeben und sinngemäß folgende Anleitung eintragen:

> The public landing page, club information, calendar, Rhine data and weather are accessible without a personal PFVR account after entering the app review code provided below. The internal attendance area requires a dedicated PFVR review/demo URL. Open Settings → General → Personal access, enter the review URL, then open Home → An-/Abmelden. No personal member account is required for this review credential.

Danach ausschließlich in der Play Console ergänzen:

- **App-Freigabecode:** `[NICHT IM REPOSITORY SPEICHERN]`
- **PFVR-Review-/Demo-Link:** `[VOM VEREIN BEREITZUSTELLEN; NICHT PERSÖNLICH]`
- ggf. Ablaufdatum / Hinweis zum Testdatensatz.

## Anforderungen an den PFVR-Demozugang

Der Verein sollte einen eigenen Review-Zugang erzeugen, der:

- keine persönliche Produktiv-URL eines Mitglieds ist,
- nur Test-/Demopersonen und unkritische Testtermine zeigt,
- An-/Abmeldung und Personenmatrix realistisch prüfbar macht,
- keine administrativen Rechte besitzt,
- für den gesamten Google-Reviewzeitraum aktiv bleibt,
- nach Store-Freigabe rotierbar/deaktivierbar ist,
- nicht in GitHub, Screenshots oder Store-Metadaten auftaucht.

## Vor jeder Einreichung testen

Auf einem Gerät bzw. App-Datenbestand ohne bestehende PFVR-Einstellungen:

1. App starten.
2. Landingpage sichtbar; keine Live-/Intern-Daten vor Freigabe initialisiert.
3. Review-Code eingeben.
4. Öffentliche App-Funktionen öffnen.
5. Einstellungen → Allgemein → Persönlicher Zugang.
6. Demo-Link eintragen.
7. Home → An-/Abmelden.
8. App-Ansicht, Originalansicht, Personenverwaltung und mindestens eine Statusänderung prüfen.
9. App neu starten und sicherstellen, dass der Zugang weiterhin funktioniert.

## Nicht tun

- keinen persönlichen `intern.pfvr.ch`-Link in Play Console, Repository oder Supporttext verwenden,
- keinen Produktiv-Adminzugang bereitstellen,
- den normalen Mitgliederdatensatz nicht für Store-Screenshots verwenden,
- Review-Code nicht in Store-Beschreibung oder öffentliche Screenshots schreiben.

Google-Referenz: https://support.google.com/googleplay/android-developer/answer/9859455
