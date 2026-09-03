from pathlib import Path

skin = Path('Android/app/src/main/java/ch/pfvr/internapp/InternalAttendanceSkin.java')
test = Path('Android/app/src/test/java/ch/pfvr/internapp/InternalAttendanceSkinTest.java')
gradle = Path('Android/app/build.gradle')
changelog = Path('Android/CHANGELOG.md')
status = Path('STATUS.md')
readme = Path('Android/README.md')

source = skin.read_text(encoding='utf-8')

# Persistent desired participant list, separate from attendance data itself.
old_key = "                  var STORAGE_KEY='pfvr-attendance-view-state-v2';\n"
new_key = old_key + "                  var PEOPLE_KEY='pfvr-attendance-people-v1';\n                  var RESTORE_KEY='pfvr-attendance-restore-v1';\n"
if source.count(old_key) != 1:
    raise SystemExit('storage key marker mismatch')
source = source.replace(old_key, new_key, 1)

# Keep names readable but bounded in the shared participant columns.
old_header = ".pfvr-person-header{padding:8px 7px!important;font-size:12px!important;font-weight:700!important;line-height:1.2!important;overflow-wrap:break-word!important;word-break:normal!important;display:flex!important;align-items:center!important;}"
new_header = ".pfvr-person-header{padding:8px 7px!important;font-size:12px!important;font-weight:700!important;line-height:1.15!important;overflow-wrap:break-word!important;word-break:normal!important;display:-webkit-box!important;-webkit-box-orient:vertical!important;-webkit-line-clamp:2!important;overflow:hidden!important;min-height:40px!important;}"
if source.count(old_header) != 1:
    raise SystemExit('header marker mismatch')
source = source.replace(old_header, new_header, 1)

cell_marker = ".pfvr-person-cell{width:var(--pfvr-person-col)!important;padding:7px!important;display:flex!important;flex-direction:column!important;gap:4px!important;}\n"
cell_extra = cell_marker + "                  .pfvr-person-name-label{font-size:11px!important;font-weight:700!important;line-height:1.15!important;min-height:25px!important;display:-webkit-box!important;-webkit-box-orient:vertical!important;-webkit-line-clamp:2!important;overflow:hidden!important;overflow-wrap:break-word!important;word-break:normal!important;margin:0 0 2px!important;}\n                  .pfvr-name-small{font-size:10px!important;}\n                  .pfvr-name-tiny{font-size:9px!important;letter-spacing:-.1px!important;}\n                  .pfvr-local-remove{background:#6D7880!important;color:#fff!important;min-height:32px!important;padding:4px 8px!important;font-size:11px!important;}\n                  .pfvr-primary-label{font-size:10px!important;color:${COLORS.muted}!important;}\n"
if source.count(cell_marker) != 1:
    raise SystemExit('cell marker mismatch')
source = source.replace(cell_marker, cell_extra, 1)

media_old = ".pfvr-person-header{font-size:11px!important;padding:7px 5px!important;}.pfvr-person-cell{padding:6px!important;}"
media_new = ".pfvr-person-header{font-size:11px!important;padding:7px 5px!important;}.pfvr-person-cell{padding:6px!important;}.pfvr-person-name-label{font-size:10px!important;min-height:23px!important;}.pfvr-name-small{font-size:9px!important;}.pfvr-name-tiny{font-size:8.5px!important;}"
if source.count(media_old) != 1:
    raise SystemExit('media marker mismatch')
source = source.replace(media_old, media_new, 1)

