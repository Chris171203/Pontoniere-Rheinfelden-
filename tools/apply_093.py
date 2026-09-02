from pathlib import Path
import re

MAIN = Path("Android/app/src/main/java/ch/pfvr/internapp/MainActivity.java")
BUILD = Path("Android/app/build.gradle")
PROJECT = Path("PROJECT.md")
STATUS = Path("STATUS.md")


def replace_between(text: str, start: str, end: str, replacement: str, label: str) -> str:
    begin = text.find(start)
    if begin < 0:
        raise SystemExit(f"start marker not found for {label}: {start!r}")
    finish = text.find(end, begin)
    if finish < 0:
        raise SystemExit(f"end marker not found for {label}: {end!r}")
    return text[:begin] + replacement + text[finish:]


def replace_once(text: str, old: str, new: str, label: str) -> str:
    count = text.count(old)
    if count != 1:
        raise SystemExit(f"expected one {label} marker, found {count}")
    return text.replace(old, new, 1)


source = MAIN.read_text(encoding="utf-8")
build = BUILD.read_text(encoding="utf-8")

already_applied = (
    "versionName '0.9.3'" in build
    and "private enum SettingsTab" in source
    and "private View cashBankStatusCard()" in source
    and "private class RiverTrendView extends View" not in source
)
if already_applied:
    print("Android 0.9.3 cleanup already applied.")
    raise SystemExit(0)

# Remove preferences and fields left behind by earlier generic app launchers and
# the superseded single-series river presentation.
for obsolete in (
    '    private static final String PREF_TWINT_PACKAGE = "twint_package";\n',
    '    private static final String PREF_TWINT_LABEL = "twint_label";\n',
    '    private static final String PREF_RIVER_METRIC = "river_metric";\n',
    '    private static final String PREF_RIVER_SLOT1_METRIC = "river_slot1_metric";\n',
    '    private static final String PREF_RIVER_SLOT2_METRIC = "river_slot2_metric";\n',
    '    private static final float DEFAULT_RIVER_LOW = 400f;\n',
    '    private static final float DEFAULT_RIVER_WARN = 2500f;\n',
    '    private static final float DEFAULT_RIVER_ALARM = 3600f;\n',
    '    private Button bankButton;\n',
):
    source = source.replace(obsolete, "")

screen_enum = '    private enum Screen { HOME, EVENTS, CASH, CLUB, SETTINGS, INTERNAL }\n'
settings_enum = '''    private enum Screen { HOME, EVENTS, CASH, CLUB, SETTINGS, INTERNAL }

    private enum SettingsTab {
        GENERAL("Allgemein"),
        RIVER("Rhein"),
        PAYMENT("Zahlung");

        final String label;

        SettingsTab(String label) {
            this.label = label;
        }
    }
'''
source = replace_once(source, screen_enum, settings_enum, "screen enum")

source = replace_once(
    source,
    "    private boolean darkMode = false;\n",
    "    private boolean darkMode = false;\n    private SettingsTab settingsTab = SettingsTab.GENERAL;\n",
    "settings tab field",
)

source = source.replace(
    '''        static RiverMetric from(String value){
            for(RiverMetric metric:values())if(metric.parameter.equals(value))return metric;
            return FLOW;
        }
''',
    "",
)

summary_block = r'''    private View riverSummaryRow(){
        LinearLayout row=new LinearLayout(this);
        row.setGravity(Gravity.TOP);
        row.setBaselineAligned(false);

        LinearLayout first=riverSummaryCard(1);
        row.addView(first,new LinearLayout.LayoutParams(0,-2,1));

        if(riverSlotEnabled(2)){
            LinearLayout second=riverSummaryCard(2);
            LinearLayout.LayoutParams secondParams=new LinearLayout.LayoutParams(0,-2,1);
            secondParams.setMargins(dp(8),0,0,0);
            row.addView(second,secondParams);
            row.post(()->equalizeSummaryCardHeights(first,second));
        }
        return row;
    }

    private void equalizeSummaryCardHeights(View first,View second){
        int height=Math.max(first.getMeasuredHeight(),second.getMeasuredHeight());
        if(height<=0)return;
        setViewHeight(first,height);
        setViewHeight(second,height);
    }

    private void setViewHeight(View view,int height){
        ViewGroup.LayoutParams params=view.getLayoutParams();
        if(params==null||params.height==height)return;
        params.height=height;
        view.setLayoutParams(params);
    }

'''
source = replace_between(
    source,
    "    private View riverSummaryRow(){",
    "    private LinearLayout riverSummaryCard(int slot){",
    summary_block,
    "equal river summary cards",
)

