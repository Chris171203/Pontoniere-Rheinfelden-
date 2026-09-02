from pathlib import Path

p = Path('Android/app/src/main/java/ch/pfvr/internapp/MainActivity.java')
s = p.read_text(encoding='utf-8')

if 'PREF_RIVER_SLOT1_STATION' in s and 'private View cashItemRow(CashCatalog.Item item)' in s:
    print('Android 0.9.0 MainActivity changes already present')
    raise SystemExit(0)


def replace_once(old, new):
    global s
    if old not in s:
        raise SystemExit('marker not found: ' + old[:100])
    s = s.replace(old, new, 1)


def replace_between(start, end, replacement):
    global s
    i = s.find(start)
    if i < 0:
        raise SystemExit('start marker not found: ' + start)
    j = s.find(end, i)
    if j < 0:
        raise SystemExit('end marker not found: ' + end)
    s = s[:i] + replacement + s[j:]


replace_once(
    'import androidx.work.WorkManager;\n',
    'import androidx.work.WorkManager;\nimport androidx.core.content.FileProvider;\n'
)
replace_once(
    'import java.io.BufferedReader;\n',
    'import java.io.BufferedReader;\nimport java.io.File;\nimport java.io.FileOutputStream;\n'
)

replace_once(
    '    private static final String PREF_RIVER_METRIC = "river_metric";\n',
    '    private static final String PREF_RIVER_METRIC = "river_metric";\n'
    '    private static final String PREF_RIVER_SLOT1_STATION = "river_slot1_station";\n'
    '    private static final String PREF_RIVER_SLOT1_METRIC = "river_slot1_metric";\n'
    '    private static final String PREF_RIVER_SLOT2_STATION = "river_slot2_station";\n'
    '    private static final String PREF_RIVER_SLOT2_METRIC = "river_slot2_metric";\n'
)

replace_once(
    '    private Button bankButton;\n',
    '    private Button bankButton;\n'
    '    private final Map<String,Integer> cashCart = new LinkedHashMap<>();\n'
    '    private CashCatalog.Catalog cashCatalog;\n'
    '    private LinearLayout cashSummaryContainer;\n'
    '    private TextView cashTotalView;\n'
)

