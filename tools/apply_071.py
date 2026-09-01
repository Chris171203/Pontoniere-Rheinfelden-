from pathlib import Path
import re

ROOT = Path(__file__).resolve().parents[1]
SRC = ROOT / "Android/app/src/main/java/ch/pfvr/internapp/MainActivity.java"
BUILD = ROOT / "Android/app/build.gradle"
CI = ROOT / ".github/workflows/android.yml"
STATUS = ROOT / "STATUS.md"


def replace_once(text: str, old: str, new: str, label: str) -> str:
    if old not in text:
        raise SystemExit(f"Patch anchor not found: {label}")
    return text.replace(old, new, 1)


text = SRC.read_text(encoding="utf-8")

text = replace_once(
    text,
    "import android.graphics.Color;\n",
    "import android.graphics.Color;\nimport android.graphics.DashPathEffect;\n",
    "DashPathEffect import",
)
text = replace_once(
    text,
    "import android.text.InputType;\n",
    "import android.text.InputType;\nimport android.text.SpannableString;\nimport android.text.Spanned;\nimport android.text.style.ForegroundColorSpan;\n",
    "Spannable imports",
)
text = replace_once(
    text,
    '    private static final String TWINT_QR_PDF = "https://www.pfvr.ch/wp-content/uploads/Seiten/vereinsbeiz_zahlung/Twint_QR.pdf";\n',
    '    private static final String TWINT_QR_PDF = "https://www.pfvr.ch/wp-content/uploads/Seiten/vereinsbeiz_zahlung/Twint_QR.pdf";\n'
    '    private static final String TWINT_DIRECT_URL = "https://www.pfvr.ch/vereinsbeiz-zahlung/";\n',
    "TWINT direct URL",
)

text = replace_once(
    text,
    '        section(b,"Schnellzugriff",null);\n'
    '        b.addView(action("Depot","Rheinweg 42 · 4310 Rheinfelden","Route",v->openMap()));\n',
    "",
    "Home depot quick link",
)

