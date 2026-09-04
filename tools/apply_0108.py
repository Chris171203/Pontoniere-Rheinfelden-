from pathlib import Path
import re


def replace_once(text: str, old: str, new: str, label: str) -> str:
    count = text.count(old)
    if count != 1:
        raise RuntimeError(f"{label}: expected exactly one match, got {count}")
    return text.replace(old, new, 1)


def replace_between(text: str, start: str, end: str, replacement: str, label: str) -> str:
    start_index = text.find(start)
    if start_index < 0:
        raise RuntimeError(f"{label}: start marker not found")
    end_index = text.find(end, start_index + len(start))
    if end_index < 0:
        raise RuntimeError(f"{label}: end marker not found")
    return text[:start_index] + replacement + text[end_index + len(end):]


root = Path('.')
source_path = root / 'Android/app/src/main/java/ch/pfvr/internapp/InternalAttendanceSkin.java'
test_path = root / 'Android/app/src/test/java/ch/pfvr/internapp/InternalAttendanceSkinTest.java'
main_path = root / 'Android/app/src/main/java/ch/pfvr/internapp/MainActivity.java'
build_path = root / 'Android/app/build.gradle'
readme_path = root / 'Android/README.md'
changelog_path = root / 'Android/CHANGELOG.md'
status_path = root / 'STATUS.md'

source = source_path.read_text(encoding='utf-8')

# Directly testable mirror of the WebView display formatting.
method_marker = """    static String javascript(
"""
format_method = r'''    static String formatPersonDisplayName(String value) {
        if (value == null) return "";
        String clean = value
                .replaceAll("(?<=[\\p{Ll}])(?=[\\p{Lu}])", " ")
                .replaceAll("(?<=[\\p{Lu}]{2})(?=[\\p{Lu}][\\p{Ll}])", " ")
                .replaceAll("\\s*,\\s*", ", ")
                .replaceAll("\\s+", " ")
                .trim();
        if (clean.isEmpty() || clean.matches("(?i)(?:Person|Teilnehmer)\\s+\\d+")) return clean;
        int comma = clean.indexOf(',');
        if (comma >= 0) {
            String family = clean.substring(0, comma).trim();
            String given = clean.substring(comma + 1).trim();
            return given.isEmpty() ? family : family + ", " + given;
        }
        String[] parts = clean.split(" ");
        if (parts.length < 2) return clean;
        StringBuilder given = new StringBuilder();
        for (int index = 1; index < parts.length; index++) {
            if (given.length() > 0) given.append(' ');
            given.append(parts[index]);
        }
        return parts[0] + ", " + given;
    }

'''
source = replace_once(source, method_marker, format_method + method_marker, 'Java name formatter')

source = replace_once(
    source,
    """                  var PEOPLE_KEY='pfvr-attendance-people-v2';
                  var RESTORE_KEY='pfvr-attendance-restore-v2';
""",
    """                  var PEOPLE_KEY='pfvr-attendance-people-v3';
                  var LEGACY_PEOPLE_KEY='pfvr-attendance-people-v2';
                  var RESTORE_KEY='pfvr-attendance-restore-v3';
""",
    'WebView storage version',
)

source = replace_once(
    source,
    """                  .pfvr-person-tools{display:flex!important;flex-direction:column!important;gap:6px!important;background:${COLORS.card}!important;border:1px solid ${COLORS.border}!important;border-radius:14px!important;padding:10px!important;box-sizing:border-box!important;}
                  .pfvr-person-tools-head{display:flex!important;align-items:center!important;justify-content:space-between!important;gap:10px!important;}
                  .pfvr-person-tools-title{font-size:17px!important;font-weight:700!important;}
                  .pfvr-person-tools-toggle{min-height:40px!important;padding:7px 11px!important;background:${COLORS.link}!important;color:#fff!important;}
                  .pfvr-person-tools-body{display:none!important;flex-direction:column!important;gap:6px!important;padding-top:2px!important;}
                  .pfvr-person-tools.open .pfvr-person-tools-body{display:flex!important;}
""",
    """                  .pfvr-person-tools{display:none!important;position:fixed!important;z-index:10002!important;left:12px!important;right:12px!important;top:12px!important;max-height:calc(100vh - 24px)!important;overflow-y:auto!important;flex-direction:column!important;gap:8px!important;background:${COLORS.card}!important;border:1px solid ${COLORS.border}!important;border-radius:16px!important;padding:12px!important;box-sizing:border-box!important;box-shadow:0 16px 46px rgba(0,0,0,.38)!important;}
                  .pfvr-person-tools.open{display:flex!important;}
                  .pfvr-person-tools-backdrop{position:fixed!important;z-index:10001!important;inset:0!important;background:rgba(0,0,0,.52)!important;}
                  .pfvr-person-tools-head{display:flex!important;align-items:center!important;justify-content:space-between!important;gap:10px!important;}
                  .pfvr-person-tools-title{font-size:18px!important;font-weight:700!important;}
                  .pfvr-person-tools-toggle{min-height:38px!important;padding:6px 10px!important;background:${COLORS.soft}!important;color:${COLORS.text}!important;}
                  .pfvr-person-tools-body{display:flex!important;flex-direction:column!important;gap:8px!important;padding-top:2px!important;}
""",
    'person manager modal CSS',
)

