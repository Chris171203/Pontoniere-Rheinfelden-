# Play-Store- und Release-Sicherheitsentscheidungen

Stand: 2026-09-05

## Ziel

Die öffentliche Android-App `ch.pfvr.app` soll über Google Play verteilt werden können, ohne persönliche PFVR-Zugänge oder produktive Signierschlüssel in Quellcode/CI-Artefakte einzubauen.

## Veröffentlichungsmodell

- Die App bleibt ein privat entwickeltes, nicht kommerzielles Hobbyprojekt.
- Google-Play-Herausgeber ist der Inhaber des persönlichen Entwicklerkontos, nicht automatisch der Pontonierfahrverein Rheinfelden.
- Vor öffentlicher Veröffentlichung wird eine schriftliche Vereinsfreigabe für Vereinsname/PFVR-Bezeichnung, Logo, vereinbarte öffentliche Inhalte/Links, Store-Material und Veröffentlichung unter dem persönlichen Entwicklerkonto archiviert.
- Store-Eintrag und Datenschutzerklärung trennen die Rollen klar: privater App-Herausgeber einerseits, bestehende PFVR-Webseiten/-Backends andererseits.
- Die Formulierung `offizielle App` wird nur verwendet, wenn der Verein diese Außendarstellung ausdrücklich schriftlich freigibt; Standard ist `privat entwickelt mit Genehmigung des Pontonierfahrvereins Rheinfelden`.
- D-U-N-S und ein Vereins-Organisationskonto sind damit kein Projektblocker. Falls das persönliche Entwicklerkonto unter Googles Closed-Test-Anforderung fällt, wird der erforderliche Test vor Produktionszugang absolviert.

## Signierung

- `ch.pfvr.app.test` verwendet weiterhin ausschließlich den bewusst öffentlichen Testschlüssel.
- `ch.pfvr.app` erhält einen separaten Upload-Key, der nur über geschützte CI-Secrets bereitgestellt wird.
- Der Store-Workflow wird ausschließlich manuell auf `main` ausgeführt und verwendet die GitHub-Environment `play-store`.
- Der Keystore wird im Runner nur temporär rekonstruiert, validiert und am Ende entfernt.
- Play App Signing soll beim ersten Store-Upload aktiviert werden; der lokale Schlüssel bleibt damit Upload-Key und ist nicht mit dem öffentlich eingecheckten Testkey identisch.

## Persönliche Vereinszugänge

- Persönliche `intern.pfvr.ch`-Links, Tokens und echte Review-Zugänge werden nicht eingecheckt.
- CI durchsucht den Quellstand vor dem Android-Build nach bekannten PFVR-Zugangsmustern und nach rohen Keystore-Dateien.
- Google Play erhält Freigabecode und einen separaten nicht personenbezogenen PFVR-Demozugang ausschließlich über die geschützten Review-Angaben der Play Console.

## Lokaler Datenspeicher / Backup

Der persönliche Intern-Link und die Teilnehmerzustände sind bewusst gerätegebunden. Deshalb:

- `android:allowBackup="false"` bleibt gesetzt.
- Für Android 12+ werden zusätzlich explizite `dataExtractionRules` hinterlegt, die App-Dateien, Datenbanken, Shared Preferences und externe App-Dateien sowohl aus Cloud-Backup als auch Device-to-Device-Transfer ausschließen.
- Für ältere Android-Versionen werden entsprechende `fullBackupContent`-Ausschlüsse gepflegt.

Damit wird nicht darauf vertraut, dass Hersteller `allowBackup=false` bei Device-to-Device-Migration identisch behandeln.

## Berechtigungen

Die App benötigt keine sensiblen Android-Laufzeitberechtigungen. Im eigenen Manifest wird nur Internetzugriff deklariert; Bibliotheken können gewöhnliche technische Berechtigungen für Hintergrundarbeit in den finalen Manifest-Merge einbringen.

CI prüft deshalb nicht nur das Quellmanifest, sondern exportiert die tatsächlich in der gebauten APK zusammengeführten Berechtigungen und bricht bei unerwarteten sensiblen/hochwirksamen Berechtigungen ab, insbesondere Standort, Kamera, Mikrofon, Kontakte, Kalender, SMS/Telefon, Medien-/Speicherzugriff, `QUERY_ALL_PACKAGES`, Benachrichtigungen, Installations-/Overlay- und Exact-Alarm-Berechtigungen.

## WebView und JavaScript

JavaScript bleibt für die bestehenden PFVR-Webseiten und insbesondere die mobile interne An-/Abmeldeansicht technisch erforderlich. Die allgemeine Lint-Warnung `SetJavaScriptEnabled` wird deshalb nicht durch Abschalten der Funktion „behoben“.

Die Risikobegrenzung erfolgt stattdessen durch:

- ausschließlich HTTPS und deaktivierten Cleartext-Verkehr,
- deaktivierten Datei- und Content-Zugriff in WebViews,
- `MIXED_CONTENT_NEVER_ALLOW`,
- Safe Browsing,
- Host-/Navigationsregeln: nicht erlaubte Ziele verlassen die interne WebView und werden extern geöffnet,
- keine JavaScript-Bridge mit nativen geheimen Funktionen,
- keine persönlichen Zugangsdaten im Quellcode.

Die Warnung bleibt im Lint-Bericht sichtbar und wird nicht pauschal unterdrückt.

## Lint / API-Kompatibilität

`lintRelease` ist Teil der CI. Store-Vorbereitung darf nicht mit Lint-Fehlern auf `main` landen. Der in der Vorbereitung entdeckte API-27-only Theme-Wert `windowLightNavigationBar` wurde aus dem Basis-Theme entfernt und in eine `values-v27`-Ressource verschoben, damit `minSdk 26` tatsächlich unterstützt bleibt.

Reine Hinweise, die durch die programmgesteuerte UI oder die bewusst eigene Lokalisierung entstehen, dürfen dokumentiert bleiben; echte Sicherheits-, Kompatibilitäts- und Releasefehler werden behoben statt gebaselined.

## Store-Assets

Das aktuell kleine JPEG-Vereinslogo ist ein Laufzeit-Asset und kein ausreichender Store-Master. Finales Store-Icon und Feature Graphic werden erst aus einer vom Verein freigegebenen hochauflösenden bzw. vektorbasierten Quelle erzeugt. Künstliche Hochskalierung des kleinen JPEG gilt nicht als finale Lösung.
