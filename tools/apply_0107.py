from pathlib import Path
import re


def replace_once(text, old, new, label):
    count = text.count(old)
    if count != 1:
        raise RuntimeError(f"{label}: expected 1 match, got {count}")
    return text.replace(old, new, 1)


def replace_block(text, start, end, replacement, label):
    pattern = re.escape(start) + r".*?" + re.escape(end)
    updated, count = re.subn(pattern, replacement, text, count=1, flags=re.S)
    if count != 1:
        raise RuntimeError(f"{label}: expected 1 block, got {count}")
    return updated


source_path = Path('Android/app/src/main/java/ch/pfvr/internapp/InternalAttendanceSkin.java')
test_path = Path('Android/app/src/test/java/ch/pfvr/internapp/InternalAttendanceSkinTest.java')
build_path = Path('Android/app/build.gradle')
readme_path = Path('Android/README.md')
changelog_path = Path('Android/CHANGELOG.md')
status_path = Path('STATUS.md')

source = source_path.read_text(encoding='utf-8')
source = replace_once(
    source,
    "var PEOPLE_KEY='pfvr-attendance-people-v1';\n                  var RESTORE_KEY='pfvr-attendance-restore-v1';",
    "var PEOPLE_KEY='pfvr-attendance-people-v2';\n                  var RESTORE_KEY='pfvr-attendance-restore-v2';",
    'storage version',
)