river_method = r'''    private LinearLayout riverCard(){
        LinearLayout c=card(); c.setOrientation(LinearLayout.VERTICAL); c.setPadding(dp(16),dp(15),dp(16),dp(14));
        String[] x=hydroSummary(); c.setOnClickListener(v->external("https://www.hydrodaten.admin.ch/de/seen-und-fluesse/stationen-und-daten/2091"));
        c.addView(txt(x[0],11,WATER,true));

        double q=currentHydroValue("Q"); RiverStatus rs=riverStatus(q);
        LinearLayout headline=new LinearLayout(this); headline.setGravity(Gravity.CENTER_VERTICAL); headline.setPadding(0,dp(4),0,dp(3)); c.addView(headline);
        TextView main=txt(x[1],27,TEXT,true); main.setGravity(Gravity.CENTER_VERTICAL); headline.addView(main,new LinearLayout.LayoutParams(0,-2,1));
        if(!Double.isNaN(q)){
            int color=statusTextColor(rs.bg);
            TextView status=txt(rs.label,18,color,true); status.setTextColor(color); status.setGravity(Gravity.CENTER); status.setPadding(dp(13),dp(7),dp(13),dp(7)); status.setBackground(statusBadge(rs.bg));
            LinearLayout.LayoutParams slp=new LinearLayout.LayoutParams(-2,-2); slp.setMargins(dp(12),0,0,0); headline.addView(status,slp);
        }
        c.addView(txt(x[2],13,MUTED,false));
        TextView limits=riverLegend(); limits.setPadding(0,dp(6),0,0); c.addView(limits);

        TrendSeries level=hydroSeries("W"), flow=hydroSeries("Q"), temp=hydroSeries("WT");
        if(level.values.size()>=2 && flow.values.size()>=2){
            TextView title=txt("Pegel & Abfluss · 7 Tage",13,TEXT,true); title.setPadding(0,dp(14),0,dp(2)); c.addView(title);
            TextView legend=txt("Blau: Pegel · Orange: Abfluss",10,MUTED,false); legend.setPadding(0,0,0,dp(3)); c.addView(legend);
            c.addView(new DualTrendView(this,level,flow,"m ü.M.","m³/s",WATER,Color.rgb(220,137,63)),new LinearLayout.LayoutParams(-1,dp(154)));
        } else if(level.values.size()>=2){
            TextView title=txt("Pegelverlauf · 7 Tage",13,TEXT,true); title.setPadding(0,dp(14),0,dp(3)); c.addView(title);
            c.addView(new TrendView(this,level,"m ü.M.",WATER),new LinearLayout.LayoutParams(-1,dp(132)));
        }
        if(temp.values.size()>=2){
            TextView title=txt("Wassertemperatur · 7 Tage",13,TEXT,true); title.setPadding(0,dp(12),0,dp(3)); c.addView(title);
            c.addView(new TrendView(this,temp,"°C",Color.rgb(220,137,63)),new LinearLayout.LayoutParams(-1,dp(132)));
        }
        TextView src=txt(x[3],10,Color.rgb(126,140,150),false); src.setPadding(0,dp(9),0,0); c.addView(src);
        return c;
    }

    private TextView riverLegend(){
        String text=String.format(Locale.GERMAN,"Niedrig < %.0f · Gut %.0f–<%.0f · Warnung %.0f–<%.0f · Alarm ab %.0f m³/s",riverLow(),riverLow(),riverWarn(),riverWarn(),riverAlarm(),riverAlarm());
        return coloredStatusText(text,10);
    }

    private TextView riverSettingsLegend(){
        String text=String.format(Locale.GERMAN,"Niedrig  < %.0f m³/s\nGut  %.0f bis < %.0f m³/s\nWarnung  %.0f bis < %.0f m³/s\nAlarm  ab %.0f m³/s",riverLow(),riverLow(),riverWarn(),riverWarn(),riverAlarm(),riverAlarm());
        return coloredStatusText(text,13);
    }

    private TextView coloredStatusText(String text,float size){
        SpannableString styled=new SpannableString(text);
        colorKeyword(styled,"Niedrig",statusTextColor(STATUS_LOW));
        colorKeyword(styled,"Gut",statusTextColor(STATUS_GOOD));
        colorKeyword(styled,"Warnung",statusTextColor(STATUS_WARN));
        colorKeyword(styled,"Alarm",statusTextColor(STATUS_ALARM));
        TextView view=txt("",size,MUTED,false); view.setText(styled); return view;
    }

    private void colorKeyword(SpannableString text,String word,int color){
        String plain=text.toString(); int start=0;
        while((start=plain.indexOf(word,start))>=0){text.setSpan(new ForegroundColorSpan(color),start,start+word.length(),Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);start+=word.length();}
    }

    private int statusTextColor(int color){
        if(!darkMode){if(color==STATUS_WARN)return Color.rgb(176,116,0);return color;}
        if(color==STATUS_LOW)return Color.rgb(91,190,213);
        if(color==STATUS_GOOD)return Color.rgb(73,196,111);
        if(color==STATUS_WARN)return Color.rgb(255,216,92);
        if(color==STATUS_ALARM)return Color.rgb(255,105,105);
        return DARK_MUTED;
    }

    private GradientDrawable statusBadge(int color){
        int actual=statusTextColor(color); GradientDrawable d=new GradientDrawable();
        d.setColor(Color.argb(darkMode?42:22,Color.red(actual),Color.green(actual),Color.blue(actual)));
        d.setStroke(dp(1),Color.argb(darkMode?190:130,Color.red(actual),Color.green(actual),Color.blue(actual)));
        d.setCornerRadius(dp(14)); return d;
    }
'''
pattern = re.compile(r"    private LinearLayout riverCard\(\)\{.*?\n    \}\n\n(?=    private boolean summerTraining)", re.S)
text, count = pattern.subn(river_method + "\n", text, count=1)
if count != 1:
    raise SystemExit(f"River card replacement count: {count}")

