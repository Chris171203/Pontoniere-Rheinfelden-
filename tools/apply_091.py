from __future__ import annotations

import json
from pathlib import Path

MAIN = Path("Android/app/src/main/java/ch/pfvr/internapp/MainActivity.java")
BUILD = Path("Android/app/build.gradle")
MANIFEST = Path("Android/app/src/main/AndroidManifest.xml")
PRICES = Path("Android/app/src/main/assets/vereinsbeiz_prices.json")
PROJECT = Path("PROJECT.md")
STATUS = Path("STATUS.md")


def replace_once(text: str, old: str, new: str, label: str) -> str:
    if old not in text:
        raise SystemExit(f"marker not found for {label}: {old[:140]!r}")
    return text.replace(old, new, 1)


def replace_between(text: str, start: str, end: str, replacement: str, label: str) -> str:
    i = text.find(start)
    if i < 0:
        raise SystemExit(f"start marker not found for {label}: {start!r}")
    j = text.find(end, i)
    if j < 0:
        raise SystemExit(f"end marker not found for {label}: {end!r}")
    return text[:i] + replacement + text[j:]


s = MAIN.read_text(encoding="utf-8")

if 'PREF_RIVER_SLOT2_ENABLED = "river_slot2_enabled"' not in s:
    s = replace_once(
        s,
        '    private static final String PREF_RIVER_SLOT2_METRIC = "river_slot2_metric";\n',
        '    private static final String PREF_RIVER_SLOT2_METRIC = "river_slot2_metric";\n'
        '    private static final String PREF_RIVER_SLOT2_ENABLED = "river_slot2_enabled";\n',
        "second river tile preference",
    )

if "cashQuantityViews" not in s:
    s = replace_once(
        s,
        '    private final Map<String,Integer> cashCart = new LinkedHashMap<>();\n',
        '    private final Map<String,Integer> cashCart = new LinkedHashMap<>();\n'
        '    private final Map<String,TextView> cashQuantityViews = new HashMap<>();\n',
        "cash quantity view map",
    )

if "private ScrollView homeScroll;" not in s:
    s = replace_once(
        s,
        '    private boolean darkMode = false;\n',
        '    private boolean darkMode = false;\n'
        '    private ScrollView homeScroll;\n'
        '    private LinearLayout homeLiveStack;\n',
        "home live view fields",
    )

old_events_callback = '''        refreshEvents(false, () -> {
            if (current == Screen.HOME || current == Screen.EVENTS) navigate(current);
        });'''
new_events_callback = '''        refreshEvents(false, () -> {
            if (current == Screen.HOME) rebuildHomePreservingScroll();
            else if (current == Screen.EVENTS) navigate(Screen.EVENTS);
        });'''
if old_events_callback in s:
    s = s.replace(old_events_callback, new_events_callback, 1)

old_resume = '''    @Override protected void onResume(){
        super.onResume();
        if(prefs!=null){loadCachedEvents();refreshLive(false);if(current==Screen.HOME||current==Screen.EVENTS)navigate(current);}
        if(dataRefreshHandler!=null){dataRefreshHandler.removeCallbacks(dataRefreshTick);dataRefreshHandler.postDelayed(dataRefreshTick,5L*60L*1000L);}
    }'''
new_resume = '''    @Override protected void onResume(){
        super.onResume();
        if(prefs!=null){
            loadCachedEvents();
            refreshLive(false);
            if(current==Screen.HOME)rebuildHomePreservingScroll();
            else if(current==Screen.EVENTS)navigate(Screen.EVENTS);
        }
        if(dataRefreshHandler!=null){dataRefreshHandler.removeCallbacks(dataRefreshTick);dataRefreshHandler.postDelayed(dataRefreshTick,5L*60L*1000L);}
    }'''
if old_resume in s:
    s = s.replace(old_resume, new_resume, 1)

old_nav = "        current = screen; activeWebView = null;\n"
new_nav = "        current = screen; activeWebView = null;\n        if(screen!=Screen.HOME){homeScroll=null;homeLiveStack=null;}\n"
if new_nav not in s:
    s = replace_once(s, old_nav, new_nav, "navigation home references")

old_home = '''    private View home() {
        ScrollView scroll = new ScrollView(this);
        LinearLayout b = body(); scroll.addView(b);'''
