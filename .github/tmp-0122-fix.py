from pathlib import Path

path=Path("Android/app/src/test/java/ch/pfvr/internapp/WeatherForecastSourceTest.java")
text=path.read_text()
bad='        assertTrue(activity.contains("hour.precipitationProbability+" %""));\n'
good='        assertTrue(activity.contains("hour.precipitationProbability"));\n'
if text.count(bad)!=1:
    raise SystemExit(f"expected one broken assertion, got {text.count(bad)}")
path.write_text(text.replace(bad,good,1))
