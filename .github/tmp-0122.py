from pathlib import Path


def replace_once(text, old, new, label):
    count=text.count(old)
    if count!=1:
        raise SystemExit(f"{label}: expected 1 occurrence, got {count}")
    return text.replace(old,new,1)

# --- MainActivity ---
path=Path("Android/app/src/main/java/ch/pfvr/internapp/MainActivity.java")
s=path.read_text()

s=replace_once(s,
'''    private static final String PREF_WEATHER_SOURCE = "weather_source";\n''',
'''    private static final String PREF_WEATHER_SOURCE = "weather_source";\n    private static final String PREF_CASH_CART = "cash_cart_v1";\n''',
"cash cart preference key")

s=replace_once(s,
'''        tileLayoutStore = new TileLayoutStore(prefs);\n        dataRefreshHandler = new Handler(Looper.getMainLooper());\n''',
'''        tileLayoutStore = new TileLayoutStore(prefs);\n        loadCashCart();\n        dataRefreshHandler = new Handler(Looper.getMainLooper());\n''',
"load cash cart during unlocked startup")

old='''        List<WeatherDaily.Summary> days=WeatherDaily.summarize(weatherHours(raw),LocalDate.now(ZoneId.of("Europe/Zurich")),3);\n        boolean hasAny=false;\n        for(WeatherDaily.Summary day:days)if(day.hasData()){hasAny=true;break;}\n        if(!hasAny){\n            c.addView(txt("Gespeicherte Wetterdaten nicht lesbar",14,MUTED,false));\n            TextView src=txtRaw(weatherAge(source,updated),10,Color.rgb(126,140,150),false);\n            src.setPadding(0,dp(8),0,0);\n            c.addView(src);\n            return c;\n        }\n\n        boolean horizontal=getResources().getConfiguration().screenWidthDp>=520;\n        LinearLayout dayStack=new LinearLayout(this);\n        dayStack.setOrientation(horizontal?LinearLayout.HORIZONTAL:LinearLayout.VERTICAL);\n        dayStack.setBaselineAligned(false);\n        c.addView(dayStack,new LinearLayout.LayoutParams(-1,-2));\n\n        for(int index=0;index<days.size();index++){\n            View dayView=weatherDaySummaryView(days.get(index),index,horizontal);\n            LinearLayout.LayoutParams params=horizontal\n                    ?new LinearLayout.LayoutParams(0,-2,1)\n                    :new LinearLayout.LayoutParams(-1,-2);\n            if(index>0){\n                if(horizontal)params.setMargins(dp(7),0,0,0);\n                else params.setMargins(0,dp(7),0,0);\n            }\n            dayStack.addView(dayView,params);\n        }\n'''
new='''        List<WeatherDaily.Hour> hours=weatherHours(raw);\n        List<WeatherDaily.Summary> days=WeatherDaily.summarize(hours,LocalDate.now(ZoneId.of("Europe/Zurich")),3);\n        boolean hasAny=false;\n        for(WeatherDaily.Summary day:days)if(day.hasData()){hasAny=true;break;}\n        if(!hasAny){\n            c.addView(txt("Gespeicherte Wetterdaten nicht lesbar",14,MUTED,false));\n            TextView src=txtRaw(weatherAge(source,updated),10,Color.rgb(126,140,150),false);\n            src.setPadding(0,dp(8),0,0);\n            c.addView(src);\n            return c;\n        }\n\n        LinearLayout dayStack=new LinearLayout(this);\n        dayStack.setOrientation(LinearLayout.VERTICAL);\n        dayStack.setBaselineAligned(false);\n        c.addView(dayStack,new LinearLayout.LayoutParams(-1,-2));\n\n        for(int index=0;index<days.size();index++){\n            View dayView=weatherDaySummaryView(days.get(index),index,hours);\n            LinearLayout.LayoutParams params=new LinearLayout.LayoutParams(-1,-2);\n            if(index>0)params.setMargins(0,dp(8),0,0);\n            dayStack.addView(dayView,params);\n        }\n'''
s=replace_once(s,old,new,"three day weather stack")

