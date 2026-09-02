from pathlib import Path

MAIN = Path('Android/app/src/main/java/ch/pfvr/internapp/MainActivity.java')
STATUS = Path('STATUS.md')
PROJECT = Path('PROJECT.md')


def replace_once(text: str, old: str, new: str, label: str) -> str:
    count = text.count(old)
    if count != 1:
        raise SystemExit(f'expected one {label} marker, found {count}')
    return text.replace(old, new, 1)


source = MAIN.read_text(encoding='utf-8')
if 'private LinearLayout riverTemperatureCard(int slot)' in source:
    print('Separate temperature chart already applied.')
    raise SystemExit(0)

old_stack = '''        stack.addView(riverSummaryRow(),margin(-1,-2,0,10,0,10));
        stack.addView(riverCombinedCard(1),margin(-1,-2,0,0,0,10));
        if(riverSlotEnabled(2))stack.addView(riverCombinedCard(2),margin(-1,-2,0,0,0,4));
'''
new_stack = '''        stack.addView(riverSummaryRow(),margin(-1,-2,0,10,0,10));
        boolean secondRiver=riverSlotEnabled(2);
        addRiverStationCharts(stack,1,!secondRiver);
        if(secondRiver)addRiverStationCharts(stack,2,true);
'''
source = replace_once(source, old_stack, new_stack, 'home river chart stack')

refresh_marker = '    private void refreshHomeLiveViews(){\n'
chart_helper = '''    private void addRiverStationCharts(LinearLayout stack,int slot,boolean last){
        HydroStation station=riverStation(slot);
        boolean temperature=station.supportsTemperature;
        stack.addView(riverCombinedCard(slot),margin(-1,-2,0,0,0,temperature?10:(last?4:10)));
        if(temperature)stack.addView(riverTemperatureCard(slot),margin(-1,-2,0,0,0,last?4:10));
    }

'''
source = replace_once(source, refresh_marker, chart_helper + refresh_marker, 'river chart helper insertion')

combined_marker = '    private LinearLayout riverCombinedCard(int slot){\n'
temperature_card = '''    private LinearLayout riverTemperatureCard(int slot){
        HydroStation station=riverStation(slot);
        RiverRange range=riverRange();
        TrendSeries temperature=hydroSeries(station,"WT",range);
        double temperatureNow=currentHydroValue(station,"WT");
        HydroMath.Stats stats=HydroMath.stats(temperature.values);
        int temperatureColor=themeText(RiverMetric.TEMPERATURE.color);

        LinearLayout card=card();
        card.setOrientation(LinearLayout.VERTICAL);
        card.setPadding(dp(16),dp(14),dp(16),dp(14));

        LinearLayout header=new LinearLayout(this);
        header.setGravity(Gravity.CENTER_VERTICAL);
        header.addView(txt("Wassertemperatur · "+station.label,12,TEXT,true),new LinearLayout.LayoutParams(0,-2,1));
        TextView source=txt("BAFU "+station.id+"  ↗",11,WATER,true);
        source.setOnClickListener(v->external(station.stationUrl()));
        source.setPadding(dp(8),dp(4),0,dp(4));
        header.addView(source,new LinearLayout.LayoutParams(-2,-2));
        card.addView(header);

        TextView value=txt(Double.isFinite(temperatureNow)?formatMetric(station,RiverMetric.TEMPERATURE,temperatureNow)+" °C":"–",24,temperatureColor,true);
        value.setTextColor(temperatureColor);
        value.setPadding(0,dp(7),0,dp(3));
        card.addView(value);

        if(stats.isValid()){
            String change=(stats.change()>=0?"+":"")+formatMetric(station,RiverMetric.TEMPERATURE,stats.change());
            TextView summary=txt(
                    "Min "+formatMetric(station,RiverMetric.TEMPERATURE,stats.min)+" °C · Max "+formatMetric(station,RiverMetric.TEMPERATURE,stats.max)+" °C · Δ "+change+" °C",
                    11,
                    MUTED,
                    false
            );
            summary.setPadding(0,0,0,dp(5));
            card.addView(summary);
        }

        if(temperature.values.size()>=2){
            SingleSeriesTrendView graph=new SingleSeriesTrendView(
                    this,
                    temperature.times,
                    temperature.values,
                    range==RiverRange.WEEK,
                    range.label,
                    "Wassertemperatur",
                    "°C",
                    1,
                    temperatureColor,
                    darkMode?Color.rgb(57,72,82):Color.rgb(220,229,234),
                    themeText(MUTED),
                    themeBg(Color.WHITE),
                    darkMode?DARK_SOFT:NAVY,
                    darkMode?DARK_TEXT:Color.WHITE
            );
            card.addView(graph,new LinearLayout.LayoutParams(-1,dp(205)));
            TextView hint=txt("Diagramm berühren für Einzelwerte",10,MUTED,false);
            hint.setGravity(Gravity.CENTER);
            hint.setPadding(0,dp(4),0,0);
            card.addView(hint);
        }else{
            TextView unavailable=txt("Für den gewählten Zeitraum liegen noch nicht genügend Temperaturwerte vor.",12,MUTED,false);
            unavailable.setPadding(0,dp(8),0,dp(3));
            card.addView(unavailable);
        }
        return card;
    }

'''
source = replace_once(source, combined_marker, temperature_card + combined_marker, 'temperature card insertion')

