package ch.pfvr.internapp;

import org.junit.Test;

import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class WeatherForecastSourceTest {
    private static String source() throws Exception {
        String relative="src/main/java/ch/pfvr/internapp/MainActivity.java";
        Path[] candidates=new Path[]{Paths.get(relative),Paths.get("app",relative),Paths.get("Android","app",relative)};
        for(Path candidate:candidates)if(Files.isRegularFile(candidate))return new String(Files.readAllBytes(candidate), StandardCharsets.UTF_8);
        throw new IllegalStateException("MainActivity.java not found from "+System.getProperty("user.dir"));
    }

    @Test public void threeDayTileReusesExistingWeatherCacheAndRequest() throws Exception {
        String activity=source();
        assertTrue(activity.contains("case \"home_weather_3day\":return homeThreeDayWeatherTile();"));
        assertTrue(activity.contains("List<WeatherDaily.Hour> hours=weatherHours(raw)"));
        assertTrue(activity.contains("WeatherDaily.slots(hours,summary.date,6,12,18)"));
        assertTrue(activity.contains("%02d Uhr"));
        assertTrue(activity.contains("hour.precipitationProbability"));
        assertTrue(activity.contains("weatherDaypartLabel(targetHour)"));
        assertTrue(activity.contains("if(hour<11)return ui(\"Morgen\")"));
        assertTrue(activity.contains("if(hour<17)return ui(\"Mittag\")"));
        assertTrue(activity.contains("return ui(\"Abend\")"));
        assertTrue(activity.contains("PREF_WEATHER_CACHE"));
        assertEquals(2,count(activity,"https://api.open-meteo.com/v1/forecast?"));
        assertTrue(activity.contains("supplementWeatherUv(raw)"));
        assertTrue(activity.contains("UV Open-Meteo Best Match"));
        for(String field:new String[]{"temperature_2m","precipitation_probability","precipitation","weather_code","wind_speed_10m","wind_gusts_10m","uv_index"}){
            assertTrue("missing weather field "+field,activity.contains(field));
        }
    }

    private static int count(String source,String token){
        int count=0,index=0;
        while((index=source.indexOf(token,index))>=0){count++;index+=token.length();}
        return count;
    }
}