function_marker = "                  var moveChildren=function(from,to){while(from&&from.firstChild)to.appendChild(from.firstChild);};\n"
helpers = function_marker + r'''                  var personKey=function(value){return cleanPersonName(value).toLowerCase();};
                  var fitPersonName=function(el,value){
                    var clean=(value||'').trim();el.textContent=clean;el.title=clean;
                    if(clean.length>26)el.classList.add('pfvr-name-tiny');
                    else if(clean.length>17)el.classList.add('pfvr-name-small');
                  };
                  var savePeopleState=function(state){
                    try{localStorage.setItem(PEOPLE_KEY,JSON.stringify(state));}catch(ignore){}
                  };
                  var loadPeopleState=function(currentNames){
                    var state=null;
                    try{var raw=localStorage.getItem(PEOPLE_KEY);if(raw)state=JSON.parse(raw);}catch(ignore){}
                    if(!state||!Array.isArray(state.desired)){
                      state={primary:currentNames[0]||'',desired:currentNames.slice(),adoptNext:false};
                    }
                    if(!state.primary&&currentNames.length)state.primary=currentNames[0];
                    if(state.primary&&!state.desired.some(function(name){return personKey(name)===personKey(state.primary);})){state.desired.unshift(state.primary);}
                    if(state.adoptNext){
                      currentNames.forEach(function(name){if(!state.desired.some(function(saved){return personKey(saved)===personKey(name);}))state.desired.push(name);});
                      state.adoptNext=false;
                    }
                    var seen={};state.desired=state.desired.filter(function(name){var key=personKey(name);if(!key||seen[key])return false;seen[key]=true;return true;});
                    savePeopleState(state);return state;
                  };
                  var addDesiredPerson=function(state,name){
                    var clean=cleanPersonName(name),key=personKey(clean);if(!clean||!key)return;
                    if(!state.desired.some(function(saved){return personKey(saved)===key;})){state.desired.push(clean);savePeopleState(state);}
                  };
                  var removeDesiredPerson=function(state,name){
                    var key=personKey(name);if(!key||key===personKey(state.primary))return false;
                    state.desired=state.desired.filter(function(saved){return personKey(saved)!==key;});savePeopleState(state);return true;
                  };
                  var findPersonToolScope=function(){
                    var candidates=Array.from(document.querySelectorAll('label,p,span,div')).filter(function(el){var value=norm(text(el));return value.indexOf('person zur liste hinzuzufügen')===0&&value.length<120;});
                    if(!candidates.length)return null;
                    var anchor=candidates[0],scope=anchor.parentElement;
                    for(var depth=0;scope&&depth<4;depth++,scope=scope.parentElement){
                      if(scope===document.body)break;var select=scope.querySelector('select');if(select)return {anchor:anchor,scope:scope,select:select};
                    }
                    return null;
                  };
                  var selectedOptionName=function(select){
                    if(!select||select.selectedIndex<0||!select.options)return '';
                    var option=select.options[select.selectedIndex];return cleanPersonName((option&&(option.textContent||option.value))||'');
                  };
                  var findOptionForPerson=function(select,name){
                    if(!select||!select.options)return -1;var wanted=personKey(name);
                    for(var i=0;i<select.options.length;i++){var candidate=personKey(select.options[i].textContent||select.options[i].value||'');if(candidate&&candidate===wanted)return i;}
                    return -1;
                  };
                  var tryRestoreMissingPerson=function(select,currentNames,state){
                    if(!select||!state||!state.desired.length)return false;
                    var currentKeys={};currentNames.forEach(function(name){currentKeys[personKey(name)]=true;});
                    var missing=state.desired.find(function(name){return !currentKeys[personKey(name)];});if(!missing)return false;
                    var optionIndex=findOptionForPerson(select,missing);if(optionIndex<0)return false;
                    var attempt={name:'',count:0,time:0};
                    try{var raw=sessionStorage.getItem(RESTORE_KEY);if(raw)attempt=JSON.parse(raw)||attempt;}catch(ignore){}
                    if(personKey(attempt.name)===personKey(missing)&&attempt.count>=2)return false;
                    if(personKey(attempt.name)===personKey(missing)&&Date.now()-(attempt.time||0)<1200){setTimeout(buildMobile,1250);return true;}
                    attempt={name:missing,count:(personKey(attempt.name)===personKey(missing)?(attempt.count||0)+1:1),time:Date.now()};
                    try{sessionStorage.setItem(RESTORE_KEY,JSON.stringify(attempt));}catch(ignore){}
                    select.selectedIndex=optionIndex;
                    select.dispatchEvent(new Event('input',{bubbles:true}));
                    select.dispatchEvent(new Event('change',{bubbles:true}));
                    setTimeout(buildMobile,1400);return true;
                  };
                  var removePersonFromMatrix=function(name){
                    var key=personKey(name),matrix=document.querySelector('.pfvr-attendance-matrix');if(!matrix||!key)return;
                    Array.from(matrix.querySelectorAll('[data-pfvr-person]')).forEach(function(el){if(el.getAttribute('data-pfvr-person')===key)el.remove();});
                    var count=matrix.querySelectorAll('.pfvr-person-header').length;
                    matrix.style.gridTemplateColumns='var(--pfvr-day-col) repeat('+count+',var(--pfvr-person-col))';
                  };
'''
if source.count(function_marker) != 1:
    raise SystemExit('helper marker mismatch')
