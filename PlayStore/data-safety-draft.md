# Google Play Data Safety – Arbeitsblatt

Stand: App `0.11.3`, Paket `ch.pfvr.app`.

Dieses Dokument ist die technische Vorarbeit für die Play-Console-Angaben. Es ist **kein automatisches Formular** und muss vor Einreichung gegen die dann ausgelieferte App, das reale `intern.pfvr.ch`-Verhalten und den tatsächlichen Herausgeber geprüft werden.

Google definiert „Erhebung“ grundsätzlich als Übertragung von Nutzerdaten vom Gerät weg. Rein lokale Verarbeitung muss im Data-Safety-Formular nicht als Erhebung angegeben werden, gehört aber bei sensiblen Daten trotzdem transparent in die Datenschutzerklärung. Nutzerinitiierte Übergaben an andere Apps können unter die von Google beschriebenen Ausnahmen für „Sharing“ fallen; das muss anhand des konkreten Console-Formulars bestätigt werden.

## Technischer Datenfluss

| Bereich | Daten | Gerät verlassen? | Ziel | Zweck | Vorläufige Play-Einordnung |
|---|---|---:|---|---|---|
| Sprache/Theme/Kacheln | UI-Einstellungen | Nein | nur lokaler App-Speicher | App-Funktion | nicht als Erhebung |
| Persönlicher Intern-Link | personenbezogene Zugangs-URL/Token | Nein, solange nur gespeichert | nur lokaler App-Speicher | Zugriff auf internen Bereich | lokal; in Privacy Policy offenlegen |
| App-Freigabestatus | boolesches Freigabe-Flag | Nein | nur lokaler App-Speicher | App-Funktion/Sicherheit | nicht als Erhebung |
| Öffentlicher Kalender | öffentliche Vereinsdaten | Ja, Download | Google Calendar → App | App-Funktion | keine persönlichen Kalenderdaten des Nutzers |
| Wetter | feste Rheinfelden-Koordinaten | Ja | Open-Meteo | App-Funktion | kein Gerätestandort; keine Location-Berechtigung |
| Rheinwerte | Stationsnummern/öffentliche Messdaten | Ja | BAFU | App-Funktion | keine Nutzerdaten |
| Vereinsmeldungen/Webseiten | öffentliche Inhalte | Ja, Download | pfvr.ch | App-Funktion | grundsätzlich öffentliche Webinhalte |
| Interner PFVR-Bereich | Personenbezug, Status, Auswahlwerte, Session/Zugriff | Ja | intern.pfvr.ch | App-Funktion / bestehender Vereinszugang | **wahrscheinlich deklarationsrelevant; siehe unten** |
| Banking-App-Erkennung | installierte/kompatible Apps | Nein | nur lokale PackageManager-Abfrage | App-Funktion | `Installed apps` wird lokal verarbeitet, nicht erhoben |
| Warenkorb/freier Betrag | lokale Auswahl/Betrag | grundsätzlich Nein | lokaler Speicher/RAM | App-Funktion | nicht als Erhebung |
| Swiss-QR | Zahlungsempfänger, Betrag, Referenz | auf Nutzeraktion | gewählte Banking-/TWINT-/Share-App | physische Konsumation bezahlen | nutzerinitiierte Übergabe; Sharing-Ausnahme prüfen |
| QR-Datei | lokal erzeugtes Bild | nur auf Nutzeraktion | gewählte Datei-/Banking-App | Zahlung | keine Foto-/Mediensammlung aus dem Gerät |
| Karten/Telefon/E-Mail/Social | Ziel-URI | auf Nutzeraktion | externe App/Browser | App-Funktion | nutzerinitiierter Wechsel zu Drittanbieter |
| Analytics/Ads/Crash-SDK | keine eingebaut | Nein | – | – | keine Erhebung |

## Manifest-/Permission-Audit

Aktuell wird nur `android.permission.INTERNET` angefordert.

Nicht angefordert werden insbesondere:

- Standort (`ACCESS_FINE_LOCATION`, `ACCESS_COARSE_LOCATION`),
- Kamera,
- Mikrofon,
- Kontakte,
- SMS/Anrufliste,
- Kalender,
- allgemeiner Datei-/Medienspeicher,
- Advertising ID,
- `QUERY_ALL_PACKAGES`.

Die Manifest-`<queries>` und Intent-Queries dienen ausschließlich dazu, lokal geeignete Banking-/Share-Apps zu finden. Sie sind keine Android-Berechtigung zum Hochladen einer installierten App-Liste.

## Wahrscheinlich relevante Data-Safety-Datentypen für den internen Bereich

Das bestehende `intern.pfvr.ch`-System ist der einzige Bereich, in dem personenbezogene Vereinsdaten aktiv zwischen Gerät und Server fließen. Vor Play-Einreichung muss anhand eines dedizierten Testzugangs verifiziert werden, welche Felder das Backend tatsächlich erhält/speichert.

Vorläufig zu prüfen:

