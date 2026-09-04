package ch.pfvr.internapp;

import org.junit.Test;

import static org.junit.Assert.assertTrue;

public class InternalAttendanceUiRegressionTest {
    private String script(){
        return InternalAttendanceSkin.javascript("#11171C","#1A2228","#232E36","#ECF1F4","#A0B0BA","#344550","#5BBED5",UiLanguage.DE);
    }

    @Test public void keepsAViewportFixedHeaderFallbackForBrokenStickyAncestors(){
        String script=script();
        assertTrue(script.contains("pfvr-head-overlay"));
        assertTrue(script.contains("window.addEventListener('scroll'"));
        assertTrue(script.contains("headerOverlay.scrollLeft=matrixScroll.scrollLeft"));
        assertTrue(script.contains("getBoundingClientRect"));
    }

    @Test public void scalesLongCookLabelsInsteadOfGrowingTheDayColumn(){
        String script=script();
        assertTrue(script.contains("fitDayMetaTexts"));
        assertTrue(script.contains("pfvr-day-fit-name"));
        assertTrue(script.contains("node.scrollWidth>available"));
        assertTrue(script.contains("var minSize=10"));
    }

    @Test public void recoveryConfirmationAlwaysReturnsToItsNeutralState(){
        String script=script();
        assertTrue(script.contains("resetRecoveryConfirm"));
        assertTrue(script.contains("pfvrConfirmTimer"));
        assertTrue(script.contains("panel.addEventListener('click'"));
        assertTrue(script.contains("Aus Initiallink neu aufbauen"));
    }
}
