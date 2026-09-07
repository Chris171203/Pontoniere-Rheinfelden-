from pathlib import Path


def replace_once(text, old, new, label):
    count = text.count(old)
    if count != 1:
        raise SystemExit(f"{label}: expected 1 occurrence, got {count}")
    return text.replace(old, new, 1)


skin_path = Path("Android/app/src/main/java/ch/pfvr/internapp/InternalAttendanceSkin.java")
skin = skin_path.read_text()

# 0.11.8 emitted JavaScript with a literal line break inside a single-quoted JS
# string. Java compiles that, but the injected JavaScript then fails to parse.
# Keep the escape as two characters (backslash+n) in the generated JS source.
skin = replace_once(
    skin,
    r"el.textContent=given?family+',\n'+given:family;",
    r"el.textContent=given?family+',\\n'+given:family;",
    "person-header JS newline escaping",
)

old_formatter = r"""                  var formatAttendanceChoiceLabel=function(el,matched){
                    if(!el||!matched||el.tagName==='SELECT')return;
                    if(matched!==statusDefs[0]&&matched!==statusDefs[1])return;
                    var label=controlValue(el);
                    if(norm(label).indexOf('komme')<0)return;
                    var formatted=label.replace(/,\s*(mit\s+essen|ohne\s+essen)/i,',\n$1');
                    if(formatted===label)return;
                    if(el.tagName==='INPUT')el.value=formatted;
                    else el.textContent=formatted;
                  };"""
new_formatter = r"""                  var formatAttendanceChoiceLabel=function(el,matched){
                    if(!el||!matched||el.tagName==='SELECT')return;
                    if(matched!==statusDefs[0]&&matched!==statusDefs[1])return;
                    var label=controlValue(el);
                    if(norm(label).indexOf('komme')<0)return;
                    var formatted=label.replace(/,\s*(mit\s+essen|ohne\s+essen)/i,',\\n$1');
                    if(formatted===label)return;
                    if(el.tagName==='BUTTON'||el.tagName==='A'||(el.classList&&el.classList.contains('btn'))){
                      el.classList.add('pfvr-attendance-display-label');
                      el.setAttribute('data-pfvr-display-label',formatted);
                    }
                  };"""
skin = replace_once(skin, old_formatter, new_formatter, "safe attendance label formatter")

css_anchor = """                  .pfvr-person-control button,.pfvr-person-control input[type=submit],.pfvr-person-control input[type=button],.pfvr-person-control a.btn,.pfvr-person-control .btn,.pfvr-person-control select{width:100%!important;min-height:60px!important;padding:10px 8px!important;font-size:13px!important;border-radius:10px!important;white-space:pre-line!important;text-align:center!important;}"""
css_replacement = css_anchor + """
                  .pfvr-person-control button.pfvr-attendance-display-label,.pfvr-person-control a.pfvr-attendance-display-label,.pfvr-person-control .btn.pfvr-attendance-display-label{font-size:0!important;}
                  .pfvr-person-control button.pfvr-attendance-display-label::after,.pfvr-person-control a.pfvr-attendance-display-label::after,.pfvr-person-control .btn.pfvr-attendance-display-label::after{content:attr(data-pfvr-display-label)!important;white-space:pre-line!important;font-size:13px!important;font-weight:700!important;line-height:1.25!important;color:inherit!important;}"""
skin = replace_once(skin, css_anchor, css_replacement, "non-destructive attendance label CSS")

small_anchor = """                    .pfvr-person-control button,.pfvr-person-control input[type=submit],.pfvr-person-control input[type=button],.pfvr-person-control a.btn,.pfvr-person-control .btn,.pfvr-person-control select{min-height:54px!important;font-size:12px!important;padding:8px 5px!important;}"""
small_replacement = small_anchor + """
                    .pfvr-person-control button.pfvr-attendance-display-label::after,.pfvr-person-control a.pfvr-attendance-display-label::after,.pfvr-person-control .btn.pfvr-attendance-display-label::after{font-size:12px!important;}"""
