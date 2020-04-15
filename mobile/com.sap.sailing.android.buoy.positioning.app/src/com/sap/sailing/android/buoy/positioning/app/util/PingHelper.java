package com.sap.sailing.android.buoy.positioning.app.util;

import android.content.Context;
import android.location.Location;
import android.net.Uri;

import com.sap.sailing.android.buoy.positioning.app.util.DatabaseHelper.GeneralDatabaseHelperException;
import com.sap.sailing.android.buoy.positioning.app.valueobjects.MarkInfo;
import com.sap.sailing.android.buoy.positioning.app.valueobjects.MarkPingInfo;
import com.sap.sailing.android.shared.data.LeaderboardInfo;
import com.sap.sailing.android.shared.logging.ExLog;
import com.sap.sailing.android.shared.services.sending.MessageSendingService;
import com.sap.sailing.android.shared.services.sending.ServerReplyCallback;
import com.sap.sailing.domain.common.tracking.impl.FlatSmartphoneUuidAndGPSFixMovingJsonSerializer;
import com.sap.sailing.domain.common.tracking.impl.GPSFixImpl;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.UUID;

import static com.sap.sailing.domain.common.racelog.tracking.DeviceMappingConstants.URL_SECRET;

public class PingHelper {
    private static String TAG = PingHelper.class.getName();

    public void sendPingToServer(final Context context, Location location, LeaderboardInfo leaderBoard, MarkInfo mark,
            String secret, Class<? extends ServerReplyCallback> callback) {
        AppPreferences prefs = new AppPreferences(context);
        try {
            JSONObject fixJson = new JSONObject();
            fixJson.put(FlatSmartphoneUuidAndGPSFixMovingJsonSerializer.TIME_MILLIS, location.getTime());
            fixJson.put(FlatSmartphoneUuidAndGPSFixMovingJsonSerializer.LON_DEG, location.getLongitude());
            fixJson.put(FlatSmartphoneUuidAndGPSFixMovingJsonSerializer.LAT_DEG, location.getLatitude());

            Uri uri = Uri.parse(leaderBoard.serverUrl);
            Uri.Builder builder = new Uri.Builder()
                    .scheme(uri.getScheme())
                    .authority(uri.getAuthority())
                    .appendEncodedPath(prefs.getServerMarkPingPath(leaderBoard.name, mark.getId().toString()));
            if (secret != null) {
                builder.appendQueryParameter(URL_SECRET, secret);
            }
            String postUrlStr = builder.build().toString();
            context.startService(MessageSendingService.createMessageIntent(context, postUrlStr, null, UUID.randomUUID(),
                    fixJson.toString(), callback));

        } catch (JSONException ex) {
            ExLog.i(context, TAG, "Error while building ping json " + ex.getMessage());
        }
    }

    public Boolean storePingInDatabase(Context context, Location location, MarkInfo mark) {
        MarkPingInfo pingInfo = new MarkPingInfo(mark.getId(),
                GPSFixImpl.create(location.getLongitude(), location.getLatitude(), location.getTime()),
                location.getAccuracy());
        try {
            DatabaseHelper.getInstance().storeMarkPing(context, pingInfo);
        } catch (GeneralDatabaseHelperException e) {
            return false;
        }
        return true;
    }
}