new_home = '''    private View home() {
        ScrollView scroll = new ScrollView(this);
        homeScroll=scroll;
        LinearLayout b = body(); scroll.addView(b);'''
if old_home in s:
    s = s.replace(old_home, new_home, 1)

live_block = r'''    private View liveInfoRow(){
        LinearLayout stack=new LinearLayout(this);
        stack.setOrientation(LinearLayout.VERTICAL);
        stack.setPadding(0,0,0,dp(2));
        homeLiveStack=stack;
        populateHomeLiveStack(stack);
        return stack;
    }

    private void populateHomeLiveStack(LinearLayout stack){
        stack.removeAllViews();
        LinearLayout weather=weatherCard();
        stack.addView(weather,margin(-1,-2,0,0,0,10));

        TextView rangeLabel=txt("RHEIN · ZEITRAUM",10,MUTED,true);
        rangeLabel.setPadding(dp(2),dp(2),0,dp(5));
        stack.addView(rangeLabel);
        stack.addView(riverRangeSelector(riverRange()),new LinearLayout.LayoutParams(-1,dp(42)));

        stack.addView(riverCard(1),margin(-1,-2,0,10,0,10));
        if(riverSlotEnabled(2))stack.addView(riverCard(2),margin(-1,-2,0,0,0,4));
    }

    private void refreshHomeLiveViews(){
        if(current!=Screen.HOME||homeLiveStack==null)return;
        final int scrollY=homeScroll==null?0:homeScroll.getScrollY();
        populateHomeLiveStack(homeLiveStack);
        if(homeScroll!=null)homeScroll.post(()->homeScroll.scrollTo(0,scrollY));
    }

    private void rebuildHomePreservingScroll(){
        if(current!=Screen.HOME)return;
        final int scrollY=homeScroll==null?0:homeScroll.getScrollY();
        navigate(Screen.HOME);
        if(homeScroll!=null)homeScroll.post(()->homeScroll.scrollTo(0,scrollY));
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
        RiverRange range=riverRange();
        String[] summary=hydroSummary(station);
        double flow=currentHydroValue(station,"Q");
        RiverStatus status=riverStatus(station,flow);
        int statusColor=statusTextColor(status.bg);

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
        headline.setPadding(0,dp(4),0,dp(2));
        card.addView(headline);

        LinearLayout valueBox=new LinearLayout(this);
        valueBox.setGravity(Gravity.BOTTOM|Gravity.START);
        TextView value=txt(Double.isFinite(flow)?formatMetric(station,RiverMetric.FLOW,flow):"–",34,statusColor,true);
        value.setTextColor(statusColor);
        valueBox.addView(value);
        TextView unit=txt("m³/s",14,statusColor,true);
        unit.setTextColor(statusColor);
        unit.setPadding(dp(5),0,0,dp(5));
        valueBox.addView(unit);
        headline.addView(valueBox,new LinearLayout.LayoutParams(0,dp(54),1f));

        LinearLayout statusArea=new LinearLayout(this);
        statusArea.setGravity(Gravity.CENTER|Gravity.END);
        statusArea.addView(riverStatusPill(status));
        headline.addView(statusArea,new LinearLayout.LayoutParams(0,dp(54),0.9f));

        TextView thresholdHint=txt(String.format(Locale.GERMAN,"Abflussstatus · Niedrig < %.0f · Warn ab %.0f · Alarm ab %.0f m³/s",riverLow(station),riverWarn(station),riverAlarm(station)),10,MUTED,false);
        thresholdHint.setPadding(0,0,0,dp(7));
        card.addView(thresholdHint);

        addRiverChartSection(card,station,RiverMetric.FLOW,range,true);
        addRiverChartSection(card,station,RiverMetric.LEVEL,range,false);
        if(station.supportsTemperature)addRiverChartSection(card,station,RiverMetric.TEMPERATURE,range,false);

        TextView hint=txt("Diagramme berühren, um Einzelwerte anzuzeigen.",10,MUTED,false);
        hint.setGravity(Gravity.CENTER);
        hint.setPadding(0,dp(5),0,0);
        card.addView(hint);

        TextView src=txt(summary[3]+" · "+range.sourceLabel,10,Color.rgb(126,140,150),false);
        src.setPadding(0,dp(9),0,0);
        card.addView(src);
        return card;
    }

    private void addRiverChartSection(LinearLayout card,HydroStation station,RiverMetric metric,RiverRange range,boolean first){
        if(!first){
            View divider=new View(this);
            divider.setBackgroundColor(darkMode?Color.rgb(51,65,74):Color.rgb(226,233,237));
            LinearLayout.LayoutParams dividerParams=new LinearLayout.LayoutParams(-1,dp(1));
            dividerParams.setMargins(0,dp(12),0,dp(12));
            card.addView(divider,dividerParams);
        }

        TrendSeries series=hydroSeries(station,metric.parameter,range);
        HydroMath.Stats stats=HydroMath.stats(series.values);
        double currentValue=displayHydroValue(station,metric,currentHydroValue(station,metric.parameter));
        int valueColor=metric==RiverMetric.FLOW?statusTextColor(riverStatus(station,currentHydroValue(station,"Q")).bg):themeText(TEXT);

        LinearLayout titleRow=new LinearLayout(this);
        titleRow.setGravity(Gravity.CENTER_VERTICAL);
        titleRow.addView(txt(riverMetricTitle(metric),15,TEXT,true),new LinearLayout.LayoutParams(0,-2,1));
        TextView current=txt(Double.isFinite(currentValue)?formatMetric(station,metric,currentValue)+" "+metricUnit(station,metric):"–",14,valueColor,true);
        current.setTextColor(valueColor);
        current.setGravity(Gravity.END);
        titleRow.addView(current,new LinearLayout.LayoutParams(-2,-2));
        card.addView(titleRow);

        if(stats.isValid()&&stats.count>=2){
            String statsText="Ø "+formatMetric(station,metric,stats.mean)
                    +"  ·  "+formatMetric(station,metric,stats.min)+"–"+formatMetric(station,metric,stats.max)
                    +"  ·  "+trendText(stats,metric,station);
            TextView statsView=txt(statsText,11,MUTED,false);
            statsView.setPadding(0,dp(3),0,dp(3));
            card.addView(statsView);
        }

        if(series.values.size()>=2){
            RiverTrendView chart=new RiverTrendView(this,series,metric,range,station);
            LinearLayout.LayoutParams chartParams=new LinearLayout.LayoutParams(-1,dp(metric==RiverMetric.TEMPERATURE?158:176));
            chartParams.setMargins(0,dp(3),0,0);
            card.addView(chart,chartParams);
        }else{
            String message=range==RiverRange.HOUR?"Für die letzte Stunde liegen noch nicht genug Livewerte vor.":"Für diesen Zeitraum liegen noch nicht genug Messwerte vor.";
            TextView missing=txt(message,12,MUTED,false);
            missing.setGravity(Gravity.CENTER);
            missing.setPadding(dp(8),dp(18),dp(8),dp(18));
            missing.setBackground(round(Color.rgb(238,243,246),14));
            LinearLayout.LayoutParams missingParams=new LinearLayout.LayoutParams(-1,-2);
            missingParams.setMargins(0,dp(7),0,0);
            card.addView(missing,missingParams);
        }
    }

    private String riverMetricTitle(RiverMetric metric){return metric==RiverMetric.TEMPERATURE?"Wassertemperatur":metric.label;}

    private boolean riverSlotEnabled(int slot){return slot==1||prefs.getBoolean(PREF_RIVER_SLOT2_ENABLED,true);}

    private HydroStation riverStation(int slot){
        String key=slot==1?PREF_RIVER_SLOT1_STATION:PREF_RIVER_SLOT2_STATION;
        HydroStation fallback=slot==1?HydroStation.BASEL_RHEINHALLE:HydroStation.RHEINFELDEN;
        return HydroStation.from(prefs.getString(key,fallback.id),fallback);
    }

'''
if "private void addRiverChartSection" not in s:
    s = replace_between(
        s,
        "    private View liveInfoRow(){",
        "    private RiverRange riverRange()",
        live_block,
        "stacked river charts",
    )

