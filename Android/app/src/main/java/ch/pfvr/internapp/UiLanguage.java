package ch.pfvr.internapp;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Exact-match localization for app-owned UI text.
 *
 * <p>The Swiss German variant deliberately stays readable and moderately
 * north-western/Aargau-ish instead of pretending there is one standardized
 * written Swiss German. Proper names, source data, calendar entries, news and
 * the original PFVR website are intentionally not translated.</p>
 */
final class UiLanguage {
    static final String DE = "de";
    static final String SWISS_GERMAN = "gsw";

    private static final Map<String, String> SWISS = new LinkedHashMap<>();

    static {
        // Shell / navigation / landing
        put("Auf dem Rhein zuhause", "Uf em Rhy dihei");
        put("Zurück", "Zrugg");
        put("Termine", "Termin");
        put("Einst.", "Iist.");
        put("Einstellungen", "Iistellige");
        put("Verein & Kontakt", "Verein & Kontakt");
        put("Kacheln anordnen", "Kachle aordne");
        put("Interner Bereich", "Interne Bereich");
        put("Erstfreigabe", "Erschtfreigab");
        put("Willkommen", "Willkomme");
        put("App freischalten", "App freischalte");
        put("Diese App kann interne Vereinsinformationen anzeigen. Gib den Freigabecode ein.", "Die App cha interni Vereinsinfos aazeige. Gib de Freigabecode ii.");
        put("Der Code wird nur zur lokalen Erstfreigabe geprüft. Persönliche PFVR-Links bleiben weiterhin ausschließlich auf diesem Gerät.", "De Code wird nume für d lokali Erschtfreigab prüeft. Persönlichi PFVR-Links blibed witerhin nur uf däm Grät.");
        put("Code stimmt nicht.", "De Code stimmt nöd.");
        put("Freischalten", "Freischalte");
        put("Neu beim PFVR?", "Neu bim PFVR?");
        put("Schnuppertraining ist auch vor der Mitgliedschaft möglich. Infos zu Einstieg, Mitgliedschaft und Formularen findest du auf pfvr.ch.", "Es Schnuppertraining isch au vor de Mitgliedschaft möglich. Infos zum Iistig, zur Mitgliedschaft und zu de Formular findsch uf pfvr.ch.");
        put("Schnuppertraining & Mitglied werden", "Schnuppertraining & Mitglied werde");
        put("Mehr erfahren", "Meh erfahre");
        put("Folge uns", "Folg eus");

        // Home
        put("RHEINFELDEN  •  SEIT 1896", "RHEINFELDEN  •  SIT 1896");
        put("Gemeinsam auf dem Rhein.", "Zäme uf em Rhy.");
        put("Training, Wettfahren und Vereinsleben – alles Wichtige direkt griffbereit.", "Training, Wettfahre und Vereinsläbe – s Wichtigschte grad zur Hand.");
        put("An-/Abmelden", "Aa-/Abmälde");
        put("Bezahlen", "Zahle");
        put("Trainingswetter", "Trainingswätter");
        put("Prognose für den nächsten relevanten Termin", "Prognose für de nöchscht relevant Termin");
        put("Rhein", "Rhy");
        put("Rhein aktuell", "Rhy aktuell");
        put("Rhein-Grafiken", "Rhy-Grafike");
        put("Rhein-Anzeige", "Rhy-Aazeig");
        put("Rhein-Kachel", "Rhy-Kachle");
        put("Kachel 1 ist immer sichtbar; Kachel 2 kann ein- oder ausgeblendet werden.", "Kachle 1 isch immer sichtbar; Kachle 2 cha ii- oder usbländet werde.");
        put("Offizielle Hochwassermarken des Pegels Basel-Rheinhalle; der Abfluss bleibt ein separater Messwert.", "Offizielli Hochwassermarke vom Pegel Basel-Rheinhalle; de Abfluss blibt en separate Messwärt.");
        put("Abfluss, Pegel, Temperatur und Messdatenstand", "Abfluss, Pegel, Temperatur und Messdatestand");
        put("Abfluss und Pegel je Station; Temperatur separat", "Abfluss und Pegel pro Station; Temperatur separat");
        put("Als Nächstes", "Als Nöchscht");
        put("Nächste Termine", "Nöchschti Termin");
        put("Aus dem öffentlichen Vereinskalender", "Us em öffentliche Vereinskaländer");
        put("Alle Termine anzeigen", "Alli Termin aazeige");
        put("Alle Termine anzeigen  →", "Alli Termin aazeige  →");
        put("Termine werden geladen …", "Termin wärded glade …");
        put("Aktuell vom Verein", "Aktuell vom Verein");
        put("Alle News anzeigen", "Alli News aazeige");
        put("Alle News anzeigen  →", "Alli News aazeige  →");
        put("News von pfvr.ch", "News vo pfvr.ch");
        put("News werden geladen …", "News wärded glade …");
        put("Noch kein gespeicherter News-Stand.", "No kei gspeicherete News-Stand.");
        put("Heute", "Hüt");
        put("Morgen", "Morn");
        put("Mehr", "Meh");
        put("Öffnen", "Ufmache");
        put("Öffnen  →", "Ufmache  →");
        put("Neu laden", "Neu lade");
        put("Aktualisieren", "Aktualisiere");
        put("NÄCHSTES TRAINING", "NÖCHSCHTS TRAINING");
        put("Wetter wird geladen …", "Wätter wird glade …");
        put("Prognose wird im Hintergrund aktualisiert.", "D Prognose wird im Hintergrund aktualisiert.");
        put("Noch keine Prognose", "No kei Prognose");
        put("Für diesen Trainingszeitraum liegen noch keine Stundenwerte vor.", "Für dä Trainingsziitruum git s no kei Stundewärt.");
        put("Gespeicherte Wetterdaten nicht lesbar", "Gspeichereti Wätterdate sind nöd läsbar");
        put("Letzter Stand bleibt erhalten, sobald wieder gültige Daten vorliegen.", "De letscht Stand blibt erhalte, bis wieder gültigi Date ume sind.");
        put("klar", "klar");
        put("leicht bewölkt", "liecht bewölkt");
        put("bewölkt", "bewölkt");
        put("Nebel", "Näbel");
        put("Nieselregen", "Nieselräge");
        put("Regen", "Räge");
        put("Schnee", "Schnee");
        put("Schauer", "Schauer");
        put("Schneeschauer", "Schneeschauer");
        put("Gewitter", "Gwitter");
        put("Wetter", "Wätter");
        put("Wind", "Wind");
        put("Böen", "Böe");
        put("niedrig", "niedrig");
        put("mässig", "mässig");
        put("hoch", "hoch");
        put("sehr hoch", "sehr hoch");
        put("extrem", "extrem");
        put("Regelmässiges Training", "Regelmässigs Training");
        put("Vereinskalender", "Vereinskaländer");
        put("Regelplan", "Regelplan");
        put("vor", "vor");

        // River / charts
        put("Abfluss", "Abfluss");
        put("Pegel", "Pegel");
        put("Wassertemperatur", "Wassertemperatur");
        put("Schifffahrtslage", "Schifffahrtslag");
        put("Schifffahrtslage richtet sich nach Pegel Basel-Rheinhalle.", "D Schifffahrtslag richtet sich nach em Pegel Basel-Rheinhalle.");
        put("Ausschlaggebend für die Hochwassermarken ist der Pegel Basel-Rheinhalle. Abflusswerte werden nur ergänzend angezeigt.", "Für d Hochwassermarke isch de Pegel Basel-Rheinhalle massgebend. D Abflusswärt sind nume ergänzend.");
        put("Pegel Basel-Rheinhalle · offizielle Schifffahrtslage", "Pegel Basel-Rheinhalle · offizielli Schifffahrtslag");
        put("Diagramm berühren für Einzelwerte", "Diagramm antippe für Einzelwärt");
        put("Livewerte", "Livewärt");
        put("10-Minuten-Mittel", "10-Minute-Mittel");
        put("Stundenmittel", "Stundemittel");
        put("Abfluss links", "Abfluss links");
        put("Pegel rechts", "Pegel rächts");
        put("Rheinwerte", "Rhywärt");
        put("im Diagramm in Meter über Meer", "im Diagramm in Meter über Meer");
        put("im Diagramm in Zentimetern", "im Diagramm in Zentimeter");
        put("Für den gewählten Zeitraum liegen noch nicht genügend Temperaturwerte vor.", "Für de gwählt Ziitruum git s no nöd gnueg Temperaturwärt.");
        put("Für diesen Zeitraum liegen noch nicht genug Abfluss- und Pegelwerte vor.", "Für dä Ziitruum git s no nöd gnueg Abfluss- und Pegelwärt.");
        put("ZEITRAUM", "ZIITRUUM");
        put("PEGEL-EINHEIT", "PEGEL-EIHEIT");
        put("Station wählen", "Station uswähle");
        put("Aktiv", "Aktiv");
        put("HWM I · Voralarm", "HWM I · Voralarm");
        put("HWM IIb · Sperre", "HWM IIb · Sperri");
        put("HWM IIa · Sperre", "HWM IIa · Sperri");
        put("Hochwassermarke I", "Hochwassermarke I");
        put("Basel, Rheinhalle · Abfluss & Pegel", "Basel, Rheinhalle · Abfluss & Pegel");
        put("Rheinfelden · Abfluss, Pegel & Wassertemperatur", "Rheinfelde · Abfluss, Pegel & Wassertemperatur");
        put("Abfluss · Pegel · Wassertemperatur", "Abfluss · Pegel · Wassertemperatur");
        put("Abfluss · Pegel", "Abfluss · Pegel");
        put("Grenzwerte", "Grenzwärt");
        put("Niedrig unter m³/s", "Niedrig under m³/s");
        put("Warnung ab m³/s", "Warnig ab m³/s");
        put("Alarm ab m³/s", "Alarm ab m³/s");
        put("Niedrig", "Niedrig");
        put("Gut", "Guet");
        put("Warnung", "Warnig");
        put("Alarm", "Alarm");
        put("Keine Daten", "Kei Date");
        put("Keine Lage", "Kei Lag");
        put("Normal", "Normal");
        put("HWM I", "HWM I");
        put("Sperre IIb", "Sperri IIb");
        put("Sperre IIa", "Sperri IIa");
        put("Basel-Pegel derzeit nicht verfügbar.", "De Basel-Pegel isch grad nöd verfüegbar.");
        put("Voralarm ab 700 cm Pegel Basel-Rheinhalle.", "Voralarm ab 700 cm Pegel Basel-Rheinhalle.");
        put("Kleinschifffahrt und Fähren Basel–Rheinfelden gesperrt.", "Chliischifffahrt und Fähre Basel–Rheinfelde gsperrt.");
        put("Kleinschifffahrt und Fähren Basel–Rheinfelden gesperrt", "Chliischifffahrt und Fähre Basel–Rheinfelde gsperrt");
        put("Schifffahrt Rheinfelden–Kembs gesperrt.", "Schifffahrt Rheinfelde–Kembs gsperrt.");
        put("Schifffahrt Rheinfelden–Kembs gesperrt", "Schifffahrt Rheinfelde–Kembs gsperrt");
        put("Unter Hochwassermarke I (< 700 cm).", "Under Hochwassermarke I (< 700 cm).");
        put("RHEIN", "RHY");
        put("Wird geladen …", "Wird glade …");
        put("Temperatur", "Temperatur");
        put("Wasser", "Wasser");
        put("Messwerte derzeit unvollständig", "Messwärt sind grad unvollständig");
        put("Gespeicherter Stand", "Gspeicherete Stand");
        put("Messdaten nicht lesbar", "Messdate nöd läsbar");

        // Calendar / news
        put("Jahresprogramm", "Jahresprogramm");
        put("Termine aus dem öffentlichen Vereinskalender – lokal gespeichert und auch ohne Verbindung sichtbar.", "Termin us em öffentliche Vereinskaländer – lokal gspeicheret und au ohni Verbindig sichtbar.");
        put("Originalkalender", "Originalkaländer");
        put("Kalender wird im Hintergrund geladen …", "Kaländer wird im Hintergrund glade …");
        put("Noch kein gespeicherter Kalenderstand vorhanden.", "No kei gspeicherete Kaländerstand vorhande.");
        put("ganztägig", "de ganz Tag");
        put("Ort", "Ort");
        put("Termin teilen", "Termin teile");
        put("Erster Abruf läuft im Hintergrund.", "De erscht Abruef lauft im Hintergrund.");
        put("Noch kein lokaler Kalender-Cache.", "No kei lokale Kaländer-Cache.");
        put("heute", "hüt");
        put("Lokal gespeichert · zuletzt aktualisiert", "Lokal gspeicheret · letscht Aktualisierig");
        put("Abgesagt", "Abgsagt");
        put("●  Termin abgesagt", "●  Termin abgsagt");
        put("Details", "Details");
        put("Teilen", "Teile");
        put("Zum Kalender", "Zum Kaländer");
        put("Route öffnen", "Route ufmache");
        put("Vereinsnews", "Vereinsnews");
        put("Direkt aus dem öffentlichen PFVR-WordPress-Feed · lokal gespeichert.", "Direkt us em öffentliche PFVR-WordPress-Feed · lokal gspeicheret.");
        put("News aktualisieren", "News aktualisiere");
        put("Artikel öffnen", "Artikel ufmache");
        put("Artikel öffnen  →", "Artikel ufmache  →");
        put("Original-Newsarchiv auf pfvr.ch  ↗", "Original-Newsarchiv uf pfvr.ch  ↗");
        put("Noch keine Vereinsnews im lokalen Cache.", "No kei Vereinsnews im lokale Cache.");
        put("Abruf läuft", "Abruef lauft");
        put("noch kein Stand", "no kei Stand");
        put("Stand", "Stand");

        // Settings
        put("Allgemein", "Allgemein");
        put("Einstellungen nach Bereich", "Iistellige nach Bereich");
        put("Zahlung", "Zahlig");
        put("Darstellung", "Darstellig");
        put("Gilt für die native App-Oberfläche", "Gilt für d App-Oberflächi");
        put("Farbschema", "Farbschema");
        put("System / Hell / Dunkel", "System / Hell / Dunkel");
        put("System · folgt Android", "System · folgt Android");
        put("Hell", "Hell");
        put("Dunkel", "Dunkel");
        put("Sprache", "Sprooch");
        put("App-Texte; externe und originale PFVR-Inhalte bleiben unverändert", "App-Texte; externi und originali PFVR-Inhalt blibed unveränderet");
        put("Persönlicher Zugang", "Persönliche Zuegang");
        put("Nur lokal auf diesem Gerät gespeichert", "Nur lokal uf däm Grät gspeicheret");
        put("Interner PFVR-Link", "Interne PFVR-Link");
        put("Noch nicht eingerichtet", "No nöd igrichtet");
        put("Link ändern", "Link ändere");
        put("Link einrichten", "Link iirichte");
        put("Ansicht & Kacheln", "Aasicht & Kachle");
        put("Home, Kasse und Verein persönlich anordnen", "Home, Kasse und Verein sälber aordne");
        put("Kacheln anordnen und ausblenden", "Kachle aordne und usblände");
        put("nach oben", "nache obe");
        put("nach unten", "nache unde");
        put("Ansicht & Kacheln öffnen", "Aasicht & Kachle ufmache");
        put("Die Auswahl wird nur auf diesem Gerät gespeichert. Neue Kacheln werden bei späteren Updates automatisch ergänzt.", "D Uswahl wird nume uf däm Grät gspeicheret. Neui Kachle wärded bi spätere Updates automatisch ergänzt.");
        put("Reihenfolge und Sichtbarkeit gelten nur auf diesem Gerät. Kompakte Kacheln werden automatisch paarweise angeordnet.", "Reihefolg und Sichtbarkeit gälted nume uf däm Grät. Kompakti Kachle wärded automatisch paarwiis aordnet.");
        put("Standard wiederherstellen", "Standard wiederherstelle");
        put("auf Standard zurücksetzen", "uf Standard zruggsetze");
        put("fixiert", "fixiert");
        put("Fixiert", "Fixiert");
        put("Immer an", "Immer aa");
        put("Sichtbar", "Sichtbar");
        put("Aus", "Us");
        put("Ein", "Aa");
        put("Home", "Home");
        put("Kasse", "Kasse");
        put("Verein", "Verein");
        put("Einblenden", "Iblände");
        put("Ausblenden", "Usblände");
        put("Nach oben", "Nache obe");
        put("Nach unten", "Nache unde");
        put("Daten", "Date");
        put("Kalender", "Kaländer");
        put("kein Stand", "kei Stand");
        put("gerade eben", "grad eben");
        put("Kalender, Training-Wetter und Rhein-Messwerte", "Kaländer, Trainingswätter und Rhy-Messwärt");
        put("Lokaler Cache", "Lokale Cache");
        put("Beim Start wird zuerst der letzte erfolgreiche Stand angezeigt und anschließend im Hintergrund aktualisiert.", "Bim Start wird zerscht de letscht erfolgrich Stand aazeigt und nachher im Hintergrund aktualisiert.");
        put("Alle Daten aktualisieren", "Alli Date aktualisiere");
        put("Daten-Cache leeren", "Date-Cache leere");
        put("Hintergrundaktualisierung", "Hintergrund-Aktualisierig");
        put("Live-Daten werden bei geöffneter App regelmäßig geprüft. Im Hintergrund aktualisiert Android bei verfügbarer Verbindung best effort; Energiesparmodi können die Ausführung verzögern.", "Live-Date wärded bi offener App regelmässig prüeft. Im Hintergrund aktualisiert Android bi verfüegbarer Verbindig best effort; Energiesparmodi chönd d Usfüehrig verzögere.");
        put("App", "App");
        put("1.0.0 bleibt für den ersten offiziellen Release reserviert.", "1.0.0 blibt für de erscht offiziell Release reserviert.");

        // Tile editor
        put("Auswahl", "Uswahl");
        put("Standard", "Standard");
        put("Breite Kachel", "Breiti Kachle");
        put("Kompakte Kachel", "Kompakti Kachle");

        // Club / public info
        put("Gegründet 1896 · Sport und Vereinsleben am Rhein", "Gründet 1896 · Sport und Vereinsläbe am Rhy");
        put("Über den Verein", "Über de Verein");
        put("Seit 1896 auf dem Rhein", "Sit 1896 uf em Rhy");
        put("Beim Pontonierfahren verbinden sich präzise Bootsführung, Kraft, Technik und Teamarbeit. Der PFVR trainiert auf dem Rhein in Rheinfelden, nimmt an Wettfahren teil und pflegt zugleich ein aktives Vereinsleben sowie die Ausbildung des Nachwuchses.", "Bim Pontonierfahre ghöred präzisi Bootsfüehrig, Chraft, Technik und Teamarbeit zäme. De PFVR trainiert uf em Rhy z Rheinfelde, macht bi Wettfahre mit und pflegt es aktives Vereinsläbe sowie d Usbildig vom Nachwuchs.");
        put("Geschichte", "Gschicht");
        put("Geschichte und Meilensteine öffnen", "Gschicht und Meilestei ufmache");
        put("Geschichte und Meilensteine öffnen  →", "Gschicht und Meilestei ufmache  →");
        put("Aktuelle Meldungen", "Aktuelli Meldige");
        put("Termine und Kalender", "Termin und Kaländer");
        put("Funktionen und Kontakte", "Funktione und Kontakt");
        put("Depot & Route", "Depot & Wäg");
        put("Kontaktseite", "Kontakt-Site");
        put("Weitere Ansprechwege", "Witeri Kontaktmöglichkeite");
        put("Schnuppertraining auch vor der Mitgliedschaft · Infos und Formulare", "Schnuppertraining au vor de Mitgliedschaft · Infos und Formular");
        put("Offizielle Kanäle des PFVR", "Offizielli Kanäl vom PFVR");
        put("Kontakt", "Kontakt");
        put("Telefon", "Telefon");
        put("E-Mail", "E-Mail");
        put("Vorstand", "Vorstand");
        put("Instagram", "Instagram");
        put("Facebook", "Facebook");

        // Cash / payment
        put("Vereinsbeiz bezahlen", "Vereinsbeiz zahle");
        put("VEREINSBEIZ", "VEREINSBEIZ");
        put("Warenkorb", "Warenchorb");
        put("Ausgewählte Artikel und Zahlungswege", "Usgwählti Artikel und Zahligswäg");
        put("Für Direktzahlungen einmalig eine Banking-App festlegen", "Für Direktzahlig einisch e Banking-App festlege");
        put("Für die direkte Übergabe des Swiss QR", "Für d direkt Übergab vom Swiss QR");
        put("Für Sonderfälle oder Beträge ausserhalb der Preisliste", "Für Sonderfäll oder Beträg usserhalb vo de Priisliste");
        put("Code oder Vereins-QR verwenden", "Code oder Vereins-QR verwände");
        put("Preisliste konnte nicht geladen werden.", "D Priisliste het nöd chönne glade werde.");
        put("Preisliste Vereinsbeiz · Stand", "Priisliste Vereinsbeiz · Stand");
        put("Trinken", "Trinke");
        put("Essen", "Ässe");
        put("Feiern", "Fiire");
        put("Gesamt", "Total");
        put("Noch keine Artikel ausgewählt.", "No kei Artikel usgwählt.");
        put("Artikel für dich, Kinder oder die ganze Runde zusammenstellen – oder weiterhin einen freien Betrag verwenden.", "Artikel für dich, Chind oder d ganz Rundi zämeschtelle – oder witerhin en freie Betrag verwände.");
        put("Freier Betrag", "Freie Betrag");
        put("Leer oder 0 erzeugt einen Swiss QR mit offenem Betrag.", "Leer oder 0 git en Swiss QR mit offem Betrag.");
        put("Konsumation bezahlen", "Konsumation zahle");
        put("Warenkorb leeren", "Warenchorb leere");
        put("Entfernen", "Entferne");
        put("Zahlungsweg", "Zahligswäg");
        put("Zahlungsdaten", "Zahligsdate");
        put("Für E-Banking und manuelle Überweisung", "Für E-Banking und manuelli Überwiisig");
        put("IBAN kopieren", "IBAN kopiere");
        put("Alles kopieren", "Alles kopiere");
        put("Swiss QR", "Swiss QR");
        put("Swiss QR erstellen", "Swiss QR erstelle");
        put("TWINT-Code erzeugen", "TWINT-Code erzeuge");
        put("Vereins-TWINT-QR öffnen", "Vereins-TWINT-QR ufmache");
        put("TWINT", "TWINT");
        put("Banking-App", "Banking-App");
        put("Direkte QR-Übergabe dokumentiert", "Direkti QR-Übergab dokumentiert");
        put("QR-Datei als Fallback", "QR-Datei als Fallback");
        put("Scanner als Fallback", "Scanner als Fallback");
        put("Fallback automatisch prüfen", "Fallback automatisch prüefe");
        put("Banking-App auswählen", "Banking-App uswähle");
        put("Bank auswählen", "Bank uswähle");
        put("In Einstellungen ändern", "I de Iistellige ändere");
        put("Banking-App festlegen", "Banking-App festlege");
        put("Noch keine Banking-App festgelegt", "No kei Banking-App feschtgleit");
        put("Keine Banking-App gewählt", "Kei Banking-App gwählt");
        put("dokumentiert", "dokumentiert");
        put("Geräteprüfung", "Gräteprüefig");
        put("Für Direktzahlungen zuerst eine Banking-App festlegen.", "Für Direktzahlig zerscht e Banking-App festlege.");
        put("Mit", "Mit");
        put("bezahlen", "zahle");
        put("IBAN kopiert", "IBAN kopiert");
        put("Zahlungsdaten kopiert", "Zahligsdate kopiert");
        put("Bereit", "Bereit");
        put("Einrichten", "Iirichte");
        put("Ändern", "Ändere");
        put("So funktioniert die Direktzahlung", "So funktioniert d Direktzahlig");
        put("Direkte Übergabe versucht den QR als temporäres Bild an eine kompatible Banking-App zu senden. Falls die Bank das nicht unterstützt, bleibt Speichern/Öffnen als Fallback.", "D direkt Übergab probiert de QR als temporärs Bild a e kompatibli Banking-App z schicke. Wenn d Bank das nöd unterstützt, blibt Speichere/Ufmache als Fallback.");
        put("Für Zahlung auf demselben Handy: Betrag übernehmen und auf der PFVR-Seite den fünfstelligen TWINT-Code erzeugen. Der Vereins-QR bleibt zusätzlich verfügbar.", "Für Zahlig uf em gliiche Handy: Betrag übernäh und uf de PFVR-Site de füüfstellig TWINT-Code erzeuge. De Vereins-QR blibt zusätzlich verfüegbar.");
        put("Je nach Banking-App nutzt PFVR direkte QR-Übergabe, Dateiimport oder einen sicheren manuellen Fallback. Die App prüft zusätzlich zur bekannten Bank-Matrix die tatsächlich auf diesem Gerät angebotenen Android-Schnittstellen.", "Je nach Banking-App nutzt PFVR d direkt QR-Übergab, Dateiimport oder en sichere manuelle Fallback. D App prüeft zusätzlich zur bekannte Bank-Matrix d Android-Schnittstelle, wo uf däm Grät tatsächlich aabote wärded.");
        put("Die lokale Preisliste ist derzeit nicht verfügbar.", "D lokali Priisliste isch grad nöd verfüegbar.");
        put("Preisliste nicht verfügbar.", "Priisliste nöd verfüegbar.");

        // Internal native wrapper. Original/site-provided controls intentionally stay unchanged.
        put("Personen", "Persone");
        put("App-Ansicht", "App-Aasicht");
        put("Original", "Original");
        put("Kein Intern-Zugang eingerichtet", "Kei Intern-Zuegang igrichtet");
        put("Der persönliche PFVR-Link wird zentral unter Einstellungen verwaltet.", "De persönlich PFVR-Link wird zentral i de Iistellige verwaltet.");
        put("Zu den Einstellungen", "Zu de Iistellige");
        put("Interner Bereich konnte nicht geladen werden", "De interne Bereich het nöd chönne glade werde");
        put("Prüfe den persönlichen Link unter Einstellungen oder tippe oben auf Neu laden.", "Prüef de persönliche Link i de Iistellige oder tipp obe uf Neu lade.");
        put("Personenverwaltung ist auf dieser Seite nicht verfügbar.", "D Personeverwaltig isch uf dere Site nöd verfüegbar.");
        put("Persönlichen Intern-Link ändern", "Persönliche Intern-Link ändere");
        put("Bitte den persönlichen An-/Abmelde-Link (what=abmeldung) verwenden.", "Bitte de persönliche Aa-/Abmäld-Link (what=abmeldung) verwände.");
        put("Link konnte nicht geöffnet werden.", "De Link het nöd chönne göffnet werde.");
        put("Mit Essen", "Mit Ässe");
        put("Ohne Essen", "Ohni Ässe");
        put("Nicht gewählt", "Nüt gwählt");
        put("Komme nicht", "Ich chumme nöd");
        put("Keine Auswahl für diesen Termin", "Für dä Termin isch nüt gwählt");
        put("Aus Initiallink neu aufbauen", "Us em Initiallink neu ufbaue");
        put("Ansicht bereinigen", "Aasicht bereinige");
        put("Falls die Personenansicht festhängt oder zu viele Personen geladen wurden, kann sie aus dem gespeicherten persönlichen Initiallink vollständig neu aufgebaut werden.", "Wenn d Personeaasicht feschthängt oder z viel Persone glade worde sind, chasch sie us em gspeicherte persönliche Initiallink komplett neu ufbaue.");
        put("Nochmal tippen: wirklich neu aufbauen", "No einisch tippe: würkli neu ufbaue");
        put("Initiallink nicht verfügbar", "Initiallink nöd verfüegbar");
        put("Personen verwalten", "Persone verwalte");
        put("Person hinzufügen:", "Person hinzuefüege:");
        put("Person hinzufügen", "Person hinzuefüege");
        put("Hinzufügen ist auf diesem Seitenstand nicht verfügbar. Vorhandene Zusatzpersonen können weiterhin entfernt werden oder die Ansicht kann unten aus dem Initiallink neu aufgebaut werden.", "Hinzuefüege isch bi däm Sitestand nöd verfüegbar. Vorhandeni Zusatzpersone chönd witerhin entfernt werde oder d Aasicht cha unde us em Initiallink neu ufbaut werde.");
        put("Entfernen aktualisiert die Personenliste automatisch.", "Entferne aktualisiert d Personelischte automatisch.");
        put("Aktuelle Personen", "Aktuelli Persone");
        put("Person aus Ansicht entfernen: ", "Person us de Aasicht entferne: ");
        put("Teilnehmer", "Person");

        // Common feedback / dialogs
        put("Aktualisierung gestartet.", "Aktualisierig gstartet.");
        put("Aktualisierung läuft …", "Aktualisierig lauft …");
        put("Keine News", "Kei News");
        put("Quelle: Schweizerische Rheinhäfen  ↗", "Quelle: Schwiizerischi Rhyhäfe  ↗");
        put("BAFU Live-Daten", "BAFU-Livedate");
        put("Abfluss links · Pegel rechts", "Abfluss links · Pegel rächts");
        put("Alle Apps", "Alli Apps");
        put("Banking-App öffnen", "Banking-App ufmache");
        put("Bankzahlung · Swiss QR", "Bankzahlig · Swiss QR");
        put("Bitte einen gültigen CHF-Betrag eingeben oder Feld leer/0 für offenen Betrag lassen.", "Bitte en gültige CHF-Betrag iigeh oder s Feld leer/0 lah für en offene Betrag.");
        put("Bitte einen gültigen CHF-Betrag eingeben oder das Feld leer lassen.", "Bitte en gültige CHF-Betrag iigeh oder s Feld leer lah.");
        put("Bitte einen gültigen CHF-Betrag eingeben.", "Bitte en gültige CHF-Betrag iigeh.");
        put("Bitte unter Einstellungen → Zahlung eine Banking-App festlegen.", "Bitte under Iistellige → Zahlig e Banking-App festlege.");
        put("Daten-Cache geleert. Neue Daten werden nachgeladen.", "Date-Cache gleert. Neui Date wärded nachglade.");
        put("Der Warenkorb ist leer.", "De Warenchorb isch leer.");
        put("Die gewählte Banking-App ist nicht mehr verfügbar.", "D gwählti Banking-App isch nüm verfüegbar.");
        put("Direkt an Banking-App", "Direkt a Banking-App");
        put("Direkte QR-Übergabe nicht möglich. QR wird stattdessen angezeigt.", "Direkti QR-Übergab isch nöd möglich. De QR wird stattdesse aazeigt.");
        put("Grenzwerte müssen aufsteigend sein: Niedrig < Warnung < Alarm.", "D Grenzwärt müend ufstiigend sii: Niedrig < Warnig < Alarm.");
        put("Installierte App auswählen", "Installierti App uswähle");
        put("Kalender lädt im Hintergrund. Bei langsamer Verbindung kann der erste Abruf etwas dauern.", "De Kaländer ladet im Hintergrund. Bi langsamer Verbindig cha de erscht Abruef öppis duure.");
        put("Kalender-Aktualisierung läuft bereits.", "D Kaländer-Aktualisierig lauft scho.");
        put("Kein QR-Code vorhanden.", "Kei QR-Code vorhande.");
        put("Keine App unterstützt die direkte QR-Übergabe. QR wird stattdessen angezeigt.", "Kei App unterstützt d direkt QR-Übergab. De QR wird stattdesse aazeigt.");
        put("Keine Kalender-App gefunden.", "Kei Kaländer-App gfunde.");
        put("Keine weitere installierte App gefunden.", "Kei witeri installierti App gfunde.");
        put("Personen hinzufügen oder entfernen", "Persone hinzuefüege oder entferne");
        put("QR speichern", "QR speichere");
        put("QR-Code konnte nicht gespeichert werden.", "De QR-Code het nöd chönne gspeicheret werde.");
        put("Schliessen", "Schliesse");
        put("Speichern ist auf diesem Gerät nicht verfügbar.", "Speichere isch uf däm Grät nöd verfüegbar.");
        put("Swiss QR gespeichert.", "Swiss QR gspeicheret.");
        put("Swiss QR konnte nicht erzeugt werden.", "Swiss QR het nöd chönne erstellt werde.");
        put("Teilen ist auf diesem Gerät nicht verfügbar.", "Teile isch uf däm Grät nöd verfüegbar.");
        put("Kacheln zurückgesetzt.", "Kachle zruggsetzt.");
        put("News aktualisiert", "News aktualisiert");
        put("News konnten gerade nicht geladen werden.", "D News händ grad nöd chönne glade werde.");
        put("Keine Verbindung – gespeicherte News bleiben sichtbar.", "Kei Verbindig – gspeichereti News blibed sichtbar.");
        put("Zahlungsdaten an", "Zahligsdate a");
        put("Swiss QR an", "Swiss QR a");
        put("Swiss QR mit", "Swiss QR mit");
        put("Zahlungsbild an", "Zahligsbild a");
        put("übergeben.", "übergeh.");
        put("geöffnet.", "göffnet.");
        put("festgelegt.", "feschtgleit.");
        put("kommende Termine aktualisiert", "kommendi Termin aktualisiert");
        put("Kalender konnte gerade nicht geladen werden.", "De Kaländer het grad nöd chönne glade werde.");
        put("Keine Verbindung – gespeicherter Kalenderstand bleibt sichtbar.", "Kei Verbindig – de gspeichereti Kaländerstand blibt sichtbar.");
        put("QR-Bildübergabe wurde von dieser App nicht angeboten – Banking-App geöffnet und Zahlungsdaten kopiert.", "D QR-Bildübergab wird vo dere App nöd aabote – Banking-App göffnet und Zahligsdate kopiert.");
        put("Direkter QR-Import wurde von dieser App nicht angeboten – Zahlungsdaten wurden kopiert.", "Direkte QR-Import wird vo dere App nöd aabote – Zahligsdate sind kopiert worde.");
        put("Zahlungsdaten kopiert – QR-Datei bei Bedarf in der Banking-App auswählen.", "Zahligsdate kopiert – QR-Datei bi Bedarf i de Banking-App uswähle.");

        // Common dialog actions
        put("Speichern", "Speichere");
        put("Abbrechen", "Abbräche");
        put("Schließen", "Schliesse");
        put("Löschen", "Lösche");
    }