# Remove selectors and helpers from the old single-metric chart implementation.
source = replace_between(
    source,
    "    private String riverMetricTitle(RiverMetric metric)",
    "    private boolean riverSlotEnabled(int slot)",
    "",
    "old river metric helpers",
)
source = replace_between(
    source,
    "    private View riverMetricSelector(RiverMetric selected){",
    "    private LinearLayout segmentedBackground(){",
    "",
    "old river metric selector",
)
source = replace_between(
    source,
    "    private View riverStatusPill(RiverStatus status){",
    "    private GradientDrawable statusDot(int color)",
    "",
    "old river status pill",
)
source = replace_between(
    source,
    "    private View riverMetrics(HydroMath.Stats stats,RiverRange range,RiverMetric metric,HydroStation station){",
    "    private String formatMetric(RiverMetric metric,double value){",
    "",
    "old river metric summary helpers",
)

# The station-specific variant is still used; only the old convenience wrapper is not.
source = source.replace(
    "    private View thresholdGrid(){return thresholdGrid(HydroStation.RHEINFELDEN);}\n\n",
    "",
)

# Remove obsolete wrappers that only served the old fixed Rheinfelden card.
for obsolete in (
    '    private double currentHydroValue(String parameter){return currentHydroValue(HydroStation.RHEINFELDEN,parameter);}\n\n',
    '    private float riverLow(){return riverLow(HydroStation.RHEINFELDEN);}\n',
    '    private float riverWarn(){return riverWarn(HydroStation.RHEINFELDEN);}\n',
    '    private float riverAlarm(){return riverAlarm(HydroStation.RHEINFELDEN);}\n\n',
    '    private RiverStatus riverStatus(double flow){return riverStatus(HydroStation.RHEINFELDEN,flow);}\n',
    '    private String[] hydroSummary(){return hydroSummary(HydroStation.RHEINFELDEN);}\n',
    '    private RiverMetric riverMetric(){return RiverMetric.from(prefs.getString(PREF_RIVER_METRIC,RiverMetric.FLOW.parameter));}\n\n',
    '    private TrendSeries hydroSeries(String parameter,RiverRange range){return hydroSeries(HydroStation.RHEINFELDEN,parameter,range);}\n',
    '    private void editRiverThresholds(){editRiverThresholds(HydroStation.RHEINFELDEN);}\n',
):
    source = source.replace(obsolete, "")

