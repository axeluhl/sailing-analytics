package com.sap.sailing.racecommittee.app.utils;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.content.Context;
import android.content.Intent;
import android.util.Log;

import com.sap.sailing.android.shared.data.http.HttpGetRequest;
import com.sap.sailing.android.shared.util.BroadcastManager;
import com.sap.sailing.android.shared.util.NetworkHelper;
import com.sap.sailing.racecommittee.app.AppConstants;
import com.sap.sailing.racecommittee.app.AppPreferences;
import com.sap.sailing.racecommittee.app.data.DataManager;
import com.sap.sailing.racecommittee.app.data.DataStore;
import com.sap.sailing.racecommittee.app.data.OnlineDataManager;
import com.sap.sailing.racecommittee.app.data.ReadonlyDataManager;
import com.sap.sailing.racecommittee.app.domain.ManagedRace;
import com.sap.sailing.racecommittee.app.domain.impl.FleetIdentifierImpl;
import com.sap.sse.common.Util;

public class WindHelper {
    private static String TAG = WindHelper.class.getName();

    public static void isTrackedRace(final Context context, final ManagedRace race) {
        try {
            Util.Triple<String, String, String> triple = FleetIdentifierImpl.unescape(race.getId());
            String path = "/sailingserver/api/v1/events/"+ getEventId(context) + "/racestates";
            List<Util.Pair<String, Object>> params = new ArrayList<>();
            params.add(new Util.Pair<String, Object>("filterByLeaderboard", triple.getA()));
            URL serverUrl = UrlHelper.generateUrl(getBaseUrl(context), path, params);

            HttpGetRequest request = new HttpGetRequest(serverUrl, context);
            NetworkHelper.getInstance(context).executeHttpJsonRequestAsync(request, new NetworkHelper.NetworkHelperSuccessListener() {
                @Override
                public void performAction(JSONObject response) {
                    Intent notifyTrackedIntent = new Intent();
                    notifyTrackedIntent.putExtra(AppConstants.INTENT_ACTION_IS_TRACKING_EXTRA, checkResponseForTracking(response, race));
                    notifyTrackedIntent.setAction(AppConstants.INTENT_ACTION_IS_TRACKING);
                    BroadcastManager.getInstance(context).addIntent(notifyTrackedIntent);
                }
            }, new NetworkHelper.NetworkHelperFailureListener() {
                @Override
                public void performAction(NetworkHelper.NetworkHelperError e) {
                    Log.e(TAG, "Failed to contact server: " + e.getMessage());
                }
            });
        }
        catch (MalformedURLException e){
            Log.e(TAG, "Failed to generate url for server call: " + e.getMessage());
        }
    }

    private static String getBaseUrl(Context context){
        /* remove trailing slash on baseUrl if exists */
        String baseUrl = AppPreferences.on(context).getServerBaseURL();
        if( baseUrl != null && baseUrl.substring(baseUrl.length() - 1).equals("/")) {
            return baseUrl.substring(0, baseUrl.length() - 1);
        } else {
            return baseUrl;
        }
    }

    private static String getEventId(Context context){
        DataStore dataStore = DataManager.create(context).getDataStore();
        return "" + dataStore.getEventUUID();
    }

    private static boolean checkResponseForTracking(JSONObject response, ManagedRace race){
        boolean isTracked = false;
        Util.Triple<String, String, String> triple = FleetIdentifierImpl.unescape(race.getId());
        try {
            JSONArray raceStates = response.getJSONArray("raceStates");
            for(int index = 0; index < raceStates.length(); index++){
                JSONObject raceState = raceStates.getJSONObject(index);
                String leaderboardName = raceState.getString("leaderboardName");
                String fleetName = raceState.getString("fleetName");
                String raceName = raceState.getString("raceName");
                boolean matchingLeaderboardName = leaderboardName != null && leaderboardName.equals(triple.getA());
                boolean matchingFleetName = fleetName != null && fleetName.equals(triple.getC());
                boolean matchingRaceName = raceName != null && raceName.equals(race.getRaceColumnName());
                if (matchingLeaderboardName && matchingFleetName && matchingRaceName){
                    if (raceState.getBoolean("trackedRaceLinked")){
                        isTracked = true;
                        break;
                    }
                }
            }
        } catch (JSONException e) {
            Log.e(TAG, "Failed to parse contents of the server response: " + e.getMessage());
        }
        return isTracked;
    }

    public static String generateMapURL(Context context, ManagedRace race, boolean showWindCharts, boolean showStreamlets, boolean showSimulation, boolean showMapControls) {
        ReadonlyDataManager dataManager = OnlineDataManager.create(context);
        return dataManager.getMapUrl(AppPreferences.on(context).getServerBaseURL(), race, getEventId(context), showWindCharts, showStreamlets, showSimulation, showMapControls);
    }
}
