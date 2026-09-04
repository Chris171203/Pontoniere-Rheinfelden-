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
        assertEquals("Schnuppertraining & Mitglied werde", UiLanguage.translate("Schnuppertraining & Mitglied werden", UiLanguage.SWISS_GERMAN));
        assertEquals("Über de Verein", UiLanguage.translate("Über den Verein", UiLanguage.SWISS_GERMAN));
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
}
