from pathlib import Path

p = Path('Android/app/src/main/java/ch/pfvr/internapp/MainActivity.java')
s = p.read_text(encoding='utf-8')


def insert_after(anchor, text):
    global s
    if anchor not in s:
        raise SystemExit(f'anchor not found: {anchor[:80]}')
    s = s.replace(anchor, anchor + text, 1)


def replace_between(start, end, replacement):
    global s
    i = s.find(start)
    if i < 0:
        raise SystemExit(f'start marker not found: {start}')
    j = s.find(end, i)
    if j < 0:
        raise SystemExit(f'end marker not found: {end}')
    s = s[:i] + replacement + s[j:]


def replace_once(old, new):
    global s
    if old not in s:
        raise SystemExit(f'replacement target not found: {old[:100]}')
    s = s.replace(old, new, 1)


if 'PREF_RIVER_LOW' not in s:
    insert_after(
        '    private static final String PREF_BACKGROUND_REFRESH = "background_refresh";\n',
        '    private static final String PREF_RIVER_LOW = "river_low";\n'
        '    private static final String PREF_RIVER_WARN = "river_warn";\n'
        '    private static final String PREF_RIVER_ALARM = "river_alarm";\n'
        '    private static final float DEFAULT_RIVER_LOW = 400f;\n'
        '    private static final float DEFAULT_RIVER_WARN = 2500f;\n'
        '    private static final float DEFAULT_RIVER_ALARM = 3600f;\n'
    )

if 'STATUS_GOOD' not in s:
    insert_after(
        '    private static final int DARK_MUTED = Color.rgb(160,176,186);\n',
        '    private static final int STATUS_LOW = Color.rgb(43,142,166);\n'
        '    private static final int STATUS_GOOD = Color.rgb(22,134,58);\n'
        '    private static final int STATUS_WARN = Color.rgb(242,201,76);\n'
        '    private static final int STATUS_ALARM = Color.rgb(200,55,55);\n'
    )

replace_between(
    '    private LinearLayout riverCard(){',
    '    private boolean summerTraining',
'''    private LinearLayout riverCard(){
        LinearLayout c=card(); c.setOrientation(LinearLayout.VERTICAL); c.setPadding(dp(16),dp(15),dp(16),dp(14));
        String[] x=hydroSummary(); c.setOnClickListener(v->external("https://www.hydrodaten.admin.ch/de/seen-und-fluesse/stationen-und-daten/2091"));
        c.addView(txt(x[0],11,WATER,true));
        TextView main=txt(x[1],22,TEXT,true); main.setPadding(0,dp(5),0,dp(3)); c.addView(main);
        c.addView(txt(x[2],13,MUTED,false));

        double q=currentHydroValue("Q"); RiverStatus rs=riverStatus(q);
        TextView status=txt(rs.label,13,rs.fg,true); status.setGravity(Gravity.CENTER); status.setPadding(dp(12),dp(7),dp(12),dp(7)); status.setBackground(round(rs.bg,12));
        LinearLayout.LayoutParams slp=new LinearLayout.LayoutParams(-2,-2); slp.setMargins(0,dp(10),0,0); c.addView(status,slp);
        TextView limits=txt(String.format(Locale.GERMAN,"Niedrig < %.0f · Warn ab %.0f · Alarm ab %.0f m³/s",riverLow(),riverWarn(),riverAlarm()),10,MUTED,false); limits.setPadding(0,dp(6),0,0); c.addView(limits);

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

    private boolean summerTraining'''
)