start=s.index('    private View weatherDaySummaryView(')
end=s.index('    private String weatherDayLabel(',start)
old=s[start:end]
new='''    private View weatherDaySummaryView(WeatherDaily.Summary summary,int index,List<WeatherDaily.Hour> hours){\n        LinearLayout day=new LinearLayout(this);\n        day.setOrientation(LinearLayout.VERTICAL);\n        day.setPadding(dp(11),dp(10),dp(11),dp(10));\n        day.setBackground(round(Color.rgb(238,243,246),14));\n\n        TextView label=txtRaw(weatherDayLabel(summary.date,index),12,WATER,true);\n        label.setPadding(dp(2),0,dp(2),dp(6));\n        day.addView(label);\n\n        LinearLayout slots=new LinearLayout(this);\n        slots.setGravity(Gravity.TOP);\n        slots.setBaselineAligned(false);\n        List<WeatherDaily.Slot> values=WeatherDaily.slots(hours,summary.date,10,14,18);\n        for(int slotIndex=0;slotIndex<values.size();slotIndex++){\n            if(slotIndex>0){\n                View divider=new View(this);\n                divider.setBackgroundColor(darkMode?Color.rgb(63,76,85):Color.rgb(216,226,232));\n                LinearLayout.LayoutParams dividerParams=new LinearLayout.LayoutParams(dp(1),dp(88));\n                dividerParams.setMargins(dp(3),dp(5),dp(3),0);\n                slots.addView(divider,dividerParams);\n            }\n            slots.addView(weatherDaySlotView(values.get(slotIndex)),new LinearLayout.LayoutParams(0,-2,1));\n        }\n        day.addView(slots,new LinearLayout.LayoutParams(-1,-2));\n\n        TextView details=txtRaw(weatherDayDetails(summary),10,MUTED,false);\n        details.setPadding(dp(2),dp(8),dp(2),0);\n        day.addView(details);\n        return day;\n    }\n\n    private View weatherDaySlotView(WeatherDaily.Slot slot){\n        LinearLayout box=new LinearLayout(this);\n        box.setOrientation(LinearLayout.VERTICAL);\n        box.setGravity(Gravity.CENTER_HORIZONTAL);\n        box.setPadding(dp(3),0,dp(3),0);\n\n        String time=String.format(Locale.GERMAN,"%02d Uhr",slot.targetTime.getHour());\n        TextView timeView=txtRaw(time,10,WATER,true);\n        timeView.setGravity(Gravity.CENTER);\n        box.addView(timeView,new LinearLayout.LayoutParams(-1,-2));\n\n        if(!slot.hasData()){\n            TextView icon=txtRaw("◌",27,TEXT,false);\n            icon.setGravity(Gravity.CENTER);\n            box.addView(icon,new LinearLayout.LayoutParams(-1,dp(36)));\n            TextView temperature=txtRaw("–",14,TEXT,true);\n            temperature.setGravity(Gravity.CENTER);\n            box.addView(temperature);\n            TextView rain=txt("Regen –",9,MUTED,false);\n            rain.setGravity(Gravity.CENTER);\n            box.addView(rain);\n            return box;\n        }\n\n        WeatherDaily.Hour hour=slot.hour;\n        TextView icon=txtRaw(weatherIcon(hour.weatherCode),27,TEXT,false);\n        icon.setGravity(Gravity.CENTER);\n        box.addView(icon,new LinearLayout.LayoutParams(-1,dp(36)));\n        String temperature=Double.isFinite(hour.temperature)?String.format(Locale.GERMAN,"%.0f °C",hour.temperature):"–";\n        TextView temperatureView=txtRaw(temperature,14,TEXT,true);\n        temperatureView.setGravity(Gravity.CENTER);\n        box.addView(temperatureView);\n        TextView rain=txt(ui("Regen")+" "+hour.precipitationProbability+" %",9,MUTED,false);\n        rain.setGravity(Gravity.CENTER);\n        box.addView(rain);\n        box.setContentDescription(time+", "+weatherCode(hour.weatherCode)+", "+temperature+", "+ui("Regen")+" "+hour.precipitationProbability+" Prozent");\n        return box;\n    }\n\n'''
s=s[:start]+new+s[end:]

