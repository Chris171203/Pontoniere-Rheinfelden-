package ch.pfvr.internapp;

import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
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

    @Test public void generatedScriptBuildsDayRowsWithParticipantsToTheRight(){
        String script=InternalAttendanceSkin.javascript("#11171C","#1A2228","#232E36","#ECF1F4","#A0B0BA","#344550","#5BBED5");
        assertTrue(script.contains("pfvr-day-section"));
        assertTrue(script.contains("grid-template-columns:minmax(140px,42%)"));
        assertTrue(script.contains("pfvr-day-people"));
        assertTrue(script.contains("pfvr-person-card"));
        assertTrue(script.contains("overflow-x:auto"));
        assertTrue(script.contains("moveChildren(header.cells[column],meta)"));
        assertTrue(script.contains("moveChildren(row.cells[column],control)"));
        assertFalse(script.contains("min-width:176px"));
    }

    @Test public void generatedScriptUsesRealWebsiteControlsAndGlobalPersonManagement(){
        String script=InternalAttendanceSkin.javascript("#11171C","#1A2228","#232E36","#ECF1F4","#A0B0BA","#344550","#5BBED5");
        assertTrue(script.contains("person zur liste hinzuzufügen"));
        assertTrue(script.contains("+ Person"));
        assertTrue(script.contains("body.appendChild(select)"));
        assertTrue(script.contains("Keine Auswahl für diesen Termin"));
        assertFalse(script.contains("option.value='Mit Essen'"));
        assertFalse(script.contains("option.value='Ohne Essen'"));
    }

    @Test public void generatedScriptDoesNotForceReloadAndPreservesScrollAcrossServerNavigation(){
        String script=InternalAttendanceSkin.javascript("#11171C","#1A2228","#232E36","#ECF1F4","#A0B0BA","#344550","#5BBED5");
        assertTrue(script.contains("sessionStorage.setItem"));
        assertTrue(script.contains("beforeunload"));
        assertTrue(script.contains("window.scrollTo"));
        assertTrue(script.contains("scrollLeft"));
        assertFalse(script.contains("window.location.reload"));
    }

    @Test public void generatedScriptKeepsStatusRepairAndMobileViewport(){
        String script=InternalAttendanceSkin.javascript("#11171C","#1A2228","#232E36","#ECF1F4","#A0B0BA","#344550","#5BBED5");
        assertTrue(script.contains("pfvr-attendance-status"));
        assertTrue(script.contains("NodeFilter.SHOW_TEXT"));
        assertTrue(script.contains("meta[name=viewport]"));
        assertTrue(script.contains("width=device-width"));
        assertTrue(script.contains("setTimeout(buildMobile,900)"));
        assertFalse(script.contains("overflow-wrap:anywhere"));
    }
}
