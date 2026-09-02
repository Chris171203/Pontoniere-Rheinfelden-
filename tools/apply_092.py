from pathlib import Path

MAIN = Path('Android/app/src/main/java/ch/pfvr/internapp/MainActivity.java')
BUILD = Path('Android/app/build.gradle')
PROJECT = Path('PROJECT.md')
STATUS = Path('STATUS.md')


def replace_between(text: str, start: str, end: str, replacement: str, label: str) -> str:
    i = text.find(start)
    if i < 0:
        raise SystemExit(f'start marker not found for {label}: {start!r}')
    j = text.find(end, i)
    if j < 0:
        raise SystemExit(f'end marker not found for {label}: {end!r}')
    return text[:i] + replacement + text[j:]


s = MAIN.read_text(encoding='utf-8')

live_block = r'''    private void populateHomeLiveStack(LinearLayout stack){
        stack.removeAllViews();
        LinearLayout weather=weatherCard();
        stack.addView(weather,margin(-1,-2,0,0,0,10));

        TextView rangeLabel=txt("RHEIN · ZEITRAUM",10,MUTED,true);
        rangeLabel.setPadding(dp(2),dp(2),0,dp(5));
        stack.addView(rangeLabel);
        stack.addView(riverRangeSelector(riverRange()),new LinearLayout.LayoutParams(-1,dp(42)));

        stack.addView(riverSummaryRow(),margin(-1,-2,0,10,0,10));
        stack.addView(riverCombinedCard(1),margin(-1,-2,0,0,0,10));
        if(riverSlotEnabled(2))stack.addView(riverCombinedCard(2),margin(-1,-2,0,0,0,4));
    }

    private void refreshHomeLiveViews(){
        if(current!=Screen.HOME||homeLiveStack==null)return;
        final int scrollY=homeScroll==null?0:homeScroll.getScrollY();
        populateHomeLiveStack(homeLiveStack);
        if(homeScroll!=null){
            homeScroll.postOnAnimation(()->homeScroll.scrollTo(0,scrollY));
            homeScroll.postDelayed(()->homeScroll.scrollTo(0,scrollY),80);
        }
    }

    private void rebuildHomePreservingScroll(){
        if(current!=Screen.HOME)return;
        final int scrollY=homeScroll==null?0:homeScroll.getScrollY();
        navigate(Screen.HOME);
        if(homeScroll!=null){
            homeScroll.postOnAnimation(()->homeScroll.scrollTo(0,scrollY));
            homeScroll.postDelayed(()->homeScroll.scrollTo(0,scrollY),80);
        }
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

    private View riverSummaryRow(){
        LinearLayout row=new LinearLayout(this);
        row.setGravity(Gravity.TOP);
        row.addView(riverSummaryCard(1),new LinearLayout.LayoutParams(0,-2,1));
        if(riverSlotEnabled(2)){
            LinearLayout.LayoutParams second=new LinearLayout.LayoutParams(0,-2,1);
            second.setMargins(dp(8),0,0,0);
            row.addView(riverSummaryCard(2),second);
        }
        return row;
    }

    private LinearLayout riverSummaryCard(int slot){
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

        LinearLayout valueRow=new LinearLayout(this);
        valueRow.setGravity(Gravity.CENTER_VERTICAL);
        valueRow.setPadding(0,dp(5),0,dp(2));
        TextView flowValue=txt(Double.isFinite(flow)?formatMetric(station,RiverMetric.FLOW,flow):"–",27,statusColor,true);
        flowValue.setTextColor(statusColor);
        valueRow.addView(flowValue);
        TextView flowUnit=txt("m³/s",12,statusColor,true);
        flowUnit.setTextColor(statusColor);
        flowUnit.setPadding(dp(4),0,0,0);
        valueRow.addView(flowUnit);
        valueRow.addView(new View(this),new LinearLayout.LayoutParams(0,1,1));
        valueRow.addView(riverStatusCompact(status));
        c.addView(valueRow);

        StringBuilder details=new StringBuilder();
        if(Double.isFinite(level))details.append("Pegel ").append(formatMetric(station,RiverMetric.LEVEL,level)).append(' ').append(metricUnit(station,RiverMetric.LEVEL));
        if(Double.isFinite(temperature)){
            if(details.length()>0)details.append("\n");
            details.append("Wasser ").append(formatMetric(station,RiverMetric.TEMPERATURE,temperature)).append(" °C");
        }
        TextView secondary=txt(details.length()==0?"Messwerte werden geladen …":details.toString(),11,MUTED,false);
        secondary.setPadding(0,dp(3),0,0);
        c.addView(secondary);
        return c;
    }

    private View riverStatusCompact(RiverStatus status){
        int color=statusTextColor(status.bg);
        TextView label=txt(status.label,10,color,true);
        label.setTextColor(color);
        label.setGravity(Gravity.CENTER);
        label.setPadding(dp(8),dp(4),dp(8),dp(4));
        label.setBackground(statusBadge(status.bg));
        return label;
    }

    private LinearLayout riverCombinedCard(int slot){
        HydroStation station=riverStation(slot);
        RiverRange range=riverRange();
        String[] summary=hydroSummary(station);
        TrendSeries flow=hydroSeries(station,"Q",range);
        TrendSeries level=hydroSeries(station,"W",range);
        double flowNow=currentHydroValue(station,"Q");
        double levelNow=displayHydroValue(station,RiverMetric.LEVEL,currentHydroValue(station,"W"));
        RiverStatus status=riverStatus(station,flowNow);
        int flowColor=statusTextColor(status.bg);

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
        TextView q=txt(Double.isFinite(flowNow)?formatMetric(station,RiverMetric.FLOW,flowNow)+" m³/s":"–",18,flowColor,true);
        q.setTextColor(flowColor);
        flowBox.addView(q);
        values.addView(flowBox,new LinearLayout.LayoutParams(0,-2,1));

        LinearLayout levelBox=new LinearLayout(this);
        levelBox.setOrientation(LinearLayout.VERTICAL);
        levelBox.setGravity(Gravity.END);
        TextView levelTitle=txt("Pegel",11,MUTED,true);levelTitle.setGravity(Gravity.END);levelBox.addView(levelTitle);
        TextView w=txt(Double.isFinite(levelNow)?formatMetric(station,RiverMetric.LEVEL,levelNow)+" "+metricUnit(station,RiverMetric.LEVEL):"–",18,WATER,true);
        w.setGravity(Gravity.END);levelBox.addView(w);
        values.addView(levelBox,new LinearLayout.LayoutParams(0,-2,1));
        card.addView(values);

        TextView limits=txt(String.format(Locale.GERMAN,"Abfluss · Niedrig < %.0f · Warn ab %.0f · Alarm ab %.0f m³/s",riverLow(station),riverWarn(station),riverAlarm(station)),10,MUTED,false);
        limits.setPadding(0,0,0,dp(4));
        card.addView(limits);

        if(flow.values.size()>=2&&level.values.size()>=2){
            DualRiverTrendView graph=new DualRiverTrendView(this,flow,level,range,station);
            card.addView(graph,new LinearLayout.LayoutParams(-1,dp(224)));
            TextView hint=txt("Abfluss links · Pegel rechts · Diagramm berühren für Einzelwerte",10,MUTED,false);
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

    private String riverMetricTitle(RiverMetric metric){return metric==RiverMetric.TEMPERATURE?"Wassertemperatur":metric.label;}

    private boolean riverSlotEnabled(int slot){return slot==1||prefs.getBoolean(PREF_RIVER_SLOT2_ENABLED,true);}

    private HydroStation riverStation(int slot){
        String key=slot==1?PREF_RIVER_SLOT1_STATION:PREF_RIVER_SLOT2_STATION;
        HydroStation fallback=slot==1?HydroStation.BASEL_RHEINHALLE:HydroStation.RHEINFELDEN;
        return HydroStation.from(prefs.getString(key,fallback.id),fallback);
    }

'''

