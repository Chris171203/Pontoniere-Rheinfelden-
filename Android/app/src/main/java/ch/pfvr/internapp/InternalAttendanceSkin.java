package ch.pfvr.internapp;

import java.util.Locale;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/** Builds the WebView skin and repairs concatenated attendance-status text. */
final class InternalAttendanceSkin {
    static final class StatusSplit {
        final String status;
        final String remainder;

        StatusSplit(String status, String remainder) {
            this.status = status;
            this.remainder = remainder;
        }
    }

    private static final Pattern[] STATUS_PATTERNS = new Pattern[]{
            Pattern.compile("^\\s*(mit\\s+essen)(.*)$", Pattern.CASE_INSENSITIVE | Pattern.DOTALL),
            Pattern.compile("^\\s*(ohne\\s+essen)(.*)$", Pattern.CASE_INSENSITIVE | Pattern.DOTALL),
            Pattern.compile("^\\s*(nicht\\s+(?:gewählt|gewaehlt))(.*)$", Pattern.CASE_INSENSITIVE | Pattern.DOTALL),
            Pattern.compile("^\\s*(komme\\s+nicht)(.*)$", Pattern.CASE_INSENSITIVE | Pattern.DOTALL)
    };

    private InternalAttendanceSkin() {}

    static StatusSplit splitLeadingStatus(String value) {
        if (value == null) return null;
        for (Pattern pattern : STATUS_PATTERNS) {
            Matcher matcher = pattern.matcher(value);
            if (!matcher.matches()) continue;
            String normalized = matcher.group(1).replaceAll("\\s+", " ").trim().toLowerCase(Locale.GERMAN);
            String label;
            if (normalized.startsWith("mit essen")) label = "Mit Essen";
            else if (normalized.startsWith("ohne essen")) label = "Ohne Essen";
            else if (normalized.startsWith("nicht")) label = "Nicht gewählt";
            else label = "Komme nicht";
            return new StatusSplit(label, matcher.group(2) == null ? "" : matcher.group(2).trim());
        }
        return null;
    }

