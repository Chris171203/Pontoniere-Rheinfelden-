package ch.pfvr.internapp;

import org.json.JSONArray;
import org.json.JSONObject;
import org.junit.Test;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import static org.junit.Assert.*;

public class PublicPayloadTest {
    @Test public void emptyOrUnalignedWeatherNeverReplacesGoodCache() throws Exception {
        long now=LocalDateTime.parse("2026-09-28T12:00").atZone(ZoneId.of("Europe/Zurich")).toInstant().toEpochMilli();
        assertFalse(PublicPayload.validWeather("{\"hourly\":{}}",now));
        JSONObject hours=new JSONObject();
        hours.put("time",new JSONArray().put("2026-09-28T12:00"));
        for(String key:new String[]{"temperature_2m","precipitation_probability","precipitation","weather_code","wind_speed_10m","wind_gusts_10m"})hours.put(key,new JSONArray().put(12));
        JSONObject payload=new JSONObject().put("hourly",hours);
        assertTrue(PublicPayload.validWeather(payload.toString(),now));
        hours.put("temperature_2m",new JSONArray());
        assertFalse(PublicPayload.validWeather(payload.toString(),now));
    }

    @Test public void hydroRequiresUsableRecentMeasurement() throws Exception {
        long now=Instant.parse("2026-09-28T12:00:00Z").toEpochMilli();
        JSONObject observations=new JSONObject().put("data_live",new JSONArray());
        JSONObject payload=new JSONObject().put("data",new JSONObject().put("water",new JSONObject().put("observations",observations)));
        assertFalse(PublicPayload.validHydro(payload.toString(),"data_live",now));
        observations.getJSONArray("data_live").put(new JSONObject().put("parameterName","W").put("value",248.20).put("timestamp","2026-09-28T11:45:00Z"));
        assertTrue(PublicPayload.validHydro(payload.toString(),"data_live",now));
        assertFalse(PublicPayload.validHydro(payload.toString(),"data_live",now+3L*60L*60L*1000L));
    }
}
