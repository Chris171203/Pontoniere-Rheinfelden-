from pathlib import Path
import re


def once(text, old, new, label):
    count=text.count(old)
    if count!=1:
        raise RuntimeError(f'{label}: expected 1 match, got {count}')
    return text.replace(old,new,1)

root=Path('.')
main_path=root/'Android/app/src/main/java/ch/pfvr/internapp/MainActivity.java'
skin_path=root/'Android/app/src/main/java/ch/pfvr/internapp/InternalAttendanceSkin.java'
test_path=root/'Android/app/src/test/java/ch/pfvr/internapp/InternalAttendanceSkinTest.java'
build_path=root/'Android/app/build.gradle'
readme_path=root/'Android/README.md'
changelog_path=root/'Android/CHANGELOG.md'
status_path=root/'STATUS.md'

main=main_path.read_text(encoding='utf-8')
main=once(main,
    'if (headerBack != null) headerBack.setVisibility(screen == Screen.HOME ? View.GONE : View.VISIBLE);',
    'if (headerBack != null) headerBack.setVisibility((screen == Screen.HOME || screen == Screen.INTERNAL) ? View.GONE : View.VISIBLE);',
    'hide shell back in internal')
main=once(main,
    'private void handleBack(){if(activeWebView!=null&&activeWebView.canGoBack())activeWebView.goBack();else if(current==Screen.TILE_SETTINGS)navigate(Screen.SETTINGS);else if(current!=Screen.HOME)navigate(Screen.HOME);else super.onBackPressed();}',
    'private void handleBack(){if(current==Screen.INTERNAL){navigate(Screen.HOME);return;}if(activeWebView!=null&&activeWebView.canGoBack())activeWebView.goBack();else if(current==Screen.TILE_SETTINGS)navigate(Screen.SETTINGS);else if(current!=Screen.HOME)navigate(Screen.HOME);else super.onBackPressed();}',
    'internal android back')
old_toolbar='''        Button people=btn(appView?"Personen":"‹ Zurück",Color.WHITE,NAVY);\n        people.setContentDescription(appView?"Personen hinzufügen oder entfernen":"Zurück");\n        people.setOnClickListener(v->{if(prefs.getBoolean(PREF_INTERNAL_APP_VIEW,true))openInternalPeopleManager(web,0);else handleBack();});\n        tools.addView(people,new LinearLayout.LayoutParams(0,dp(40),1));\n        Button mode=btn(appView?"Original":"App-Ansicht",NAVY,Color.WHITE);\n        mode.setOnClickListener(v->{boolean next=!prefs.getBoolean(PREF_INTERNAL_APP_VIEW,true);prefs.edit().putBoolean(PREF_INTERNAL_APP_VIEW,next).apply();mode.setText(next?"Original":"App-Ansicht");people.setText(next?"Personen":"‹ Zurück");people.setContentDescription(next?"Personen hinzufügen oder entfernen":"Zurück");web.clearCache(false);web.reload();});\n'''
new_toolbar='''        Button people=btn("Personen",Color.WHITE,NAVY);\n        people.setContentDescription("Personen hinzufügen oder entfernen");\n        people.setVisibility(appView?View.VISIBLE:View.GONE);\n        people.setOnClickListener(v->openInternalPeopleManager(web,0));\n        tools.addView(people,new LinearLayout.LayoutParams(0,dp(40),1));\n        Button mode=btn(appView?"Original":"App-Ansicht",NAVY,Color.WHITE);\n        mode.setOnClickListener(v->{boolean next=!prefs.getBoolean(PREF_INTERNAL_APP_VIEW,true);prefs.edit().putBoolean(PREF_INTERNAL_APP_VIEW,next).apply();mode.setText(next?"Original":"App-Ansicht");people.setVisibility(next?View.VISIBLE:View.GONE);web.clearCache(false);web.reload();});\n'''
main=once(main,old_toolbar,new_toolbar,'internal toolbar')
main=once(main,
    'view.evaluateJavascript(InternalAttendanceSkin.javascript(background,card,soft,text,muted,border,link),null);',
    'String baseInternalUrl=normalizeInternalUrl(prefs.getString(PREF_INTERNAL_URL,""));\n    view.evaluateJavascript("window.__pfvrBaseInternalUrl="+JSONObject.quote(baseInternalUrl)+";"+InternalAttendanceSkin.javascript(background,card,soft,text,muted,border,link),null);',
    'inject base internal url')