text = replace_once(
    text,
    '        TextView ranges=txt(String.format(Locale.GERMAN,"🔵 Niedrig  < %.0f m³/s\\n🟢 Gut  %.0f–%.0f m³/s\\n🟡 Warnung  ab %.0f m³/s\\n🔴 Alarm  ab %.0f m³/s",riverLow(),riverLow(),riverWarn(),riverWarn(),riverAlarm()),13,MUTED,false); ranges.setPadding(0,dp(5),0,dp(10)); limitsCard.addView(ranges);\n',
    '        TextView ranges=riverSettingsLegend(); ranges.setPadding(0,dp(5),0,dp(10)); limitsCard.addView(ranges);\n',
    "colored settings thresholds",
)

text = replace_once(
    text,
    '        about.addView(txt("PFVR Rheinfelden",16,TEXT,true)); about.addView(txt("Testversion 0.7.0 · 1.0.0 bleibt für den ersten offiziellen Release reserviert.",13,MUTED,false));\n',
    '        about.addView(txt("PFVR Rheinfelden",16,TEXT,true)); about.addView(txt("Testversion 0.7.1 · 1.0.0 bleibt für den ersten offiziellen Release reserviert.",13,MUTED,false));\n',
    "settings version",
)

twint_old = '''        LinearLayout tw=card(); tw.setOrientation(LinearLayout.VERTICAL); b.addView(tw,margin(-1,-2,0,0,0,12)); tw.addView(txt("TWINT",16,TEXT,true)); TextView ti=txt("Alternative zur Bankzahlung. Der offizielle Vereinsbeiz-TWINT-QR liegt auf pfvr.ch und kann als PDF geöffnet bzw. gespeichert werden.",13,MUTED,false); ti.setPadding(0,dp(4),0,dp(10)); tw.addView(ti);
        Button twq=btn("Vereins-TWINT-QR öffnen",NAVY,Color.WHITE); twq.setOnClickListener(v->external(TWINT_QR_PDF)); tw.addView(twq,new LinearLayout.LayoutParams(-1,dp(48)));
        Button twb=btn("TWINT-App öffnen",Color.rgb(232,240,244),NAVY); twb.setOnClickListener(v->openPreferred(true,amount)); LinearLayout.LayoutParams twbp=new LinearLayout.LayoutParams(-1,dp(44)); twbp.setMargins(0,dp(8),0,0); tw.addView(twb,twbp);
'''
twint_new = '''        LinearLayout tw=card(); tw.setOrientation(LinearLayout.VERTICAL); b.addView(tw,margin(-1,-2,0,0,0,12)); tw.addView(txt("TWINT",16,TEXT,true)); TextView ti=txt("Für die Zahlung auf demselben Handy: Betrag auf der PFVR-Seite eingeben und TWINT wählen. Dort wird der gültige fünfstellige Zahlungscode erzeugt. Der statische QR bleibt zum Scannen vor Ort.",13,MUTED,false); ti.setPadding(0,dp(4),0,dp(10)); tw.addView(ti);
        Button twDirect=btn("TWINT-Code erzeugen",NAVY,Color.WHITE); twDirect.setOnClickListener(v->openTwintDirect(amount)); tw.addView(twDirect,new LinearLayout.LayoutParams(-1,dp(48)));
        Button twq=btn("Vereins-TWINT-QR öffnen",Color.rgb(232,240,244),NAVY); twq.setOnClickListener(v->external(TWINT_QR_PDF)); LinearLayout.LayoutParams twqp=new LinearLayout.LayoutParams(-1,dp(44)); twqp.setMargins(0,dp(8),0,0); tw.addView(twq,twqp);
'''
text = replace_once(text, twint_old, twint_new, "TWINT direct payment")