old='''    private String weatherDayDetails(WeatherDaily.Summary summary){\n        if(!summary.hasData())return ui("Für diesen Tag liegen noch keine Stundenwerte vor.");\n        StringBuilder details=new StringBuilder();\n        details.append(ui("Regen")).append(' ').append(summary.precipitationProbabilityMax).append(" % · ")\n                .append(String.format(Locale.GERMAN,"%.1f mm",summary.precipitationSum));\n        if(Double.isFinite(summary.windMax)||Double.isFinite(summary.gustMax)){\n            details.append("\\n").append(ui("Wind")).append(' ')\n                    .append(Double.isFinite(summary.windMax)?Math.round(summary.windMax):0).append(" km/h")\n                    .append(" · ").append(ui("Böen")).append(' ')\n                    .append(Double.isFinite(summary.gustMax)?Math.round(summary.gustMax):0).append(" km/h");\n        }\n        details.append("\\nUV ");\n        if(Double.isFinite(summary.uvMax))details.append(String.format(Locale.GERMAN,"%.1f",summary.uvMax)).append(" · ").append(uvLabel(summary.uvMax));\n        else details.append("–");\n        return details.toString();\n    }\n'''
new='''    private String weatherDayDetails(WeatherDaily.Summary summary){\n        if(!summary.hasData())return ui("Für diesen Tag liegen noch keine Stundenwerte vor.");\n        StringBuilder details=new StringBuilder();\n        if(Double.isFinite(summary.minTemperature)&&Double.isFinite(summary.maxTemperature)){\n            details.append(ui("Tag")).append(' ').append(String.format(Locale.GERMAN,"%.0f–%.0f °C",summary.minTemperature,summary.maxTemperature));\n        }\n        if(Double.isFinite(summary.precipitationSum)){\n            if(details.length()>0)details.append(" · ");\n            details.append(ui("Regenmenge")).append(' ').append(String.format(Locale.GERMAN,"%.1f mm",summary.precipitationSum));\n        }\n        details.append("\\nUV ");\n        if(Double.isFinite(summary.uvMax))details.append(String.format(Locale.GERMAN,"%.1f",summary.uvMax)).append(" · ").append(uvLabel(summary.uvMax));\n        else details.append("–");\n        if(Double.isFinite(summary.windMax)||Double.isFinite(summary.gustMax)){\n            details.append(" · ").append(ui("Wind")).append(' ')\n                    .append(Double.isFinite(summary.windMax)?Math.round(summary.windMax):0).append(" km/h")\n                    .append(" · ").append(ui("Böen")).append(' ')\n                    .append(Double.isFinite(summary.gustMax)?Math.round(summary.gustMax):0).append(" km/h");\n        }\n        return details.toString();\n    }\n'''
s=replace_once(s,old,new,"daily weather footer")

s=replace_once(s,
'''    TextView clearCart=link("Warenkorb leeren");\n    clearCart.setOnClickListener(v->{cashCart.clear();for(TextView quantity:cashQuantityViews.values())quantity.setText("0");updateCashSummary();});\n''',
'''    TextView clearCart=link("Warenkorb leeren");\n    clearCart.setOnClickListener(v->clearCashCart());\n''',
"cart clear persistence")

s=replace_once(s,
'''    private void setCashQuantity(String itemId,int quantity){\n        if(quantity<=0)cashCart.remove(itemId);else cashCart.put(itemId,quantity);\n        TextView view=cashQuantityViews.get(itemId);\n        if(view!=null)view.setText(String.valueOf(Math.max(0,quantity)));\n    }\n\n''',
'''    private void setCashQuantity(String itemId,int quantity){\n        if(quantity<=0)cashCart.remove(itemId);else cashCart.put(itemId,quantity);\n        saveCashCart();\n        TextView view=cashQuantityViews.get(itemId);\n        if(view!=null)view.setText(String.valueOf(Math.max(0,quantity)));\n    }\n\n    private void loadCashCart(){\n        cashCart.clear();\n        if(prefs==null)return;\n        Set<String> stored=prefs.getStringSet(PREF_CASH_CART,null);\n        cashCart.putAll(CashCartState.decode(stored));\n    }\n\n    private void saveCashCart(){\n        if(prefs==null)return;\n        prefs.edit().putStringSet(PREF_CASH_CART,CashCartState.encode(cashCart)).apply();\n    }\n\n    private void clearCashCart(){\n        cashCart.clear();\n        saveCashCart();\n        for(TextView quantity:cashQuantityViews.values())quantity.setText("0");\n        updateCashSummary();\n    }\n\n''',
"cash cart load/save helpers")

