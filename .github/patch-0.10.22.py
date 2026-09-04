from pathlib import Path
import sys


def replace_once(text, old, new, label):
    if old not in text:
        raise SystemExit(f'{label} not found')
    return text.replace(old, new, 1)


def update_status():
    p = Path('STATUS.md')
    s = p.read_text(encoding='utf-8')
    s = s.replace('Stand: Testversion `0.10.21`', 'Stand: Testversion `0.10.22`', 1)
    anchor = '- Lange Koch-/Verantwortlichkeitsnamen in der linken Terminspalte werden nach dem Rendern anhand der tatsächlich verfügbaren Breite verkleinert. Datum, Zähler, Statusbadges und normaler Termintext werden davon nicht verändert.\n'
    addition = anchor + '- Koch-/Verantwortlichkeitsnamen werden in der App-Matrix ausschließlich als nicht editierbarer Anzeigetext dargestellt; zugrunde liegende Website-Textfelder bleiben unsichtbar für die Formularfunktion erhalten. Lange Namen werden bis zu einer definierten Mindestschriftgröße verkleinert, und das Datum in der Terminspalte wird fett hervorgehoben.\n'
    s = replace_once(s, anchor, addition, 'STATUS anchor')
    p.write_text(s, encoding='utf-8')


def update_source():
    main = Path('Android/app/src/main/java/ch/pfvr/internapp/InternalAttendanceSkin.java')
    s = main.read_text(encoding='utf-8')

    old_css = '''                  .pfvr-day-meta>*{max-width:100%!important;box-sizing:border-box!important;white-space:normal!important;overflow-wrap:break-word!important;word-break:normal!important;}
                  .pfvr-day-fit-name{display:block!important;max-width:100%!important;white-space:nowrap!important;overflow:hidden!important;text-overflow:clip!important;line-height:1.1!important;}
'''
    new_css = '''                  .pfvr-day-meta>*{max-width:100%!important;box-sizing:border-box!important;white-space:normal!important;overflow-wrap:break-word!important;word-break:normal!important;}
                  .pfvr-day-source-input{display:none!important;}
                  .pfvr-day-date{display:block!important;font-weight:700!important;line-height:1.2!important;margin-bottom:1px!important;}
                  .pfvr-day-display-value{display:block!important;max-width:100%!important;font-size:18px!important;font-weight:400!important;white-space:nowrap!important;overflow:hidden!important;text-overflow:clip!important;line-height:1.1!important;margin:5px 0 3px!important;}
                  .pfvr-day-fit-name{display:block!important;max-width:100%!important;white-space:nowrap!important;overflow:hidden!important;text-overflow:clip!important;line-height:1.1!important;}
'''
    s = replace_once(s, old_css, new_css, 'day meta css')

    anchor = '''                  var fitDayMetaTexts=function(root){
                    (root||document).querySelectorAll('.pfvr-day-meta').forEach(function(meta){
                      var candidates=Array.from(meta.querySelectorAll('button,input[type=submit],input[type=button],a,.btn'));
'''
    replacement = r'''                  var decorateDayMeta=function(meta){
                    if(!meta)return;
                    Array.from(meta.querySelectorAll('input[type=text],input:not([type])')).forEach(function(input){
                      if(input.classList.contains('pfvr-day-source-input'))return;
                      var value=((input.value||input.getAttribute('value')||'')+'').replace(/\s+/g,' ').trim();
                      if(!value)return;
                      if(document.activeElement===input&&input.blur)input.blur();
                      var visual=element('span','pfvr-day-display-value');
                      visual.textContent=value;
                      visual.setAttribute('aria-label',value);
                      input.classList.add('pfvr-day-source-input');
                      input.readOnly=true;
                      input.setAttribute('tabindex','-1');
                      input.setAttribute('aria-hidden','true');
                      input.setAttribute('inputmode','none');
                      input.insertAdjacentElement('afterend',visual);
                    });
                    var datePattern=/^(?:mo|di|mi|do|fr|sa|so)\.?[,]?\s*\d{1,2}\.\d{1,2}\.?$/i;
                    Array.from(meta.childNodes).forEach(function(node){
                      if(node.nodeType===3){
                        var raw=(node.textContent||'').replace(/\s+/g,' ').trim();
                        if(!datePattern.test(raw))return;
                        var date=element('span','pfvr-day-date');
                        date.textContent=raw;
                        node.parentNode.replaceChild(date,node);
                        return;
                      }
                      if(node.nodeType!==1||node.classList.contains('pfvr-day-date')||node.classList.contains('pfvr-day-display-value'))return;
                      var raw=text(node).replace(/\s+/g,' ').trim();
                      if(datePattern.test(raw)&&node.children.length===0)node.classList.add('pfvr-day-date');
                    });
                  };

                  var fitDayMetaTexts=function(root){
                    (root||document).querySelectorAll('.pfvr-day-meta').forEach(function(meta){
                      decorateDayMeta(meta);
                      var candidates=Array.from(meta.querySelectorAll('button,input[type=submit],input[type=button],a,.btn,.pfvr-day-display-value'));
'''
    s = replace_once(s, anchor, replacement, 'fit day meta anchor')
    main.write_text(s, encoding='utf-8')

    test = Path('Android/app/src/test/java/ch/pfvr/internapp/InternalAttendanceSkinTest.java')
    t = test.read_text(encoding='utf-8')
    test_anchor = '''    @Test public void generatedScriptShrinksLongCookNamesOnlyInDayMeta(){
        String script=script();
'''
    new_test = '''    @Test public void generatedScriptProjectsDayTextInputsAsLabelsAndBoldsDate(){
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
'''
    t = replace_once(t, test_anchor, new_test, 'test anchor')
    t = t.replace('        assertTrue(script.contains("fitDayMetaTexts"));\n', '        assertTrue(script.contains("fitDayMetaTexts"));\n        assertTrue(script.contains("decorateDayMeta(meta)"));\n', 1)
    test.write_text(t, encoding='utf-8')

    gradle = Path('Android/app/build.gradle')
    g = gradle.read_text(encoding='utf-8')
    g = g.replace('versionCode 45', 'versionCode 46', 1).replace("versionName '0.10.21'", "versionName '0.10.22'", 1)
    gradle.write_text(g, encoding='utf-8')

    changelog = Path('Android/CHANGELOG.md')
    c = changelog.read_text(encoding='utf-8')
    c = c.replace('# Android Changelog\n', '# Android Changelog\n\n## 0.10.22\n- Koch-/Verantwortlichkeitsnamen in der linken Terminspalte sind in der App-Ansicht keine bedienbaren Textfelder mehr. Die Originalfelder bleiben unsichtbar im DOM erhalten, während ein reiner Anzeigetext gerendert wird.\n- Lange Namen werden im verfügbaren Platz automatisch kleiner skaliert; ein Antippen öffnet keine Tastatur mehr.\n- Datumsangaben in der Terminspalte werden fett hervorgehoben.\n', 1)
    changelog.write_text(c, encoding='utf-8')

    readme = Path('Android/README.md')
    r = readme.read_text(encoding='utf-8')
    r = r.replace('Aktuelle Android-Testversion: `0.10.21` (`versionCode 45`).', 'Aktuelle Android-Testversion: `0.10.22` (`versionCode 46`).', 1)
    r = r.replace('## Gerätetest 0.10.21', '## Gerätetest 0.10.22', 1)
    marker = '- Lange Koch-/Verantwortlichkeitsnamen müssen sich in der Terminspalte verkleinern, ohne Datum oder übrigen Termintext mitzuskaliert.\n'
    repl = '- Koch-/Verantwortlichkeitsnamen in der Terminspalte dürfen nicht fokussierbar oder editierbar sein und beim Antippen keine Tastatur öffnen. Lange Namen müssen sich verkleinern; das Datum soll fett bleiben, ohne Zähler oder Termintext mitzuskaliert.\n'
    if marker in r:
        r = r.replace(marker, repl, 1)
    readme.write_text(r, encoding='utf-8')

    root = Path('README.md')
    rr = root.read_text(encoding='utf-8').replace('Android-Testversion `0.10.21` auf `main`.', 'Android-Testversion `0.10.22` auf `main`.', 1)
    root.write_text(rr, encoding='utf-8')


mode = sys.argv[1] if len(sys.argv) > 1 else ''
if mode == 'status':
    update_status()
elif mode == 'source':
    update_source()
else:
    raise SystemExit('usage: patch-0.10.22.py status|source')