text = replace_once(
    text,
    '    private void showPaymentQr(EditText amountInput) {\n',
    '''    private void openTwintDirect(EditText amountInput){
        String a=amount(amountInput==null?null:amountInput.getText().toString());
        if(a==null){Toast.makeText(this,"Bitte einen gültigen CHF-Betrag eingeben oder das Feld leer lassen.",Toast.LENGTH_LONG).show();return;}
        if(!a.isBlank())copy("PFVR TWINT-Betrag",a,"CHF "+a+" kopiert – auf der PFVR-Seite eintragen.");
        external(TWINT_DIRECT_URL);
    }

    private void showPaymentQr(EditText amountInput) {
''',
    "TWINT direct helper",
)

text = replace_once(
    text,
    '        b.addView(action("Interner Bereich","An-/Abmeldung und interne PFVR-Seite","Öffnen",v->navigate(Screen.INTERNAL)));\n',
    "",
    "club internal duplicate",
)

chart_block = r'''    private static class TrendSeries {List<Long> times=new ArrayList<>();List<Double> values=new ArrayList<>();}
    private static class RiverStatus {final String label;final int bg,fg;RiverStatus(String l,int b,int f){label=l;bg=b;fg=f;}}
    private static class AxisScale {final double min,max,step;AxisScale(double min,double max,double step){this.min=min;this.max=max;this.step=step;}}

    private AxisScale niceAxis(List<Double> values){
        double dataMin=Double.POSITIVE_INFINITY,dataMax=Double.NEGATIVE_INFINITY;
        for(Double value:values){if(value==null||!Double.isFinite(value))continue;dataMin=Math.min(dataMin,value);dataMax=Math.max(dataMax,value);}
        if(!Double.isFinite(dataMin)||!Double.isFinite(dataMax))return new AxisScale(0,1,1);
        if(!(dataMax>dataMin)){double half=Math.max(Math.abs(dataMin)*0.02,Math.abs(dataMin)>=100?10:(Math.abs(dataMin)>=10?1:0.1));dataMin-=half;dataMax+=half;}
        double step=niceStep((dataMax-dataMin)/4d);
        double min=Math.floor(dataMin/step+1e-9)*step;
        double max=Math.ceil(dataMax/step-1e-9)*step;
        if(!(max>min)){min-=step;max+=step;}
        return new AxisScale(min,max,step);
    }

    private double niceStep(double raw){
        if(!Double.isFinite(raw)||raw<=0)return 1;
        double exponent=Math.floor(Math.log10(raw)),base=Math.pow(10,exponent),fraction=raw/base,nice;
        if(fraction<=1)nice=1;else if(fraction<=2)nice=2;else if(fraction<=5)nice=5;else nice=10;
        return nice*base;
    }

    private String axisLabel(double value,double step){
        double s=Math.abs(step);
        if(s>=1)return String.format(Locale.GERMAN,"%.0f",value);
        if(s>=0.1)return String.format(Locale.GERMAN,"%.1f",value);
        if(s>=0.01)return String.format(Locale.GERMAN,"%.2f",value);
        return String.format(Locale.GERMAN,"%.3f",value);
    }

    private class DualTrendView extends View {
        private final TrendSeries leftSeries,rightSeries; private final String leftUnit,rightUnit; private final int leftColor,rightColor;
        private final Paint line=new Paint(Paint.ANTI_ALIAS_FLAG),grid=new Paint(Paint.ANTI_ALIAS_FLAG),label=new Paint(Paint.ANTI_ALIAS_FLAG),axis=new Paint(Paint.ANTI_ALIAS_FLAG),threshold=new Paint(Paint.ANTI_ALIAS_FLAG);
        DualTrendView(Context c,TrendSeries l,TrendSeries r,String lu,String ru,int lc,int rc){super(c);leftSeries=l;rightSeries=r;leftUnit=lu;rightUnit=ru;leftColor=lc;rightColor=rc;line.setStrokeWidth(dp(2));line.setStyle(Paint.Style.STROKE);grid.setStrokeWidth(dp(1));threshold.setStrokeWidth(dp(1.4f));threshold.setStyle(Paint.Style.STROKE);threshold.setPathEffect(new DashPathEffect(new float[]{dp(6),dp(4)},0));label.setTextSize(9*getResources().getDisplayMetrics().scaledDensity);axis.setTextSize(10*getResources().getDisplayMetrics().scaledDensity);axis.setTypeface(Typeface.DEFAULT_BOLD);}
        @Override protected void onDraw(Canvas canvas){
            super.onDraw(canvas);if(leftSeries.values.size()<2||rightSeries.values.size()<2)return;
            float w=getWidth(),h=getHeight(),left=dp(50),right=w-dp(54),top=dp(20),bottom=h-dp(28);
            long minT=Math.min(leftSeries.times.get(0),rightSeries.times.get(0)),maxT=Math.max(leftSeries.times.get(leftSeries.times.size()-1),rightSeries.times.get(rightSeries.times.size()-1));if(maxT<=minT)maxT=minT+1;
            AxisScale leftScale=niceAxis(leftSeries.values),rightScale=niceAxis(rightSeries.values);
            grid.setColor(darkMode?Color.rgb(58,72,82):Color.rgb(220,229,234));label.setColor(themeText(MUTED));axis.setColor(themeText(MUTED));
            for(int i=0;i<=4;i++){float y=top+(bottom-top)*i/4f;canvas.drawLine(left,y,right,y,grid);}
            ZoneId zone=ZoneId.of("Europe/Zurich");ZonedDateTime firstZ=java.time.Instant.ofEpochMilli(minT).atZone(zone),lastZ=java.time.Instant.ofEpochMilli(maxT).atZone(zone);LocalDate tick=firstZ.toLocalDate().plusDays(1);DateTimeFormatter df=DateTimeFormatter.ofPattern("EE",Locale.GERMAN);while(!tick.isAfter(lastZ.toLocalDate())){long tt=tick.atStartOfDay(zone).toInstant().toEpochMilli();if(tt>=minT&&tt<=maxT){float x=left+(right-left)*(tt-minT)/(float)(maxT-minT);canvas.drawLine(x,top,x,bottom,grid);String lab=tick.format(df);canvas.drawText(lab,x-label.measureText(lab)/2f,h-dp(5),label);}tick=tick.plusDays(1);}
            drawFlowThresholds(canvas,rightScale,left,right,top,bottom);
            drawSeries(canvas,leftSeries,leftColor,left,right,top,bottom,minT,maxT,leftScale);
            drawSeries(canvas,rightSeries,rightColor,left,right,top,bottom,minT,maxT,rightScale);
            canvas.drawText(axisLabel(leftScale.max,leftScale.step),dp(2),top+dp(4),label);canvas.drawText(axisLabel(leftScale.min,leftScale.step),dp(2),bottom,label);
            String rmax=axisLabel(rightScale.max,rightScale.step),rmin=axisLabel(rightScale.min,rightScale.step);canvas.drawText(rmax,w-dp(2)-label.measureText(rmax),top+dp(4),label);canvas.drawText(rmin,w-dp(2)-label.measureText(rmin),bottom,label);
            canvas.drawText(leftUnit,dp(2),dp(10),axis);canvas.drawText(rightUnit,w-dp(2)-axis.measureText(rightUnit),dp(10),axis);
        }
        private void drawSeries(Canvas c,TrendSeries s,int color,float left,float right,float top,float bottom,long minT,long maxT,AxisScale scale){Path p=new Path();for(int i=0;i<s.values.size();i++){float x=left+(right-left)*(s.times.get(i)-minT)/(float)(maxT-minT);float y=(float)(bottom-(s.values.get(i)-scale.min)/(scale.max-scale.min)*(bottom-top));if(i==0)p.moveTo(x,y);else p.lineTo(x,y);}line.setColor(themeText(color));c.drawPath(p,line);}
        private void drawFlowThresholds(Canvas c,AxisScale scale,float left,float right,float top,float bottom){
            double[] values={riverLow(),riverWarn(),riverAlarm()};int[] colors={STATUS_LOW,STATUS_WARN,STATUS_ALARM};double epsilon=Math.max(1e-9,scale.step*1e-6);
            for(int i=0;i<values.length;i++){double value=values[i];if(value<scale.min-epsilon||value>scale.max+epsilon)continue;float y=(float)(bottom-(value-scale.min)/(scale.max-scale.min)*(bottom-top));threshold.setColor(statusTextColor(colors[i]));c.drawLine(left,y,right,y,threshold);}
        }
    }

    private class TrendView extends View {
        private final TrendSeries series; private final String unit; private final int lineColor;
        private final Paint line=new Paint(Paint.ANTI_ALIAS_FLAG),grid=new Paint(Paint.ANTI_ALIAS_FLAG),label=new Paint(Paint.ANTI_ALIAS_FLAG),axis=new Paint(Paint.ANTI_ALIAS_FLAG);
        TrendView(Context c,TrendSeries s,String u,int color){super(c);series=s;unit=u;lineColor=color;line.setStrokeWidth(dp(2));line.setStyle(Paint.Style.STROKE);grid.setStrokeWidth(dp(1));label.setTextSize(9*getResources().getDisplayMetrics().scaledDensity);axis.setTextSize(10*getResources().getDisplayMetrics().scaledDensity);axis.setTypeface(Typeface.DEFAULT_BOLD);setPadding(0,0,0,0);}
        @Override protected void onDraw(Canvas canvas){
            super.onDraw(canvas);if(series.values.size()<2||series.times.size()!=series.values.size())return;
            float w=getWidth(),h=getHeight(),left=dp(50),right=w-dp(7),top=dp(18),bottom=h-dp(30);AxisScale scale=niceAxis(series.values);
            long minT=series.times.get(0),maxT=series.times.get(series.times.size()-1);if(maxT<=minT)maxT=minT+1;
            grid.setColor(darkMode?Color.rgb(58,72,82):Color.rgb(220,229,234));label.setColor(themeText(MUTED));axis.setColor(themeText(MUTED));
            for(int i=0;i<=4;i++){float y=top+(bottom-top)*i/4f;canvas.drawLine(left,y,right,y,grid);}
            ZoneId zone=ZoneId.of("Europe/Zurich");ZonedDateTime firstZ=java.time.Instant.ofEpochMilli(minT).atZone(zone),lastZ=java.time.Instant.ofEpochMilli(maxT).atZone(zone);LocalDate tickDay=firstZ.toLocalDate().plusDays(1);DateTimeFormatter dayFmt=DateTimeFormatter.ofPattern("EE",Locale.GERMAN);
            while(!tickDay.isAfter(lastZ.toLocalDate())){long tt=tickDay.atStartOfDay(zone).toInstant().toEpochMilli();if(tt>=minT&&tt<=maxT){float x=left+(right-left)*(tt-minT)/(float)(maxT-minT);canvas.drawLine(x,top,x,bottom,grid);String lab=tickDay.format(dayFmt);float tw=label.measureText(lab);canvas.drawText(lab,x-tw/2f,h-dp(6),label);}tickDay=tickDay.plusDays(1);}
            Path path=new Path();for(int i=0;i<series.values.size();i++){float x=left+(right-left)*(series.times.get(i)-minT)/(float)(maxT-minT);float y=(float)(bottom-(series.values.get(i)-scale.min)/(scale.max-scale.min)*(bottom-top));if(i==0)path.moveTo(x,y);else path.lineTo(x,y);}line.setColor(themeText(lineColor));canvas.drawPath(path,line);
            canvas.drawText(axisLabel(scale.max,scale.step),dp(2),top+dp(4),label);canvas.drawText(axisLabel(scale.min,scale.step),dp(2),bottom,label);canvas.drawText(unit,dp(2),dp(10),axis);
            String xLabel="7 Tage · Stundenmittel";float xw=axis.measureText(xLabel);canvas.drawText(xLabel,left+(right-left-xw)/2f,dp(11),axis);
        }
    }
'''
chart_pattern = re.compile(r"    private static class TrendSeries \{.*?\n(?=    private static class AppChoice)", re.S)
text, count = chart_pattern.subn(chart_block + "\n", text, count=1)
if count != 1:
    raise SystemExit(f"Chart replacement count: {count}")

