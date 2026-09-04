from pathlib import Path

path = Path('Android/app/src/main/java/ch/pfvr/internapp/MainActivity.java')
text = path.read_text(encoding='utf-8')
start_marker = '    private LinearLayout riverSummaryCard(int slot){'
end_marker = '    private View riverStatusCompact(RiverStatus status){'
start = text.index(start_marker)
end = text.index(end_marker, start)
replacement = '''    private LinearLayout riverSummaryCard(int slot){
        HydroStation station=riverStation(slot);
        double flow=currentHydroValue(station,"Q");
        double level=displayHydroValue(station,RiverMetric.LEVEL,currentHydroValue(station,"W"));
        double temperature=station.supportsTemperature?currentHydroValue(station,"WT"):Double.NaN;
        RiverStatus status=riverStatus(station,flow);
        int statusColor=statusTextColor(status.bg);

        LinearLayout c=card();
        c.setOrientation(LinearLayout.VERTICAL);
        c.setPadding(dp(12),dp(11),dp(12),dp(11));
        c.setOnClickListener(v->external(station.stationUrl()));

        TextView stationName=txt(station.label,11,WATER,true);
        stationName.setMaxLines(1);
        c.addView(stationName);

        LinearLayout flowRow=new LinearLayout(this);
        flowRow.setGravity(Gravity.CENTER_VERTICAL);
        flowRow.setPadding(0,dp(6),0,0);
        String flowText=Double.isFinite(flow)?formatMetric(station,RiverMetric.FLOW,flow):"–";
        flowRow.addView(riverSummaryMetric("Abfluss",flowText,"m³/s",statusColor),new LinearLayout.LayoutParams(0,-2,1));
        LinearLayout.LayoutParams statusParams=new LinearLayout.LayoutParams(-2,-2);
        statusParams.setMargins(dp(6),dp(8),0,0);
        flowRow.addView(riverStatusCompact(status),statusParams);
        c.addView(flowRow);

        String levelText=Double.isFinite(level)?formatMetric(station,RiverMetric.LEVEL,level):"–";
        LinearLayout levelMetric=riverSummaryMetric("Pegel",levelText,metricUnit(station,RiverMetric.LEVEL),themeText(WATER));
        levelMetric.setPadding(0,dp(5),0,0);
        c.addView(levelMetric,new LinearLayout.LayoutParams(-1,-2));

        if(Double.isFinite(temperature)){
            TextView temperatureView=txt("Wasser "+formatMetric(station,RiverMetric.TEMPERATURE,temperature)+" °C",11,MUTED,false);
            temperatureView.setPadding(0,dp(5),0,0);
            c.addView(temperatureView);
        }

        View timestampSpacer=new View(this);
        c.addView(timestampSpacer,new LinearLayout.LayoutParams(1,0,1));
        String[] summary=hydroSummary(station);
        TextView stand=txt(summary[3],10,Color.rgb(126,140,150),false);
        stand.setMaxLines(1);
        stand.setPadding(0,dp(7),0,0);
        c.addView(stand);
        return c;
    }

    private LinearLayout riverSummaryMetric(String label,String value,String unit,int color){
        LinearLayout metric=new LinearLayout(this);
        metric.setOrientation(LinearLayout.VERTICAL);
        TextView labelView=txt(label,9,MUTED,true);
        metric.addView(labelView);
        LinearLayout valueLine=new LinearLayout(this);
        valueLine.setGravity(Gravity.CENTER_VERTICAL);
        TextView valueView=txt(value,20,color,true);
        valueView.setTextColor(color);
        valueLine.addView(valueView);
        TextView unitView=txt(unit,10,color,true);
        unitView.setTextColor(color);
        unitView.setPadding(dp(4),dp(2),0,0);
        valueLine.addView(unitView);
        metric.addView(valueLine);
        return metric;
    }

'''
path.write_text(text[:start] + replacement + text[end:], encoding='utf-8')

gradle = Path('Android/app/build.gradle')
build = gradle.read_text(encoding='utf-8')
if "versionCode 39" not in build or "versionName '0.10.15'" not in build:
    raise SystemExit('Unexpected version baseline')
build = build.replace('versionCode 39', 'versionCode 40', 1)
build = build.replace("versionName '0.10.15'", "versionName '0.10.16'", 1)
gradle.write_text(build, encoding='utf-8')