live_block = r'''    private View liveInfoRow(){
        LinearLayout stack=new LinearLayout(this);
        stack.setOrientation(LinearLayout.VERTICAL);
        stack.setPadding(0,0,0,dp(2));
        LinearLayout weather=weatherCard();
        stack.addView(weather,margin(-1,-2,0,0,0,10));

        TextView rangeLabel=txt("RHEIN · ZEITRAUM",10,MUTED,true);
        rangeLabel.setPadding(dp(2),dp(2),0,dp(5));
        stack.addView(rangeLabel);
        stack.addView(riverRangeSelector(riverRange()),new LinearLayout.LayoutParams(-1,dp(42)));

        LinearLayout first=riverCard(1);
        LinearLayout.LayoutParams firstParams=margin(-1,-2,0,10,0,10);
        stack.addView(first,firstParams);
        LinearLayout second=riverCard(2);
        stack.addView(second,margin(-1,-2,0,0,0,4));
        return stack;
    }

    private LinearLayout weatherCard(){
        LinearLayout c=card(); c.setOrientation(LinearLayout.VERTICAL); c.setPadding(dp(16),dp(15),dp(16),dp(14));
        String[] x=weatherSummary();
        c.addView(txt(x[0],11,WATER,true));
        LinearLayout row=new LinearLayout(this); row.setGravity(Gravity.CENTER_VERTICAL); row.setPadding(0,dp(5),0,dp(5)); c.addView(row);
        TextView icon=txt(x[5],38,themeText(TEXT),false); icon.setGravity(Gravity.CENTER); row.addView(icon,new LinearLayout.LayoutParams(dp(58),dp(58)));
        LinearLayout info=new LinearLayout(this); info.setOrientation(LinearLayout.VERTICAL); info.setPadding(dp(7),0,0,0); row.addView(info,new LinearLayout.LayoutParams(0,-2,1));
        info.addView(txt(x[1],15,MUTED,true));
        TextView main=txt(x[2],21,TEXT,true); main.setPadding(0,dp(2),0,0); info.addView(main);
        TextView details=txt(x[3],13,MUTED,false); details.setPadding(0,dp(5),0,0); c.addView(details);
        TextView src=txt(x[4],10,Color.rgb(126,140,150),false); src.setPadding(0,dp(8),0,0); c.addView(src);
        return c;
    }

    private LinearLayout riverCard(int slot){
        HydroStation station=riverStation(slot);
        RiverMetric metric=riverMetricForSlot(slot);
        RiverRange range=riverRange();
        String[] summary=hydroSummary(station);
        TrendSeries series=hydroSeries(station,metric.parameter,range);
        HydroMath.Stats stats=HydroMath.stats(series.values);
        double flow=currentHydroValue(station,"Q");
        double primary=currentHydroValue(station,metric.parameter);
        RiverStatus status=riverStatus(station,flow);

        LinearLayout card=card();
        card.setOrientation(LinearLayout.VERTICAL);
        card.setPadding(dp(16),dp(15),dp(16),dp(15));

        LinearLayout eyebrow=new LinearLayout(this);
        eyebrow.setGravity(Gravity.CENTER_VERTICAL);
        eyebrow.addView(txt("RHEIN · "+station.label.toUpperCase(Locale.GERMAN)+" · "+station.id,11,WATER,true),new LinearLayout.LayoutParams(0,-2,1));
        TextView sourceLink=txt("BAFU  ↗",11,WATER,true);
        sourceLink.setGravity(Gravity.END);
        sourceLink.setPadding(dp(8),dp(4),0,dp(4));
        sourceLink.setOnClickListener(v->external(station.stationUrl()));
        eyebrow.addView(sourceLink,new LinearLayout.LayoutParams(-2,-2));
        card.addView(eyebrow);

        LinearLayout headline=new LinearLayout(this);
        headline.setGravity(Gravity.CENTER_VERTICAL);
        headline.setPadding(0,dp(4),0,dp(1));
        card.addView(headline);

        LinearLayout valueBox=new LinearLayout(this);
        valueBox.setGravity(Gravity.BOTTOM);
        TextView value=txt(Double.isFinite(primary)?formatMetric(metric,primary):"–",34,TEXT,true);
        valueBox.addView(value);
        TextView unit=txt(metric.unit,14,MUTED,true);
        unit.setPadding(dp(5),0,0,dp(5));
        valueBox.addView(unit);
        headline.addView(valueBox,new LinearLayout.LayoutParams(0,-2,1));
        headline.addView(riverStatusPill(status),new LinearLayout.LayoutParams(-2,-2));

        TextView metricName=txt(metric.label+" · Hauptwert dieser Kachel",11,MUTED,true);
        metricName.setPadding(0,0,0,dp(3));
        card.addView(metricName);
        String secondaryText=hydroSecondary(station,metric);
        if(!secondaryText.isBlank()){
            TextView secondary=txt(secondaryText,12,MUTED,false);
            secondary.setPadding(0,0,0,dp(5));
            card.addView(secondary);
        }

        TextView thresholdHint=txt(String.format(Locale.GERMAN,"Abflussstatus · Niedrig < %.0f · Warn ab %.0f · Alarm ab %.0f m³/s",riverLow(station),riverWarn(station),riverAlarm(station)),10,MUTED,false);
        thresholdHint.setPadding(0,dp(2),0,dp(8));
        card.addView(thresholdHint);

        if(stats.isValid()&&stats.count>=2){
            card.addView(riverMetrics(stats,range,metric),margin(-1,-2,0,2,0,10));
        }

        LinearLayout chartTitle=new LinearLayout(this);
        chartTitle.setGravity(Gravity.CENTER_VERTICAL);
        chartTitle.addView(txt(metric.label,15,TEXT,true),new LinearLayout.LayoutParams(0,-2,1));
        chartTitle.addView(txt(metric.unit+" · "+range.label,10,MUTED,false),new LinearLayout.LayoutParams(-2,-2));
        card.addView(chartTitle);

        if(series.values.size()>=2){
            RiverTrendView chart=new RiverTrendView(this,series,metric,range,station);
            card.addView(chart,new LinearLayout.LayoutParams(-1,dp(190)));
            TextView hint=txt("Diagramm berühren, um Einzelwerte anzuzeigen.",10,MUTED,false);
            hint.setGravity(Gravity.CENTER);
            hint.setPadding(0,dp(2),0,0);
            card.addView(hint);
        }else{
            TextView missing=txt(range==RiverRange.HOUR?"Für die letzte Stunde liegen noch nicht genug Livewerte vor.":"Für diesen Zeitraum liegen noch nicht genug Messwerte vor.",12,MUTED,false);
            missing.setGravity(Gravity.CENTER);
            missing.setPadding(dp(8),dp(22),dp(8),dp(22));
            missing.setBackground(round(Color.rgb(238,243,246),14));
            LinearLayout.LayoutParams missingParams=new LinearLayout.LayoutParams(-1,-2);
            missingParams.setMargins(0,dp(8),0,0);
            card.addView(missing,missingParams);
        }

        TextView src=txt(summary[3]+" · "+range.sourceLabel,10,Color.rgb(126,140,150),false);
        src.setPadding(0,dp(10),0,0);
        card.addView(src);
        return card;
    }

    private HydroStation riverStation(int slot){
        String key=slot==1?PREF_RIVER_SLOT1_STATION:PREF_RIVER_SLOT2_STATION;
        HydroStation fallback=slot==1?HydroStation.BASEL_RHEINHALLE:HydroStation.RHEINFELDEN;
        return HydroStation.from(prefs.getString(key,fallback.id),fallback);
    }

    private RiverMetric riverMetricForSlot(int slot){
        HydroStation station=riverStation(slot);
        String key=slot==1?PREF_RIVER_SLOT1_METRIC:PREF_RIVER_SLOT2_METRIC;
        RiverMetric metric=RiverMetric.from(prefs.getString(key,RiverMetric.FLOW.parameter));
        if(metric==RiverMetric.TEMPERATURE&&!station.supportsTemperature)return RiverMetric.FLOW;
        return metric;
    }

    private String hydroSecondary(HydroStation station,RiverMetric primary){
        StringBuilder out=new StringBuilder();
        if(primary!=RiverMetric.FLOW)appendSecondary(out,"Abfluss",currentHydroValue(station,"Q"),RiverMetric.FLOW);
        if(primary!=RiverMetric.LEVEL)appendSecondary(out,"Pegel",currentHydroValue(station,"W"),RiverMetric.LEVEL);
        if(station.supportsTemperature&&primary!=RiverMetric.TEMPERATURE)appendSecondary(out,"Wasser",currentHydroValue(station,"WT"),RiverMetric.TEMPERATURE);
        return out.toString();
    }

    private void appendSecondary(StringBuilder out,String label,double value,RiverMetric metric){
        if(!Double.isFinite(value))return;
        if(out.length()>0)out.append("   ·   ");
        out.append(label).append(' ').append(formatMetric(metric,value)).append(' ').append(metric.unit);
    }

'''
replace_between('    private View liveInfoRow(){', '    private RiverRange riverRange()', live_block)

threshold_block = r'''    private View thresholdGrid(){return thresholdGrid(HydroStation.RHEINFELDEN);}

    private View thresholdGrid(HydroStation station){
        LinearLayout stack=new LinearLayout(this);
        stack.setOrientation(LinearLayout.VERTICAL);
        LinearLayout first=new LinearLayout(this);
        addThresholdTile(first,"Niedrig",String.format(Locale.GERMAN,"< %.0f",riverLow(station)),STATUS_LOW);
        addThresholdTile(first,"Gut",String.format(Locale.GERMAN,"%.0f – < %.0f",riverLow(station),riverWarn(station)),STATUS_GOOD);
        stack.addView(first,new LinearLayout.LayoutParams(-1,-2));
        LinearLayout second=new LinearLayout(this);
        addThresholdTile(second,"Warnung",String.format(Locale.GERMAN,"%.0f – < %.0f",riverWarn(station),riverAlarm(station)),STATUS_WARN);
        addThresholdTile(second,"Alarm",String.format(Locale.GERMAN,"ab %.0f",riverAlarm(station)),STATUS_ALARM);
        LinearLayout.LayoutParams secondParams=new LinearLayout.LayoutParams(-1,-2);
        secondParams.setMargins(0,dp(6),0,0);
        stack.addView(second,secondParams);
        return stack;
    }

'''
replace_between('    private View thresholdGrid(){', '    private void addThresholdTile(', threshold_block)

