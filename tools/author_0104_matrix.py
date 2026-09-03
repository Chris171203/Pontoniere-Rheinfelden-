from pathlib import Path

skin = Path('Android/app/src/main/java/ch/pfvr/internapp/InternalAttendanceSkin.java')
test = Path('Android/app/src/test/java/ch/pfvr/internapp/InternalAttendanceSkinTest.java')
changelog = Path('Android/CHANGELOG.md')
status = Path('STATUS.md')

source = skin.read_text(encoding='utf-8')

old_css = '''                  .pfvr-day-section{display:grid!important;grid-template-columns:minmax(108px,32%) minmax(0,1fr)!important;gap:6px!important;align-items:stretch!important;width:100%!important;max-width:100%!important;box-sizing:border-box!important;}
                  .pfvr-day-meta{min-width:0!important;background:${COLORS.card}!important;border:1px solid ${COLORS.border}!important;border-radius:13px!important;padding:9px!important;font-size:13px!important;box-sizing:border-box!important;overflow-wrap:break-word!important;word-break:normal!important;}
                  .pfvr-day-meta>*{max-width:100%!important;box-sizing:border-box!important;white-space:normal!important;overflow-wrap:break-word!important;word-break:normal!important;}
                  .pfvr-day-people{display:flex!important;gap:6px!important;overflow-x:auto!important;overflow-y:hidden!important;min-width:0!important;padding:0 0 3px!important;box-sizing:border-box!important;scroll-snap-type:x proximity!important;-webkit-overflow-scrolling:touch!important;overscroll-behavior-x:contain!important;}
                  .pfvr-person-card{flex:0 0 calc((100% - 6px)/2)!important;min-width:0!important;min-height:100%!important;background:${COLORS.card}!important;border:1px solid ${COLORS.border}!important;border-radius:13px!important;padding:8px!important;box-sizing:border-box!important;scroll-snap-align:start!important;display:flex!important;flex-direction:column!important;gap:6px!important;}
                  .pfvr-person-name{font-size:13px!important;font-weight:700!important;line-height:1.25!important;overflow-wrap:break-word!important;word-break:normal!important;}
                  .pfvr-person-control{display:flex!important;flex-direction:column!important;gap:4px!important;margin-top:auto!important;min-width:0!important;}
                  .pfvr-person-control>*{max-width:100%!important;box-sizing:border-box!important;}
                  .pfvr-person-control button,.pfvr-person-control input[type=submit],.pfvr-person-control input[type=button],.pfvr-person-control a.btn,.pfvr-person-control .btn,.pfvr-person-control select{width:100%!important;min-height:38px!important;padding:6px 7px!important;font-size:12px!important;border-radius:9px!important;}
                  .pfvr-empty-status{font-size:12px!important;color:${COLORS.muted}!important;}
                  .pfvr-attendance-status{display:block!important;width:max-content!important;max-width:100%!important;padding:5px 7px!important;margin:0 0 5px!important;font-size:12px!important;border-radius:10px!important;font-weight:700!important;line-height:1.2!important;white-space:normal!important;overflow-wrap:break-word!important;word-break:normal!important;}
                  .pfvr-attendance-detail{display:block!important;margin:0 0 4px!important;line-height:1.35!important;color:${COLORS.text}!important;white-space:normal!important;overflow-wrap:break-word!important;word-break:normal!important;}
                  @media(max-width:340px){.pfvr-day-section{grid-template-columns:100px minmax(0,1fr)!important;}.pfvr-day-meta{padding:8px!important;font-size:12px!important;}.pfvr-person-card{padding:7px!important;}.pfvr-person-name{font-size:12px!important;}.pfvr-person-control button,.pfvr-person-control input[type=submit],.pfvr-person-control input[type=button],.pfvr-person-control a.btn,.pfvr-person-control .btn,.pfvr-person-control select{font-size:11px!important;padding:5px!important;}}
'''
new_css = '''                  .pfvr-matrix-scroll{display:block!important;width:100%!important;max-width:100%!important;overflow-x:auto!important;overflow-y:visible!important;box-sizing:border-box!important;padding:0 0 4px!important;-webkit-overflow-scrolling:touch!important;overscroll-behavior-x:contain!important;}
                  .pfvr-attendance-matrix{--pfvr-day-col:96px;--pfvr-person-col:clamp(104px,calc((100vw - 128px)/2),138px);display:grid!important;gap:6px!important;align-items:stretch!important;width:max-content!important;min-width:100%!important;box-sizing:border-box!important;}
                  .pfvr-matrix-corner,.pfvr-person-header,.pfvr-day-meta,.pfvr-person-cell{box-sizing:border-box!important;border:1px solid ${COLORS.border}!important;border-radius:13px!important;background:${COLORS.card}!important;min-width:0!important;}
                  .pfvr-matrix-corner{position:sticky!important;left:0!important;z-index:4!important;width:var(--pfvr-day-col)!important;padding:8px!important;font-size:11px!important;font-weight:700!important;color:${COLORS.muted}!important;display:flex!important;align-items:center!important;}
                  .pfvr-person-header{padding:8px 7px!important;font-size:12px!important;font-weight:700!important;line-height:1.2!important;overflow-wrap:break-word!important;word-break:normal!important;display:flex!important;align-items:center!important;}
                  .pfvr-day-meta{position:sticky!important;left:0!important;z-index:3!important;width:var(--pfvr-day-col)!important;padding:8px!important;font-size:12px!important;overflow-wrap:break-word!important;word-break:normal!important;box-shadow:3px 0 8px rgba(0,0,0,.04)!important;}
                  .pfvr-day-meta>*{max-width:100%!important;box-sizing:border-box!important;white-space:normal!important;overflow-wrap:break-word!important;word-break:normal!important;}
                  .pfvr-person-cell{width:var(--pfvr-person-col)!important;padding:7px!important;display:flex!important;flex-direction:column!important;gap:4px!important;}
                  .pfvr-person-control{display:flex!important;flex-direction:column!important;gap:4px!important;margin-top:auto!important;min-width:0!important;}
                  .pfvr-person-control>*{max-width:100%!important;box-sizing:border-box!important;}
                  .pfvr-person-control button,.pfvr-person-control input[type=submit],.pfvr-person-control input[type=button],.pfvr-person-control a.btn,.pfvr-person-control .btn,.pfvr-person-control select{width:100%!important;min-height:36px!important;padding:5px 6px!important;font-size:11px!important;border-radius:8px!important;}
                  .pfvr-empty-status{font-size:11px!important;color:${COLORS.muted}!important;}
                  .pfvr-attendance-status{display:block!important;width:max-content!important;max-width:100%!important;padding:4px 6px!important;margin:0 0 4px!important;font-size:11px!important;border-radius:8px!important;font-weight:700!important;line-height:1.2!important;white-space:normal!important;overflow-wrap:break-word!important;word-break:normal!important;}
                  .pfvr-attendance-detail{display:block!important;margin:0 0 3px!important;font-size:11px!important;line-height:1.3!important;color:${COLORS.text}!important;white-space:normal!important;overflow-wrap:break-word!important;word-break:normal!important;}
                  @media(max-width:340px){.pfvr-attendance-matrix{--pfvr-day-col:84px;--pfvr-person-col:102px;}.pfvr-day-meta{padding:7px!important;font-size:11px!important;}.pfvr-person-header{font-size:11px!important;padding:7px 5px!important;}.pfvr-person-cell{padding:6px!important;}.pfvr-person-control button,.pfvr-person-control input[type=submit],.pfvr-person-control input[type=button],.pfvr-person-control a.btn,.pfvr-person-control .btn,.pfvr-person-control select{font-size:10px!important;padding:4px!important;}}
'''
if source.count(old_css) != 1:
    raise SystemExit(f'old matrix css matches={source.count(old_css)}')