replace_between(
    '    private String[] weatherSummary(){',
    '    private String weatherIcon',
'''    private String[] weatherSummary(){
        LocalDate d=nextTrainingDay(); String date=cap(d.format(DateTimeFormatter.ofPattern("EEEE, dd.MM.",Locale.GERMAN)))+" · "+trainingTime(d);
        String raw=prefs.getString(PREF_WEATHER_CACHE,""); long updated=prefs.getLong(PREF_WEATHER_UPDATED,0L); String source=prefs.getString(PREF_WEATHER_SOURCE,"MeteoSwiss ICON via Open-Meteo");
        if(raw.trim().isEmpty())return new String[]{"NÄCHSTES TRAINING",date,"Wetter wird geladen …","Prognose wird im Hintergrund aktualisiert.",source,"◌"};
        try{
            JSONObject h=new JSONObject(raw).getJSONObject("hourly"); JSONArray times=h.getJSONArray("time"),temp=h.getJSONArray("temperature_2m"),prob=h.getJSONArray("precipitation_probability"),prec=h.getJSONArray("precipitation"),code=h.getJSONArray("weather_code"),wind=h.getJSONArray("wind_speed_10m"),gust=h.getJSONArray("wind_gusts_10m"); JSONArray uv=h.optJSONArray("uv_index");
            double tFirst=Double.NaN,tLast=Double.NaN,psum=0,wmax=0,gmax=0,uvMax=Double.NaN; int pmax=0,wcode=-1,count=0; String prefix=d.toString()+"T"; int[] hours=trainingWeatherHours(d);
            for(int i=0;i<times.length();i++){
                String tm=times.optString(i,""); boolean inWindow=false; for(int hour:hours)if(tm.equals(prefix+String.format(Locale.ROOT,"%02d:00",hour))){inWindow=true;break;} if(!inWindow)continue;
                double tv=temp.optDouble(i,Double.NaN); if(count==0){tFirst=tv;wcode=code.optInt(i,-1);} tLast=tv;
                pmax=Math.max(pmax,prob.optInt(i,0)); psum+=Math.max(0,prec.optDouble(i,0)); wmax=Math.max(wmax,wind.optDouble(i,0)); gmax=Math.max(gmax,gust.optDouble(i,0));
                if(uv!=null){double u=uv.optDouble(i,Double.NaN);if(!Double.isNaN(u)&&(Double.isNaN(uvMax)||u>uvMax))uvMax=u;}
                count++;
            }
            if(count==0)return new String[]{"NÄCHSTES TRAINING",date,"Noch keine Prognose","Für diesen Trainingszeitraum liegen noch keine Stundenwerte vor.",weatherAge(source,updated),"◌"};
            String tempText=Double.isNaN(tFirst)?"":String.format(Locale.GERMAN,"%.0f °C",tFirst);
            if(!Double.isNaN(tLast)&&!Double.isNaN(tFirst)&&Math.abs(tLast-tFirst)>=1.0)tempText+=String.format(Locale.GERMAN," → %.0f °C",tLast);
            String main=tempText+(tempText.isEmpty()?"":" · ")+weatherCode(wcode);
            String details="Regen "+pmax+" % · "+String.format(Locale.GERMAN,"%.1f mm",psum)+"\nWind "+Math.round(wmax)+" km/h · Böen "+Math.round(gmax)+" km/h";
            if(!Double.isNaN(uvMax))details+="\nUV "+String.format(Locale.GERMAN,"%.1f",uvMax)+" · "+uvLabel(uvMax);
            return new String[]{"NÄCHSTES TRAINING",date,main,details,weatherAge(source,updated),weatherIcon(wcode)};
        }catch(Exception e){return new String[]{"NÄCHSTES TRAINING",date,"Gespeicherte Wetterdaten nicht lesbar","Letzter Stand bleibt erhalten, sobald wieder gültige Daten vorliegen.",weatherAge(source,updated),"◌"};}
    }

    private String uvLabel(double uv){if(uv<3)return "niedrig";if(uv<6)return "mässig";if(uv<8)return "hoch";if(uv<11)return "sehr hoch";return "extrem";}

    private String weatherIcon'''
)

