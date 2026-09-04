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

    @Test public void formatsParticipantNamesAsFamilyCommaGiven(){
        assertEquals("Neugebauer, Christoph",InternalAttendanceSkin.formatPersonDisplayName("NeugebauerChristoph"));
        assertEquals("Wiekert, Stephan",InternalAttendanceSkin.formatPersonDisplayName("Wiekert Stephan"));
        assertEquals("Müller-Lüdenscheidt, Anna Maria",InternalAttendanceSkin.formatPersonDisplayName("Müller-Lüdenscheidt Anna Maria"));
        assertEquals("Person 2",InternalAttendanceSkin.formatPersonDisplayName("Person 2"));
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
        assertTrue(script.contains("Personen verwalten"));
        assertTrue(script.contains("window.pfvrOpenPeopleManager"));
        assertTrue(script.contains("pfvr-person-tools-backdrop"));
        assertFalse(script.contains("body.appendChild(select)"));
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
        assertTrue(script.contains("pfvr-attendance-people-v4"));
        assertTrue(script.contains("pfvr-local-remove"));
        assertTrue(script.contains("removeDesiredPerson"));
        assertTrue(script.contains("setPersonColumnHidden"));
        assertTrue(script.contains("window.location.replace(base)"));
        assertTrue(script.contains("savePeopleState"));
        assertTrue(script.contains("loadPeopleState"));
        assertTrue(script.contains("state.primary"));
        assertTrue(script.contains("state.hidden"));
        assertFalse(script.contains("personManagementControls"));
        assertFalse(script.contains("looksLikeRemoveAction"));
    }

    @Test public void generatedScriptKeepsOriginalPersonControlInWebsiteContextAndSyncsNewRows(){
        String script=InternalAttendanceSkin.javascript("#11171C","#1A2228","#232E36","#ECF1F4","#A0B0BA","#344550","#5BBED5");
        assertTrue(script.contains("select.cloneNode(true)"));
        assertTrue(script.contains("proxy.removeAttribute('name')"));
        assertTrue(script.contains("proxy.removeAttribute('onchange')"));
        assertTrue(script.contains("select.dispatchEvent(new Event('change'"));
        assertTrue(script.contains("control.click()"));
        assertTrue(script.contains("scheduleParticipantSync"));
        assertTrue(script.contains("syncAddedParticipants"));
        assertTrue(script.contains("bindSourcePeopleObserver"));
        assertTrue(script.contains("appendPersonColumn"));
        assertFalse(script.contains("body.appendChild(select)"));
    }

    @Test public void generatedScriptPersistsRestoresRemovesAndLabelsParticipants(){
        String script=InternalAttendanceSkin.javascript("#11171C","#1A2228","#232E36","#ECF1F4","#A0B0BA","#344550","#5BBED5");
        assertTrue(script.contains("pfvr-attendance-people-v4"));
        assertTrue(script.contains("localStorage.setItem(PEOPLE_KEY"));
        assertTrue(script.contains("loadPeopleState"));
        assertTrue(script.contains("tryRestoreMissingPerson"));
        assertTrue(script.contains("restoreValues"));
        assertTrue(script.contains("pendingAdd"));
        assertTrue(script.contains("samePersonName"));
        assertTrue(script.contains("dispatchEvent(new Event('change'"));
        assertTrue(script.contains("removeDesiredPerson"));
        assertTrue(script.contains("pfvr-local-remove"));
        assertTrue(script.contains("data-pfvr-person"));
        assertTrue(script.contains("state.primary"));
        assertTrue(script.contains("pfvr-person-name-label"));
        assertTrue(script.contains("fitPersonName(personLabel,names[rowIndex])"));
        assertTrue(script.contains("-webkit-line-clamp:2"));
        assertTrue(script.contains("clean.length>28"));
        assertTrue(script.contains("clean.length>19"));
        assertTrue(script.contains("formatPersonName"));
        assertTrue(script.contains("personCellText"));
        assertTrue(script.contains("optionNameForRow"));
        assertTrue(script.contains("--pfvr-person-col:clamp(104px,calc((100vw - 128px)/2),138px)"));
        assertFalse(script.contains("window.location.reload"));
    }

    @Test public void generatedScriptKeepsEveryCurrentWebsitePersonUnlessExplicitlyHidden(){
        String script=InternalAttendanceSkin.javascript("#11171C","#1A2228","#232E36","#ECF1F4","#A0B0BA","#344550","#5BBED5");
        assertTrue(script.contains("adoptCurrentPeople(state,names)"));
        assertTrue(script.contains("if(!isHiddenPerson(state,name)&&appendPersonColumn"));
        assertTrue(script.contains("if(!isHiddenPerson(peopleState,allNames[index]))"));
        assertTrue(script.contains("rememberPendingPerson(state,sourceTableRef,select,chosen)"));
        assertTrue(script.contains("personTokenKey"));
        assertFalse(script.contains("var desiredKeys={}"));
        assertFalse(script.contains("if(chosen)addDesiredPerson(state,chosen)"));
    }

    @Test public void generatedScriptHidesBulkActionAndExposesManagerFromNativeToolbar(){
        String script=InternalAttendanceSkin.javascript("#11171C","#1A2228","#232E36","#ECF1F4","#A0B0BA","#344550","#5BBED5");
        assertTrue(script.contains("bulkPeopleAction"));
        assertTrue(script.contains("value.indexOf('alle anzeigen')"));
        assertTrue(script.contains("value.indexOf('alle hinzufügen')"));
        assertTrue(script.contains("if(bulkPeopleAction(label))return"));
        assertTrue(script.contains("Schliessen"));
        assertTrue(script.contains("Angezeigte Personen"));
        assertTrue(script.contains("Entfernen aktualisiert die Personenliste automatisch."));
        assertTrue(script.contains("Aktuelle Personen"));
        assertFalse(script.contains("Ausgeblendet"));
    }

    @Test public void generatedScriptRebuildsWebsiteListAfterRemoval(){
        String script=InternalAttendanceSkin.javascript("#11171C","#1A2228","#232E36","#ECF1F4","#A0B0BA","#344550","#5BBED5");
        assertTrue(script.contains("state.rowNames=[];state.pendingAdd=null"));
        assertTrue(script.contains("window.__pfvrBaseInternalUrl"));
        assertTrue(script.contains("window.location.replace(base)"));
        assertTrue(script.contains("removeDesiredPerson"));
        assertFalse(script.contains("Entfernen blendet die Person nur"));
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