path.write_text(s)

# --- WeatherDaily slot selection ---
path=Path("Android/app/src/main/java/ch/pfvr/internapp/WeatherDaily.java")
s=path.read_text()
s=replace_once(s,
'''    static final class Summary {\n''',
'''    static final class Slot {\n        final LocalTime targetTime;\n        final Hour hour;\n\n        Slot(LocalTime targetTime,Hour hour){\n            this.targetTime=targetTime;\n            this.hour=hour;\n        }\n\n        boolean hasData(){return hour!=null;}\n    }\n\n    static final class Summary {\n''',
"weather slot model")

s=replace_once(s,
'''    static List<Summary> summarize(List<Hour> hours,LocalDate firstDay,int dayCount){\n''',
'''    static List<Slot> slots(List<Hour> hours,LocalDate day,int... targetHours){\n        List<Slot> out=new ArrayList<>();\n        if(day==null||targetHours==null)return out;\n        List<Hour> safe=hours==null?List.of():hours;\n        for(int targetHour:targetHours){\n            if(targetHour<0||targetHour>23)continue;\n            LocalTime target=LocalTime.of(targetHour,0);\n            Hour best=null;\n            int bestDistance=Integer.MAX_VALUE;\n            for(Hour hour:safe){\n                if(hour==null||hour.time==null||!day.equals(hour.time.toLocalDate()))continue;\n                int distance=Math.abs(hour.time.toLocalTime().toSecondOfDay()-target.toSecondOfDay());\n                if(distance<bestDistance){best=hour;bestDistance=distance;}\n            }\n            if(bestDistance>90*60)best=null;\n            out.add(new Slot(target,best));\n        }\n        return out;\n    }\n\n    static List<Summary> summarize(List<Hour> hours,LocalDate firstDay,int dayCount){\n''',
"weather slot selection")
path.write_text(s)

# --- pure cart codec ---
Path("Android/app/src/main/java/ch/pfvr/internapp/CashCartState.java").write_text('''package ch.pfvr.internapp;\n\nimport java.util.LinkedHashMap;\nimport java.util.LinkedHashSet;\nimport java.util.Map;\nimport java.util.Set;\n\n/** Durable representation of the Vereinsbeiz shopping cart. */\nfinal class CashCartState {\n    private CashCartState() {}\n\n    static Set<String> encode(Map<String,Integer> cart){\n        Set<String> out=new LinkedHashSet<>();\n        if(cart==null)return out;\n        for(Map.Entry<String,Integer> entry:cart.entrySet()){\n            String id=cleanId(entry.getKey());\n            int quantity=entry.getValue()==null?0:entry.getValue();\n            if(id.isEmpty()||quantity<=0)continue;\n            out.add(id+"="+Math.min(99,quantity));\n        }\n        return out;\n    }\n\n    static Map<String,Integer> decode(Set<String> stored){\n        Map<String,Integer> out=new LinkedHashMap<>();\n        if(stored==null)return out;\n        for(String value:stored){\n            if(value==null)continue;\n            int separator=value.lastIndexOf('=');\n            if(separator<=0||separator>=value.length()-1)continue;\n            String id=cleanId(value.substring(0,separator));\n            if(id.isEmpty())continue;\n            try{\n                int quantity=Integer.parseInt(value.substring(separator+1));\n                if(quantity>0)out.put(id,Math.min(99,quantity));\n            }catch(NumberFormatException ignored){}\n        }\n        return out;\n    }\n\n    private static String cleanId(String id){\n        if(id==null)return "";\n        String clean=id.trim();\n        if(clean.isEmpty()||clean.length()>120||clean.indexOf('\\n')>=0||clean.indexOf('\\r')>=0||clean.indexOf('=')>=0)return "";\n        return clean;\n    }\n}\n''')

