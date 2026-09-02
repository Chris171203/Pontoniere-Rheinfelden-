from pathlib import Path

p = Path('Android/app/src/main/java/ch/pfvr/internapp/MainActivity.java')
s = p.read_text(encoding='utf-8')

if 'private double baselLevelCm(double metres)' in s:
    print('Android 0.9.0 Basel refinements already present')
    raise SystemExit(0)


def rep(old, new, count=1):
    global s
    if old not in s:
        raise SystemExit('marker not found: ' + old[:120])
    s = s.replace(old, new, count)

rep(
    '        double primary=currentHydroValue(station,metric.parameter);\n',
    '        double primary=displayHydroValue(station,metric,currentHydroValue(station,metric.parameter));\n'
)
rep(
    '        TextView value=txt(Double.isFinite(primary)?formatMetric(metric,primary):"–",34,TEXT,true);\n',
    '        TextView value=txt(Double.isFinite(primary)?formatMetric(station,metric,primary):"–",34,TEXT,true);\n'
)
rep(
    '        TextView unit=txt(metric.unit,14,MUTED,true);\n',
    '        TextView unit=txt(metricUnit(station,metric),14,MUTED,true);\n'
)
rep(
    '            card.addView(riverMetrics(stats,range,metric),margin(-1,-2,0,2,0,10));\n',
    '            card.addView(riverMetrics(stats,range,metric,station),margin(-1,-2,0,2,0,10));\n'
)
rep(
    '        chartTitle.addView(txt(metric.unit+" · "+range.label,10,MUTED,false),new LinearLayout.LayoutParams(-2,-2));\n',
    '        chartTitle.addView(txt(metricUnit(station,metric)+" · "+range.label,10,MUTED,false),new LinearLayout.LayoutParams(-2,-2));\n'
)

rep(
    '        if(primary!=RiverMetric.FLOW)appendSecondary(out,"Abfluss",currentHydroValue(station,"Q"),RiverMetric.FLOW);\n'
    '        if(primary!=RiverMetric.LEVEL)appendSecondary(out,"Pegel",currentHydroValue(station,"W"),RiverMetric.LEVEL);\n'
    '        if(station.supportsTemperature&&primary!=RiverMetric.TEMPERATURE)appendSecondary(out,"Wasser",currentHydroValue(station,"WT"),RiverMetric.TEMPERATURE);\n',
    '        if(primary!=RiverMetric.FLOW)appendSecondary(out,station,"Abfluss",currentHydroValue(station,"Q"),RiverMetric.FLOW);\n'
    '        if(primary!=RiverMetric.LEVEL)appendSecondary(out,station,"Pegel",currentHydroValue(station,"W"),RiverMetric.LEVEL);\n'
    '        if(station.supportsTemperature&&primary!=RiverMetric.TEMPERATURE)appendSecondary(out,station,"Wasser",currentHydroValue(station,"WT"),RiverMetric.TEMPERATURE);\n'
)
rep(
    '    private void appendSecondary(StringBuilder out,String label,double value,RiverMetric metric){\n'
    '        if(!Double.isFinite(value))return;\n'
    '        if(out.length()>0)out.append("   ·   ");\n'
    "        out.append(label).append(' ').append(formatMetric(metric,value)).append(' ').append(metric.unit);\n"
    '    }\n',
    '    private void appendSecondary(StringBuilder out,HydroStation station,String label,double rawValue,RiverMetric metric){\n'
    '        if(!Double.isFinite(rawValue))return;\n'
    '        double value=displayHydroValue(station,metric,rawValue);\n'
    '        if(out.length()>0)out.append("   ·   ");\n'
    "        out.append(label).append(' ').append(formatMetric(station,metric,value)).append(' ').append(metricUnit(station,metric));\n"
    '        if(station==HydroStation.BASEL_RHEINHALLE&&metric==RiverMetric.LEVEL)out.append(" rel.");\n'
    '    }\n'
)

old_metrics = '''    private View riverMetrics(HydroMath.Stats stats,RiverRange range,RiverMetric metric){
        LinearLayout row=new LinearLayout(this);
        row.setGravity(Gravity.CENTER);
        addMetric(row,formatMetric(metric,stats.mean),"Ø "+range.label);
        addMetric(row,formatMetric(metric,stats.min)+"–"+formatMetric(metric,stats.max),"Min–Max");
        addMetric(row,trendText(stats,metric),"Start → Ende");
        return row;
    }
'''
new_metrics = '''    private View riverMetrics(HydroMath.Stats stats,RiverRange range,RiverMetric metric,HydroStation station){
        LinearLayout row=new LinearLayout(this);
        row.setGravity(Gravity.CENTER);
        addMetric(row,formatMetric(station,metric,stats.mean),"Ø "+range.label);
        addMetric(row,formatMetric(station,metric,stats.min)+"–"+formatMetric(station,metric,stats.max),"Min–Max");
        addMetric(row,trendText(stats,metric,station),"Start → Ende");
        return row;
    }
'''
rep(old_metrics,new_metrics)