hydro_block = r'''    private double currentHydroValue(String parameter){return currentHydroValue(HydroStation.RHEINFELDEN,parameter);}

    private double currentHydroValue(HydroStation station,String parameter){
        String raw=prefs.getString(station.liveCacheKey(),"");
        if(raw.isBlank())return Double.NaN;
        try{
            JSONArray data=new JSONObject(raw).getJSONObject("data").getJSONObject("water").getJSONObject("observations").getJSONArray("data_live");
            String latest="";
            double value=Double.NaN;
            for(int i=0;i<data.length();i++){
                JSONObject row=data.getJSONObject(i);
                if(!parameter.equals(row.optString("parameterName","")))continue;
                String timestamp=row.optString("timestamp","");
                if(timestamp.compareTo(latest)>0){latest=timestamp;value=row.optDouble("value",Double.NaN);}
            }
            return value;
        }catch(Exception ignored){return Double.NaN;}
    }

    private float riverLow(){return riverLow(HydroStation.RHEINFELDEN);}
    private float riverWarn(){return riverWarn(HydroStation.RHEINFELDEN);}
    private float riverAlarm(){return riverAlarm(HydroStation.RHEINFELDEN);}

    private float riverLow(HydroStation station){
        if(prefs.contains(station.lowPreferenceKey()))return prefs.getFloat(station.lowPreferenceKey(),station.defaultLow);
        if(station==HydroStation.RHEINFELDEN&&prefs.contains(PREF_RIVER_LOW))return prefs.getFloat(PREF_RIVER_LOW,station.defaultLow);
        return station.defaultLow;
    }
    private float riverWarn(HydroStation station){
        if(prefs.contains(station.warnPreferenceKey()))return prefs.getFloat(station.warnPreferenceKey(),station.defaultWarn);
        if(station==HydroStation.RHEINFELDEN&&prefs.contains(PREF_RIVER_WARN))return prefs.getFloat(PREF_RIVER_WARN,station.defaultWarn);
        return station.defaultWarn;
    }
    private float riverAlarm(HydroStation station){
        if(prefs.contains(station.alarmPreferenceKey()))return prefs.getFloat(station.alarmPreferenceKey(),station.defaultAlarm);
        if(station==HydroStation.RHEINFELDEN&&prefs.contains(PREF_RIVER_ALARM))return prefs.getFloat(PREF_RIVER_ALARM,station.defaultAlarm);
        return station.defaultAlarm;
    }

    private RiverStatus riverStatus(double flow){return riverStatus(HydroStation.RHEINFELDEN,flow);}
    private RiverStatus riverStatus(HydroStation station,double flow){
        if(Double.isNaN(flow))return new RiverStatus("Keine Daten",Color.rgb(109,120,128),Color.WHITE);
        if(flow<riverLow(station))return new RiverStatus("Niedrig",STATUS_LOW,Color.WHITE);
        if(flow>=riverAlarm(station))return new RiverStatus("Alarm",STATUS_ALARM,Color.WHITE);
        if(flow>=riverWarn(station))return new RiverStatus("Warnung",STATUS_WARN,Color.rgb(23,34,43));
        return new RiverStatus("Gut",STATUS_GOOD,Color.WHITE);
    }

    private String[] hydroSummary(){return hydroSummary(HydroStation.RHEINFELDEN);}
    private String[] hydroSummary(HydroStation station){
        String raw=prefs.getString(station.liveCacheKey(),"");
        long cache=prefs.getLong(station.liveUpdatedKey(),0L);
        String title="RHEIN · "+station.label+" · "+station.id;
        if(raw.isBlank())return new String[]{title,"Wird geladen …","Abfluss · Pegel"+(station.supportsTemperature?" · Temperatur":""),"BAFU Live-Daten"};
        try{
            JSONArray data=new JSONObject(raw).getJSONObject("data").getJSONObject("water").getJSONObject("observations").getJSONArray("data_live");
            Map<String,Double> values=new HashMap<>();
            Map<String,String> timestamps=new HashMap<>();
            for(int i=0;i<data.length();i++){
                JSONObject row=data.getJSONObject(i);
                String parameter=row.optString("parameterName","");
                String timestamp=row.optString("timestamp","");
                if(!(parameter.equals("Q")||parameter.equals("W")||parameter.equals("WT")))continue;
                if(!timestamps.containsKey(parameter)||timestamp.compareTo(timestamps.get(parameter))>0){timestamps.put(parameter,timestamp);values.put(parameter,row.optDouble("value",Double.NaN));}
            }
            double q=values.getOrDefault("Q",Double.NaN),w=values.getOrDefault("W",Double.NaN),wt=values.getOrDefault("WT",Double.NaN);
            String latest="";for(String timestamp:timestamps.values())if(timestamp.compareTo(latest)>0)latest=timestamp;
            String main=Double.isNaN(q)?station.label:String.format(Locale.GERMAN,"%.0f m³/s",q);
            StringBuilder sub=new StringBuilder();
            if(!Double.isNaN(w))sub.append(String.format(Locale.GERMAN,"Pegel %.2f m ü.M.",w));
            if(station.supportsTemperature&&!Double.isNaN(wt)){if(sub.length()>0)sub.append("\n");sub.append(String.format(Locale.GERMAN,"Wasser %.1f °C",wt));}
            String stand="BAFU "+station.id;
            try{if(!latest.isBlank())stand+=" · Stand "+java.time.Instant.parse(latest).atZone(ZoneId.of("Europe/Zurich")).format(DateTimeFormatter.ofPattern("HH:mm"));}catch(Exception ignored){}
            if(cache>0&&(System.currentTimeMillis()-cache)>45*60000L)stand+=" · Cache";
            return new String[]{title,main,sub.length()==0?"Messwerte derzeit unvollständig":sub.toString(),stand};
        }catch(Exception ignored){return new String[]{title,"Gespeicherter Stand","Messdaten nicht lesbar","BAFU · Cache"};}
    }

    private TrendSeries hydroSeries(String parameter,RiverRange range){return hydroSeries(HydroStation.RHEINFELDEN,parameter,range);}
    private TrendSeries hydroSeries(HydroStation station,String parameter,RiverRange range){
        TrendSeries result=new TrendSeries();
        TreeMap<Long,Double> points=new TreeMap<>();
        String live=prefs.getString(station.liveCacheKey(),"");
        String fine=prefs.getString(station.fineCacheKey(),"");
        String history=prefs.getString(station.historyCacheKey(),"");

        if(range==RiverRange.HOUR){
            appendHydroPoints(points,live,"data_live",parameter);
        }else if(range==RiverRange.DAY){
            appendHydroPoints(points,fine,"data_10min_mean",parameter);
            long after=points.isEmpty()?Long.MIN_VALUE:points.lastKey();
            appendHydroTail(points,live,"data_live",parameter,after,9L*60L*1000L);
            if(points.size()<2)appendHydroPoints(points,history,"data_1hour_mean",parameter);
        }else{
            appendHydroPoints(points,history,"data_1hour_mean",parameter);
            HydroPoint latest=latestHydroPoint(live,"data_live",parameter);
            if(latest!=null)points.put(latest.time,latest.value);
        }
        if(points.size()<2)appendHydroPoints(points,live,"data_live",parameter);
        if(points.isEmpty())return result;

        long newest=points.lastKey();
        long cutoff=newest-range.windowMs;
        for(Map.Entry<Long,Double> entry:points.entrySet()){
            if(entry.getKey()<cutoff)continue;
            result.times.add(entry.getKey());
            result.values.add(entry.getValue());
        }
        return result;
    }

'''
replace_between('    private double currentHydroValue(String parameter){', '    private void appendHydroPoints(', hydro_block)