old_range_refresh = '''                if(current==Screen.HOME)navigate(Screen.HOME);'''
if old_range_refresh in s:
    s = s.replace(old_range_refresh, '                if(current==Screen.HOME)refreshHomeLiveViews();', 1)

old_home_rerender = '                runOnUiThread(()->{if(current==Screen.HOME)navigate(Screen.HOME);});'
if old_home_rerender in s:
    s = s.replace(old_home_rerender, '                runOnUiThread(this::refreshHomeLiveViews);')

old_river_settings = '''        section(b,"Rhein-Anzeige","Zwei frei konfigurierbare Messkacheln auf Home");
        b.addView(riverSlotSettingCard(1),margin(-1,-2,0,0,0,9));
        b.addView(riverSlotSettingCard(2),margin(-1,-2,0,0,0,12));

'''
new_river_settings = '''        section(b,"Rhein-Anzeige","Kachel 1 ist immer sichtbar; Kachel 2 kann ein- oder ausgeblendet werden. Jede Kachel zeigt alle verfügbaren Messwerte.");
        b.addView(riverSlotSettingCard(1),margin(-1,-2,0,0,0,9));
        b.addView(riverSlotSettingCard(2),margin(-1,-2,0,0,0,12));

'''
if old_river_settings in s:
    s = s.replace(old_river_settings, new_river_settings, 1)

