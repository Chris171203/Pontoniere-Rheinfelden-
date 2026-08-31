from pathlib import Path

p = Path('Android/app/src/main/java/ch/pfvr/internapp/MainActivity.java')
s = p.read_text(encoding='utf-8')

if 'Testversion 0.5.0' in s:
    print('0.5.0 patch already applied')
    raise SystemExit(0)

def repl(old: str, new: str, name: str):
    global s
    n = s.count(old)
    if n != 1:
        raise SystemExit(f'{name}: expected exactly one match, got {n}')
    s = s.replace(old, new, 1)

# Keep a reference so the selected banking app label updates immediately.
repl(
    '    private Bitmap pendingQrBitmap;\n    private volatile boolean weatherLoading = false;',
    '    private Bitmap pendingQrBitmap;\n    private Button bankButton;\n    private volatile boolean weatherLoading = false;',
    'bank button field'
)

# Remove redundant cash shortcut; Kasse already exists in primary navigation.
repl(
    '        section(b,"Schnellzugriff",null);\n        b.addView(action("Vereinsbeiz","Betrag eingeben und Banking-App öffnen","Zur Kasse",v->navigate(Screen.CASH)));\n        b.addView(action("Depot","Rheinweg 42 · 4310 Rheinfelden","Route",v->openMap()));',
    '        section(b,"Schnellzugriff",null);\n        b.addView(action("Depot","Rheinweg 42 · 4310 Rheinfelden","Route",v->openMap()));',
    'remove redundant cash shortcut'
)

# Make the Rhine graphs readable as actual charts: last 24 h, y-unit, x time labels.
repl(
    '            c.addView(new TrendView(this,level,"m ü.M.",WATER),new LinearLayout.LayoutParams(-1,dp(104)));',
    '            c.addView(new TrendView(this,level,"m ü.M.",WATER),new LinearLayout.LayoutParams(-1,dp(132)));',
    'level chart height'
)
repl(
    '            c.addView(new TrendView(this,temp,"°C",Color.rgb(220,137,63)),new LinearLayout.LayoutParams(-1,dp(104)));',
    '            c.addView(new TrendView(this,temp,"°C",Color.rgb(220,137,63)),new LinearLayout.LayoutParams(-1,dp(132)));',
    'temperature chart height'
)

old_training = '''    private LocalDate nextTrainingDay(){
        ZoneId zone=ZoneId.of("Europe/Zurich"); ZonedDateTime now=ZonedDateTime.now(zone); LocalDate start=now.toLocalDate();
        for(int i=0;i<8;i++){LocalDate d=start.plusDays(i);DayOfWeek w=d.getDayOfWeek();if(w!=DayOfWeek.MONDAY&&w!=DayOfWeek.WEDNESDAY)continue;ZonedDateTime cutoff=d.atTime(21,0).atZone(zone);if(i>0||now.isBefore(cutoff))return d;}
        return start.plusDays(1);
    }

    private String[] weatherSummary(){
        LocalDate d=nextTrainingDay(); String date=cap(d.format(DateTimeFormatter.ofPattern("EEEE, dd.MM.",Locale.GERMAN)))+" · 18–20 Uhr";'''
