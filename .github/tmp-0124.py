from pathlib import Path


def replace_once(text, old, new, label):
    count=text.count(old)
    if count!=1:
        raise SystemExit(f"{label}: expected 1 occurrence, got {count}")
    return text.replace(old,new,1)

# --- MainActivity ---
path=Path("Android/app/src/main/java/ch/pfvr/internapp/MainActivity.java")
s=path.read_text()

s=replace_once(
    s,
    '    LinearLayout group=tileGroup("Trainingswetter","Prognose für den nächsten relevanten Termin");',
    '    LinearLayout group=tileGroup("Wetter zum nächsten Termin","Prognose für den nächsten relevanten Vereinsanlass");',
    'weather tile title'
)

old='''    private LinearLayout weatherCard(){
        LinearLayout c=card(); c.setOrientation(LinearLayout.VERTICAL); c.setPadding(dp(16),dp(15),dp(16),dp(14));
        String[] x=weatherSummary();
        c.addView(txt(x[0],11,WATER,true));
        LinearLayout row=new LinearLayout(this); row.setGravity(Gravity.CENTER_VERTICAL); row.setPadding(0,dp(5),0,dp(5)); c.addView(row);
        TextView icon=txtRaw(x[5],38,themeText(TEXT),false); icon.setGravity(Gravity.CENTER); row.addView(icon,new LinearLayout.LayoutParams(dp(58),dp(58)));
        LinearLayout info=new LinearLayout(this); info.setOrientation(LinearLayout.VERTICAL); info.setPadding(dp(7),0,0,0); row.addView(info,new LinearLayout.LayoutParams(0,-2,1));
        info.addView(txtRaw(x[1],15,MUTED,true));
        TextView main=txtRaw(x[2],21,TEXT,true); main.setPadding(0,dp(2),0,0); info.addView(main);
        TextView details=txtRaw(x[3],13,MUTED,false); details.setPadding(0,dp(5),0,0); c.addView(details);
        TextView src=txtRaw(x[4],10,Color.rgb(126,140,150),false); src.setPadding(0,dp(8),0,0); c.addView(src);
        return c;
    }

'''
new='''    private LinearLayout weatherCard(){
        TrainingSlot slot=nextWeatherSlot();
        if(WeatherEventPolicy.usesThreePoints(slot.allDay,slot.start,slot.end))return weatherMultiPointCard(slot);
        LinearLayout c=card(); c.setOrientation(LinearLayout.VERTICAL); c.setPadding(dp(16),dp(15),dp(16),dp(14));
        String[] x=weatherSummary(slot);
        c.addView(txt(x[0],11,WATER,true));
        LinearLayout row=new LinearLayout(this); row.setGravity(Gravity.CENTER_VERTICAL); row.setPadding(0,dp(5),0,dp(5)); c.addView(row);
        TextView icon=txtRaw(x[5],38,themeText(TEXT),false); icon.setGravity(Gravity.CENTER); row.addView(icon,new LinearLayout.LayoutParams(dp(58),dp(58)));
        LinearLayout info=new LinearLayout(this); info.setOrientation(LinearLayout.VERTICAL); info.setPadding(dp(7),0,0,0); row.addView(info,new LinearLayout.LayoutParams(0,-2,1));
        info.addView(txtRaw(x[1],15,MUTED,true));
        TextView main=txtRaw(x[2],21,TEXT,true); main.setPadding(0,dp(2),0,0); info.addView(main);
        TextView details=txtRaw(x[3],13,MUTED,false); details.setPadding(0,dp(5),0,0); c.addView(details);
        TextView src=txtRaw(x[4],10,Color.rgb(126,140,150),false); src.setPadding(0,dp(8),0,0); c.addView(src);
        return c;
    }

    private LinearLayout weatherMultiPointCard(TrainingSlot slot){
        LinearLayout c=card();
        c.setOrientation(LinearLayout.VERTICAL);
        c.setPadding(dp(14),dp(14),dp(14),dp(13));
        c.addView(txt("NÄCHSTER TERMIN",11,WATER,true));
        TextView title=txtRaw(slot.title==null||slot.title.isBlank()?ui("Vereinstermin"):slot.title,16,TEXT,true);
        title.setPadding(0,dp(5),0,dp(2));
        c.addView(title);
        c.addView(txtRaw(weatherSlotDateLabel(slot),12,MUTED,false));

        String raw=prefs.getString(PREF_WEATHER_CACHE,"");
        long updated=prefs.getLong(PREF_WEATHER_UPDATED,0L);
        String source=prefs.getString(PREF_WEATHER_SOURCE,"MeteoSwiss ICON via Open-Meteo");
        String provenance=ui(slot.fromCalendar?"Vereinskalender":"Regelplan")+" · "+weatherAge(source,updated);
        if(raw.trim().isEmpty()){
            TextView loading=txt("Wetter wird geladen …",14,MUTED,false);
            loading.setPadding(0,dp(11),0,0);
            c.addView(loading);
            TextView src=txtRaw(provenance,10,Color.rgb(126,140,150),false);
            src.setPadding(0,dp(8),0,0);
            c.addView(src);
            return c;
        }

        List<WeatherDaily.Hour> hours=weatherHours(raw);
        int[] targets=WeatherEventPolicy.targetHours(slot.allDay,slot.start,slot.end);
        List<WeatherDaily.Slot> values=WeatherDaily.slots(hours,slot.start.toLocalDate(),targets);
        LinearLayout slots=new LinearLayout(this);
        slots.setGravity(Gravity.TOP);
        slots.setBaselineAligned(false);
        slots.setPadding(0,dp(10),0,0);
        for(int index=0;index<values.size();index++){
            if(index>0){
                View divider=new View(this);
                divider.setBackgroundColor(darkMode?Color.rgb(63,76,85):Color.rgb(216,226,232));
                LinearLayout.LayoutParams dividerParams=new LinearLayout.LayoutParams(dp(1),dp(94));
                dividerParams.setMargins(dp(3),dp(4),dp(3),0);
                slots.addView(divider,dividerParams);
            }
            slots.addView(weatherDaySlotView(values.get(index)),new LinearLayout.LayoutParams(0,-2,1));
        }
        c.addView(slots,new LinearLayout.LayoutParams(-1,-2));

        List<WeatherDaily.Summary> summaries=WeatherDaily.summarize(hours,slot.start.toLocalDate(),1);
        if(!summaries.isEmpty()&&summaries.get(0).hasData()){
            TextView details=txtRaw(weatherDayDetails(summaries.get(0)),10,MUTED,false);
            details.setPadding(0,dp(8),0,0);
            c.addView(details);
        }else{
            TextView missing=txt("Für diesen Veranstaltungstag liegen noch keine Stundenwerte vor.",11,MUTED,false);
            missing.setPadding(0,dp(8),0,0);
            c.addView(missing);
        }
        TextView src=txtRaw(provenance,10,Color.rgb(126,140,150),false);
        src.setPadding(0,dp(8),0,0);
        c.addView(src);
        return c;
    }

'''
s=replace_once(s,old,new,'weather card with event multipoint mode')

