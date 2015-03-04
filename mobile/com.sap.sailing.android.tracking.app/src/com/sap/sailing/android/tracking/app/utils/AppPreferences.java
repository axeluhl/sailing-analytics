package com.sap.sailing.android.tracking.app.utils;

import android.content.Context;
import android.content.SharedPreferences;

import com.sap.sailing.android.shared.util.PrefUtils;
import com.sap.sailing.android.tracking.app.R;
import com.sap.sailing.domain.abstractlog.AbstractLogEventAuthor;
import com.sap.sailing.domain.abstractlog.impl.LogEventAuthorImpl;

public class AppPreferences {
    protected final Context context;
    protected final SharedPreferences preferences;
    public static final AbstractLogEventAuthor raceLogEventAuthor = new LogEventAuthorImpl("Tracking App", 0);
    
    public AppPreferences(Context context) {
        this.context = context;
        //multi process mode so that services read consistent values
        this.preferences = context.getSharedPreferences(AppPreferences.class.getName(), Context.MODE_MULTI_PROCESS);
    }
    
    public void setDeviceIdentifier(String deviceId)
    {
    	preferences.edit().putString(context.getString(R.string.preference_device_identifier_key), deviceId).commit();
    }

    public String getDeviceIdentifier() {
        return UniqueDeviceUuid.getUniqueId(context);
    }
    
    public String getServerUploadTeamImagePath() {
    	 return PrefUtils.getString(context, R.string.preference_server_team_image_upload_path, R.string.preference_server_team_image_upload_path);
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
    	preferences.edit().putBoolean(context.getString(R.string.preference_battery_is_charging), batteryIsCharging).commit();
    }
    
    public boolean getBatteryIsCharging()
    {
    	return preferences.getBoolean(context.getString(R.string.preference_battery_is_charging), false);
    }
    
    public void setCompetitorId(String id) {
        preferences.edit().putString(context.getString(R.string.preference_competitor_key), id).commit();
    }

    public void setEnergySavingEnabledByUser(boolean newValue) {
        preferences.edit().putBoolean(context.getString(R.string.preference_energy_saving_enabled_key), newValue).commit();
    }
    
    public boolean getEnergySavingEnabledByUser() {
    	return preferences.getBoolean(context.getString(R.string.preference_energy_saving_enabled_key), false);
    }

    public void setDisplayHeadingWithSubtractedDeclination(boolean newValue) {
        preferences.edit().putBoolean(context.getString(R.string.preference_heading_with_declination_subtracted_key), newValue).commit();
    }
    
    public boolean getDisplayHeadingWithSubtractedDeclination() {
    	return preferences.getBoolean(context.getString(R.string.preference_heading_with_declination_subtracted_key), true);
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
    
    public void setTrackerIsTrackingCheckinDigest(String checkinDigest)
    {
    	preferences.edit().putString(context.getString(R.string.preference_tracker_is_tracking_checkin_digest), checkinDigest).commit();
    }
    
    public String getTrackerIsTrackingCheckinDigest()
    {
    	return preferences.getString(context.getString(R.string.preference_tracker_is_tracking_checkin_digest), null);
    }
    
    public static boolean getPrintDatabaseOperationDebugMessages()
    {
    	return false;
    }
    
    public String getLastScannedQRCode()
    {
    	return preferences.getString(context.getString(R.string.preference_last_scanned_qr_code), null);
    }
    
    public void setLastScannedQRCode(String lastQRCode)
    {
    	preferences.edit().putString(context.getString(R.string.preference_last_scanned_qr_code), lastQRCode).commit();
    }
    
    public void setMessageResendInterval(int interval)
    {
    	preferences.edit().putInt(context.getString(R.string.preference_messageResendIntervalMillis_key), interval).commit();
    }
}