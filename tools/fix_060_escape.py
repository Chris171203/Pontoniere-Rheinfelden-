from pathlib import Path

p = Path('Android/app/src/main/java/ch/pfvr/internapp/MainActivity.java')
text = p.read_text(encoding='utf-8')

if 'JSONObject.quote(css)' in text and 'String safe=message.replace("&","&amp;")' in text:
    print('0.6.0 Java escaping already fixed')
    raise SystemExit(0)

lines = text.splitlines()
out = []
changed = 0

safe_line = '        String safe=message.replace("&","&amp;").replace("<","&lt;").replace(">","&gt;");'
js_line = r'''        String js="(function(){var st=document.getElementById('pfvr-internal-style');if(!st){st=document.createElement('style');st.id='pfvr-internal-style';document.head.appendChild(st);}st.innerHTML="+JSONObject.quote(css)+";var norm=function(x){return (x||'').replace(/\\s+/g,' ').trim().toLowerCase();};var controls=document.querySelectorAll('button,input[type=submit],input[type=button],a.btn,.btn');controls.forEach(function(el){var t=norm(el.innerText||el.value);if(t.indexOf('mit essen')>=0){el.style.setProperty('background','#22A447','important');el.style.setProperty('color','#fff','important');}else if(t.indexOf('ohne essen')>=0){el.style.setProperty('background','#247E99','important');el.style.setProperty('color','#fff','important');}else if(t.indexOf('nicht')>=0){el.style.setProperty('background','#6D7880','important');el.style.setProperty('color','#fff','important');}else{el.style.setProperty('background','"+link+"','important');el.style.setProperty('color','#fff','important');}});document.querySelectorAll('p,div,strong,label').forEach(function(el){var t=norm(el.innerText);if(t.indexOf('tipp: diese seite als favorit')===0&&t.length<350){el.style.display='none';}});})();";'''

for line in lines:
    if 'String safe=message.replace' in line:
        out.append(safe_line)
        changed += 1
    elif "String js=\"(function(){var st=document.getElementById('pfvr-internal-style')" in line:
        out.append(js_line)
        changed += 1
    else:
        out.append(line)

if changed != 2:
    raise SystemExit(f'expected 2 escape fixes, got {changed}')

p.write_text('\n'.join(out) + '\n', encoding='utf-8')
print('Fixed 0.6.0 generated Java escaping')