refresh_block = r'''    private void refreshHydro(boolean force){
        if(hydroLoading)return;
        long now=System.currentTimeMillis();
        boolean needsRefresh=force;
        for(HydroStation station:HydroStation.values()){
            boolean liveFresh=!prefs.getString(station.liveCacheKey(),"").isBlank()&&now-prefs.getLong(station.liveUpdatedKey(),0L)<10*60000L;
            boolean fineFresh=!prefs.getString(station.fineCacheKey(),"").isBlank()&&now-prefs.getLong(station.fineUpdatedKey(),0L)<30*60000L;
            boolean historyFresh=!prefs.getString(station.historyCacheKey(),"").isBlank()&&now-prefs.getLong(station.historyUpdatedKey(),0L)<60*60000L;
            if(!(liveFresh&&fineFresh&&historyFresh))needsRefresh=true;
        }
        if(!needsRefresh)return;
        hydroLoading=true;
        new Thread(()->{
            try{
                String quote=String.valueOf((char)34);
                for(HydroStation station:HydroStation.values()){
                    long check=System.currentTimeMillis();
                    boolean liveFresh=!prefs.getString(station.liveCacheKey(),"").isBlank()&&check-prefs.getLong(station.liveUpdatedKey(),0L)<10*60000L;
                    boolean fineFresh=!prefs.getString(station.fineCacheKey(),"").isBlank()&&check-prefs.getLong(station.fineUpdatedKey(),0L)<30*60000L;
                    boolean historyFresh=!prefs.getString(station.historyCacheKey(),"").isBlank()&&check-prefs.getLong(station.historyUpdatedKey(),0L)<60*60000L;
                    if(force||!liveFresh){
                        String query="{ water { observations { data_live(where:{stationNo:{_eq:"+quote+station.id+quote+"}}) { stationNo parameterName timestamp value releaseStatus } } } }";
                        refreshHydroCache(query,"data_live",station.liveCacheKey(),station.liveUpdatedKey());
                    }
                    if(force||!fineFresh){
                        String from=java.time.Instant.now().minus(java.time.Duration.ofHours(26)).toString();
                        String query="{ water { observations { data_10min_mean(where:{station:{no:{_eq:"+quote+station.id+quote+"}},timestamp:{_gte:"+quote+from+quote+"}}) { parameterName timestamp value } } } }";
                        refreshHydroCache(query,"data_10min_mean",station.fineCacheKey(),station.fineUpdatedKey());
                    }
                    if(force||!historyFresh){
                        String from=java.time.Instant.now().minus(java.time.Duration.ofDays(8)).toString();
                        String query="{ water { observations { data_1hour_mean(where:{station:{no:{_eq:"+quote+station.id+quote+"}},timestamp:{_gte:"+quote+from+quote+"}}) { parameterName timestamp value } } } }";
                        refreshHydroCache(query,"data_1hour_mean",station.historyCacheKey(),station.historyUpdatedKey());
                    }
                }
            }finally{
                hydroLoading=false;
                runOnUiThread(()->{if(current==Screen.HOME)navigate(Screen.HOME);});
            }
        }).start();
    }

'''
replace_between('    private void refreshHydro(boolean force){', '    private void refreshHydroCache(', refresh_block)

settings_replacement = r'''        section(b,"Rhein-Anzeige","Zwei frei konfigurierbare Messkacheln auf Home");
        b.addView(riverSlotSettingCard(1),margin(-1,-2,0,0,0,9));
        b.addView(riverSlotSettingCard(2),margin(-1,-2,0,0,0,12));

        section(b,"Rhein-Grenzwerte","Status basiert auf dem Abfluss der jeweiligen BAFU-Station");
        b.addView(riverThresholdSettingsCard(HydroStation.BASEL_RHEINHALLE),margin(-1,-2,0,0,0,9));
        b.addView(riverThresholdSettingsCard(HydroStation.RHEINFELDEN),margin(-1,-2,0,0,0,12));

'''
replace_between('        section(b,"Rhein-Grenzwerte"', '        section(b,"App",null);', settings_replacement)

