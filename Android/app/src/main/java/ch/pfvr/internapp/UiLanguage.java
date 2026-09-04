package ch.pfvr.internapp;

import java.util.LinkedHashMap;
import java.util.Map;

/** Small exact-match localization layer for app-owned UI strings. */
final class UiLanguage {
    static final String DE = "de";
    static final String SWISS_GERMAN = "gsw";

    private static final Map<String, String> SWISS = new LinkedHashMap<>();

    static {
        // Shell / navigation
        put("Auf dem Rhein zuhause", "Uf em Rhy dihei");
        put("Zurück", "Zrugg");
        put("Termine", "Termin");
        put("Einst.", "Iist.");
        put("Vereinsbeiz bezahlen", "Vereinsbeiz zahle");
        put("Vereinsnews", "Vereinsnews");
        put("Einstellungen", "Iistellige");
        put("Kacheln anordnen", "Kachle aordne");
        put("Interner Bereich", "Interne Bereich");

        // Home / public data
        put("Rhein", "Rhy");
        put("Rhein aktuell", "Rhy aktuell");
        put("Abfluss, Pegel, Temperatur und Messdatenstand", "Abfluss, Pegel, Temperatur und Messdatestand");
        put("Als Nächstes", "Als Nöchscht");
        put("Aus dem öffentlichen Vereinskalender", "Us em öffentliche Vereinskaländer");
        put("Alle Termine anzeigen", "Alli Termin aazeige");
        put("Aktuell vom Verein", "Aktuell vom Verein");
        put("Alle News anzeigen", "Alli News aazeige");
        put("Heute", "Hüt");
        put("Morgen", "Morn");
        put("Mehr", "Meh");
        put("Öffnen", "Ufmache");
        put("Neu laden", "Neu lade");
        put("Aktualisieren", "Aktualisiere");
        put("Rhein-Grafiken", "Rhy-Grafike");
        put("Schifffahrtslage", "Schifffahrtslag");
        put("Wassertemperatur", "Wassertemperatur");

        // Settings
        put("Allgemein", "Allgmein");
        put("Einstellungen nach Bereich", "Iistellige nach Bereich");
        put("Zahlung", "Zahlig");
        put("Darstellung", "Darstellig");
        put("Gilt für die native App-Oberfläche", "Gilt für d App-Oberflächi");
        put("Sprache", "Sproch");
        put("App-Texte; externe und originale PFVR-Inhalte bleiben unverändert", "App-Texte; externi und originali PFVR-Inhalt blibed unveränderet");
        put("Persönlicher Zugang", "Persönliche Zuegang");
        put("Nur lokal auf diesem Gerät gespeichert", "Nur lokal uf däm Grät gspeicheret");
        put("Noch nicht eingerichtet", "No nöd igrichtet");
        put("Link ändern", "Link ändere");
        put("Link einrichten", "Link iirichte");
        put("Ansicht & Kacheln", "Aasicht & Kachle");
        put("Home, Kasse und Verein persönlich anordnen", "Home, Kasse und Verein sälber aordne");
        put("Standard wiederherstellen", "Standard wiederherstelle");
        put("Einblenden", "Iblände");
        put("Ausblenden", "Usblände");
        put("Nach oben", "Nache obe");
        put("Nach unten", "Nache unde");
        put("Hintergrundaktualisierung", "Hintergrund-Aktualisierig");

        // Cash / club
        put("Warenkorb", "Warenchorb");
        put("Gesamt", "Total");
        put("Freier Betrag", "Freie Betrag");
        put("Bezahlen", "Zahle");
        put("Zahlungsweg", "Zahligswäg");
        put("Geschichte", "Gschicht");
        put("Kontakt", "Kontakt");
        put("Vorstand", "Vorstand");
        put("Jahresprogramm", "Jahresprogramm");

        // Native toolbar around the internal page. Website/source controls are intentionally untouched.
        put("Personen", "Persone");
        put("App-Ansicht", "App-Aasicht");
        put("Original", "Original");

        // Common dialog actions
        put("Speichern", "Speichere");
        put("Abbrechen", "Abbräche");
        put("Schließen", "Schliesse");
        put("Löschen", "Lösche");
        put("Ändern", "Ändere");
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

        // Common action helper appends an arrow after its label.
        String arrowSuffix = "  →";
        if (value.endsWith(arrowSuffix)) {
            String base = value.substring(0, value.length() - arrowSuffix.length());
            String translated = SWISS.get(base);
            if (translated != null) return translated + arrowSuffix;
        }
        return value;
    }
}