new_training = '''    private boolean summerTraining(LocalDate d){int m=d.getMonthValue();return m>=4&&m<=9;}
    private boolean regularTrainingDay(LocalDate d){DayOfWeek w=d.getDayOfWeek();return summerTraining(d)?(w==DayOfWeek.MONDAY||w==DayOfWeek.WEDNESDAY):w==DayOfWeek.THURSDAY;}
    private String trainingTime(LocalDate d){return summerTraining(d)?"18:30–20:00 Uhr":"19:30–21:00 Uhr";}
    private int trainingEndHour(LocalDate d){return summerTraining(d)?20:21;}
    private int[] trainingWeatherHours(LocalDate d){return summerTraining(d)?new int[]{18,19,20}:new int[]{19,20,21};}
    private LocalDate nextTrainingDay(){
        ZoneId zone=ZoneId.of("Europe/Zurich"); ZonedDateTime now=ZonedDateTime.now(zone); LocalDate start=now.toLocalDate();
        for(int i=0;i<10;i++){LocalDate d=start.plusDays(i);if(!regularTrainingDay(d))continue;ZonedDateTime cutoff=d.atTime(trainingEndHour(d),0).atZone(zone);if(i>0||now.isBefore(cutoff))return d;}
        return start.plusDays(1);
    }

    private String[] weatherSummary(){
        LocalDate d=nextTrainingDay(); String date=cap(d.format(DateTimeFormatter.ofPattern("EEEE, dd.MM.",Locale.GERMAN)))+" · "+trainingTime(d);'''
repl(old_training, new_training, 'season-aware training schedule')

repl(
    '            double tFirst=Double.NaN,tLast=Double.NaN,psum=0,wmax=0,gmax=0; int pmax=0,wcode=-1,count=0; String prefix=d.toString()+"T";\n            for(int i=0;i<times.length();i++){\n                String tm=times.optString(i,""); if(!(tm.equals(prefix+"18:00")||tm.equals(prefix+"19:00")||tm.equals(prefix+"20:00")))continue;',
    '            double tFirst=Double.NaN,tLast=Double.NaN,psum=0,wmax=0,gmax=0; int pmax=0,wcode=-1,count=0; String prefix=d.toString()+"T"; int[] hours=trainingWeatherHours(d);\n            for(int i=0;i<times.length();i++){\n                String tm=times.optString(i,""); boolean inWindow=false; for(int hour:hours)if(tm.equals(prefix+String.format(Locale.ROOT,"%02d:00",hour))){inWindow=true;break;} if(!inWindow)continue;',
    'weather hours follow training schedule'
)

# Restrict trend data to the actual last 24 hours of the returned station series.
repl(
    '            points.sort(Comparator.comparingLong(x->x.time));\n            long last=Long.MIN_VALUE; for(HydroPoint hp:points){if(hp.time==last)continue;last=hp.time;out.times.add(hp.time);out.values.add(hp.value);}\n            if(out.values.size()>180){int cut=out.values.size()-180;out.times=new ArrayList<>(out.times.subList(cut,out.times.size()));out.values=new ArrayList<>(out.values.subList(cut,out.values.size()));}',
    '            points.sort(Comparator.comparingLong(x->x.time));\n            long newest=points.isEmpty()?Long.MIN_VALUE:points.get(points.size()-1).time; long cutoff=newest==Long.MIN_VALUE?Long.MIN_VALUE:newest-24L*60L*60L*1000L;\n            long last=Long.MIN_VALUE; for(HydroPoint hp:points){if(hp.time<cutoff||hp.time==last)continue;last=hp.time;out.times.add(hp.time);out.values.add(hp.value);}',
    '24 hour trend window'
)

