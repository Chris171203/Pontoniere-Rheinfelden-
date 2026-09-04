package ch.pfvr.internapp;

import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

public class InternalAttendanceSkinTest {
    private static String script(){
        return InternalAttendanceSkin.javascript("#11171C","#1A2228","#232E36","#ECF1F4","#A0B0BA","#344550","#5BBED5",UiLanguage.DE);
    }

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

    @Test public void formatsParticipantNamesAndStripsEditMarker(){
        assertEquals("Neugebauer, Christoph",InternalAttendanceSkin.formatPersonDisplayName("NeugebauerChristoph"));
        assertEquals("Wiekert, Stephan",InternalAttendanceSkin.formatPersonDisplayName("Wiekert Stephan"));
        assertEquals("Kougionis, Eleni",InternalAttendanceSkin.formatPersonDisplayName("✎ Kougionis Eleni"));
        assertEquals("Krokos, Aron",InternalAttendanceSkin.formatPersonDisplayName("Bearbeiten: Krokos Aron"));
        assertEquals("Müller-Lüdenscheidt, Anna Maria",InternalAttendanceSkin.formatPersonDisplayName("Müller-Lüdenscheidt Anna Maria"));
        assertEquals("Person 2",InternalAttendanceSkin.formatPersonDisplayName("Person 2"));
    }

    @Test public void generatedScriptBuildsSharedMatrixWithFixedViewportHeader(){
        String script=script();
        assertTrue(script.contains("pfvr-matrix-head-scroll"));
        assertTrue(script.contains("pfvr-matrix-scroll"));
        assertTrue(script.contains("pfvr-attendance-head"));
        assertTrue(script.contains("pfvr-attendance-matrix"));
        assertTrue(script.contains("var columns='var(--pfvr-day-col) repeat('+names.length+',var(--pfvr-person-col))'"));
        assertTrue(script.contains(".pfvr-matrix-head-scroll{position:sticky!important;top:0!important"));
        assertTrue(script.contains("overflow-x:hidden!important;overflow-y:hidden!important;pointer-events:none!important"));
        assertTrue(script.contains("pfvr-head-overlay"));
        assertTrue(script.contains("updateHeaderOverlay"));
        assertTrue(script.contains("matrixScroll.addEventListener('scroll'"));
        assertFalse(script.contains("headScroll.addEventListener('scroll'"));
        assertTrue(script.contains("moveChildren(header.cells[column],meta)"));
        assertTrue(script.contains("moveChildren(row.cells[column],control)"));
    }

    @Test public void generatedScriptUsesLargeAttendanceControlsWithoutRepeatedNames(){
        String script=script();
        assertTrue(script.contains(".pfvr-person-control{display:flex!important;flex-direction:column!important;justify-content:flex-end!important"));
        assertTrue(script.contains("min-height:60px!important;padding:10px 8px!important;font-size:13px!important"));
        assertFalse(script.contains("pfvr-person-name-label"));
    }

    @Test public void generatedScriptReadsNamesFromOriginalEditControls(){
        String script=script();
        assertTrue(script.contains("personNameCandidate"));
        assertTrue(script.contains("button,input[type=submit],input[type=button],a"));
        assertTrue(script.contains("controlLabel(control)"));
        assertTrue(script.contains("^[✎✏✐✑✒]+"));
        assertTrue(script.contains("bearbeiten|edit"));
        assertTrue(script.contains("personCellText"));
        assertTrue(script.contains("optionNameForRow"));
    }

    @Test public void generatedScriptDoesNotMisapplyOldNamesByIndexAfterSourceExpansion(){
        String script=script();
        assertTrue(script.contains("var storedRows=state&&Array.isArray(state.rowNames)?state.rowNames:[]"));
        assertTrue(script.contains("var allowStoredByIndex=storedRows.length===rows.length&&storedRows.length>0"));
        assertTrue(script.contains("if(!value&&allowStoredByIndex&&storedRows[index]"));
        assertFalse(script.contains("(state.rowNames||[]).concat(state.desired||[]).concat(state.hidden||[])"));
    }

    @Test public void generatedScriptLetsLargeOriginalListBecomeSourceOfTruth(){
        String script=script();
        assertTrue(script.contains("shouldTakeSourceList"));
        assertTrue(script.contains("if(currentNames.length>=12)return true"));
        assertTrue(script.contains("hidden:[]"));
        assertTrue(script.contains("desired:sourceReal.slice()"));
        assertTrue(script.contains("rowNames:currentNames.slice()"));
        assertTrue(script.contains("if(currentNames.length&&shouldTakeSourceList(currentNames,state))"));
    }

    @Test public void generatedScriptKeepsRealWebsiteStatusControls(){
        String script=script();
        assertTrue(script.contains("statusForValue"));
        assertTrue(script.contains("controlValue"));
        assertTrue(script.contains("selectedIndex"));
        assertTrue(script.contains("refreshInteractiveSoon"));
        assertTrue(script.contains("bindInteractiveObserver"));
        assertTrue(script.contains("attributeFilter:['value','selected','class']"));
        assertFalse(script.contains("option.value='Mit Essen'"));
        assertFalse(script.contains("option.value='Ohne Essen'"));
        assertFalse(script.contains("window.location.reload"));
    }