s=replace_once(
    s,
    '        String daypart=targetHour==6?ui("Morgen"):targetHour==12?ui("Mittag"):targetHour==18?ui("Abend"):ui("Prognose");',
    '        String daypart=weatherDaypartLabel(targetHour);',
    'generic weather daypart labels'
)

marker='''        box.setContentDescription(time+", "+weatherCode(hour.weatherCode)+", "+temperature+", "+ui("Regen")+" "+hour.precipitationProbability+" Prozent");
        return box;
    }

'''
insert=marker+'''    private String weatherDaypartLabel(int hour){
        if(hour<11)return ui("Morgen");
        if(hour<17)return ui("Mittag");
        return ui("Abend");
    }

'''
s=replace_once(s,marker,insert,'weather daypart helper')

# General weather-event selection before the existing training-only helper.
anchor='''    private TrainingSlot nextTrainingSlot(){
'''
addition='''    private TrainingSlot nextWeatherSlot(){
        ZoneId zone=ZoneId.of("Europe/Zurich");
        ZonedDateTime now=ZonedDateTime.now(zone);
        TrainingSlot calendar=nextCalendarWeatherSlot(now);
        TrainingSlot regular=nextRegularTraining(now,zone);
        if(calendar==null)return regular;
        if(regular==null)return calendar;
        return !calendar.start.isAfter(regular.start)?calendar:regular;
    }

    private TrainingSlot nextCalendarWeatherSlot(ZonedDateTime now){
        Event best=null;
        ZonedDateTime limit=now.plusDays(21);
        for(Event event:events){
            if(event.start==null||event.start.isAfter(limit)||isCancelledEvent(event))continue;
            if(!eventEnd(event).isAfter(now))continue;
            if(best==null||event.start.isBefore(best.start))best=event;
        }
        return best==null?null:weatherSlotFromEvent(best);
    }

    private TrainingSlot weatherSlotFromEvent(Event event){
        ZonedDateTime start=event.start;
        ZonedDateTime end=eventEnd(event);
        if(!end.isAfter(start))end=event.allDay?start.plusDays(1):start.plusHours(1);
        return new TrainingSlot(start,end,true,event.title,event.allDay);
    }

'''+anchor
s=replace_once(s,anchor,addition,'general weather event selection')

