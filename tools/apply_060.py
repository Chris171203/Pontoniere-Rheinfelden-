from pathlib import Path

p = Path('Android/app/src/main/java/ch/pfvr/internapp/MainActivity.java')
s = p.read_text(encoding='utf-8')

if 'Testversion 0.6.0' in s:
    print('0.6.0 patch already applied')
    raise SystemExit(0)

def repl(old: str, new: str, name: str):
    global s
    n = s.count(old)
    if n != 1:
        raise SystemExit(f'{name}: expected exactly one match, got {n}')
    s = s.replace(old, new, 1)

repl(
    '    private static final String PREF_THEME = "theme_mode";\n',
    '    private static final String PREF_THEME = "theme_mode";\n    private static final String PREF_INTERNAL_APP_VIEW = "internal_app_view";\n',
    'internal view pref'
)

repl(
    '        about.addView(txt("PFVR Rheinfelden",16,TEXT,true)); about.addView(txt("Testversion 0.5.0 · 1.0.0 bleibt für den ersten offiziellen Release reserviert.",13,MUTED,false));',
    '        about.addView(txt("PFVR Rheinfelden",16,TEXT,true)); about.addView(txt("Testversion 0.6.0 · 1.0.0 bleibt für den ersten offiziellen Release reserviert.",13,MUTED,false));',
    'visible version'
)

