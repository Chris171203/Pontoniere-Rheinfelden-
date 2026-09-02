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

/** Refreshes public PFVR, weather and BAFU data into the app's local cache. */
public final class BackgroundRefreshWorker extends Worker {
    private static final String PREFS = "pfvr_prefs";
    private static final String PREF_ICS_CACHE = "ics_cache";
    private static final String PREF_ICS_UPDATED = "ics_updated";
    private static final String PREF_WEATHER_CACHE = "weather_cache";
    private static final String PREF_WEATHER_UPDATED = "weather_updated";
    private static final String PREF_WEATHER_SOURCE = "weather_source";

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

        for (HydroStation station : HydroStation.values()) {
            if (stale(prefs, station.liveUpdatedKey(), 15L * 60L * 1000L)) refreshHydroLive(prefs, station, now);
            if (stale(prefs, station.fineUpdatedKey(), 30L * 60L * 1000L)) refreshHydroFine(prefs, station, now);
            if (stale(prefs, station.historyUpdatedKey(), 60L * 60L * 1000L)) refreshHydroHistory(prefs, station, now);
        }
        if (stale(prefs, PREF_WEATHER_UPDATED, 60L * 60L * 1000L)) refreshWeather(prefs, now);
        if (stale(prefs, PREF_ICS_UPDATED, 4L * 60L * 60L * 1000L)) refreshCalendar(prefs, now);

        // Individual source failures preserve the previous successful cache.
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

    private void refreshHydroLive(SharedPreferences prefs, HydroStation station, long now) {
        String quote = String.valueOf((char) 34);
        String query = "{ water { observations { data_live(where:{stationNo:{_eq:" + quote + station.id + quote + "}}) { stationNo parameterName timestamp value releaseStatus } } } }";
        refreshHydroSeries(prefs, now, query, "data_live", station.liveCacheKey(), station.liveUpdatedKey());
    }

    private void refreshHydroFine(SharedPreferences prefs, HydroStation station, long now) {
        String quote = String.valueOf((char) 34);
        String from = Instant.now().minus(Duration.ofHours(26)).toString();
        String query = "{ water { observations { data_10min_mean(where:{station:{no:{_eq:" + quote + station.id + quote + "}},timestamp:{_gte:" + quote + from + quote + "}}) { parameterName timestamp value } } } }";
        refreshHydroSeries(prefs, now, query, "data_10min_mean", station.fineCacheKey(), station.fineUpdatedKey());
    }

    private void refreshHydroHistory(SharedPreferences prefs, HydroStation station, long now) {
        String quote = String.valueOf((char) 34);
        String from = Instant.now().minus(Duration.ofDays(8)).toString();
        String query = "{ water { observations { data_1hour_mean(where:{station:{no:{_eq:" + quote + station.id + quote + "}},timestamp:{_gte:" + quote + from + quote + "}}) { parameterName timestamp value } } } }";
        refreshHydroSeries(prefs, now, query, "data_1hour_mean", station.historyCacheKey(), station.historyUpdatedKey());
    }

    private void refreshHydroSeries(SharedPreferences prefs, long now, String query, String arrayName, String cacheKey, String updatedKey) {
        try {
            String raw = bafuPost(query);
            JSONObject json = new JSONObject(raw);
            if (json.has("errors")) return;
            json.getJSONObject("data").getJSONObject("water").getJSONObject("observations").getJSONArray(arrayName);
            prefs.edit().putString(cacheKey, raw).putLong(updatedKey, now).apply();
        } catch (Exception ignored) {}
    }

    private String bafuPost(String query) throws Exception {
        HttpURLConnection connection = (HttpURLConnection) new URL("https://data.bafu.admin.ch/api").openConnection();
        try {
            connection.setRequestMethod("POST");
            connection.setDoOutput(true);
            connection.setConnectTimeout(7000);
            connection.setReadTimeout(12000);
            connection.setRequestProperty("Content-Type", "application/json");
            connection.setRequestProperty("Accept", "application/json");
            connection.setRequestProperty("User-Agent", "PFVR-Rheinfelden-App/" + BuildConfig.VERSION_NAME);
            String body = new JSONObject().put("query", query).toString();
            try (OutputStream out = connection.getOutputStream()) {
                out.write(body.getBytes(StandardCharsets.UTF_8));
            }
            if (connection.getResponseCode() / 100 != 2) throw new Exception("HTTP " + connection.getResponseCode());
            return read(connection);
        } finally {
            connection.disconnect();
        }
    }

    private String httpGet(String url, String accept) throws Exception {
        HttpURLConnection connection = (HttpURLConnection) new URL(url).openConnection();
        try {
            connection.setConnectTimeout(7000);
            connection.setReadTimeout(12000);
            connection.setUseCaches(true);
            connection.setRequestProperty("Accept", accept);
            connection.setRequestProperty("User-Agent", "PFVR-Rheinfelden-App/" + BuildConfig.VERSION_NAME);
            if (connection.getResponseCode() / 100 != 2) throw new Exception("HTTP " + connection.getResponseCode());
            return read(connection);
        } finally {
            connection.disconnect();
        }
    }

    private String read(HttpURLConnection connection) throws Exception {
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(connection.getInputStream(), StandardCharsets.UTF_8))) {
            StringBuilder out = new StringBuilder();
            String line;
            while ((line = reader.readLine()) != null) out.append(line).append('\n');
            return out.toString();
        }
    }
}