# Generic date label and summary wording.
anchor='''    private boolean weatherHourMatches(String timestamp,TrainingSlot slot){
'''
addition='''    private String weatherSlotDateLabel(TrainingSlot slot){
        String date=localizedDateWords(cap(slot.start.format(DateTimeFormatter.ofPattern("EEEE, dd.MM.",Locale.GERMAN))));
        return slot.allDay?date+" · "+ui("ganztägig"):date+" · "+trainingTimeLabel(slot);
    }

'''+anchor
s=replace_once(s,anchor,addition,'weather slot date label')

start=s.index('    private String[] weatherSummary(){')
end=s.index('    private String uvLabel(',start)
old=s[start:end]
new='''    private String[] weatherSummary(TrainingSlot slot){
        String date=weatherSlotDateLabel(slot);
        if(slot.title!=null&&!slot.title.isBlank())date+="\\n"+slot.title;
        String raw=prefs.getString(PREF_WEATHER_CACHE,"");
        long updated=prefs.getLong(PREF_WEATHER_UPDATED,0L);
        String source=prefs.getString(PREF_WEATHER_SOURCE,"MeteoSwiss ICON via Open-Meteo");
        String provenance=ui(slot.fromCalendar?"Vereinskalender":"Regelplan")+" · "+weatherAge(source,updated);
        if(raw.trim().isEmpty())return new String[]{ui("NÄCHSTER TERMIN"),date,ui("Wetter wird geladen …"),ui("Prognose wird im Hintergrund aktualisiert."),provenance,"◌"};
        try{
            JSONObject hourly=new JSONObject(raw).getJSONObject("hourly");
            JSONArray times=hourly.getJSONArray("time"),temperatures=hourly.getJSONArray("temperature_2m"),probabilities=hourly.getJSONArray("precipitation_probability"),precipitation=hourly.getJSONArray("precipitation"),codes=hourly.getJSONArray("weather_code"),wind=hourly.getJSONArray("wind_speed_10m"),gusts=hourly.getJSONArray("wind_gusts_10m");
            JSONArray uv=hourly.optJSONArray("uv_index");
            double firstTemperature=Double.NaN,lastTemperature=Double.NaN,precipitationSum=0,windMax=0,gustMax=0,uvMax=Double.NaN;
            int probabilityMax=0,codeValue=-1,count=0;
            for(int i=0;i<times.length();i++){
                if(!weatherHourMatches(times.optString(i,""),slot))continue;
                double temperature=temperatures.optDouble(i,Double.NaN);
                if(count==0){firstTemperature=temperature;codeValue=codes.optInt(i,-1);}
                lastTemperature=temperature;
                probabilityMax=Math.max(probabilityMax,probabilities.optInt(i,0));
                precipitationSum+=Math.max(0,precipitation.optDouble(i,0));
                windMax=Math.max(windMax,wind.optDouble(i,0));
                gustMax=Math.max(gustMax,gusts.optDouble(i,0));
                if(uv!=null){double value=uv.optDouble(i,Double.NaN);if(Double.isFinite(value)&&(Double.isNaN(uvMax)||value>uvMax))uvMax=value;}
                count++;
            }
            if(count==0)return new String[]{ui("NÄCHSTER TERMIN"),date,ui("Noch keine Prognose"),ui("Für diesen Terminzeitraum liegen noch keine Stundenwerte vor."),provenance,"◌"};
            String temperatureText=Double.isNaN(firstTemperature)?"":String.format(Locale.GERMAN,"%.0f °C",firstTemperature);
            if(Double.isFinite(lastTemperature)&&Double.isFinite(firstTemperature)&&Math.abs(lastTemperature-firstTemperature)>=1.0)temperatureText+=String.format(Locale.GERMAN," → %.0f °C",lastTemperature);
            String main=temperatureText+(temperatureText.isEmpty()?"":" · ")+weatherCode(codeValue);
            String details=ui("Regen")+" "+probabilityMax+" % · "+String.format(Locale.GERMAN,"%.1f mm",precipitationSum)+"\\n"+ui("Wind")+" "+Math.round(windMax)+" km/h · "+ui("Böen")+" "+Math.round(gustMax)+" km/h";
            details+="\\nUV "+(Double.isFinite(uvMax)?String.format(Locale.GERMAN,"%.1f",uvMax)+" · "+uvLabel(uvMax):"–");
            return new String[]{ui("NÄCHSTER TERMIN"),date,main,details,provenance,weatherIcon(codeValue)};
        }catch(Exception e){
            return new String[]{ui("NÄCHSTER TERMIN"),date,ui("Gespeicherte Wetterdaten nicht lesbar"),ui("Letzter Stand bleibt erhalten, sobald wieder gültige Daten vorliegen."),provenance,"◌"};
        }
    }

'''
s=s[:start]+new+s[end:]

