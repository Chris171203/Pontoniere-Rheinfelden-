package ch.pfvr.internapp;

import java.util.Locale;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/** Builds the mobile WebView projection of the external attendance matrix. */
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
        String template = """
                (function(){
                  if(window.__pfvrAttendanceMobileV2)return;
                  window.__pfvrAttendanceMobileV2=true;

                  var COLORS={background:'__BG__',card:'__CARD__',soft:'__SOFT__',text:'__TEXT__',muted:'__MUTED__',border:'__BORDER__',link:'__LINK__'};
                  var STORAGE_KEY='pfvr-attendance-view-state-v2';
                  var norm=function(value){return (value||'').replace(/\\s+/g,' ').trim().toLowerCase();};
                  var text=function(el){return (el&&((el.innerText||el.textContent)||''))||'';};
                  var element=function(tag,className){var node=document.createElement(tag);if(className)node.className=className;return node;};
                  var isInteractive=function(el){return !!(el&&el.closest&&el.closest('button,input,select,textarea,a'))};

                  var css=`
                  html{color-scheme:__SCHEME__!important;}
                  html,body{width:100%!important;max-width:100%!important;overflow-x:hidden!important;}
                  body{margin:0!important;padding:10px 10px 34px!important;box-sizing:border-box!important;background:${COLORS.background}!important;color:${COLORS.text}!important;font-family:Arial,sans-serif!important;font-size:16px!important;}
                  header,nav,footer,.navbar,.site-header,.site-footer{display:none!important;}
                  p,span,div,label,strong{color:${COLORS.text}!important;}small{color:${COLORS.muted}!important;}a{color:${COLORS.link}!important;}
                  select,input[type=text],input[type=number]{background:${COLORS.soft}!important;color:${COLORS.text}!important;border:1px solid ${COLORS.border}!important;border-radius:12px!important;padding:10px!important;min-height:44px!important;box-sizing:border-box!important;}
                  button,input[type=submit],input[type=button],a.btn,.btn{min-height:46px!important;border:0!important;border-radius:12px!important;padding:9px 12px!important;font-size:15px!important;font-weight:700!important;line-height:1.25!important;box-shadow:none!important;white-space:normal!important;overflow-wrap:break-word!important;word-break:normal!important;}
                  .pfvr-attendance-source{display:none!important;}
                  .pfvr-attendance-mobile{display:flex!important;flex-direction:column!important;gap:12px!important;width:100%!important;max-width:100%!important;box-sizing:border-box!important;}
                  .pfvr-person-tools{display:flex!important;flex-direction:column!important;gap:8px!important;background:${COLORS.card}!important;border:1px solid ${COLORS.border}!important;border-radius:16px!important;padding:12px!important;box-sizing:border-box!important;}
                  .pfvr-person-tools-head{display:flex!important;align-items:center!important;justify-content:space-between!important;gap:10px!important;}
                  .pfvr-person-tools-title{font-size:17px!important;font-weight:700!important;}
                  .pfvr-person-tools-toggle{min-height:40px!important;padding:7px 11px!important;background:${COLORS.link}!important;color:#fff!important;}
                  .pfvr-person-tools-body{display:none!important;flex-direction:column!important;gap:8px!important;padding-top:2px!important;}
                  .pfvr-person-tools.open .pfvr-person-tools-body{display:flex!important;}
                  .pfvr-person-tools-body select{width:100%!important;max-width:100%!important;}
                  .pfvr-day-section{display:grid!important;grid-template-columns:minmax(140px,42%) minmax(0,1fr)!important;gap:9px!important;align-items:stretch!important;width:100%!important;max-width:100%!important;box-sizing:border-box!important;}
                  .pfvr-day-meta{min-width:0!important;background:${COLORS.card}!important;border:1px solid ${COLORS.border}!important;border-radius:16px!important;padding:12px!important;box-sizing:border-box!important;overflow-wrap:break-word!important;word-break:normal!important;}
                  .pfvr-day-meta>*{max-width:100%!important;box-sizing:border-box!important;white-space:normal!important;overflow-wrap:break-word!important;word-break:normal!important;}
                  .pfvr-day-people{display:flex!important;gap:8px!important;overflow-x:auto!important;overflow-y:hidden!important;min-width:0!important;padding:0 0 3px!important;box-sizing:border-box!important;scroll-snap-type:x proximity!important;-webkit-overflow-scrolling:touch!important;overscroll-behavior-x:contain!important;}
                  .pfvr-person-card{flex:0 0 clamp(148px,48vw,190px)!important;min-width:0!important;min-height:100%!important;background:${COLORS.card}!important;border:1px solid ${COLORS.border}!important;border-radius:16px!important;padding:11px!important;box-sizing:border-box!important;scroll-snap-align:start!important;display:flex!important;flex-direction:column!important;gap:9px!important;}
                  .pfvr-person-name{font-size:14px!important;font-weight:700!important;line-height:1.25!important;overflow-wrap:break-word!important;word-break:normal!important;}
                  .pfvr-person-control{display:flex!important;flex-direction:column!important;gap:7px!important;margin-top:auto!important;min-width:0!important;}
                  .pfvr-person-control>*{max-width:100%!important;box-sizing:border-box!important;}
                  .pfvr-person-control button,.pfvr-person-control input[type=submit],.pfvr-person-control input[type=button],.pfvr-person-control a.btn,.pfvr-person-control .btn,.pfvr-person-control select{width:100%!important;}
                  .pfvr-empty-status{font-size:12px!important;color:${COLORS.muted}!important;}
                  .pfvr-attendance-status{display:block!important;width:max-content!important;max-width:100%!important;padding:6px 9px!important;margin:0 0 7px!important;border-radius:10px!important;font-weight:700!important;line-height:1.2!important;white-space:normal!important;overflow-wrap:break-word!important;word-break:normal!important;}
                  .pfvr-attendance-detail{display:block!important;margin:0 0 4px!important;line-height:1.35!important;color:${COLORS.text}!important;white-space:normal!important;overflow-wrap:break-word!important;word-break:normal!important;}
                  @media(max-width:320px){.pfvr-day-section{grid-template-columns:minmax(125px,42%) minmax(0,1fr)!important;}.pfvr-person-card{flex-basis:145px!important;}.pfvr-day-meta{padding:10px!important;}}
                  `;
                  var style=document.getElementById('pfvr-internal-style');
                  if(!style){style=document.createElement('style');style.id='pfvr-internal-style';document.head.appendChild(style);}
                  style.textContent=css;

                  var viewport=document.querySelector('meta[name=viewport]');
                  if(!viewport){viewport=document.createElement('meta');viewport.name='viewport';document.head.appendChild(viewport);}
                  viewport.content='width=device-width,initial-scale=1,maximum-scale=3,user-scalable=yes';

                  var statusDefs=[
                    {pattern:/^\\s*mit\\s+essen/i,label:'Mit Essen',background:'#16863A',foreground:'#FFFFFF'},
                    {pattern:/^\\s*ohne\\s+essen/i,label:'Ohne Essen',background:'#F2C94C',foreground:'#17222B'},
                    {pattern:/^\\s*nicht\\s+(?:gewählt|gewaehlt)/i,label:'Nicht gewählt',background:'#6D7880',foreground:'#FFFFFF'},
                    {pattern:/^\\s*komme\\s+nicht/i,label:'Komme nicht',background:'#C83737',foreground:'#FFFFFF'}
                  ];
                  var paint=function(el,bg,fg){if(!el)return;el.style.setProperty('background',bg,'important');el.style.setProperty('color',fg,'important');el.style.setProperty('border-color',bg,'important');};
                  var statusForValue=function(value){
                    var normalized=norm(value);
                    if(normalized.indexOf('ohne essen')>=0)return statusDefs[1];
                    if(normalized.indexOf('mit essen')>=0)return statusDefs[0];
                    if(normalized.indexOf('nicht gewählt')>=0||normalized.indexOf('nicht gewaehlt')>=0||normalized.indexOf('keine auswahl')>=0)return statusDefs[2];
                    if(normalized.indexOf('komme nicht')>=0||normalized==='nicht')return statusDefs[3];
                    return null;
                  };
                  var controlValue=function(el){
                    if(!el)return '';
                    if(el.tagName==='SELECT'){
                      var option=el.options&&el.selectedIndex>=0?el.options[el.selectedIndex]:null;
                      return ((option&&(option.textContent||option.value))||el.value||'');
                    }
                    return el.innerText||el.value||el.textContent||'';
                  };
                  var styleInteractive=function(root){
                    (root||document).querySelectorAll('button,input[type=submit],input[type=button],a.btn,.btn,select').forEach(function(el){
                      var matched=statusForValue(controlValue(el));
                      if(matched){paint(el,matched.background,matched.foreground);return;}
                      if(el.tagName==='SELECT'){
                        paint(el,COLORS.soft,COLORS.text);
                        el.style.setProperty('border-color',COLORS.border,'important');
                      }else paint(el,COLORS.link,'#FFFFFF');
                    });
                  };
                  var refreshInteractiveSoon=function(root){
                    if(!root)return;
                    styleInteractive(root);
                    setTimeout(function(){styleInteractive(root);},80);
                    setTimeout(function(){styleInteractive(root);},350);
                  };
                  var splitStatusText=function(root){
                    var nodes=[];
                    (root||document).querySelectorAll('.pfvr-day-meta,.pfvr-person-control').forEach(function(container){
                      var walker=document.createTreeWalker(container,NodeFilter.SHOW_TEXT);while(walker.nextNode())nodes.push(walker.currentNode);
                    });
                    nodes.forEach(function(node){
                      if(!node.parentElement||isInteractive(node.parentElement)||node.parentElement.closest('.pfvr-attendance-status'))return;
                      var raw=node.nodeValue||'';if(!raw.trim())return;
                      for(var i=0;i<statusDefs.length;i++){
                        var def=statusDefs[i],match=raw.match(def.pattern);if(!match)continue;
                        var rest=raw.slice(match[0].length).trim(),fragment=document.createDocumentFragment();
                        var badge=element('span','pfvr-attendance-status');badge.textContent=def.label;paint(badge,def.background,def.foreground);fragment.appendChild(badge);
                        if(rest){var detail=element('span','pfvr-attendance-detail');detail.textContent=rest;fragment.appendChild(detail);}
                        node.parentNode.replaceChild(fragment,node);break;
                      }
                    });
                  };

                  var scoreTable=function(table){
                    if(!table||table.dataset.pfvrIgnore==='1')return -1;
                    var rows=Array.from(table.rows||[]);if(rows.length<2)return -1;
                    var columns=0;rows.forEach(function(row){columns=Math.max(columns,row.cells?row.cells.length:0);});if(columns<2)return -1;
                    var controls=table.querySelectorAll('button,input,select,a.btn,.btn').length;
                    var dateCells=0;Array.from(rows[0].cells||[]).slice(1).forEach(function(cell){if(/\\b\\d{1,2}\\.\\d{1,2}\\b/.test(text(cell)))dateCells++;});
                    return controls*3+dateCells*5+rows.length+columns;
                  };
                  var findTable=function(){var best=null,bestScore=-1;document.querySelectorAll('table').forEach(function(table){var score=scoreTable(table);if(score>bestScore){best=table;bestScore=score;}});return bestScore>=5?best:null;};
                  var cleanPersonName=function(value){return (value||'').replace(/[\\u{1F300}-\\u{1FAFF}]/gu,' ').replace(/\\s+/g,' ').trim();};
                  var moveChildren=function(from,to){while(from&&from.firstChild)to.appendChild(from.firstChild);};

                  var findPersonTools=function(table){
                    var candidates=Array.from(document.querySelectorAll('label,p,span,div')).filter(function(el){var value=norm(text(el));return value.indexOf('person zur liste hinzuzufügen')===0&&value.length<120;});
                    if(!candidates.length)return null;
                    var anchor=candidates[0],scope=anchor.parentElement;
                    for(var depth=0;scope&&depth<4;depth++,scope=scope.parentElement){
                      if(scope===document.body)break;
                      var select=scope.querySelector('select');
                      if(!select)continue;
                      var panel=element('div','pfvr-person-tools');
                      var head=element('div','pfvr-person-tools-head');
                      var title=element('div','pfvr-person-tools-title');title.textContent='Teilnehmende';
                      var toggle=element('button','pfvr-person-tools-toggle');toggle.type='button';toggle.textContent='+ Person';
                      head.appendChild(title);head.appendChild(toggle);panel.appendChild(head);
                      var body=element('div','pfvr-person-tools-body');
                      var hint=element('div');hint.textContent='Person zur Liste hinzufügen:';hint.style.color=COLORS.muted;hint.style.fontSize='12px';body.appendChild(hint);
                      body.appendChild(select);
                      var buttons=Array.from(scope.querySelectorAll('button,input[type=submit],input[type=button],a.btn,.btn')).filter(function(control){return norm(control.innerText||control.value).indexOf('alle anzeigen')>=0;});
                      buttons.forEach(function(control){body.appendChild(control);});
                      panel.appendChild(body);
                      toggle.addEventListener('click',function(){panel.classList.toggle('open');});
                      anchor.style.display='none';
                      return panel;
                    }
                    return null;
                  };

                  var saveViewState=function(){
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
                  var bindStatePreservation=function(root){
                    if(window.__pfvrAttendanceStateBound)return;window.__pfvrAttendanceStateBound=true;
                    document.addEventListener('click',function(event){
                      if(event.target&&event.target.closest&&event.target.closest('.pfvr-person-control button,.pfvr-person-control input,.pfvr-person-control a,.pfvr-person-tools button,.pfvr-person-tools a')){
                        saveViewState();refreshInteractiveSoon(document.querySelector('.pfvr-attendance-mobile'));
                      }
                    },true);
                    document.addEventListener('change',function(event){
                      if(event.target&&event.target.closest&&event.target.closest('.pfvr-person-control select,.pfvr-person-tools select')){
                        saveViewState();refreshInteractiveSoon(document.querySelector('.pfvr-attendance-mobile'));
                      }
                    },true);
                    window.addEventListener('beforeunload',saveViewState);
                  };
                  var bindInteractiveObserver=function(root){
                    if(!root||root.dataset.pfvrColorObserver==='1'||!window.MutationObserver)return;
                    root.dataset.pfvrColorObserver='1';
                    var scheduled=false;
                    var observer=new MutationObserver(function(){
                      if(scheduled)return;scheduled=true;
                      setTimeout(function(){scheduled=false;styleInteractive(root);},40);
                    });
                    observer.observe(root,{subtree:true,childList:true,characterData:true,attributes:true,attributeFilter:['value','selected','class']});
                  };

                  var buildMobile=function(){
                    if(document.querySelector('.pfvr-attendance-mobile'))return true;
                    var table=findTable();if(!table)return false;
                    var rows=Array.from(table.rows||[]);if(rows.length<2||!rows[0].cells||rows[0].cells.length<2)return false;
                    var header=rows[0],mobile=element('div','pfvr-attendance-mobile');
                    table.parentNode.insertBefore(mobile,table);
                    var tools=findPersonTools(table);if(tools)mobile.appendChild(tools);
                    var participantRows=rows.slice(1).filter(function(row){return row.cells&&row.cells.length>=2;});
                    var names=participantRows.map(function(row,index){var value=cleanPersonName(text(row.cells[0]));return value||('Person '+(index+1));});
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
                    table.classList.add('pfvr-attendance-source');
                    splitStatusText(mobile);styleInteractive(mobile);bindStatePreservation(mobile);bindInteractiveObserver(mobile);restoreViewState();
                    return true;
                  };

                  document.querySelectorAll('p,div,strong,label').forEach(function(el){var value=norm(text(el));if(value.indexOf('tipp: diese seite als favorit')===0&&value.length<350)el.style.display='none';});
                  buildMobile();setTimeout(buildMobile,250);setTimeout(buildMobile,900);
                })();
                """;

        String scheme = "#11171C".equalsIgnoreCase(background) ? "dark" : "light";
        return template
                .replace("__BG__", escapeJs(background))
                .replace("__CARD__", escapeJs(card))
                .replace("__SOFT__", escapeJs(soft))
                .replace("__TEXT__", escapeJs(text))
                .replace("__MUTED__", escapeJs(muted))
                .replace("__BORDER__", escapeJs(border))
                .replace("__LINK__", escapeJs(link))
                .replace("__SCHEME__", scheme);
    }

    private static String escapeJs(String value) {
        return value.replace("\\", "\\\\").replace("'", "\\'").replace("\r", "").replace("\n", "\\n");
    }
}