# Update the banking-app button immediately when a selection is made.
repl(
    '        String bankLabel=prefs.getString(PREF_BANK_LABEL,""); Button bank=btn(bankLabel.trim().isEmpty()?"Banking-App auswählen":bankLabel+" öffnen",Color.rgb(232,240,244),NAVY); bank.setOnClickListener(v->openPreferred(false,amount)); LinearLayout.LayoutParams bp=new LinearLayout.LayoutParams(-1,dp(46)); bp.setMargins(0,dp(8),0,0); amountCard.addView(bank,bp);',
    '        String bankLabel=prefs.getString(PREF_BANK_LABEL,""); Button bank=btn(bankLabel.trim().isEmpty()?"Banking-App auswählen":bankLabel+" öffnen",Color.rgb(232,240,244),NAVY); bankButton=bank; bank.setOnClickListener(v->openPreferred(false,amount)); LinearLayout.LayoutParams bp=new LinearLayout.LayoutParams(-1,dp(46)); bp.setMargins(0,dp(8),0,0); amountCard.addView(bank,bp);',
    'bank button reference'
)
repl(
    '        new AlertDialog.Builder(this,dialogTheme()).setTitle(twint?"TWINT-App auswählen":"Banking-App auswählen").setItems(labels,(d,i)->{AppChoice c=found.get(i); prefs.edit().putString(twint?PREF_TWINT_PACKAGE:PREF_BANK_PACKAGE,c.pkg).putString(twint?PREF_TWINT_LABEL:PREF_BANK_LABEL,c.label).apply(); copyAmount(amountInput); Intent launch=pm.getLaunchIntentForPackage(c.pkg); if(launch!=null) startActivity(launch);}).setNegativeButton("Abbrechen",null).show();',
    '        new AlertDialog.Builder(this,dialogTheme()).setTitle(twint?"TWINT-App auswählen":"Banking-App auswählen").setItems(labels,(d,i)->{AppChoice c=found.get(i); prefs.edit().putString(twint?PREF_TWINT_PACKAGE:PREF_BANK_PACKAGE,c.pkg).putString(twint?PREF_TWINT_LABEL:PREF_BANK_LABEL,c.label).apply(); if(!twint&&bankButton!=null)bankButton.setText(c.label+" öffnen"); copyAmount(amountInput); Intent launch=pm.getLaunchIntentForPackage(c.pkg); if(launch!=null) startActivity(launch);}).setNegativeButton("Abbrechen",null).show();',
    'update bank button label'
)

# Simplified website pages stay light even when the native shell is dark. This avoids white-on-white
# text and broken embedded viewers from third-party WordPress/PDF CSS.
repl(
    '    private WebView web(boolean simplify){WebView w=new WebView(this);w.setBackgroundColor(themeBg(Color.WHITE));',
    '    private WebView web(boolean simplify){WebView w=new WebView(this);w.setBackgroundColor(simplify?Color.WHITE:themeBg(Color.WHITE));',
    'webview background'
)
old_skin = '''    private void skin(WebView v){
        String bg=darkMode?"#11171C":"#F4F7F9", card=darkMode?"#1A2228":"#FFFFFF", text=darkMode?"#ECF1F4":"#15232E", heading=darkMode?"#ECF1F4":"#0C2D48", link=darkMode?"#5BBED5":"#247E99";
        String css="header,.site-header,.header-wrapper,nav,.main-navigation,footer,.site-footer,.scroll-top,.back-to-top{display:none!important;}html,body{background:"+bg+"!important;}body{margin:0!important;padding:14px 14px 40px!important;font-family:Arial,sans-serif!important;color:"+text+"!important;}main,.site-content,.content-area,.container,.wrapper{width:100%!important;max-width:none!important;margin:0!important;padding:0!important;}article,.post,.entry{background:"+card+"!important;border-radius:16px!important;padding:16px!important;margin:0 0 14px!important;box-shadow:0 2px 10px rgba(0,0,0,.16)!important;}img{max-width:100%!important;height:auto!important;border-radius:12px!important;}a{color:"+link+"!important;}h1,h2,h3{color:"+heading+"!important;}";
        String js="(function(){var s=document.getElementById('pfvr-app-style');if(!s){s=document.createElement('style');s.id='pfvr-app-style';document.head.appendChild(s);}s.innerHTML='"+css.replace("\\\\","\\\\\\\\").replace("'","\\\\'")+"';})();"; v.evaluateJavascript(js,null);
    }'''