old_trend = '''    private String trendText(HydroMath.Stats stats,RiverMetric metric){
        double change=stats.change();
        double tolerance;
        if(metric==RiverMetric.FLOW)tolerance=Math.max(1d,Math.abs(stats.last)*0.0025d);
        else if(metric==RiverMetric.LEVEL)tolerance=0.01d;
        else tolerance=0.1d;
        if(Math.abs(change)<=tolerance)return "→ stabil";
        return (change>0?"↗ +":"↘ −")+formatMetric(metric,Math.abs(change));
    }

    private String formatMetric(RiverMetric metric,double value){
        if(!Double.isFinite(value))return "–";
        if(metric.decimals==0)return String.format(Locale.GERMAN,"%.0f",value);
        if(metric.decimals==1)return String.format(Locale.GERMAN,"%.1f",value);
        return String.format(Locale.GERMAN,"%.2f",value);
    }
'''
new_trend = '''    private String trendText(HydroMath.Stats stats,RiverMetric metric,HydroStation station){
        double change=stats.change();
        double tolerance;
        if(metric==RiverMetric.FLOW)tolerance=Math.max(1d,Math.abs(stats.last)*0.0025d);
        else if(metric==RiverMetric.LEVEL)tolerance=station==HydroStation.BASEL_RHEINHALLE?1d:0.01d;
        else tolerance=0.1d;
        if(Math.abs(change)<=tolerance)return "→ stabil";
        return (change>0?"↗ +":"↘ −")+formatMetric(station,metric,Math.abs(change));
    }

    private String formatMetric(RiverMetric metric,double value){
        if(!Double.isFinite(value))return "–";
        if(metric.decimals==0)return String.format(Locale.GERMAN,"%.0f",value);
        if(metric.decimals==1)return String.format(Locale.GERMAN,"%.1f",value);
        return String.format(Locale.GERMAN,"%.2f",value);
    }

    private String formatMetric(HydroStation station,RiverMetric metric,double value){
        if(!Double.isFinite(value))return "–";
        if(station==HydroStation.BASEL_RHEINHALLE&&metric==RiverMetric.LEVEL)return String.format(Locale.GERMAN,"%.0f",value);
        return formatMetric(metric,value);
    }

    private String metricUnit(HydroStation station,RiverMetric metric){
        return station==HydroStation.BASEL_RHEINHALLE&&metric==RiverMetric.LEVEL?"cm":metric.unit;
    }

    private double displayHydroValue(HydroStation station,RiverMetric metric,double rawValue){
        if(!Double.isFinite(rawValue))return rawValue;
        return station==HydroStation.BASEL_RHEINHALLE&&metric==RiverMetric.LEVEL?baselLevelCm(rawValue):rawValue;
    }

    private double baselLevelCm(double metres){return (metres-240.0d)*100.0d;}
'''
rep(old_trend,new_trend)

# Convert Basel water-level trend from metres a.s.l. to the operational relative centimetre scale.
old_return = '''        for(Map.Entry<Long,Double> entry:points.entrySet()){
            if(entry.getKey()<cutoff)continue;
            result.times.add(entry.getKey());
            result.values.add(entry.getValue());
        }
        return result;
    }
'''
new_return = '''        for(Map.Entry<Long,Double> entry:points.entrySet()){
            if(entry.getKey()<cutoff)continue;
            result.times.add(entry.getKey());
            result.values.add(entry.getValue());
        }
        if(station==HydroStation.BASEL_RHEINHALLE&&"W".equals(parameter)){
            for(int index=0;index<result.values.size();index++)result.values.set(index,baselLevelCm(result.values.get(index)));
        }
        return result;
    }
'''
rep(old_return,new_return)

rep(
    '                "Basel, Rheinhalle · Pegel",\n',
    '                "Basel, Rheinhalle · Pegel (cm)",\n'
)

old_note = '''        String official=station==HydroStation.BASEL_RHEINHALLE
                ?"BAFU: Gefahrenstufe 2 ab 2550 m³/s, Stufe 4 ab 3700 m³/s."
                :"BAFU: Gefahrenstufe 2 ab 2500 m³/s, Stufe 4 ab 3600 m³/s.";
        TextView note=txt(official+" Niedrig < 400 m³/s ist nur eine anpassbare App-Vorgabe.",11,MUTED,false);
'''
new_note = '''        String official=station==HydroStation.BASEL_RHEINHALLE
                ?"Schifffahrt: ca. 1800 m³/s = 700 cm / Voralarm; ca. 2500 m³/s = 790 cm / Sperrung Kleinschifffahrt und Fähren Basel–Rheinfelden."
                :"BAFU: Gefahrenstufe 2 ab 2500 m³/s, Stufe 4 ab 3600 m³/s.";
        TextView note=txt(official+" Niedrig < 400 m³/s ist nur eine anpassbare App-Vorgabe.",11,MUTED,false);
'''
rep(old_note,new_note)

rep(
    '            String text=timestamp.format(formatter)+" · "+formatMetric(metric,series.values.get(selectedIndex))+" "+metric.unit;\n',
    '            String text=timestamp.format(formatter)+" · "+formatMetric(station,metric,series.values.get(selectedIndex))+" "+metricUnit(station,metric);\n'
)

p.write_text(s,encoding='utf-8')
print('Applied Android 0.9.0 Basel display refinements')
