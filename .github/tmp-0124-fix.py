from pathlib import Path


def replace_once(text, old, new, label):
    count=text.count(old)
    if count!=1:
        raise SystemExit(f"{label}: expected 1 occurrence, got {count}")
    return text.replace(old,new,1)

path=Path("Android/app/src/test/java/ch/pfvr/internapp/ExternalContentLocalizationSourceTest.java")
s=path.read_text()
s=replace_once(s,'assertTrue(source.contains("ui(\\"NÄCHSTES TRAINING\\")"));','assertTrue(source.contains("ui(\\"NÄCHSTER TERMIN\\")"));','localized next-event weather heading')
path.write_text(s)

path=Path("Android/app/src/test/java/ch/pfvr/internapp/WeatherForecastSourceTest.java")
s=path.read_text()
s=replace_once(
    s,
    'assertTrue(activity.contains("targetHour==6?ui(\\"Morgen\\"):targetHour==12?ui(\\"Mittag\\"):targetHour==18?ui(\\"Abend\\")"));',
    'assertTrue(activity.contains("weatherDaypartLabel(targetHour)"));\n        assertTrue(activity.contains("if(hour<11)return ui(\\"Morgen\\")"));\n        assertTrue(activity.contains("if(hour<17)return ui(\\"Mittag\\")"));\n        assertTrue(activity.contains("return ui(\\"Abend\\")"));',
    'weather daypart helper source assertion'
)
path.write_text(s)
