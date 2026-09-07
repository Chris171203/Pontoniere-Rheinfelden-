from pathlib import Path


def replace_once(text, old, new, label):
    count = text.count(old)
    if count != 1:
        raise SystemExit(f"{label}: expected 1 occurrence, got {count}")
    return text.replace(old, new, 1)


# Keep the already large generated JS below Java's single-constant size limit:
# reuse the existing person-tool scope and add only a compact source-label cleanup.
skin_path = Path("Android/app/src/main/java/ch/pfvr/internapp/InternalAttendanceSkin.java")
skin = skin_path.read_text()
old = """                    if(anchor&&scope&&anchor!==scope)anchor.style.display='none';
                    return panel;
"""
new = """                    if(anchor&&scope&&anchor!==scope)anchor.style.display='none';
                    if(scope)scope.querySelectorAll('label,p,span,strong,small,div').forEach(function(el){var v=norm(text(el));if(v.length<140&&v.indexOf('person')>=0&&v.indexOf('hinzuf')>=0&&!el.querySelector('select,button,input,a'))el.style.display='none';});
                    return panel;
"""
skin = replace_once(skin, old, new, "hide original person-add prompt remnants")
skin_path.write_text(skin)


# Keep the original website invisible while the saved App-Ansicht is being projected.
main_path = Path("Android/app/src/main/java/ch/pfvr/internapp/MainActivity.java")
main = main_path.read_text()

old = """        boolean appView=prefs.getBoolean(PREF_INTERNAL_APP_VIEW,true);
        Button people=btn(\"Personen\",Color.WHITE,NAVY);
"""
new = """        boolean appView=prefs.getBoolean(PREF_INTERNAL_APP_VIEW,true);
        if(appView)hideInternalWebForAppView(web);
        Button people=btn(\"Personen\",Color.WHITE,NAVY);
"""
main = replace_once(main, old, new, "hide initial internal webview")

old = """        mode.setOnClickListener(v->{boolean next=!prefs.getBoolean(PREF_INTERNAL_APP_VIEW,true);prefs.edit().putBoolean(PREF_INTERNAL_APP_VIEW,next).apply();mode.setText(ui(next?\"Original\":\"App-Ansicht\"));people.setVisibility(next?View.VISIBLE:View.GONE);web.clearCache(false);web.reload();});
"""
new = """        mode.setOnClickListener(v->{boolean next=!prefs.getBoolean(PREF_INTERNAL_APP_VIEW,true);prefs.edit().putBoolean(PREF_INTERNAL_APP_VIEW,next).apply();mode.setText(ui(next?\"Original\":\"App-Ansicht\"));people.setVisibility(next?View.VISIBLE:View.GONE);if(next)hideInternalWebForAppView(web);else showInternalWeb(web);web.clearCache(false);web.reload();});
"""
main = replace_once(main, old, new, "mode switch visibility")

old = """        Button reload=btn(\"Neu laden\",Color.WHITE,NAVY); reload.setOnClickListener(v->{web.clearCache(false);web.reload();}); LinearLayout.LayoutParams rp=new LinearLayout.LayoutParams(0,dp(40),1); rp.setMargins(dp(7),0,0,0); tools.addView(reload,rp);
"""
new = """        Button reload=btn(\"Neu laden\",Color.WHITE,NAVY); reload.setOnClickListener(v->{if(prefs.getBoolean(PREF_INTERNAL_APP_VIEW,true))hideInternalWebForAppView(web);web.clearCache(false);web.reload();}); LinearLayout.LayoutParams rp=new LinearLayout.LayoutParams(0,dp(40),1); rp.setMargins(dp(7),0,0,0); tools.addView(reload,rp);
"""
main = replace_once(main, old, new, "reload visibility")

