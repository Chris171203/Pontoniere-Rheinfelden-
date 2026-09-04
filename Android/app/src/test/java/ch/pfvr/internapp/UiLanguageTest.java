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

    @Test public void swissGermanTranslatesAppOwnedLabels() {
        assertTrue(UiLanguage.isSwissGerman(UiLanguage.SWISS_GERMAN));
        assertEquals("Uf em Rhy dihei", UiLanguage.translate("Auf dem Rhein zuhause", UiLanguage.SWISS_GERMAN));
        assertEquals("Als Nöchscht", UiLanguage.translate("Als Nächstes", UiLanguage.SWISS_GERMAN));
        assertEquals("Rhy aktuell", UiLanguage.translate("Rhein aktuell", UiLanguage.SWISS_GERMAN));
        assertEquals("Alli Termin aazeige  →", UiLanguage.translate("Alle Termine anzeigen  →", UiLanguage.SWISS_GERMAN));
    }

    @Test public void germanAndUnknownContentRemainUntouched() {
        assertEquals("Als Nächstes", UiLanguage.translate("Als Nächstes", UiLanguage.DE));
        assertEquals("JP-Prüfungen & Endfahren", UiLanguage.translate("JP-Prüfungen & Endfahren", UiLanguage.SWISS_GERMAN));
    }
}