old_name_block_start = "                  var cleanPersonName=function(value){"
old_name_block_end = "                  var savePeopleState=function(state){"
new_name_block = r'''                  var cleanPersonName=function(value){
                    return (value||'')
                      .replace(/[\u{1F300}-\u{1FAFF}]/gu,' ')
                      .replace(/([a-zäöüß])([A-ZÄÖÜ])/g,'$1 $2')
                      .replace(/([A-ZÄÖÜ]{2,})([A-ZÄÖÜ][a-zäöüß])/g,'$1 $2')
                      .replace(/\s*,\s*/g,', ')
                      .replace(/\s+/g,' ')
                      .trim();
                  };
                  var isPlaceholderPersonName=function(value){return /^(?:person|teilnehmer)\s+\d+$/i.test(cleanPersonName(value));};
                  var formatPersonName=function(value){
                    var clean=cleanPersonName(value);if(!clean||isPlaceholderPersonName(clean))return clean;
                    var comma=clean.indexOf(',');
                    if(comma>=0){var family=clean.slice(0,comma).trim(),given=clean.slice(comma+1).trim();return given?family+', '+given:family;}
                    var parts=clean.split(' ').filter(Boolean);if(parts.length<2)return clean;
                    return parts.shift()+', '+parts.join(' ');
                  };
                  var personCellText=function(cell){
                    if(!cell)return '';
                    var clone=cell.cloneNode(true);
                    clone.querySelectorAll('button,input,select,textarea,svg,img,picture,script,style').forEach(function(node){node.remove();});
                    var chunks=[],walker=document.createTreeWalker(clone,NodeFilter.SHOW_TEXT);
                    while(walker.nextNode()){
                      var chunk=cleanPersonName(walker.currentNode.nodeValue||'');if(!chunk)continue;
                      var normalized=norm(chunk);
                      if(/^(?:entfernen|löschen|loeschen|bearbeiten|kalender|anzeigen|zurück|zurueck)$/.test(normalized))continue;
                      chunks.push(chunk);
                    }
                    var candidate=cleanPersonName(chunks.join(' '));if(candidate)return candidate;
                    var attributes=['data-person-name','data-name','aria-label','title'];
                    var nodes=[cell].concat(Array.from(cell.querySelectorAll('[data-person-name],[data-name],[aria-label],[title]')));
                    for(var nodeIndex=0;nodeIndex<nodes.length;nodeIndex++){
                      for(var attrIndex=0;attrIndex<attributes.length;attrIndex++){
                        var attr=cleanPersonName(nodes[nodeIndex].getAttribute&&nodes[nodeIndex].getAttribute(attributes[attrIndex]));
                        if(attr&&!/^(?:entfernen|löschen|loeschen|bearbeiten|kalender|anzeigen)$/i.test(attr))return attr;
                      }
                    }
                    return '';
                  };
                  var moveChildren=function(from,to){while(from&&from.firstChild)to.appendChild(from.firstChild);};
                  var personKey=function(value){return cleanPersonName(value).toLowerCase();};
                  var fitPersonName=function(el,value){
                    var clean=formatPersonName(value);el.textContent=clean;el.title=clean;
                    if(clean.length>28)el.classList.add('pfvr-name-tiny');
                    else if(clean.length>19)el.classList.add('pfvr-name-small');
                  };
                  var savePeopleState=function(state){'''
source = replace_between(source, old_name_block_start, old_name_block_end, new_name_block, 'name extraction and display block')

source = replace_once(
    source,
    """                  var dedupePeople=function(list){var result=[];(list||[]).forEach(function(name){var clean=cleanPersonName(name);if(clean&&!listHasPerson(result,clean))result.push(clean);});return result;};
""",
    """                  var dedupePeople=function(list){var result=[];(list||[]).forEach(function(name){var clean=cleanPersonName(name);if(clean&&!isPlaceholderPersonName(clean)&&!listHasPerson(result,clean))result.push(clean);});return result;};
""",
    'placeholder filtering',
)