settings_helpers = r'''    private View riverSlotSettingCard(int slot){
        HydroStation station=riverStation(slot);
        RiverMetric metric=riverMetricForSlot(slot);
        LinearLayout card=card();
        card.setOrientation(LinearLayout.VERTICAL);
        card.addView(txt("Rhein-Kachel "+slot,15,TEXT,true));
        TextView current=txt(station.label+" · BAFU "+station.id+"\nHauptwert: "+metric.label,13,MUTED,false);
        current.setPadding(0,dp(5),0,dp(10));
        card.addView(current);
        Button configure=btn("Kachel konfigurieren",Color.rgb(232,240,244),NAVY);
        configure.setOnClickListener(v->configureRiverSlot(slot));
        card.addView(configure,new LinearLayout.LayoutParams(-1,dp(44)));
        return card;
    }

    private void configureRiverSlot(int slot){
        final HydroStation[] stations={HydroStation.BASEL_RHEINHALLE,HydroStation.BASEL_RHEINHALLE,HydroStation.RHEINFELDEN,HydroStation.RHEINFELDEN,HydroStation.RHEINFELDEN};
        final RiverMetric[] metrics={RiverMetric.FLOW,RiverMetric.LEVEL,RiverMetric.FLOW,RiverMetric.LEVEL,RiverMetric.TEMPERATURE};
        String[] labels={
                "Basel, Rheinhalle · Abfluss",
                "Basel, Rheinhalle · Pegel",
                "Rheinfelden · Abfluss",
                "Rheinfelden · Pegel",
                "Rheinfelden · Wassertemperatur"
        };
        HydroStation currentStation=riverStation(slot);
        RiverMetric currentMetric=riverMetricForSlot(slot);
        int selected=0;
        for(int i=0;i<stations.length;i++)if(stations[i]==currentStation&&metrics[i]==currentMetric){selected=i;break;}
        new AlertDialog.Builder(this,dialogTheme()).setTitle("Rhein-Kachel "+slot)
                .setSingleChoiceItems(labels,selected,(dialog,which)->{
                    String stationKey=slot==1?PREF_RIVER_SLOT1_STATION:PREF_RIVER_SLOT2_STATION;
                    String metricKey=slot==1?PREF_RIVER_SLOT1_METRIC:PREF_RIVER_SLOT2_METRIC;
                    prefs.edit().putString(stationKey,stations[which].id).putString(metricKey,metrics[which].parameter).apply();
                    dialog.dismiss();
                    navigate(Screen.SETTINGS);
                }).setNegativeButton("Abbrechen",null).show();
    }

    private View riverThresholdSettingsCard(HydroStation station){
        LinearLayout card=card();
        card.setOrientation(LinearLayout.VERTICAL);
        card.addView(txt(station.label+" · BAFU "+station.id,15,TEXT,true));
        card.addView(thresholdGrid(station),margin(-1,-2,0,7,0,9));
        String official=station==HydroStation.BASEL_RHEINHALLE
                ?"BAFU: Gefahrenstufe 2 ab 2550 m³/s, Stufe 4 ab 3700 m³/s."
                :"BAFU: Gefahrenstufe 2 ab 2500 m³/s, Stufe 4 ab 3600 m³/s.";
        TextView note=txt(official+" Niedrig < 400 m³/s ist nur eine anpassbare App-Vorgabe.",11,MUTED,false);
        note.setPadding(0,0,0,dp(9));
        card.addView(note);
        LinearLayout actions=new LinearLayout(this);
        Button edit=btn("Ändern",Color.rgb(232,240,244),NAVY);
        edit.setOnClickListener(v->editRiverThresholds(station));
        actions.addView(edit,new LinearLayout.LayoutParams(0,dp(42),1));
        Button reset=btn("Standard",Color.rgb(232,240,244),NAVY);
        reset.setOnClickListener(v->{prefs.edit().remove(station.lowPreferenceKey()).remove(station.warnPreferenceKey()).remove(station.alarmPreferenceKey()).apply();navigate(Screen.SETTINGS);});
        LinearLayout.LayoutParams resetParams=new LinearLayout.LayoutParams(0,dp(42),1);resetParams.setMargins(dp(8),0,0,0);actions.addView(reset,resetParams);
        card.addView(actions);
        return card;
    }

'''
replace_once('    private View dataFreshnessRow(){\n', settings_helpers + '    private View dataFreshnessRow(){\n')

data_freshness = r'''    private View dataFreshnessRow(){
        LinearLayout stack=new LinearLayout(this);
        stack.setOrientation(LinearLayout.VERTICAL);
        LinearLayout first=new LinearLayout(this);
        addFreshnessTile(first,"Kalender",prefs.getLong(PREF_ICS_UPDATED,0L),4L*60L*60L*1000L);
        addFreshnessTile(first,"Wetter",prefs.getLong(PREF_WEATHER_UPDATED,0L),90L*60L*1000L);
        stack.addView(first,new LinearLayout.LayoutParams(-1,-2));
        LinearLayout second=new LinearLayout(this);
        addFreshnessTile(second,"Basel 2289",prefs.getLong(HydroStation.BASEL_RHEINHALLE.liveUpdatedKey(),0L),45L*60L*1000L);
        addFreshnessTile(second,"Rheinfelden 2091",prefs.getLong(HydroStation.RHEINFELDEN.liveUpdatedKey(),0L),45L*60L*1000L);
        LinearLayout.LayoutParams secondParams=new LinearLayout.LayoutParams(-1,-2);
        secondParams.setMargins(0,dp(6),0,0);
        stack.addView(second,secondParams);
        return stack;
    }

'''
replace_between('    private View dataFreshnessRow(){', '    private void addFreshnessTile(', data_freshness)

old_clear = '    private void clearDataCache(){prefs.edit().remove(PREF_ICS_CACHE).remove(PREF_ICS_UPDATED).remove(PREF_WEATHER_CACHE).remove(PREF_WEATHER_UPDATED).remove(PREF_HYDRO_CACHE).remove(PREF_HYDRO_UPDATED).remove(PREF_HYDRO_FINE_CACHE).remove(PREF_HYDRO_FINE_UPDATED).remove(PREF_HYDRO_HISTORY_CACHE).remove(PREF_HYDRO_HISTORY_UPDATED).apply();events=new ArrayList<>();eventsUpdated=0L;Toast.makeText(this,"Daten-Cache geleert. Neue Daten werden nachgeladen.",Toast.LENGTH_SHORT).show();refreshEvents(false,()->{});refreshLive(true);}\n'
new_clear = r'''    private void clearDataCache(){
        SharedPreferences.Editor editor=prefs.edit().remove(PREF_ICS_CACHE).remove(PREF_ICS_UPDATED).remove(PREF_WEATHER_CACHE).remove(PREF_WEATHER_UPDATED);
        for(HydroStation station:HydroStation.values())editor.remove(station.liveCacheKey()).remove(station.liveUpdatedKey()).remove(station.fineCacheKey()).remove(station.fineUpdatedKey()).remove(station.historyCacheKey()).remove(station.historyUpdatedKey());
        editor.apply();
        events=new ArrayList<>();eventsUpdated=0L;
        Toast.makeText(this,"Daten-Cache geleert. Neue Daten werden nachgeladen.",Toast.LENGTH_SHORT).show();
        refreshEvents(false,()->{});refreshLive(true);
    }
'''
replace_once(old_clear,new_clear)