old_start = "                  var savePeopleState=function(state){"
old_end = "                  var findPersonToolScope=function(){"
new_state = r"""                  var savePeopleState=function(state){
                    try{localStorage.setItem(PEOPLE_KEY,JSON.stringify(state));}catch(ignore){}
                  };
                  var personTokenKey=function(value){
                    var normalized=personKey(value);try{normalized=normalized.normalize('NFD');}catch(ignore){}
                    return normalized.replace(/[^a-z0-9\s]/g,' ').replace(/\s+/g,' ').trim().split(' ').filter(function(token){return token.length>1;}).sort().join('|');
                  };
                  var samePersonName=function(left,right){
                    var leftKey=personKey(left),rightKey=personKey(right);if(!leftKey||!rightKey)return false;if(leftKey===rightKey)return true;
                    var leftTokens=personTokenKey(left),rightTokens=personTokenKey(right);return !!leftTokens&&leftTokens===rightTokens;
                  };
                  var listHasPerson=function(list,name){return Array.isArray(list)&&list.some(function(saved){return samePersonName(saved,name);});};
                  var isHiddenPerson=function(state,name){return !!(state&&listHasPerson(state.hidden,name));};
                  var dedupePeople=function(list){var result=[];(list||[]).forEach(function(name){var clean=cleanPersonName(name);if(clean&&!listHasPerson(result,clean))result.push(clean);});return result;};
                  var addDesiredPerson=function(state,name,restoreValue){
                    var clean=cleanPersonName(name);if(!state||!clean)return false;
                    if(!Array.isArray(state.desired))state.desired=[];if(!Array.isArray(state.hidden))state.hidden=[];if(!state.restoreValues||typeof state.restoreValues!=='object')state.restoreValues={};
                    state.hidden=state.hidden.filter(function(saved){return !samePersonName(saved,clean);});
                    var index=state.desired.findIndex(function(saved){return samePersonName(saved,clean);});
                    var previous=index>=0?state.desired[index]:'',previousKey=personKey(previous),key=personKey(clean);
                    if(index<0)state.desired.push(clean);else state.desired[index]=clean;
                    if(restoreValue!==undefined&&restoreValue!==null&&String(restoreValue)!=='')state.restoreValues[key]=String(restoreValue);
                    else if(previousKey&&previousKey!==key&&state.restoreValues[previousKey]){state.restoreValues[key]=state.restoreValues[previousKey];delete state.restoreValues[previousKey];}
                    return true;
                  };
                  var removeDesiredPerson=function(state,name){
                    var clean=cleanPersonName(name);if(!state||!clean||samePersonName(clean,state.primary))return false;
                    state.desired=(state.desired||[]).filter(function(saved){return !samePersonName(saved,clean);});
                    if(!listHasPerson(state.hidden,clean))state.hidden.push(clean);
                    Object.keys(state.restoreValues||{}).forEach(function(key){if(samePersonName(key,clean))delete state.restoreValues[key];});
                    savePeopleState(state);return true;
                  };
                  var selectedOptionValue=function(select){
                    if(!select||select.selectedIndex<0||!select.options)return '';var option=select.options[select.selectedIndex];return String((option&&option.value)||'');
                  };
                  var currentSourceNames=function(table){
                    if(!table)return [];return Array.from(table.rows||[]).slice(1).filter(function(row){return row.cells&&row.cells.length>=2;}).map(function(row,index){var value=cleanPersonName(text(row.cells[0]));return value||('Person '+(index+1));});
                  };
                  var rememberPendingPerson=function(state,table,select,expectedName){
                    if(!state)return;state.pendingAdd={before:currentSourceNames(table),optionValue:selectedOptionValue(select),optionText:cleanPersonName(expectedName||''),time:Date.now()};savePeopleState(state);
                  };
                  var adoptCurrentPeople=function(state,currentNames){
                    if(!state)return;
                    var pending=state.pendingAdd;
                    if(pending&&Date.now()-(pending.time||0)<=120000){
                      var before=Array.isArray(pending.before)?pending.before:[];
                      var added=currentNames.filter(function(name){return !before.some(function(previous){return samePersonName(previous,name);});});
                      if(!added.length&&pending.optionText){var existing=currentNames.find(function(name){return samePersonName(name,pending.optionText);});if(existing)added=[existing];}
                      if(added.length){added.forEach(function(name){addDesiredPerson(state,name,pending.optionValue);});state.pendingAdd=null;}
                    }else if(pending){state.pendingAdd=null;}
                    currentNames.forEach(function(name){if(!isHiddenPerson(state,name))addDesiredPerson(state,name);});
                    savePeopleState(state);
                  };
                  var loadPeopleState=function(currentNames){
                    var state=null;try{var raw=localStorage.getItem(PEOPLE_KEY);if(raw)state=JSON.parse(raw);}catch(ignore){}
                    if(!state||!Array.isArray(state.desired))state={version:2,primary:currentNames[0]||'',desired:[],hidden:[],restoreValues:{},pendingAdd:null};
                    state.version=2;if(!Array.isArray(state.hidden))state.hidden=[];if(!state.restoreValues||typeof state.restoreValues!=='object')state.restoreValues={};
                    if(!state.primary&&currentNames.length)state.primary=currentNames[0];
                    state.hidden=dedupePeople(state.hidden).filter(function(name){return !samePersonName(name,state.primary);});state.desired=dedupePeople(state.desired);
                    if(state.primary&&!listHasPerson(state.desired,state.primary))state.desired.unshift(state.primary);
                    adoptCurrentPeople(state,currentNames);return state;
                  };
                  var findPersonToolScope=function(){"""
source = replace_block(source, old_start, old_end, new_state, 'people state block')