main_path.write_text(main,encoding='utf-8')

skin=skin_path.read_text(encoding='utf-8')
skin=once(skin,
    "var PEOPLE_KEY='pfvr-attendance-people-v3';\n                  var LEGACY_PEOPLE_KEY='pfvr-attendance-people-v2';\n                  var RESTORE_KEY='pfvr-attendance-restore-v3';",
    "var PEOPLE_KEY='pfvr-attendance-people-v4';\n                  var LEGACY_PEOPLE_KEY='pfvr-attendance-people-v3';\n                  var RESTORE_KEY='pfvr-attendance-restore-v4';",
    'storage version')
old_remove='''                  var removeDesiredPerson=function(state,name){\n                    var clean=cleanPersonName(name);if(!state||!clean||samePersonName(clean,state.primary))return false;\n                    state.desired=(state.desired||[]).filter(function(saved){return !samePersonName(saved,clean);});\n                    if(!listHasPerson(state.hidden,clean))state.hidden.push(clean);\n                    Object.keys(state.restoreValues||{}).forEach(function(key){if(samePersonName(key,clean))delete state.restoreValues[key];});\n                    savePeopleState(state);return true;\n                  };\n'''
new_remove='''                  var removeDesiredPerson=function(state,name){\n                    var clean=cleanPersonName(name);if(!state||!clean||samePersonName(clean,state.primary))return false;\n                    state.desired=(state.desired||[]).filter(function(saved){return !samePersonName(saved,clean);});\n                    if(!listHasPerson(state.hidden,clean))state.hidden.push(clean);\n                    state.rowNames=[];state.pendingAdd=null;\n                    Object.keys(state.restoreValues||{}).forEach(function(key){if(samePersonName(key,clean))delete state.restoreValues[key];});\n                    savePeopleState(state);return true;\n                  };\n'''
skin=once(skin,old_remove,new_remove,'remove state reset')
old_load='''                    if(!state||!Array.isArray(state.desired))state={version:3,primary:currentNames[0]||'',desired:[],hidden:[],restoreValues:{},pendingAdd:null,rowNames:[]};\n                    state.version=3;if(!Array.isArray(state.hidden))state.hidden=[];if(!Array.isArray(state.rowNames))state.rowNames=[];if(!state.restoreValues||typeof state.restoreValues!=='object')state.restoreValues={};\n'''
new_load='''                    var migrated=!!(state&&state.version!==4);\n                    if(!state||!Array.isArray(state.desired))state={version:4,primary:currentNames[0]||'',desired:[],hidden:[],restoreValues:{},pendingAdd:null,rowNames:[]};\n                    state.version=4;if(migrated)state.hidden=[];if(!Array.isArray(state.hidden))state.hidden=[];if(!Array.isArray(state.rowNames))state.rowNames=[];if(!state.restoreValues||typeof state.restoreValues!=='object')state.restoreValues={};\n'''
skin=once(skin,old_load,new_load,'state migration')
old_remove_listener="remove.addEventListener('click',function(){if(!removeDesiredPerson(state,personName))return;setPersonColumnHidden(personName,true);appendHiddenPerson(document.querySelector('.pfvr-hidden-people'),state,personName);line.remove();});"
new_remove_listener="remove.addEventListener('click',function(){if(!removeDesiredPerson(state,personName))return;saveViewState();closePersonManager();var base=window.__pfvrBaseInternalUrl||'';if(base){window.location.replace(base);return;}setPersonColumnHidden(personName,true);line.remove();});"
skin=once(skin,old_remove_listener,new_remove_listener,'automatic server list rebuild')
old_manager='''                    var note=element('div');note.textContent='Entfernen blendet die Person nur in der App-Ansicht aus.';note.style.color=COLORS.muted;note.style.fontSize='11px';body.appendChild(note);\n                    var list=element('div','pfvr-managed-people pfvr-managed-people-visible');\n                    var listTitle=element('div','pfvr-managed-people-title');listTitle.textContent='Angezeigte Personen';list.appendChild(listTitle);\n                    (currentNames||state.desired).forEach(function(personName){appendManagedPerson(list,state,personName);});body.appendChild(list);\n                    var hiddenList=element('div','pfvr-managed-people pfvr-hidden-people');\n                    var hiddenTitle=element('div','pfvr-managed-people-title');hiddenTitle.textContent='Ausgeblendet';hiddenList.appendChild(hiddenTitle);\n                    (state.hidden||[]).forEach(function(personName){appendHiddenPerson(hiddenList,state,personName);});body.appendChild(hiddenList);panel.appendChild(body);\n'''
new_manager='''                    var note=element('div');note.textContent='Entfernen aktualisiert die Personenliste automatisch.';note.style.color=COLORS.muted;note.style.fontSize='11px';body.appendChild(note);\n                    var list=element('div','pfvr-managed-people pfvr-managed-people-visible');\n                    var listTitle=element('div','pfvr-managed-people-title');listTitle.textContent='Aktuelle Personen';list.appendChild(listTitle);\n                    (currentNames||state.desired).forEach(function(personName){appendManagedPerson(list,state,personName);});body.appendChild(list);panel.appendChild(body);\n'''
skin=once(skin,old_manager,new_manager,'person manager semantics')
skin_path.write_text(skin,encoding='utf-8')