slot_block = r'''    private View riverSlotSettingCard(int slot){
        HydroStation station=riverStation(slot);
        boolean enabled=riverSlotEnabled(slot);
        LinearLayout card=card();
        card.setOrientation(LinearLayout.VERTICAL);

        LinearLayout titleRow=new LinearLayout(this);
        titleRow.setGravity(Gravity.CENTER_VERTICAL);
        titleRow.addView(txt("Rhein-Kachel "+slot,15,TEXT,true),new LinearLayout.LayoutParams(0,-2,1));
        int stateColor=enabled?STATUS_GOOD:MUTED;
        TextView state=txt(enabled?"Aktiv":"Aus",11,stateColor,true);
        state.setTextColor(enabled?statusTextColor(STATUS_GOOD):themeText(MUTED));
        state.setGravity(Gravity.CENTER);
        state.setPadding(dp(10),dp(5),dp(10),dp(5));
        state.setBackground(enabled?statusBadge(STATUS_GOOD):round(Color.rgb(238,243,246),12));
        titleRow.addView(state,new LinearLayout.LayoutParams(-2,-2));
        card.addView(titleRow);

        String metrics=station.supportsTemperature?"Abfluss · Pegel · Wassertemperatur":"Abfluss · Pegel";
        TextView current=txt(station.label+" · BAFU "+station.id+"\n"+metrics,13,MUTED,false);
        current.setPadding(0,dp(5),0,dp(10));
        card.addView(current);

        LinearLayout actions=new LinearLayout(this);
        if(slot==2){
            Button toggle=btn(enabled?"Ausblenden":"Einblenden",enabled?Color.rgb(232,240,244):NAVY,enabled?NAVY:Color.WHITE);
            toggle.setOnClickListener(v->{boolean next=!prefs.getBoolean(PREF_RIVER_SLOT2_ENABLED,true);prefs.edit().putBoolean(PREF_RIVER_SLOT2_ENABLED,next).apply();navigate(Screen.SETTINGS);});
            actions.addView(toggle,new LinearLayout.LayoutParams(0,dp(44),1));
        }
        Button configure=btn("Station wählen",Color.rgb(232,240,244),NAVY);
        configure.setOnClickListener(v->configureRiverSlot(slot));
        LinearLayout.LayoutParams configureParams=new LinearLayout.LayoutParams(0,dp(44),slot==2?1:2);
        if(slot==2)configureParams.setMargins(dp(8),0,0,0);
        actions.addView(configure,configureParams);
        card.addView(actions);
        return card;
    }

    private void configureRiverSlot(int slot){
        final HydroStation[] stations={HydroStation.BASEL_RHEINHALLE,HydroStation.RHEINFELDEN};
        String[] labels={
                "Basel, Rheinhalle · Abfluss & Pegel",
                "Rheinfelden · Abfluss, Pegel & Wassertemperatur"
        };
        HydroStation currentStation=riverStation(slot);
        int selected=currentStation==HydroStation.RHEINFELDEN?1:0;
        new AlertDialog.Builder(this,dialogTheme()).setTitle("Rhein-Kachel "+slot)
                .setSingleChoiceItems(labels,selected,(dialog,which)->{
                    String stationKey=slot==1?PREF_RIVER_SLOT1_STATION:PREF_RIVER_SLOT2_STATION;
                    String metricKey=slot==1?PREF_RIVER_SLOT1_METRIC:PREF_RIVER_SLOT2_METRIC;
                    prefs.edit().putString(stationKey,stations[which].id).remove(metricKey).apply();
                    dialog.dismiss();
                    navigate(Screen.SETTINGS);
                }).setNegativeButton("Abbrechen",null).show();
    }

'''
if 'Button configure=btn("Kachel konfigurieren"' in s:
    s = replace_between(
        s,
        "    private View riverSlotSettingCard(int slot){",
        "    private View riverThresholdSettingsCard(HydroStation station)",
        slot_block,
        "river slot settings",
    )