source = source.replace(old_css, new_css, 1)

old_state = '''                  var saveViewState=function(){
                    try{
                      var strips=Array.from(document.querySelectorAll('.pfvr-day-people')).map(function(strip){return strip.scrollLeft||0;});
                      sessionStorage.setItem(STORAGE_KEY,JSON.stringify({y:window.scrollY||document.documentElement.scrollTop||0,strips:strips,time:Date.now()}));
                    }catch(ignore){}
                  };
                  var restoreViewState=function(){
                    try{
                      var raw=sessionStorage.getItem(STORAGE_KEY);if(!raw)return;
                      var state=JSON.parse(raw);if(!state||Date.now()-(state.time||0)>120000)return;
                      requestAnimationFrame(function(){window.scrollTo(0,state.y||0);Array.from(document.querySelectorAll('.pfvr-day-people')).forEach(function(strip,index){if(state.strips&&state.strips[index]!=null)strip.scrollLeft=state.strips[index];});});
                    }catch(ignore){}
                  };
'''
new_state = '''                  var saveViewState=function(){
                    try{
                      var matrixScroll=document.querySelector('.pfvr-matrix-scroll');
                      sessionStorage.setItem(STORAGE_KEY,JSON.stringify({y:window.scrollY||document.documentElement.scrollTop||0,x:matrixScroll?(matrixScroll.scrollLeft||0):0,time:Date.now()}));
                    }catch(ignore){}
                  };
                  var restoreViewState=function(){
                    try{
                      var raw=sessionStorage.getItem(STORAGE_KEY);if(!raw)return;
                      var state=JSON.parse(raw);if(!state||Date.now()-(state.time||0)>120000)return;
                      requestAnimationFrame(function(){
                        window.scrollTo(0,state.y||0);
                        var matrixScroll=document.querySelector('.pfvr-matrix-scroll');if(matrixScroll)matrixScroll.scrollLeft=state.x||0;
                      });
                    }catch(ignore){}
                  };
'''
if source.count(old_state) != 1:
    raise SystemExit(f'old state matches={source.count(old_state)}')