replace_between(
    '    private String[] hydroSummary(){',
    '    private TrendSeries hydroSeries',
'''    private double currentHydroValue(String parameter){
        String raw=prefs.getString(PREF_HYDRO_CACHE,""); if(raw.isBlank())return Double.NaN;
        try{
            JSONArray a=new JSONObject(raw).getJSONObject("data").getJSONObject("water").getJSONObject("observations").getJSONArray("data_live");
            String latest="";double value=Double.NaN;
            for(int i=0;i<a.length();i++){JSONObject o=a.getJSONObject(i);if(!parameter.equals(o.optString("parameterName","")))continue;String t=o.optString("timestamp","");if(t.compareTo(latest)>0){latest=t;value=o.optDouble("value",Double.NaN);}}
            return value;
        }catch(Exception e){return Double.NaN;}
    }

    private float riverLow(){return prefs.getFloat(PREF_RIVER_LOW,DEFAULT_RIVER_LOW);}
    private float riverWarn(){return prefs.getFloat(PREF_RIVER_WARN,DEFAULT_RIVER_WARN);}
    private float riverAlarm(){return prefs.getFloat(PREF_RIVER_ALARM,DEFAULT_RIVER_ALARM);}
    private RiverStatus riverStatus(double q){
        if(Double.isNaN(q))return new RiverStatus("Status unbekannt",themeBg(Color.rgb(109,120,128)),Color.WHITE);
        if(q<riverLow())return new RiverStatus("Niedrig",STATUS_LOW,Color.WHITE);
        if(q>=riverAlarm())return new RiverStatus("Alarm",STATUS_ALARM,Color.WHITE);
        if(q>=riverWarn())return new RiverStatus("Warnung",STATUS_WARN,Color.rgb(23,34,43));
        return new RiverStatus("Gut",STATUS_GOOD,Color.WHITE);
    }

    private String[] hydroSummary(){
        String raw=prefs.getString(PREF_HYDRO_CACHE,""); long cache=prefs.getLong(PREF_HYDRO_UPDATED,0L);
        if(raw.isBlank())return new String[]{"RHEIN · BAFU 2091","Wird geladen …","Abfluss · Pegel · Temperatur","BAFU Live-Daten"};
        try{
            JSONArray a=new JSONObject(raw).getJSONObject("data").getJSONObject("water").getJSONObject("observations").getJSONArray("data_live");
            Map<String,Double> val=new HashMap<>();Map<String,String> ts=new HashMap<>();
            for(int i=0;i<a.length();i++){JSONObject o=a.getJSONObject(i);String p=o.optString("parameterName","");String t=o.optString("timestamp","");if(!(p.equals("Q")||p.equals("W")||p.equals("WT")))continue;if(!ts.containsKey(p)||t.compareTo(ts.get(p))>0){ts.put(p,t);val.put(p,o.optDouble("value",Double.NaN));}}
            double q=val.getOrDefault("Q",Double.NaN),w=val.getOrDefault("W",Double.NaN),wt=val.getOrDefault("WT",Double.NaN);String latest="";for(String t:ts.values())if(t.compareTo(latest)>0)latest=t;
            String main=Double.isNaN(q)?"Rhein Rheinfelden":String.format(Locale.GERMAN,"%.0f m³/s",q);
            StringBuilder sub=new StringBuilder();if(!Double.isNaN(w))sub.append(String.format(Locale.GERMAN,"Pegel %.2f m ü.M.",w));if(!Double.isNaN(wt)){if(sub.length()>0)sub.append("\n");sub.append(String.format(Locale.GERMAN,"Wasser %.1f °C",wt));}
            String stand="BAFU 2091";try{if(!latest.isBlank())stand+=" · Stand "+java.time.Instant.parse(latest).atZone(ZoneId.of("Europe/Zurich")).format(DateTimeFormatter.ofPattern("HH:mm"));}catch(Exception ignored){}
            if(cache>0&&(System.currentTimeMillis()-cache)>45*60000L)stand+=" · Cache";
            return new String[]{"RHEIN · BAFU 2091",main,sub.length()==0?"Messwerte derzeit unvollständig":sub.toString(),stand};
        }catch(Exception e){return new String[]{"RHEIN · BAFU 2091","Gespeicherter Stand","Messdaten nicht lesbar","BAFU · Cache"};}
    }

    private TrendSeries hydroSeries'''
)