source = replace_once(
    source,
    """                  var loadPeopleState=function(currentNames){
                    var state=null;try{var raw=localStorage.getItem(PEOPLE_KEY);if(raw)state=JSON.parse(raw);}catch(ignore){}
                    if(!state||!Array.isArray(state.desired))state={version:2,primary:currentNames[0]||'',desired:[],hidden:[],restoreValues:{},pendingAdd:null};
                    state.version=2;if(!Array.isArray(state.hidden))state.hidden=[];if(!state.restoreValues||typeof state.restoreValues!=='object')state.restoreValues={};
                    if(!state.primary&&currentNames.length)state.primary=currentNames[0];
                    state.hidden=dedupePeople(state.hidden).filter(function(name){return !samePersonName(name,state.primary);});state.desired=dedupePeople(state.desired);
                    if(state.primary&&!listHasPerson(state.desired,state.primary))state.desired.unshift(state.primary);
                    adoptCurrentPeople(state,currentNames);return state;
                  };
""",
    """                  var readPeopleState=function(){
                    var state=null;
                    try{var raw=localStorage.getItem(PEOPLE_KEY);if(raw)state=JSON.parse(raw);else{var legacy=localStorage.getItem(LEGACY_PEOPLE_KEY);if(legacy)state=JSON.parse(legacy);}}catch(ignore){}
                    return state;
                  };
                  var loadPeopleState=function(currentNames,seed){
                    var state=seed||readPeopleState();
                    if(!state||!Array.isArray(state.desired))state={version:3,primary:currentNames[0]||'',desired:[],hidden:[],restoreValues:{},pendingAdd:null,rowNames:[]};
                    state.version=3;if(!Array.isArray(state.hidden))state.hidden=[];if(!Array.isArray(state.rowNames))state.rowNames=[];if(!state.restoreValues||typeof state.restoreValues!=='object')state.restoreValues={};
                    state.primary=cleanPersonName(state.primary);
                    if((!state.primary||isPlaceholderPersonName(state.primary))&&currentNames.length)state.primary=currentNames[0];
                    state.hidden=dedupePeople(state.hidden).filter(function(name){return !samePersonName(name,state.primary);});state.desired=dedupePeople(state.desired);state.rowNames=dedupePeople(state.rowNames);
                    if(state.primary&&!listHasPerson(state.desired,state.primary))state.desired.unshift(state.primary);
                    adoptCurrentPeople(state,currentNames);state.rowNames=currentNames.filter(function(name){return name&&!isPlaceholderPersonName(name);});savePeopleState(state);return state;
                  };
""",
    'state migration',
)

source = replace_once(
    source,
    """                  var currentSourceNames=function(table){
                    if(!table)return [];return Array.from(table.rows||[]).slice(1).filter(function(row){return row.cells&&row.cells.length>=2;}).map(function(row,index){var value=cleanPersonName(text(row.cells[0]));return value||('Person '+(index+1));});
                  };
                  var rememberPendingPerson=function(state,table,select,expectedName){
                    if(!state)return;state.pendingAdd={before:currentSourceNames(table),optionValue:selectedOptionValue(select),optionText:cleanPersonName(expectedName||''),time:Date.now()};savePeopleState(state);
                  };
""",
    """                  var optionNameForRow=function(row,select){
                    if(!row||!select||!select.options)return '';
                    var values=[];
                    var addValue=function(value){value=String(value||'').trim();if(value&&values.indexOf(value)<0)values.push(value);};
                    [row,row.cells&&row.cells[0]].filter(Boolean).forEach(function(node){['data-person-id','data-id-person','data-person','data-id'].forEach(function(attr){addValue(node.getAttribute&&node.getAttribute(attr));});});
                    row.querySelectorAll('input[type=hidden],a[href],[data-person-id],[data-id-person]').forEach(function(node){
                      var marker=norm((node.getAttribute&&((node.getAttribute('name')||'')+' '+(node.getAttribute('id')||'')))||'');
                      if(node.tagName==='INPUT'&&(marker.indexOf('person')>=0||marker.indexOf('teilnehmer')>=0))addValue(node.value);
                      ['data-person-id','data-id-person'].forEach(function(attr){addValue(node.getAttribute&&node.getAttribute(attr));});
                      if(node.tagName==='A')try{var url=new URL(node.href,location.href);['id_person','person_id','person'].forEach(function(key){addValue(url.searchParams.get(key));});}catch(ignore){}
                    });
                    for(var optionIndex=0;optionIndex<select.options.length;optionIndex++){
                      var option=select.options[optionIndex];if(values.indexOf(String(option.value||'').trim())>=0)return cleanPersonName(option.textContent||option.value||'');
                    }
                    return '';
                  };
                  var resolvePersonNames=function(rows,select,state){
                    var stored=[];
                    if(state){stored=(state.rowNames||[]).concat(state.desired||[]).concat(state.hidden||[]).filter(function(name){return name&&!isPlaceholderPersonName(name);});}
                    return rows.map(function(row,index){
                      var value=personCellText(row.cells&&row.cells[0]);
                      if(!value)value=optionNameForRow(row,select);
                      if(!value&&stored[index])value=stored[index];
                      return value||('Teilnehmer '+(index+1));
                    });
                  };
                  var currentSourceNames=function(table,select,state){
                    if(!table)return [];var rows=Array.from(table.rows||[]).slice(1).filter(function(row){return row.cells&&row.cells.length>=2;});return resolvePersonNames(rows,select,state);
                  };
                  var rememberPendingPerson=function(state,table,select,expectedName){
                    if(!state)return;state.pendingAdd={before:currentSourceNames(table,select,state),optionValue:selectedOptionValue(select),optionText:cleanPersonName(expectedName||''),time:Date.now()};savePeopleState(state);
                  };
""",
    'row name resolution',
)