if "cashQuantityViews.clear();" not in s:
    s = replace_once(
        s,
        '''    private View cash() {
        ScrollView scroll=new ScrollView(this);
        LinearLayout body=body();
        scroll.addView(body);
''',
        '''    private View cash() {
        ScrollView scroll=new ScrollView(this);
        LinearLayout body=body();
        cashQuantityViews.clear();
        scroll.addView(body);
''',
        "cash quantity map reset",
    )

cash_start = s.find("    private View cash() {")
catalog_start = s.find("        CashCatalog.Catalog catalog=cashCatalog();", cash_start)
cart_start = s.find('        section(body,"Warenkorb"', cash_start)
free_start = s.find('        section(body,"Freier Betrag"', cash_start)
if min(cash_start, catalog_start, cart_start, free_start) < 0:
    raise SystemExit("cash block markers not found")
if catalog_start < cart_start:
    selection_block = s[catalog_start:cart_start]
    cart_block = s[cart_start:free_start]
    selection_block = selection_block.replace(
        'section(body,"Konsumation zusammenstellen",catalog==null?"Preisliste konnte nicht geladen werden.":"Preise gemäss Vereinsbeiz-Preisliste · Stand "+catalog.validFrom);',
        'section(body,"Auswahl",catalog==null?"Preisliste konnte nicht geladen werden.":"Trinken, Essen und Feiern · Stand "+catalog.validFrom);',
        1,
    )
    s = s[:catalog_start] + cart_block + "\n" + selection_block + s[free_start:]

old_clear_cart = 'TextView clearCart=link("Warenkorb leeren");clearCart.setOnClickListener(v->{cashCart.clear();navigate(Screen.CASH);});cart.addView(clearCart);'
new_clear_cart = 'TextView clearCart=link("Warenkorb leeren");clearCart.setOnClickListener(v->{cashCart.clear();for(TextView quantity:cashQuantityViews.values())quantity.setText("0");updateCashSummary();});cart.addView(clearCart);'
if old_clear_cart in s:
    s = s.replace(old_clear_cart, new_clear_cart, 1)

cash_helpers = r'''    private View cashItemRow(CashCatalog.Item item){
        LinearLayout row=new LinearLayout(this);row.setGravity(Gravity.CENTER_VERTICAL);row.setPadding(0,dp(9),0,dp(9));
        LinearLayout copy=new LinearLayout(this);copy.setOrientation(LinearLayout.VERTICAL);copy.setPadding(0,0,dp(8),0);row.addView(copy,new LinearLayout.LayoutParams(0,-2,1));
        copy.addView(txt(item.name,14,TEXT,true));
        String detail=(item.variant==null||item.variant.isBlank()?"":item.variant+" · ")+formatCashPrice(item.price)+(item.deposit?" · Depot":"");copy.addView(txt(detail,12,item.deposit?WATER:MUTED,false));
        Button minus=btn("−",Color.rgb(232,240,244),NAVY);row.addView(minus,new LinearLayout.LayoutParams(dp(40),dp(40)));
        TextView quantity=txt(String.valueOf(cashCart.getOrDefault(item.id,0)),16,TEXT,true);quantity.setGravity(Gravity.CENTER);row.addView(quantity,new LinearLayout.LayoutParams(dp(38),dp(40)));
        cashQuantityViews.put(item.id,quantity);
        Button plus=btn("+",NAVY,Color.WHITE);row.addView(plus,new LinearLayout.LayoutParams(dp(40),dp(40)));
        minus.setOnClickListener(v->{int next=Math.max(0,cashCart.getOrDefault(item.id,0)-1);setCashQuantity(item.id,next);updateCashSummary();});
        plus.setOnClickListener(v->{int next=Math.min(99,cashCart.getOrDefault(item.id,0)+1);setCashQuantity(item.id,next);updateCashSummary();});
        return row;
    }

    private void setCashQuantity(String itemId,int quantity){
        if(quantity<=0)cashCart.remove(itemId);else cashCart.put(itemId,quantity);
        TextView view=cashQuantityViews.get(itemId);
        if(view!=null)view.setText(String.valueOf(Math.max(0,quantity)));
    }

    private View cashSummaryRow(CashCatalog.Item item,int quantity){
        LinearLayout row=new LinearLayout(this);
        row.setGravity(Gravity.CENTER_VERTICAL);
        row.setPadding(0,dp(4),0,dp(4));
        LinearLayout copy=new LinearLayout(this);
        copy.setOrientation(LinearLayout.VERTICAL);
        copy.setPadding(0,0,dp(8),0);
        copy.addView(txt(quantity+"× "+item.displayName(),13,TEXT,true));
        copy.addView(txt(formatCashPrice(item.price*quantity),12,item.deposit?WATER:MUTED,false));
        row.addView(copy,new LinearLayout.LayoutParams(0,-2,1));
        Button remove=btn("Entfernen",Color.rgb(232,240,244),NAVY);
        remove.setOnClickListener(v->{setCashQuantity(item.id,0);updateCashSummary();});
        row.addView(remove,new LinearLayout.LayoutParams(dp(94),dp(38)));
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
                cashSummaryContainer.addView(cashSummaryRow(item,quantity));
            }
        }
        if(!any)cashSummaryContainer.addView(txt("Noch keine Artikel ausgewählt.",13,MUTED,false));
        cashTotalView.setText("Total "+formatCashPrice(catalog.total(cashCart)));
    }

'''
if "private View cashSummaryRow" not in s:
    s = replace_between(
        s,
        "    private View cashItemRow(CashCatalog.Item item){",
        "    private String formatCashPrice(double value)",
        cash_helpers,
        "cash summary with removal",
    )