    static String javascript(
            String background,
            String card,
            String soft,
            String text,
            String muted,
            String border,
            String link
    ) {
        String css =
                "html{color-scheme:" + ("#11171C".equalsIgnoreCase(background) ? "dark" : "light") + "!important;}" +
                "body{margin:0!important;padding:10px 10px 34px!important;background:" + background + "!important;color:" + text + "!important;font-family:Arial,sans-serif!important;font-size:16px!important;}" +
                "header,nav,footer,.navbar,.site-header,.site-footer{display:none!important;}" +
                "table{border-collapse:separate!important;border-spacing:8px!important;width:max-content!important;min-width:100%!important;background:transparent!important;}" +
                "td,th{background:" + card + "!important;color:" + text + "!important;border:1px solid " + border + "!important;border-radius:14px!important;padding:12px 10px!important;vertical-align:top!important;overflow-wrap:anywhere!important;}" +
                "td *,th *{white-space:normal!important;}" +
                "p,span,div,label,strong{color:" + text + "!important;}small{color:" + muted + "!important;}a{color:" + link + "!important;}" +
                "select,input[type=text],input[type=number]{background:" + soft + "!important;color:" + text + "!important;border:1px solid " + border + "!important;border-radius:12px!important;padding:10px!important;min-height:44px!important;}" +
                "button,input[type=submit],input[type=button],a.btn,.btn{min-height:48px!important;border:0!important;border-radius:12px!important;padding:10px 14px!important;font-size:16px!important;font-weight:700!important;line-height:1.25!important;box-shadow:none!important;}" +
                ".pfvr-attendance-status{display:block!important;width:max-content!important;max-width:100%!important;padding:7px 10px!important;margin:0 0 9px!important;border-radius:10px!important;font-weight:700!important;line-height:1.2!important;white-space:normal!important;}" +
                ".pfvr-attendance-detail{display:block!important;margin:0 0 5px!important;line-height:1.35!important;color:" + text + "!important;}";

        String definitions = "[" +
                "{p:'^\\\\s*mit\\\\s+essen',l:'Mit Essen',b:'#16863A',f:'#FFFFFF'}," +
                "{p:'^\\\\s*ohne\\\\s+essen',l:'Ohne Essen',b:'#F2C94C',f:'#17222B'}," +
                "{p:'^\\\\s*nicht\\\\s+(?:gewählt|gewaehlt)',l:'Nicht gewählt',b:'#6D7880',f:'#FFFFFF'}," +
                "{p:'^\\\\s*komme\\\\s+nicht',l:'Komme nicht',b:'#C83737',f:'#FFFFFF'}" +
                "]";

        return "(function(){" +
                "var st=document.getElementById('pfvr-internal-style');" +
                "if(!st){st=document.createElement('style');st.id='pfvr-internal-style';document.head.appendChild(st);}" +
                "st.innerHTML='" + escapeJs(css) + "';" +
                "var defs=" + definitions + ";" +
                "var norm=function(x){return (x||'').replace(/\\s+/g,' ').trim().toLowerCase();};" +
                "var paint=function(el,bg,fg){el.style.setProperty('background',bg,'important');el.style.setProperty('color',fg,'important');el.style.setProperty('border-color',bg,'important');};" +
                "var badge=function(d){var el=document.createElement('span');el.className='pfvr-attendance-status';el.textContent=d.l;paint(el,d.b,d.f);return el;};" +
                "var formatText=function(){" +
                    "var nodes=[];document.querySelectorAll('td,th').forEach(function(cell){var w=document.createTreeWalker(cell,NodeFilter.SHOW_TEXT);while(w.nextNode())nodes.push(w.currentNode);});" +
                    "nodes.forEach(function(node){" +
                        "var parent=node.parentElement;if(!parent||parent.closest('button,input,select,option,a,.pfvr-attendance-status'))return;" +
                        "var raw=node.nodeValue||'';if(!raw.trim())return;" +
                        "for(var i=0;i<defs.length;i++){var d=defs[i],m=raw.match(new RegExp(d.p,'i'));if(!m)continue;" +
                            "var rest=raw.slice(m[0].length).trim(),frag=document.createDocumentFragment();frag.appendChild(badge(d));" +
                            "if(rest){var detail=document.createElement('span');detail.className='pfvr-attendance-detail';detail.textContent=rest;frag.appendChild(detail);}" +
                            "node.parentNode.replaceChild(frag,node);break;" +
                        "}" +
                    "});" +
                "};" +
                "var controls=document.querySelectorAll('button,input[type=submit],input[type=button],a.btn,.btn');" +
                "controls.forEach(function(el){var t=norm(el.innerText||el.value),attend=false;" +
                    "if(t.indexOf('mit essen')>=0){paint(el,'#16863A','#FFFFFF');attend=true;}" +
                    "else if(t.indexOf('ohne essen')>=0){paint(el,'#F2C94C','#17222B');attend=true;}" +
                    "else if(t.indexOf('nicht gewählt')>=0||t.indexOf('nicht gewaehlt')>=0||t.indexOf('keine auswahl')>=0){paint(el,'#6D7880','#FFFFFF');attend=true;}" +
                    "else if(t.indexOf('komme nicht')>=0||t==='nicht'){paint(el,'#C83737','#FFFFFF');attend=true;}" +
                    "else{paint(el,'" + escapeJs(link) + "','#FFFFFF');}" +
                    "if(attend&&!el.dataset.pfvrRefreshBound){el.dataset.pfvrRefreshBound='1';el.addEventListener('click',function(){setTimeout(function(){window.location.reload();},2000);});}" +
                "});" +
                "document.querySelectorAll('p,div,strong,label').forEach(function(el){var t=norm(el.innerText);if(t.indexOf('tipp: diese seite als favorit')===0&&t.length<350)el.style.display='none';});" +
                "formatText();setTimeout(formatText,250);setTimeout(formatText,900);" +
                "var scheduled=false;" +
                "if(window.MutationObserver&&document.body){var observer=new MutationObserver(function(){if(scheduled)return;scheduled=true;setTimeout(function(){scheduled=false;formatText();},80);});observer.observe(document.body,{childList:true,subtree:true});}" +
                "})();";
    }

    private static String escapeJs(String value) {
        return value.replace("\\", "\\\\").replace("'", "\\'").replace("\r", "").replace("\n", "\\n");
    }
}