settings_block = r'''    private View settings(){
        ScrollView scroll=new ScrollView(this);
        LinearLayout body=body();
        scroll.addView(body);

        TextView intro=txt("Einstellungen nach Bereich",13,MUTED,false);
        intro.setPadding(dp(2),0,0,dp(9));
        body.addView(intro);
        body.addView(settingsTabSelector(),new LinearLayout.LayoutParams(-1,dp(44)));

        View divider=new View(this);
        body.addView(divider,new LinearLayout.LayoutParams(-1,dp(12)));

        switch(settingsTab){
            case RIVER:
                addRiverSettings(body);
                break;
            case PAYMENT:
                addPaymentSettings(body);
                break;
            default:
                addGeneralSettings(body);
                break;
        }
        return scroll;
    }

    private View settingsTabSelector(){
        LinearLayout tabs=segmentedBackground();
        for(SettingsTab tab:SettingsTab.values()){
            TextView option=segmentOption(tab.label,tab==settingsTab);
            option.setContentDescription("Einstellungen "+tab.label);
            option.setOnClickListener(v->{
                settingsTab=tab;
                navigate(Screen.SETTINGS);
            });
            tabs.addView(option,segmentParams(tabs));
        }
        return tabs;
    }

    private void addGeneralSettings(LinearLayout body){
        section(body,"Darstellung","Gilt für die native App-Oberfläche");
        LinearLayout theme=card();
        theme.setOrientation(LinearLayout.VERTICAL);
        body.addView(theme,margin(-1,-2,0,0,0,12));
        theme.addView(txt("Farbschema",16,TEXT,true));
        TextView currentTheme=txt(themeLabel(),13,MUTED,false);
        currentTheme.setPadding(0,dp(4),0,dp(10));
        theme.addView(currentTheme);
        Button chooseTheme=btn("System / Hell / Dunkel",Color.rgb(232,240,244),NAVY);
        chooseTheme.setOnClickListener(v->chooseTheme());
        theme.addView(chooseTheme,new LinearLayout.LayoutParams(-1,dp(44)));

        section(body,"Persönlicher Zugang","Nur lokal auf diesem Gerät gespeichert");
        LinearLayout access=card();
        access.setOrientation(LinearLayout.VERTICAL);
        body.addView(access,margin(-1,-2,0,0,0,12));
        access.addView(txt("Interner PFVR-Link",16,TEXT,true));
        String internal=prefs.getString(PREF_INTERNAL_URL,"");
        TextView status=txt(validInternal(internal)?"intern.pfvr.ch · eingerichtet":"Noch nicht eingerichtet",13,validInternal(internal)?WATER:MUTED,false);
        status.setPadding(0,dp(4),0,dp(10));
        access.addView(status);
        Button edit=btn(validInternal(internal)?"Link ändern":"Link einrichten",NAVY,Color.WHITE);
        edit.setOnClickListener(v->editInternalSetting());
        access.addView(edit,new LinearLayout.LayoutParams(-1,dp(46)));

        section(body,"Daten","Kalender, Training-Wetter und Rhein-Messwerte");
        LinearLayout data=card();
        data.setOrientation(LinearLayout.VERTICAL);
        body.addView(data,margin(-1,-2,0,0,0,12));
        data.addView(txt("Lokaler Cache",16,TEXT,true));
        TextView description=txt("Beim Start wird zuerst der letzte erfolgreiche Stand angezeigt und anschließend im Hintergrund aktualisiert.",13,MUTED,false);
        description.setPadding(0,dp(4),0,dp(10));
        data.addView(description);
        data.addView(dataFreshnessRow(),margin(-1,-2,0,0,0,10));

        Button reload=btn("Alle Daten aktualisieren",Color.rgb(232,240,244),NAVY);
        reload.setOnClickListener(v->{
            refreshEvents(true,()->{});
            refreshLive(true);
            Toast.makeText(this,"Aktualisierung gestartet.",Toast.LENGTH_SHORT).show();
        });
        data.addView(reload,new LinearLayout.LayoutParams(-1,dp(44)));

        Button clear=btn("Daten-Cache leeren",Color.rgb(232,240,244),NAVY);
        clear.setOnClickListener(v->clearDataCache());
        LinearLayout.LayoutParams clearParams=new LinearLayout.LayoutParams(-1,dp(44));
        clearParams.setMargins(0,dp(8),0,0);
        data.addView(clear,clearParams);

        boolean backgroundOn=prefs.getBoolean(PREF_BACKGROUND_REFRESH,true);
        Button background=btn("Hintergrundaktualisierung: "+(backgroundOn?"Ein":"Aus"),Color.rgb(232,240,244),NAVY);
        background.setOnClickListener(v->{
            boolean next=!prefs.getBoolean(PREF_BACKGROUND_REFRESH,true);
            prefs.edit().putBoolean(PREF_BACKGROUND_REFRESH,next).apply();
            scheduleBackgroundRefresh();
            background.setText("Hintergrundaktualisierung: "+(next?"Ein":"Aus"));
        });
        LinearLayout.LayoutParams backgroundParams=new LinearLayout.LayoutParams(-1,dp(44));
        backgroundParams.setMargins(0,dp(8),0,0);
        data.addView(background,backgroundParams);

        TextView autoInfo=txt("Live-Daten werden bei geöffneter App regelmäßig geprüft. Im Hintergrund aktualisiert Android bei verfügbarer Verbindung best effort; Energiesparmodi können die Ausführung verzögern.",11,MUTED,false);
        autoInfo.setPadding(0,dp(8),0,0);
        data.addView(autoInfo);

        section(body,"App",null);
        LinearLayout about=card();
        about.setOrientation(LinearLayout.VERTICAL);
        body.addView(about,margin(-1,-2,0,0,0,8));
        about.addView(txt("PFVR Rheinfelden",16,TEXT,true));
        about.addView(txt("Testversion "+BuildConfig.VERSION_NAME+" · 1.0.0 bleibt für den ersten offiziellen Release reserviert.",13,MUTED,false));
    }

    private void addRiverSettings(LinearLayout body){
        section(body,"Rhein-Anzeige","Kachel 1 ist immer sichtbar; Kachel 2 kann ein- oder ausgeblendet werden.");
        body.addView(riverSlotSettingCard(1),margin(-1,-2,0,0,0,9));
        body.addView(riverSlotSettingCard(2),margin(-1,-2,0,0,0,12));

        section(body,"Rhein-Grenzwerte","Status basiert auf dem Abfluss der jeweiligen BAFU-Station");
        body.addView(riverThresholdSettingsCard(HydroStation.BASEL_RHEINHALLE),margin(-1,-2,0,0,0,9));
        body.addView(riverThresholdSettingsCard(HydroStation.RHEINFELDEN),margin(-1,-2,0,0,0,12));
    }

    private void addPaymentSettings(LinearLayout body){
        section(body,"Banking-App","Für die direkte Übergabe des Swiss QR");
        body.addView(bankChoiceSettingsCard(),margin(-1,-2,0,0,0,12));

        LinearLayout info=card();
        info.setOrientation(LinearLayout.VERTICAL);
        body.addView(info,margin(-1,-2,0,0,0,8));
        info.addView(txt("So funktioniert die Direktzahlung",16,TEXT,true));
        TextView explanation=txt("Die App erzeugt beim Bezahlen einen Swiss QR als temporäres Bild und übergibt ihn an die gewählte Banking-App. Die Auswahl allein öffnet keine andere App. Ob der Bildimport verarbeitet wird, hängt von der Banking-App ab.",13,MUTED,false);
        explanation.setPadding(0,dp(5),0,0);
        info.addView(explanation);
    }

    private void openPaymentSettings(){
        settingsTab=SettingsTab.PAYMENT;
        navigate(Screen.SETTINGS);
    }

'''
source = replace_between(
    source,
    "    private View settings(){",
    "    private View riverSlotSettingCard(int slot){",
    settings_block,
    "tabbed settings",
)