new_skin = '''    private void skin(WebView v){
        // Keep embedded PFVR content in a controlled light presentation. The native shell may stay dark,
        // but forcing arbitrary WordPress/PDF content dark caused unreadable white-on-white combinations.
        String css="html{color-scheme:light!important;}header,.site-header,.header-wrapper,nav,.main-navigation,footer,.site-footer,.scroll-top,.back-to-top{display:none!important;}html,body{background:#F4F7F9!important;}body{margin:0!important;padding:14px 14px 40px!important;font-family:Arial,sans-serif!important;color:#15232E!important;}main,.site-content,.content-area,.container,.wrapper{width:100%!important;max-width:none!important;margin:0!important;padding:0!important;}article,.post,.entry,.entry-content{background:#FFFFFF!important;color:#15232E!important;border-radius:16px!important;padding:16px!important;margin:0 0 14px!important;box-shadow:0 2px 10px rgba(0,0,0,.10)!important;}article p,article li,article span,.entry-content p,.entry-content li,.entry-content span,.entry-content div{color:#15232E!important;}img{max-width:100%!important;height:auto!important;border-radius:12px!important;}iframe{background:#FFFFFF!important;}a{color:#247E99!important;}h1,h2,h3,h4,h5,h6{color:#0C2D48!important;}";
        String js="(function(){var s=document.getElementById('pfvr-app-style');if(!s){s=document.createElement('style');s.id='pfvr-app-style';document.head.appendChild(s);}s.innerHTML='"+css.replace("\\\\","\\\\\\\\").replace("'","\\\\'")+"';})();"; v.evaluateJavascript(js,null);
    }'''
repl(old_skin, new_skin, 'safe light web skin')

# Bump visible in-app test version.
repl(
    '        about.addView(txt("PFVR Rheinfelden",16,TEXT,true)); about.addView(txt("Testversion 0.4.0 · 1.0.0 bleibt für den ersten offiziellen Release reserviert.",13,MUTED,false));',
    '        about.addView(txt("PFVR Rheinfelden",16,TEXT,true)); about.addView(txt("Testversion 0.5.0 · 1.0.0 bleibt für den ersten offiziellen Release reserviert.",13,MUTED,false));',
    'visible version'
)

# Replace the chart renderer with real axis labels and timestamp-based x positioning.
old_chart = '''    private class TrendView extends View {
        private final TrendSeries series; private final String unit; private final int lineColor;
        private final Paint line=new Paint(Paint.ANTI_ALIAS_FLAG), grid=new Paint(Paint.ANTI_ALIAS_FLAG), label=new Paint(Paint.ANTI_ALIAS_FLAG);
        TrendView(Context c,TrendSeries s,String u,int color){super(c);series=s;unit=u;lineColor=color;line.setStrokeWidth(dp(2));line.setStyle(Paint.Style.STROKE);grid.setStrokeWidth(dp(1));label.setTextSize(10*getResources().getDisplayMetrics().scaledDensity);setPadding(0,0,0,0);}
        @Override protected void onDraw(Canvas canvas){super.onDraw(canvas);if(series.values.size()<2)return;float w=getWidth(),h=getHeight(),left=dp(7),right=w-dp(7),top=dp(8),bottom=h-dp(22);double min=Double.POSITIVE_INFINITY,max=Double.NEGATIVE_INFINITY;for(double v:series.values){min=Math.min(min,v);max=Math.max(max,v);}if(!(max>min)){max=min+1;}grid.setColor(darkMode?Color.rgb(58,72,82):Color.rgb(220,229,234));for(int i=0;i<3;i++){float y=top+(bottom-top)*i/2f;canvas.drawLine(left,y,right,y,grid);}Path path=new Path();for(int i=0;i<series.values.size();i++){float x=left+(right-left)*i/(series.values.size()-1f);float y=(float)(bottom-(series.values.get(i)-min)/(max-min)*(bottom-top));if(i==0)path.moveTo(x,y);else path.lineTo(x,y);}line.setColor(lineColor);canvas.drawPath(path,line);label.setColor(themeText(MUTED));String lo=fmtTrend(min)+" "+unit,hi=fmtTrend(max)+" "+unit;canvas.drawText(lo,left,h-dp(5),label);float tw=label.measureText(hi);canvas.drawText(hi,right-tw,h-dp(5),label);}
        private String fmtTrend(double v){return Math.abs(v)>=100?String.format(Locale.GERMAN,"%.1f",v):String.format(Locale.GERMAN,"%.2f",v);}
    }'''