    private UiLanguage() {}

    private static void put(String german, String swissGerman) {
        SWISS.put(german, swissGerman);
    }

    static String normalizeMode(String mode) {
        return SWISS_GERMAN.equals(mode) ? SWISS_GERMAN : DE;
    }

    static boolean isSwissGerman(String mode) {
        return SWISS_GERMAN.equals(normalizeMode(mode));
    }

    static String translate(String value, String mode) {
        if (value == null || !isSwissGerman(mode)) return value;
        String direct = SWISS.get(value);
        if (direct != null) return direct;

        String arrowSuffix = "  →";
        if (value.endsWith(arrowSuffix)) {
            String base = value.substring(0, value.length() - arrowSuffix.length());
            String translated = SWISS.get(base);
            if (translated != null) return translated + arrowSuffix;
        }

        // A few app-owned labels are assembled dynamically. Translate only
        // well-known prefixes/suffixes so external calendar/news text is never touched.
        if (value.startsWith("Hintergrundaktualisierung: ")) {
            String state = value.substring("Hintergrundaktualisierung: ".length());
            return "Hintergrund-Aktualisierig: " + ("Ein".equals(state) ? "Aa" : ("Aus".equals(state) ? "Us" : state));
        }
        if (value.startsWith("Schifffahrtslage · ")) {
            return "Schifffahrtslag · " + value.substring("Schifffahrtslage · ".length());
        }
        if (value.startsWith("Wassertemperatur · ")) {
            return "Wassertemperatur · " + value.substring("Wassertemperatur · ".length());
        }
        if (value.startsWith("Testversion ")) {
            return "Testversion " + value.substring("Testversion ".length());
        }
        if (value.startsWith("Rhein-Kachel ")) {
            return "Rhy-Kachle " + value.substring("Rhein-Kachel ".length());
        }
        return value;
    }
}
