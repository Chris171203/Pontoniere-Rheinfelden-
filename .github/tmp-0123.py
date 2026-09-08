from pathlib import Path


def replace_once(text, old, new, label):
    count=text.count(old)
    if count!=1:
        raise SystemExit(f"{label}: expected 1 occurrence, got {count}")
    return text.replace(old,new,1)

# Main weather UI
path=Path("Android/app/src/main/java/ch/pfvr/internapp/MainActivity.java")
s=path.read_text()
s=replace_once(
    s,
    'List<WeatherDaily.Slot> values=WeatherDaily.slots(hours,summary.date,10,14,18);',
    'List<WeatherDaily.Slot> values=WeatherDaily.slots(hours,summary.date,6,12,18);',
    'weather slots 6/12/18'
)
s=replace_once(
    s,
    '''        String time=String.format(Locale.GERMAN,"%02d Uhr",slot.targetTime.getHour());\n        TextView timeView=txtRaw(time,10,WATER,true);\n        timeView.setGravity(Gravity.CENTER);\n        box.addView(timeView,new LinearLayout.LayoutParams(-1,-2));\n''',
    '''        int hour=slot.targetTime.getHour();\n        String daypart=hour==6?ui("Morgen"):hour==12?ui("Mittag"):hour==18?ui("Abend"):ui("Prognose");\n        TextView daypartView=txtRaw(daypart,10,WATER,true);\n        daypartView.setGravity(Gravity.CENTER);\n        box.addView(daypartView,new LinearLayout.LayoutParams(-1,-2));\n        String time=String.format(Locale.GERMAN,"%02d Uhr",hour);\n        TextView timeView=txtRaw(time,9,MUTED,false);\n        timeView.setGravity(Gravity.CENTER);\n        box.addView(timeView,new LinearLayout.LayoutParams(-1,-2));\n''',
    'daypart labels'
)
path.write_text(s)

# Weather slot unit tests
path=Path("Android/app/src/test/java/ch/pfvr/internapp/WeatherDailyTest.java")
s=path.read_text()
s=replace_once(s,'hour(day,9,14,5,0.0,4,7,1.0,1),','hour(day,5,11,5,0.0,4,7,1.0,1),','weather slot early neighbor')
s=replace_once(s,'hour(day,10,15,10,0.0,5,8,2.0,2),','hour(day,6,12,10,0.0,5,8,2.0,2),','weather slot morning')
s=replace_once(s,'hour(day,14,23,35,0.3,10,18,6.0,2),','hour(day,12,23,35,0.3,10,18,6.0,2),','weather slot noon')
s=replace_once(s,'List<WeatherDaily.Slot> slots=WeatherDaily.slots(hours,day,10,14,18);','List<WeatherDaily.Slot> slots=WeatherDaily.slots(hours,day,6,12,18);','first slot call')
s=replace_once(s,'assertEquals(10,slots.get(0).targetTime.getHour());','assertEquals(6,slots.get(0).targetTime.getHour());','target hour assertion')
s=replace_once(s,'assertEquals(15.0,slots.get(0).hour.temperature,0.001);','assertEquals(12.0,slots.get(0).hour.temperature,0.001);','morning temperature assertion')
s=replace_once(s,'List<WeatherDaily.Hour> hours=List.of(hour(day,11,16,20,0.0,5,8,2.0,2));','List<WeatherDaily.Hour> hours=List.of(hour(day,7,16,20,0.0,5,8,2.0,2));','nearby-hour test data')
s=replace_once(s,'List<WeatherDaily.Slot> slots=WeatherDaily.slots(hours,day,10,14,18);','List<WeatherDaily.Slot> slots=WeatherDaily.slots(hours,day,6,12,18);','second slot call')
path.write_text(s)

# Source regression test
path=Path("Android/app/src/test/java/ch/pfvr/internapp/WeatherForecastSourceTest.java")
s=path.read_text()
s=replace_once(
    s,
    'assertTrue(activity.contains("WeatherDaily.slots(hours,summary.date,10,14,18)"));',
    'assertTrue(activity.contains("WeatherDaily.slots(hours,summary.date,6,12,18)"));',
    'source slot assertion'
)
insert='''        assertTrue(activity.contains("hour.precipitationProbability"));\n'''
replacement='''        assertTrue(activity.contains("hour.precipitationProbability"));\n        assertTrue(activity.contains("hour==6?ui(\\\"Morgen\\\"):hour==12?ui(\\\"Mittag\\\"):hour==18?ui(\\\"Abend\\\")"));\n'''
s=replace_once(s,insert,replacement,'daypart source assertion')
path.write_text(s)

# Version
path=Path("Android/app/build.gradle")
s=path.read_text()
s=replace_once(s,"versionCode 59","versionCode 60","version code")
s=replace_once(s,"versionName '0.12.2'","versionName '0.12.3'","version name")
path.write_text(s)

# STATUS
path=Path("STATUS.md")
s=path.read_text()
s=replace_once(s,"Stand: Testversion `0.12.2`","Stand: Testversion `0.12.3`","status version")
anchor="## Aktueller Teststand\n\n"
entry="- `0.12.3` verteilt die drei Prognosepunkte der 3-Tage-Wetterkachel gleichmässig über den Tag: `Morgen · 06 Uhr`, `Mittag · 12 Uhr` und `Abend · 18 Uhr`. Symbol, Temperatur und Regenwahrscheinlichkeit bleiben je Zeitpunkt sichtbar; Tages-Min/Max, Regenmenge, UV sowie Wind/Böen bleiben als Tageszusammenfassung darunter. Es wird weiterhin ausschließlich der vorhandene Stunden-Wettercache verwendet.\n"
if anchor not in s: raise SystemExit('STATUS anchor missing')
s=s.replace(anchor,anchor+entry,1)
path.write_text(s)

# PROJECT
path=Path("PROJECT.md")
s=path.read_text()
s=replace_once(
    s,
    "  - Zusätzlich gibt es eine anordenbare 3-Tage-Wetterkachel für Rheinfelden mit drei Tageszeitpunkten (10:00, 14:00 und 18:00 Uhr) inklusive Wettersymbol, Temperatur und Regenwahrscheinlichkeit. Tages-Min/Max, Regenmenge, Wind/Böen und UV-Maximum bleiben als Tageszusammenfassung erhalten. Sie nutzt denselben lokal gecachten Wetterabruf wie das Trainingswetter und erzeugt keinen zusätzlichen API-Request.\n",
    "  - Zusätzlich gibt es eine anordenbare 3-Tage-Wetterkachel für Rheinfelden mit drei gleichmässig verteilten Tageszeitpunkten (`Morgen · 06 Uhr`, `Mittag · 12 Uhr`, `Abend · 18 Uhr`) inklusive Wettersymbol, Temperatur und Regenwahrscheinlichkeit. Tages-Min/Max, Regenmenge, Wind/Böen und UV-Maximum bleiben als Tageszusammenfassung erhalten. Sie nutzt denselben lokal gecachten Wetterabruf wie das Trainingswetter und erzeugt keinen zusätzlichen API-Request.\n",
    'project weather times'
)
path.write_text(s)
