from pathlib import Path
import sys


def replace_once(text, old, new, label):
    if old not in text:
        raise SystemExit(f"{label} not found")
    return text.replace(old, new, 1)


def plan():
    status = Path("STATUS.md")
    text = status.read_text(encoding="utf-8")
    text = text.replace("Stand: Testversion `0.10.19`", "Stand: Testversion `0.10.20`", 1)
    anchor = "- `Rhein aktuell` zeigt Basel-Rheinhalle und Rheinfelden primär als vom BAFU gelieferten Wasserstand in `m ü.M.` mit zwei Nachkommastellen. Nur Basel-Rheinhalle zeigt zusätzlich klein den relativen Pegel in `cm`, weil dafür ein belastbarer aktueller Bezug vorliegt. Für Rheinfelden wird ohne bestätigten aktuellen Pegelnullpunkt kein cm-Wert abgeleitet.\n"
    addition = (
        anchor
        + "- Schifffahrtswarnung und -sperrung werden ausschließlich aus dem offiziellen Pegel Basel-Rheinhalle abgeleitet: 700 cm = Hochwassermarke I / Voralarm, 790 cm = Hochwassermarke IIb / Sperrung der Kleinschifffahrt und Fähren Basel–Rheinfelden, 820 cm = Hochwassermarke IIa / Sperrung der Schifffahrt Rheinfelden–Kembs. Der Abfluss bleibt ein eigener hydrologischer Zusatzwert und steuert diese Lage nicht mehr.\n"
        + "- Der Pegel ist in `Rhein aktuell` und im Diagrammkopf wieder die visuell priorisierte Größe. Basel-Pegelwert und -kurve folgen der offiziellen Hochwasserstufe; die Abflusskurve nutzt je Stufe eine unterscheidbare Schwesterfarbe. Rheinfelden erhält ohne eigenen offiziellen Sperrbezug keine erfundene Stufenfarbe.\n"
        + "- Im Basel-Graphen werden die offiziellen Hochwassermarken auf der Pegelachse eingezeichnet. Historische Pegel- und Abflussabschnitte werden nach der zum jeweiligen Zeitpunkt geltenden Basel-Pegelstufe eingefärbt, bleiben aber durch unterschiedliche Farbtöne klar unterscheidbar.\n"
    )
    text = replace_once(text, anchor, addition, "STATUS Rhein anchor")
    status.write_text(text, encoding="utf-8")