s = replace_between(
    s,
    '    private void populateHomeLiveStack(LinearLayout stack){',
    '    private RiverRange riverRange()',
    live_block,
    '0.9.2 combined river presentation',
)

hero_marker = '        hero.addView(txt("Artikel für dich, Kinder oder die ganze Runde zusammenstellen – oder weiterhin einen freien Betrag verwenden.",14,Color.rgb(232,243,247),false));\n\n'
if 'body.addView(bankChoiceCard()' not in s:
    if hero_marker not in s:
        raise SystemExit('cash hero marker not found')
    s = s.replace(
        hero_marker,
        hero_marker + '        section(body,"Banking-App","Einmal festlegen; geöffnet wird sie erst beim Bezahlen.");\n        body.addView(bankChoiceCard(),margin(-1,-2,0,0,0,12));\n\n',
        1,
    )

old_pay_row = '''        LinearLayout payRow=new LinearLayout(this);
        Button cartQr=btn("Swiss QR",NAVY,Color.WHITE);cartQr.setOnClickListener(v->{EditText input=cartAmountInput();if(input!=null)showPaymentQr(input);});payRow.addView(cartQr,new LinearLayout.LayoutParams(0,dp(46),1));
        Button cartBank=btn("Direkt Bank",Color.rgb(232,240,244),NAVY);cartBank.setOnClickListener(v->{EditText input=cartAmountInput();if(input!=null)sharePaymentQr(input);});LinearLayout.LayoutParams cbp=new LinearLayout.LayoutParams(0,dp(46),1);cbp.setMargins(dp(7),0,0,0);payRow.addView(cartBank,cbp);
        Button cartTwint=btn("TWINT",Color.rgb(232,240,244),NAVY);cartTwint.setOnClickListener(v->{EditText input=cartAmountInput();if(input!=null)openTwintDirect(input);});LinearLayout.LayoutParams ctp=new LinearLayout.LayoutParams(0,dp(46),1);ctp.setMargins(dp(7),0,0,0);payRow.addView(cartTwint,ctp);
        cart.addView(payRow);
        TextView directInfo=txt("„Direkt Bank“ übergibt den Swiss-QR temporär an eine kompatible Banking-App – ohne ihn vorher dauerhaft zu speichern. Nicht jede Bank unterstützt diesen Android-Import.",11,MUTED,false);directInfo.setPadding(0,dp(8),0,0);cart.addView(directInfo);'''