source = replace_once(
    source,
    """                  var findPersonToolScope=function(){
                    var candidates=Array.from(document.querySelectorAll('label,p,span,div')).filter(function(el){var value=norm(text(el));return value.indexOf('person zur liste hinzuzufügen')===0&&value.length<120;});
                    if(!candidates.length)return null;
                    var anchor=candidates[0],scope=anchor.parentElement;
                    for(var depth=0;scope&&depth<4;depth++,scope=scope.parentElement){
                      if(scope===document.body)break;var select=scope.querySelector('select');if(select)return {anchor:anchor,scope:scope,select:select};
                    }
                    return null;
                  };
""",
    """                  var findPersonToolScope=function(){
                    var candidates=Array.from(document.querySelectorAll('label,p,span,div')).filter(function(el){var value=norm(text(el));return value.indexOf('person zur liste hinzuzufügen')===0&&value.length<120;});
                    if(candidates.length){
                      var anchor=candidates[0],scope=anchor.parentElement;
                      for(var depth=0;scope&&depth<4;depth++,scope=scope.parentElement){if(scope===document.body)break;var select=scope.querySelector('select');if(select)return {anchor:anchor,scope:scope,select:select};}
                    }
                    var best=null,bestScore=-1;
                    document.querySelectorAll('select').forEach(function(select){
                      if(select.closest('table'))return;var options=Array.from(select.options||[]),statusOptions=options.filter(function(option){return !!statusForValue(option.textContent||option.value||'');}).length;
                      if(options.length<3||statusOptions>Math.max(1,options.length/3))return;
                      var marker=norm((select.name||'')+' '+(select.id||'')+' '+text(select.parentElement));var score=options.length+(marker.indexOf('person')>=0?20:0)+(marker.indexOf('hinzuf')>=0?20:0);
                      if(score>bestScore){bestScore=score;best=select;}
                    });
                    if(!best)return null;var fallbackScope=best.closest('form')||best.parentElement;return {anchor:null,scope:fallbackScope,select:best};
                  };
""",
    'person select discovery',
)

old_remove_start = "                  var removePersonFromMatrix=function(name){"
old_remove_end = "                  var appendPersonColumn=function(table,row,personName,state){"
new_remove_block = r'''                  var updateMatrixColumns=function(matrix){
                    if(!matrix)return;var count=Array.from(matrix.querySelectorAll('.pfvr-person-header')).filter(function(header){return header.style.display!=='none';}).length;
                    matrix.style.gridTemplateColumns='var(--pfvr-day-col) repeat('+count+',var(--pfvr-person-col))';
                  };
                  var setPersonColumnHidden=function(name,hidden){
                    var matrix=document.querySelector('.pfvr-attendance-matrix');if(!matrix)return false;
                    var changed=false;Array.from(matrix.querySelectorAll('[data-pfvr-person]')).forEach(function(el){if(samePersonName(el.getAttribute('data-pfvr-person')||'',name)){el.style.display=hidden?'none':'';changed=true;}});
                    updateMatrixColumns(matrix);return changed;
                  };
                  var showDesiredPerson=function(state,name){
                    if(!state)return;state.hidden=(state.hidden||[]).filter(function(saved){return !samePersonName(saved,name);});addDesiredPerson(state,name);savePeopleState(state);
                  };
                  var appendHiddenPerson=function(list,state,personName){
                    if(!list)return;var key=personKey(personName);if(!key)return;
                    var exists=Array.from(list.querySelectorAll('.pfvr-managed-person')).some(function(line){return samePersonName(line.getAttribute('data-pfvr-managed-person')||'',personName);});if(exists)return;
                    var line=element('div','pfvr-managed-person');line.setAttribute('data-pfvr-managed-person',key);
                    var name=element('div','pfvr-managed-person-name');fitPersonName(name,personName);line.appendChild(name);
                    var show=element('button','pfvr-local-remove');show.type='button';show.textContent='Einblenden';
                    show.addEventListener('click',function(){showDesiredPerson(state,personName);if(!setPersonColumnHidden(personName,false))scheduleParticipantSync(state);appendManagedPerson(document.querySelector('.pfvr-managed-people-visible'),state,personName);line.remove();});
                    line.appendChild(show);list.appendChild(line);
                  };
                  var appendManagedPerson=function(list,state,personName){
                    if(!list||isHiddenPerson(state,personName))return;var key=personKey(personName);if(!key)return;
                    var exists=Array.from(list.querySelectorAll('.pfvr-managed-person')).some(function(line){return samePersonName(line.getAttribute('data-pfvr-managed-person')||'',personName);});if(exists)return;
                    var line=element('div','pfvr-managed-person');line.setAttribute('data-pfvr-managed-person',key);
                    var name=element('div','pfvr-managed-person-name');fitPersonName(name,personName);line.appendChild(name);
                    if(samePersonName(personName,state.primary)){
                      var primary=element('span','pfvr-primary-label');primary.textContent='Standard';line.appendChild(primary);
                    }else{
                      var remove=element('button','pfvr-local-remove');remove.type='button';remove.textContent='Entfernen';remove.setAttribute('aria-label','Person aus Ansicht entfernen: '+formatPersonName(personName));
                      remove.addEventListener('click',function(){if(!removeDesiredPerson(state,personName))return;setPersonColumnHidden(personName,true);appendHiddenPerson(document.querySelector('.pfvr-hidden-people'),state,personName);line.remove();});
                      line.appendChild(remove);
                    }
                    list.appendChild(line);
                  };
                  var matrixHasPerson=function(matrix,name){
                    if(!matrix)return false;return Array.from(matrix.querySelectorAll('.pfvr-person-header')).some(function(el){return samePersonName(el.getAttribute('data-pfvr-person')||'',name);});
                  };
                  var appendPersonColumn=function(table,row,personName,state){'''