edit_thresholds = r'''    private void editRiverThresholds(){editRiverThresholds(HydroStation.RHEINFELDEN);}
    private void editRiverThresholds(HydroStation station){
        LinearLayout box=new LinearLayout(this);box.setOrientation(LinearLayout.VERTICAL);box.setPadding(dp(16),dp(4),dp(16),0);
        EditText low=thresholdInput("Niedrig unter m³/s",riverLow(station)),warn=thresholdInput("Warnung ab m³/s",riverWarn(station)),alarm=thresholdInput("Alarm ab m³/s",riverAlarm(station));
        box.addView(txt("Niedrig",12,MUTED,true));box.addView(low,new LinearLayout.LayoutParams(-1,dp(48)));
        TextView w1=txt("Warnung",12,MUTED,true);w1.setPadding(0,dp(10),0,0);box.addView(w1);box.addView(warn,new LinearLayout.LayoutParams(-1,dp(48)));
        TextView a1=txt("Alarm",12,MUTED,true);a1.setPadding(0,dp(10),0,0);box.addView(a1);box.addView(alarm,new LinearLayout.LayoutParams(-1,dp(48)));
        new AlertDialog.Builder(this,dialogTheme()).setTitle(station.label+" · Grenzwerte").setView(box).setPositiveButton("Speichern",(d,w)->{
            try{
                float l=Float.parseFloat(low.getText().toString().replace(',','.')),wa=Float.parseFloat(warn.getText().toString().replace(',','.')),al=Float.parseFloat(alarm.getText().toString().replace(',','.'));
                if(l<0||!(l<wa&&wa<al))throw new Exception();
                prefs.edit().putFloat(station.lowPreferenceKey(),l).putFloat(station.warnPreferenceKey(),wa).putFloat(station.alarmPreferenceKey(),al).apply();
                navigate(Screen.SETTINGS);
            }catch(Exception e){Toast.makeText(this,"Grenzwerte müssen aufsteigend sein: Niedrig < Warnung < Alarm.",Toast.LENGTH_LONG).show();}
        }).setNegativeButton("Abbrechen",null).show();
    }
'''
replace_between('    private void editRiverThresholds(){', '    private EditText thresholdInput(', edit_thresholds)