# App view is the default; an explicitly chosen Original mode remains respected.
s = s.replace("getBoolean(PREF_INTERNAL_APP_VIEW,false)", "getBoolean(PREF_INTERNAL_APP_VIEW,true)")

s = s.replace(
    "LinearLayout root=new LinearLayout(this); root.setOrientation(LinearLayout.VERTICAL); root.setBackgroundColor(Color.WHITE);",
    "LinearLayout root=new LinearLayout(this); root.setOrientation(LinearLayout.VERTICAL); root.setBackgroundColor(themeBg(Color.WHITE));",
    1,
)
s = s.replace(
    "WebView web=web(false); activeWebView=web; web.setBackgroundColor(Color.WHITE);",
    "WebView web=web(false); activeWebView=web; web.setBackgroundColor(themeBg(Color.WHITE));",
    1,
)

error_block = r'''    private void showInternalLoadError(WebView v,String message){
        String safe=message.replace("&","&amp;").replace("<","&lt;").replace(">","&gt;");
        String bg=darkMode?"#11171C":"#FFFFFF",text=darkMode?"#ECF1F4":"#15232E",muted=darkMode?"#A0B0BA":"#60717E";
        String html="<html><head><meta name='viewport' content='width=device-width,initial-scale=1'><meta name='color-scheme' content='"+(darkMode?"dark":"light")+"'></head><body style='font-family:sans-serif;background:"+bg+";color:"+text+";padding:24px'><h2>Interner Bereich konnte nicht geladen werden</h2><p>"+safe+"</p><p style='color:"+muted+"'>Prüfe den persönlichen Link unter Einstellungen oder tippe oben auf Neu laden.</p></body></html>";
        v.loadDataWithBaseURL("https://intern.pfvr.ch/",html,"text/html","UTF-8",null);
    }

'''
if "meta name='color-scheme'" not in s:
    s = replace_between(
        s,
        "    private void showInternalLoadError(WebView v,String message){",
        "    private void internalSkin(WebView v)",
        error_block,
        "dark internal error view",
    )

# Flow line follows the current threshold status; threshold lines remain independently visible.
old_chart_color = "            int actual=themeText(metric.color);"
new_chart_color = "            int actual=metric==RiverMetric.FLOW?statusTextColor(riverStatus(station,currentHydroValue(station,\"Q\")).bg):themeText(metric.color);"
if old_chart_color in s:
    s = s.replace(old_chart_color, new_chart_color, 1)

MAIN.write_text(s, encoding="utf-8")

