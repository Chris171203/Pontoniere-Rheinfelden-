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

    static String formatPersonDisplayName(String value) {
        if (value == null) return "";
        String clean = value
                .replaceAll("(?<=[\\p{Ll}])(?=[\\p{Lu}])", " ")
                .replaceAll("(?<=[\\p{Lu}]{2})(?=[\\p{Lu}][\\p{Ll}])", " ")
                .replaceAll("\\s*,\\s*", ", ")
                .replaceAll("\\s+", " ")
                .trim();
        if (clean.isEmpty() || clean.matches("(?i)(?:Person|Teilnehmer)\\s+\\d+")) return clean;
        int comma = clean.indexOf(',');
        if (comma >= 0) {
            String family = clean.substring(0, comma).trim();
            String given = clean.substring(comma + 1).trim();
            return given.isEmpty() ? family : family + ", " + given;
        }
        String[] parts = clean.split(" ");
        if (parts.length < 2) return clean;
        StringBuilder given = new StringBuilder();
        for (int index = 1; index < parts.length; index++) {
            if (given.length() > 0) given.append(' ');
            given.append(parts[index]);
        }
        return parts[0] + ", " + given;
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
                  var PEOPLE_KEY='pfvr-attendance-people-v4';
                  var LEGACY_PEOPLE_KEY='pfvr-attendance-people-v3';
                  var RESTORE_KEY='pfvr-attendance-restore-v4';
                  var sourceTableRef=null;
                  var sourcePeopleObserver=null;
                  var norm=function(value){return (value||'').replace(/\\s+/g,' ').trim().toLowerCase();};
                  var text=function(el){return (el&&((el.innerText||el.textContent)||''))||'';};
                  var element=function(tag,className){var node=document.createElement(tag);if(className)node.className=className;return node;};
                  var isInteractive=function(el){return !!(el&&el.closest&&el.closest('button,input,select,textarea,a'))};
                  var bulkPeopleAction=function(label){
                    var value=norm(label);
                    return value.indexOf('alle personen anzeigen')>=0||value.indexOf('alle personen hinzufügen')>=0||value.indexOf('alle personen hinzuf')>=0||value.indexOf('alle personen einblenden')>=0||value.indexOf('alle anzeigen')>=0||value.indexOf('alle hinzufügen')>=0||value.indexOf('alle hinzuf')>=0||value.indexOf('alle einblenden')>=0;
                  };
                  var controlLabel=function(control){return ((control&&((control.innerText||control.value)||control.textContent))||'').trim();};
                  var suppressBulkPeopleActions=function(root){
                    (root||document).querySelectorAll('button,input[type=submit],input[type=button],a,.btn').forEach(function(control){
                      if(!bulkPeopleAction(controlLabel(control)))return;
                      control.style.setProperty('display','none','important');control.setAttribute('aria-hidden','true');control.setAttribute('tabindex','-1');
                    });
                  };
                  var bindBulkPeopleGuard=function(){
                    if(window.__pfvrBulkPeopleGuardBound)return;window.__pfvrBulkPeopleGuardBound=true;
                    document.addEventListener('click',function(event){
                      var control=event.target&&event.target.closest&&event.target.closest('button,input[type=submit],input[type=button],a,.btn');
                      if(!control||!bulkPeopleAction(controlLabel(control)))return;
                      event.preventDefault();event.stopPropagation();event.stopImmediatePropagation();suppressBulkPeopleActions(document);
                    },true);
                    suppressBulkPeopleActions(document);
                    if(window.MutationObserver){var observer=new MutationObserver(function(){suppressBulkPeopleActions(document);});observer.observe(document.documentElement,{subtree:true,childList:true});}
                  };

                  var css=`
                  html{color-scheme:__SCHEME__!important;}
                  html,body{width:100%!important;max-width:100%!important;overflow-x:hidden!important;}
                  body{margin:0!important;padding:10px 10px 34px!important;box-sizing:border-box!important;background:${COLORS.background}!important;color:${COLORS.text}!important;font-family:Arial,sans-serif!important;font-size:16px!important;}
                  header,nav,footer,.navbar,.site-header,.site-footer{display:none!important;}
                  p,span,div,label,strong{color:${COLORS.text}!important;}small{color:${COLORS.muted}!important;}a{color:${COLORS.link}!important;}
                  select,input[type=text],input[type=number]{background:${COLORS.soft}!important;color:${COLORS.text}!important;border:1px solid ${COLORS.border}!important;border-radius:12px!important;padding:10px!important;min-height:44px!important;box-sizing:border-box!important;}
                  button,input[type=submit],input[type=button],a.btn,.btn{min-height:46px!important;border:0!important;border-radius:12px!important;padding:9px 12px!important;font-size:15px!important;font-weight:700!important;line-height:1.25!important;box-shadow:none!important;white-space:normal!important;overflow-wrap:break-word!important;word-break:normal!important;}
                  .pfvr-attendance-source{display:none!important;}
                  .pfvr-attendance-mobile{display:flex!important;flex-direction:column!important;gap:8px!important;width:100%!important;max-width:100%!important;box-sizing:border-box!important;}
                  .pfvr-person-tools{display:none!important;position:fixed!important;z-index:10002!important;left:12px!important;right:12px!important;top:12px!important;max-height:calc(100vh - 24px)!important;overflow-y:auto!important;flex-direction:column!important;gap:8px!important;background:${COLORS.card}!important;border:1px solid ${COLORS.border}!important;border-radius:16px!important;padding:12px!important;box-sizing:border-box!important;box-shadow:0 16px 46px rgba(0,0,0,.38)!important;}
                  .pfvr-person-tools.open{display:flex!important;}
                  .pfvr-person-tools-backdrop{position:fixed!important;z-index:10001!important;inset:0!important;background:rgba(0,0,0,.52)!important;}
                  .pfvr-person-tools-head{display:flex!important;align-items:center!important;justify-content:space-between!important;gap:10px!important;}
                  .pfvr-person-tools-title{font-size:18px!important;font-weight:700!important;}
                  .pfvr-person-tools-toggle{min-height:38px!important;padding:6px 10px!important;background:${COLORS.soft}!important;color:${COLORS.text}!important;}
                  .pfvr-person-tools-body{display:flex!important;flex-direction:column!important;gap:8px!important;padding-top:2px!important;}
                  .pfvr-person-tools-body select{width:100%!important;max-width:100%!important;}
                  .pfvr-managed-people{display:flex!important;flex-direction:column!important;gap:4px!important;border-top:1px solid ${COLORS.border}!important;padding-top:7px!important;margin-top:2px!important;}
                  .pfvr-managed-people-title{font-size:12px!important;font-weight:700!important;color:${COLORS.muted}!important;}
                  .pfvr-managed-person{display:flex!important;align-items:center!important;gap:6px!important;min-width:0!important;padding:4px 0!important;}
                  .pfvr-managed-person-name{flex:1 1 auto!important;min-width:0!important;font-size:13px!important;font-weight:600!important;overflow-wrap:break-word!important;}
                  .pfvr-managed-person-actions{display:flex!important;align-items:center!important;gap:4px!important;flex:0 0 auto!important;}
                  .pfvr-managed-person-actions button,.pfvr-managed-person-actions input[type=submit],.pfvr-managed-person-actions input[type=button],.pfvr-managed-person-actions a{min-height:34px!important;min-width:34px!important;padding:5px 7px!important;font-size:12px!important;border-radius:9px!important;}
                  .pfvr-managed-action-label{font-size:10px!important;color:${COLORS.muted}!important;}
                  .pfvr-people-recovery{display:flex!important;flex-direction:column!important;gap:6px!important;margin-top:4px!important;padding:9px!important;border:1px solid ${COLORS.border}!important;border-radius:12px!important;background:${COLORS.soft}!important;}
                  .pfvr-people-recovery-title{font-size:12px!important;font-weight:700!important;}
                  .pfvr-people-recovery-note{font-size:11px!important;color:${COLORS.muted}!important;line-height:1.3!important;}
                  .pfvr-people-recovery button{width:100%!important;min-height:42px!important;background:${COLORS.link}!important;color:#fff!important;font-size:12px!important;}
                  .pfvr-matrix-head-scroll{position:sticky!important;top:0!important;z-index:8!important;display:block!important;width:100%!important;max-width:100%!important;overflow-x:hidden!important;overflow-y:hidden!important;pointer-events:none!important;box-sizing:border-box!important;padding:0 0 6px!important;background:${COLORS.background}!important;scrollbar-width:none!important;}
                  .pfvr-matrix-head-scroll::-webkit-scrollbar{display:none!important;}
                  .pfvr-matrix-scroll{display:block!important;width:100%!important;max-width:100%!important;overflow-x:auto!important;overflow-y:visible!important;box-sizing:border-box!important;padding:0 0 4px!important;-webkit-overflow-scrolling:touch!important;overscroll-behavior-x:contain!important;}
                  .pfvr-attendance-head,.pfvr-attendance-matrix{--pfvr-day-col:96px;--pfvr-person-col:clamp(104px,calc((100vw - 128px)/2),138px);display:grid!important;gap:6px!important;align-items:stretch!important;width:max-content!important;min-width:100%!important;box-sizing:border-box!important;}
                  .pfvr-matrix-corner,.pfvr-person-header,.pfvr-day-meta,.pfvr-person-cell{box-sizing:border-box!important;border:1px solid ${COLORS.border}!important;border-radius:13px!important;background:${COLORS.card}!important;min-width:0!important;}
                  .pfvr-matrix-corner{position:sticky!important;left:0!important;z-index:10!important;width:var(--pfvr-day-col)!important;padding:8px!important;font-size:11px!important;font-weight:700!important;color:${COLORS.muted}!important;display:flex!important;align-items:center!important;box-shadow:3px 3px 8px rgba(0,0,0,.10)!important;}
                  .pfvr-person-header{padding:8px 7px!important;font-size:12px!important;font-weight:700!important;line-height:1.15!important;overflow-wrap:break-word!important;word-break:normal!important;display:-webkit-box!important;-webkit-box-orient:vertical!important;-webkit-line-clamp:2!important;overflow:hidden!important;min-height:40px!important;box-shadow:0 3px 8px rgba(0,0,0,.10)!important;}
                  .pfvr-day-meta{position:sticky!important;left:0!important;z-index:3!important;width:var(--pfvr-day-col)!important;padding:8px!important;font-size:12px!important;overflow-wrap:break-word!important;word-break:normal!important;box-shadow:3px 0 8px rgba(0,0,0,.04)!important;}
                  .pfvr-day-meta>*{max-width:100%!important;box-sizing:border-box!important;white-space:normal!important;overflow-wrap:break-word!important;word-break:normal!important;}
                  .pfvr-person-cell{width:var(--pfvr-person-col)!important;padding:8px!important;display:flex!important;flex-direction:column!important;justify-content:flex-end!important;}
                  .pfvr-name-small{font-size:10px!important;}
                  .pfvr-name-tiny{font-size:9px!important;letter-spacing:-.1px!important;}
                  .pfvr-local-remove{background:#6D7880!important;color:#fff!important;min-height:32px!important;padding:4px 8px!important;font-size:11px!important;}
                  .pfvr-primary-label{font-size:10px!important;color:${COLORS.muted}!important;}
                  .pfvr-person-control{display:flex!important;flex-direction:column!important;justify-content:flex-end!important;gap:6px!important;margin-top:auto!important;min-width:0!important;min-height:64px!important;width:100%!important;}
                  .pfvr-person-control>*{max-width:100%!important;box-sizing:border-box!important;}
                  .pfvr-person-control button,.pfvr-person-control input[type=submit],.pfvr-person-control input[type=button],.pfvr-person-control a.btn,.pfvr-person-control .btn,.pfvr-person-control select{width:100%!important;min-height:60px!important;padding:10px 8px!important;font-size:13px!important;border-radius:10px!important;}
                  .pfvr-empty-status{font-size:11px!important;color:${COLORS.muted}!important;}
                  .pfvr-attendance-status{display:block!important;width:max-content!important;max-width:100%!important;padding:4px 6px!important;margin:0 0 4px!important;font-size:11px!important;border-radius:8px!important;font-weight:700!important;line-height:1.2!important;white-space:normal!important;overflow-wrap:break-word!important;word-break:normal!important;}
                  .pfvr-attendance-detail{display:block!important;margin:0 0 3px!important;font-size:11px!important;line-height:1.3!important;color:${COLORS.text}!important;white-space:normal!important;overflow-wrap:break-word!important;word-break:normal!important;}
                  @media(max-width:340px){.pfvr-attendance-head,.pfvr-attendance-matrix{--pfvr-day-col:84px;--pfvr-person-col:102px;}.pfvr-day-meta{padding:7px!important;font-size:11px!important;}.pfvr-person-header{font-size:11px!important;padding:7px 5px!important;}.pfvr-person-cell{padding:6px!important;}.pfvr-name-small{font-size:9px!important;}.pfvr-name-tiny{font-size:8.5px!important;}.pfvr-person-control{min-height:58px!important;}.pfvr-person-control button,.pfvr-person-control input[type=submit],.pfvr-person-control input[type=button],.pfvr-person-control a.btn,.pfvr-person-control .btn,.pfvr-person-control select{min-height:54px!important;font-size:12px!important;padding:8px 5px!important;}}
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
                  var cleanPersonName=function(value){
                    return (value||'')
                      .replace(/[\\u{1F300}-\\u{1FAFF}]/gu,' ')
                      .replace(/([a-zäöüß])([A-ZÄÖÜ])/g,'$1 $2')
                      .replace(/([A-ZÄÖÜ]{2,})([A-ZÄÖÜ][a-zäöüß])/g,'$1 $2')
                      .replace(/\\s*,\\s*/g,', ')
                      .replace(/\\s+/g,' ')
                      .trim();
                  };
                  var isPlaceholderPersonName=function(value){return /^(?:person|teilnehmer)\\s+\\d+$/i.test(cleanPersonName(value));};
                  var formatPersonName=function(value){
                    var clean=cleanPersonName(value);if(!clean||isPlaceholderPersonName(clean))return clean;
                    var comma=clean.indexOf(',');
                    if(comma>=0){var family=clean.slice(0,comma).trim(),given=clean.slice(comma+1).trim();return given?family+', '+given:family;}
                    var parts=clean.split(' ').filter(Boolean);if(parts.length<2)return clean;
                    return parts.shift()+', '+parts.join(' ');
                  };
                  var personCellText=function(cell){
                    if(!cell)return '';
                    var clone=cell.cloneNode(true);
                    clone.querySelectorAll('button,input,select,textarea,svg,img,picture,script,style').forEach(function(node){node.remove();});
                    var chunks=[],walker=document.createTreeWalker(clone,NodeFilter.SHOW_TEXT);
                    while(walker.nextNode()){
                      var chunk=cleanPersonName(walker.currentNode.nodeValue||'');if(!chunk)continue;
                      var normalized=norm(chunk);
                      if(/^(?:entfernen|löschen|loeschen|bearbeiten|kalender|anzeigen|zurück|zurueck)$/.test(normalized))continue;
                      chunks.push(chunk);
                    }
                    var candidate=cleanPersonName(chunks.join(' '));if(candidate)return candidate;
                    var attributes=['data-person-name','data-name','aria-label','title'];
                    var nodes=[cell].concat(Array.from(cell.querySelectorAll('[data-person-name],[data-name],[aria-label],[title]')));
                    for(var nodeIndex=0;nodeIndex<nodes.length;nodeIndex++){
                      for(var attrIndex=0;attrIndex<attributes.length;attrIndex++){
                        var attr=cleanPersonName(nodes[nodeIndex].getAttribute&&nodes[nodeIndex].getAttribute(attributes[attrIndex]));
                        if(attr&&!/^(?:entfernen|löschen|loeschen|bearbeiten|kalender|anzeigen)$/i.test(attr))return attr;
                      }
                    }
                    return '';
                  };
                  var moveChildren=function(from,to){while(from&&from.firstChild)to.appendChild(from.firstChild);};
                  var personKey=function(value){return cleanPersonName(value).toLowerCase();};
                  var fitPersonName=function(el,value){
                    var clean=formatPersonName(value);el.textContent=clean;el.title=clean;
                    if(clean.length>28)el.classList.add('pfvr-name-tiny');
                    else if(clean.length>19)el.classList.add('pfvr-name-small');
                  };
                  var savePeopleState=function(state){
                    try{localStorage.setItem(PEOPLE_KEY,JSON.stringify(state));}catch(ignore){}
                  };
                  var personTokenKey=function(value){
                    var normalized=personKey(value);try{normalized=normalized.normalize('NFD');}catch(ignore){}
                    return normalized.replace(/[^a-z0-9\\s]/g,' ').replace(/\\s+/g,' ').trim().split(' ').filter(function(token){return token.length>1;}).sort().join('|');
                  };
                  var samePersonName=function(left,right){
                    var leftKey=personKey(left),rightKey=personKey(right);if(!leftKey||!rightKey)return false;if(leftKey===rightKey)return true;
                    var leftTokens=personTokenKey(left),rightTokens=personTokenKey(right);return !!leftTokens&&leftTokens===rightTokens;
                  };
                  var listHasPerson=function(list,name){return Array.isArray(list)&&list.some(function(saved){return samePersonName(saved,name);});};
                  var isHiddenPerson=function(state,name){return !!(state&&listHasPerson(state.hidden,name));};
                  var dedupePeople=function(list){var result=[];(list||[]).forEach(function(name){var clean=cleanPersonName(name);if(clean&&!isPlaceholderPersonName(clean)&&!listHasPerson(result,clean))result.push(clean);});return result;};
                  var addDesiredPerson=function(state,name,restoreValue){
                    var clean=cleanPersonName(name);if(!state||!clean)return false;
                    if(!Array.isArray(state.desired))state.desired=[];if(!Array.isArray(state.hidden))state.hidden=[];if(!state.restoreValues||typeof state.restoreValues!=='object')state.restoreValues={};
                    state.hidden=state.hidden.filter(function(saved){return !samePersonName(saved,clean);});
                    var index=state.desired.findIndex(function(saved){return samePersonName(saved,clean);});
                    var previous=index>=0?state.desired[index]:'',previousKey=personKey(previous),key=personKey(clean);
                    if(index<0)state.desired.push(clean);else state.desired[index]=clean;
                    if(restoreValue!==undefined&&restoreValue!==null&&String(restoreValue)!=='')state.restoreValues[key]=String(restoreValue);
                    else if(previousKey&&previousKey!==key&&state.restoreValues[previousKey]){state.restoreValues[key]=state.restoreValues[previousKey];delete state.restoreValues[previousKey];}
                    return true;
                  };
                  var removeDesiredPerson=function(state,name){
                    var clean=cleanPersonName(name);if(!state||!clean||samePersonName(clean,state.primary))return false;
                    state.desired=(state.desired||[]).filter(function(saved){return !samePersonName(saved,clean);});
                    if(!listHasPerson(state.hidden,clean))state.hidden.push(clean);
                    state.rowNames=[];state.pendingAdd=null;
                    Object.keys(state.restoreValues||{}).forEach(function(key){if(samePersonName(key,clean))delete state.restoreValues[key];});
                    savePeopleState(state);return true;
                  };
                  var selectedOptionValue=function(select){
                    if(!select||select.selectedIndex<0||!select.options)return '';var option=select.options[select.selectedIndex];return String((option&&option.value)||'');
                  };
                  var optionNameForRow=function(row,select){
                    if(!row||!select||!select.options)return '';
                    var values=[];
                    var addValue=function(value){value=String(value||'').trim();if(value&&values.indexOf(value)<0)values.push(value);};
                    [row,row.cells&&row.cells[0]].filter(Boolean).forEach(function(node){['data-person-id','data-id-person','data-person','data-id'].forEach(function(attr){addValue(node.getAttribute&&node.getAttribute(attr));});});
                    row.querySelectorAll('input[type=hidden],a[href],[data-person-id],[data-id-person]').forEach(function(node){
                      var marker=norm((node.getAttribute&&((node.getAttribute('name')||'')+' '+(node.getAttribute('id')||'')))||'');
                      if(node.tagName==='INPUT'&&(marker.indexOf('person')>=0||marker.indexOf('teilnehmer')>=0))addValue(node.value);
                      ['data-person-id','data-id-person'].forEach(function(attr){addValue(node.getAttribute&&node.getAttribute(attr));});
                      if(node.tagName==='A')try{var url=new URL(node.href,location.href);['id_person','person_id','person'].forEach(function(key){addValue(url.searchParams.get(key));});}catch(ignore){}
                    });
                    for(var optionIndex=0;optionIndex<select.options.length;optionIndex++){
                      var option=select.options[optionIndex];if(values.indexOf(String(option.value||'').trim())>=0)return cleanPersonName(option.textContent||option.value||'');
                    }
                    return '';
                  };
                  var resolvePersonNames=function(rows,select,state){
                    var stored=[];
                    if(state){stored=(state.rowNames||[]).concat(state.desired||[]).concat(state.hidden||[]).filter(function(name){return name&&!isPlaceholderPersonName(name);});}
                    return rows.map(function(row,index){
                      var value=personCellText(row.cells&&row.cells[0]);
                      if(!value)value=optionNameForRow(row,select);
                      if(!value&&stored[index])value=stored[index];
                      return value||('Teilnehmer '+(index+1));
                    });
                  };
                  var currentSourceNames=function(table,select,state){
                    if(!table)return [];var rows=Array.from(table.rows||[]).slice(1).filter(function(row){return row.cells&&row.cells.length>=2;});return resolvePersonNames(rows,select,state);
                  };
                  var rememberPendingPerson=function(state,table,select,expectedName){
                    if(!state)return;state.pendingAdd={before:currentSourceNames(table,select,state),optionValue:selectedOptionValue(select),optionText:cleanPersonName(expectedName||''),time:Date.now()};savePeopleState(state);
                  };
                  var adoptCurrentPeople=function(state,currentNames){
                    if(!state)return;
                    var pending=state.pendingAdd;
                    if(pending&&Date.now()-(pending.time||0)<=120000){
                      var before=Array.isArray(pending.before)?pending.before:[];
                      var added=currentNames.filter(function(name){return !before.some(function(previous){return samePersonName(previous,name);});});
                      if(!added.length&&pending.optionText){var existing=currentNames.find(function(name){return samePersonName(name,pending.optionText);});if(existing)added=[existing];}
                      if(added.length){added.forEach(function(name){addDesiredPerson(state,name,pending.optionValue);});state.pendingAdd=null;}
                    }else if(pending){state.pendingAdd=null;}
                    currentNames.forEach(function(name){if(!isHiddenPerson(state,name))addDesiredPerson(state,name);});
                    savePeopleState(state);
                  };
                  var readPeopleState=function(){
                    var state=null;
                    try{var raw=localStorage.getItem(PEOPLE_KEY);if(raw)state=JSON.parse(raw);else{var legacy=localStorage.getItem(LEGACY_PEOPLE_KEY);if(legacy)state=JSON.parse(legacy);}}catch(ignore){}
                    return state;
                  };
                  var loadPeopleState=function(currentNames,seed){
                    var state=seed||readPeopleState();
                    var migrated=!!(state&&state.version!==4);
                    if(!state||!Array.isArray(state.desired))state={version:4,primary:currentNames[0]||'',desired:[],hidden:[],restoreValues:{},pendingAdd:null,rowNames:[]};
                    state.version=4;if(migrated)state.hidden=[];if(!Array.isArray(state.hidden))state.hidden=[];if(!Array.isArray(state.rowNames))state.rowNames=[];if(!state.restoreValues||typeof state.restoreValues!=='object')state.restoreValues={};
                    state.primary=cleanPersonName(state.primary);
                    if((!state.primary||isPlaceholderPersonName(state.primary))&&currentNames.length)state.primary=currentNames[0];
                    state.hidden=dedupePeople(state.hidden).filter(function(name){return !samePersonName(name,state.primary);});state.desired=dedupePeople(state.desired);state.rowNames=dedupePeople(state.rowNames);
                    if(state.primary&&!listHasPerson(state.desired,state.primary))state.desired.unshift(state.primary);
                    adoptCurrentPeople(state,currentNames);state.rowNames=currentNames.filter(function(name){return name&&!isPlaceholderPersonName(name);});savePeopleState(state);return state;
                  };
                  var findPersonToolScope=function(){
                    var candidates=Array.from(document.querySelectorAll('label,p,span,div')).filter(function(el){var value=norm(text(el));return value.indexOf('person zur liste hinzuzufügen')===0&&value.length<120;});
                    if(candidates.length){
                      var anchor=candidates[0],scope=anchor.parentElement;
                      for(var depth=0;scope&&depth<4;depth++,scope=scope.parentElement){if(scope===document.body)break;var select=scope.querySelector('select');if(select)return {anchor:anchor,scope:scope,select:select};}
                    }
                    var best=null,bestScore=-1;
                    document.querySelectorAll('select').forEach(function(select){
                      if(select.closest('table'))return;var options=Array.from(select.options||[]),statusOptions=options.filter(function(option){return !!statusForValue(option.textContent||option.value||'');}).length;
                      if(options.length<3||statusOptions>Math.max(1,options.length/3))return;
                      var marker=norm((select.name||'')+' '+(select.id||'')+' '+text(select.parentElement));var score=options.length+(marker.indexOf('person')>=0?20:0)+(marker.indexOf('hinzuf')>=0?20:0);
                      if(score>bestScore){bestScore=score;best=select;}
                    });
                    if(!best)return null;var fallbackScope=best.closest('form')||best.parentElement;return {anchor:null,scope:fallbackScope,select:best};
                  };
                  var selectedOptionName=function(select){
                    if(!select||select.selectedIndex<0||!select.options)return '';
                    var option=select.options[select.selectedIndex];return cleanPersonName((option&&(option.textContent||option.value))||'');
                  };
                  var findOptionForPerson=function(select,name,state){
                    if(!select||!select.options)return -1;var savedValue=state&&state.restoreValues?state.restoreValues[personKey(name)]:'';
                    if(savedValue){for(var byValue=0;byValue<select.options.length;byValue++){if(String(select.options[byValue].value||'')===String(savedValue))return byValue;}}
                    for(var i=0;i<select.options.length;i++){var candidate=select.options[i].textContent||select.options[i].value||'';if(samePersonName(candidate,name))return i;}
                    return -1;
                  };
                  var tryRestoreMissingPerson=function(select,currentNames,state){
                    if(!select||!state||!state.desired.length)return false;
                    var missing=state.desired.find(function(name){return !isHiddenPerson(state,name)&&!currentNames.some(function(current){return samePersonName(current,name);});});if(!missing)return false;
                    var optionIndex=findOptionForPerson(select,missing,state);if(optionIndex<0)return false;
                    var attempt={name:'',count:0,time:0};
                    try{var raw=sessionStorage.getItem(RESTORE_KEY);if(raw)attempt=JSON.parse(raw)||attempt;}catch(ignore){}
                    if(personKey(attempt.name)===personKey(missing)&&attempt.count>=2)return false;
                    if(personKey(attempt.name)===personKey(missing)&&Date.now()-(attempt.time||0)<1200){setTimeout(buildMobile,1250);return true;}
                    attempt={name:missing,count:(personKey(attempt.name)===personKey(missing)?(attempt.count||0)+1:1),time:Date.now()};
                    try{sessionStorage.setItem(RESTORE_KEY,JSON.stringify(attempt));}catch(ignore){}
                    select.selectedIndex=optionIndex;rememberPendingPerson(state,sourceTableRef,select,missing);
                    select.dispatchEvent(new Event('input',{bubbles:true}));
                    select.dispatchEvent(new Event('change',{bubbles:true}));
                    setTimeout(buildMobile,1400);return true;
                  };
                  var updateMatrixColumns=function(matrix){
                    if(!matrix)return;
                    var matrixHead=document.querySelector('.pfvr-attendance-head');
                    var count=Array.from((matrixHead||document).querySelectorAll('.pfvr-person-header')).filter(function(header){return header.style.display!=='none';}).length;
                    var columns='var(--pfvr-day-col) repeat('+count+',var(--pfvr-person-col))';
                    matrix.style.gridTemplateColumns=columns;if(matrixHead)matrixHead.style.gridTemplateColumns=columns;
                  };
                  var setPersonColumnHidden=function(name,hidden){
                    var matrix=document.querySelector('.pfvr-attendance-matrix');if(!matrix)return false;
                    var mobile=document.querySelector('.pfvr-attendance-mobile'),changed=false;
                    Array.from((mobile||matrix).querySelectorAll('[data-pfvr-person]')).forEach(function(el){if(samePersonName(el.getAttribute('data-pfvr-person')||'',name)){el.style.display=hidden?'none':'';changed=true;}});
                    updateMatrixColumns(matrix);return changed;
                  };
                  var showDesiredPerson=function(state,name){
                    if(!state)return;state.hidden=(state.hidden||[]).filter(function(saved){return !samePersonName(saved,name);});addDesiredPerson(state,name);savePeopleState(state);
                  };
                  var appendHiddenPerson=function(list,state,personName){
                    if(!list)return;var key=personKey(personName);if(!key)return;
                    var exists=Array.from(list.querySelectorAll('.pfvr-managed-person')).some(function(line){return samePersonName(line.getAttribute('data-pfvr-managed-person')||'',personName);});if(exists)return;
                    var line=element('div','pfvr-managed-person');line.setAttribute('data-pfvr-managed-person',key);
                    var name=element('div','pfvr-managed-person-name');fitPersonName(name,personName);line.appendChild(name);
                    var show=element('button','pfvr-local-remove');show.type='button';show.textContent='Einblenden';
                    show.addEventListener('click',function(){showDesiredPerson(state,personName);if(!setPersonColumnHidden(personName,false))scheduleParticipantSync(state);appendManagedPerson(document.querySelector('.pfvr-managed-people-visible'),state,personName);line.remove();});
                    line.appendChild(show);list.appendChild(line);
                  };
                  var appendManagedPerson=function(list,state,personName){
                    if(!list||isHiddenPerson(state,personName))return;var key=personKey(personName);if(!key)return;
                    var exists=Array.from(list.querySelectorAll('.pfvr-managed-person')).some(function(line){return samePersonName(line.getAttribute('data-pfvr-managed-person')||'',personName);});if(exists)return;
                    var line=element('div','pfvr-managed-person');line.setAttribute('data-pfvr-managed-person',key);
                    var name=element('div','pfvr-managed-person-name');fitPersonName(name,personName);line.appendChild(name);
                    if(samePersonName(personName,state.primary)){
                      var primary=element('span','pfvr-primary-label');primary.textContent='Standard';line.appendChild(primary);
                    }else{
                      var remove=element('button','pfvr-local-remove');remove.type='button';remove.textContent='Entfernen';remove.setAttribute('aria-label','Person aus Ansicht entfernen: '+formatPersonName(personName));
                      remove.addEventListener('click',function(){if(!removeDesiredPerson(state,personName))return;saveViewState();closePersonManager();var base=window.__pfvrBaseInternalUrl||'';if(base){window.location.replace(base);return;}setPersonColumnHidden(personName,true);line.remove();});
                      line.appendChild(remove);
                    }
                    list.appendChild(line);
                  };
                  var matrixHasPerson=function(matrix,name){
                    var matrixHead=document.querySelector('.pfvr-attendance-head');if(!matrix||!matrixHead)return false;
                    return Array.from(matrixHead.querySelectorAll('.pfvr-person-header')).some(function(el){return samePersonName(el.getAttribute('data-pfvr-person')||'',name);});
                  };
                  var appendPersonColumn=function(table,row,personName,state){
                    var matrix=document.querySelector('.pfvr-attendance-matrix'),matrixHead=document.querySelector('.pfvr-attendance-head');if(!table||!row||!matrix||!matrixHead||matrixHasPerson(matrix,personName))return false;
                    var rows=Array.from(table.rows||[]),sourceHeader=rows[0];if(!sourceHeader||!sourceHeader.cells||sourceHeader.cells.length<2)return false;
                    var metas=Array.from(matrix.querySelectorAll('.pfvr-day-meta'));if(metas.length!==sourceHeader.cells.length-1)return false;
                    var key=personKey(personName),personHeader=element('div','pfvr-person-header');fitPersonName(personHeader,personName);personHeader.setAttribute('data-pfvr-person',key);
                    matrixHead.appendChild(personHeader);
                    for(var column=1;column<sourceHeader.cells.length;column++){
                      var cell=element('div','pfvr-person-cell');cell.setAttribute('data-pfvr-person',key);
                      var control=element('div','pfvr-person-control');if(row.cells[column])moveChildren(row.cells[column],control);
                      if(!control.textContent.trim()&&!control.querySelector('button,input,select,a')){var empty=element('span','pfvr-empty-status');empty.textContent='Keine Auswahl für diesen Termin';control.appendChild(empty);}
                      cell.appendChild(control);matrix.insertBefore(cell,metas[column]||null);
                    }
                    updateMatrixColumns(matrix);
                    appendManagedPerson(document.querySelector('.pfvr-managed-people-visible'),state,personName);
                    splitStatusText(matrix);refreshInteractiveSoon(matrix);return true;
                  };
                  var syncAddedParticipants=function(state){
                    var table=sourceTableRef;
                    if(!table||!table.isConnected){
                      var fresh=findTable();if(!fresh)return false;
                      var mobile=document.querySelector('.pfvr-attendance-mobile');if(mobile)mobile.remove();
                      sourceTableRef=null;buildMobile();return true;
                    }
                    var rows=Array.from(table.rows||[]);if(rows.length<2||!rows[0].cells)return false;
                    var headerFresh=Array.from(rows[0].cells||[]).slice(1).some(function(cell){return !!(text(cell).trim()||cell.children.length);});
                    if(headerFresh&&document.querySelector('.pfvr-attendance-mobile')){
                      var mobile=document.querySelector('.pfvr-attendance-mobile');if(mobile)mobile.remove();table.classList.remove('pfvr-attendance-source');sourceTableRef=null;buildMobile();return true;
                    }
                    var participantRows=rows.slice(1).filter(function(row){return row.cells&&row.cells.length>=2;});
                    var toolInfo=findPersonToolScope(),names=resolvePersonNames(participantRows,toolInfo&&toolInfo.select,state);
                    adoptCurrentPeople(state,names);state.rowNames=names.filter(function(name){return !isPlaceholderPersonName(name);});savePeopleState(state);
                    var added=false;participantRows.forEach(function(row,index){var name=names[index];if(!isHiddenPerson(state,name)&&appendPersonColumn(table,row,name,state))added=true;});return added;
                  };
                  var scheduleParticipantSync=function(state){[180,550,1200,2500].forEach(function(delay){setTimeout(function(){syncAddedParticipants(state);},delay);});};
                  var bindSourcePeopleObserver=function(table,state){
                    if(!table||!window.MutationObserver)return;if(sourcePeopleObserver)sourcePeopleObserver.disconnect();
                    var scheduled=false;sourcePeopleObserver=new MutationObserver(function(){if(scheduled)return;scheduled=true;setTimeout(function(){scheduled=false;syncAddedParticipants(state);},90);});
                    sourcePeopleObserver.observe(table,{subtree:true,childList:true});
                  };

                  var closePersonManager=function(){var panel=document.querySelector('.pfvr-person-tools');if(panel)panel.classList.remove('open');var backdrop=document.querySelector('.pfvr-person-tools-backdrop');if(backdrop)backdrop.remove();};
                  var clearLocalPeopleState=function(){
                    try{localStorage.removeItem(PEOPLE_KEY);localStorage.removeItem(LEGACY_PEOPLE_KEY);}catch(ignore){}
                    try{sessionStorage.removeItem(RESTORE_KEY);sessionStorage.removeItem(STORAGE_KEY);}catch(ignore){}
                  };
                  var resetPeopleViewFromBase=function(){
                    var base=window.__pfvrBaseInternalUrl||'';if(!base)return false;
                    clearLocalPeopleState();closePersonManager();
                    try{window.location.replace(base);}catch(error){window.location.href=base;}
                    return true;
                  };
                  var appendPeopleRecovery=function(body){
                    if(!body)return;
                    var recovery=element('div','pfvr-people-recovery');
                    var title=element('div','pfvr-people-recovery-title');title.textContent='Ansicht bereinigen';recovery.appendChild(title);
                    var note=element('div','pfvr-people-recovery-note');note.textContent='Falls die Personenansicht festhängt oder zu viele Personen geladen wurden, kann sie aus dem gespeicherten persönlichen Initiallink vollständig neu aufgebaut werden.';recovery.appendChild(note);
                    var reset=element('button');reset.type='button';reset.textContent='Aus Initiallink neu aufbauen';
                    reset.addEventListener('click',function(){
                      if(reset.dataset.pfvrConfirm!=='1'){
                        reset.dataset.pfvrConfirm='1';reset.textContent='Nochmal tippen: wirklich neu aufbauen';
                        setTimeout(function(){if(reset&&reset.isConnected){reset.dataset.pfvrConfirm='0';reset.textContent='Aus Initiallink neu aufbauen';}},5000);return;
                      }
                      if(!resetPeopleViewFromBase()){reset.dataset.pfvrConfirm='0';reset.textContent='Initiallink nicht verfügbar';}
                    });
                    recovery.appendChild(reset);body.appendChild(recovery);
                  };
                  var findPersonTools=function(toolInfo,state,currentNames){
                    var anchor=toolInfo&&toolInfo.anchor,scope=toolInfo&&toolInfo.scope,select=toolInfo&&toolInfo.select;
                    var panel=element('div','pfvr-person-tools');
                    var head=element('div','pfvr-person-tools-head');
                    var title=element('div','pfvr-person-tools-title');title.textContent='Personen verwalten';
                    var toggle=element('button','pfvr-person-tools-toggle');toggle.type='button';toggle.textContent='Schliessen';toggle.addEventListener('click',closePersonManager);
                    head.appendChild(title);head.appendChild(toggle);panel.appendChild(head);
                    var body=element('div','pfvr-person-tools-body');
                    if(select){
                      var hint=element('div');hint.textContent='Person hinzufügen:';hint.style.color=COLORS.muted;hint.style.fontSize='12px';body.appendChild(hint);
                      var proxy=select.cloneNode(true);proxy.removeAttribute('id');proxy.removeAttribute('name');proxy.removeAttribute('form');proxy.removeAttribute('onchange');proxy.onchange=null;proxy.setAttribute('aria-label','Person hinzufügen');proxy.dataset.pfvrProxy='1';proxy.selectedIndex=select.selectedIndex;
                      body.appendChild(proxy);select.style.setProperty('display','none','important');
                      proxy.addEventListener('change',function(){
                        var chosen=selectedOptionName(proxy);select.selectedIndex=proxy.selectedIndex;try{select.value=proxy.value;}catch(ignore){}
                        rememberPendingPerson(state,sourceTableRef,select,chosen);
                        select.dispatchEvent(new Event('input',{bubbles:true}));select.dispatchEvent(new Event('change',{bubbles:true}));scheduleParticipantSync(state);
                      },false);
                      var actionControls=Array.from(scope.querySelectorAll('button,input[type=submit],input[type=button],a.btn,.btn')).filter(function(control){return control!==select&&norm(control.innerText||control.value).length>0;});
                      actionControls.forEach(function(control){
                        var label=(control.innerText||control.value||'').trim();control.style.setProperty('display','none','important');
                        if(bulkPeopleAction(label))return;
                        var action=element('button');action.type='button';action.textContent=label;action.setAttribute('aria-label',label);
                        action.addEventListener('click',function(){control.click();scheduleParticipantSync(state);});body.appendChild(action);
                      });
                    }else{
                      var unavailable=element('div');unavailable.textContent='Hinzufügen ist auf diesem Seitenstand nicht verfügbar. Vorhandene Zusatzpersonen können weiterhin entfernt werden oder die Ansicht kann unten aus dem Initiallink neu aufgebaut werden.';unavailable.style.color=COLORS.muted;unavailable.style.fontSize='11px';body.appendChild(unavailable);
                    }
                    var note=element('div');note.textContent='Entfernen aktualisiert die Personenliste automatisch.';note.style.color=COLORS.muted;note.style.fontSize='11px';body.appendChild(note);
                    var list=element('div','pfvr-managed-people pfvr-managed-people-visible');
                    var listTitle=element('div','pfvr-managed-people-title');listTitle.textContent='Aktuelle Personen';list.appendChild(listTitle);
                    (currentNames||state.desired).forEach(function(personName){appendManagedPerson(list,state,personName);});body.appendChild(list);
                    appendPeopleRecovery(body);panel.appendChild(body);
                    if(anchor&&scope&&anchor!==scope)anchor.style.display='none';return panel;
                  };

                  var bindHorizontalHeaderSync=function(headScroll,matrixScroll){
                    if(!headScroll||!matrixScroll)return;
                    headScroll.scrollLeft=matrixScroll.scrollLeft;
                    matrixScroll.addEventListener('scroll',function(){headScroll.scrollLeft=matrixScroll.scrollLeft;},{passive:true});
                  };
                  var saveViewState=function(){
                    try{
                      var matrixScroll=document.querySelector('.pfvr-matrix-scroll');
                      sessionStorage.setItem(STORAGE_KEY,JSON.stringify({y:window.scrollY||document.documentElement.scrollTop||0,x:matrixScroll?(matrixScroll.scrollLeft||0):0,time:Date.now()}));
                    }catch(ignore){}
                  };
                  var restoreViewState=function(){
                    try{
                      var raw=sessionStorage.getItem(STORAGE_KEY);if(!raw)return;
                      var state=JSON.parse(raw);if(!state||Date.now()-(state.time||0)>120000)return;
                      requestAnimationFrame(function(){
                        window.scrollTo(0,state.y||0);
                        var matrixScroll=document.querySelector('.pfvr-matrix-scroll');if(matrixScroll)matrixScroll.scrollLeft=state.x||0;
                        var matrixHeadScroll=document.querySelector('.pfvr-matrix-head-scroll');if(matrixHeadScroll)matrixHeadScroll.scrollLeft=state.x||0;
                      });
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
                    suppressBulkPeopleActions(document);
                    if(document.querySelector('.pfvr-attendance-mobile'))return true;
                    var table=findTable();if(!table)return false;sourceTableRef=table;
                    var rows=Array.from(table.rows||[]);if(rows.length<2||!rows[0].cells||rows[0].cells.length<2)return false;
                    var header=rows[0];
                    var allParticipantRows=rows.slice(1).filter(function(row){return row.cells&&row.cells.length>=2;});
                    var toolInfo=findPersonToolScope(),seedState=readPeopleState();
                    var allNames=resolvePersonNames(allParticipantRows,toolInfo&&toolInfo.select,seedState);
                    var peopleState=loadPeopleState(allNames,seedState);
                    if(toolInfo&&tryRestoreMissingPerson(toolInfo.select,allNames,peopleState))return false;
                    try{sessionStorage.removeItem(RESTORE_KEY);}catch(ignore){}
                    var participantRows=[],names=[];
                    allParticipantRows.forEach(function(row,index){if(!isHiddenPerson(peopleState,allNames[index])){participantRows.push(row);names.push(allNames[index]);}});
                    var mobile=element('div','pfvr-attendance-mobile');table.parentNode.insertBefore(mobile,table);
                    var tools=findPersonTools(toolInfo,peopleState,names);if(tools)mobile.appendChild(tools);
                    var columns='var(--pfvr-day-col) repeat('+names.length+',var(--pfvr-person-col))';
                    var matrixHeadScroll=element('div','pfvr-matrix-head-scroll');
                    var matrixHead=element('div','pfvr-attendance-head');matrixHead.style.gridTemplateColumns=columns;
                    var corner=element('div','pfvr-matrix-corner');corner.textContent='Termin';matrixHead.appendChild(corner);
                    names.forEach(function(personName){var personHeader=element('div','pfvr-person-header');fitPersonName(personHeader,personName);personHeader.setAttribute('data-pfvr-person',personKey(personName));matrixHead.appendChild(personHeader);});
                    matrixHeadScroll.appendChild(matrixHead);mobile.appendChild(matrixHeadScroll);
                    var matrixScroll=element('div','pfvr-matrix-scroll');
                    var matrix=element('div','pfvr-attendance-matrix');matrix.style.gridTemplateColumns=columns;
                    for(var column=1;column<header.cells.length;column++){
                      var meta=element('div','pfvr-day-meta');moveChildren(header.cells[column],meta);matrix.appendChild(meta);
                      participantRows.forEach(function(row,rowIndex){
                        var cell=element('div','pfvr-person-cell');cell.setAttribute('data-pfvr-person',personKey(names[rowIndex]));
                        var control=element('div','pfvr-person-control');
                        if(row.cells[column])moveChildren(row.cells[column],control);
                        if(!control.textContent.trim()&&!control.querySelector('button,input,select,a')){var empty=element('span','pfvr-empty-status');empty.textContent='Keine Auswahl für diesen Termin';control.appendChild(empty);}
                        cell.appendChild(control);matrix.appendChild(cell);
                      });
                    }
                    matrixScroll.appendChild(matrix);mobile.appendChild(matrixScroll);bindHorizontalHeaderSync(matrixHeadScroll,matrixScroll);
                    table.classList.add('pfvr-attendance-source');
                    splitStatusText(mobile);styleInteractive(mobile);bindStatePreservation(mobile);bindInteractiveObserver(mobile);bindSourcePeopleObserver(table,peopleState);restoreViewState();
                    return true;
                  };
                  var buildFallbackPeopleManager=function(){
                    var state=readPeopleState();
                    state=loadPeopleState([],state);
                    var names=dedupePeople(([state.primary||'']).concat(state.rowNames||[]).concat(state.desired||[]));
                    var panel=findPersonTools(null,state,names);document.body.appendChild(panel);return panel;
                  };
                  window.pfvrOpenPeopleManager=function(){
                    var panel=document.querySelector('.pfvr-person-tools');
                    if(!panel){buildMobile();panel=document.querySelector('.pfvr-person-tools');}
                    if(!panel)panel=buildFallbackPeopleManager();
                    if(!panel)return false;
                    closePersonManager();var backdrop=element('div','pfvr-person-tools-backdrop');backdrop.addEventListener('click',closePersonManager);document.body.appendChild(backdrop);panel.classList.add('open');panel.scrollTop=0;return true;
                  };

                  bindBulkPeopleGuard();
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