old = """            @Override public boolean shouldOverrideUrlLoading(WebView v,WebResourceRequest r){Uri u=r.getUrl();if(\"https\".equalsIgnoreCase(u.getScheme())&&AppLinkPolicy.isInternalPfvrHost(u.getHost()))return false;external(u.toString());return true;}
            @Override public void onPageFinished(WebView v,String u){super.onPageFinished(v,u);if(prefs.getBoolean(PREF_INTERNAL_APP_VIEW,true))internalSkin(v);}
"""
new = """            @Override public boolean shouldOverrideUrlLoading(WebView v,WebResourceRequest r){Uri u=r.getUrl();if(\"https\".equalsIgnoreCase(u.getScheme())&&AppLinkPolicy.isInternalPfvrHost(u.getHost()))return false;external(u.toString());return true;}
            @Override public void onPageStarted(WebView v,String u,Bitmap icon){super.onPageStarted(v,u,icon);if(prefs.getBoolean(PREF_INTERNAL_APP_VIEW,true))hideInternalWebForAppView(v);else showInternalWeb(v);}
            @Override public void onPageFinished(WebView v,String u){super.onPageFinished(v,u);if(prefs.getBoolean(PREF_INTERNAL_APP_VIEW,true)){internalSkin(v);revealInternalAppViewWhenReady(v,0);}else showInternalWeb(v);}
"""
main = replace_once(main, old, new, "webview page lifecycle visibility")

old = """    view.evaluateJavascript(InternalAttendanceSkin.javascript(background,card,soft,text,muted,border,link,uiMode(),baseInternalUrl),null);
}

    private View internalMissing() {
"""
new = """    view.evaluateJavascript(InternalAttendanceSkin.javascript(background,card,soft,text,muted,border,link,uiMode(),baseInternalUrl),null);
}

    private void hideInternalWebForAppView(WebView web){if(web!=null){web.animate().cancel();web.setAlpha(1f);web.setVisibility(View.INVISIBLE);}}
    private void showInternalWeb(WebView web){if(web!=null){web.animate().cancel();web.setAlpha(1f);web.setVisibility(View.VISIBLE);}}
    private void revealInternalAppViewWhenReady(WebView web,int attempt){
        if(web==null)return;
        if(!prefs.getBoolean(PREF_INTERNAL_APP_VIEW,true)){showInternalWeb(web);return;}
        web.evaluateJavascript(\"(function(){return !!document.querySelector('.pfvr-attendance-mobile');})()\",result->{
            if(!prefs.getBoolean(PREF_INTERNAL_APP_VIEW,true)){showInternalWeb(web);return;}
            if(\"true\".equalsIgnoreCase(String.valueOf(result))){web.animate().cancel();web.setAlpha(0f);web.setVisibility(View.VISIBLE);web.animate().alpha(1f).setDuration(80L).start();return;}
            if(attempt>=24){showInternalWeb(web);return;}
            new Handler(Looper.getMainLooper()).postDelayed(()->revealInternalAppViewWhenReady(web,attempt+1),100L);
        });
    }

    private View internalMissing() {
"""
main = replace_once(main, old, new, "internal app view reveal helpers")
main_path.write_text(main)


ui_test_path = Path("Android/app/src/test/java/ch/pfvr/internapp/InternalAttendanceUiRegressionTest.java")
ui_test = ui_test_path.read_text()
anchor = """    @Test public void personHeadersUseUniformTwoLineNamesAndFoodButtonsBreakAfterComma(){
        String script=script();
        assertTrue(script.contains(\".pfvr-person-header{padding:8px 7px!important;font-size:12px!important\"));
        assertTrue(script.contains(\"white-space:pre-line!important\"));
        assertTrue(script.contains(\"-webkit-line-clamp:2\"));
        assertTrue(script.contains(\"el.classList.remove('pfvr-name-small','pfvr-name-tiny')\"));
        assertTrue(script.contains(\"el.classList.contains('pfvr-person-header')\"));
        assertTrue(script.contains(\"family+',\\\\n'+given\"));
        assertFalse(script.contains(\"family+',\\n'+given\"));
        assertTrue(script.contains(\"formatAttendanceChoiceLabel\"));
        assertTrue(script.contains(\"',\\\\n$1'\"));
        assertFalse(script.contains(\"',\\n$1'\"));
        assertTrue(script.contains(\"data-pfvr-display-label\"));
        assertTrue(script.contains(\"pfvr-attendance-display-label::after\"));
        assertFalse(script.contains(\"el.value=formatted\"));
        assertFalse(script.contains(\"else el.textContent=formatted\"));
    }
"""
addition = anchor + """

    @Test public void originalPersonAddPromptRemnantsAreHiddenInAppProjection(){
        String script=script();
        assertTrue(script.contains(\"scope.querySelectorAll('label,p,span,strong,small,div')\"));
        assertTrue(script.contains(\"v.indexOf('person')>=0&&v.indexOf('hinzuf')>=0\"));
        assertTrue(script.contains(\"!el.querySelector('select,button,input,a')\"));
    }
"""
ui_test = replace_once(ui_test, anchor, addition, "attendance prompt cleanup regression test")
ui_test_path.write_text(ui_test)