build=build_path.read_text(encoding='utf-8')
build=once(build,'versionCode 32','versionCode 33','version code')
build=once(build,"versionName '0.10.8'","versionName '0.10.9'",'version name')
build_path.write_text(build,encoding='utf-8')

test=test_path.read_text(encoding='utf-8')
test=test.replace('pfvr-attendance-people-v3','pfvr-attendance-people-v4')
test=once(test,
    'assertTrue(script.contains("Einblenden"));',
    'assertTrue(script.contains("window.location.replace(base)"));',
    'server removal assertion')
test=once(test,
    'assertTrue(script.contains("Entfernen blendet die Person nur in der App-Ansicht aus."));',
    'assertTrue(script.contains("Entfernen aktualisiert die Personenliste automatisch."));\n        assertTrue(script.contains("Aktuelle Personen"));\n        assertFalse(script.contains("Ausgeblendet"));',
    'manager semantics test')
marker='''    @Test public void generatedScriptKeepsStatusRepairAndMobileViewport(){\n'''
extra='''    @Test public void generatedScriptRebuildsWebsiteListAfterRemoval(){\n        String script=InternalAttendanceSkin.javascript("#11171C","#1A2228","#232E36","#ECF1F4","#A0B0BA","#344550","#5BBED5");\n        assertTrue(script.contains("state.rowNames=[];state.pendingAdd=null"));\n        assertTrue(script.contains("window.__pfvrBaseInternalUrl"));\n        assertTrue(script.contains("window.location.replace(base)"));\n        assertTrue(script.contains("removeDesiredPerson"));\n        assertFalse(script.contains("Entfernen blendet die Person nur"));\n    }\n\n'''
test=once(test,marker,extra+marker,'add removal regression test')
test_path.write_text(test,encoding='utf-8')