new_pay_row = '''        Button cartBank=btn(preferredBankPaymentLabel(),NAVY,Color.WHITE);cartBank.setOnClickListener(v->{EditText input=cartAmountInput();if(input!=null)payWithPreferredBank(input);});cart.addView(cartBank,new LinearLayout.LayoutParams(-1,dp(48)));
        LinearLayout payRow=new LinearLayout(this);payRow.setPadding(0,dp(8),0,0);
        Button cartQr=btn("Swiss QR",Color.rgb(232,240,244),NAVY);cartQr.setOnClickListener(v->{EditText input=cartAmountInput();if(input!=null)showPaymentQr(input);});payRow.addView(cartQr,new LinearLayout.LayoutParams(0,dp(44),1));
        Button cartTwint=btn("TWINT",Color.rgb(232,240,244),NAVY);cartTwint.setOnClickListener(v->{EditText input=cartAmountInput();if(input!=null)openTwintDirect(input);});LinearLayout.LayoutParams ctp=new LinearLayout.LayoutParams(0,dp(44),1);ctp.setMargins(dp(7),0,0,0);payRow.addView(cartTwint,ctp);
        cart.addView(payRow);
        TextView directInfo=txt("Die gewählte Banking-App erhält den Swiss QR temporär – ohne vorheriges Speichern. Mit Yuh wurde dieser Weg erfolgreich getestet.",11,MUTED,false);directInfo.setPadding(0,dp(8),0,0);cart.addView(directInfo);'''
if old_pay_row not in s:
    raise SystemExit('cart payment row marker not found')
s = s.replace(old_pay_row,new_pay_row,1)

old_free_bank = '''        Button direct=btn("QR direkt an Banking-App",Color.rgb(232,240,244),NAVY);direct.setOnClickListener(v->sharePaymentQr(amount));LinearLayout.LayoutParams dlp=new LinearLayout.LayoutParams(-1,dp(44));dlp.setMargins(0,dp(8),0,0);amountCard.addView(direct,dlp);
        String bankLabel=prefs.getString(PREF_BANK_LABEL,"");
        Button bank=btn(bankLabel.trim().isEmpty()?"Banking-App auswählen":bankLabel+" öffnen",Color.rgb(232,240,244),NAVY);bankButton=bank;bank.setOnClickListener(v->openPreferred(false,amount));LinearLayout.LayoutParams bp=new LinearLayout.LayoutParams(-1,dp(44));bp.setMargins(0,dp(8),0,0);amountCard.addView(bank,bp);
        TextView change=link(bankLabel.trim().isEmpty()?"Banking-App für Direktstart wählen":"Andere Banking-App wählen");change.setOnClickListener(v->chooseApp(false,amount));amountCard.addView(change);'''
