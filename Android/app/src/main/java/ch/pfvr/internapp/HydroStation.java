package ch.pfvr.internapp;

/** BAFU Rhine stations available as configurable home cards. */
enum HydroStation {
    BASEL_RHEINHALLE("2289", "Basel, Rheinhalle", false, 400f, 2550f, 3700f),
    RHEINFELDEN("2091", "Rheinfelden", true, 400f, 2500f, 3600f);

    final String id;
    final String label;
    final boolean supportsTemperature;
    final float defaultLow;
    final float defaultWarn;
    final float defaultAlarm;

    HydroStation(String id, String label, boolean supportsTemperature, float defaultLow, float defaultWarn, float defaultAlarm) {
        this.id = id;
        this.label = label;
        this.supportsTemperature = supportsTemperature;
        this.defaultLow = defaultLow;
        this.defaultWarn = defaultWarn;
        this.defaultAlarm = defaultAlarm;
    }

    static HydroStation from(String value, HydroStation fallback) {
        if (value != null) {
            for (HydroStation station : values()) {
                if (station.id.equals(value) || station.name().equals(value)) return station;
            }
        }
        return fallback;
    }

    String liveCacheKey() {
        return this == RHEINFELDEN ? "hydro_cache" : "hydro_2289_cache";
    }

    String liveUpdatedKey() {
        return this == RHEINFELDEN ? "hydro_updated" : "hydro_2289_updated";
    }

    String fineCacheKey() {
        return this == RHEINFELDEN ? "hydro_fine_cache" : "hydro_2289_fine_cache";
    }

    String fineUpdatedKey() {
        return this == RHEINFELDEN ? "hydro_fine_updated" : "hydro_2289_fine_updated";
    }

    String historyCacheKey() {
        return this == RHEINFELDEN ? "hydro_history_cache" : "hydro_2289_history_cache";
    }

    String historyUpdatedKey() {
        return this == RHEINFELDEN ? "hydro_history_updated" : "hydro_2289_history_updated";
    }

    String lowPreferenceKey() {
        return "river_" + id + "_low";
    }

    String warnPreferenceKey() {
        return "river_" + id + "_warn";
    }

    String alarmPreferenceKey() {
        return "river_" + id + "_alarm";
    }

    String stationUrl() {
        return "https://www.hydrodaten.admin.ch/de/seen-und-fluesse/stationen-und-daten/" + id;
    }
}