source = source.replace(old_state, new_state, 1)

old_build = '''                    var tools=findPersonTools(table,participantRows,names);if(tools)mobile.appendChild(tools);
                    for(var column=1;column<header.cells.length;column++){
                      var section=element('section','pfvr-day-section');
                      var meta=element('div','pfvr-day-meta');moveChildren(header.cells[column],meta);section.appendChild(meta);
                      var people=element('div','pfvr-day-people');
                      participantRows.forEach(function(row,rowIndex){
                        if(!row.cells[column])return;
                        var person=element('div','pfvr-person-card');
                        var name=element('div','pfvr-person-name');name.textContent=names[rowIndex];person.appendChild(name);
                        var control=element('div','pfvr-person-control');moveChildren(row.cells[column],control);
                        if(!control.textContent.trim()&&!control.querySelector('button,input,select,a')){var empty=element('span','pfvr-empty-status');empty.textContent='Keine Auswahl für diesen Termin';control.appendChild(empty);}
                        person.appendChild(control);people.appendChild(person);
                      });
                      section.appendChild(people);mobile.appendChild(section);
                    }
'''
new_build = '''                    var tools=findPersonTools(table,participantRows,names);if(tools)mobile.appendChild(tools);
                    var matrixScroll=element('div','pfvr-matrix-scroll');
                    var matrix=element('div','pfvr-attendance-matrix');
                    matrix.style.gridTemplateColumns='var(--pfvr-day-col) repeat('+names.length+',var(--pfvr-person-col))';
                    var corner=element('div','pfvr-matrix-corner');corner.textContent='Termin';matrix.appendChild(corner);
                    names.forEach(function(personName){var personHeader=element('div','pfvr-person-header');personHeader.textContent=personName;matrix.appendChild(personHeader);});
                    for(var column=1;column<header.cells.length;column++){
                      var meta=element('div','pfvr-day-meta');moveChildren(header.cells[column],meta);matrix.appendChild(meta);
                      participantRows.forEach(function(row){
                        var cell=element('div','pfvr-person-cell');
                        var control=element('div','pfvr-person-control');
                        if(row.cells[column])moveChildren(row.cells[column],control);
                        if(!control.textContent.trim()&&!control.querySelector('button,input,select,a')){var empty=element('span','pfvr-empty-status');empty.textContent='Keine Auswahl für diesen Termin';control.appendChild(empty);}
                        cell.appendChild(control);matrix.appendChild(cell);
                      });
                    }
                    matrixScroll.appendChild(matrix);mobile.appendChild(matrixScroll);
'''
if source.count(old_build) != 1:
    raise SystemExit(f'old build matches={source.count(old_build)}')
source = source.replace(old_build, new_build, 1)
skin.write_text(source, encoding='utf-8')