# --- tests ---
path=Path("Android/app/src/test/java/ch/pfvr/internapp/WeatherDailyTest.java")
s=path.read_text()
insert='''\n    @Test public void selectsMorningNoonAndEveningForecastSlots(){\n        LocalDate day=LocalDate.of(2026,9,7);\n        List<WeatherDaily.Hour> hours=List.of(\n                hour(day,9,14,5,0.0,4,7,1.0,1),\n                hour(day,10,15,10,0.0,5,8,2.0,2),\n                hour(day,14,23,35,0.3,10,18,6.0,2),\n                hour(day,18,19,70,1.1,8,16,1.0,61)\n        );\n        List<WeatherDaily.Slot> slots=WeatherDaily.slots(hours,day,10,14,18);\n        assertEquals(3,slots.size());\n        assertEquals(10,slots.get(0).targetTime.getHour());\n        assertEquals(15.0,slots.get(0).hour.temperature,0.001);\n        assertEquals(35,slots.get(1).hour.precipitationProbability);\n        assertEquals(61,slots.get(2).hour.weatherCode);\n    }\n\n    @Test public void usesOnlyNearbyHourAndKeepsMissingSlotVisible(){\n        LocalDate day=LocalDate.of(2026,9,7);\n        List<WeatherDaily.Hour> hours=List.of(hour(day,11,16,20,0.0,5,8,2.0,2));\n        List<WeatherDaily.Slot> slots=WeatherDaily.slots(hours,day,10,14,18);\n        assertTrue(slots.get(0).hasData());\n        assertFalse(slots.get(1).hasData());\n        assertFalse(slots.get(2).hasData());\n    }\n'''
s=s.replace('\n    private static WeatherDaily.Hour hour(',insert+'\n    private static WeatherDaily.Hour hour(',1)
path.write_text(s)

Path("Android/app/src/test/java/ch/pfvr/internapp/CashCartStateTest.java").write_text('''package ch.pfvr.internapp;\n\nimport org.junit.Test;\n\nimport java.util.LinkedHashMap;\nimport java.util.Map;\nimport java.util.Set;\n\nimport static org.junit.Assert.assertEquals;\nimport static org.junit.Assert.assertFalse;\n\npublic class CashCartStateTest {\n    @Test public void roundTripsCartAcrossProcessLifetime(){\n        Map<String,Integer> cart=new LinkedHashMap<>();\n        cart.put("soft_5dl",2);\n        cart.put("food_other",3);\n        Map<String,Integer> restored=CashCartState.decode(CashCartState.encode(cart));\n        assertEquals(cart,restored);\n    }\n\n    @Test public void ignoresEmptyAndCorruptEntriesAndClampsQuantity(){\n        Map<String,Integer> restored=CashCartState.decode(Set.of("soft_5dl=120","bad","=4","food_other=x"));\n        assertEquals(Integer.valueOf(99),restored.get("soft_5dl"));\n        assertEquals(1,restored.size());\n        assertFalse(restored.containsKey("food_other"));\n    }\n}\n''')

Path("Android/app/src/test/java/ch/pfvr/internapp/CashCartPersistenceSourceTest.java").write_text('''package ch.pfvr.internapp;\n\nimport org.junit.Test;\n\nimport java.nio.charset.StandardCharsets;\nimport java.nio.file.Files;\nimport java.nio.file.Path;\nimport java.nio.file.Paths;\n\nimport static org.junit.Assert.assertTrue;\n\npublic class CashCartPersistenceSourceTest {\n    private static String source() throws Exception {\n        String relative="src/main/java/ch/pfvr/internapp/MainActivity.java";\n        Path[] candidates={Paths.get(relative),Paths.get("app",relative),Paths.get("Android","app",relative)};\n        for(Path candidate:candidates)if(Files.isRegularFile(candidate))return new String(Files.readAllBytes(candidate), StandardCharsets.UTF_8);\n        throw new IllegalStateException("MainActivity.java not found");\n    }\n\n    @Test public void cartLoadsOnStartupAndPersistsEveryQuantityChange() throws Exception {\n        String activity=source();\n        assertTrue(activity.contains("PREF_CASH_CART = \\\"cash_cart_v1\\\""));\n        assertTrue(activity.contains("tileLayoutStore = new TileLayoutStore(prefs);\\n        loadCashCart();"));\n        assertTrue(activity.contains("saveCashCart();\\n        TextView view=cashQuantityViews.get(itemId)"));\n        assertTrue(activity.contains("clearCart.setOnClickListener(v->clearCashCart())"));\n        assertTrue(activity.contains("putStringSet(PREF_CASH_CART,CashCartState.encode(cashCart))"));\n    }\n}\n''')