replace_once(
    'String base="https://api.open-meteo.com/v1/forecast?latitude=47.5544&longitude=7.7940&hourly=temperature_2m,precipitation_probability,precipitation,weather_code,wind_speed_10m,wind_gusts_10m&timezone=Europe%2FZurich&forecast_days=8";',
    'String base="https://api.open-meteo.com/v1/forecast?latitude=47.5544&longitude=7.7940&hourly=temperature_2m,precipitation_probability,precipitation,weather_code,wind_speed_10m,wind_gusts_10m,uv_index&timezone=Europe%2FZurich&forecast_days=8";'
)

settings_insert = '''        section(b,"Rhein-Grenzwerte","Farbliche Einordnung des Abflusses an BAFU-Station 2091");
        LinearLayout limitsCard=card(); limitsCard.setOrientation(LinearLayout.VERTICAL); b.addView(limitsCard,margin(-1,-2,0,0,0,12));
        limitsCard.addView(txt("Abfluss-Ampel",16,TEXT,true));
        TextView ranges=txt(String.format(Locale.GERMAN,"🔵 Niedrig  < %.0f m³/s\\n🟢 Gut  %.0f–%.0f m³/s\\n🟡 Warnung  ab %.0f m³/s\\n🔴 Alarm  ab %.0f m³/s",riverLow(),riverLow(),riverWarn(),riverWarn(),riverAlarm()),13,MUTED,false); ranges.setPadding(0,dp(5),0,dp(10)); limitsCard.addView(ranges);
        TextView sourceInfo=txt("Standard: Warnung ab 2500 m³/s = BAFU Gefahrenstufe 2; Alarm ab 3600 m³/s = Gefahrenstufe 4. Der Niedrigwert 400 m³/s ist eine anpassbare App-Vorgabe, kein offizieller BAFU-Grenzwert.",11,MUTED,false); sourceInfo.setPadding(0,0,0,dp(10)); limitsCard.addView(sourceInfo);
        Button editLimits=btn("Grenzwerte ändern",Color.rgb(232,240,244),NAVY); editLimits.setOnClickListener(v->editRiverThresholds()); limitsCard.addView(editLimits,new LinearLayout.LayoutParams(-1,dp(44)));
        Button resetLimits=btn("Standardwerte wiederherstellen",Color.rgb(232,240,244),NAVY); resetLimits.setOnClickListener(v->{prefs.edit().remove(PREF_RIVER_LOW).remove(PREF_RIVER_WARN).remove(PREF_RIVER_ALARM).apply();navigate(Screen.SETTINGS);}); LinearLayout.LayoutParams rlp=new LinearLayout.LayoutParams(-1,dp(44));rlp.setMargins(0,dp(8),0,0);limitsCard.addView(resetLimits,rlp);

'''
replace_once('        section(b,"App",null);\n', settings_insert + '        section(b,"App",null);\n')
replace_once('Testversion 0.6.1 · 1.0.0 bleibt', 'Testversion 0.7.0 · 1.0.0 bleibt')

