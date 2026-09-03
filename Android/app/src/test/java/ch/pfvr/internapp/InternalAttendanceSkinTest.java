package ch.pfvr.internapp;

import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

public class InternalAttendanceSkinTest {
    @Test public void separatesStatusFromConcatenatedAppointmentText(){
        InternalAttendanceSkin.StatusSplit split=InternalAttendanceSkin.splitLeadingStatus("Ohne EssenSchiffe verladen & Depot");
        assertNotNull(split);
        assertEquals("Ohne Essen",split.status);
        assertEquals("Schiffe verladen & Depot",split.remainder);
    }

    @Test public void acceptsWhitespaceAndAsciiFallback(){
        InternalAttendanceSkin.StatusSplit split=InternalAttendanceSkin.splitLeadingStatus("  Nicht   gewaehlt   Wintertraining");
        assertNotNull(split);
        assertEquals("Nicht gewählt",split.status);
        assertEquals("Wintertraining",split.remainder);
    }

    @Test public void leavesOrdinaryAppointmentTextUntouched(){
        assertNull(InternalAttendanceSkin.splitLeadingStatus("Schiffe reinigen"));
    }

    @Test public void generatedScriptContainsDomRepairAndDelayedPasses(){
        String script=InternalAttendanceSkin.javascript("#11171C","#1A2228","#232E36","#ECF1F4","#A0B0BA","#344550","#5BBED5");
        assertTrue(script.contains("pfvr-attendance-status"));
        assertTrue(script.contains("NodeFilter.SHOW_TEXT"));
        assertTrue(script.contains("setTimeout(formatText,900)"));
        assertTrue(script.contains("MutationObserver"));
        assertTrue(script.contains("observer.observe(document.body"));
        assertTrue(script.contains("overflow-wrap:anywhere"));
        assertTrue(script.contains("td *,th *{white-space:normal"));
    }
}
