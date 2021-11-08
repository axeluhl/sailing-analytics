package com.sap.sailing.android.buoy.positioning.app.util;

import com.sap.sailing.android.buoy.positioning.app.R;
import com.sap.sailing.android.shared.util.BaseAppPreferences;

import android.content.Context;

public class AppPreferences extends BaseAppPreferences {

    public AppPreferences(Context context) {
        super(context);
    }

    public String getServerLeaderboardPath(String leaderboardName) {
        return context.getString(R.string.preference_server_leaderboard_path).replace("{leaderboard_name}",
                leaderboardName);
    }

    public String getServerMarkPath(String leaderboardName) {
        String cleanLeaderBoardName = leaderboardName.replaceAll(" ", "%20");
        return context.getString(R.string.preference_server_marks_path).replace("{leaderboard_name}",
                cleanLeaderBoardName);
    }

    public String getServerMarkPingPath(String leaderBoardName, String markIdAsString) {
        String cleanLeaderBoardName = leaderBoardName.replaceAll(" ", "%20");
        return context.getString(R.string.preference_server_gps_fixes_post_path)
                .replace("{leaderboard_name}", cleanLeaderBoardName).replace("{mark-id}", markIdAsString);
    }

    public void setDataRefreshInterval(long interval) {
        preferences.edit().putLong(context.getString(R.string.preference_data_refresh_interval_seconds_key), interval)
                .apply();
        MarkerUtils.withContext(context).restartMarkerService();
    }

    public long getDataRefreshInterval() {
        return preferences.getLong(context.getString(R.string.preference_data_refresh_interval_seconds_key),
                context.getResources().getInteger(R.integer.preference_data_refresh_interval_seconds_default));
    }
}
