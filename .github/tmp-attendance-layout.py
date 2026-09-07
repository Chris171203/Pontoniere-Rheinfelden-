from pathlib import Path


def replace_once(text, old, new, label):
    count = text.count(old)
    if count != 1:
        raise SystemExit(f"{label}: expected 1 occurrence, got {count}")
    return text.replace(old, new, 1)


path = Path("Android/app/src/main/java/ch/pfvr/internapp/InternalAttendanceSkin.java")
text = path.read_text()

# Keep the proven two-column/-webkit-line-clamp layout. Only add explicit newline
# support; all person headers keep the same 12 px size (11 px on <=340 dp).
old = """                  .pfvr-person-header{padding:8px 7px!important;font-size:12px!important;font-weight:700!important;line-height:1.15!important;overflow-wrap:break-word!important;word-break:normal!important;display:-webkit-box!important;-webkit-box-orient:vertical!important;-webkit-line-clamp:2!important;overflow:hidden!important;min-height:40px!important;box-shadow:0 3px 8px rgba(0,0,0,.10)!important;}"""
new = """                  .pfvr-person-header{padding:8px 7px!important;font-size:12px!important;font-weight:700!important;line-height:1.15!important;white-space:pre-line!important;overflow-wrap:break-word!important;word-break:normal!important;display:-webkit-box!important;-webkit-box-orient:vertical!important;-webkit-line-clamp:2!important;overflow:hidden!important;min-height:40px!important;box-shadow:0 3px 8px rgba(0,0,0,.10)!important;}"""
text = replace_once(text, old, new, "uniform person header CSS")

old = """                  .pfvr-person-control button,.pfvr-person-control input[type=submit],.pfvr-person-control input[type=button],.pfvr-person-control a.btn,.pfvr-person-control .btn,.pfvr-person-control select{width:100%!important;min-height:60px!important;padding:10px 8px!important;font-size:13px!important;border-radius:10px!important;}"""
new = """                  .pfvr-person-control button,.pfvr-person-control input[type=submit],.pfvr-person-control input[type=button],.pfvr-person-control a.btn,.pfvr-person-control .btn,.pfvr-person-control select{width:100%!important;min-height:60px!important;padding:10px 8px!important;font-size:13px!important;border-radius:10px!important;white-space:pre-line!important;text-align:center!important;}"""
text = replace_once(text, old, new, "attendance control pre-line CSS")

old = """                  var styleInteractive=function(root){
                    (root||document).querySelectorAll('button,input[type=submit],input[type=button],a.btn,.btn,select').forEach(function(el){
                      if(el.closest&&el.closest('.pfvr-person-tools'))return;
                      var matched=statusForValue(controlValue(el));
                      if(matched){paint(el,matched.background,matched.foreground);return;}"""
new = """                  var formatAttendanceChoiceLabel=function(el,matched){
                    if(!el||!matched||el.tagName==='SELECT')return;
                    if(matched!==statusDefs[0]&&matched!==statusDefs[1])return;
                    var label=controlValue(el);
                    if(norm(label).indexOf('komme')<0)return;
                    var formatted=label.replace(/,\\s*(mit\\s+essen|ohne\\s+essen)/i,',\\n$1');
                    if(formatted===label)return;
                    if(el.tagName==='INPUT')el.value=formatted;
                    else el.textContent=formatted;
                  };
                  var styleInteractive=function(root){
                    (root||document).querySelectorAll('button,input[type=submit],input[type=button],a.btn,.btn,select').forEach(function(el){
                      if(el.closest&&el.closest('.pfvr-person-tools'))return;
                      var matched=statusForValue(controlValue(el));
                      if(matched){formatAttendanceChoiceLabel(el,matched);paint(el,matched.background,matched.foreground);return;}"""
text = replace_once(text, old, new, "attendance choice line break")

old = """                  var fitPersonName=function(el,value){
                    var clean=formatPersonName(value);
                    el.textContent=clean;
                    el.title=clean;
                    if(clean.length>28)el.classList.add('pfvr-name-tiny');
                    else if(clean.length>19)el.classList.add('pfvr-name-small');
                  };"""
new = """                  var fitPersonName=function(el,value){
                    var clean=formatPersonName(value);
                    el.title=clean;
                    if(el.classList)el.classList.remove('pfvr-name-small','pfvr-name-tiny');
                    if(el.classList&&el.classList.contains('pfvr-person-header')){
                      var comma=clean.indexOf(',');
                      if(comma>=0){
                        var family=clean.slice(0,comma).trim(),given=clean.slice(comma+1).trim();
                        el.textContent=given?family+',\\n'+given:family;
                      }else el.textContent=clean;
                      return;
                    }
                    el.textContent=clean;
                    if(clean.length>28)el.classList.add('pfvr-name-tiny');
                    else if(clean.length>19)el.classList.add('pfvr-name-small');
                  };"""
text = replace_once(text, old, new, "uniform person name sizing")
path.write_text(text)


test_path = Path("Android/app/src/test/java/ch/pfvr/internapp/InternalAttendanceUiRegressionTest.java")
test = test_path.read_text()
anchor = """    @Test public void recoveryConfirmationAlwaysReturnsToItsNeutralState(){
        String script=script();
        assertTrue(script.contains("resetRecoveryConfirm"));
        assertTrue(script.contains("pfvrConfirmTimer"));
        assertTrue(script.contains("panel.addEventListener('click'"));
        assertTrue(script.contains("Aus Initiallink neu aufbauen"));
    }
"""
addition = anchor + """
    @Test public void personHeadersUseUniformTwoLineNamesAndFoodButtonsBreakAfterComma(){
        String script=script();
        assertTrue(script.contains(".pfvr-person-header{padding:8px 7px!important;font-size:12px!important"));
        assertTrue(script.contains("white-space:pre-line!important"));
        assertTrue(script.contains("-webkit-line-clamp:2"));
        assertTrue(script.contains("el.classList.remove('pfvr-name-small','pfvr-name-tiny')"));
        assertTrue(script.contains("el.classList.contains('pfvr-person-header')"));
        assertTrue(script.contains("formatAttendanceChoiceLabel"));
        assertTrue(script.contains("formatted=label.replace"));
        assertTrue(script.contains("matched!==statusDefs[0]&&matched!==statusDefs[1]"));
    }
"""
test = replace_once(test, anchor, addition, "attendance UI regression test")
test_path.write_text(test)


gradle_path = Path("Android/app/build.gradle")
gradle = gradle_path.read_text()
gradle = replace_once(gradle, "versionCode 54", "versionCode 55", "version code")
gradle = replace_once(gradle, "versionName '0.11.7'", "versionName '0.11.8'", "version name")
gradle_path.write_text(gradle)


status_path = Path("STATUS.md")
status = status_path.read_text()
status = replace_once(status, "Stand: Testversion `0.11.7`", "Stand: Testversion `0.11.8`", "status version")
anchor = "## Aktueller Teststand\n\n"
addition = "- `0.11.8` glättet die mobile An-/Abmeldung: Personenspalten verwenden in der Kopfzeile eine einheitliche Schriftgrösse und brechen Namen bevorzugt nach dem Komma auf zwei Zeilen um. Die Statusschaltflächen `Ich komme, mit Essen` und `Ich komme, ohne Essen` erhalten einen festen Umbruch direkt nach dem Komma; `Ich komme nicht` bleibt bewusst einzeilig.\n"
if anchor not in status:
    raise SystemExit("STATUS anchor missing")
status = status.replace(anchor, anchor + addition, 1)
status_path.write_text(status)