cash_block = r'''    private View cash() {
        ScrollView scroll=new ScrollView(this);
        LinearLayout body=body();
        scroll.addView(body);

        LinearLayout hero=new LinearLayout(this);
        hero.setOrientation(LinearLayout.VERTICAL);
        hero.setPadding(dp(20),dp(18),dp(20),dp(18));
        GradientDrawable gradient=new GradientDrawable(GradientDrawable.Orientation.TL_BR,new int[]{NAVY,Color.rgb(21,90,122),WATER});
        gradient.setCornerRadius(dp(22));hero.setBackground(gradient);
        body.addView(hero,margin(-1,-2,0,4,0,16));
        hero.addView(txt("VEREINSBEIZ",12,Color.rgb(208,231,239),true));
        TextView title=txt("Konsumation bezahlen",27,Color.WHITE,true);title.setPadding(0,dp(5),0,dp(5));hero.addView(title);
        hero.addView(txt("Artikel für dich, Kinder oder die ganze Runde zusammenstellen – oder weiterhin einen freien Betrag verwenden.",14,Color.rgb(232,243,247),false));

        CashCatalog.Catalog catalog=cashCatalog();
        section(body,"Konsumation zusammenstellen",catalog==null?"Preisliste konnte nicht geladen werden.":"Preise gemäss Vereinsbeiz-Preisliste · Stand "+catalog.validFrom);
        if(catalog!=null){
            for(CashCatalog.Category category:catalog.categories){
                TextView categoryTitle=txt(category.label,15,TEXT,true);
                categoryTitle.setPadding(dp(2),dp(3),0,dp(7));
                body.addView(categoryTitle);
                LinearLayout categoryCard=card();
                categoryCard.setOrientation(LinearLayout.VERTICAL);
                for(int i=0;i<category.items.size();i++){
                    if(i>0){View divider=new View(this);divider.setBackgroundColor(darkMode?Color.rgb(51,65,74):Color.rgb(226,233,237));categoryCard.addView(divider,new LinearLayout.LayoutParams(-1,dp(1)));}
                    categoryCard.addView(cashItemRow(category.items.get(i)));
                }
                body.addView(categoryCard,margin(-1,-2,0,0,0,10));
            }
        }

        section(body,"Warenkorb","Mengen können mehrere Personen gemeinsam abdecken");
        LinearLayout cart=card();cart.setOrientation(LinearLayout.VERTICAL);body.addView(cart,margin(-1,-2,0,0,0,12));
        cashSummaryContainer=new LinearLayout(this);cashSummaryContainer.setOrientation(LinearLayout.VERTICAL);cart.addView(cashSummaryContainer);
        cashTotalView=txt("Total CHF 0.00",24,TEXT,true);cashTotalView.setPadding(0,dp(12),0,dp(10));cart.addView(cashTotalView);
        LinearLayout payRow=new LinearLayout(this);
        Button cartQr=btn("Swiss QR",NAVY,Color.WHITE);cartQr.setOnClickListener(v->{EditText input=cartAmountInput();if(input!=null)showPaymentQr(input);});payRow.addView(cartQr,new LinearLayout.LayoutParams(0,dp(46),1));
        Button cartBank=btn("Direkt Bank",Color.rgb(232,240,244),NAVY);cartBank.setOnClickListener(v->{EditText input=cartAmountInput();if(input!=null)sharePaymentQr(input);});LinearLayout.LayoutParams cbp=new LinearLayout.LayoutParams(0,dp(46),1);cbp.setMargins(dp(7),0,0,0);payRow.addView(cartBank,cbp);
        Button cartTwint=btn("TWINT",Color.rgb(232,240,244),NAVY);cartTwint.setOnClickListener(v->{EditText input=cartAmountInput();if(input!=null)openTwintDirect(input);});LinearLayout.LayoutParams ctp=new LinearLayout.LayoutParams(0,dp(46),1);ctp.setMargins(dp(7),0,0,0);payRow.addView(cartTwint,ctp);
        cart.addView(payRow);
        TextView directInfo=txt("„Direkt Bank“ übergibt den Swiss-QR temporär an eine kompatible Banking-App – ohne ihn vorher dauerhaft zu speichern. Nicht jede Bank unterstützt diesen Android-Import.",11,MUTED,false);directInfo.setPadding(0,dp(8),0,0);cart.addView(directInfo);
        TextView clearCart=link("Warenkorb leeren");clearCart.setOnClickListener(v->{cashCart.clear();navigate(Screen.CASH);});cart.addView(clearCart);
        updateCashSummary();

        section(body,"Freier Betrag","Für Sonderfälle oder Beträge ausserhalb der Preisliste");
        LinearLayout amountCard=card();amountCard.setOrientation(LinearLayout.VERTICAL);body.addView(amountCard,margin(-1,-2,0,0,0,12));
        LinearLayout amountRow=new LinearLayout(this);amountRow.setGravity(Gravity.CENTER_VERTICAL);amountRow.setPadding(0,0,0,dp(8));amountCard.addView(amountRow);
        TextView chf=txt("CHF",20,NAVY,true);chf.setGravity(Gravity.CENTER_VERTICAL);amountRow.addView(chf,new LinearLayout.LayoutParams(dp(55),dp(56)));
        EditText amount=new EditText(this);amount.setHint("0.00");amount.setTextSize(25);amount.setSingleLine(true);amount.setInputType(InputType.TYPE_CLASS_NUMBER|InputType.TYPE_NUMBER_FLAG_DECIMAL);amount.setTextColor(themeText(TEXT));amount.setHintTextColor(themeText(MUTED));amount.setBackground(round(Color.rgb(238,243,246),14));amount.setPadding(dp(14),0,dp(14),0);amountRow.addView(amount,new LinearLayout.LayoutParams(0,dp(56),1));
        TextView amountInfo=txt("Leer oder 0 erzeugt einen Swiss QR mit offenem Betrag.",12,MUTED,false);amountInfo.setPadding(0,0,0,dp(10));amountCard.addView(amountInfo);
        Button qr=btn("Swiss QR erstellen",NAVY,Color.WHITE);qr.setOnClickListener(v->showPaymentQr(amount));amountCard.addView(qr,new LinearLayout.LayoutParams(-1,dp(48)));
        Button direct=btn("QR direkt an Banking-App",Color.rgb(232,240,244),NAVY);direct.setOnClickListener(v->sharePaymentQr(amount));LinearLayout.LayoutParams dlp=new LinearLayout.LayoutParams(-1,dp(44));dlp.setMargins(0,dp(8),0,0);amountCard.addView(direct,dlp);
        String bankLabel=prefs.getString(PREF_BANK_LABEL,"");
        Button bank=btn(bankLabel.trim().isEmpty()?"Banking-App auswählen":bankLabel+" öffnen",Color.rgb(232,240,244),NAVY);bankButton=bank;bank.setOnClickListener(v->openPreferred(false,amount));LinearLayout.LayoutParams bp=new LinearLayout.LayoutParams(-1,dp(44));bp.setMargins(0,dp(8),0,0);amountCard.addView(bank,bp);
        TextView change=link(bankLabel.trim().isEmpty()?"Banking-App für Direktstart wählen":"Andere Banking-App wählen");change.setOnClickListener(v->chooseApp(false,amount));amountCard.addView(change);

        LinearLayout twint=card();twint.setOrientation(LinearLayout.VERTICAL);body.addView(twint,margin(-1,-2,0,0,0,12));
        twint.addView(txt("TWINT",16,TEXT,true));
        TextView twintInfo=txt("Für Zahlung auf demselben Handy: Betrag auf der PFVR-Seite übernehmen und dort den fünfstelligen TWINT-Code erzeugen. Der Vereins-QR bleibt zusätzlich verfügbar.",13,MUTED,false);twintInfo.setPadding(0,dp(4),0,dp(10));twint.addView(twintInfo);
        Button twDirect=btn("TWINT-Code erzeugen",NAVY,Color.WHITE);twDirect.setOnClickListener(v->openTwintDirect(amount));twint.addView(twDirect,new LinearLayout.LayoutParams(-1,dp(48)));
        Button twQr=btn("Vereins-TWINT-QR öffnen",Color.rgb(232,240,244),NAVY);twQr.setOnClickListener(v->external(TWINT_QR_PDF));LinearLayout.LayoutParams twqp=new LinearLayout.LayoutParams(-1,dp(44));twqp.setMargins(0,dp(8),0,0);twint.addView(twQr,twqp);

        section(body,"Zahlungsdaten","Für E-Banking und manuelle Überweisung");
        LinearLayout details=card();details.setOrientation(LinearLayout.VERTICAL);body.addView(details,margin(-1,-2,0,0,0,12));details.addView(txt(CLUB_PAYEE,16,TEXT,true));details.addView(txt("Rheinweg · 4310 Rheinfelden",13,MUTED,false));TextView iban=txt(CLUB_IBAN,19,NAVY,true);iban.setPadding(0,dp(12),0,dp(4));details.addView(iban);details.addView(txt(CLUB_PAYMENT_NOTE,13,MUTED,false));
        LinearLayout copies=new LinearLayout(this);copies.setPadding(0,dp(12),0,0);details.addView(copies);Button copyIban=btn("IBAN kopieren",Color.rgb(232,240,244),NAVY);copyIban.setOnClickListener(v->copy("PFVR IBAN",CLUB_IBAN.replace(" ",""),"IBAN kopiert"));copies.addView(copyIban,new LinearLayout.LayoutParams(0,dp(42),1));Button copyAll=btn("Alles kopieren",Color.rgb(232,240,244),NAVY);copyAll.setOnClickListener(v->{String x=CLUB_PAYEE+"\n"+CLUB_IBAN+"\n"+CLUB_PAYMENT_NOTE;String a=amount(amount.getText().toString());if(a!=null&&!a.isBlank())x+="\nCHF "+a;copy("PFVR Zahlung",x,"Zahlungsdaten kopiert");});LinearLayout.LayoutParams cap=new LinearLayout.LayoutParams(0,dp(42),1);cap.setMargins(dp(8),0,0,0);copies.addView(copyAll,cap);
        return scroll;
    }

    private CashCatalog.Catalog cashCatalog(){
        if(cashCatalog!=null)return cashCatalog;
        try{cashCatalog=CashCatalog.load(this);}catch(Exception ignored){cashCatalog=null;}
        return cashCatalog;
    }

    private View cashItemRow(CashCatalog.Item item){
        LinearLayout row=new LinearLayout(this);row.setGravity(Gravity.CENTER_VERTICAL);row.setPadding(0,dp(9),0,dp(9));
        LinearLayout copy=new LinearLayout(this);copy.setOrientation(LinearLayout.VERTICAL);copy.setPadding(0,0,dp(8),0);row.addView(copy,new LinearLayout.LayoutParams(0,-2,1));
        copy.addView(txt(item.name,14,TEXT,true));
        String detail=(item.variant==null||item.variant.isBlank()?"":item.variant+" · ")+formatCashPrice(item.price)+(item.deposit?" · Depot":"");copy.addView(txt(detail,12,item.deposit?WATER:MUTED,false));
        Button minus=btn("−",Color.rgb(232,240,244),NAVY);row.addView(minus,new LinearLayout.LayoutParams(dp(40),dp(40)));
        TextView quantity=txt(String.valueOf(cashCart.getOrDefault(item.id,0)),16,TEXT,true);quantity.setGravity(Gravity.CENTER);row.addView(quantity,new LinearLayout.LayoutParams(dp(38),dp(40)));
        Button plus=btn("+",NAVY,Color.WHITE);row.addView(plus,new LinearLayout.LayoutParams(dp(40),dp(40)));
        minus.setOnClickListener(v->{int next=Math.max(0,cashCart.getOrDefault(item.id,0)-1);if(next==0)cashCart.remove(item.id);else cashCart.put(item.id,next);quantity.setText(String.valueOf(next));updateCashSummary();});
        plus.setOnClickListener(v->{int next=Math.min(99,cashCart.getOrDefault(item.id,0)+1);cashCart.put(item.id,next);quantity.setText(String.valueOf(next));updateCashSummary();});
        return row;
    }

    private void updateCashSummary(){
        if(cashSummaryContainer==null||cashTotalView==null)return;
        cashSummaryContainer.removeAllViews();
        CashCatalog.Catalog catalog=cashCatalog();
        if(catalog==null){cashSummaryContainer.addView(txt("Preisliste nicht verfügbar.",13,MUTED,false));cashTotalView.setText("Total CHF 0.00");return;}
        boolean any=false;
        for(CashCatalog.Category category:catalog.categories){
            for(CashCatalog.Item item:category.items){
                int quantity=cashCart.getOrDefault(item.id,0);if(quantity<=0)continue;any=true;
                TextView line=txt(quantity+"× "+item.displayName()+" · "+formatCashPrice(item.price*quantity),13,TEXT,false);line.setPadding(0,dp(2),0,dp(2));cashSummaryContainer.addView(line);
            }
        }
        if(!any)cashSummaryContainer.addView(txt("Noch keine Artikel ausgewählt.",13,MUTED,false));
        cashTotalView.setText("Total "+formatCashPrice(catalog.total(cashCart)));
    }

    private String formatCashPrice(double value){return "CHF "+String.format(Locale.GERMAN,"%.2f",value);}

    private EditText cartAmountInput(){
        CashCatalog.Catalog catalog=cashCatalog();
        if(catalog==null||catalog.itemCount(cashCart)<=0){Toast.makeText(this,"Der Warenkorb ist leer.",Toast.LENGTH_SHORT).show();return null;}
        EditText input=new EditText(this);input.setText(String.format(Locale.US,"%.2f",catalog.total(cashCart)));return input;
    }

'''
replace_between('    private View cash() {', '    private void openTwintDirect(', cash_block)