source = source.replace(
    '''                    String stationKey=slot==1?PREF_RIVER_SLOT1_STATION:PREF_RIVER_SLOT2_STATION;
                    String metricKey=slot==1?PREF_RIVER_SLOT1_METRIC:PREF_RIVER_SLOT2_METRIC;
                    prefs.edit().putString(stationKey,stations[which].id).remove(metricKey).apply();
''',
    '''                    String stationKey=slot==1?PREF_RIVER_SLOT1_STATION:PREF_RIVER_SLOT2_STATION;
                    prefs.edit().putString(stationKey,stations[which].id).apply();
''',
)

cash_bank_section = '''        section(body,"Banking-App","Einmal festlegen; geöffnet wird sie erst beim Bezahlen.");
        body.addView(bankChoiceCard(),margin(-1,-2,0,0,0,12));

'''
source = replace_once(
    source,
    cash_bank_section,
    '''        section(body,"Zahlungsweg","Banking-App wird zentral in den Einstellungen verwaltet");
        body.addView(cashBankStatusCard(),margin(-1,-2,0,0,0,12));

''',
    "cash banking section",
)

old_cart_listener = '        Button cartBank=btn(preferredBankPaymentLabel(),NAVY,Color.WHITE);cartBank.setOnClickListener(v->{EditText input=cartAmountInput();if(input!=null)payWithPreferredBank(input);});cart.addView(cartBank,new LinearLayout.LayoutParams(-1,dp(48)));\n'
new_cart_listener = '''        Button cartBank=btn(preferredBankPaymentLabel(),NAVY,Color.WHITE);
        cartBank.setOnClickListener(v->{
            if(!hasPreferredBank()){
                openPaymentSettings();
                return;
            }
            EditText input=cartAmountInput();
            if(input!=null)payWithPreferredBank(input);
        });
        cart.addView(cartBank,new LinearLayout.LayoutParams(-1,dp(48)));
'''
source = replace_once(source, old_cart_listener, new_cart_listener, "cart bank action")