source_test_path = Path("Android/app/src/test/java/ch/pfvr/internapp/InternalAppViewSourceTest.java")
source_test_path.write_text('''package ch.pfvr.internapp;\n\nimport static org.junit.Assert.assertTrue;\n\nimport java.nio.charset.StandardCharsets;\nimport java.nio.file.Files;\nimport java.nio.file.Path;\nimport java.nio.file.Paths;\n\nimport org.junit.Test;\n\npublic class InternalAppViewSourceTest {\n    private static String source() throws Exception {\n        String relative = "src/main/java/ch/pfvr/internapp/MainActivity.java";\n        Path[] candidates = new Path[]{Paths.get(relative), Paths.get("app", relative), Paths.get("Android", "app", relative)};\n        for (Path candidate : candidates) if (Files.isRegularFile(candidate)) return new String(Files.readAllBytes(candidate), StandardCharsets.UTF_8);\n        throw new IllegalStateException("MainActivity.java not found from " + System.getProperty("user.dir"));\n    }\n    private static String internalSection(String source) {\n        int start=source.indexOf("private View internal()"),end=source.indexOf("private View internalMissing()",start);\n        assertTrue(start>=0&&end>start);\n        return source.substring(start,end);\n    }\n    @Test public void savedAppViewStaysInvisibleUntilProjectionExists() throws Exception {\n        String section=internalSection(source());\n        assertTrue(section.contains("if(appView)hideInternalWebForAppView(web)"));\n        assertTrue(section.contains("onPageStarted(WebView v,String u,Bitmap icon)"));\n        assertTrue(section.contains("internalSkin(v);revealInternalAppViewWhenReady(v,0)"));\n        assertTrue(section.contains("document.querySelector('.pfvr-attendance-mobile')"));\n    }\n    @Test public void revealIsBoundedAndOriginalModeShowsImmediately() throws Exception {\n        String section=internalSection(source());\n        assertTrue(section.contains("if(attempt>=24){showInternalWeb(web);return;}"));\n        assertTrue(section.contains("postDelayed(()->revealInternalAppViewWhenReady(web,attempt+1),100L)"));\n        assertTrue(section.contains("if(next)hideInternalWebForAppView(web);else showInternalWeb(web)"));\n        assertTrue(section.contains("else showInternalWeb(v)"));\n    }\n}\n''')


gradle_path = Path("Android/app/build.gradle")
gradle = gradle_path.read_text()
gradle = replace_once(gradle, "versionCode 56", "versionCode 57", "version code")
gradle = replace_once(gradle, "versionName '0.11.9'", "versionName '0.12.0'", "version name")
gradle_path.write_text(gradle)

status_path = Path("STATUS.md")
status = status_path.read_text()
status = replace_once(status, "Stand: Testversion `0.11.9`", "Stand: Testversion `0.12.0`", "status version")
anchor = "## Aktueller Teststand\n\n"
entry = "- `0.12.0` poliert den internen App-Modus: Textreste des originalen Personen-hinzufügen-Bereichs werden in der App-Ansicht ausgeblendet, die von der App benötigten echten Website-Controls bleiben erhalten. Ist die App-Ansicht gespeichert, bleibt die Originalseite beim Laden unsichtbar und wird erst nach erfolgreichem Aufbau der mobilen Matrix eingeblendet; ein begrenzter Timeout erhält die Originalseite als Fehler-Fallback. Umschalten auf `Original` bleibt direkt möglich.\n"
if anchor not in status: raise SystemExit("STATUS anchor missing")
status = status.replace(anchor, anchor + entry, 1)
status_path.write_text(status)