new_free_bank = '''        Button direct=btn(preferredBankPaymentLabel(),Color.rgb(232,240,244),NAVY);direct.setOnClickListener(v->payWithPreferredBank(amount));LinearLayout.LayoutParams dlp=new LinearLayout.LayoutParams(-1,dp(44));dlp.setMargins(0,dp(8),0,0);amountCard.addView(direct,dlp);'''
if old_free_bank not in s:
    raise SystemExit('free amount bank block marker not found')
s = s.replace(old_free_bank,new_free_bank,1)

bank_helpers = r'''    private View bankChoiceCard(){
        LinearLayout card=card();
        card.setOrientation(LinearLayout.VERTICAL);
        String label=prefs.getString(PREF_BANK_LABEL,"").trim();
        String pkg=prefs.getString(PREF_BANK_PACKAGE,"").trim();
        boolean selected=!label.isEmpty()&&!pkg.isEmpty();
        card.addView(txt(selected?label:"Noch keine Banking-App festgelegt",16,TEXT,true));
        TextView info=txt(selected?"Wird für die direkte Swiss-QR-Übergabe verwendet.":"Wähle die Banking-App, die beim Tippen auf Bezahlen verwendet werden soll.",12,MUTED,false);
        info.setPadding(0,dp(4),0,dp(10));
        card.addView(info);
        LinearLayout actions=new LinearLayout(this);
        Button choose=btn(selected?"Ändern":"Bank auswählen",selected?Color.rgb(232,240,244):NAVY,selected?NAVY:Color.WHITE);
        choose.setOnClickListener(v->chooseApp(false,null));
        actions.addView(choose,new LinearLayout.LayoutParams(0,dp(44),1));
        if(selected){
            Button clear=btn("Entfernen",Color.rgb(232,240,244),NAVY);
            clear.setOnClickListener(v->{prefs.edit().remove(PREF_BANK_PACKAGE).remove(PREF_BANK_LABEL).apply();navigate(Screen.CASH);});
            LinearLayout.LayoutParams clearParams=new LinearLayout.LayoutParams(0,dp(44),1);clearParams.setMargins(dp(8),0,0,0);actions.addView(clear,clearParams);
        }
        card.addView(actions);
        return card;
    }

    private String preferredBankPaymentLabel(){
        String label=prefs.getString(PREF_BANK_LABEL,"").trim();
        return label.isEmpty()?"Banking-App auswählen":"Mit "+label+" bezahlen";
    }

    private void payWithPreferredBank(EditText amountInput){
        if(prefs.getString(PREF_BANK_PACKAGE,"").trim().isEmpty()){
            Toast.makeText(this,"Bitte oben zuerst eine Banking-App auswählen.",Toast.LENGTH_SHORT).show();
            return;
        }
        sharePaymentQr(amountInput);
    }

'''
if 'private View bankChoiceCard()' not in s:
    marker='    private CashCatalog.Catalog cashCatalog()'
    if marker not in s:
        raise SystemExit('cashCatalog marker not found')
    s=s.replace(marker,bank_helpers+marker,1)

