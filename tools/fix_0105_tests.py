from pathlib import Path

path = Path('Android/app/src/test/java/ch/pfvr/internapp/InternalAttendanceSkinTest.java')
source = path.read_text(encoding='utf-8')
old = '''    @Test public void generatedScriptShowsTwoParticipantsAndReusesPersonManagementActions(){
        String script=InternalAttendanceSkin.javascript("#11171C","#1A2228","#232E36","#ECF1F4","#A0B0BA","#344550","#5BBED5");
        assertTrue(script.contains("--pfvr-person-col:clamp(104px,calc((100vw - 128px)/2),138px)"));
        assertTrue(script.contains("--pfvr-day-col:84px;--pfvr-person-col:102px"));
        assertTrue(script.contains("personManagementControls"));
        assertTrue(script.contains("appendExistingPeople"));
        assertTrue(script.contains("pfvr-managed-person"));
        assertTrue(script.contains("looksLikeRemoveAction"));
        assertTrue(script.contains("actionBox.appendChild(control)"));
        assertFalse(script.contains("document.createElement('button').textContent='Entfernen'"));
    }
'''
new = '''    @Test public void generatedScriptKeepsTwoParticipantsAndUsesLocalViewManagement(){
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
'''
if source.count(old) != 1:
    raise SystemExit(f'legacy test block matches={source.count(old)}')
path.write_text(source.replace(old, new, 1), encoding='utf-8')
