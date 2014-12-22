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
    
    public void setDeviceIdentifier(String deviceId)
    {
    	preferences.edit().putString(context.getString(R.string.preference_device_identifier_key), deviceId).commit();
    }

    public String getDeviceIdentifier() {
        String DeviceIdentifier = PrefUtils.getString(context, R.string.preference_device_identifier_key,
                R.string.preference_device_identifier_default);
        return DeviceIdentifier;
    }

    public String getServerGpsFixesPostPath() {
        return PrefUtils.getString(context, R.string.preference_server_gps_fixes_post_path, R.string.preference_server_gps_fixes_post_path);
    }
    
    public String getServerCheckinPath() {
        return PrefUtils.getString(context, R.string.preference_server_checkin_path, R.string.preference_server_checkin_path);
    }
    
    public String getServerCheckoutPath() {
    	return PrefUtils.getString(context, R.string.preference_server_checkout_path, R.string.preference_server_checkout_path);
    }
    
    public String getServerEventPath(String eventId) {
    	return context.getString(R.string.preference_server_event_path, "/events").replace("{event_id}", eventId);
    }

    public String getServerLeaderboardPath(String leaderboardName) {
    	return context.getString(R.string.preference_server_leaderboard_path, "/leaderboards").replace("{leaderboard_name}", leaderboardName);
    }
    
    public String getServerCompetitorPath(String competitorId) {
    	return context.getString(R.string.preference_server_competitor_path, "/competitors").replace("{competitor_id}", competitorId);
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
    
    public String getEventId() {
        return PrefUtils.getString(context, R.string.preference_eventid_key, R.string.preference_eventid_default);
    }
    
    public void setEventId(String id) {
        preferences.edit().putString(context.getString(R.string.preference_eventid_key), id).commit();
    }
    
    public String getCompetitorId() {
        return PrefUtils.getString(context, R.string.preference_competitor_key, R.string.preference_competitor_default);
    }
    
    public void setBatteryIsCharging(boolean batteryIsCharging)
    {
    	preferences.edit().putBoolean(context.getString(R.string.preference_battery_is_charging), batteryIsCharging);
    }
    
    public boolean getBatteryIsCharging()
    {
    	return preferences.getBoolean(context.getString(R.string.preference_battery_is_charging), false);
    }
    
    public void setCompetitorId(String id) {
        preferences.edit().putString(context.getString(R.string.preference_competitor_key), id).commit();
    }
    
    public boolean getEnergySavingEnabledByUser() {
    	return preferences.getBoolean(context.getString(R.string.preference_energy_saving_enabled_key), false);
    }
    
    public boolean getHeadingFromMagneticSensorPreferred() {
    	return preferences.getBoolean(context.getString(R.string.preference_heading_from_magnetic_key), true);
    }
    
    public void setTrackingTimerStarted(long milliseconds)
    {
    	preferences.edit().putLong(context.getString(R.string.preference_tracking_timer_started), milliseconds).commit();
    }
    
    public long getTrackingTimerStarted()
    {
    	return preferences.getLong(context.getString(R.string.preference_tracking_timer_started), 0);
    }
    
    public void setTrackerIsTracking(boolean isTracking)
    {
    	preferences.edit().putBoolean(context.getString(R.string.preference_tracker_is_tracking), isTracking).commit();
    }
    
    public boolean getTrackerIsTracking()
    {
    	return preferences.getBoolean(context.getString(R.string.preference_tracker_is_tracking), false);
    }
    
    public void setTrackerIsTrackingEventId(String eventId)
    {
    	preferences.edit().putString(context.getString(R.string.preference_tracker_is_tracking_event_id), eventId).commit();
    }
    
    public String getTrackerIsTrackingEventId()
    {
    	return preferences.getString(context.getString(R.string.preference_tracker_is_tracking_event_id), null);
    }
    
    public static boolean getPrintDatabaseOperationDebugMessages()
    {
    	return false;
    }
}