choose_app = r'''    private void chooseApp(boolean twint, EditText amountInput) {
        PackageManager pm=getPackageManager(); Intent q=new Intent(Intent.ACTION_MAIN); q.addCategory(Intent.CATEGORY_LAUNCHER); List<ResolveInfo> all=pm.queryIntentActivities(q,0); List<AppChoice> found=new ArrayList<>();
        for(ResolveInfo r:all){
            if(r.activityInfo==null||r.activityInfo.packageName==null)continue;
            String pkg=r.activityInfo.packageName;String label=String.valueOf(r.loadLabel(pm));String h=(label+" "+pkg).toLowerCase(Locale.ROOT);
            boolean looksTwint=h.contains("twint");
            boolean looksBank=h.matches(".*(ubs|postfinance|raiffeisen|zkb|kantonal|bank|neon|yuh|revolut|swissquote|cler|zak|migros|credit suisse|csx).*");
            if((twint&&looksTwint)||(!twint&&looksBank))found.add(new AppChoice(label,pkg));
        }
        if(found.isEmpty()&&!twint) for(ResolveInfo r:all) if(r.activityInfo!=null && r.activityInfo.packageName!=null && !r.activityInfo.packageName.equals(getPackageName())) found.add(new AppChoice(String.valueOf(r.loadLabel(pm)),r.activityInfo.packageName));
        found.sort((a,b)->{int pa=bankPriority(a),pb=bankPriority(b);if(pa!=pb)return Integer.compare(pa,pb);return a.label.compareToIgnoreCase(b.label);});
        if(found.isEmpty()) { if(twint) { try{startActivity(new Intent(Intent.ACTION_VIEW,Uri.parse("market://search?q=TWINT&c=apps")));}catch(Exception e){external("https://www.twint.ch/privatkunden/");} } else Toast.makeText(this,"Keine passende Banking-App gefunden.",Toast.LENGTH_LONG).show(); return; }
        String[] labels=new String[found.size()]; for(int i=0;i<found.size();i++) labels[i]=found.get(i).label;
        new AlertDialog.Builder(this,dialogTheme()).setTitle(twint?"TWINT-App auswählen":"Banking-App auswählen").setItems(labels,(d,i)->{
            AppChoice c=found.get(i);
            prefs.edit().putString(twint?PREF_TWINT_PACKAGE:PREF_BANK_PACKAGE,c.pkg).putString(twint?PREF_TWINT_LABEL:PREF_BANK_LABEL,c.label).apply();
            if(!twint){
                Toast.makeText(this,c.label+" festgelegt.",Toast.LENGTH_SHORT).show();
                if(current==Screen.CASH)navigate(Screen.CASH);
            }else if(bankButton!=null){bankButton.setText(c.label);}
        }).setNegativeButton("Abbrechen",null).show();
    }

'''
s = replace_between(s,'    private void chooseApp(boolean twint, EditText amountInput) {','    private int bankPriority(AppChoice app)',choose_app,'bank chooser without auto launch')