threshold_methods = '''    private void editRiverThresholds(){
        LinearLayout box=new LinearLayout(this);box.setOrientation(LinearLayout.VERTICAL);box.setPadding(dp(16),dp(4),dp(16),0);
        EditText low=thresholdInput("Niedrig unter m³/s",riverLow()),warn=thresholdInput("Warnung ab m³/s",riverWarn()),alarm=thresholdInput("Alarm ab m³/s",riverAlarm());
        box.addView(txt("Niedrig",12,MUTED,true));box.addView(low,new LinearLayout.LayoutParams(-1,dp(48)));TextView w1=txt("Warnung",12,MUTED,true);w1.setPadding(0,dp(10),0,0);box.addView(w1);box.addView(warn,new LinearLayout.LayoutParams(-1,dp(48)));TextView a1=txt("Alarm",12,MUTED,true);a1.setPadding(0,dp(10),0,0);box.addView(a1);box.addView(alarm,new LinearLayout.LayoutParams(-1,dp(48)));
        new AlertDialog.Builder(this,dialogTheme()).setTitle("Rhein-Grenzwerte").setView(box).setPositiveButton("Speichern",(d,w)->{
            try{float l=Float.parseFloat(low.getText().toString().replace(',','.')),wa=Float.parseFloat(warn.getText().toString().replace(',','.')),al=Float.parseFloat(alarm.getText().toString().replace(',','.'));if(l<0||!(l<wa&&wa<al))throw new Exception();prefs.edit().putFloat(PREF_RIVER_LOW,l).putFloat(PREF_RIVER_WARN,wa).putFloat(PREF_RIVER_ALARM,al).apply();navigate(Screen.SETTINGS);}catch(Exception e){Toast.makeText(this,"Grenzwerte müssen aufsteigend sein: Niedrig < Warnung < Alarm.",Toast.LENGTH_LONG).show();}
        }).setNegativeButton("Abbrechen",null).show();
    }
    private EditText thresholdInput(String hint,float value){EditText e=new EditText(this);e.setHint(hint);e.setText(String.format(Locale.US,"%.0f",value));e.setSingleLine(true);e.setInputType(InputType.TYPE_CLASS_NUMBER|InputType.TYPE_NUMBER_FLAG_DECIMAL);e.setTextColor(themeText(TEXT));e.setHintTextColor(themeText(MUTED));e.setBackground(round(Color.rgb(238,243,246),12));e.setPadding(dp(12),0,dp(12),0);return e;}

'''
replace_once('    private View eventScreen() {\n', threshold_methods + '    private View eventScreen() {\n')

replace_between(
    '    private void showPaymentQr(EditText amountInput) {',
    '    private Bitmap makeSwissQr',
'''    private void showPaymentQr(EditText amountInput) {
        String a=amount(amountInput==null?null:amountInput.getText().toString());
        if(a==null){Toast.makeText(this,"Bitte einen gültigen CHF-Betrag eingeben oder Feld leer/0 für offenen Betrag lassen.",Toast.LENGTH_LONG).show();return;}
        try {
            Bitmap qr=makeSwissQr(a);
            pendingQrBitmap=qr;
            LinearLayout box=new LinearLayout(this); box.setOrientation(LinearLayout.VERTICAL); box.setPadding(dp(16),dp(8),dp(16),0);
            ImageView image=new ImageView(this); image.setImageBitmap(qr); image.setAdjustViewBounds(true); image.setScaleType(ImageView.ScaleType.FIT_CENTER); box.addView(image,new LinearLayout.LayoutParams(-1,dp(330)));
            String amountLine=a.isBlank()?"Betrag offen · in Banking-App eingeben":"CHF "+a;
            TextView details=txt(amountLine+"\n"+CLUB_PAYEE+"\n"+CLUB_IBAN+"\n"+CLUB_PAYMENT_NOTE,14,TEXT,false); details.setGravity(Gravity.CENTER); details.setPadding(0,dp(8),0,dp(4)); box.addView(details);
            TextView note=txt("QR speichern und in der Banking-App aus Datei/Foto importieren, sofern die Bank das unterstützt. Yuh kann QR-Rechnungen als Dokument/Bild einlesen.",12,MUTED,false); note.setGravity(Gravity.CENTER); box.addView(note);
            new AlertDialog.Builder(this,dialogTheme())
                    .setTitle("Bankzahlung · Swiss QR")
                    .setView(box)
                    .setPositiveButton("Banking-App öffnen",(d,w)->openPreferred(false,amountInput))
                    .setNeutralButton("QR speichern",(d,w)->saveQr(a))
                    .setNegativeButton("Schliessen",null)
                    .show();
        } catch(Exception e) {
            Toast.makeText(this,"Swiss QR konnte nicht erzeugt werden.",Toast.LENGTH_LONG).show();
        }
    }

    private Bitmap makeSwissQr'''
)
replace_once('save.putExtra(Intent.EXTRA_TITLE,"PFVR-Zahlung-CHF-"+amount.replace(\'.\',\'_\')+".png");', 'save.putExtra(Intent.EXTRA_TITLE,amount.isBlank()?"PFVR-Zahlung-offener-Betrag.png":"PFVR-Zahlung-CHF-"+amount.replace(\'.\',\'_\')+".png");')
replace_once('private void copyAmount(EditText input) { String a=amount(input==null?null:input.getText().toString()); if(a==null){Toast.makeText(this,"Banking-App geöffnet. Betrag noch nicht eingegeben.",Toast.LENGTH_SHORT).show();return;} copy("PFVR Betrag",a,"CHF "+a+" kopiert"); }', 'private void copyAmount(EditText input) { String a=amount(input==null?null:input.getText().toString()); if(a==null||a.isBlank()){Toast.makeText(this,"Banking-App geöffnet. Betrag dort eingeben.",Toast.LENGTH_SHORT).show();return;} copy("PFVR Betrag",a,"CHF "+a+" kopiert"); }')
replace_once('private String amount(String raw) { if(raw==null)return null; try{double n=Double.parseDouble(raw.trim().replace(\',\',\'.\')); if(n<=0||n>100000)return null; return String.format(Locale.US,"%.2f",n);}catch(Exception e){return null;} }', 'private String amount(String raw) { if(raw==null||raw.trim().isEmpty())return ""; try{double n=Double.parseDouble(raw.trim().replace(\',\',\'.\')); if(n<0||n>100000)return null; if(n==0)return ""; return String.format(Locale.US,"%.2f",n);}catch(Exception e){return null;} }')
replace_once('Button qr=btn("Swiss QR mit Betrag erstellen",NAVY,Color.WHITE);', 'Button qr=btn("Swiss QR erstellen",NAVY,Color.WHITE);')
replace_once('TextView hi=txt("Der Swiss QR enthält Empfänger, IBAN, Betrag und Zahlungszweck. So gehen die Zahlungsdaten vollständig mit.",12,MUTED,false);', 'TextView hi=txt("Der Swiss QR enthält Empfänger, IBAN und Zahlungszweck. Betrag leer oder 0 = offener Betrag, der später in der Banking-App eingegeben wird.",12,MUTED,false);')
replace_once('if(a!=null)x+="\\nCHF "+a;', 'if(a!=null&&!a.isBlank())x+="\\nCHF "+a;')