source = replace_once(
    source,
    """                  var findOptionForPerson=function(select,name){
                    if(!select||!select.options)return -1;var wanted=personKey(name);
                    for(var i=0;i<select.options.length;i++){var candidate=personKey(select.options[i].textContent||select.options[i].value||'');if(candidate&&candidate===wanted)return i;}
                    return -1;
                  };
""",
    """                  var findOptionForPerson=function(select,name,state){
                    if(!select||!select.options)return -1;var savedValue=state&&state.restoreValues?state.restoreValues[personKey(name)]:'';
                    if(savedValue){for(var byValue=0;byValue<select.options.length;byValue++){if(String(select.options[byValue].value||'')===String(savedValue))return byValue;}}
                    for(var i=0;i<select.options.length;i++){var candidate=select.options[i].textContent||select.options[i].value||'';if(samePersonName(candidate,name))return i;}
                    return -1;
                  };
""",
    'restore option matching',
)
source = replace_once(
    source,
    """                    var currentKeys={};currentNames.forEach(function(name){currentKeys[personKey(name)]=true;});
                    var missing=state.desired.find(function(name){return !currentKeys[personKey(name)];});if(!missing)return false;
                    var optionIndex=findOptionForPerson(select,missing);if(optionIndex<0)return false;
""",
    """                    var missing=state.desired.find(function(name){return !isHiddenPerson(state,name)&&!currentNames.some(function(current){return samePersonName(current,name);});});if(!missing)return false;
                    var optionIndex=findOptionForPerson(select,missing,state);if(optionIndex<0)return false;
""",
    'missing person matching',
)
source = replace_once(
    source,
    """                    select.selectedIndex=optionIndex;
                    select.dispatchEvent(new Event('input',{bubbles:true}));
""",
    """                    select.selectedIndex=optionIndex;rememberPendingPerson(state,sourceTableRef,select,missing);
                    select.dispatchEvent(new Event('input',{bubbles:true}));
""",
    'restore pending mapping',
)

source = replace_once(
    source,
    """                    if(key===personKey(state.primary)){
""",
    """                    if(samePersonName(personName,state.primary)){
""",
    'primary comparison',
)

old_sync_start = "                  var syncAddedParticipants=function(state){"
old_sync_end = "                  var scheduleParticipantSync=function(state){"
new_sync = """                  var syncAddedParticipants=function(state){
                    var table=sourceTableRef;
                    if(!table||!table.isConnected){
                      var fresh=findTable();if(!fresh)return false;
                      var mobile=document.querySelector('.pfvr-attendance-mobile');if(mobile)mobile.remove();
                      sourceTableRef=null;buildMobile();return true;
                    }
                    var rows=Array.from(table.rows||[]);if(rows.length<2||!rows[0].cells)return false;
                    var headerFresh=Array.from(rows[0].cells||[]).slice(1).some(function(cell){return !!(text(cell).trim()||cell.children.length);});
                    if(headerFresh&&document.querySelector('.pfvr-attendance-mobile')){
                      var mobile=document.querySelector('.pfvr-attendance-mobile');if(mobile)mobile.remove();table.classList.remove('pfvr-attendance-source');sourceTableRef=null;buildMobile();return true;
                    }
                    var participantRows=rows.slice(1).filter(function(row){return row.cells&&row.cells.length>=2;});
                    var names=participantRows.map(function(row,index){var value=cleanPersonName(text(row.cells[0]));return value||('Person '+(index+1));});
                    adoptCurrentPeople(state,names);
                    var added=false;participantRows.forEach(function(row,index){var name=names[index];if(!isHiddenPerson(state,name)&&appendPersonColumn(table,row,name,state))added=true;});return added;
                  };
                  var scheduleParticipantSync=function(state){"""
source = replace_block(source, old_sync_start, old_sync_end, new_sync, 'server authoritative sync')

