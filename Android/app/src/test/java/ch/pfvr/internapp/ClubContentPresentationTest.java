package ch.pfvr.internapp;

import org.junit.Test;
import static org.junit.Assert.*;

public class ClubContentPresentationTest {
    @Test public void overviewExtractsBothSeasonsWithoutCopyingFactsIntoApp()throws Exception{
        var c=ClubContentParserTest.parse(ClubPageRepository.Page.ABOUT,ClubContentParserTest.ABOUT);
        var training=ClubContentPresentation.training(c);assertEquals(2,training.size());
        assertEquals("April-September",training.get(0).season());
        assertEquals("Montag- und Mittwochabend",training.get(0).days());
        assertEquals("18:30–20:00",training.get(0).time());assertEquals("",training.get(0).fallback());
        assertEquals("Donnerstag",training.get(1).days());assertEquals("19:30",training.get(1).time());
        assertEquals("Schützenturnhalle Rheinfelden",training.get(1).location());
        var changed=ClubContentParserTest.parse(ClubPageRepository.Page.ABOUT,ClubContentParserTest.ABOUT.replace("18:30 Uhr","18:45 Uhr"));
        assertEquals("18:45–20:00",ClubContentPresentation.training(changed).get(0).time());
    }
    @Test public void missingSchedulePatternUsesSourceSentenceWithoutGuessedTimes()throws Exception{
        var c=ClubContentParserTest.parse(ClubPageRepository.Page.ABOUT,ClubContentParserTest.ABOUT.replace("am Montag- und Mittwochabend, von 18:30 Uhr bis 20:00 Uhr","nach Absprache"));
        var training=ClubContentPresentation.training(c);
        assertTrue(training.get(0).fallback().contains("nach Absprache"));assertEquals("",training.get(0).time());
    }
    @Test public void destinationsShareSourceCacheButSelectOnlyTheirOwnSections()throws Exception{
        var c=ClubContentParserTest.parse(ClubPageRepository.Page.ABOUT,ClubContentParserTest.ABOUT);
        var sport=ClubContentPresentation.sections(c,ClubContentPresentation.Destination.SPORT);
        assertEquals(2,sport.size());assertEquals("boats",sport.get(0).id());assertEquals("sport",sport.get(1).id());
        assertEquals("training",ClubContentPresentation.sections(c,ClubContentPresentation.Destination.TRAINING).get(0).id());
        assertEquals("life",ClubContentPresentation.sections(c,ClubContentPresentation.Destination.LIFE).get(0).id());
        assertEquals(ClubPageRepository.Page.ABOUT,ClubContentPresentation.Destination.SPORT.page);
    }
    @Test public void overviewHasShortSourceIntroductionWithoutOldStatistics()throws Exception{
        var c=ClubContentParserTest.parse(ClubPageRepository.Page.ABOUT,ClubContentParserTest.ABOUT);
        String text=ClubContentPresentation.overviewIntro(c);
        assertEquals("Der Testverein wurde 1896 gegründet. Pontonier ist eine Sportart auf dem Wasser.",text);
        assertFalse(text.contains("38 Mitglieder"));
    }
}