source = replace_between(source, old_remove_start, old_remove_end, new_remove_block, 'non-destructive person removal')

source = replace_once(
    source,
    """                    var count=matrix.querySelectorAll('.pfvr-person-header').length;matrix.style.gridTemplateColumns='var(--pfvr-day-col) repeat('+count+',var(--pfvr-person-col))';
                    appendManagedPerson(document.querySelector('.pfvr-managed-people'),state,personName);
""",
    """                    updateMatrixColumns(matrix);
                    appendManagedPerson(document.querySelector('.pfvr-managed-people-visible'),state,personName);
""",
    'matrix append bookkeeping',
)

source = replace_once(
    source,
    """                    var participantRows=rows.slice(1).filter(function(row){return row.cells&&row.cells.length>=2;});
                    var names=participantRows.map(function(row,index){var value=cleanPersonName(text(row.cells[0]));return value||('Person '+(index+1));});
                    adoptCurrentPeople(state,names);
""",
    """                    var participantRows=rows.slice(1).filter(function(row){return row.cells&&row.cells.length>=2;});
                    var toolInfo=findPersonToolScope(),names=resolvePersonNames(participantRows,toolInfo&&toolInfo.select,state);
                    adoptCurrentPeople(state,names);state.rowNames=names.filter(function(name){return !isPlaceholderPersonName(name);});savePeopleState(state);
""",
    'dynamic name resolution',
)

source = replace_once(
    source,
    """                  var findPersonTools=function(toolInfo,state){
                    if(!toolInfo||!toolInfo.select)return null;
                    var anchor=toolInfo.anchor,scope=toolInfo.scope,select=toolInfo.select;
                    var panel=element('div','pfvr-person-tools');
                    var head=element('div','pfvr-person-tools-head');
                    var title=element('div','pfvr-person-tools-title');title.textContent='Teilnehmende';
                    var toggle=element('button','pfvr-person-tools-toggle');toggle.type='button';toggle.textContent='+ / − Person';
                    head.appendChild(title);head.appendChild(toggle);panel.appendChild(head);
""",
    """                  var closePersonManager=function(){var panel=document.querySelector('.pfvr-person-tools');if(panel)panel.classList.remove('open');var backdrop=document.querySelector('.pfvr-person-tools-backdrop');if(backdrop)backdrop.remove();};
                  var findPersonTools=function(toolInfo,state,currentNames){
                    if(!toolInfo||!toolInfo.select)return null;
                    var anchor=toolInfo.anchor,scope=toolInfo.scope,select=toolInfo.select;
                    var panel=element('div','pfvr-person-tools');
                    var head=element('div','pfvr-person-tools-head');
                    var title=element('div','pfvr-person-tools-title');title.textContent='Personen verwalten';
                    var toggle=element('button','pfvr-person-tools-toggle');toggle.type='button';toggle.textContent='Schliessen';toggle.addEventListener('click',closePersonManager);
                    head.appendChild(title);head.appendChild(toggle);panel.appendChild(head);
""",
    'manager header and signature',
)

