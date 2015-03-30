package com.sap.sailing.android.buoy.positioning.app.util;

import java.util.UUID;

import org.json.JSONException;
import org.json.JSONObject;

import android.content.Context;
import android.location.Location;

import com.sap.sailing.android.buoy.positioning.app.util.DatabaseHelper.GeneralDatabaseHelperException;
import com.sap.sailing.android.buoy.positioning.app.valueobjects.MarkInfo;
import com.sap.sailing.android.buoy.positioning.app.valueobjects.MarkPingInfo;
import com.sap.sailing.android.shared.data.LeaderboardInfo;
import com.sap.sailing.android.shared.logging.ExLog;
import com.sap.sailing.android.shared.services.sending.MessageSendingService;

public class PingHelper {
	private static String TAG = PingHelper.class.getName();
	

	public void sendPingToServer(final Context context, Location location, LeaderboardInfo leaderBoard, MarkInfo mark) {
		AppPreferences prefs = new AppPreferences(context);
		try {
			JSONObject fixJson = new JSONObject();

			fixJson.put("timestamp", location.getTime());
			fixJson.put("longitude", location.getLongitude());
			fixJson.put("latitude", location.getLatitude());
			storePingInDatabase(context, location, mark);

			String postUrlStr = leaderBoard.serverUrl
					+ prefs.getServerMarkPingPath(leaderBoard.name, mark.getId());

            context.startService(MessageSendingService.createMessageIntent(context, postUrlStr,
                    null, UUID.randomUUID(), fixJson.toString(), null));


		} catch (JSONException ex) {
			ExLog.i(context, TAG,
					"Error while building ping json " + ex.getMessage());
		} catch (GeneralDatabaseHelperException e) {
			e.printStackTrace();
		}
	}


	public void storePingInDatabase(Context context, Location location,
			MarkInfo mark) throws GeneralDatabaseHelperException {
		MarkPingInfo pingInfo = new MarkPingInfo();
		pingInfo.setMarkId(mark.getId());
		pingInfo.setLattitude(""+ location.getLatitude());
		pingInfo.setLongitude("" + location.getLongitude());
		pingInfo.setAccuracy(location.getAccuracy());
		pingInfo.setTimestamp((int) location.getTime());
		DatabaseHelper.getInstance().storeMarkPing(context, pingInfo);
	}
}