source = replace_once(
    source,
    """                    proxy.addEventListener('change',function(){
                      var chosen=selectedOptionName(proxy);if(chosen)addDesiredPerson(state,chosen);
                      select.selectedIndex=proxy.selectedIndex;try{select.value=proxy.value;}catch(ignore){}
                      select.dispatchEvent(new Event('input',{bubbles:true}));select.dispatchEvent(new Event('change',{bubbles:true}));scheduleParticipantSync(state);
                    },false);
""",
    """                    proxy.addEventListener('change',function(){
                      var chosen=selectedOptionName(proxy);select.selectedIndex=proxy.selectedIndex;try{select.value=proxy.value;}catch(ignore){}
                      rememberPendingPerson(state,sourceTableRef,select,chosen);
                      select.dispatchEvent(new Event('input',{bubbles:true}));select.dispatchEvent(new Event('change',{bubbles:true}));scheduleParticipantSync(state);
                    },false);
""",
    'proxy add behavior',
)
source = replace_once(
    source,
    """                      action.addEventListener('click',function(){if(norm(label).indexOf('alle anzeigen')>=0){state.adoptNext=true;savePeopleState(state);}control.click();scheduleParticipantSync(state);});
""",
    """                      action.addEventListener('click',function(){if(norm(label).indexOf('alle anzeigen')>=0){state.hidden=[];state.pendingAdd=null;savePeopleState(state);}control.click();scheduleParticipantSync(state);});
""",
    'all show behavior',
)

source = replace_once(
    source,
    """                    var toolInfo=findPersonToolScope(),peopleState=loadPeopleState(allNames);
                    if(toolInfo&&tryRestoreMissingPerson(toolInfo.select,allNames,peopleState))return false;
                    try{sessionStorage.removeItem(RESTORE_KEY);}catch(ignore){}
                    var desiredKeys={};peopleState.desired.forEach(function(name){desiredKeys[personKey(name)]=true;});
                    var participantRows=[],names=[];
                    allParticipantRows.forEach(function(row,index){if(desiredKeys[personKey(allNames[index])]){participantRows.push(row);names.push(allNames[index]);}});
""",
    """                    var toolInfo=findPersonToolScope(),peopleState=loadPeopleState(allNames);
                    if(toolInfo&&tryRestoreMissingPerson(toolInfo.select,allNames,peopleState))return false;
                    try{sessionStorage.removeItem(RESTORE_KEY);}catch(ignore){}
                    var participantRows=[],names=[];
                    allParticipantRows.forEach(function(row,index){if(!isHiddenPerson(peopleState,allNames[index])){participantRows.push(row);names.push(allNames[index]);}});
""",
    'authoritative build filtering',
)

source_path.write_text(source, encoding='utf-8')

build = build_path.read_text(encoding='utf-8')
build = replace_once(build, 'versionCode 30', 'versionCode 31', 'version code')
build = replace_once(build, "versionName '0.10.6'", "versionName '0.10.7'", 'version name')
build_path.write_text(build, encoding='utf-8')

test = test_path.read_text(encoding='utf-8')
test = test.replace('pfvr-attendance-people-v1', 'pfvr-attendance-people-v2')
test = replace_once(
    test,
    """        assertTrue(script.contains("state.primary"));
        assertFalse(script.contains("personManagementControls"));
""",
    """        assertTrue(script.contains("state.primary"));
        assertTrue(script.contains("state.hidden"));
        assertFalse(script.contains("personManagementControls"));
""",
    'hidden state assertion',
)
test = replace_once(
    test,
    """        assertTrue(script.contains("loadPeopleState"));
        assertTrue(script.contains("tryRestoreMissingPerson"));
        assertTrue(script.contains("dispatchEvent(new Event('change'"));
""",
    """        assertTrue(script.contains("loadPeopleState"));
        assertTrue(script.contains("tryRestoreMissingPerson"));
        assertTrue(script.contains("restoreValues"));
        assertTrue(script.contains("pendingAdd"));
        assertTrue(script.contains("samePersonName"));
        assertTrue(script.contains("dispatchEvent(new Event('change'"));
""",
    'restore mapping assertions',
)
marker = '    @Test public void generatedScriptKeepsStatusRepairAndMobileViewport(){\n'
regression = """    @Test public void generatedScriptKeepsEveryCurrentWebsitePersonUnlessExplicitlyHidden(){
        String script=InternalAttendanceSkin.javascript("#11171C","#1A2228","#232E36","#ECF1F4","#A0B0BA","#344550","#5BBED5");
        assertTrue(script.contains("adoptCurrentPeople(state,names)"));
        assertTrue(script.contains("if(!isHiddenPerson(state,name)&&appendPersonColumn"));
        assertTrue(script.contains("if(!isHiddenPerson(peopleState,allNames[index]))"));
        assertTrue(script.contains("rememberPendingPerson(state,sourceTableRef,select,chosen)"));
        assertTrue(script.contains("personTokenKey"));
        assertFalse(script.contains("var desiredKeys={}"));
        assertFalse(script.contains("if(chosen)addDesiredPerson(state,chosen)"));
    }

"""
test = replace_once(test, marker, regression + marker, 'regression test insertion')
test_path.write_text(test, encoding='utf-8')