path=Path("Android/app/src/test/java/ch/pfvr/internapp/WeatherForecastSourceTest.java")
s=path.read_text()
s=replace_once(s,
'''        assertTrue(activity.contains("WeatherDaily.summarize(weatherHours(raw)"));\n''',
'''        assertTrue(activity.contains("List<WeatherDaily.Hour> hours=weatherHours(raw)"));\n        assertTrue(activity.contains("WeatherDaily.slots(hours,summary.date,10,14,18)"));\n        assertTrue(activity.contains("%02d Uhr"));\n        assertTrue(activity.contains("hour.precipitationProbability+\" %\""));\n''',
"weather source regression")
path.write_text(s)

# --- version/docs ---
path=Path("Android/app/build.gradle")
s=path.read_text()
s=replace_once(s,"versionCode 58","versionCode 59","version code")
s=replace_once(s,"versionName '0.12.1'","versionName '0.12.2'","version name")
path.write_text(s)

path=Path("STATUS.md")
s=path.read_text()
s=replace_once(s,"Stand: Testversion `0.12.1`","Stand: Testversion `0.12.2`","status version")
anchor="## Aktueller Teststand\n\n"
entry="- `0.12.2` macht den Vereinsbeiz-Warenkorb dauerhaft: jede Mengenänderung wird unmittelbar lokal gespeichert und nach App-/Prozessneustart wieder geladen; geleert wird er nur über `Warenkorb leeren`. Die 3-Tage-Wetterkachel zeigt pro Tag zusätzlich drei feste Tageszeitpunkte um 10:00, 14:00 und 18:00 Uhr mit Wetter-Symbol, Temperatur und Regenwahrscheinlichkeit. Tages-Min/Max, Regenmenge, UV sowie Wind/Böen bleiben kompakt als Tageszusammenfassung erhalten. Die Darstellung nutzt weiterhin ausschließlich denselben vorhandenen 8-Tage-Stundencache und erzeugt keine weiteren Wetter-Requests.\n"
if anchor not in s: raise SystemExit("status anchor missing")
s=s.replace(anchor,anchor+entry,1)
path.write_text(s)

path=Path("PROJECT.md")
s=path.read_text()
s=replace_once(s,
"  - Zusätzlich gibt es eine anordenbare 3-Tage-Wetterkachel für Rheinfelden mit Tages-Min/Max-Temperatur, Regenwahrscheinlichkeit/-menge, Wind/Böen und UV-Maximum. Sie nutzt denselben lokal gecachten Wetterabruf wie das Trainingswetter und erzeugt keinen zusätzlichen API-Request.\n",
"  - Zusätzlich gibt es eine anordenbare 3-Tage-Wetterkachel für Rheinfelden. Pro Tag zeigt sie Prognosepunkte um 10:00, 14:00 und 18:00 Uhr mit Wetter-Symbol, Temperatur und Regenwahrscheinlichkeit; Tages-Min/Max, Regenmenge, Wind/Böen und UV-Maximum bleiben als kompakte Tageszusammenfassung erhalten. Sie nutzt denselben lokal gecachten Wetterabruf wie das Trainingswetter und erzeugt keinen zusätzlichen API-Request.\n",
"project weather description")
s=replace_once(s,
"- Vereinsbeiz: fixierter Warenkorb, anordenbare Kategorien Trinken/Essen/Feiern, freier Betrag, Swiss-QR-Zahlung, direkte Android-Übergabe an eine unter Einstellungen → Zahlung gewählte Banking-App und TWINT-Zahlungsweg.\n",
"- Vereinsbeiz: fixierter, dauerhaft lokal gespeicherter Warenkorb, der App-/Prozessneustarts übersteht und nur durch `Warenkorb leeren` zurückgesetzt wird; dazu anordenbare Kategorien Trinken/Essen/Feiern, freier Betrag, Swiss-QR-Zahlung, direkte Android-Übergabe an eine unter Einstellungen → Zahlung gewählte Banking-App und TWINT-Zahlungsweg.\n",
"project cart persistence")
path.write_text(s)