    @Test public void generatedScriptPreservesScrollAcrossServerNavigation(){
        String script=script();
        assertTrue(script.contains("sessionStorage.setItem"));
        assertTrue(script.contains("beforeunload"));
        assertTrue(script.contains("window.scrollTo"));
        assertTrue(script.contains("matrixScroll.scrollLeft=state.x||0"));
        assertTrue(script.contains("matrixHeadScroll.scrollLeft=state.x||0"));
        assertTrue(script.contains("x:matrixScroll?(matrixScroll.scrollLeft||0):0"));
    }

    @Test public void generatedScriptKeepsPersonManagementAndRecovery(){
        String script=script();
        assertTrue(script.contains("person zur liste hinzuzufügen"));
        assertTrue(script.contains("Personen verwalten"));
        assertTrue(script.contains("window.pfvrOpenPeopleManager"));
        assertTrue(script.contains("select.cloneNode(true)"));
        assertTrue(script.contains("proxy.removeAttribute('name')"));
        assertTrue(script.contains("removeDesiredPerson"));
        assertTrue(script.contains("window.location.replace(base)"));
        assertTrue(script.contains("buildFallbackPeopleManager"));
        assertTrue(script.contains("Aus Initiallink neu aufbauen"));
        assertTrue(script.contains("Nochmal tippen: wirklich neu aufbauen"));
        assertTrue(script.contains("resetRecoveryConfirm"));
        assertTrue(script.contains("localStorage.removeItem(PEOPLE_KEY)"));
        assertFalse(script.contains("localStorage.clear()"));
    }

    @Test public void generatedScriptBlocksBulkActionsOnlyInsideAppSkin(){
        String script=script();
        assertTrue(script.contains("bulkPeopleAction"));
        assertTrue(script.contains("value.indexOf('alle personen anzeigen')"));
        assertTrue(script.contains("value.indexOf('alle anzeigen')"));
        assertTrue(script.contains("suppressBulkPeopleActions"));
        assertTrue(script.contains("bindBulkPeopleGuard"));
        assertTrue(script.contains("stopImmediatePropagation"));
        assertTrue(script.contains("observer.observe(document.documentElement,{subtree:true,childList:true})"));
    }

    @Test public void generatedScriptProjectsDayTextInputsAsLabelsAndBoldsDate(){
        String script=script();
        assertTrue(script.contains("decorateDayMeta"));
        assertTrue(script.contains("input[type=text],input:not([type])"));
        assertTrue(script.contains("pfvr-day-source-input"));
        assertTrue(script.contains("pfvr-day-display-value"));
        assertTrue(script.contains("input.readOnly=true"));
        assertTrue(script.contains("input.setAttribute('tabindex','-1')"));
        assertTrue(script.contains("input.insertAdjacentElement('afterend',visual)"));
        assertTrue(script.contains(".pfvr-day-source-input{display:none!important;}"));
        assertTrue(script.contains(".pfvr-day-date{display:block!important;font-weight:700!important"));
        assertTrue(script.contains(".btn,.pfvr-day-display-value"));
    }

    @Test public void generatedScriptShrinksLongCookNamesOnlyInDayMeta(){
        String script=script();
        assertTrue(script.contains("fitDayMetaTexts"));
        assertTrue(script.contains("decorateDayMeta(meta)"));
        assertTrue(script.contains(".pfvr-day-fit-name"));
        assertTrue(script.contains("node.scrollWidth>available"));
        assertTrue(script.contains("var minSize=10"));
        assertTrue(script.contains("querySelectorAll('.pfvr-day-meta')"));
    }

    @Test public void generatedScriptKeepsTwoVisibleParticipantColumnsOnPhones(){
        String script=script();
        assertTrue(script.contains("--pfvr-person-col:clamp(104px,calc((100vw - 128px)/2),138px)"));
        assertTrue(script.contains("--pfvr-day-col:84px;--pfvr-person-col:102px"));
        assertTrue(script.contains("-webkit-line-clamp:2"));
        assertTrue(script.contains("clean.length>28"));
        assertTrue(script.contains("clean.length>19"));
    }

    @Test public void generatedScriptKeepsStatusRepairAndViewport(){
        String script=script();
        assertTrue(script.contains("pfvr-attendance-status"));
        assertTrue(script.contains("NodeFilter.SHOW_TEXT"));
        assertTrue(script.contains("meta[name=viewport]"));
        assertTrue(script.contains("width=device-width"));
        assertTrue(script.contains("setTimeout(buildMobile,900)"));
        assertFalse(script.contains("overflow-wrap:anywhere"));
    }
    @Test public void generatedScriptLocalizesProjectedAttendanceControls(){
        String script=InternalAttendanceSkin.javascript(
                "#11171C","#1A2228","#232E36","#ECF1F4","#A0B0BA","#344550","#5BBED5",
                UiLanguage.SWISS_GERMAN
        );
        assertTrue(script.contains("withFood:'Mit Ässe'"));
        assertTrue(script.contains("withoutFood:'Ohni Ässe'"));
        assertTrue(script.contains("notSelected:'Nüt gwählt'"));
        assertTrue(script.contains("notComing:'Ich chumme nöd'"));
        assertTrue(script.contains("managePeople:'Persone verwalte'"));
        assertTrue(script.contains("rebuildConfirm:'No einisch tippe: würkli neu ufbaue'"));
        assertTrue(script.contains("removeFromView:'Person us de Aasicht entferne: '"));
        assertTrue(script.contains("participant:'Person'"));
        assertFalse(script.contains("managePeople:'Personen verwalten'"));
    }

}