bank_block = r'''    private View cashBankStatusCard(){
        LinearLayout card=card();
        card.setOrientation(LinearLayout.VERTICAL);

        boolean selected=hasPreferredBank();
        String label=selectedBankLabel();

        LinearLayout titleRow=new LinearLayout(this);
        titleRow.setGravity(Gravity.CENTER_VERTICAL);
        titleRow.addView(txt(selected?label:"Keine Banking-App gewählt",16,TEXT,true),new LinearLayout.LayoutParams(0,-2,1));

        int stateColor=selected?STATUS_GOOD:STATUS_WARN;
        TextView state=txt(selected?"Bereit":"Einrichten",10,stateColor,true);
        state.setTextColor(statusTextColor(stateColor));
        state.setGravity(Gravity.CENTER);
        state.setPadding(dp(9),dp(4),dp(9),dp(4));
        state.setBackground(statusBadge(stateColor));
        titleRow.addView(state,new LinearLayout.LayoutParams(-2,-2));
        card.addView(titleRow);

        TextView info=txt(
                selected
                        ?"Diese App wird beim Bezahlen für die direkte Swiss-QR-Übergabe verwendet."
                        :"Für Direktzahlungen zuerst unter Einstellungen → Zahlung eine Banking-App festlegen.",
                12,
                selected?MUTED:statusTextColor(STATUS_WARN),
                false
        );
        info.setTextColor(selected?themeText(MUTED):statusTextColor(STATUS_WARN));
        info.setPadding(0,dp(5),0,dp(10));
        card.addView(info);

        Button manage=btn(selected?"In Einstellungen ändern":"Banking-App auswählen",selected?Color.rgb(232,240,244):NAVY,selected?NAVY:Color.WHITE);
        manage.setOnClickListener(v->openPaymentSettings());
        card.addView(manage,new LinearLayout.LayoutParams(-1,dp(44)));
        return card;
    }

    private View bankChoiceSettingsCard(){
        LinearLayout card=card();
        card.setOrientation(LinearLayout.VERTICAL);

        boolean selected=hasPreferredBank();
        String label=selectedBankLabel();
        card.addView(txt(selected?label:"Noch keine Banking-App festgelegt",16,TEXT,true));

        TextView info=txt(
                selected
                        ?"Wird für Zahlungen aus Warenkorb und freiem Betrag verwendet."
                        :"Es werden nur installierte Apps angeboten, die PNG-Bilder über Androids Teilen-Funktion annehmen und als Banking-App erkannt werden.",
                12,
                MUTED,
                false
        );
        info.setPadding(0,dp(4),0,dp(10));
        card.addView(info);

        LinearLayout actions=new LinearLayout(this);
        Button choose=btn(selected?"Ändern":"Bank auswählen",selected?Color.rgb(232,240,244):NAVY,selected?NAVY:Color.WHITE);
        choose.setOnClickListener(v->chooseBankingApp());
        actions.addView(choose,new LinearLayout.LayoutParams(0,dp(44),1));

        if(selected){
            Button clear=btn("Entfernen",Color.rgb(232,240,244),NAVY);
            clear.setOnClickListener(v->{
                prefs.edit().remove(PREF_BANK_PACKAGE).remove(PREF_BANK_LABEL).apply();
                navigate(Screen.SETTINGS);
            });
            LinearLayout.LayoutParams clearParams=new LinearLayout.LayoutParams(0,dp(44),1);
            clearParams.setMargins(dp(8),0,0,0);
            actions.addView(clear,clearParams);
        }
        card.addView(actions);
        return card;
    }

    private boolean hasPreferredBank(){
        return !prefs.getString(PREF_BANK_PACKAGE,"").trim().isEmpty()
                && !prefs.getString(PREF_BANK_LABEL,"").trim().isEmpty();
    }

    private String selectedBankLabel(){
        return prefs.getString(PREF_BANK_LABEL,"").trim();
    }

    private String preferredBankPaymentLabel(){
        String label=selectedBankLabel();
        return label.isEmpty()?"Banking-App festlegen":"Mit "+label+" bezahlen";
    }

    private void payWithPreferredBank(EditText amountInput){
        if(!hasPreferredBank()){
            Toast.makeText(this,"Bitte unter Einstellungen → Zahlung eine Banking-App festlegen.",Toast.LENGTH_LONG).show();
            openPaymentSettings();
            return;
        }
        sharePaymentQr(amountInput);
    }

'''
source = replace_between(
    source,
    "    private View bankChoiceCard(){",
    "    private CashCatalog.Catalog cashCatalog(){",
    bank_block,
    "settings-owned bank choice",
)