source = replace_once(
    source,
    """                    var actionControls=Array.from(scope.querySelectorAll('button,input[type=submit],input[type=button],a.btn,.btn')).filter(function(control){return control!==select&&norm(control.innerText||control.value).length>0;});
                    actionControls.forEach(function(control){
                      var label=(control.innerText||control.value||'').trim(),action=element('button');action.type='button';action.textContent=label;action.setAttribute('aria-label',label);
                      action.addEventListener('click',function(){if(norm(label).indexOf('alle anzeigen')>=0){state.hidden=[];state.pendingAdd=null;savePeopleState(state);}control.click();scheduleParticipantSync(state);});
                      body.appendChild(action);control.style.setProperty('display','none','important');
                    });
                    var list=element('div','pfvr-managed-people');
                    var listTitle=element('div','pfvr-managed-people-title');listTitle.textContent='In deiner Ansicht';list.appendChild(listTitle);
                    state.desired.forEach(function(personName){appendManagedPerson(list,state,personName);});
                    body.appendChild(list);panel.appendChild(body);
                    toggle.addEventListener('click',function(){panel.classList.toggle('open');});
                    anchor.style.display='none';return panel;
""",
    """                    var bulkPeopleAction=function(label){var value=norm(label);return value.indexOf('alle anzeigen')>=0||value.indexOf('alle hinzufügen')>=0||value.indexOf('alle hinzuf')>=0;};
                    var actionControls=Array.from(scope.querySelectorAll('button,input[type=submit],input[type=button],a.btn,.btn')).filter(function(control){return control!==select&&norm(control.innerText||control.value).length>0;});
                    actionControls.forEach(function(control){
                      var label=(control.innerText||control.value||'').trim();control.style.setProperty('display','none','important');
                      if(bulkPeopleAction(label))return;
                      var action=element('button');action.type='button';action.textContent=label;action.setAttribute('aria-label',label);
                      action.addEventListener('click',function(){control.click();scheduleParticipantSync(state);});body.appendChild(action);
                    });
                    var note=element('div');note.textContent='Entfernen blendet die Person nur in der App-Ansicht aus.';note.style.color=COLORS.muted;note.style.fontSize='11px';body.appendChild(note);
                    var list=element('div','pfvr-managed-people pfvr-managed-people-visible');
                    var listTitle=element('div','pfvr-managed-people-title');listTitle.textContent='Angezeigte Personen';list.appendChild(listTitle);
                    (currentNames||state.desired).forEach(function(personName){appendManagedPerson(list,state,personName);});body.appendChild(list);
                    var hiddenList=element('div','pfvr-managed-people pfvr-hidden-people');
                    var hiddenTitle=element('div','pfvr-managed-people-title');hiddenTitle.textContent='Ausgeblendet';hiddenList.appendChild(hiddenTitle);
                    (state.hidden||[]).forEach(function(personName){appendHiddenPerson(hiddenList,state,personName);});body.appendChild(hiddenList);panel.appendChild(body);
                    if(anchor&&anchor!==scope)anchor.style.display='none';return panel;
""",
    'manager actions and lists',
)

source = replace_once(
    source,
    """                    var allParticipantRows=rows.slice(1).filter(function(row){return row.cells&&row.cells.length>=2;});
                    var allNames=allParticipantRows.map(function(row,index){var value=cleanPersonName(text(row.cells[0]));return value||('Person '+(index+1));});
                    var toolInfo=findPersonToolScope(),peopleState=loadPeopleState(allNames);
""",
    """                    var allParticipantRows=rows.slice(1).filter(function(row){return row.cells&&row.cells.length>=2;});
                    var toolInfo=findPersonToolScope(),seedState=readPeopleState();
                    var allNames=resolvePersonNames(allParticipantRows,toolInfo&&toolInfo.select,seedState);
                    var peopleState=loadPeopleState(allNames,seedState);
""",
    'initial name resolution',
)

source = replace_once(
    source,
    """                    var tools=findPersonTools(toolInfo,peopleState);if(tools)mobile.appendChild(tools);
""",
    """                    var tools=findPersonTools(toolInfo,peopleState,names);if(tools)mobile.appendChild(tools);
""",
    'manager current names',
)

source = replace_once(
    source,
    """                    return true;
                  };

                  document.querySelectorAll('p,div,strong,label').forEach(function(el){var value=norm(text(el));if(value.indexOf('tipp: diese seite als favorit')===0&&value.length<350)el.style.display='none';});
""",
    """                    return true;
                  };
                  window.pfvrOpenPeopleManager=function(){
                    var panel=document.querySelector('.pfvr-person-tools');if(!panel){buildMobile();panel=document.querySelector('.pfvr-person-tools');}if(!panel)return false;
                    closePersonManager();var backdrop=element('div','pfvr-person-tools-backdrop');backdrop.addEventListener('click',closePersonManager);document.body.appendChild(backdrop);panel.classList.add('open');panel.scrollTop=0;return true;
                  };

                  document.querySelectorAll('p,div,strong,label').forEach(function(el){var value=norm(text(el));if(value.indexOf('tipp: diese seite als favorit')===0&&value.length<350)el.style.display='none';});
""",
    'native manager bridge',
)

source_path.write_text(source, encoding='utf-8')