- **Personal info → Name**: wenn der Personenname serverseitig übertragen/gespeichert wird.
- **Personal info → User IDs**: wenn der persönliche PFVR-Link bzw. dessen serverseitige Kennung einen identifizierbaren Nutzer bezeichnet.
- **App activity → Other actions** oder **Other user-generated content**: für An-/Abmeldestatus, Essens-/Teilnahmeauswahl und vergleichbare Nutzeraktionen, soweit das Play-Formular diese Einordnung verlangt.

Zweck voraussichtlich ausschließlich **App functionality** und ggf. **Account management** für den bestehenden Vereinszugang. Keine Verwendung für Werbung, Marketing, Profiling oder Analytics.

### Erforderlich oder optional?

Der interne Bereich ist eine optionale Funktion der App. Öffentliche Funktionen können ohne persönlichen Intern-Link genutzt werden. Entsprechend sollte personenbezogene Datenerhebung des internen Bereichs – soweit im Formular abbildbar – als **optional / nur bei Nutzung der Funktion** behandelt werden.

## Installed Apps

Die App erkennt lokal installierte bzw. kompatible Banking-Apps, damit der Nutzer eine Ziel-App auswählen kann. Diese Information:

- wird nicht an einen eigenen Server gesendet,
- wird nicht zu Analytics-/Marketingzwecken verwendet,
- wird nicht dauerhaft als vollständige App-Liste gespeichert; gespeichert wird nur die ausdrücklich gewählte Banking-App.

Damit ist `Installed apps` nach aktuellem technischen Stand **lokale Verarbeitung, keine Erhebung**. Die Datenschutzrichtlinie erwähnt den Zugriff trotzdem ausdrücklich.

## Zahlungen

Die App verarbeitet keine Bankzugangsdaten, Karteninformationen oder Kontosalden des Nutzers. Sie erzeugt Händler-Zahlungsdaten für physische Vereinsbeiz-Konsumationen und übergibt diese nur nach einer bewussten Nutzeraktion.

Für Data Safety bei Einreichung prüfen:

- ob die konkrete Android-Share-/Banking-Übergabe unter Googles Ausnahme für **user-initiated action** fällt,
- ob aufgrund der übertragenen Zahlungsdaten überhaupt ein Play-Datentyp des Nutzers betroffen ist; der Empfänger-IBAN gehört dem Verein, nicht dem Nutzer,
- dass keine Nutzer-Bankdaten von der PFVR-App gelesen oder gespeichert werden.

## Sicherheit

- Cleartext-Traffic ist deaktiviert.
- App-Netzwerkverbindungen verwenden HTTPS.
- Persönlicher Intern-Link liegt im privaten App-Speicher.
- Kein eigenes Analytics-/Tracking-Backend.
- Keine Werbe-SDKs.

Im Play-Formular darf „Daten werden bei der Übertragung verschlüsselt“ nur gewählt werden, wenn dies für **alle** dort als erhoben angegebenen Nutzerdaten gilt. Für den aktuellen internen PFVR-Datenfluss ist HTTPS vorgesehen; dies vor Einreichung mit dem Review-/Testzugang verifizieren.

## Löschung

- Lokale Cache-Daten: in der App löschbar.
- Alle lokalen App-Daten: Android-Systemeinstellungen / App-Daten löschen / Deinstallation.
- Serverdaten des internen PFVR-Systems: nicht durch die App-Löschung entfernt; Lösch-/Auskunftsprozess muss über den Verein/Datenschutzkontakt erfolgen.
- Die App erstellt selbst kein neues Benutzerkonto.

Für das Data-Safety-Badge „Löschanfrage möglich“ nur dann Ja wählen, wenn der finale Herausgeber einen tatsächlich erreichbaren Prozess für serverseitige PFVR-Daten bereitstellt und dieser in der Datenschutzerklärung genannt ist.

## Vorläufige Console-Antworten – nur als Arbeitsstand

- Sammelt oder teilt die App Nutzerdaten? **Nicht pauschal „Nein“ wählen**, solange der interne PFVR-Bereich personenbezogene Daten zum Vereinsserver überträgt.
- Werbung: **Nein**.
- Gerätestandort: **Nein**; Wetter nutzt feste Koordinaten.
- Analytics: **Nein**.
- Crash-/Diagnosedaten: **Nein**, soweit keine Play-/Herstellerdienste außerhalb der App selbst hinzugerechnet werden müssen.
- Installierte Apps: **lokal verarbeitet, nicht erhoben**.
- Persönliche Daten: **für den optionalen internen Bereich final prüfen und voraussichtlich deklarieren**.

## Vor Einreichung zwingend testen

1. Mit separatem Review-/Demo-Zugang den Netzwerk-/Serverfluss der internen An-/Abmeldung verifizieren.
2. Prüfen, ob intern.pfvr.ch außer Name/Status weitere Daten wie E-Mail, Telefonnummer oder Gerätekennungen verarbeitet.
3. Finalen Herausgeber und Datenschutzprozess einsetzen.
4. Prüfen, ob seit diesem Audit neue SDKs/Berechtigungen/Funktionen hinzugekommen sind.
5. Data-Safety-Antworten und Privacy Policy müssen übereinstimmen.

Offizielle Referenz: https://support.google.com/googleplay/android-developer/answer/10787469
