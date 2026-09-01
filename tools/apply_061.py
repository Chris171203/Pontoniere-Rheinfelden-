from pathlib import Path

p = Path('Android/app/src/main/java/ch/pfvr/internapp/MainActivity.java')
text = p.read_text(encoding='utf-8')

if 'normalizeInternalUrl' in text and '#F2C94C' in text and 'LOAD_NO_CACHE' in text:
    print('0.6.1 patch already applied')
    raise SystemExit(0)

old = '        String url=prefs.getString(PREF_INTERNAL_URL,""); if(!validInternal(url)) return internalMissing();'
new = '        String url=normalizeInternalUrl(prefs.getString(PREF_INTERNAL_URL,"")); if(!validInternal(url)) return internalMissing(); prefs.edit().putString(PREF_INTERNAL_URL,url).apply();'
if old not in text:
    raise SystemExit('internal URL line not found')
text = text.replace(old, new, 1)

old = '        if(android.os.Build.VERSION.SDK_INT>=33) web.getSettings().setAlgorithmicDarkeningAllowed(false);'
new = old + '\n        web.getSettings().setCacheMode(WebSettings.LOAD_NO_CACHE);'
if old not in text:
    raise SystemExit('darkening line not found')
text = text.replace(old, new, 1)

old = '        mode.setOnClickListener(v->{boolean next=!prefs.getBoolean(PREF_INTERNAL_APP_VIEW,false);prefs.edit().putBoolean(PREF_INTERNAL_APP_VIEW,next).apply();mode.setText(next?"Original":"App-Ansicht");web.reload();});'
new = '        mode.setOnClickListener(v->{boolean next=!prefs.getBoolean(PREF_INTERNAL_APP_VIEW,false);prefs.edit().putBoolean(PREF_INTERNAL_APP_VIEW,next).apply();mode.setText(next?"Original":"App-Ansicht");web.clearCache(false);web.reload();});'
if old not in text:
    raise SystemExit('mode listener not found')
text = text.replace(old, new, 1)

old = '        Button reload=btn("Neu laden",Color.WHITE,NAVY); reload.setOnClickListener(v->web.reload()); LinearLayout.LayoutParams rp=new LinearLayout.LayoutParams(0,dp(40),1); rp.setMargins(dp(7),0,0,0); tools.addView(reload,rp);'
new = '        Button reload=btn("Neu laden",Color.WHITE,NAVY); reload.setOnClickListener(v->{web.clearCache(false);web.reload();}); LinearLayout.LayoutParams rp=new LinearLayout.LayoutParams(0,dp(40),1); rp.setMargins(dp(7),0,0,0); tools.addView(reload,rp);'
if old not in text:
    raise SystemExit('reload listener not found')
text = text.replace(old, new, 1)

lines = text.splitlines()
out = []
replaced_js = False
for line in lines:
    if 'String js="(function(){var st=document.getElementById(\'pfvr-internal-style\')' in line:
        out.append(r'''        String js="(function(){var st=document.getElementById('pfvr-internal-style');if(!st){st=document.createElement('style');st.id='pfvr-internal-style';document.head.appendChild(st);}st.innerHTML="+JSONObject.quote(css)+";var norm=function(x){return (x||'').replace(/\\s+/g,' ').trim().toLowerCase();};var paint=function(el,bg,fg){el.style.setProperty('background',bg,'important');el.style.setProperty('color',fg,'important');el.style.setProperty('border-color',bg,'important');};var controls=document.querySelectorAll('button,input[type=submit],input[type=button],a.btn,.btn');controls.forEach(function(el){var t=norm(el.innerText||el.value);var attend=false;if(t.indexOf('mit essen')>=0){paint(el,'#16863A','#FFFFFF');attend=true;}else if(t.indexOf('ohne essen')>=0){paint(el,'#F2C94C','#17222B');attend=true;}else if(t.indexOf('nicht gewählt')>=0||t.indexOf('nicht gewaehlt')>=0||t.indexOf('keine auswahl')>=0){paint(el,'#6D7880','#FFFFFF');attend=true;}else if(t.indexOf('komme nicht')>=0||t==='nicht'){paint(el,'#C83737','#FFFFFF');attend=true;}else{paint(el,'"+link+"','#FFFFFF');}if(attend&&!el.dataset.pfvrRefreshBound){el.dataset.pfvrRefreshBound='1';el.addEventListener('click',function(){setTimeout(function(){window.location.reload();},2000);});}});document.querySelectorAll('p,div,strong,label').forEach(function(el){var t=norm(el.innerText);if(t.indexOf('tipp: diese seite als favorit')===0&&t.length<350){el.style.display='none';}});})();";''')
        replaced_js = True
    else:
        out.append(line)
if not replaced_js:
    raise SystemExit('internalSkin JS line not found')
text = '\n'.join(out) + '\n'

old = '            .setPositiveButton("Speichern",(d,w)->{String x=input.getText().toString().trim();if(validInternal(x)){prefs.edit().putString(PREF_INTERNAL_URL,x).apply();if(current==Screen.SETTINGS)navigate(Screen.SETTINGS);}else Toast.makeText(this,"Ungültiger https://intern.pfvr.ch-Link",Toast.LENGTH_LONG).show();})'
new = '            .setPositiveButton("Speichern",(d,w)->{String x=normalizeInternalUrl(input.getText().toString().trim());if(validInternal(x)){prefs.edit().putString(PREF_INTERNAL_URL,x).apply();if(current==Screen.SETTINGS)navigate(Screen.SETTINGS);}else Toast.makeText(this,"Bitte den persönlichen An-/Abmelde-Link (what=abmeldung) verwenden.",Toast.LENGTH_LONG).show();})'
if old not in text:
    raise SystemExit('settings save line not found')
text = text.replace(old, new, 1)

old = '    private boolean validInternal(String x){if(x==null||x.isBlank())return false;try{Uri u=Uri.parse(x);return "https".equalsIgnoreCase(u.getScheme())&&"intern.pfvr.ch".equalsIgnoreCase(u.getHost());}catch(Exception e){return false;}}'
new = '''    private String normalizeInternalUrl(String x){if(x==null)return "";return x.trim().replace("what=abmeldung_ics_feed","what=abmeldung");}\n    private boolean validInternal(String x){if(x==null||x.isBlank())return false;try{Uri u=Uri.parse(x);return "https".equalsIgnoreCase(u.getScheme())&&"intern.pfvr.ch".equalsIgnoreCase(u.getHost())&&"abmeldung".equals(u.getQueryParameter("what"));}catch(Exception e){return false;}}'''
if old not in text:
    raise SystemExit('validInternal method not found')
text = text.replace(old, new, 1)

p.write_text(text, encoding='utf-8')
print('Applied 0.6.1 attendance colors, no-cache refresh and internal URL normalization')