# Preserve all-day information for general calendar weather while keeping old training constructors compatible.
old='''    private static class TrainingSlot {
        final ZonedDateTime start,end;final boolean fromCalendar;final String title;
        TrainingSlot(ZonedDateTime start,ZonedDateTime end,boolean fromCalendar,String title){this.start=start;this.end=end;this.fromCalendar=fromCalendar;this.title=title;}
    }
'''
new='''    private static class TrainingSlot {
        final ZonedDateTime start,end;final boolean fromCalendar,allDay;final String title;
        TrainingSlot(ZonedDateTime start,ZonedDateTime end,boolean fromCalendar,String title){this(start,end,fromCalendar,title,false);}
        TrainingSlot(ZonedDateTime start,ZonedDateTime end,boolean fromCalendar,String title,boolean allDay){this.start=start;this.end=end;this.fromCalendar=fromCalendar;this.title=title;this.allDay=allDay;}
    }
'''
s=replace_once(s,old,new,'training slot all-day metadata')
path.write_text(s)

# --- Pure policy for 1-point vs 3-point event weather ---
Path("Android/app/src/main/java/ch/pfvr/internapp/WeatherEventPolicy.java").write_text('''package ch.pfvr.internapp;\n\nimport java.time.Duration;\nimport java.time.ZonedDateTime;\n\n/** Decides when event weather needs a morning/noon/evening style overview. */\nfinal class WeatherEventPolicy {\n    private static final long THREE_POINT_MINUTES=5L*60L;\n\n    private WeatherEventPolicy() {}\n\n    static boolean usesThreePoints(boolean allDay,ZonedDateTime start,ZonedDateTime end){\n        if(allDay)return true;\n        if(start==null||end==null||!end.isAfter(start))return false;\n        if(!start.toLocalDate().equals(end.toLocalDate()))return true;\n        return Duration.between(start,end).toMinutes()>=THREE_POINT_MINUTES;\n    }\n\n    static int[] targetHours(boolean allDay,ZonedDateTime start,ZonedDateTime end){\n        if(!usesThreePoints(allDay,start,end))return new int[0];\n        if(allDay||start==null||end==null||!start.toLocalDate().equals(end.toLocalDate()))return new int[]{6,12,18};\n        long minutes=Duration.between(start,end).toMinutes();\n        ZonedDateTime middle=start.plusMinutes(minutes/2L);\n        return new int[]{start.getHour(),middle.getHour(),end.getHour()};\n    }\n}\n''')