replace_between(
    '    private View club() {',
    '    private View internal() {',
'''    private View club() {
        ScrollView scroll=new ScrollView(this); LinearLayout b=body(); scroll.addView(b);
        LinearLayout top=card(); top.setGravity(Gravity.CENTER_VERTICAL); ImageView logo=new ImageView(this); logo.setImageResource(R.drawable.pfvr_logo); logo.setScaleType(ImageView.ScaleType.CENTER_CROP); top.addView(logo,new LinearLayout.LayoutParams(dp(82),dp(82))); LinearLayout info=new LinearLayout(this); info.setOrientation(LinearLayout.VERTICAL); info.setPadding(dp(15),0,0,0); info.addView(txt("Pontonierfahrverein Rheinfelden",19,TEXT,true)); info.addView(txt("Gegründet 1896 · Sport und Vereinsleben am Rhein",13,MUTED,false)); top.addView(info,new LinearLayout.LayoutParams(0,-2,1)); b.addView(top,margin(-1,-2,0,4,0,18));

        section(b,"Über den Verein",null);
        LinearLayout about=card();about.setOrientation(LinearLayout.VERTICAL);b.addView(about,margin(-1,-2,0,0,0,12));
        about.addView(txt("Seit 1896 auf dem Rhein",18,TEXT,true));
        TextView a=txt("Beim Pontonierfahren verbinden sich präzise Bootsführung, Kraft, Technik und Teamarbeit. Der PFVR trainiert auf dem Rhein in Rheinfelden, nimmt an Wettfahren teil und pflegt zugleich ein aktives Vereinsleben sowie die Ausbildung des Nachwuchses.",14,MUTED,false);a.setPadding(0,dp(7),0,0);about.addView(a);

        section(b,"Geschichte","Einige feste Meilensteine direkt in der App");
        b.addView(milestone("1896","Gründung","Beginn der Rheinfelder Pontonier-Tradition."));
        b.addView(milestone("1971","75 Jahre","Jubiläumswettfahren in Rheinfelden."));
        b.addView(milestone("1996","100 Jahre","Schweizerisches Jungpontonierwettfahren zum 100-Jahr-Jubiläum."));
        b.addView(milestone("2021","125 Jahre","Jubiläumsjahr und Jungfernfahrt eines neuen Kunststoffweidlings; laut Stadt Rheinfelden war der PFVR damit der erste Pontonierfahrverein der Schweiz mit einem privaten Kunststoffweidling."));
        b.addView(milestone("2023","Schweizer-Meisterschaft","Der PFVR richtete die Schweizer-Meisterschaft auf dem Rhein in Rheinfelden aus."));

        section(b,"Aktuell & Organisation",null);
        b.addView(action("Vorstand","Ansprechpersonen und Funktionen","Öffnen",v->openInApp(BOARD,"Vorstand")));
        b.addView(action("Jahresprogramm","Originalseite und Kalenderhinweise","Öffnen",v->openInApp(PROGRAM,"Jahresprogramm")));
        b.addView(action("News-Archiv","Beiträge auf pfvr.ch","Öffnen",v->openInApp(NEWS,"News-Archiv")));
        b.addView(action("Interner Bereich","An-/Abmeldung und interne PFVR-Seite","Öffnen",v->navigate(Screen.INTERNAL)));

        section(b,"Kontakt",null); b.addView(contact("Depot","Rheinweg 42\n4310 Rheinfelden","Route",v->openMap())); b.addView(contact("Telefon","076 209 18 96","Anrufen",v->startActivity(new Intent(Intent.ACTION_DIAL,Uri.parse("tel:+41762091896"))))); b.addView(contact("E-Mail","info@pfvr.ch","Schreiben",v->startActivity(new Intent(Intent.ACTION_SENDTO,Uri.parse("mailto:info@pfvr.ch"))))); b.addView(contact("Kontaktseite","pfvr.ch/kontakt","Öffnen",v->openInApp(CONTACT,"Kontakt"))); return scroll;
    }

    private View milestone(String year,String title,String detail){
        LinearLayout c=card();c.setGravity(Gravity.CENTER_VERTICAL);c.setLayoutParams(margin(-1,-2,0,0,0,9));
        TextView y=txt(year,15,Color.WHITE,true);y.setGravity(Gravity.CENTER);y.setBackground(round(NAVY,13));c.addView(y,new LinearLayout.LayoutParams(dp(62),dp(48)));
        LinearLayout text=new LinearLayout(this);text.setOrientation(LinearLayout.VERTICAL);text.setPadding(dp(13),0,0,0);text.addView(txt(title,15,TEXT,true));text.addView(txt(detail,12,MUTED,false));c.addView(text,new LinearLayout.LayoutParams(0,-2,1));return c;
    }

    private View internal() {'''
)