SRC.write_text(text, encoding="utf-8")

build = BUILD.read_text(encoding="utf-8")
build, c1 = re.subn(r"versionCode\s+11", "versionCode 12", build, count=1)
build, c2 = re.subn(r"versionName\s+'0\.7\.0'", "versionName '0.7.1'", build, count=1)
if c1 != 1 or c2 != 1:
    raise SystemExit(f"Version patch failed: code={c1}, name={c2}")
BUILD.write_text(build, encoding="utf-8")

ci = CI.read_text(encoding="utf-8")
ci, c1 = re.subn(r"versionCode='\d+' versionName='[^']+'", "versionCode='12' versionName='0.7.1'", ci, count=1)
ci, c2 = re.subn(r"name: PFVR-Android-debug-[^\n]+", "name: PFVR-Android-debug-0.7.1", ci, count=1)
if c1 != 1 or c2 != 1:
    raise SystemExit(f"CI version patch failed: badge={c1}, artifact={c2}")
CI.write_text(ci, encoding="utf-8")

STATUS.write_text("""# Status

Stand: Testversion `0.7.1` auf Branch `dev-0.7.1`.

## Implementiert / im Test

- Android-App mit Home, Terminen, Verein, Kasse, Einstellungen und persönlichem An-/Abmeldebereich.
- Wetter für das nächste reguläre Training, Kalender-Cache sowie BAFU-Live- und 7-Tage-Daten für Station 2091.
- Rheinstatus direkt neben dem aktuellen Abflusswert; `Niedrig`, `Gut`, `Warnung` und `Alarm` sind in ihrer Statusfarbe hervorgehoben.
- Dynamische, gerundete Y-Achsen statt Rohwert-Minimum und -Maximum.
- Im kombinierten Pegel-/Abflussdiagramm werden relevante Abflussgrenzen innerhalb der rechten Skala farbig gestrichelt eingezeichnet.
- TWINT bietet neben dem statischen Vereins-QR nun einen Einstieg zur offiziellen PFVR-Onlinezahlung für dasselbe Smartphone. Nach Betragseingabe und Auswahl von TWINT erzeugt der Händlerprozess den gültigen fünfstelligen Zahlungscode.
- Der wirkungslose direkte Start der TWINT-App ohne Händlertransaktion wurde durch `TWINT-Code erzeugen` ersetzt.
- Depot-Schnellzugriff auf Home entfernt; doppelter Eintrag `Interner Bereich` unter Verein entfernt. An-/Abmelden bleibt auf Home und in den Einstellungen erreichbar.
- Dark Mode, Hintergrundaktualisierung, anpassbare Rhein-Grenzwerte und Swiss QR bleiben erhalten.

## Technische Entscheidung TWINT

Der fünfstellige TWINT-Zahlungscode ist kein aus dem statischen QR ableitbarer Wert. Er entsteht innerhalb eines offiziellen Händler-/Online-Bezahlvorgangs. Die App öffnet deshalb die bereits vorhandene PFVR-Direktzahlungsseite, statt lokal einen ungültigen Code zu erfinden.

## Verifiziert durch den Branch-Workflow

- Android-Quellcode kompiliert gegen API 36.
- APK-Signatur sowie Paketname, `versionCode 12` und `versionName 0.7.1` werden geprüft.
- Statische Prüfungen kontrollieren Direktzahlung, gerundete Achsen, Grenzlinien und die entfernten Doppelverlinkungen.

## Nächste Punkte

- 0.7.1 auf einem realen Android-Gerät prüfen: TWINT-Direktzahlung/App-Wechsel, Lesbarkeit der Statusfarben und Grenzlinien bei passenden Abflussbereichen.
- Preisliste fotografieren und Vereinsbeiz-Warenkorb ergänzen.
- Nativen Bereich `Aktuelles` aus WordPress REST/RSS entwickeln.
- Google-Play-Voraussetzungen abschließen.
""", encoding="utf-8")

print("Applied PFVR Android 0.7.1 patch")