readme=readme_path.read_text(encoding='utf-8')
readme=readme.replace('Aktuelle Android-Testversion: `0.10.8`.','Aktuelle Android-Testversion: `0.10.9`.')
readme=readme.replace('## Gerätetest 0.10.8','## Gerätetest 0.10.9')
readme=readme.replace('0.10.8 verwendet `versionCode 32`','0.10.9 verwendet `versionCode 33`')
readme=readme.replace('In der Personenverwaltung eine Zusatzperson lokal entfernen: Sie muss sofort aus der Matrix verschwinden und nach Neustart ausgeblendet bleiben. Über `Einblenden` muss sie zurückkehren; die Standardperson darf nicht entfernbar sein.',
                      'In der Personenverwaltung eine Zusatzperson entfernen: Die App lädt automatisch den persönlichen Basislink neu und stellt danach nur die verbleibenden Zusatzpersonen über die echten Website-Controls wieder her. Kein manueller Neu-laden-Schritt darf nötig sein; die Standardperson darf nicht entfernbar sein.')
readme=readme.replace('In der App-Ansicht öffnet der linke Werkzeugleistenbutton `Personen` eine modale Verwaltung zum Hinzufügen, lokalen Ausblenden und Wieder-Einblenden. Der normale Zurückweg bleibt über den App-Header und Android-Zurück erhalten.',
                      'In der App-Ansicht öffnet der Werkzeugleistenbutton `Personen` die Verwaltung zum Hinzufügen und Entfernen. Der Zurückpfeil neben dem Logo ist im internen Bereich entfernt; Navigation erfolgt über die untere Leiste, und Android-Zurück führt von dort direkt zu Home statt durch die WebView-Historie.')
readme_path.write_text(readme,encoding='utf-8')

changelog=changelog_path.read_text(encoding='utf-8')
entry='''## 0.10.9\n- `Entfernen` ist keine reine lokale Ausblendung mehr: Die App setzt die interne Seite automatisch auf den persönlichen Basislink zurück und stellt anschließend nur die verbleibenden Zusatzpersonen über die echten Website-Controls wieder her.\n- Dadurch wird die entfernte Person auch aus der Originalansicht genommen und der fehlerhafte Grid-Zwischenzustand nach lokalem Ausblenden vermieden; kein manueller Reload ist nötig.\n- Der Zurückpfeil neben dem Logo ist im internen Bereich entfernt. Android-Zurück navigiert dort direkt zu Home und kann nicht mehr durch WebView-Historie eine Personenänderung rückgängig machen.\n- Im Originalmodus entfällt zusätzlich der redundante innere Zurück-Button; `Personen` ist ausschließlich in der App-Ansicht sichtbar.\n\n'''
changelog=once(changelog,'# Android Changelog\n\n','# Android Changelog\n\n'+entry,'changelog entry')
changelog_path.write_text(changelog,encoding='utf-8')

status=status_path.read_text(encoding='utf-8')
status=status.replace('Stand: Testversion `0.10.8` · aktualisiert 2026-09-04.','Stand: Testversion `0.10.9` · aktualisiert 2026-09-04.')
status=status.replace('Lokales Entfernen blendet eine Personenspalte aus, ohne die echten An-/Abmelde-Controls aus dem DOM oder Daten auf dem Server zu löschen. Ausgeblendete Personen können in derselben Verwaltung wieder eingeblendet werden.',
                      'Entfernen baut die Website-Personenliste automatisch vom persönlichen Basislink neu auf und stellt nur die verbleibenden Zusatzpersonen wieder her. Dadurch verschwindet die Person auch aus der Originalansicht, ohne dass der Benutzer manuell neu laden muss.')
status=status.replace('In der App-Ansicht ersetzt der linke Werkzeugleistenbutton `Personen` den redundanten Zurück-Button. Er öffnet eine modale Verwaltung zum Hinzufügen, lokalen Ausblenden und Wieder-Einblenden. Der normale Zurückweg bleibt über den App-Header bzw. Android-Zurück erhalten.',
                      'In der App-Ansicht öffnet `Personen` die Verwaltung zum Hinzufügen und Entfernen. Der Zurückpfeil neben dem Logo wird im internen Bereich nicht angezeigt; Android-Zurück führt direkt zu Home, damit WebView-Historie keine Personenänderung rückgängig macht.')
status_path.write_text(status,encoding='utf-8')

print('Applied 0.10.9 person-removal and internal-navigation fix')