# --- Tests ---
Path("Android/app/src/test/java/ch/pfvr/internapp/WeatherEventPolicyTest.java").write_text('''package ch.pfvr.internapp;\n\nimport static org.junit.Assert.assertArrayEquals;\nimport static org.junit.Assert.assertFalse;\nimport static org.junit.Assert.assertTrue;\n\nimport java.time.ZoneId;\nimport java.time.ZonedDateTime;\n\nimport org.junit.Test;\n\npublic class WeatherEventPolicyTest {\n    private static final ZoneId ZONE=ZoneId.of("Europe/Zurich");\n\n    @Test public void shortTimedEventKeepsCompactIntervalWeather(){\n        ZonedDateTime start=ZonedDateTime.of(2026,9,14,18,30,0,0,ZONE);\n        ZonedDateTime end=ZonedDateTime.of(2026,9,14,20,0,0,0,ZONE);\n        assertFalse(WeatherEventPolicy.usesThreePoints(false,start,end));\n        assertArrayEquals(new int[0],WeatherEventPolicy.targetHours(false,start,end));\n    }\n\n    @Test public void longTimedEventUsesStartMiddleAndEndHours(){\n        ZonedDateTime start=ZonedDateTime.of(2026,9,12,8,0,0,0,ZONE);\n        ZonedDateTime end=ZonedDateTime.of(2026,9,12,18,0,0,0,ZONE);\n        assertTrue(WeatherEventPolicy.usesThreePoints(false,start,end));\n        assertArrayEquals(new int[]{8,13,18},WeatherEventPolicy.targetHours(false,start,end));\n    }\n\n    @Test public void allDayEventUsesMorningNoonEvening(){\n        ZonedDateTime start=ZonedDateTime.of(2026,9,20,0,0,0,0,ZONE);\n        ZonedDateTime end=start.plusDays(1);\n        assertTrue(WeatherEventPolicy.usesThreePoints(true,start,end));\n        assertArrayEquals(new int[]{6,12,18},WeatherEventPolicy.targetHours(true,start,end));\n    }\n}\n''')

Path("Android/app/src/test/java/ch/pfvr/internapp/WeatherEventSourceTest.java").write_text('''package ch.pfvr.internapp;\n\nimport static org.junit.Assert.assertFalse;\nimport static org.junit.Assert.assertTrue;\n\nimport java.nio.charset.StandardCharsets;\nimport java.nio.file.Files;\nimport java.nio.file.Path;\nimport java.nio.file.Paths;\n\nimport org.junit.Test;\n\npublic class WeatherEventSourceTest {\n    private static String source() throws Exception {\n        String relative="src/main/java/ch/pfvr/internapp/MainActivity.java";\n        Path[] candidates=new Path[]{Paths.get(relative),Paths.get("app",relative),Paths.get("Android","app",relative)};\n        for(Path candidate:candidates)if(Files.isRegularFile(candidate))return new String(Files.readAllBytes(candidate),StandardCharsets.UTF_8);\n        throw new IllegalStateException("MainActivity.java not found from "+System.getProperty("user.dir"));\n    }\n\n    @Test public void weatherUsesNextGeneralClubEventBeforeRegularTraining() throws Exception {\n        String activity=source();\n        assertTrue(activity.contains("tileGroup(\\\"Wetter zum nächsten Termin\\\",\\\"Prognose für den nächsten relevanten Vereinsanlass\\\")"));\n        int start=activity.indexOf("private TrainingSlot nextCalendarWeatherSlot");\n        int end=activity.indexOf("private TrainingSlot weatherSlotFromEvent",start);\n        assertTrue(start>=0&&end>start);\n        String selector=activity.substring(start,end);\n        assertTrue(selector.contains("for(Event event:events)"));\n        assertTrue(selector.contains("isCancelledEvent(event)"));\n        assertTrue(selector.contains("eventEnd(event).isAfter(now)"));\n        assertFalse(selector.contains("TrainingMatcher"));\n        assertTrue(activity.contains("return !calendar.start.isAfter(regular.start)?calendar:regular;"));\n        assertTrue(activity.contains("new TrainingSlot(start,end,true,event.title,event.allDay)"));\n    }\n\n    @Test public void longAndAllDayEventsRenderThreeForecastPoints() throws Exception {\n        String activity=source();\n        assertTrue(activity.contains("WeatherEventPolicy.usesThreePoints(slot.allDay,slot.start,slot.end)"));\n        assertTrue(activity.contains("WeatherEventPolicy.targetHours(slot.allDay,slot.start,slot.end)"));\n        assertTrue(activity.contains("WeatherDaily.slots(hours,slot.start.toLocalDate(),targets)"));\n        assertTrue(activity.contains("weatherDaySlotView(values.get(index))"));\n        assertTrue(activity.contains("ui(\\\"NÄCHSTER TERMIN\\\")"));\n        assertTrue(activity.contains("Für diesen Terminzeitraum liegen noch keine Stundenwerte vor."));\n        assertFalse(activity.contains("tileGroup(\\\"Trainingswetter\\\""));\n    }\n}\n''')