old_has_bank = '''    private boolean hasPreferredBank(){
        return !prefs.getString(PREF_BANK_PACKAGE,"").trim().isEmpty()
                && !prefs.getString(PREF_BANK_LABEL,"").trim().isEmpty();
    }
'''
new_has_bank = '''    private boolean hasPreferredBank(){
        String packageName=prefs.getString(PREF_BANK_PACKAGE,"").trim();
        String label=prefs.getString(PREF_BANK_LABEL,"").trim();
        if(packageName.isEmpty()||label.isEmpty())return false;
        Intent share=new Intent(Intent.ACTION_SEND);
        share.setType("image/png");
        share.setPackage(packageName);
        return share.resolveActivity(getPackageManager())!=null;
    }
'''
source = replace_once(source, old_has_bank, new_has_bank, 'preferred bank availability check')

old_payment_label = '''    private String preferredBankPaymentLabel(){
        String label=selectedBankLabel();
        return label.isEmpty()?"Banking-App festlegen":"Mit "+label+" bezahlen";
    }
'''
new_payment_label = '''    private String preferredBankPaymentLabel(){
        String label=selectedBankLabel();
        return hasPreferredBank()?"Mit "+label+" bezahlen":"Banking-App festlegen";
    }
'''
source = replace_once(source, old_payment_label, new_payment_label, 'preferred bank button label')

MAIN.write_text(source, encoding='utf-8')

if STATUS.exists():
    status=STATUS.read_text(encoding='utf-8')
    status=status.replace(
        '- Pro aktiver Station bleibt ein gemeinsames Diagramm für Abfluss und Pegel mit zwei dynamischen Y-Achsen erhalten; Rheinfelden zeigt die Wassertemperatur in der Kurzkarte.\n',
        '- Pro aktiver Station bleibt ein gemeinsames Diagramm für Abfluss und Pegel mit zwei dynamischen Y-Achsen erhalten; Stationen mit Temperaturdaten erhalten darunter eine separate Wassertemperaturgrafik.\n'
    )
    status=status.replace(
        '- Direkte QR-Übergabe mit weiteren Banking-Apps; Yuh ist real bestätigt.\n',
        '- Direkte QR-Übergabe mit weiteren Banking-Apps; Yuh ist real bestätigt. Nicht mehr installierte oder nicht mehr kompatible bevorzugte Apps werden als nicht verfügbar behandelt.\n'
    )
    STATUS.write_text(status,encoding='utf-8')

if PROJECT.exists():
    project=PROJECT.read_text(encoding='utf-8')
    project=project.replace(
        '- Rhein: zwei Stationskacheln, davon die zweite optional. Aktuelle Abflüsse stehen kompakt nebeneinander; Rheinfelden zeigt oben zusätzlich die Wassertemperatur. Pro aktiver Station kombiniert ein `1h`/`24h`/`7d`-Diagramm Abfluss und Pegel mit zwei dynamischen Y-Achsen.\n',
        '- Rhein: zwei Stationskacheln, davon die zweite optional. Aktuelle Abflüsse stehen kompakt nebeneinander; Rheinfelden zeigt oben zusätzlich die Wassertemperatur. Pro aktiver Station kombiniert ein `1h`/`24h`/`7d`-Diagramm Abfluss und Pegel mit zwei dynamischen Y-Achsen; vorhandene Temperaturdaten werden darunter separat dargestellt.\n'
    )
    PROJECT.write_text(project,encoding='utf-8')

required=(
    'private void addRiverStationCharts',
    'private LinearLayout riverTemperatureCard(int slot)',
    'SingleSeriesTrendView graph=new SingleSeriesTrendView',
    'return share.resolveActivity(getPackageManager())!=null;',
)
for marker in required:
    if marker not in source:
        raise SystemExit(f'missing marker: {marker}')

print('Applied separate river temperature chart and bank availability check.')