chooser_block = r'''    private void chooseBankingApp(){
        PackageManager packageManager=getPackageManager();
        Intent shareIntent=new Intent(Intent.ACTION_SEND);
        shareIntent.setType("image/png");

        List<ResolveInfo> candidates=packageManager.queryIntentActivities(shareIntent,PackageManager.MATCH_DEFAULT_ONLY);
        Map<String,AppChoice> byPackage=new LinkedHashMap<>();
        for(ResolveInfo resolveInfo:candidates){
            if(resolveInfo.activityInfo==null||resolveInfo.activityInfo.packageName==null)continue;
            String packageName=resolveInfo.activityInfo.packageName;
            if(packageName.equals(getPackageName()))continue;
            String label=String.valueOf(resolveInfo.loadLabel(packageManager)).trim();
            String haystack=(label+" "+packageName).toLowerCase(Locale.ROOT);
            if(!looksLikeBankingApp(haystack))continue;
            byPackage.putIfAbsent(packageName,new AppChoice(label,packageName));
        }

        List<AppChoice> found=new ArrayList<>(byPackage.values());
        found.sort((first,second)->{
            int firstPriority=bankPriority(first);
            int secondPriority=bankPriority(second);
            if(firstPriority!=secondPriority)return Integer.compare(firstPriority,secondPriority);
            return first.label.compareToIgnoreCase(second.label);
        });

        if(found.isEmpty()){
            Toast.makeText(this,"Keine kompatible Banking-App gefunden. Swiss QR kann weiterhin angezeigt oder gespeichert werden.",Toast.LENGTH_LONG).show();
            return;
        }

        String currentPackage=prefs.getString(PREF_BANK_PACKAGE,"");
        int selected=-1;
        String[] labels=new String[found.size()];
        for(int index=0;index<found.size();index++){
            AppChoice app=found.get(index);
            labels[index]=app.label;
            if(app.pkg.equals(currentPackage))selected=index;
        }

        new AlertDialog.Builder(this,dialogTheme())
                .setTitle("Banking-App auswählen")
                .setSingleChoiceItems(labels,selected,(dialog,index)->{
                    AppChoice choice=found.get(index);
                    prefs.edit()
                            .putString(PREF_BANK_PACKAGE,choice.pkg)
                            .putString(PREF_BANK_LABEL,choice.label)
                            .apply();
                    dialog.dismiss();
                    Toast.makeText(this,choice.label+" festgelegt.",Toast.LENGTH_SHORT).show();
                    if(current==Screen.SETTINGS)navigate(Screen.SETTINGS);
                })
                .setNegativeButton("Abbrechen",null)
                .show();
    }

    private boolean looksLikeBankingApp(String value){
        return value.matches(".*(ubs|postfinance|raiffeisen|zkb|kantonal|bank|neon|yuh|revolut|swissquote|cler|zak|migros|credit suisse|csx|bcv|bekb|bkb|blkb|akb|sgkb|luzerner|thurgauer|graub.ndner).*");
    }

'''
source = replace_between(
    source,
    "    private void openPreferred(boolean twint, EditText amountInput) {",
    "    private int bankPriority(AppChoice app) {",
    chooser_block,
    "banking app chooser",
)

source = source.replace(
    '    private void copyAmount(EditText input) { String a=amount(input==null?null:input.getText().toString()); if(a==null||a.isBlank()){Toast.makeText(this,"Banking-App geöffnet. Betrag dort eingeben.",Toast.LENGTH_SHORT).show();return;} copy("PFVR Betrag",a,"CHF "+a+" kopiert"); }\n',
    "",
)
source = source.replace(
    "    private boolean any(String s,String... xs){for(String x:xs)if(s.contains(x))return true;return false;}\n",
    "",
)

# Remove the unused single-series chart class. The active implementation is DualRiverTrendView.
if "new RiverTrendView(" in source:
    raise SystemExit("RiverTrendView is still instantiated; refusing to remove it")
source = replace_between(
    source,
    "    private class RiverTrendView extends View {",
    "    private static class AppChoice {",
    "",
    "unused single-series river chart",
)

# Version and project documentation.
build = replace_once(build, "        versionCode 16\n", "        versionCode 17\n", "version code")
build = replace_once(build, "        versionName '0.9.2'\n", "        versionName '0.9.3'\n", "version name")

