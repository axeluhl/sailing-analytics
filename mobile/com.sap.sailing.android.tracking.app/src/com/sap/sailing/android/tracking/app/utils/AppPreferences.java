package com.sap.sailing.android.tracking.app.utils;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

import com.sap.sailing.android.shared.util.PrefUtils;
import com.sap.sailing.android.tracking.app.R;
import com.sap.sailing.domain.racelog.RaceLogEventAuthor;
import com.sap.sailing.domain.racelog.impl.RaceLogEventAuthorImpl;

public class AppPreferences {
    protected final Context context;
    protected final SharedPreferences preferences;
    public static final RaceLogEventAuthor raceLogEventAuthor = new RaceLogEventAuthorImpl("Tracking App", 0);
    
    public AppPreferences(Context context) {
        this.context = context;
        this.preferences = PreferenceManager.getDefaultSharedPreferences(context);
    }

    public String getDeviceIdentifier() {
        return PrefUtils.getString(context, R.string.preference_device_identifier_key,
                R.string.preference_device_identifier_default);
    }

    public String getServerURL() {
        return PrefUtils.getString(context, R.string.preference_server_url_key, R.string.preference_server_url_default);
    }
    
    public String getServerGpsFixesPostPath() {
        return PrefUtils.getString(context, R.string.preference_server_gps_fixes_post_path, R.string.preference_server_gps_fixes_post_path);
    }

    public int getGPSFixInterval() {
        //EditTextPreference saves value as string, even if android:inputType="number" is set
        String value = PrefUtils.getString(context, R.string.preference_gps_fix_interval_ms_key, R.string.preference_gps_fix_interval_ms_default);
        return value == null ? -1 : Integer.valueOf(value);
    }
    
    public int getGPSFixFastestInterval() {
        //EditTextPreference saves value as string, even if android:inputType="number" is set
        String value = PrefUtils.getString(context, R.string.preference_gps_fix_fastest_interval_ms_key, R.string.preference_gps_fastest_fix_interval_ms_default);
        return value == null ? -1 : Integer.valueOf(value);
    }
    
    public void setServerURL(String serverUrl) {
        preferences.edit().putString(context.getString(R.string.preference_server_url_key), serverUrl).commit();
    }
    
    public String getEventId() {
        return PrefUtils.getString(context, R.string.preference_eventid_key, R.string.preference_eventid_default);
    }
    
    public void setEventId(String id) {
        preferences.edit().putString(context.getString(R.string.preference_eventid_key), id).commit();
    }
    
    public String getCompetitorId() {
        return PrefUtils.getString(context, R.string.preference_competitor_key, R.string.preference_competitor_default);
    }
    
    public void setCompetitorId(String id) {
        preferences.edit().putString(context.getString(R.string.preference_competitor_key), id).commit();
    }
}