# Native toolbar: app mode uses the otherwise redundant local back button for person management.
main = main_path.read_text(encoding='utf-8')
main = replace_once(
    main,
    """        Button back=btn("‹ Zurück",Color.WHITE,NAVY); back.setOnClickListener(v->handleBack()); tools.addView(back,new LinearLayout.LayoutParams(0,dp(40),1));
        boolean appView=prefs.getBoolean(PREF_INTERNAL_APP_VIEW,true);
        Button mode=btn(appView?"Original":"App-Ansicht",NAVY,Color.WHITE);
        mode.setOnClickListener(v->{boolean next=!prefs.getBoolean(PREF_INTERNAL_APP_VIEW,true);prefs.edit().putBoolean(PREF_INTERNAL_APP_VIEW,next).apply();mode.setText(next?"Original":"App-Ansicht");web.clearCache(false);web.reload();});
""",
    """        boolean appView=prefs.getBoolean(PREF_INTERNAL_APP_VIEW,true);
        Button people=btn(appView?"Personen":"‹ Zurück",Color.WHITE,NAVY);
        people.setContentDescription(appView?"Personen hinzufügen oder entfernen":"Zurück");
        people.setOnClickListener(v->{if(prefs.getBoolean(PREF_INTERNAL_APP_VIEW,true))openInternalPeopleManager(web,0);else handleBack();});
        tools.addView(people,new LinearLayout.LayoutParams(0,dp(40),1));
        Button mode=btn(appView?"Original":"App-Ansicht",NAVY,Color.WHITE);
        mode.setOnClickListener(v->{boolean next=!prefs.getBoolean(PREF_INTERNAL_APP_VIEW,true);prefs.edit().putBoolean(PREF_INTERNAL_APP_VIEW,next).apply();mode.setText(next?"Original":"App-Ansicht");people.setText(next?"Personen":"‹ Zurück");people.setContentDescription(next?"Personen hinzufügen oder entfernen":"Zurück");web.clearCache(false);web.reload();});
""",
    'internal toolbar people button',
)

helper_marker = """    private void showInternalLoadError(WebView v,String message){
"""
helper = """    private void openInternalPeopleManager(WebView web,int attempt){
        if(web==null)return;
        String script="(function(){try{return !!(window.pfvrOpenPeopleManager&&window.pfvrOpenPeopleManager());}catch(e){return false;}})();";
        web.evaluateJavascript(script,result->{
            if("true".equalsIgnoreCase(String.valueOf(result)))return;
            if(attempt>=2){Toast.makeText(this,"Personenverwaltung ist auf dieser Seite nicht verfügbar.",Toast.LENGTH_SHORT).show();return;}
            if(attempt==0)internalSkin(web);
            new Handler(Looper.getMainLooper()).postDelayed(()->openInternalPeopleManager(web,attempt+1),attempt==0?260L:650L);
        });
    }

"""
main = replace_once(main, helper_marker, helper + helper_marker, 'native people manager helper')
main_path.write_text(main, encoding='utf-8')

# Version.
build = build_path.read_text(encoding='utf-8')
build = replace_once(build, 'versionCode 31', 'versionCode 32', 'version code')
build = replace_once(build, "versionName '0.10.7'", "versionName '0.10.8'", 'version name')
build_path.write_text(build, encoding='utf-8')

# Tests.
test = test_path.read_text(encoding='utf-8')
test = replace_once(
    test,
    """    @Test public void leavesOrdinaryAppointmentTextUntouched(){
        assertNull(InternalAttendanceSkin.splitLeadingStatus("Schiffe reinigen"));
    }

""",
    """    @Test public void leavesOrdinaryAppointmentTextUntouched(){
        assertNull(InternalAttendanceSkin.splitLeadingStatus("Schiffe reinigen"));
    }

    @Test public void formatsParticipantNamesAsFamilyCommaGiven(){
        assertEquals("Neugebauer, Christoph",InternalAttendanceSkin.formatPersonDisplayName("NeugebauerChristoph"));
        assertEquals("Wiekert, Stephan",InternalAttendanceSkin.formatPersonDisplayName("Wiekert Stephan"));
        assertEquals("Müller-Lüdenscheidt, Anna Maria",InternalAttendanceSkin.formatPersonDisplayName("Müller-Lüdenscheidt Anna Maria"));
        assertEquals("Person 2",InternalAttendanceSkin.formatPersonDisplayName("Person 2"));
    }

""",
    'direct name formatting tests',
)
test = test.replace('pfvr-attendance-people-v2', 'pfvr-attendance-people-v3')
test = replace_once(
    test,
    """        assertTrue(script.contains("+ / − Person"));
""",
    """        assertTrue(script.contains("Personen verwalten"));
        assertTrue(script.contains("window.pfvrOpenPeopleManager"));
        assertTrue(script.contains("pfvr-person-tools-backdrop"));
""",
    'manager bridge assertions',
)
test = replace_once(
    test,
    """        assertTrue(script.contains("pfvr-local-remove"));
        assertTrue(script.contains("removeDesiredPerson"));
""",
    """        assertTrue(script.contains("pfvr-local-remove"));
        assertTrue(script.contains("removeDesiredPerson"));
        assertTrue(script.contains("setPersonColumnHidden"));
        assertTrue(script.contains("Einblenden"));
""",
    'non destructive removal assertions',
)
test = replace_once(
    test,
    """        assertTrue(script.contains("clean.length>26"));
        assertTrue(script.contains("clean.length>17"));
""",
    """        assertTrue(script.contains("clean.length>28"));
        assertTrue(script.contains("clean.length>19"));
        assertTrue(script.contains("formatPersonName"));
        assertTrue(script.contains("personCellText"));
        assertTrue(script.contains("optionNameForRow"));
""",
    'name extraction assertions',
)
marker = """    @Test public void generatedScriptKeepsStatusRepairAndMobileViewport(){
"""
new_test = """    @Test public void generatedScriptHidesBulkActionAndExposesManagerFromNativeToolbar(){
        String script=InternalAttendanceSkin.javascript("#11171C","#1A2228","#232E36","#ECF1F4","#A0B0BA","#344550","#5BBED5");
        assertTrue(script.contains("bulkPeopleAction"));
        assertTrue(script.contains("value.indexOf('alle anzeigen')"));
        assertTrue(script.contains("value.indexOf('alle hinzufügen')"));
        assertTrue(script.contains("if(bulkPeopleAction(label))return"));
        assertTrue(script.contains("Schliessen"));
        assertTrue(script.contains("Angezeigte Personen"));
        assertTrue(script.contains("Entfernen blendet die Person nur in der App-Ansicht aus."));
    }

"""
test = replace_once(test, marker, new_test + marker, 'bulk action regression test')
test_path.write_text(test, encoding='utf-8')