project = PROJECT.read_text(encoding="utf-8")
project = project.replace(
    "- Vereinsbeiz: Warenkorb in den Gruppen Trinken, Essen und Feiern, freier Betrag, Swiss-QR-Zahlung, direkte Android-Übergabe an kompatible Banking-Apps und TWINT-Zahlungsweg.\n",
    "- Vereinsbeiz: Warenkorb in den Gruppen Trinken, Essen und Feiern, freier Betrag, Swiss-QR-Zahlung, direkte Android-Übergabe an eine unter Einstellungen → Zahlung gewählte kompatible Banking-App und TWINT-Zahlungsweg.\n",
)
project = project.replace(
    "- Darstellung: System / Hell / Dunkel.\n",
    "- Einstellungen: Bereiche Allgemein, Rhein und Zahlung; Darstellung System / Hell / Dunkel.\n",
)

status = '''# Status

Stand: Testversion `0.9.3` · aktualisiert 2026-09-02.

## Implementiert / im Test

- Einstellungen sind in die Bereiche Allgemein, Rhein und Zahlung gegliedert.
- Die bevorzugte Banking-App wird nur noch unter Einstellungen → Zahlung verwaltet. Die Auswahl speichert die App, startet sie aber nicht.
- Die Kasse zeigt den gewählten Zahlungsweg kompakt an. Ohne Auswahl erscheint ein deutlicher Hinweis mit direktem Sprung zur passenden Einstellung.
- Der Banking-Dialog bietet nur installierte, als Banking-App erkennbare Apps an, die PNG-Bilder über Androids Teilen-Funktion annehmen.
- Die beiden Abfluss-Kurzkarten auf Home werden bei aktiver zweiter Station auf dieselbe Höhe gesetzt; maßgeblich ist die höhere Karte mit allen Zusatzinformationen.
- Pro aktiver Station bleibt ein gemeinsames Diagramm für Abfluss und Pegel mit zwei dynamischen Y-Achsen erhalten; Rheinfelden zeigt die Wassertemperatur in der Kurzkarte.
- Live-Aktualisierung baut nur den Live-Bereich neu auf und stellt die Scrollposition nach dem Layout wieder her.
- Nicht mehr verwendete Einzelgrafik-, Metrik- und generische App-Startlogik wurde entfernt.
- Vereinsbeiz bleibt in Trinken, Essen und Feiern gegliedert; Warenkorb steht vor der Auswahl, zeigt Positionen und Total und erlaubt direktes Entfernen.
- Interner Bereich startet standardmäßig in der App-Ansicht und übernimmt Hell-/Dunkelmodus.
- Android 16 / API 36 als Target.

## Noch zu verifizieren

- Visuelle Prüfung der gleich hohen Kurzkarten und der Einstellungs-Tabs auf kleinen realen Android-Geräten und im Dark Mode.
- Direkte QR-Übergabe mit weiteren Banking-Apps; Yuh ist real bestätigt.
- Updatepfad: Dauerhaft installierbare Updates benötigen einmalig eine stabile private Test-/Release-Signatur. Ein wechselnder GitHub-Runner-Debug-Key ist nicht updatefähig.

## Spätere Punkte

- Weitere Aufteilung der noch großen `MainActivity` in klar getrennte UI- und Service-Komponenten.
- Native Vereinsnews aus WordPress REST/RSS.
- Optionale Trainingsbenachrichtigungen und Homescreen-Widget.
- iOS-Implementierung.
'''

# Sanity checks before committing generated source.
required = (
    "private enum SettingsTab",
    "private View cashBankStatusCard()",
    "private View bankChoiceSettingsCard()",
    "private void chooseBankingApp()",
    "equalizeSummaryCardHeights(first,second)",
    "Einstellungen → Zahlung",
)
for marker in required:
    if marker not in source:
        raise SystemExit(f"required 0.9.3 marker missing: {marker}")

for marker in (
    "PREF_TWINT_PACKAGE",
    "PREF_TWINT_LABEL",
    "PREF_RIVER_METRIC",
    "PREF_RIVER_SLOT1_METRIC",
    "PREF_RIVER_SLOT2_METRIC",
    "private class RiverTrendView extends View",
    "private void chooseApp(",
    "private void openPreferred(",
    "bankChoiceCard()",
):
    if marker in source:
        raise SystemExit(f"obsolete marker remains: {marker}")

MAIN.write_text(source,encoding="utf-8")
BUILD.write_text(build,encoding="utf-8")
PROJECT.write_text(project,encoding="utf-8")
STATUS.write_text(status,encoding="utf-8")
print("Applied Android 0.9.3 UI and cleanup changes.")
