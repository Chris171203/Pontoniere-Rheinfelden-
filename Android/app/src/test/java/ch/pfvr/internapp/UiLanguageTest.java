package ch.pfvr.internapp;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

public class UiLanguageTest {
    @Test public void defaultsUnknownModesToGerman() {
        assertEquals(UiLanguage.DE, UiLanguage.normalizeMode(null));
        assertEquals(UiLanguage.DE, UiLanguage.normalizeMode("fr"));
        assertFalse(UiLanguage.isSwissGerman("de"));
    }

    @Test public void swissGermanTranslatesCoreNavigationAndHome() {
        assertTrue(UiLanguage.isSwissGerman(UiLanguage.SWISS_GERMAN));
        assertEquals("Uf em Rhy dihei", UiLanguage.translate("Auf dem Rhein zuhause", UiLanguage.SWISS_GERMAN));
        assertEquals("Zäme uf em Rhy.", UiLanguage.translate("Gemeinsam auf dem Rhein.", UiLanguage.SWISS_GERMAN));
        assertEquals("Als Nöchscht", UiLanguage.translate("Als Nächstes", UiLanguage.SWISS_GERMAN));
        assertEquals("Rhy aktuell", UiLanguage.translate("Rhein aktuell", UiLanguage.SWISS_GERMAN));
        assertEquals("Alli Termin aazeige  →", UiLanguage.translate("Alle Termine anzeigen  →", UiLanguage.SWISS_GERMAN));
        assertEquals("Sprooch", UiLanguage.translate("Sprache", UiLanguage.SWISS_GERMAN));
    }

    @Test public void swissGermanCoversFirstUseAndPublicDiscovery() {
        assertEquals("Erschtfreigab", UiLanguage.translate("Erstfreigabe", UiLanguage.SWISS_GERMAN));
        assertEquals("De Code stimmt nöd.", UiLanguage.translate("Code stimmt nicht.", UiLanguage.SWISS_GERMAN));
        assertEquals("Neu bim PFVR?", UiLanguage.translate("Neu beim PFVR?", UiLanguage.SWISS_GERMAN));
        assertEquals("Willkomme", UiLanguage.translate("Willkommen", UiLanguage.SWISS_GERMAN));
        assertEquals("Schnuppertraining & Mitglied werde", UiLanguage.translate("Schnuppertraining & Mitglied werden", UiLanguage.SWISS_GERMAN));
        assertEquals("Es Schnuppertraining isch au vor de Mitgliedschaft möglich. Infos zum Iistig, zur Mitgliedschaft und zu de Formular findsch uf pfvr.ch.", UiLanguage.translate("Schnuppertraining ist auch vor der Mitgliedschaft möglich. Infos zu Einstieg, Mitgliedschaft und Formularen findest du auf pfvr.ch.", UiLanguage.SWISS_GERMAN));
        assertEquals("Über de Verein", UiLanguage.translate("Über den Verein", UiLanguage.SWISS_GERMAN));
        assertEquals("Folg eus", UiLanguage.translate("Folge uns", UiLanguage.SWISS_GERMAN));
    }

    @Test public void dynamicAppOwnedLabelsAreTranslatedWithoutTouchingPayload() {
        assertEquals("Hintergrund-Aktualisierig: Aa", UiLanguage.translate("Hintergrundaktualisierung: Ein", UiLanguage.SWISS_GERMAN));
        assertEquals("Hintergrund-Aktualisierig: Us", UiLanguage.translate("Hintergrundaktualisierung: Aus", UiLanguage.SWISS_GERMAN));
        assertEquals("Rhy-Kachle 2", UiLanguage.translate("Rhein-Kachel 2", UiLanguage.SWISS_GERMAN));
        assertEquals("Schifffahrtslag · HWM I", UiLanguage.translate("Schifffahrtslage · HWM I", UiLanguage.SWISS_GERMAN));
    }

    @Test public void germanAndExternalContentRemainUntouched() {
        assertEquals("Als Nächstes", UiLanguage.translate("Als Nächstes", UiLanguage.DE));
        assertEquals("JP-Prüfungen & Endfahren", UiLanguage.translate("JP-Prüfungen & Endfahren", UiLanguage.SWISS_GERMAN));
        assertEquals("Kougionis Eleni", UiLanguage.translate("Kougionis Eleni", UiLanguage.SWISS_GERMAN));
    }

    @Test public void swissGermanCoversProjectedAttendanceAndSettingsDetails() {
        assertEquals("Mit Ässe", UiLanguage.translate("Mit Essen", UiLanguage.SWISS_GERMAN));
        assertEquals("Ohni Ässe", UiLanguage.translate("Ohne Essen", UiLanguage.SWISS_GERMAN));
        assertEquals("Nüt gwählt", UiLanguage.translate("Nicht gewählt", UiLanguage.SWISS_GERMAN));
        assertEquals("Persone verwalte", UiLanguage.translate("Personen verwalten", UiLanguage.SWISS_GERMAN));
        assertEquals("Us em Initiallink neu ufbaue", UiLanguage.translate("Aus Initiallink neu aufbauen", UiLanguage.SWISS_GERMAN));
        assertEquals("Kachle 1 isch immer sichtbar; Kachle 2 cha ii- oder usbländet werde.", UiLanguage.translate("Kachel 1 ist immer sichtbar; Kachel 2 kann ein- oder ausgeblendet werden.", UiLanguage.SWISS_GERMAN));
        assertEquals("Rhy-Kachle", UiLanguage.translate("Rhein-Kachel", UiLanguage.SWISS_GERMAN));
        assertEquals("Direkti QR-Übergab dokumentiert", UiLanguage.translate("Direkte QR-Übergabe dokumentiert", UiLanguage.SWISS_GERMAN));
    }
}