source = source.replace(function_marker, helpers, 1)

# Replace management UI: persisted desired list, protected primary, local remove, original select for additions.
start = source.index("                  var personManagementControls=function(cell,name){")
end = source.index("\n\n                  var saveViewState=function(){", start)
new_management = r'''                  var findPersonTools=function(toolInfo,state){
                    if(!toolInfo||!toolInfo.select)return null;
                    var anchor=toolInfo.anchor,scope=toolInfo.scope,select=toolInfo.select;
                    var panel=element('div','pfvr-person-tools');
                    var head=element('div','pfvr-person-tools-head');
                    var title=element('div','pfvr-person-tools-title');title.textContent='Teilnehmende';
                    var toggle=element('button','pfvr-person-tools-toggle');toggle.type='button';toggle.textContent='+ / − Person';
                    head.appendChild(title);head.appendChild(toggle);panel.appendChild(head);
                    var body=element('div','pfvr-person-tools-body');
                    var hint=element('div');hint.textContent='Person hinzufügen:';hint.style.color=COLORS.muted;hint.style.fontSize='12px';body.appendChild(hint);
                    body.appendChild(select);
                    select.addEventListener('change',function(){var chosen=selectedOptionName(select);if(chosen)addDesiredPerson(state,chosen);},true);
                    var buttons=Array.from(scope.querySelectorAll('button,input[type=submit],input[type=button],a.btn,.btn')).filter(function(control){return norm(control.innerText||control.value).indexOf('alle anzeigen')>=0;});
                    buttons.forEach(function(control){control.addEventListener('click',function(){state.adoptNext=true;savePeopleState(state);},true);body.appendChild(control);});
                    var list=element('div','pfvr-managed-people');
                    var listTitle=element('div','pfvr-managed-people-title');listTitle.textContent='In deiner Ansicht';list.appendChild(listTitle);
                    state.desired.forEach(function(personName){
                      var line=element('div','pfvr-managed-person');
                      var name=element('div','pfvr-managed-person-name');fitPersonName(name,personName);line.appendChild(name);
                      if(personKey(personName)===personKey(state.primary)){
                        var primary=element('span','pfvr-primary-label');primary.textContent='Standard';line.appendChild(primary);
                      }else{
                        var remove=element('button','pfvr-local-remove');remove.type='button';remove.textContent='Entfernen';remove.setAttribute('aria-label','Person aus Ansicht entfernen: '+personName);
                        remove.addEventListener('click',function(){if(!removeDesiredPerson(state,personName))return;removePersonFromMatrix(personName);line.remove();});
                        line.appendChild(remove);
                      }
                      list.appendChild(line);
                    });
                    body.appendChild(list);panel.appendChild(body);
                    toggle.addEventListener('click',function(){panel.classList.toggle('open');});
                    anchor.style.display='none';return panel;
                  };'''