new_chart = '''    private class TrendView extends View {
        private final TrendSeries series; private final String unit; private final int lineColor;
        private final Paint line=new Paint(Paint.ANTI_ALIAS_FLAG), grid=new Paint(Paint.ANTI_ALIAS_FLAG), label=new Paint(Paint.ANTI_ALIAS_FLAG), axis=new Paint(Paint.ANTI_ALIAS_FLAG);
        TrendView(Context c,TrendSeries s,String u,int color){super(c);series=s;unit=u;lineColor=color;line.setStrokeWidth(dp(2));line.setStyle(Paint.Style.STROKE);grid.setStrokeWidth(dp(1));label.setTextSize(9*getResources().getDisplayMetrics().scaledDensity);axis.setTextSize(10*getResources().getDisplayMetrics().scaledDensity);axis.setTypeface(Typeface.DEFAULT_BOLD);setPadding(0,0,0,0);}
        @Override protected void onDraw(Canvas canvas){
            super.onDraw(canvas);if(series.values.size()<2||series.times.size()!=series.values.size())return;
            float w=getWidth(),h=getHeight(),left=dp(50),right=w-dp(7),top=dp(18),bottom=h-dp(30);
            double min=Double.POSITIVE_INFINITY,max=Double.NEGATIVE_INFINITY;for(double v:series.values){min=Math.min(min,v);max=Math.max(max,v);}if(!(max>min)){max=min+1;}
            long minT=series.times.get(0),maxT=series.times.get(series.times.size()-1);if(maxT<=minT)maxT=minT+1;
            grid.setColor(darkMode?Color.rgb(58,72,82):Color.rgb(220,229,234));
            for(int i=0;i<3;i++){float y=top+(bottom-top)*i/2f;canvas.drawLine(left,y,right,y,grid);}for(int i=0;i<3;i++){float x=left+(right-left)*i/2f;canvas.drawLine(x,top,x,bottom,grid);}
            Path path=new Path();for(int i=0;i<series.values.size();i++){float x=left+(right-left)*(series.times.get(i)-minT)/(float)(maxT-minT);float y=(float)(bottom-(series.values.get(i)-min)/(max-min)*(bottom-top));if(i==0)path.moveTo(x,y);else path.lineTo(x,y);}line.setColor(lineColor);canvas.drawPath(path,line);
            label.setColor(themeText(MUTED));axis.setColor(themeText(MUTED));
            canvas.drawText(fmtTrend(max),dp(2),top+dp(4),label);canvas.drawText(fmtTrend(min),dp(2),bottom,label);canvas.drawText(unit,dp(2),dp(10),axis);
            ZoneId zone=ZoneId.of("Europe/Zurich");DateTimeFormatter tf=DateTimeFormatter.ofPattern("HH:mm");String first=java.time.Instant.ofEpochMilli(minT).atZone(zone).format(tf),last=java.time.Instant.ofEpochMilli(maxT).atZone(zone).format(tf);canvas.drawText(first,left,h-dp(6),label);float lw=label.measureText(last);canvas.drawText(last,right-lw,h-dp(6),label);String xLabel="Zeit · 24 h";float xw=axis.measureText(xLabel);canvas.drawText(xLabel,left+(right-left-xw)/2f,h-dp(6),axis);
        }
        private String fmtTrend(double v){return Math.abs(v)>=100?String.format(Locale.GERMAN,"%.1f",v):String.format(Locale.GERMAN,"%.2f",v);}
    }'''
repl(old_chart, new_chart, 'chart axes')

p.write_text(s, encoding='utf-8')
print('Applied 0.5.0 patch')
