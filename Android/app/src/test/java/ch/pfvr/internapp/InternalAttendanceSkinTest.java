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

    @Test public void generatedScriptBuildsSharedPersonColumnsAcrossAllDays(){
        String script=InternalAttendanceSkin.javascript("#11171C","#1A2228","#232E36","#ECF1F4","#A0B0BA","#344550","#5BBED5");
        assertTrue(script.contains("pfvr-matrix-scroll"));
        assertTrue(script.contains("pfvr-attendance-matrix"));
        assertTrue(script.contains("gridTemplateColumns='var(--pfvr-day-col) repeat('+names.length+',var(--pfvr-person-col))'"));
        assertTrue(script.contains("pfvr-person-header"));
        assertTrue(script.contains("pfvr-person-cell"));
        assertTrue(script.contains("position:sticky"));
        assertTrue(script.contains("overflow-x:auto"));
        assertTrue(script.contains("moveChildren(header.cells[column],meta)"));
        assertTrue(script.contains("moveChildren(row.cells[column],control)"));
        assertFalse(script.contains("pfvr-day-people"));
        assertFalse(script.contains("pfvr-person-card"));
    }

    @Test public void generatedScriptUsesRealWebsiteControlsAndGlobalPersonManagement(){
        String script=InternalAttendanceSkin.javascript("#11171C","#1A2228","#232E36","#ECF1F4","#A0B0BA","#344550","#5BBED5");
        assertTrue(script.contains("person zur liste hinzuzufügen"));
        assertTrue(script.contains("+ / − Person"));
        assertTrue(script.contains("body.appendChild(select)"));
        assertTrue(script.contains("Keine Auswahl für diesen Termin"));
        assertFalse(script.contains("option.value='Mit Essen'"));
        assertFalse(script.contains("option.value='Ohne Essen'"));
    }

    @Test public void generatedScriptRestylesCurrentSelectionWithoutReload(){
        String script=InternalAttendanceSkin.javascript("#11171C","#1A2228","#232E36","#ECF1F4","#A0B0BA","#344550","#5BBED5");
        assertTrue(script.contains("statusForValue"));
        assertTrue(script.contains("controlValue"));
        assertTrue(script.contains("selectedIndex"));
        assertTrue(script.contains(".btn,select"));
        assertTrue(script.contains("refreshInteractiveSoon"));
        assertTrue(script.contains("bindInteractiveObserver"));
        assertTrue(script.contains("attributeFilter:['value','selected','class']"));
        assertTrue(script.contains("indexOf('mit essen')"));
        assertTrue(script.contains("indexOf('ohne essen')"));
        assertFalse(script.contains("window.location.reload"));
    }

    @Test public void generatedScriptDoesNotForceReloadAndPreservesScrollAcrossServerNavigation(){
        String script=InternalAttendanceSkin.javascript("#11171C","#1A2228","#232E36","#ECF1F4","#A0B0BA","#344550","#5BBED5");
        assertTrue(script.contains("sessionStorage.setItem"));
        assertTrue(script.contains("beforeunload"));
        assertTrue(script.contains("window.scrollTo"));
        assertTrue(script.contains("matrixScroll.scrollLeft=state.x||0"));
        assertTrue(script.contains("x:matrixScroll?(matrixScroll.scrollLeft||0):0"));
        assertFalse(script.contains("strips:strips"));
        assertFalse(script.contains("window.location.reload"));
    }

    @Test public void generatedScriptKeepsTwoParticipantsAndUsesLocalViewManagement(){
        String script=InternalAttendanceSkin.javascript("#11171C","#1A2228","#232E36","#ECF1F4","#A0B0BA","#344550","#5BBED5");
        assertTrue(script.contains("--pfvr-person-col:clamp(104px,calc((100vw - 128px)/2),138px)"));
        assertTrue(script.contains("--pfvr-day-col:84px;--pfvr-person-col:102px"));
        assertTrue(script.contains("pfvr-attendance-people-v1"));
        assertTrue(script.contains("pfvr-local-remove"));
        assertTrue(script.contains("removeDesiredPerson"));
        assertTrue(script.contains("savePeopleState"));
        assertTrue(script.contains("loadPeopleState"));
        assertTrue(script.contains("state.primary"));
        assertFalse(script.contains("personManagementControls"));
        assertFalse(script.contains("looksLikeRemoveAction"));
    }

    @Test public void generatedScriptPersistsRestoresRemovesAndLabelsParticipants(){
        String script=InternalAttendanceSkin.javascript("#11171C","#1A2228","#232E36","#ECF1F4","#A0B0BA","#344550","#5BBED5");
        assertTrue(script.contains("pfvr-attendance-people-v1"));
        assertTrue(script.contains("localStorage.setItem(PEOPLE_KEY"));
        assertTrue(script.contains("loadPeopleState"));
        assertTrue(script.contains("tryRestoreMissingPerson"));
        assertTrue(script.contains("dispatchEvent(new Event('change'"));
        assertTrue(script.contains("removeDesiredPerson"));
        assertTrue(script.contains("pfvr-local-remove"));
        assertTrue(script.contains("data-pfvr-person"));
        assertTrue(script.contains("state.primary"));
        assertTrue(script.contains("pfvr-person-name-label"));
        assertTrue(script.contains("fitPersonName(personLabel,names[rowIndex])"));
        assertTrue(script.contains("-webkit-line-clamp:2"));
        assertTrue(script.contains("clean.length>26"));
        assertTrue(script.contains("clean.length>17"));
        assertTrue(script.contains("--pfvr-person-col:clamp(104px,calc((100vw - 128px)/2),138px)"));
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
