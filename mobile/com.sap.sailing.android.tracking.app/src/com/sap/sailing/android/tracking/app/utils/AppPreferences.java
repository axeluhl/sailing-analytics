package com.sap.sailing.android.tracking.app.utils;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;

import com.sap.sailing.android.shared.util.BaseAppPreferences;
import com.sap.sailing.android.shared.util.PrefUtils;
import com.sap.sailing.android.tracking.app.R;
import com.sap.sailing.android.tracking.app.services.TrackingService;
import com.sap.sailing.domain.abstractlog.AbstractLogEventAuthor;
import com.sap.sailing.domain.abstractlog.impl.LogEventAuthorImpl;

import android.content.Context;
import android.content.SharedPreferences;

public class AppPreferences extends BaseAppPreferences {

    private final SharedPreferences pref;

    public AppPreferences(Context context) {
        super(context);

        pref = context.getSharedPreferences("failed_uploads", Context.MODE_PRIVATE);
    }

    public static final AbstractLogEventAuthor raceLogEventAuthor = new LogEventAuthorImpl("Tracking App", 0);

    public String getServerUploadTeamImagePath() {
        return PrefUtils.getString(context, R.string.preference_server_team_image_upload_path,
                R.string.preference_server_team_image_upload_path);
    }

    public String getServerGpsFixesPostPath() {
        return PrefUtils.getString(context, R.string.preference_server_gps_fixes_post_path,
                R.string.preference_server_gps_fixes_post_path);
    }

    public String getServerCheckinPath() {
        return PrefUtils.getString(context, R.string.preference_server_checkin_path,
                R.string.preference_server_checkin_path);
    }

    public String getServerCheckoutPath() {
        return PrefUtils.getString(context, R.string.preference_server_checkout_path,
                R.string.preference_server_checkout_path);
    }

    public String getServerEventPath(String eventId) {
        return context.getString(R.string.preference_server_event_path).replace("{event_id}", eventId);
    }

    public String getServerEventUrl(String eventId) {
        return context.getString(R.string.preference_server_event_url, eventId);
    }

    public String getServerLeaderboardPath(String leaderboardName) {
        return context.getString(R.string.preference_server_leaderboard_path).replace("{leaderboard_name}",
                leaderboardName);
    }

    public String getServerCompetitorPath(String competitorId) throws UnsupportedEncodingException {
        return context.getString(R.string.preference_server_competitor_path).replace("{competitor_id}",
                URLEncoder.encode(competitorId, "UTF-8").replaceAll("\\+", "%20"));
    }

    public String getServerCompetitorTeamPath(String competitorId) {
        return context.getString(R.string.preference_server_team_info_path).replace("{competitor_id}", competitorId);
    }

    public String getServerMarkPath(String leaderboardName, String markId) {
        String path = context.getString(R.string.preference_server_mark_path);
        return path.replace("{leaderboardName}", leaderboardName).replace("{mark_id}", markId);
    }

    public String getServerBoatPath(String boatId) {
        String path = context.getString(R.string.preference_server_boat_path);
        return path.replace("{boat_id}", boatId);
    }

    public int getGPSFixInterval() {
        // EditTextPreference saves value as string, even if android:inputType="number" is set
        String value = PrefUtils.getString(context, R.string.preference_gps_fix_interval_ms_key,
                R.string.preference_gps_fix_interval_ms_default);
        return value == null ? -1 : Integer.valueOf(value);
    }

    public String getEventId() {
        return PrefUtils.getString(context, R.string.preference_eventid_key, R.string.preference_eventid_default);
    }

    public void setEventId(String id) {
        preferences.edit().putString(context.getString(R.string.preference_eventid_key), id).apply();
    }

    public String getCompetitorId() {
        return PrefUtils.getString(context, R.string.preference_competitor_key, R.string.preference_competitor_default);
    }

    public void setBatteryIsCharging(boolean batteryIsCharging) {
        preferences.edit().putBoolean(context.getString(R.string.preference_battery_is_charging), batteryIsCharging)
                .apply();
    }

    public boolean getBatteryIsCharging() {
        return preferences.getBoolean(context.getString(R.string.preference_battery_is_charging), false);
    }

    public void setCompetitorId(String id) {
        preferences.edit().putString(context.getString(R.string.preference_competitor_key), id).apply();
    }

    public void setDisplayHeadingWithSubtractedDeclination(boolean newValue) {
        preferences.edit()
                .putBoolean(context.getString(R.string.preference_heading_with_declination_subtracted_key), newValue)
                .apply();
    }

    public boolean getDisplayHeadingWithSubtractedDeclination() {
        return preferences.getBoolean(context.getString(R.string.preference_heading_with_declination_subtracted_key),
                true);
    }

    public void setTrackingTimerStarted(long milliseconds) {
        preferences.edit().putLong(context.getString(R.string.preference_tracking_timer_started), milliseconds).apply();
    }

    public long getTrackingTimerStarted() {
        return preferences.getLong(context.getString(R.string.preference_tracking_timer_started), 0);
    }

    public void setTrackerIsTracking(boolean isTracking) {
        preferences.edit().putBoolean(context.getString(R.string.preference_tracker_is_tracking), isTracking).apply();
    }

    public boolean getTrackerIsTracking() {
        return preferences.getBoolean(context.getString(R.string.preference_tracker_is_tracking), false);
    }

    public void setTrackerIsTrackingCheckinDigest(String checkinDigest) {
        preferences.edit()
                .putString(context.getString(R.string.preference_tracker_is_tracking_checkin_digest), checkinDigest)
                .apply();
    }

    public String getTrackerIsTrackingCheckinDigest() {
        return preferences.getString(context.getString(R.string.preference_tracker_is_tracking_checkin_digest), null);
    }

    public void setMessageResendIntervalInMillis(int intervalInMillis) {
        preferences.edit()
                .putInt(context.getString(R.string.preference_messageResendIntervalMillis_key), intervalInMillis)
                .apply();
    }

    /**
     * Returns the message sending interval in milliseconds
     */
    public int getMessageSendingIntervalInMillis() {
        return preferences.getInt(context.getString(R.string.preference_messageResendIntervalMillis_key),
                /* default */ TrackingService.UPDATE_INTERVAL_IN_MILLIS_DEFAULT);
    }

    public boolean hasFailedUpload(String key) {
        return pref.getBoolean(key, false);
    }

    public void setFailedUpload(String key) {
        pref.edit().putBoolean(key, true).apply();
    }

    public void removeFailedUpload(String key) {
        pref.edit().remove(key).apply();
    }

}