dual_chart = r'''    private class DualRiverTrendView extends View {
        private final TrendSeries flow;
        private final TrendSeries level;
        private final RiverRange range;
        private final HydroStation station;
        private final Paint grid=new Paint(Paint.ANTI_ALIAS_FLAG);
        private final Paint label=new Paint(Paint.ANTI_ALIAS_FLAG);
        private final Paint flowLine=new Paint(Paint.ANTI_ALIAS_FLAG);
        private final Paint levelLine=new Paint(Paint.ANTI_ALIAS_FLAG);
        private final Paint threshold=new Paint(Paint.ANTI_ALIAS_FLAG);
        private final Paint point=new Paint(Paint.ANTI_ALIAS_FLAG);
        private final Paint tooltip=new Paint(Paint.ANTI_ALIAS_FLAG);
        private final Paint tooltipText=new Paint(Paint.ANTI_ALIAS_FLAG);
        private long selectedTime=Long.MIN_VALUE;

        DualRiverTrendView(Context context,TrendSeries flow,TrendSeries level,RiverRange range,HydroStation station){
            super(context);this.flow=flow;this.level=level;this.range=range;this.station=station;
            setClickable(true);setFocusable(true);
            grid.setStrokeWidth(dp(1));label.setTextSize(9*getResources().getDisplayMetrics().scaledDensity);
            flowLine.setStyle(Paint.Style.STROKE);flowLine.setStrokeWidth(dp(2.5f));flowLine.setStrokeCap(Paint.Cap.ROUND);flowLine.setStrokeJoin(Paint.Join.ROUND);
            levelLine.setStyle(Paint.Style.STROKE);levelLine.setStrokeWidth(dp(2.2f));levelLine.setStrokeCap(Paint.Cap.ROUND);levelLine.setStrokeJoin(Paint.Join.ROUND);
            threshold.setStyle(Paint.Style.STROKE);threshold.setStrokeWidth(dp(1.2f));threshold.setPathEffect(new DashPathEffect(new float[]{dp(6),dp(4)},0));
            point.setStyle(Paint.Style.FILL);tooltip.setStyle(Paint.Style.FILL);
            tooltipText.setTextSize(10*getResources().getDisplayMetrics().scaledDensity);tooltipText.setTypeface(Typeface.DEFAULT_BOLD);
            setContentDescription("Abfluss und Pegel "+station.label+" · "+range.label);
        }

        @Override public boolean performClick(){super.performClick();return true;}

        @Override public boolean onTouchEvent(MotionEvent event){
            if(flow.times.size()<2||level.times.size()<2)return super.onTouchEvent(event);
            if(event.getAction()==MotionEvent.ACTION_DOWN||event.getAction()==MotionEvent.ACTION_MOVE||event.getAction()==MotionEvent.ACTION_UP){
                float left=dp(53),right=getWidth()-dp(53);
                float x=Math.max(left,Math.min(right,event.getX()));
                long minTime=Math.min(flow.times.get(0),level.times.get(0));
                long maxTime=Math.max(flow.times.get(flow.times.size()-1),level.times.get(level.times.size()-1));
                selectedTime=minTime+Math.round((maxTime-minTime)*(x-left)/Math.max(1f,right-left));
                invalidate();
                if(event.getAction()==MotionEvent.ACTION_UP)performClick();
                return true;
            }
            if(event.getAction()==MotionEvent.ACTION_CANCEL){selectedTime=Long.MIN_VALUE;invalidate();return true;}
            return super.onTouchEvent(event);
        }

        @Override protected void onDraw(Canvas canvas){
            super.onDraw(canvas);
            if(flow.values.size()<2||level.values.size()<2)return;
            float left=dp(53),right=getWidth()-dp(53),top=dp(18),bottom=getHeight()-dp(31);
            long minTime=Math.min(flow.times.get(0),level.times.get(0));
            long maxTime=Math.max(flow.times.get(flow.times.size()-1),level.times.get(level.times.size()-1));
            if(maxTime<=minTime)maxTime=minTime+1;
            HydroMath.AxisScale flowScale=HydroMath.niceAxis(flow.values);
            HydroMath.AxisScale levelScale=HydroMath.niceAxis(level.values);
            int flowColor=statusTextColor(riverStatus(station,currentHydroValue(station,"Q")).bg);
            int levelColor=themeText(WATER);

            grid.setColor(darkMode?Color.rgb(57,72,82):Color.rgb(220,229,234));
            label.setColor(themeText(MUTED));
            for(int i=0;i<=4;i++){
                float y=top+(bottom-top)*i/4f;
                canvas.drawLine(left,y,right,y,grid);
                double q=flowScale.max-(flowScale.max-flowScale.min)*i/4d;
                double w=levelScale.max-(levelScale.max-levelScale.min)*i/4d;
                String qText=axisLabel(q,flowScale.step);
                String wText=axisLabel(w,levelScale.step);
                label.setColor(flowColor);
                canvas.drawText(qText,left-dp(5)-label.measureText(qText),y+dp(3),label);
                label.setColor(levelColor);
                canvas.drawText(wText,right+dp(5),y+dp(3),label);
            }
            drawDualTimeGrid(canvas,left,right,top,bottom,minTime,maxTime);
            drawDualThresholds(canvas,flowScale,left,right,top,bottom);
            drawDualSeries(canvas,flow,flowScale,left,right,top,bottom,minTime,maxTime,flowLine,flowColor);
            drawDualSeries(canvas,level,levelScale,left,right,top,bottom,minTime,maxTime,levelLine,levelColor);

            if(selectedTime!=Long.MIN_VALUE)drawDualSelection(canvas,flowScale,levelScale,left,right,top,bottom,minTime,maxTime,flowColor,levelColor);
        }

        private void drawDualSeries(Canvas canvas,TrendSeries series,HydroMath.AxisScale scale,float left,float right,float top,float bottom,long minTime,long maxTime,Paint paint,int color){
            if(series.values.size()<2)return;
            Path path=new Path();
            boolean started=false;
            for(int i=0;i<series.values.size();i++){
                double value=series.values.get(i);if(!Double.isFinite(value))continue;
                float x=left+(right-left)*(series.times.get(i)-minTime)/(float)(maxTime-minTime);
                float y=(float)(bottom-(value-scale.min)/(scale.max-scale.min)*(bottom-top));
                if(!started){path.moveTo(x,y);started=true;}else path.lineTo(x,y);
            }
            paint.setColor(color);canvas.drawPath(path,paint);
        }

        private void drawDualTimeGrid(Canvas canvas,float left,float right,float top,float bottom,long minTime,long maxTime){
            int intervals=range==RiverRange.HOUR?3:4;
            ZoneId zone=ZoneId.of("Europe/Zurich");
            DateTimeFormatter formatter=range==RiverRange.WEEK?DateTimeFormatter.ofPattern("EE dd.",Locale.GERMAN):DateTimeFormatter.ofPattern("HH:mm",Locale.GERMAN);
            label.setColor(themeText(MUTED));
            for(int i=0;i<=intervals;i++){
                float fraction=i/(float)intervals;float x=left+(right-left)*fraction;
                if(i>0&&i<intervals)canvas.drawLine(x,top,x,bottom,grid);
                long timestamp=minTime+Math.round((maxTime-minTime)*fraction);
                String value=java.time.Instant.ofEpochMilli(timestamp).atZone(zone).format(formatter);
                float width=label.measureText(value);float textX=Math.max(left,Math.min(right-width,x-width/2f));
                canvas.drawText(value,textX,bottom+dp(18),label);
            }
        }

        private void drawDualThresholds(Canvas canvas,HydroMath.AxisScale scale,float left,float right,float top,float bottom){
            double[] values={riverLow(station),riverWarn(station),riverAlarm(station)};
            int[] colors={STATUS_LOW,STATUS_WARN,STATUS_ALARM};
            for(int i=0;i<values.length;i++){
                double value=values[i];if(value<scale.min||value>scale.max)continue;
                float y=(float)(bottom-(value-scale.min)/(scale.max-scale.min)*(bottom-top));
                threshold.setColor(statusTextColor(colors[i]));canvas.drawLine(left,y,right,y,threshold);
            }
        }

        private void drawDualSelection(Canvas canvas,HydroMath.AxisScale flowScale,HydroMath.AxisScale levelScale,float left,float right,float top,float bottom,long minTime,long maxTime,int flowColor,int levelColor){
            int qi=HydroMath.nearestIndex(flow.times,selectedTime),wi=HydroMath.nearestIndex(level.times,selectedTime);
            if(qi<0||wi<0)return;
            float x=left+(right-left)*(selectedTime-minTime)/(float)(maxTime-minTime);
            Paint cross=new Paint(Paint.ANTI_ALIAS_FLAG);cross.setColor(Color.argb(darkMode?150:100,128,145,155));cross.setStrokeWidth(dp(1));canvas.drawLine(x,top,x,bottom,cross);

            double q=flow.values.get(qi),w=level.values.get(wi);
            float qx=left+(right-left)*(flow.times.get(qi)-minTime)/(float)(maxTime-minTime);
            float qy=(float)(bottom-(q-flowScale.min)/(flowScale.max-flowScale.min)*(bottom-top));
            float wx=left+(right-left)*(level.times.get(wi)-minTime)/(float)(maxTime-minTime);
            float wy=(float)(bottom-(w-levelScale.min)/(levelScale.max-levelScale.min)*(bottom-top));
            point.setColor(flowColor);canvas.drawCircle(qx,qy,dp(4.5f),point);
            point.setColor(levelColor);canvas.drawCircle(wx,wy,dp(4.5f),point);

            long timestamp=Math.max(flow.times.get(qi),level.times.get(wi));
            ZonedDateTime time=java.time.Instant.ofEpochMilli(timestamp).atZone(ZoneId.of("Europe/Zurich"));
            DateTimeFormatter formatter=range==RiverRange.WEEK?DateTimeFormatter.ofPattern("EE dd.MM. HH:mm",Locale.GERMAN):DateTimeFormatter.ofPattern("HH:mm",Locale.GERMAN);
            String text=time.format(formatter)+" · "+formatMetric(station,RiverMetric.FLOW,q)+" m³/s · "+formatMetric(station,RiverMetric.LEVEL,w)+" "+metricUnit(station,RiverMetric.LEVEL);
            tooltipText.setColor(darkMode?DARK_TEXT:Color.WHITE);
            float textWidth=tooltipText.measureText(text),textHeight=Math.abs(tooltipText.ascent())+Math.abs(tooltipText.descent());
            float boxWidth=Math.min(right-left,textWidth+dp(16));float boxLeft=Math.max(left,Math.min(right-boxWidth,x-boxWidth/2f));
            RectF box=new RectF(boxLeft,top+dp(4),boxLeft+boxWidth,top+textHeight+dp(14));
            tooltip.setColor(darkMode?DARK_SOFT:NAVY);canvas.drawRoundRect(box,dp(8),dp(8),tooltip);
            canvas.save();canvas.clipRect(box);canvas.drawText(text,box.left+dp(8),box.bottom-dp(6),tooltipText);canvas.restore();
            setContentDescription(text);
        }
    }

'''
if 'private class DualRiverTrendView' not in s:
    marker='    private class RiverTrendView extends View {'
    if marker not in s: raise SystemExit('RiverTrendView marker not found')
    s=s.replace(marker,dual_chart+marker,1)

