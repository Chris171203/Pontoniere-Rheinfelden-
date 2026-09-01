package ch.pfvr.internapp;

import android.content.Context;
import android.content.SharedPreferences;

import androidx.annotation.NonNull;
import androidx.work.Worker;
import androidx.work.WorkerParameters;

import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.time.Instant;

/**
 * Refreshes public PFVR/BAFU/weather data into the same local cache used by MainActivity.
 * WorkManager execution is intentionally best-effort: Android may defer periodic work in Doze.
 */
public final class BackgroundRefreshWorker extends Worker {
    private static final String PREFS = "pfvr_prefs";
    private static final String PREF_ICS_CACHE = "ics_cache";
    private static final String PREF_ICS_UPDATED = "ics_updated";
    private static final String PREF_WEATHER_CACHE = "weather_cache";
    private static final String PREF_WEATHER_UPDATED = "weather_updated";
    private static final String PREF_WEATHER_SOURCE = "weather_source";
    private static final String PREF_HYDRO_CACHE = "hydro_cache";
    private static final String PREF_HYDRO_UPDATED = "hydro_updated";
    private static final String PREF_HYDRO_HISTORY_CACHE = "hydro_history_cache";
    private static final String PREF_HYDRO_HISTORY_UPDATED = "hydro_history_updated";

    private static final String ICS = "https://calendar.google.com/calendar/ical/a8mtko83nd27vsvp4i1cnpt3gs%40group.calendar.google.com/public/basic.ics";
    private static final String WEATHER_BASE = "https://api.open-meteo.com/v1/forecast?latitude=47.5544&longitude=7.7940&hourly=temperature_2m,precipitation_probability,precipitation,weather_code,wind_speed_10m,wind_gusts_10m,uv_index&timezone=Europe%2FZurich&forecast_days=8";

    public BackgroundRefreshWorker(@NonNull Context context, @NonNull WorkerParameters params) {
        super(context, params);
    }

    @NonNull
    @Override
    public Result doWork() {
        SharedPreferences prefs = getApplicationContext().getSharedPreferences(PREFS, Context.MODE_PRIVATE);
        long now = System.currentTimeMillis();

        if (stale(prefs, PREF_HYDRO_UPDATED, 15L * 60L * 1000L)) refreshHydroLive(prefs, now);
        if (stale(prefs, PREF_HYDRO_HISTORY_UPDATED, 60L * 60L * 1000L)) refreshHydroHistory(prefs, now);
        if (stale(prefs, PREF_WEATHER_UPDATED, 60L * 60L * 1000L)) refreshWeather(prefs, now);
        if (stale(prefs, PREF_ICS_UPDATED, 4L * 60L * 60L * 1000L)) refreshCalendar(prefs, now);

        // Individual source failures keep the previous cache. The next periodic run tries again.
        return Result.success();
    }

    private boolean stale(SharedPreferences prefs, String key, long maxAge) {
        long updated = prefs.getLong(key, 0L);
        return updated <= 0L || System.currentTimeMillis() - updated >= maxAge;
    }

    private void refreshCalendar(SharedPreferences prefs, long now) {
        try {
            String raw = httpGet(ICS, "text/calendar,text/plain,*/*");
            if (!raw.contains("BEGIN:VCALENDAR") || !raw.contains("BEGIN:VEVENT")) return;
            prefs.edit().putString(PREF_ICS_CACHE, raw).putLong(PREF_ICS_UPDATED, now).apply();
        } catch (Exception ignored) {}
    }

    private void refreshWeather(SharedPreferences prefs, long now) {
        try {
            String raw;
            String source;
            try {
                raw = httpGet(WEATHER_BASE + "&models=meteoswiss_icon_seamless", "application/json");
                source = "MeteoSwiss ICON via Open-Meteo";
            } catch (Exception first) {
                raw = httpGet(WEATHER_BASE, "application/json");
                source = "Open-Meteo Best Match";
            }
            new JSONObject(raw).getJSONObject("hourly");
            prefs.edit().putString(PREF_WEATHER_CACHE, raw).putLong(PREF_WEATHER_UPDATED, now).putString(PREF_WEATHER_SOURCE, source).apply();
        } catch (Exception ignored) {}
    }

    private void refreshHydroLive(SharedPreferences prefs, long now) {
        try {
            String dq = String.valueOf((char)34);
            String query = "{ water { observations { data_live(where:{stationNo:{_eq:" + dq + "2091" + dq + "}}) { stationNo parameterName timestamp value releaseStatus } } } }";
            String raw = bafuPost(query);
            JSONObject json = new JSONObject(raw);
            if (json.has("errors")) return;
            json.getJSONObject("data").getJSONObject("water").getJSONObject("observations").getJSONArray("data_live");
            prefs.edit().putString(PREF_HYDRO_CACHE, raw).putLong(PREF_HYDRO_UPDATED, now).apply();
        } catch (Exception ignored) {}
    }

    private void refreshHydroHistory(SharedPreferences prefs, long now) {
        try {
            String dq = String.valueOf((char)34);
            String from = Instant.now().minus(Duration.ofDays(7)).toString();
            String query = "{ water { observations { data_1hour_mean(where:{station:{no:{_eq:" + dq + "2091" + dq + "}},timestamp:{_gte:" + dq + from + dq + "}}) { parameterName timestamp value } } } }";
            String raw = bafuPost(query);
            JSONObject json = new JSONObject(raw);
            if (json.has("errors")) return;
            json.getJSONObject("data").getJSONObject("water").getJSONObject("observations").getJSONArray("data_1hour_mean");
            prefs.edit().putString(PREF_HYDRO_HISTORY_CACHE, raw).putLong(PREF_HYDRO_HISTORY_UPDATED, now).apply();
        } catch (Exception ignored) {}
    }

    private String bafuPost(String query) throws Exception {
        HttpURLConnection c = (HttpURLConnection) new URL("https://data.bafu.admin.ch/api").openConnection();
        try {
            c.setRequestMethod("POST");
            c.setDoOutput(true);
            c.setConnectTimeout(7000);
            c.setReadTimeout(12000);
            c.setRequestProperty("Content-Type", "application/json");
            c.setRequestProperty("Accept", "application/json");
            c.setRequestProperty("User-Agent", "PFVR-Rheinfelden-App/0.7.0");
            String body = new JSONObject().put("query", query).toString();
            try (OutputStream out = c.getOutputStream()) {
                out.write(body.getBytes(StandardCharsets.UTF_8));
            }
            if (c.getResponseCode() / 100 != 2) throw new Exception("HTTP " + c.getResponseCode());
            return read(c);
        } finally {
            c.disconnect();
        }
    }

    private String httpGet(String url, String accept) throws Exception {
        HttpURLConnection c = (HttpURLConnection) new URL(url).openConnection();
        try {
            c.setConnectTimeout(7000);
            c.setReadTimeout(12000);
            c.setUseCaches(true);
            c.setRequestProperty("Accept", accept);
            c.setRequestProperty("User-Agent", "PFVR-Rheinfelden-App/0.7.0");
            if (c.getResponseCode() / 100 != 2) throw new Exception("HTTP " + c.getResponseCode());
            return read(c);
        } finally {
            c.disconnect();
        }
    }

    private String read(HttpURLConnection c) throws Exception {
        try (BufferedReader br = new BufferedReader(new InputStreamReader(c.getInputStream(), StandardCharsets.UTF_8))) {
            StringBuilder sb = new StringBuilder();
            String line;
            while ((line = br.readLine()) != null) sb.append(line).append('\n');
            return sb.toString();
        }
    }
}
