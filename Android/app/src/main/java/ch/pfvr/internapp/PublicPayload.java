package ch.pfvr.internapp;

import org.json.JSONArray;
import org.json.JSONObject;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;

/** Keep the last usable public cache when a source returns an empty or malformed payload. */
final class PublicPayload {
    private PublicPayload() {}

    static boolean validWeather(String raw,long now){
        try{
            JSONObject hourly=new JSONObject(raw).getJSONObject("hourly");
            JSONArray times=hourly.getJSONArray("time");
            if(times.length()==0)return false;
            String[] required={"temperature_2m","precipitation_probability","precipitation","weather_code","wind_speed_10m","wind_gusts_10m"};
            for(String key:required)if(hourly.getJSONArray(key).length()!=times.length())return false;
            JSONArray uv=hourly.optJSONArray("uv_index");
            if(uv!=null&&uv.length()!=times.length())return false;
            LocalDateTime minimum=Instant.ofEpochMilli(now).atZone(ZoneId.of("Europe/Zurich")).toLocalDateTime().minusHours(6);
            LocalDateTime maximum=minimum.plusDays(10);
            for(int i=0;i<times.length();i++){
                LocalDateTime time=LocalDateTime.parse(times.getString(i));
                if(time.isBefore(minimum)||time.isAfter(maximum))continue;
                if(Double.isFinite(hourly.getJSONArray("temperature_2m").optDouble(i,Double.NaN))
                        &&Double.isFinite(hourly.getJSONArray("precipitation").optDouble(i,Double.NaN))
                        &&Double.isFinite(hourly.getJSONArray("wind_speed_10m").optDouble(i,Double.NaN)))return true;
            }
        }catch(Exception ignored){}
        return false;
    }

    static boolean validHydro(String raw,String arrayName,long now){
        try{
            JSONObject root=new JSONObject(raw);
            if(root.has("errors"))return false;
            JSONArray rows=root.getJSONObject("data").getJSONObject("water").getJSONObject("observations").getJSONArray(arrayName);
            long oldest=now-("data_live".equals(arrayName)?2L*60L*60L*1000L:9L*24L*60L*60L*1000L);
            for(int i=0;i<rows.length();i++){
                JSONObject row=rows.optJSONObject(i);
                if(row==null)continue;
                String parameter=row.optString("parameterName","");
                if(!("Q".equals(parameter)||"W".equals(parameter)||"WT".equals(parameter)))continue;
                double value=row.optDouble("value",Double.NaN);
                if(!Double.isFinite(value))continue;
                long timestamp=Instant.parse(row.getString("timestamp")).toEpochMilli();
                if(timestamp>=oldest&&timestamp<=now+10L*60L*1000L)return true;
            }
        }catch(Exception ignored){}
        return false;
    }
}