MAIN.write_text(s,encoding='utf-8')

build=BUILD.read_text(encoding='utf-8')
build=build.replace('versionCode 15','versionCode 16',1)
build=build.replace("versionName '0.9.1'","versionName '0.9.2'",1)
BUILD.write_text(build,encoding='utf-8')

project=PROJECT.read_text(encoding='utf-8')
project=project.replace(
    '- Rhein: zwei Stationskacheln, davon die zweite optional. Jede aktive Kachel zeigt Abfluss und Pegel; Rheinfelden zusätzlich Wassertemperatur. Verläufe für `1h`, `24h` und `7d` werden untereinander dargestellt.',
    '- Rhein: zwei Stationskacheln, davon die zweite optional. Aktuelle Abflüsse stehen kompakt nebeneinander; Rheinfelden zeigt oben zusätzlich die Wassertemperatur. Pro aktiver Station kombiniert ein `1h`/`24h`/`7d`-Diagramm Abfluss und Pegel mit zwei dynamischen Y-Achsen.',
)
PROJECT.write_text(project,encoding='utf-8')

status='''# Status\n\nStand: Testversion `0.9.2` · aktualisiert 2026-09-02.\n\n## Implementiert / im Test\n\n- Rhein-Übersicht: aktuelle Abflüsse der aktiven Stationen kompakt nebeneinander; Status und Wert folgen Niedrig/Gut/Warn/Alarm.\n- Rheinfelden zeigt die aktuelle Wassertemperatur direkt in der oberen Stationskarte; ein eigener Temperaturgraph entfällt.\n- Pro aktiver Station ein gemeinsames Diagramm für Abfluss und Pegel mit zwei dynamischen Y-Achsen; Basel nutzt für Pegel cm, Rheinfelden m ü.M.\n- Zweite Rhein-Kachel bleibt in den Einstellungen ein- und ausblendbar.\n- Live-Aktualisierung baut nur den Live-Bereich neu auf und stellt die Scrollposition zusätzlich nach dem Layout nochmals her.\n- Banking-App wird oben in der Kasse festgelegt. Die Auswahl speichert nur die App und öffnet sie nicht. Erst der Zahlungsbutton übergibt den temporären Swiss QR.\n- Vereinsbeiz bleibt in Trinken, Essen und Feiern gegliedert; Warenkorb steht vor der Auswahl, zeigt Positionen und Total und erlaubt direktes Entfernen.\n- Direkte Swiss-QR-Übergabe wurde mit Yuh real erfolgreich geprüft; weitere Banken bleiben geräteabhängig.\n- Interner Bereich startet standardmäßig in der App-Ansicht und übernimmt Hell-/Dunkelmodus.\n- Android 16 / API 36 als Target.\n\n## Noch zu verifizieren\n\n- Visuelle Prüfung der kombinierten Doppelachsen-Diagramme auf realem Android-Gerät, insbesondere kleine Displays und Dark Mode.\n- Direkte QR-Übergabe mit weiteren Banking-Apps.\n- Updatepfad: 0.9.1 wurde mangels geschützter Signing-Secrets nur als getrenntes Debug-Testpaket gebaut. Für dauerhaft installierbare Updates muss einmalig eine stabile private Test-/Release-Signatur eingerichtet werden; ein wechselnder GitHub-Runner-Debug-Key ist nicht updatefähig.\n\n## Spätere Punkte\n\n- Native Vereinsnews aus WordPress REST/RSS.\n- Optionale Trainingsbenachrichtigungen und Homescreen-Widget.\n- iOS-Implementierung.\n'''
STATUS.write_text(status,encoding='utf-8')

print('Applied Android 0.9.2 river and banking refinements')