source = source[:start] + new_management + source[end:]

# Build from persisted desired list and restore missing rows before projecting the matrix.
old_build_prefix = """                    var header=rows[0],mobile=element('div','pfvr-attendance-mobile');
                    table.parentNode.insertBefore(mobile,table);
                    var participantRows=rows.slice(1).filter(function(row){return row.cells&&row.cells.length>=2;});
                    var names=participantRows.map(function(row,index){var value=cleanPersonName(text(row.cells[0]));return value||('Person '+(index+1));});
                    var tools=findPersonTools(table,participantRows,names);if(tools)mobile.appendChild(tools);
                    var matrixScroll=element('div','pfvr-matrix-scroll');
"""
new_build_prefix = """                    var header=rows[0];
                    var allParticipantRows=rows.slice(1).filter(function(row){return row.cells&&row.cells.length>=2;});
                    var allNames=allParticipantRows.map(function(row,index){var value=cleanPersonName(text(row.cells[0]));return value||('Person '+(index+1));});
                    var toolInfo=findPersonToolScope(),peopleState=loadPeopleState(allNames);
                    if(toolInfo&&tryRestoreMissingPerson(toolInfo.select,allNames,peopleState))return false;
                    try{sessionStorage.removeItem(RESTORE_KEY);}catch(ignore){}
                    var desiredKeys={};peopleState.desired.forEach(function(name){desiredKeys[personKey(name)]=true;});
                    var participantRows=[],names=[];
                    allParticipantRows.forEach(function(row,index){if(desiredKeys[personKey(allNames[index])]){participantRows.push(row);names.push(allNames[index]);}});
                    var mobile=element('div','pfvr-attendance-mobile');table.parentNode.insertBefore(mobile,table);
                    var tools=findPersonTools(toolInfo,peopleState);if(tools)mobile.appendChild(tools);
                    var matrixScroll=element('div','pfvr-matrix-scroll');
"""
if source.count(old_build_prefix) != 1:
    raise SystemExit('build prefix mismatch')
source = source.replace(old_build_prefix, new_build_prefix, 1)

old_headers = "                    names.forEach(function(personName){var personHeader=element('div','pfvr-person-header');personHeader.textContent=personName;matrix.appendChild(personHeader);});"
new_headers = "                    names.forEach(function(personName){var personHeader=element('div','pfvr-person-header');fitPersonName(personHeader,personName);personHeader.setAttribute('data-pfvr-person',personKey(personName));matrix.appendChild(personHeader);});"
if source.count(old_headers) != 1:
    raise SystemExit('header build mismatch')
source = source.replace(old_headers, new_headers, 1)

old_rows = """                      participantRows.forEach(function(row){
                        var cell=element('div','pfvr-person-cell');
                        var control=element('div','pfvr-person-control');
"""
new_rows = """                      participantRows.forEach(function(row,rowIndex){
                        var cell=element('div','pfvr-person-cell');cell.setAttribute('data-pfvr-person',personKey(names[rowIndex]));
                        var personLabel=element('div','pfvr-person-name-label');fitPersonName(personLabel,names[rowIndex]);cell.appendChild(personLabel);
                        var control=element('div','pfvr-person-control');
"""
if source.count(old_rows) != 1:
    raise SystemExit('row build mismatch')
source = source.replace(old_rows, new_rows, 1)

skin.write_text(source, encoding='utf-8')

# Tests for the exact regression set.
test_source = test.read_text(encoding='utf-8')
marker = "    @Test public void generatedScriptKeepsStatusRepairAndMobileViewport(){\n"
insertion = '''    @Test public void generatedScriptPersistsRestoresRemovesAndLabelsParticipants(){
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

'''
if insertion not in test_source:
    if marker not in test_source:
        raise SystemExit('test insertion marker missing')
    test_source = test_source.replace(marker, insertion + marker, 1)
test.write_text(test_source, encoding='utf-8')