test_source = test.read_text(encoding='utf-8')
old_test = '''    @Test public void generatedScriptBuildsDayRowsWithParticipantsToTheRight(){
        String script=InternalAttendanceSkin.javascript("#11171C","#1A2228","#232E36","#ECF1F4","#A0B0BA","#344550","#5BBED5");
        assertTrue(script.contains("pfvr-day-section"));
        assertTrue(script.contains("grid-template-columns:minmax(108px,32%)"));
        assertTrue(script.contains("pfvr-day-people"));
        assertTrue(script.contains("pfvr-person-card"));
        assertTrue(script.contains("overflow-x:auto"));
        assertTrue(script.contains("moveChildren(header.cells[column],meta)"));
        assertTrue(script.contains("moveChildren(row.cells[column],control)"));
        assertFalse(script.contains("min-width:176px"));
    }
'''
new_test = '''    @Test public void generatedScriptBuildsSharedPersonColumnsAcrossAllDays(){
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
'''
if test_source.count(old_test) != 1:
    raise SystemExit(f'old layout test matches={test_source.count(old_test)}')
test_source = test_source.replace(old_test, new_test, 1)

old_two = '''    @Test public void generatedScriptShowsTwoParticipantsAndReusesPersonManagementActions(){
        String script=InternalAttendanceSkin.javascript("#11171C","#1A2228","#232E36","#ECF1F4","#A0B0BA","#344550","#5BBED5");
        assertTrue(script.contains("calc((100% - 6px)/2)"));
        assertTrue(script.contains("personManagementControls"));
'''
new_two = '''    @Test public void generatedScriptShowsTwoParticipantsAndReusesPersonManagementActions(){
        String script=InternalAttendanceSkin.javascript("#11171C","#1A2228","#232E36","#ECF1F4","#A0B0BA","#344550","#5BBED5");
        assertTrue(script.contains("--pfvr-person-col:clamp(104px,calc((100vw - 128px)/2),138px)"));
        assertTrue(script.contains("--pfvr-day-col:84px;--pfvr-person-col:102px"));
        assertTrue(script.contains("personManagementControls"));
'''
if test_source.count(old_two) != 1:
    raise SystemExit(f'old two-person test matches={test_source.count(old_two)}')
test_source = test_source.replace(old_two, new_two, 1)

old_scroll_asserts = '''        assertTrue(script.contains("window.scrollTo"));
        assertTrue(script.contains("scrollLeft"));
        assertFalse(script.contains("window.location.reload"));
'''
new_scroll_asserts = '''        assertTrue(script.contains("window.scrollTo"));
        assertTrue(script.contains("matrixScroll.scrollLeft=state.x||0"));
        assertTrue(script.contains("x:matrixScroll?(matrixScroll.scrollLeft||0):0"));
        assertFalse(script.contains("strips:strips"));
        assertFalse(script.contains("window.location.reload"));
'''
if test_source.count(old_scroll_asserts) != 1:
    raise SystemExit(f'old scroll assertions matches={test_source.count(old_scroll_asserts)}')
test_source = test_source.replace(old_scroll_asserts, new_scroll_asserts, 1)
test.write_text(test_source, encoding='utf-8')

change_source = changelog.read_text(encoding='utf-8')
old_change = '- Interne Terminansicht kompakter: Termin-Metadaten schmaler, Teilnehmerkarten so dimensioniert, dass mindestens zwei Personen gleichzeitig im rechten Bereich sichtbar sind.\n'
new_change = '- Interne Terminansicht als gemeinsam horizontal scrollende Matrix: Termine/Kochinfo links, jede Person als feste Spalte über alle Tage. Mindestens zwei Personenspalten sind gleichzeitig sichtbar.\n'
if old_change in change_source:
    change_source = change_source.replace(old_change, new_change, 1)
changelog.write_text(change_source, encoding='utf-8')

status_source = status.read_text(encoding='utf-8')
old_status = '- Die mobile interne Terminansicht zeigt mindestens zwei kompakte Teilnehmerkarten gleichzeitig; Termin-Metadaten beanspruchen dafür weniger Breite. Unter `+ / − Person` werden vorhandene Website-Aktionen aus der Personen-Spalte wieder sichtbar, sodass zusätzliche Personen über die serverseitig vorhandene Funktion entfernt werden können.\n'
new_status = '- Die mobile interne Terminansicht verwendet eine gemeinsam horizontal scrollende Matrix: Termin-/Kochinfo bleibt links sticky, jede Person ist über alle Tage dieselbe feste Spalte und mindestens zwei Personenspalten sind gleichzeitig sichtbar. Unter `+ / − Person` werden vorhandene Website-Aktionen aus der Personen-Spalte wieder sichtbar, sodass zusätzliche Personen über die serverseitig vorhandene Funktion entfernt werden können.\n'
if old_status in status_source:
    status_source = status_source.replace(old_status, new_status, 1)
status.write_text(status_source, encoding='utf-8')
