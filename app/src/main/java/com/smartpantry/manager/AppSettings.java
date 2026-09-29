package com.smartpantry.manager;

import android.content.Context;
import android.content.SharedPreferences;

// settings are saved in SharedPreferences, not in SQLite
public class AppSettings {

    private static final String FILE = "pantry_settings";
    private static final String ALERTS = "expiry_alerts";
    private static final String UNIT = "default_unit";

    private final SharedPreferences prefs;

    public AppSettings(Context context) {
        prefs = context.getSharedPreferences(FILE, Context.MODE_PRIVATE);
    }

    public boolean expiryAlertsEnabled() {
        return prefs.getBoolean(ALERTS, false);
    }

    public void setExpiryAlertsEnabled(boolean enabled) {
        prefs.edit().putBoolean(ALERTS, enabled).apply();
    }

    public String getDefaultUnit() {
        String unit = prefs.getString(UNIT, "g");
        if (unit == null || unit.isEmpty()) {
            return "g";
        }
        return unit;
    }

    public void setDefaultUnit(String unit) {
        prefs.edit().putString(UNIT, unit).apply();
    }
}