build = BUILD.read_text(encoding="utf-8")
build = build.replace("versionCode 14", "versionCode 15", 1)
build = build.replace("versionName '0.9.0'", "versionName '0.9.1'", 1)
if "manifestPlaceholders = [appLabel:" not in build:
    build = build.replace(
        "        versionName '0.9.1'\n",
        "        versionName '0.9.1'\n        manifestPlaceholders = [appLabel: 'PFVR Rheinfelden']\n",
        1,
    )
if "applicationIdSuffix '.test'" not in build:
    build = build.replace(
        "    buildTypes {\n        release {",
        "    buildTypes {\n        debug {\n            applicationIdSuffix '.test'\n            manifestPlaceholders = [appLabel: 'PFVR Rheinfelden Test']\n        }\n        release {",
        1,
    )
BUILD.write_text(build, encoding="utf-8")

manifest = MANIFEST.read_text(encoding="utf-8")
manifest = manifest.replace('android:label="PFVR Rheinfelden"', 'android:label="${appLabel}"', 1)
MANIFEST.write_text(manifest, encoding="utf-8")

catalog = {
    "schemaVersion": 1,
    "currency": "CHF",
    "title": "Preisliste Vereinsbeiz Pontoniere Rheinfelden",
    "validFrom": "2026",
    "sourceNote": "Preisliste Vereinsbeiz, Stand 2026",
    "categories": [
        {
            "id": "drinks",
            "label": "Trinken",
            "items": [
                {"id": "soft_5dl", "name": "Mineral / Softdrink", "variant": "ca. 5 dl", "price": 3.0},
                {"id": "soft_3dl", "name": "Mineral / Softdrink", "variant": "ca. 3 dl", "price": 2.0},
                {"id": "beer_5dl", "name": "Bier / Somersby / EVE / Smirnoff etc.", "variant": "ca. 5 dl", "price": 4.5},
                {"id": "beer_3dl", "name": "Bier / Somersby / EVE / Smirnoff etc.", "variant": "ca. 3 dl", "price": 3.0},
                {"id": "spirits_1dl", "name": "Spirituosen (≥ 15 % Vol.)", "variant": "pro dl", "price": 2.0},
                {"id": "wine_1dl", "name": "Wein", "variant": "pro dl", "price": 1.0},
            ],
        },
        {
            "id": "food",
            "label": "Essen",
            "items": [
                {"id": "food_jp", "name": "Essen normal", "variant": "JP bis 21", "price": 5.0},
                {"id": "food_other", "name": "Essen normal", "variant": "Andere", "price": 8.0},
                {"id": "sides_jp", "name": "Nur Beilagen / ohne Fleisch", "variant": "JP bis 21", "price": 3.0},
                {"id": "sides_other", "name": "Nur Beilagen / ohne Fleisch", "variant": "Andere", "price": 5.0},
            ],
        },
        {
            "id": "celebrations",
            "label": "Feiern",
            "items": [
                {"id": "keg_20l", "name": "Bierfass", "variant": "20 l", "price": 80.0},
                {"id": "keg_deposit", "name": "Depot Bierfass", "variant": "optional", "price": 50.0, "deposit": True},
                {"id": "crate_20x5dl", "name": "Bierharasse", "variant": "20 × 5 dl", "price": 45.0},
                {"id": "crate_deposit", "name": "Depot Bierharasse", "variant": "optional", "price": 15.0, "deposit": True},
            ],
        },
    ],
}
PRICES.write_text(json.dumps(catalog, ensure_ascii=False, indent=2) + "\n", encoding="utf-8")

