from pathlib import Path

path = Path('Android/app/src/main/java/ch/pfvr/internapp/InternalAttendanceSkin.java')
text = path.read_text(encoding='utf-8')
old = """                    var datePattern=/^(?:mo|di|mi|do|fr|sa|so)\\.?[,]?\\s*\\d{1,2}\\.\\d{1,2}\\.?$/i;
"""
new = """                    var digitsOnly=function(value){
                      if(!value)return false;
                      for(var index=0;index<value.length;index++){
                        var code=value.charCodeAt(index);
                        if(code<48||code>57)return false;
                      }
                      return true;
                    };
                    var dayDateLabel=function(value){
                      var compact=norm(value).replace(/[,]/g,' ').trim();
                      var pieces=compact.split(' ').filter(function(part){return !!part;});
                      if(pieces.length<2)return false;
                      var weekday=pieces[0].replace(/[.]/g,'');
                      if(['mo','di','mi','do','fr','sa','so'].indexOf(weekday)<0)return false;
                      var date=pieces[1];
                      while(date.endsWith('.'))date=date.slice(0,-1);
                      var dateParts=date.split('.');
                      return dateParts.length===2
                        &&dateParts[0].length>=1&&dateParts[0].length<=2
                        &&dateParts[1].length>=1&&dateParts[1].length<=2
                        &&digitsOnly(dateParts[0])&&digitsOnly(dateParts[1]);
                    };
"""
if old not in text:
    raise SystemExit('datePattern source not found')
text = text.replace(old, new, 1)
text = text.replace('if(!datePattern.test(raw))return;', 'if(!dayDateLabel(raw))return;', 1)
text = text.replace("if(datePattern.test(raw)&&node.children.length===0)node.classList.add('pfvr-day-date');", "if(dayDateLabel(raw)&&node.children.length===0)node.classList.add('pfvr-day-date');", 1)
path.write_text(text, encoding='utf-8')
