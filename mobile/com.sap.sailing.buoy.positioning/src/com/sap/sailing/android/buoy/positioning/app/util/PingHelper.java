package com.sap.sailing.android.buoy.positioning.app.util;

import android.content.Context;
import android.location.Location;
import com.sap.sailing.android.buoy.positioning.app.util.DatabaseHelper.GeneralDatabaseHelperException;
import com.sap.sailing.android.buoy.positioning.app.valueobjects.MarkInfo;
import com.sap.sailing.android.buoy.positioning.app.valueobjects.MarkPingInfo;
import com.sap.sailing.android.shared.data.LeaderboardInfo;
import com.sap.sailing.android.shared.logging.ExLog;
import com.sap.sailing.android.shared.services.sending.MessageSendingService;
import com.sap.sailing.android.shared.services.sending.ServerReplyCallback;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.InputStream;
import java.util.UUID;

public class PingHelper {
    private static String TAG = PingHelper.class.getName();

    public void sendPingToServer(final Context context, Location location, LeaderboardInfo leaderBoard, MarkInfo mark, Class<? extends ServerReplyCallback> callback) {
        AppPreferences prefs = new AppPreferences(context);
        try {
            JSONObject fixJson = new JSONObject();

            fixJson.put("timestamp", location.getTime());
            fixJson.put("longitude", location.getLongitude());
            fixJson.put("latitude", location.getLatitude());

            String postUrlStr = leaderBoard.serverUrl + prefs.getServerMarkPingPath(leaderBoard.name, mark.getId());

            context.startService(MessageSendingService.createMessageIntent(context, postUrlStr, null,
                    UUID.randomUUID(), fixJson.toString(), callback));

        } catch (JSONException ex) {
            ExLog.i(context, TAG, "Error while building ping json " + ex.getMessage());
        }
    }

    public Boolean storePingInDatabase(Context context, Location location, MarkInfo mark) {
        MarkPingInfo pingInfo = new MarkPingInfo();
        pingInfo.setMarkId(mark.getId());
        pingInfo.setLatitude("" + location.getLatitude());
        pingInfo.setLongitude("" + location.getLongitude());
        pingInfo.setAccuracy(location.getAccuracy());
        pingInfo.setTimestamp((int) location.getTime());
        try {
            DatabaseHelper.getInstance().storeMarkPing(context, pingInfo);
        } catch (GeneralDatabaseHelperException e) {
            return false;
        }
        return true;
    }
}