def implement():
    main_path = Path("Android/app/src/main/java/ch/pfvr/internapp/MainActivity.java")
    text = main_path.read_text(encoding="utf-8")

    text = replace_once(
        text,
        '    private static final String TWINT_DIRECT_URL = "https://www.pfvr.ch/vereinsbeiz-zahlung/";\n',
        '    private static final String TWINT_DIRECT_URL = "https://www.pfvr.ch/vereinsbeiz-zahlung/";\n    private static final String RIVER_NAVIGATION_SOURCE = "https://port-of-switzerland.ch/hafenservice/pegel/";\n',
        "navigation source constant",
    )

    old_settings = '''        section(body,"Rhein-Grenzwerte","Status basiert auf dem Abfluss der jeweiligen BAFU-Station");
        body.addView(riverThresholdSettingsCard(HydroStation.BASEL_RHEINHALLE),margin(-1,-2,0,0,0,9));
        body.addView(riverThresholdSettingsCard(HydroStation.RHEINFELDEN),margin(-1,-2,0,0,0,12));
'''
    new_settings = '''        section(body,"Schifffahrtslage","Offizielle Hochwassermarken des Pegels Basel-Rheinhalle; der Abfluss bleibt ein separater Messwert.");
        body.addView(riverNavigationSettingsCard(),margin(-1,-2,0,0,0,12));
'''
    text = replace_once(text, old_settings, new_settings, "river settings section")

    settings_anchor = "    private View riverSlotSettingCard(int slot){\n"
    navigation_settings = '''    private View riverNavigationSettingsCard(){
        LinearLayout card=card();
        card.setOrientation(LinearLayout.VERTICAL);
        card.addView(txt("Pegel Basel-Rheinhalle · offizielle Schifffahrtslage",15,TEXT,true));
        TextView intro=txt("Ausschlaggebend für die Hochwassermarken ist der Pegel Basel-Rheinhalle. Abflusswerte werden nur ergänzend angezeigt.",12,MUTED,false);
        intro.setPadding(0,dp(5),0,dp(8));
        card.addView(intro);
        card.addView(navigationRuleRow("700 cm","HWM I · Voralarm","Hochwassermarke I",navigationLevelColor(RhineNavigation.Stage.HWM_I)));
        card.addView(navigationRuleRow("790 cm","HWM IIb · Sperre","Kleinschifffahrt und Fähren Basel–Rheinfelden gesperrt",navigationLevelColor(RhineNavigation.Stage.HWM_IIB)));
        card.addView(navigationRuleRow("820 cm","HWM IIa · Sperre","Schifffahrt Rheinfelden–Kembs gesperrt",navigationLevelColor(RhineNavigation.Stage.HWM_IIA)));
        TextView source=txt("Quelle: Schweizerische Rheinhäfen  ↗",10,WATER,true);
        source.setPadding(0,dp(9),0,0);
        source.setOnClickListener(v->external(RIVER_NAVIGATION_SOURCE));
        card.addView(source);
        return card;
    }

    private View navigationRuleRow(String level,String labelText,String detail,int color){
        LinearLayout row=new LinearLayout(this);
        row.setGravity(Gravity.CENTER_VERTICAL);
        row.setPadding(0,dp(5),0,dp(5));
        View dot=new View(this);
        dot.setBackground(statusDot(color));
        row.addView(dot,new LinearLayout.LayoutParams(dp(9),dp(9)));
        LinearLayout copy=new LinearLayout(this);
        copy.setOrientation(LinearLayout.VERTICAL);
        copy.setPadding(dp(8),0,0,0);
        TextView title=txt(level+" · "+labelText,12,color,true);
        title.setTextColor(color);
        copy.addView(title);
        copy.addView(txt(detail,10,MUTED,false));
        row.addView(copy,new LinearLayout.LayoutParams(0,-2,1));
        return row;
    }

'''
    if settings_anchor not in text:
        raise SystemExit("river slot settings anchor not found")
    text = text.replace(settings_anchor, navigation_settings + settings_anchor, 1)

    summary_start = text.index("    private LinearLayout riverSummaryCard(int slot){")
    summary_end = text.index("    private View riverStatusCompact", summary_start)
    summary_replacement = '''    private LinearLayout riverSummaryCard(int slot){
        HydroStation station=riverStation(slot);
        double flow=currentHydroValue(station,"Q");
        double rawLevel=currentHydroValue(station,"W");
        double level=displayHydroValue(station,RiverMetric.LEVEL,rawLevel);
        double gaugeCm=RiverDisplay.hasVerifiedGaugeCentimetres(station)?RiverDisplay.gaugeCentimetres(station,rawLevel):Double.NaN;
        double temperature=station.supportsTemperature?currentHydroValue(station,"WT"):Double.NaN;
        RhineNavigation.Stage stage=station==HydroStation.BASEL_RHEINHALLE?navigationStage():RhineNavigation.Stage.NORMAL;
        int levelColor=navigationLevelColor(stage);
        int flowColor=navigationFlowColor(stage);

        LinearLayout c=card();
        c.setOrientation(LinearLayout.VERTICAL);
        c.setPadding(dp(12),dp(11),dp(12),dp(11));
        c.setOnClickListener(v->external(station.stationUrl()));

        TextView stationName=txt(station.label,11,WATER,true);
        stationName.setMaxLines(1);
        c.addView(stationName);

        LinearLayout levelRow=new LinearLayout(this);
        levelRow.setGravity(Gravity.CENTER_VERTICAL);
        levelRow.setPadding(0,dp(6),0,0);
        String levelText=Double.isFinite(level)?formatMetric(station,RiverMetric.LEVEL,level):"–";
        levelRow.addView(riverSummaryMetric("Pegel",levelText,metricUnit(station,RiverMetric.LEVEL),levelColor,25),new LinearLayout.LayoutParams(0,-2,1));
        if(station==HydroStation.BASEL_RHEINHALLE){
            LinearLayout.LayoutParams badgeParams=new LinearLayout.LayoutParams(-2,-2);
            badgeParams.setMargins(dp(6),dp(8),0,0);
            levelRow.addView(navigationBadge(stage),badgeParams);
        }
        c.addView(levelRow);

        if(Double.isFinite(gaugeCm)){
            TextView gaugeView=txt(String.format(Locale.GERMAN,"%.0f cm",gaugeCm),10,levelColor,true);
            gaugeView.setTextColor(levelColor);
            gaugeView.setPadding(0,dp(1),0,0);
            c.addView(gaugeView);
        }

        String flowText=Double.isFinite(flow)?formatMetric(station,RiverMetric.FLOW,flow):"–";
        LinearLayout flowMetric=riverSummaryMetric("Abfluss",flowText,"m³/s",flowColor,17);
        flowMetric.setPadding(0,dp(6),0,0);
        c.addView(flowMetric,new LinearLayout.LayoutParams(-1,-2));

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

    private LinearLayout riverSummaryMetric(String label,String value,String unit,int color,float valueSize){
        LinearLayout metric=new LinearLayout(this);
        metric.setOrientation(LinearLayout.VERTICAL);
        TextView labelView=txt(label,9,MUTED,true);
        metric.addView(labelView);
        LinearLayout valueLine=new LinearLayout(this);
        valueLine.setGravity(Gravity.CENTER_VERTICAL);
        TextView valueView=txt(value,valueSize,color,true);
        valueView.setTextColor(color);
        valueLine.addView(valueView);
        TextView unitView=txt(unit,10,color,true);
        unitView.setTextColor(color);
        unitView.setPadding(dp(4),dp(2),0,0);
        valueLine.addView(unitView);
        metric.addView(valueLine);
        return metric;
    }

    private View navigationBadge(RhineNavigation.Stage stage){
        int color=navigationLevelColor(stage);
        TextView badge=txt(RhineNavigation.shortLabel(stage),10,color,true);
        badge.setTextColor(color);
        badge.setGravity(Gravity.CENTER);
        badge.setPadding(dp(8),dp(4),dp(8),dp(4));
        badge.setBackground(statusBadge(color));
        return badge;
    }

'''
    text = text[:summary_start] + summary_replacement + text[summary_end:]

    combined_start = text.index("    private LinearLayout riverCombinedCard(int slot){")
    combined_end = text.index("    private boolean riverSlotEnabled", combined_start)
    combined_replacement = '''    private LinearLayout riverCombinedCard(int slot){
        HydroStation station=riverStation(slot);
        RiverRange range=riverRange();
        String[] summary=hydroSummary(station);
        TrendSeries flow=hydroSeries(station,"Q",range);
        TrendSeries level=hydroSeries(station,"W",range);
        double flowNow=currentHydroValue(station,"Q");
        double levelNow=graphLevelValue(station,currentHydroValue(station,"W"));
        RhineNavigation.Stage stage=station==HydroStation.BASEL_RHEINHALLE?navigationStage():RhineNavigation.Stage.NORMAL;
        int flowColor=navigationFlowColor(stage);
        int levelColor=navigationLevelColor(stage);

        LinearLayout card=card();
        card.setOrientation(LinearLayout.VERTICAL);
        card.setPadding(dp(16),dp(14),dp(16),dp(14));

        LinearLayout header=new LinearLayout(this);
        header.setGravity(Gravity.CENTER_VERTICAL);
        header.addView(txt(station.label+" · BAFU "+station.id,12,TEXT,true),new LinearLayout.LayoutParams(0,-2,1));
        TextView source=txt("BAFU  ↗",11,WATER,true);
        source.setOnClickListener(v->external(station.stationUrl()));
        source.setPadding(dp(8),dp(4),0,dp(4));
        header.addView(source,new LinearLayout.LayoutParams(-2,-2));
        card.addView(header);

        LinearLayout values=new LinearLayout(this);
        values.setPadding(0,dp(8),0,dp(6));
        LinearLayout flowBox=new LinearLayout(this);
        flowBox.setOrientation(LinearLayout.VERTICAL);
        flowBox.addView(txt("Abfluss",11,MUTED,true));
        TextView q=txt(Double.isFinite(flowNow)?formatMetric(station,RiverMetric.FLOW,flowNow)+" m³/s":"–",16,flowColor,true);
        q.setTextColor(flowColor);
        flowBox.addView(q);
        values.addView(flowBox,new LinearLayout.LayoutParams(0,-2,1));

        LinearLayout levelBox=new LinearLayout(this);
        levelBox.setOrientation(LinearLayout.VERTICAL);
        levelBox.setGravity(Gravity.END);
        TextView levelTitle=txt("Pegel",11,MUTED,true);
        levelTitle.setGravity(Gravity.END);
        levelBox.addView(levelTitle);
        TextView w=txt(Double.isFinite(levelNow)?formatGraphLevel(station,levelNow)+" "+graphLevelUnit(station):"–",24,levelColor,true);
        w.setTextColor(levelColor);
        w.setGravity(Gravity.END);
        levelBox.addView(w);
        values.addView(levelBox,new LinearLayout.LayoutParams(0,-2,1));
        card.addView(values);

        if(station==HydroStation.BASEL_RHEINHALLE){
            TextView navigation=txt("Schifffahrtslage · "+RhineNavigation.shortLabel(stage),11,levelColor,true);
            navigation.setTextColor(levelColor);
            card.addView(navigation);
            TextView navigationDetail=txt(RhineNavigation.detail(stage),10,MUTED,false);
            navigationDetail.setPadding(0,dp(2),0,dp(5));
            card.addView(navigationDetail);
        }else{
            TextView basis=txt("Schifffahrtslage richtet sich nach Pegel Basel-Rheinhalle.",10,MUTED,false);
            basis.setPadding(0,0,0,dp(5));
            card.addView(basis);
        }

        if(RiverDisplay.hasVerifiedGaugeCentimetres(station)){
            TextView levelUnitLabel=txt("PEGEL-EINHEIT",9,MUTED,true);
            levelUnitLabel.setPadding(0,dp(2),0,dp(4));
            card.addView(levelUnitLabel);
            card.addView(riverGraphLevelUnitSelector(station),new LinearLayout.LayoutParams(-1,dp(38)));
        }

        if(flow.values.size()>=2&&level.values.size()>=2){
            DualRiverTrendView graph=new DualRiverTrendView(this,flow,level,range,station);
            card.addView(graph,new LinearLayout.LayoutParams(-1,dp(224)));
            TextView hint=txt("Abfluss links · Pegel rechts ("+graphLevelUnit(station)+") · Diagramm berühren für Einzelwerte",10,MUTED,false);
            hint.setGravity(Gravity.CENTER);
            hint.setPadding(0,dp(4),0,0);
            card.addView(hint);
        }else{
            TextView missing=txt("Für diesen Zeitraum liegen noch nicht genug Abfluss- und Pegelwerte vor.",12,MUTED,false);
            missing.setGravity(Gravity.CENTER);
            missing.setPadding(dp(8),dp(22),dp(8),dp(22));
            missing.setBackground(round(Color.rgb(238,243,246),14));
            card.addView(missing,margin(-1,-2,0,7,0,0));
        }

        TextView src=txt(summary[3]+" · "+range.sourceLabel,10,Color.rgb(126,140,150),false);
        src.setPadding(0,dp(8),0,0);
        card.addView(src);
        return card;
    }

'''
    text = text[:combined_start] + combined_replacement + text[combined_end:]

    helper_anchor = "    private LinearLayout segmentedBackground(){\n"
    navigation_helpers = '''    private RhineNavigation.Stage navigationStage(){
        double raw=currentHydroValue(HydroStation.BASEL_RHEINHALLE,"W");
        return RhineNavigation.fromBaselGaugeCm(RiverDisplay.gaugeCentimetres(HydroStation.BASEL_RHEINHALLE,raw));
    }

    private RhineNavigation.Stage navigationStageForGraphValue(HydroStation station,double displayedLevel){
        if(station!=HydroStation.BASEL_RHEINHALLE)return RhineNavigation.Stage.NORMAL;
        if(!Double.isFinite(displayedLevel))return RhineNavigation.Stage.UNKNOWN;
        double centimetres=riverGraphLevelCentimetres(station)
                ?displayedLevel
                :RiverDisplay.gaugeCentimetres(station,displayedLevel);
        return RhineNavigation.fromBaselGaugeCm(centimetres);
    }

    private int navigationLevelColor(RhineNavigation.Stage stage){
        if(stage==RhineNavigation.Stage.UNKNOWN)return themeText(MUTED);
        if(stage==RhineNavigation.Stage.NORMAL)return themeText(WATER);
        if(stage==RhineNavigation.Stage.HWM_I)return darkMode?Color.rgb(255,216,92):Color.rgb(205,143,0);
        if(stage==RhineNavigation.Stage.HWM_IIB)return darkMode?Color.rgb(255,148,86):Color.rgb(220,92,38);
        return darkMode?Color.rgb(255,105,105):Color.rgb(200,55,55);
    }

    private int navigationFlowColor(RhineNavigation.Stage stage){
        if(stage==RhineNavigation.Stage.UNKNOWN)return themeText(MUTED);
        if(stage==RhineNavigation.Stage.NORMAL)return themeText(RiverMetric.FLOW.color);
        if(stage==RhineNavigation.Stage.HWM_I)return darkMode?Color.rgb(224,169,72):Color.rgb(174,103,0);
        if(stage==RhineNavigation.Stage.HWM_IIB)return darkMode?Color.rgb(225,111,76):Color.rgb(166,66,31);
        return darkMode?Color.rgb(210,77,88):Color.rgb(139,39,45);
    }

'''
    if helper_anchor not in text:
        raise SystemExit("segmented background anchor not found")
    text = text.replace(helper_anchor, navigation_helpers + helper_anchor, 1)

    text = replace_once(
        text,
        '            int flowColor=statusTextColor(riverStatus(station,currentHydroValue(station,"Q")).bg);\n            int levelColor=themeText(WATER);\n',
        '            RhineNavigation.Stage currentStage=station==HydroStation.BASEL_RHEINHALLE?navigationStage():RhineNavigation.Stage.NORMAL;\n            int flowColor=navigationFlowColor(currentStage);\n            int levelColor=navigationLevelColor(currentStage);\n',
        "graph current colors",
    )
    text = replace_once(
        text,
        '            drawDualThresholds(canvas,flowScale,left,right,top,bottom);\n            drawDualSeries(canvas,flow,flowScale,left,right,top,bottom,minTime,maxTime,flowLine,flowColor);\n            drawDualSeries(canvas,level,levelScale,left,right,top,bottom,minTime,maxTime,levelLine,levelColor);\n',
        '            drawNavigationThresholds(canvas,levelScale,left,right,top,bottom);\n            drawDualSeries(canvas,flow,flowScale,left,right,top,bottom,minTime,maxTime,flowLine,false,flowColor);\n            drawDualSeries(canvas,level,levelScale,left,right,top,bottom,minTime,maxTime,levelLine,true,levelColor);\n',
        "graph draw calls",
    )

    series_start = text.index("        private void drawDualSeries(Canvas canvas,TrendSeries series")
    series_end = text.index("        private void drawDualTimeGrid", series_start)
    series_replacement = '''        private RhineNavigation.Stage navigationStageAtGraphTime(long timestamp){
            if(station!=HydroStation.BASEL_RHEINHALLE)return RhineNavigation.Stage.NORMAL;
            int index=HydroMath.nearestIndex(level.times,timestamp);
            if(index<0)return RhineNavigation.Stage.UNKNOWN;
            return navigationStageForGraphValue(station,level.values.get(index));
        }

        private void drawDualSeries(Canvas canvas,TrendSeries series,HydroMath.AxisScale scale,float left,float right,float top,float bottom,long minTime,long maxTime,Paint paint,boolean levelSeries,int fallbackColor){
            if(series.values.size()<2)return;
            if(station!=HydroStation.BASEL_RHEINHALLE){
                Path path=new Path();
                boolean started=false;
                for(int i=0;i<series.values.size();i++){
                    double value=series.values.get(i);if(!Double.isFinite(value))continue;
                    float x=left+(right-left)*(series.times.get(i)-minTime)/(float)(maxTime-minTime);
                    float y=(float)(bottom-(value-scale.min)/(scale.max-scale.min)*(bottom-top));
                    if(!started){path.moveTo(x,y);started=true;}else path.lineTo(x,y);
                }
                paint.setColor(fallbackColor);
                canvas.drawPath(path,paint);
                return;
            }
            boolean previous=false;
            float previousX=0f,previousY=0f;
            for(int i=0;i<series.values.size();i++){
                double value=series.values.get(i);if(!Double.isFinite(value)){previous=false;continue;}
                float x=left+(right-left)*(series.times.get(i)-minTime)/(float)(maxTime-minTime);
                float y=(float)(bottom-(value-scale.min)/(scale.max-scale.min)*(bottom-top));
                if(previous){
                    RhineNavigation.Stage segmentStage=navigationStageAtGraphTime(series.times.get(i));
                    paint.setColor(levelSeries?navigationLevelColor(segmentStage):navigationFlowColor(segmentStage));
                    canvas.drawLine(previousX,previousY,x,y,paint);
                }
                previous=true;previousX=x;previousY=y;
            }
        }

'''
    text = text[:series_start] + series_replacement + text[series_end:]

    thresholds_start = text.index("        private void drawDualThresholds(")
    thresholds_end = text.index("        private void drawDualSelection(", thresholds_start)
    thresholds_replacement = '''        private void drawNavigationThresholds(Canvas canvas,HydroMath.AxisScale levelScale,float left,float right,float top,float bottom){
            if(station!=HydroStation.BASEL_RHEINHALLE)return;
            boolean centimetres=riverGraphLevelCentimetres(station);
            for(RhineNavigation.Stage mark:RhineNavigation.officialThresholdStages()){
                double value=RhineNavigation.thresholdGraphValue(mark,centimetres);
                if(!Double.isFinite(value)||value<levelScale.min||value>levelScale.max)continue;
                float y=(float)(bottom-(value-levelScale.min)/(levelScale.max-levelScale.min)*(bottom-top));
                int color=navigationLevelColor(mark);
                threshold.setColor(color);
                canvas.drawLine(left,y,right,y,threshold);
                String marker=RhineNavigation.thresholdMarker(mark,centimetres);
                label.setColor(color);
                float markerWidth=label.measureText(marker);
                canvas.drawText(marker,Math.max(left+dp(4),right-markerWidth-dp(4)),Math.max(top+dp(10),y-dp(3)),label);
            }
        }

'''
    text = text[:thresholds_start] + thresholds_replacement + text[thresholds_end:]

    selection_anchor = '            double q=flow.values.get(qi),w=level.values.get(wi);\n'
    selection_replacement = '''            double q=flow.values.get(qi),w=level.values.get(wi);
            RhineNavigation.Stage selectedStage=station==HydroStation.BASEL_RHEINHALLE?navigationStageForGraphValue(station,w):RhineNavigation.Stage.NORMAL;
            if(station==HydroStation.BASEL_RHEINHALLE){
                flowColor=navigationFlowColor(selectedStage);
                levelColor=navigationLevelColor(selectedStage);
            }
'''
    text = replace_once(text, selection_anchor, selection_replacement, "selection stage colors")
    text = replace_once(
        text,
        '            String text=time.format(formatter)+" · "+formatMetric(station,RiverMetric.FLOW,q)+" m³/s · "+formatGraphLevel(station,w)+" "+graphLevelUnit(station);\n',
        '            String text=time.format(formatter)+" · "+formatMetric(station,RiverMetric.FLOW,q)+" m³/s · "+formatGraphLevel(station,w)+" "+graphLevelUnit(station)+(station==HydroStation.BASEL_RHEINHALLE?" · "+RhineNavigation.shortLabel(selectedStage):"");\n',
        "selection tooltip stage",
    )
    main_path.write_text(text, encoding="utf-8")

    Path("Android/app/src/main/java/ch/pfvr/internapp/RhineNavigation.java").write_text(
        '''package ch.pfvr.internapp;

/** Official Rhine navigation stages derived from Basel-Rheinhalle gauge levels. */
final class RhineNavigation {
    enum Stage { UNKNOWN, NORMAL, HWM_I, HWM_IIB, HWM_IIA }

    static final double HWM_I_CM = 700.0d;
    static final double HWM_IIB_CM = 790.0d;
    static final double HWM_IIA_CM = 820.0d;

    private RhineNavigation() {}

    static Stage fromBaselGaugeCm(double gaugeCm){
        if(!Double.isFinite(gaugeCm))return Stage.UNKNOWN;
        if(gaugeCm>=HWM_IIA_CM)return Stage.HWM_IIA;
        if(gaugeCm>=HWM_IIB_CM)return Stage.HWM_IIB;
        if(gaugeCm>=HWM_I_CM)return Stage.HWM_I;
        return Stage.NORMAL;
    }

    static String shortLabel(Stage stage){
        if(stage==Stage.UNKNOWN)return "Keine Lage";
        if(stage==Stage.HWM_I)return "HWM I";
        if(stage==Stage.HWM_IIB)return "Sperre IIb";
        if(stage==Stage.HWM_IIA)return "Sperre IIa";
        return "Normal";
    }

    static String detail(Stage stage){
        if(stage==Stage.UNKNOWN)return "Basel-Pegel derzeit nicht verfügbar.";
        if(stage==Stage.HWM_I)return "Voralarm ab 700 cm Pegel Basel-Rheinhalle.";
        if(stage==Stage.HWM_IIB)return "Kleinschifffahrt und Fähren Basel–Rheinfelden gesperrt.";
        if(stage==Stage.HWM_IIA)return "Schifffahrt Rheinfelden–Kembs gesperrt.";
        return "Unter Hochwassermarke I (< 700 cm).";
    }

    static Stage[] officialThresholdStages(){
        return new Stage[]{Stage.HWM_I,Stage.HWM_IIB,Stage.HWM_IIA};
    }

    static double thresholdGaugeCm(Stage stage){
        if(stage==Stage.HWM_I)return HWM_I_CM;
        if(stage==Stage.HWM_IIB)return HWM_IIB_CM;
        if(stage==Stage.HWM_IIA)return HWM_IIA_CM;
        return Double.NaN;
    }

    static double thresholdGraphValue(Stage stage,boolean centimetres){
        double cm=thresholdGaugeCm(stage);
        if(!Double.isFinite(cm))return Double.NaN;
        return centimetres?cm:240.0d+cm/100.0d;
    }

    static String thresholdMarker(Stage stage,boolean centimetres){
        double value=thresholdGraphValue(stage,centimetres);
        String prefix=stage==Stage.HWM_I?"I":(stage==Stage.HWM_IIB?"IIb":"IIa");
        return centimetres
                ?String.format(java.util.Locale.GERMAN,"%s %.0f",prefix,value)
                :String.format(java.util.Locale.GERMAN,"%s %.2f",prefix,value);
    }
}
''',
        encoding="utf-8",
    )

    Path("Android/app/src/test/java/ch/pfvr/internapp/RhineNavigationTest.java").write_text(
        '''package ch.pfvr.internapp;

import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class RhineNavigationTest {
    @Test public void appliesOfficialBaselGaugeThresholdsAtExactBoundaries(){
        assertEquals(RhineNavigation.Stage.NORMAL,RhineNavigation.fromBaselGaugeCm(699.9));
        assertEquals(RhineNavigation.Stage.HWM_I,RhineNavigation.fromBaselGaugeCm(700.0));
        assertEquals(RhineNavigation.Stage.HWM_I,RhineNavigation.fromBaselGaugeCm(789.9));
        assertEquals(RhineNavigation.Stage.HWM_IIB,RhineNavigation.fromBaselGaugeCm(790.0));
        assertEquals(RhineNavigation.Stage.HWM_IIB,RhineNavigation.fromBaselGaugeCm(819.9));
        assertEquals(RhineNavigation.Stage.HWM_IIA,RhineNavigation.fromBaselGaugeCm(820.0));
    }

    @Test public void convertsOfficialMarkersForBothGraphUnits(){
        assertEquals(700.0,RhineNavigation.thresholdGraphValue(RhineNavigation.Stage.HWM_I,true),0.0001);
        assertEquals(247.00,RhineNavigation.thresholdGraphValue(RhineNavigation.Stage.HWM_I,false),0.0001);
        assertEquals(247.90,RhineNavigation.thresholdGraphValue(RhineNavigation.Stage.HWM_IIB,false),0.0001);
        assertEquals(248.20,RhineNavigation.thresholdGraphValue(RhineNavigation.Stage.HWM_IIA,false),0.0001);
    }

    @Test public void reportsUnknownWhenBaselGaugeIsMissing(){
        assertEquals(RhineNavigation.Stage.UNKNOWN,RhineNavigation.fromBaselGaugeCm(Double.NaN));
        assertTrue(RhineNavigation.detail(RhineNavigation.Stage.HWM_IIB).contains("Kleinschifffahrt"));
        assertTrue(RhineNavigation.detail(RhineNavigation.Stage.HWM_IIA).contains("Rheinfelden"));
    }
}
''',
        encoding="utf-8",
    )

    hydro = Path("Android/app/src/main/java/ch/pfvr/internapp/HydroStation.java")
    hs = hydro.read_text(encoding="utf-8")
    old_comments = '''    // Basel defaults are operational navigation thresholds from Port of Switzerland:
    // ~1800 m³/s = 700 cm / Hochwassermarke I (Voralarm),
    // ~2500 m³/s = 790 cm / IIb (small craft and ferries Basel–Rheinfelden closed).
    BASEL_RHEINHALLE("2289", "Basel, Rheinhalle", false, 400f, 1800f, 2500f),
    // Rheinfelden defaults use BAFU flood danger levels until PFVR-specific limits are available.
    RHEINFELDEN("2091", "Rheinfelden", true, 400f, 2500f, 3600f);
'''
    new_comments = '''    // Legacy flow thresholds remain only for preference compatibility. Official navigation
    // status is derived separately from the verified Basel-Rheinhalle gauge in RhineNavigation.
    BASEL_RHEINHALLE("2289", "Basel, Rheinhalle", false, 400f, 1800f, 2500f),
    RHEINFELDEN("2091", "Rheinfelden", true, 400f, 2500f, 3600f);
'''
    hs = replace_once(hs, old_comments, new_comments, "HydroStation legacy comments")
    hydro.write_text(hs, encoding="utf-8")

    gradle = Path("Android/app/build.gradle")
    build = gradle.read_text(encoding="utf-8")
    build = replace_once(build, "versionCode 43", "versionCode 44", "version code")
    build = replace_once(build, "versionName '0.10.19'", "versionName '0.10.20'", "version name")
    gradle.write_text(build, encoding="utf-8")

    changelog = Path("Android/CHANGELOG.md")
    ch = changelog.read_text(encoding="utf-8")
    ch = replace_once(
        ch,
        "# Android Changelog\n",
        '''# Android Changelog

## 0.10.20
- Die offizielle Schifffahrtslage wird ausschließlich aus dem Pegel Basel-Rheinhalle abgeleitet: 700 cm = HWM I/Voralarm, 790 cm = HWM IIb/Sperrung Kleinschifffahrt und Fähren Basel–Rheinfelden, 820 cm = HWM IIa/Sperrung Rheinfelden–Kembs. Abflusswerte steuern keine Sperrampel mehr.
- `Rhein aktuell` priorisiert den Pegel wieder visuell. Basel-Pegel und Statusbadge verwenden die aktuelle Hochwasserstufe; der Abfluss bleibt kleiner und erhält je Stufe eine unterscheidbare Schwesterfarbe. Rheinfelden bleibt mangels eigenem offiziellem Sperrbezug neutral.
- Im Basel-Diagramm liegen die drei offiziellen Hochwassermarken auf der Pegelachse. Pegel- und Abflusskurve werden abschnittsweise nach der zeitgleichen Pegelstufe eingefärbt, bleiben aber farblich voneinander unterscheidbar; Auswahlpunkte und Tooltip übernehmen die historische Stufe.
- Die bisherigen frei einstellbaren Abfluss-Grenzwerte werden in den Rhein-Einstellungen nicht mehr als Schifffahrtsstatus angeboten. Stattdessen werden die offiziellen Pegelmarken samt Quelle dargestellt.
''',
        "changelog header",
    )
    changelog.write_text(ch, encoding="utf-8")

    status = Path("STATUS.md")
    st = status.read_text(encoding="utf-8")
    st = st.replace(
        "- Zusätzliche Tests prüfen absolute Wasserstände in `m ü.M.`, den bestätigten Basel-cm-Bezug sowie den sicheren Fallback auf `m ü.M.`, wenn für eine Station kein verifizierter cm-Bezug vorhanden ist.\n",
        "- Zusätzliche Tests prüfen absolute Wasserstände in `m ü.M.`, den bestätigten Basel-cm-Bezug sowie den sicheren Fallback auf `m ü.M.`, wenn für eine Station kein verifizierter cm-Bezug vorhanden ist. Die offiziellen Basel-Hochwassermarken werden an ihren exakten Grenzen 700/790/820 cm sowie in beiden Graph-Einheiten getestet.\n",
        1,
    )
    old_device = "- Bei Basel-Rheinhalle muss der Graph zwischen `m ü.M.` und `cm` umschaltbar sein. Aktueller Pegelwert, rechte Achse, Kurve, Hinweis und Tooltip dürfen dabei keine gemischten Einheiten zeigen. Bei Rheinfelden darf kein cm-Umschalter angeboten werden.\n"
    if old_device in st:
        st = st.replace(
            old_device,
            old_device
            + "- Basel muss bei 700/790/820 cm sauber zwischen Normal, HWM I, Sperre IIb und Sperre IIa wechseln. Pegelwert, Badge, Hochwassermarken, Kurvenabschnitte und ausgewählte Diagrammpunkte müssen die Stufe widerspruchsfrei zeigen; Abfluss und Pegel müssen dabei trotz gemeinsamer Stufenfamilie optisch unterscheidbar bleiben.\n",
            1,
        )
    status.write_text(st, encoding="utf-8")

    readme = Path("README.md")
    rd = readme.read_text(encoding="utf-8")
    rd = rd.replace("Android-Testversion `0.10.19` auf `main`.", "Android-Testversion `0.10.20` auf `main`.", 1)
    rd = rd.replace(
        "BAFU-Rheindaten mit Abfluss, Wasserstand, Wassertemperatur und interaktiven Verläufen; Basel zeigt zusätzlich den verifizierten cm-Pegel und kann im Graph zwischen m ü.M. und cm wechseln, Rheinfelden bleibt ohne abgeleiteten cm-Wert;",
        "BAFU-Rheindaten mit Abfluss, Wasserstand, Wassertemperatur und interaktiven Verläufen; die offizielle Schifffahrtslage folgt ausschließlich den Hochwassermarken 700/790/820 cm am Pegel Basel-Rheinhalle, während der Abfluss als eigener Messwert erhalten bleibt;",
        1,
    )
    readme.write_text(rd, encoding="utf-8")

    android_readme = Path("Android/README.md")
    ard = android_readme.read_text(encoding="utf-8")
    ard = ard.replace("Aktuelle Android-Testversion: `0.10.19`.", "Aktuelle Android-Testversion: `0.10.20`.", 1)
    ard = ard.replace("0.10.19 verwendet `versionCode 43`", "0.10.20 verwendet `versionCode 44`")
    android_readme.write_text(ard, encoding="utf-8")

    Path("decisions/river-navigation-status.md").write_text(
        '''# Rhein-Schifffahrtslage

Stand: 2026-09-04

## Entscheidung

Die App leitet Warnung und Schifffahrtssperrung ausschließlich aus dem Pegel **Basel-Rheinhalle** ab. Maßgeblich sind die von den Schweizerischen Rheinhäfen veröffentlichten Hochwassermarken:

- 700 cm: Hochwassermarke I, Voralarm.
- 790 cm: Hochwassermarke IIb, Sperrung der Kleinschifffahrt und des Fährbetriebs zwischen Basel und Rheinfelden.
- 820 cm: Hochwassermarke IIa, Sperrung der Schifffahrt zwischen Rheinfelden und der Schleuse Kembs.

Der Abfluss in m³/s bleibt sichtbar, bestimmt aber keine offizielle Sperrstufe. Näherungsweise korrespondierende Abflusswerte werden nicht zur Statuslogik verwendet.

## Darstellung

- Pegel ist die priorisierte Größe der Rhein-Kurzkarten.
- Basel erhält eine stufenabhängige Pegelfarbe und einen Lage-Badge.
- Pegel- und Abflusskurve verwenden innerhalb einer Stufe unterschiedliche, aber verwandte Farbtöne. So bleibt erkennbar, welche Kurve welcher Messgröße gehört, während die Hochwasserstufe visuell erhalten bleibt.
- Historische Abschnitte im Basel-Graph werden anhand des zeitgleichen Basel-Pegels eingefärbt.
- Die Hochwassermarken werden auf der Pegelachse eingezeichnet.
- Rheinfelden wird nicht aus einem angenommenen Pegelnullpunkt oder lokalen Schwellen in eine offizielle Schifffahrtsstufe umgerechnet.

## Daten- und Quellenregel

Ableitungen werden nur verwendet, wenn ihr Bezug belastbar dokumentiert ist. Für Basel ist der Zusammenhang zwischen BAFU-Wasserstand und Pegel verifiziert. Für Rheinfelden wird ohne bestätigten aktuellen Pegelnullpunkt kein relativer cm-Pegel und keine lokale Sperrstufe erzeugt.

Primärquelle für Hochwassermarken und Sperrbedeutung: Schweizerische Rheinhäfen, `https://port-of-switzerland.ch/hafenservice/pegel/`.
''',
        encoding="utf-8",
    )


if __name__ == "__main__":
    if len(sys.argv) != 2 or sys.argv[1] not in {"plan", "implement"}:
        raise SystemExit("usage: update_0_10_20.py plan|implement")
    if sys.argv[1] == "plan":
        plan()
    else:
        implement()