share_method = r'''    private void sharePaymentQr(EditText amountInput){
        String value=amount(amountInput==null?null:amountInput.getText().toString());
        if(value==null){Toast.makeText(this,"Bitte einen gültigen CHF-Betrag eingeben.",Toast.LENGTH_LONG).show();return;}
        try{
            Bitmap qr=makeSwissQr(value);
            pendingQrBitmap=qr;
            File directory=new File(getCacheDir(),"shared");
            if(!directory.exists()&&!directory.mkdirs())throw new Exception("cache directory");
            File file=new File(directory,value.isBlank()?"PFVR-Zahlung-offen.png":"PFVR-Zahlung-CHF-"+value.replace('.','_')+".png");
            try(FileOutputStream out=new FileOutputStream(file)){if(!qr.compress(Bitmap.CompressFormat.PNG,100,out))throw new Exception("png");}
            Uri uri=FileProvider.getUriForFile(this,getPackageName()+".fileprovider",file);
            Intent send=new Intent(Intent.ACTION_SEND);
            send.setType("image/png");
            send.putExtra(Intent.EXTRA_STREAM,uri);
            send.setClipData(ClipData.newUri(getContentResolver(),"PFVR Swiss QR",uri));
            send.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
            String preferred=prefs.getString(PREF_BANK_PACKAGE,"");
            if(!preferred.isBlank()){
                send.setPackage(preferred);
                if(send.resolveActivity(getPackageManager())!=null){startActivity(send);Toast.makeText(this,"Swiss QR temporär an Banking-App übergeben.",Toast.LENGTH_SHORT).show();return;}
                send.setPackage(null);
                Toast.makeText(this,"Die gewählte Banking-App nimmt keinen direkten Bild-Import an. Kompatible Apps werden angezeigt.",Toast.LENGTH_LONG).show();
            }
            if(getPackageManager().queryIntentActivities(send,0).isEmpty()){
                Toast.makeText(this,"Keine App unterstützt die direkte QR-Übergabe. QR wird stattdessen angezeigt.",Toast.LENGTH_LONG).show();
                showPaymentQr(amountInput);
                return;
            }
            startActivity(Intent.createChooser(send,"Swiss QR an Banking-App übergeben"));
        }catch(Exception e){Toast.makeText(this,"Direkte QR-Übergabe nicht möglich. QR wird stattdessen angezeigt.",Toast.LENGTH_LONG).show();showPaymentQr(amountInput);}
    }

'''
replace_once('    private void showPaymentQr(EditText amountInput) {\n', share_method + '    private void showPaymentQr(EditText amountInput) {\n')
replace_once(
    '            TextView note=txt("QR speichern und in der Banking-App aus Datei/Foto importieren, sofern die Bank das unterstützt. Yuh kann QR-Rechnungen als Dokument/Bild einlesen.",12,MUTED,false); note.setGravity(Gravity.CENTER); box.addView(note);\n',
    '            TextView note=txt("Direkte Übergabe versucht den QR als temporäres Bild an eine kompatible Banking-App zu senden. Falls die Bank das nicht unterstützt, bleibt Speichern/Öffnen als Fallback.",12,MUTED,false); note.setGravity(Gravity.CENTER); box.addView(note);\n'
)
replace_once(
    '                    .setPositiveButton("Banking-App öffnen",(d,w)->openPreferred(false,amountInput))\n',
    '                    .setPositiveButton("Direkt an Banking-App",(d,w)->sharePaymentQr(amountInput))\n'
)

replace_once(
    '        private final RiverMetric metric;\n        private final RiverRange range;\n',
    '        private final RiverMetric metric;\n        private final RiverRange range;\n        private final HydroStation station;\n'
)
replace_once(
    '        RiverTrendView(Context context,TrendSeries series,RiverMetric metric,RiverRange range){\n            super(context);\n            this.series=series;this.metric=metric;this.range=range;\n',
    '        RiverTrendView(Context context,TrendSeries series,RiverMetric metric,RiverRange range,HydroStation station){\n            super(context);\n            this.series=series;this.metric=metric;this.range=range;this.station=station;\n'
)
replace_once(
    '            double[] values={riverLow(),riverWarn(),riverAlarm()};\n',
    '            double[] values={riverLow(station),riverWarn(station),riverAlarm(station)};\n'
)

p.write_text(s,encoding='utf-8')
print('Applied Android 0.9.0 MainActivity changes')