# Version bump.
gradle_source = gradle.read_text(encoding='utf-8')
gradle_source = gradle_source.replace('versionCode 28', 'versionCode 29')
gradle_source = gradle_source.replace("versionName '0.10.4'", "versionName '0.10.5'")
if 'versionCode 29' not in gradle_source or "versionName '0.10.5'" not in gradle_source:
    raise SystemExit('version bump failed')
gradle.write_text(gradle_source, encoding='utf-8')

change_source = changelog.read_text(encoding='utf-8')
if '## 0.10.5' not in change_source:
    body = change_source.split('# Android Changelog\n',1)[1].lstrip()
    change_source = '# Android Changelog\n\n## 0.10.5\n- Gewünschte Personenliste der internen App-Ansicht wird dauerhaft im WebView-Origin gespeichert und nach App-Neustart über den echten Website-Select schrittweise wiederhergestellt.\n- Zusätzliche Personen können lokal aus der eigenen Übersicht entfernt werden; die Standard-/eigene Person bleibt geschützt. Das verändert keine serverseitigen An-/Abmeldedaten.\n- Teilnehmername steht zusätzlich in jeder Statuszelle. Lange Namen werden auf maximal zwei Zeilen begrenzt und moderat verkleinert; die gedeckelte Spaltenbreite für mindestens zwei sichtbare Personen bleibt bestehen.\n- Gemeinsames horizontales Scrollen der festen Personenspalten bleibt erhalten.\n\n' + body
changelog.write_text(change_source, encoding='utf-8')

status_source = status.read_text(encoding='utf-8')
status_source = status_source.replace('Stand: Testversion `0.10.4`', 'Stand: Testversion `0.10.5`')
status_marker = '## Implementiert / im Test\n\n'
status_entry = '- Die gewünschte Personenliste der internen App-Ansicht wird dauerhaft gespeichert. Fehlende Zusatzpersonen werden beim nächsten Laden über den originalen Website-Select wieder eingeblendet; zusätzliche Personen können lokal aus der Übersicht entfernt werden, ohne An-/Abmeldedaten auf dem Server zu löschen.\n- Teilnehmernamen stehen zusätzlich in jeder Statuszelle; lange Namen sind auf zwei Zeilen begrenzt und werden innerhalb der gedeckelten Personenspalte moderat verkleinert.\n'
if status_entry not in status_source:
    status_source = status_source.replace(status_marker, status_marker + status_entry, 1)
status_source = status_source.replace('Finale Testidentität: `0.10.4`, `versionCode 28`', 'Finale Testidentität: `0.10.5`, `versionCode 29`')
status.write_text(status_source, encoding='utf-8')

readme_source = readme.read_text(encoding='utf-8')
readme_source = readme_source.replace('Aktuelle Android-Testversion: `0.10.4`.', 'Aktuelle Android-Testversion: `0.10.5`.')
readme_source = readme_source.replace('## Gerätetest 0.10.4', '## Gerätetest 0.10.5')
readme_source = readme_source.replace('0.10.4 verwendet `versionCode 28`', '0.10.5 verwendet `versionCode 29`')
extra = '- Zwei oder mehr Zusatzpersonen einblenden, App komplett schließen und neu öffnen: die App-Ansicht soll die gespeicherte Personenliste über den Website-Select wiederherstellen.\n- Unter `+ / − Person` eine Zusatzperson lokal entfernen: sie muss sofort aus der Matrix verschwinden und nach Neustart entfernt bleiben; die Standardperson darf nicht entfernbar sein.\n- Auch weit unten in der Terminliste muss in jeder Personenzelle der Name sichtbar sein; lange Namen dürfen höchstens zwei Zeilen belegen und die Personenspalte nicht verbreitern.\n'
heading = '## Gerätetest 0.10.5\n\n'
if extra not in readme_source and heading in readme_source:
    readme_source = readme_source.replace(heading, heading + extra, 1)
readme.write_text(readme_source, encoding='utf-8')