old_internal = '''    private View internal() {
        String url=prefs.getString(PREF_INTERNAL_URL,""); if(!validInternal(url)) return internalMissing();
        LinearLayout root=new LinearLayout(this); root.setOrientation(LinearLayout.VERTICAL); root.setBackgroundColor(themeBg(Color.WHITE));
        LinearLayout tools=new LinearLayout(this); tools.setPadding(dp(9),dp(8),dp(9),dp(8)); tools.setBackgroundColor(themeBg(Color.rgb(236,243,247))); root.addView(tools,new LinearLayout.LayoutParams(-1,dp(56)));
        WebView web=web(false); activeWebView=web;
        Button back=btn("‹ Zurück",Color.WHITE,NAVY); back.setOnClickListener(v->handleBack()); tools.addView(back,new LinearLayout.LayoutParams(0,dp(40),1));
        Button start=btn("Start",NAVY,Color.WHITE); start.setOnClickListener(v->web.loadUrl(prefs.getString(PREF_INTERNAL_URL,""))); LinearLayout.LayoutParams sp=new LinearLayout.LayoutParams(0,dp(40),1); sp.setMargins(dp(7),0,0,0); tools.addView(start,sp);
        Button reload=btn("Neu laden",Color.WHITE,NAVY); reload.setOnClickListener(v->web.reload()); LinearLayout.LayoutParams rp=new LinearLayout.LayoutParams(0,dp(40),1); rp.setMargins(dp(7),0,0,0); tools.addView(reload,rp);
        root.addView(web,new LinearLayout.LayoutParams(-1,0,1)); web.loadUrl(url); return root;
    }
'''
new_internal = '''    private View internal() {
        String url=prefs.getString(PREF_INTERNAL_URL,""); if(!validInternal(url)) return internalMissing();
        LinearLayout root=new LinearLayout(this); root.setOrientation(LinearLayout.VERTICAL); root.setBackgroundColor(Color.WHITE);
        LinearLayout tools=new LinearLayout(this); tools.setPadding(dp(9),dp(8),dp(9),dp(8)); tools.setBackgroundColor(themeBg(Color.rgb(236,243,247))); root.addView(tools,new LinearLayout.LayoutParams(-1,dp(56)));
        WebView web=web(false); activeWebView=web; web.setBackgroundColor(Color.WHITE);
        if(android.os.Build.VERSION.SDK_INT>=33) web.getSettings().setAlgorithmicDarkeningAllowed(false);
        Button back=btn("‹ Zurück",Color.WHITE,NAVY); back.setOnClickListener(v->handleBack()); tools.addView(back,new LinearLayout.LayoutParams(0,dp(40),1));
        boolean appView=prefs.getBoolean(PREF_INTERNAL_APP_VIEW,false);
        Button mode=btn(appView?"Original":"App-Ansicht",NAVY,Color.WHITE);
        mode.setOnClickListener(v->{boolean next=!prefs.getBoolean(PREF_INTERNAL_APP_VIEW,false);prefs.edit().putBoolean(PREF_INTERNAL_APP_VIEW,next).apply();mode.setText(next?"Original":"App-Ansicht");web.reload();});
        LinearLayout.LayoutParams mp=new LinearLayout.LayoutParams(0,dp(40),1.25f); mp.setMargins(dp(7),0,0,0); tools.addView(mode,mp);
        Button reload=btn("Neu laden",Color.WHITE,NAVY); reload.setOnClickListener(v->web.reload()); LinearLayout.LayoutParams rp=new LinearLayout.LayoutParams(0,dp(40),1); rp.setMargins(dp(7),0,0,0); tools.addView(reload,rp);
        web.setWebViewClient(new WebViewClient(){
            @Override public boolean shouldOverrideUrlLoading(WebView v,WebResourceRequest r){Uri u=r.getUrl();String h=u.getHost()==null?"":u.getHost().toLowerCase(Locale.ROOT);if(h.endsWith("pfvr.ch"))return false;external(u.toString());return true;}
            @Override public void onPageFinished(WebView v,String u){super.onPageFinished(v,u);if(prefs.getBoolean(PREF_INTERNAL_APP_VIEW,false))internalSkin(v);}
            @Override public void onReceivedError(WebView v,android.webkit.WebResourceRequest r,android.webkit.WebResourceError e){super.onReceivedError(v,r,e);if(r.isForMainFrame())showInternalLoadError(v,"Ladefehler "+e.getErrorCode()+": "+String.valueOf(e.getDescription()));}
            @Override public void onReceivedHttpError(WebView v,android.webkit.WebResourceRequest r,android.webkit.WebResourceResponse e){super.onReceivedHttpError(v,r,e);if(r.isForMainFrame()&&e.getStatusCode()>=400)showInternalLoadError(v,"PFVR antwortet mit HTTP "+e.getStatusCode());}
        });
        root.addView(web,new LinearLayout.LayoutParams(-1,0,1)); web.loadUrl(url); return root;
    }

    private void showInternalLoadError(WebView v,String message){
        String safe=message.replace("\\","\\\\").replace("'","\\'").replace("<","&lt;").replace(">","&gt;");
        String html="<html><head><meta name='viewport' content='width=device-width,initial-scale=1'></head><body style='font-family:sans-serif;background:#fff;color:#15232e;padding:24px'><h2>Interner Bereich konnte nicht geladen werden</h2><p>"+safe+"</p><p>Prüfe den persönlichen Link unter Einstellungen oder tippe oben auf Neu laden.</p></body></html>";
        v.loadDataWithBaseURL("https://intern.pfvr.ch/",html,"text/html","UTF-8",null);
    }

    private void internalSkin(WebView v){
        String bg=darkMode?"#11171C":"#F4F7F9",card=darkMode?"#1A2228":"#FFFFFF",soft=darkMode?"#232E36":"#EDF3F6",text=darkMode?"#ECF1F4":"#15232E",muted=darkMode?"#A0B0BA":"#60717E",border=darkMode?"#344550":"#DCE5EA",link=darkMode?"#5BBED5":"#247E99";
        String css="html{color-scheme:"+(darkMode?"dark":"light")+"!important;}body{margin:0!important;padding:10px 10px 34px!important;background:"+bg+"!important;color:"+text+"!important;font-family:Arial,sans-serif!important;font-size:16px!important;}header,nav,footer,.navbar,.site-header,.site-footer{display:none!important;}table{border-collapse:separate!important;border-spacing:8px!important;width:max-content!important;min-width:100%!important;background:transparent!important;}td,th{background:"+card+"!important;color:"+text+"!important;border:1px solid "+border+"!important;border-radius:14px!important;padding:12px 10px!important;vertical-align:top!important;}p,span,div,label,strong{color:"+text+"!important;}small{color:"+muted+"!important;}a{color:"+link+"!important;}select,input[type=text],input[type=number]{background:"+soft+"!important;color:"+text+"!important;border:1px solid "+border+"!important;border-radius:12px!important;padding:10px!important;min-height:44px!important;}button,input[type=submit],input[type=button],a.btn,.btn{min-height:48px!important;border:0!important;border-radius:12px!important;padding:10px 14px!important;font-size:16px!important;font-weight:700!important;line-height:1.25!important;box-shadow:none!important;}";
        String js="(function(){var st=document.getElementById('pfvr-internal-style');if(!st){st=document.createElement('style');st.id='pfvr-internal-style';document.head.appendChild(st);}st.innerHTML='"+css.replace("\\","\\\\").replace("'","\\'")+"';var norm=function(x){return (x||'').replace(/\\s+/g,' ').trim().toLowerCase();};var controls=document.querySelectorAll('button,input[type=submit],input[type=button],a.btn,.btn');controls.forEach(function(el){var t=norm(el.innerText||el.value);if(t.indexOf('mit essen')>=0){el.style.setProperty('background','#22A447','important');el.style.setProperty('color','#fff','important');}else if(t.indexOf('ohne essen')>=0){el.style.setProperty('background','#247E99','important');el.style.setProperty('color','#fff','important');}else if(t.indexOf('nicht')>=0){el.style.setProperty('background','#6D7880','important');el.style.setProperty('color','#fff','important');}else{el.style.setProperty('background','"+link+"','important');el.style.setProperty('color','#fff','important');}});document.querySelectorAll('p,div,strong,label').forEach(function(el){var t=norm(el.innerText);if(t.indexOf('tipp: diese seite als favorit')===0&&t.length<350){el.style.display='none';}});})();";
        v.evaluateJavascript(js,null);
    }
'''
repl(old_internal, new_internal, 'internal app view')

p.write_text(s, encoding='utf-8')
print('Applied 0.6.0 internal app view + WebView diagnostics')