# --- Version and docs ---
path=Path("Android/app/build.gradle")
s=path.read_text()
s=replace_once(s,"versionCode 60","versionCode 61","version code")
s=replace_once(s,"versionName '0.12.3'","versionName '0.12.4'","version name")
path.write_text(s)

path=Path("PROJECT.md")
s=path.read_text()
old='''- Home mit nächstem Training und Wetter für den tatsächlichen Zeitraum.\n  - Bevorzugt wird ein passender Termin aus dem öffentlichen Vereinskalender.\n  - Der saisonale Trainingsplan dient als Fallback, wenn kein Kalendereintrag vorhanden ist.\n'''
new='''- Home mit Wetter zum nächsten relevanten Vereinsanlass.\n  - Der nächste laufende oder kommende, nicht abgesagte Termin aus dem öffentlichen Vereinskalender wird unabhängig vom Termin-Titel berücksichtigt, damit z. B. Wettfahren, JP-Prüfungen, Endfahren, Wanderungen und weitere gemeinsame Anlässe Wetter erhalten.\n  - Ein früher liegendes reguläres Training aus dem saisonalen Trainingsplan bleibt als Fallback/Ergänzung berücksichtigt, wenn es im Kalender nicht explizit geführt wird.\n  - Kurze Termine zeigen weiterhin die Prognose über den tatsächlichen Terminzeitraum. Ganztägige oder mindestens fünfstündige Termine zeigen drei Prognosepunkte; ganztägig/multitägig `06/12/18 Uhr`, lange eintägige Termine anhand Start/Mitte/Ende.\n'''
s=replace_once(s,old,new,'project general event weather')
path.write_text(s)

path=Path("STATUS.md")
s=path.read_text()
s=replace_once(s,"Stand: Testversion `0.12.3` · aktualisiert 2026-09-07.","Stand: Testversion `0.12.4` · aktualisiert 2026-09-09.",'status header')
anchor="## Aktueller Teststand\n\n"
entry="- `0.12.4` erweitert das bisherige Trainingswetter zu `Wetter zum nächsten Termin`: Der nächste laufende/kommende, nicht abgesagte öffentliche Vereinskalender-Termin wird unabhängig von Training-Schlüsselwörtern berücksichtigt; ein früheres reguläres Training aus dem Regelplan bleibt als Fallback erhalten. Kurze Termine nutzen weiter ihren echten Zeitraum. Ganztägige bzw. mindestens fünfstündige Anlässe erhalten drei Wetterpunkte: ganztägig/multitägig `Morgen 06 Uhr`, `Mittag 12 Uhr`, `Abend 18 Uhr`; lange eintägige Termine verwenden Start, zeitliche Mitte und Ende. So erhalten insbesondere JP-Prüfungen, Endfahren, Wanderungen, Wettfahren und weitere gemeinsame Anlässe eine passende Wettervorschau.\n"
if anchor not in s: raise SystemExit('STATUS anchor missing')
s=s.replace(anchor,anchor+entry,1)
path.write_text(s)