PROJECT.write_text(
    """# Projekt

## Ziel

Mobile PFVR-App mit schnellem Zugriff auf Training, Rhein- und Wetterdaten, Vereinstermine, interne An-/Abmeldung und Vereinsbeiz-Zahlung.

## Kernfunktionen

- Home mit nächstem Training und Wetter für den tatsächlichen Zeitraum.
  - Bevorzugt wird ein passender Termin aus dem öffentlichen Vereinskalender.
  - Der saisonale Trainingsplan dient als Fallback, wenn kein Kalendereintrag vorhanden ist.
- Rhein: zwei Stationskacheln, davon die zweite optional. Jede aktive Kachel zeigt Abfluss und Pegel; Rheinfelden zusätzlich Wassertemperatur. Verläufe für `1h`, `24h` und `7d` werden untereinander dargestellt.
- Termine aus dem öffentlichen PFVR-Google-Kalender mit persistentem Cache, Detailansicht, Teilen, Route und Übergabe an die persönliche Kalender-App.
- Verein und Kontakt inklusive News-Archiv-Verlinkung.
- Interner Bereich über persönlichen `intern.pfvr.ch`-Link; Konfiguration nur in den Einstellungen und nur lokal gespeichert. App-Ansicht ist Standard und folgt dem nativen Hell-/Dunkelmodus.
- Vereinsbeiz: Warenkorb in den Gruppen Trinken, Essen und Feiern, freier Betrag, Swiss-QR-Zahlung, direkte Android-Übergabe an kompatible Banking-Apps und TWINT-Zahlungsweg.
- Darstellung: System / Hell / Dunkel.

## Datenquellen

- PFVR-Website / WordPress-REST/RSS für öffentliche Vereinsinhalte.
- Öffentlicher PFVR-Google-Kalender.
- Wetter: MeteoSwiss ICON via Open-Meteo.
- Hydrologie: BAFU Stationen 2091 Rhein–Rheinfelden und 2289 Basel–Rheinhalle.

## Qualitätsziele

- Letzten erfolgreichen Datenstand lokal anzeigen und Datenalter sichtbar machen.
- Live-Aktualisierungen dürfen die aktuelle Scrollposition nicht verändern.
- Keine persönlichen Zugangsparameter, Schlüssel oder Personen-IDs im Repository oder in Diagnosedaten.
- Test-APK reproduzierbar aus dem eingecheckten Quellstand bauen; keine verdeckten Build-Patches.
- Dauerhafte Android-Updates setzen eine unveränderte Paket-ID und dieselbe geschützte Signatur voraus.
- Android und spätere iOS-App sollen dieselben fachlichen Kernfunktionen bieten.

## Release

Entwicklung/Test: `0.x.y`. Erster offizieller Release: `1.0.0`.
""",
    encoding="utf-8",
)

STATUS.write_text(
    """# Status

Stand: Testversion `0.9.1` · aktualisiert 2026-09-02.

## Implementiert / im Test

- Zwei BAFU-Stationskacheln auf Home; Kachel 2 ist in den Einstellungen ein- und ausblendbar.
- Jede aktive Stationskachel zeigt die verfügbaren Diagramme untereinander: Abfluss, Pegel und bei Rheinfelden Wassertemperatur.
- Abflusswert, Status und Abflusskurve verwenden die aktuelle Grenzfarbe; Grenzlinien bleiben auf der Abflussskala sichtbar.
- Live-Aktualisierung ersetzt nur den Live-Bereich und stellt die bisherige Scrollposition wieder her.
- Vereinsbeiz in Trinken, Essen und Feiern gegliedert; Warenkorb steht vor der Auswahl, zeigt Positionen und Total und erlaubt das direkte Entfernen einzelner Positionen.
- Direkte Swiss-QR-Übergabe als temporäres PNG wurde mit Yuh auf einem realen Android-Gerät erfolgreich geprüft.
- Interner Bereich startet standardmäßig in der App-Ansicht und übernimmt den nativen Hell-/Dunkelmodus.
- Persönlicher Intern-Link ausschließlich lokal gespeichert; CI weist bekannte Zugangsmuster zurück.
- Der Build erzeugt immer ein getrenntes Debug-Testpaket und zusätzlich eine release-signierte APK, sofern die geschützten Android-Signing-Secrets im Repository eingerichtet sind.
- Android 16 / API 36 als Target.

## Noch zu verifizieren

- Visuelle Prüfung auf realem Android-Gerät: drei gestapelte Rheinfelden-Diagramme, zwei Basel-Diagramme, Hell/Dunkel und zweite Kachel aus/ein.
- Update von `0.9.1` auf die nächste Testversion mit unveränderter Paket-ID und derselben geschützten Signatur. Der Wechsel von bisherigen wechselnden Debug-Signaturen erfordert einmalig eine Neuinstallation.
- Direkte Swiss-QR-Übergabe mit weiteren Banking-Apps; Android-Intent-Unterstützung allein garantiert noch keine QR-Auswertung durch die Bank.

## Spätere Punkte

- Native Vereinsnews aus WordPress REST/RSS.
- Optionale Trainingsbenachrichtigungen und Homescreen-Widget.
- iOS-Implementierung.
""",
    encoding="utf-8",
)

print("Applied Android 0.9.1 feedback changes")