# Add dual-axis chart and river status value object before the existing TrendView.
insert_after(
    '    private static class TrendSeries {List<Long> times=new ArrayList<>();List<Double> values=new ArrayList<>();}\n',
'''    private static class RiverStatus {final String label;final int bg,fg;RiverStatus(String l,int b,int f){label=l;bg=b;fg=f;}}\n    private class DualTrendView extends View {\n        private final TrendSeries leftSeries,rightSeries; private final String leftUnit,rightUnit; private final int leftColor,rightColor;\n        private final Paint line=new Paint(Paint.ANTI_ALIAS_FLAG),grid=new Paint(Paint.ANTI_ALIAS_FLAG),label=new Paint(Paint.ANTI_ALIAS_FLAG),axis=new Paint(Paint.ANTI_ALIAS_FLAG);\n        DualTrendView(Context c,TrendSeries l,TrendSeries r,String lu,String ru,int lc,int rc){super(c);leftSeries=l;rightSeries=r;leftUnit=lu;rightUnit=ru;leftColor=lc;rightColor=rc;line.setStrokeWidth(dp(2));line.setStyle(Paint.Style.STROKE);grid.setStrokeWidth(dp(1));label.setTextSize(9*getResources().getDisplayMetrics().scaledDensity);axis.setTextSize(10*getResources().getDisplayMetrics().scaledDensity);axis.setTypeface(Typeface.DEFAULT_BOLD);}\n        @Override protected void onDraw(Canvas canvas){super.onDraw(canvas);if(leftSeries.values.size()<2||rightSeries.values.size()<2)return;float w=getWidth(),h=getHeight(),left=dp(50),right=w-dp(54),top=dp(20),bottom=h-dp(28);long minT=Math.min(leftSeries.times.get(0),rightSeries.times.get(0)),maxT=Math.max(leftSeries.times.get(leftSeries.times.size()-1),rightSeries.times.get(rightSeries.times.size()-1));if(maxT<=minT)maxT=minT+1;double[] lr=range(leftSeries.values),rr=range(rightSeries.values);grid.setColor(darkMode?Color.rgb(58,72,82):Color.rgb(220,229,234));label.setColor(themeText(MUTED));axis.setColor(themeText(MUTED));for(int i=0;i<3;i++){float y=top+(bottom-top)*i/2f;canvas.drawLine(left,y,right,y,grid);}\n            ZoneId zone=ZoneId.of("Europe/Zurich");ZonedDateTime firstZ=java.time.Instant.ofEpochMilli(minT).atZone(zone),lastZ=java.time.Instant.ofEpochMilli(maxT).atZone(zone);LocalDate tick=firstZ.toLocalDate().plusDays(1);DateTimeFormatter df=DateTimeFormatter.ofPattern("EE",Locale.GERMAN);while(!tick.isAfter(lastZ.toLocalDate())){long tt=tick.atStartOfDay(zone).toInstant().toEpochMilli();if(tt>=minT&&tt<=maxT){float x=left+(right-left)*(tt-minT)/(float)(maxT-minT);canvas.drawLine(x,top,x,bottom,grid);String lab=tick.format(df);canvas.drawText(lab,x-label.measureText(lab)/2f,h-dp(5),label);}tick=tick.plusDays(1);}\n            drawSeries(canvas,leftSeries,leftColor,left,right,top,bottom,minT,maxT,lr[0],lr[1]);drawSeries(canvas,rightSeries,rightColor,left,right,top,bottom,minT,maxT,rr[0],rr[1]);canvas.drawText(fmt(lr[1]),dp(2),top+dp(4),label);canvas.drawText(fmt(lr[0]),dp(2),bottom,label);String rmax=fmt(rr[1]),rmin=fmt(rr[0]);canvas.drawText(rmax,w-dp(2)-label.measureText(rmax),top+dp(4),label);canvas.drawText(rmin,w-dp(2)-label.measureText(rmin),bottom,label);canvas.drawText(leftUnit,dp(2),dp(10),axis);canvas.drawText(rightUnit,w-dp(2)-axis.measureText(rightUnit),dp(10),axis);}\n        private void drawSeries(Canvas c,TrendSeries s,int color,float left,float right,float top,float bottom,long minT,long maxT,double min,double max){Path p=new Path();for(int i=0;i<s.values.size();i++){float x=left+(right-left)*(s.times.get(i)-minT)/(float)(maxT-minT);float y=(float)(bottom-(s.values.get(i)-min)/(max-min)*(bottom-top));if(i==0)p.moveTo(x,y);else p.lineTo(x,y);}line.setColor(color);c.drawPath(p,line);}\n        private double[] range(List<Double> values){double min=Double.POSITIVE_INFINITY,max=Double.NEGATIVE_INFINITY;for(double v:values){min=Math.min(min,v);max=Math.max(max,v);}if(!(max>min)){max=min+1;}double pad=(max-min)*0.06;return new double[]{min-pad,max+pad};}\n        private String fmt(double v){return Math.abs(v)>=100?String.format(Locale.GERMAN,"%.0f",v):String.format(Locale.GERMAN,"%.2f",v);}\n    }\n'''
)

p.write_text(s, encoding='utf-8')
print('Applied Android 0.7.0 changes')
