package com.sap.sailing.android.buoy.positioning.app.util;

import android.content.Context;

import com.sap.sailing.android.buoy.positioning.app.R;
import com.sap.sailing.android.shared.util.BaseAppPreferences;

public class AppPreferences extends BaseAppPreferences {

	public AppPreferences(Context context) {
		super(context);
	}

	public String getServerLeaderboardPath(String leaderboardName) {
		return context.getString(R.string.preference_server_leaderboard_path,
				"/leaderboards").replace("{leaderboard_name}", leaderboardName);
	}

	public static boolean getPrintDatabaseOperationDebugMessages() {
		return false;
	}
}