skin = replace_once(skin, small_anchor, small_replacement, "small-screen attendance pseudo label size")
skin_path.write_text(skin)


test_path = Path("Android/app/src/test/java/ch/pfvr/internapp/InternalAttendanceUiRegressionTest.java")
test = test_path.read_text()
test = replace_once(
    test,
    "import static org.junit.Assert.assertTrue;",
    "import static org.junit.Assert.assertFalse;\nimport static org.junit.Assert.assertTrue;",
    "assertFalse import",
)
old_test = """    @Test public void personHeadersUseUniformTwoLineNamesAndFoodButtonsBreakAfterComma(){
        String script=script();
        assertTrue(script.contains(\".pfvr-person-header{padding:8px 7px!important;font-size:12px!important\"));
        assertTrue(script.contains(\"white-space:pre-line!important\"));
        assertTrue(script.contains(\"-webkit-line-clamp:2\"));
        assertTrue(script.contains(\"el.classList.remove('pfvr-name-small','pfvr-name-tiny')\"));
        assertTrue(script.contains(\"el.classList.contains('pfvr-person-header')\"));
        assertTrue(script.contains(\"formatAttendanceChoiceLabel\"));
        assertTrue(script.contains(\"formatted=label.replace\"));
        assertTrue(script.contains(\"matched!==statusDefs[0]&&matched!==statusDefs[1]\"));
    }
"""
new_test = r'''    @Test public void personHeadersUseUniformTwoLineNamesAndFoodButtonsBreakAfterComma(){
        String script=script();
        assertTrue(script.contains(".pfvr-person-header{padding:8px 7px!important;font-size:12px!important"));
        assertTrue(script.contains("white-space:pre-line!important"));
        assertTrue(script.contains("-webkit-line-clamp:2"));
        assertTrue(script.contains("el.classList.remove('pfvr-name-small','pfvr-name-tiny')"));
        assertTrue(script.contains("el.classList.contains('pfvr-person-header')"));
        assertTrue(script.contains("family+',\\n'+given"));
        assertFalse(script.contains("family+',\n'+given"));
        assertTrue(script.contains("formatAttendanceChoiceLabel"));
        assertTrue(script.contains("',\\n$1'"));
        assertFalse(script.contains("',\n$1'"));
        assertTrue(script.contains("data-pfvr-display-label"));
        assertTrue(script.contains("pfvr-attendance-display-label::after"));
        assertFalse(script.contains("el.value=formatted"));
        assertFalse(script.contains("else el.textContent=formatted"));
    }
'''
test = replace_once(test, old_test, new_test, "strong JS escape regression test")
test_path.write_text(test)


gradle_path = Path("Android/app/build.gradle")
gradle = gradle_path.read_text()
gradle = replace_once(gradle, "versionCode 55", "versionCode 56", "version code")
gradle = replace_once(gradle, "versionName '0.11.8'", "versionName '0.11.9'", "version name")
gradle_path.write_text(gradle)


status_path = Path("STATUS.md")
status = status_path.read_text()
status = replace_once(status, "Stand: Testversion `0.11.8`", "Stand: Testversion `0.11.9`", "status version")
anchor = "## Aktueller Teststand\n\n"
entry = "- `0.11.9` behebt einen Laufzeitfehler der in `0.11.8` injizierten App-Ansicht: Die neu eingefügten Zeilenumbrüche wurden im erzeugten JavaScript als echte Zeilenumbrüche innerhalb einfacher String-Literale ausgegeben und machten das komplette Skin-Skript syntaktisch ungültig. Die Escape-Sequenzen werden nun explizit erhalten und per Regressionstest geprüft. Statusbeschriftungen werden ausserdem nur noch visuell über ein CSS-Pseudoelement dargestellt; die echten Website-Buttons/-Inputs und deren Werte bleiben unverändert.\n"
if anchor not in status:
    raise SystemExit("status anchor missing")
status = status.replace(anchor, anchor + entry, 1)
status_path.write_text(status)