readme = readme_path.read_text(encoding='utf-8')
readme = readme.replace('Aktuelle Android-Testversion: `0.10.6`.', 'Aktuelle Android-Testversion: `0.10.7`.')
readme = readme.replace('## Gerätetest 0.10.6', '## Gerätetest 0.10.7')
readme = readme.replace('0.10.6 verwendet `versionCode 30`', '0.10.7 verwendet `versionCode 31`')
readme = replace_once(
    readme,
    '- Zusatzperson über den sichtbaren App-Select hinzufügen: die Auswahl wird am unveränderten originalen Website-Control ausgelöst; die neue Personenzeile muss bei Navigation oder DOM-Aktualisierung direkt in der Matrix erscheinen.\n',
    '- Zusatzperson über den sichtbaren App-Select hinzufügen: jede danach in der Originaltabelle vorhandene Personenzeile muss in der App-Matrix sichtbar bleiben. Die Originaltabelle ist die Quelle der Wahrheit; nur ausdrücklich lokal entfernte Personen werden ausgeblendet.\n- Unterschiedliche Reihenfolge oder Schreibweise von Auswahltext und Tabellenname darf die Person nicht mehr herausfiltern. Für die Wiederherstellung wird zusätzlich der Optionswert gespeichert und der Name tokenbasiert verglichen.\n',
    'readme test notes',
)
readme_path.write_text(readme, encoding='utf-8')

changelog = changelog_path.read_text(encoding='utf-8')
entry = """## 0.10.7
- Aktuell in der Originaltabelle vorhandene Personenzeilen sind die Quelle der Wahrheit und werden nicht mehr durch eine exakte lokale Namens-Whitelist entfernt.
- Auswahltext und Tabellenname werden über gespeicherten Optionswert sowie tokenbasierte Namensnormalisierung zusammengeführt; Vorname/Nachname-Reihenfolge darf abweichen.
- Lokales Entfernen wird separat als Ausblenden gespeichert, damit nur bewusst entfernte Personen unterdrückt werden.
- Regression behoben: Eine hinzugefügte Person blitzte kurz auf, blieb in der Originalansicht vorhanden und verschwand dennoch wieder aus der mobilen Matrix.

"""
changelog = replace_once(changelog, '# Android Changelog\n\n', '# Android Changelog\n\n' + entry, 'changelog')
changelog_path.write_text(changelog, encoding='utf-8')

status = status_path.read_text(encoding='utf-8')
status = status.replace('Stand: Testversion `0.10.6` · aktualisiert 2026-09-04.', 'Stand: Testversion `0.10.7` · aktualisiert 2026-09-04.')
status = replace_once(
    status,
    '## Implementiert / im Test\n\n',
    '## Implementiert / im Test\n\n- Die aktuelle Originaltabelle ist für vorhandene Teilnehmer die Quelle der Wahrheit. Jede serverseitig vorhandene Zeile wird angezeigt, außer die Person wurde lokal ausdrücklich entfernt. Auswahltext und Tabellenname werden über Optionswert plus tokenbasierte Namensnormalisierung verbunden.\n\n',
    'status',
)
status_path.write_text(status, encoding='utf-8')

print('0.10.7 patch applied')
