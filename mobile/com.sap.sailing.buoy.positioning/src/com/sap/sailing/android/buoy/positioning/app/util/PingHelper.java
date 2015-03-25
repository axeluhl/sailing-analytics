package com.sap.sailing.android.buoy.positioning.app.util;

import java.net.MalformedURLException;
import java.net.URL;

import org.json.JSONException;
import org.json.JSONObject;

import android.content.Context;
import android.location.Location;
import android.widget.Toast;

import com.sap.sailing.android.buoy.positioning.app.util.DatabaseHelper.GeneralDatabaseHelperException;
import com.sap.sailing.android.buoy.positioning.app.valueobjects.MarkInfo;
import com.sap.sailing.android.buoy.positioning.app.valueobjects.MarkPingInfo;
import com.sap.sailing.android.shared.data.LeaderboardInfo;
import com.sap.sailing.android.shared.data.http.HttpJsonPostRequest;
import com.sap.sailing.android.shared.logging.ExLog;
import com.sap.sailing.android.shared.util.NetworkHelper;
import com.sap.sailing.android.shared.util.NetworkHelper.NetworkHelperError;
import com.sap.sailing.android.shared.util.NetworkHelper.NetworkHelperSuccessListener;

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
			
			HttpJsonPostRequest request = new HttpJsonPostRequest(new URL(postUrlStr),
					fixJson.toString(), context);
            NetworkHelper.getInstance(context)
                    .executeHttpJsonRequestAsnchronously(request, new NetworkHelperSuccessListener() {
						
						@Override
						public void performAction(JSONObject arg0) {
							Toast.makeText(context, "DATA SEND TO SERVER", Toast.LENGTH_LONG).show();
							
						}
					}, new NetworkHelper.NetworkHelperFailureListener() {
						
						@Override
						public void performAction(NetworkHelperError arg0) {
							Toast.makeText(context, "FAILURE SERVER WAS NOT NOTIFIED", Toast.LENGTH_LONG).show();
							
						}
					});;

			//context.startService(MessageSendingService.createMessageIntent(context,
			//		postUrlStr, null, UUID.randomUUID(), fixJson.toString(), null));

		} catch (JSONException ex) {
			ExLog.i(context, TAG,
					"Error while building ping json " + ex.getMessage());
		} catch (GeneralDatabaseHelperException e) {
			e.printStackTrace();
		} catch (MalformedURLException e) {
			// TODO Auto-generated catch block
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