# Documentation.
readme = readme_path.read_text(encoding='utf-8')
readme = readme.replace('Aktuelle Android-Testversion: `0.10.7`.', 'Aktuelle Android-Testversion: `0.10.8`.')
readme = readme.replace('## Gerätetest 0.10.7', '## Gerätetest 0.10.8')
readme = readme.replace('0.10.7 verwendet `versionCode 31`', '0.10.8 verwendet `versionCode 32`')
readme = replace_once(
    readme,
    """- Ausgangslage für die Regression: Ist die zweite Person in `Original` bereits sichtbar, muss sie nach Wechsel zu `App` dauerhaft als zweite feste Personenspalte stehen bleiben und darf nicht mehr kurz aufblitzen und wieder verschwinden.
""",
    """- Namen werden als `Nachname, Vorname` angezeigt. Getrennte DOM-Textteile und zusammengezogene Formen wie `NeugebauerChristoph` müssen gleich ausgewertet werden; generische `Person 1`-Bezeichnungen dürfen nur als letzter technischer Fallback erscheinen.
- In der App-Ansicht öffnet der linke Werkzeugleisten-Button `Personen` eine modale Verwaltung zum Hinzufügen, Entfernen und Wieder-Einblenden. Der separate Website-Befehl `Alle anzeigen/hinzufügen` wird dort bewusst nicht angeboten und bleibt der Originalansicht vorbehalten.
- Ausgangslage für die Regression: Ist die zweite Person in `Original` bereits sichtbar, muss sie nach Wechsel zu `App` dauerhaft als zweite feste Personenspalte stehen bleiben und darf nicht mehr kurz aufblitzen und wieder verschwinden.
""",
    'README 0.10.8 focus',
)
readme_path.write_text(readme, encoding='utf-8')

changelog = changelog_path.read_text(encoding='utf-8')
entry = """## 0.10.8
- Teilnehmernamen werden robust aus getrennten DOM-Textteilen, CamelCase und vorhandenen Personen-IDs gelesen und als `Nachname, Vorname` dargestellt.
- Der App-Modus verwendet den linken Werkzeugleisten-Button als direkt erreichbare modale Personenverwaltung; Hinzufügen, Entfernen und Wieder-Einblenden liegen an einer Stelle.
- `Alle anzeigen/hinzufügen` wird im App-Modus nicht mehr gespiegelt. Die Funktion bleibt ausschließlich in der unveränderten Originalansicht verfügbar.
- Lokales Entfernen blendet Personenspalten nur aus und löscht die darin enthaltenen echten Website-Controls nicht mehr aus dem DOM.
- Alte generische Namen aus 0.10.7 werden bei der Migration verworfen; letzte erkannte Zeilennamen dienen nur noch als Fallback.

"""
changelog = replace_once(changelog, '# Android Changelog\n\n', '# Android Changelog\n\n' + entry, 'changelog entry')
changelog_path.write_text(changelog, encoding='utf-8')

status = status_path.read_text(encoding='utf-8')
status = status.replace('Stand: Testversion `0.10.7` · aktualisiert 2026-09-04.', 'Stand: Testversion `0.10.8` · aktualisiert 2026-09-04.')
status = replace_once(
    status,
    '## Implementiert / im Test\n\n',
    '## Implementiert / im Test\n\n- Namen in der mobilen Teilnehmermatrix werden aus Textknoten und Personenattributen rekonstruiert, CamelCase wird getrennt und die Anzeige auf `Nachname, Vorname` normalisiert.\n- Der linke App-Werkzeugleistenbutton öffnet eine modale Personenverwaltung. `Alle anzeigen/hinzufügen` bleibt der Originalansicht vorbehalten; Entfernen ist als nicht-destruktives lokales Ausblenden mit Wieder-Einblenden umgesetzt.\n\n',
    'status entry',
)
status = status.replace('Finale Testidentität: `0.10.7`, `versionCode 31`', 'Finale Testidentität: `0.10.8`, `versionCode 32`')
status_path.write_text(status, encoding='utf-8')

print('Applied PFVR 0.10.8 person manager and name formatting update')
