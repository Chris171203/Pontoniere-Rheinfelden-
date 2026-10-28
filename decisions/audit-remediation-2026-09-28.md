# Umsetzung des App-Audits vom 28.09.2026

Prüfstand des Audits: Commit `96914447c49cd77cdeb5da1c68a9c01a0c89c444` (0.15.3). Diese Änderung baut auf dem offenen Saison-PR auf und erstellt einen eigenen Testkandidaten 0.15.4. Maßgeblich für den aktuellen Funktionsstand sind Code und grüne Tests des neuen Commits, nicht die historische Auditbeschreibung.

## Fachliche Entscheidungen

- Die BAFU-Umrechnung Meter→Zentimeter kann an genau 820 cm knapp darunter liegen. Eine minimale Rechentoleranz gilt für alle drei offiziellen Grenzwerte; 1 cm darunter bleibt unverändert unter der jeweiligen Grenze. Veraltete Messungen bleiben „Lage unklar“.
- Ein persönlicher Intern-Link hat eine gerätelokale, zufällige Identitätsgeneration. Gespeicherte Personenstände ohne diese Generation oder mit anderer Generation werden nicht übernommen. Beim tatsächlichen Linkwechsel wird zunächst der alte Link entfernt, die alte WebView beendet und Browserdaten gelöscht; der neue Link wird erst nach der Cookie-Callback-Fertigstellung gespeichert. Bestehende Personenansichten aus älteren App-Versionen werden beim ersten Start neu aufgebaut. Der Freigabecode bleibt nur eine lokale Zugangsschranke.
- Der Intern-WebView öffnet keine Fremdhost-Navigation in anderen Apps. Benötigte externe Ziele müssen künftig als explizite, tokenfreie Aktion modelliert werden. News werden nur unter `https://www.pfvr.ch` im App-WebView gezeigt; ein fremdes Feed-Erstziel wird nicht geöffnet.
- Ein syntaktisch gültiger VCALENDAR ohne VEVENT ist ein erfolgreicher leerer Kalenderstand. Wetter-/Hydroantworten benötigen dagegen wenigstens brauchbare zeitlich zuordenbare Messwerte, bevor der letzte gute Cache überschrieben wird.
- Wiederholungen folgen Tages-/Monatskalenderregeln in `Europe/Zurich`, überspringen nicht existente Monatstage und behalten die Kalendertagsdauer ganztägiger Ereignisse. Negative/Null-Intervalle und nicht unterstützte Regeln werden verworfen und dürfen weder hängen noch stille Fantasietermine erzeugen.
- Ein freier QR-Betrag akzeptiert leer/0 als bewusst offenen Betrag und sonst exakt höchstens zwei Nachkommastellen bis CHF 100'000. Mehr Präzision wird abgelehnt statt gerundet.
- Das Play-Gate liest das tatsächliche signierte AAB-Manifest mit auf SHA-256 geprüftem Bundletool und akzeptiert nur die dokumentierte minimale Berechtigungsmenge. Neue legitime Bibliotheksrechte brauchen einen expliziten Review der Allowlist.

## Noch notwendige Abnahme

Synthetische Tests und CI prüfen keinen tatsächlichen Wechsel zwischen zwei produktiven Mitgliedern, keine Serverwirkung eines `change`-Events, keine echte Banking-App und keine Screenreader-Ausgabe auf physischen Geräten. Diese Pfade müssen vor breiter Verteilung mit separaten Testidentitäten, Testtransaktionen und TalkBack/VoiceOver geprüft werden. Ein signiertes iOS-IPA und die Play-Console-Konfiguration bleiben eigene Store-Schritte.
