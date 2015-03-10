package com.sap.sailing.android.tracking.app.utils;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.net.Uri;
import android.widget.Toast;

import com.sap.sailing.android.shared.data.http.HttpGetRequest;
import com.sap.sailing.android.shared.logging.ExLog;
import com.sap.sailing.android.tracking.app.R;
import com.sap.sailing.android.tracking.app.ui.activities.CheckinDataActivity;
import com.sap.sailing.android.tracking.app.valueobjects.CheckinData;
import com.sap.sailing.domain.common.racelog.tracking.DeviceMappingConstants;
import com.sap.sailing.domain.racelogtracking.DeviceIdentifier;
import com.sap.sailing.domain.racelogtracking.impl.SmartphoneUUIDIdentifierImpl;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.UnsupportedEncodingException;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLEncoder;
import java.security.NoSuchAlgorithmException;
import java.util.UUID;

public class CheckinManager {

    private final static String TAG = CheckinManager.class.getName();
    private CheckinData checkinData;
    private CheckinDataActivity activity;
    private AppPreferences prefs;
    private String url;

    public  CheckinManager(String url, CheckinDataActivity activity){
        this.activity = activity;
        this.url = url;
        prefs = new AppPreferences(activity);
    }

    public void callServerAndGenerateCheckinData(){
        Uri uri = Uri.parse(url);
        setCheckinData(null);
        // TODO: assuming scheme is http, is this valid?
        String scheme = uri.getScheme();
        if (scheme != "http" && scheme != "https") {
            scheme = "http";
        }

        final String uriStr = uri.toString();
        final String server = scheme + "://" + uri.getHost();
        final int port = (uri.getPort() == -1) ? 80 : uri.getPort();
        final String hostWithPort = server + ":" + port;

        String leaderboardNameFromQR;
        try {
            leaderboardNameFromQR = URLEncoder.encode(uri.getQueryParameter(DeviceMappingConstants.URL_LEADERBOARD_NAME), "UTF-8")
                    .replace("+", "%20");
        } catch (UnsupportedEncodingException e) {
            ExLog.e(activity, TAG, "Failed to encode leaderboard name: " + e.getMessage());
            leaderboardNameFromQR = "";
        } catch (NullPointerException e) {
            ExLog.e(activity, TAG, "Invalid Barcode (no leaderboard-name set): " + e.getMessage());
            Toast.makeText(activity, activity.getString(R.string.error_invalid_qr_code), Toast.LENGTH_LONG).show();
            return;
        }

        final String competitorId = uri.getQueryParameter(DeviceMappingConstants.URL_COMPETITOR_ID_AS_STRING);
        final String checkinURLStr = hostWithPort
                + prefs.getServerCheckinPath().replace("{leaderboard-name}", leaderboardNameFromQR);
        final String eventId = uri.getQueryParameter(DeviceMappingConstants.URL_EVENT_ID);
        final String leaderboardName = leaderboardNameFromQR;

        final DeviceIdentifier deviceUuid = new SmartphoneUUIDIdentifierImpl(UUID.fromString(UniqueDeviceUuid
                .getUniqueId(activity)));

        // There are 5 Stages after the QR-Code scan is complete:
        // 1. Get Event
        // 2. Get Leaderboard
        // 3. Get Competitor
        // 4. Let user confirm that the information is correct
        // 5. Checkin


        final String getEventUrl = hostWithPort + prefs.getServerEventPath(eventId);
        final String getLeaderboardUrl = hostWithPort + prefs.getServerLeaderboardPath(leaderboardName);
        final String getCompetitorUrl = hostWithPort + prefs.getServerCompetitorPath(competitorId);

        activity.showProgressDialog(R.string.please_wait, R.string.getting_leaderboard);

        try {
            HttpGetRequest getLeaderboardRequest = new HttpGetRequest(new URL(getLeaderboardUrl), activity);
            NetworkHelper.getInstance(activity).executeHttpJsonRequestAsnchronously(getLeaderboardRequest,
                    new NetworkHelper.NetworkHelperSuccessListener() {

                        @Override
                        public void performAction(JSONObject response) {
                            // TODO Auto-generated method stub

                            activity.dismissProgressDialog();

                            final String leaderboardName;

                            try {
                                leaderboardName = response.getString("name");
                            } catch (JSONException e) {
                                ExLog.e(activity, TAG, "Error getting data from call on URL: " + getLeaderboardUrl
                                        + ", Error: " + e.getMessage());
                                activity.dismissProgressDialog();
                                displayAPIErrorRecommendRetry();
                                return;
                            }

                            activity.showProgressDialog(R.string.please_wait, R.string.getting_event);

                            HttpGetRequest getEventRequest;
                            try {
                                getEventRequest = new HttpGetRequest(new URL(getEventUrl), activity);
                                NetworkHelper.getInstance(activity).executeHttpJsonRequestAsnchronously(
                                        getEventRequest, new NetworkHelper.NetworkHelperSuccessListener() {

                                            @Override
                                            public void performAction(JSONObject response) {
                                                activity.dismissProgressDialog();

                                                final String eventId;
                                                final String eventName;
                                                final String eventStartDateStr;
                                                final String eventEndDateStr;
                                                final String eventFirstImageUrl;

                                                try {
                                                    eventId = response.getString("id");
                                                    eventName = response.getString("name");
                                                    eventStartDateStr = response.getString("startDate");
                                                    eventEndDateStr = response.getString("endDate");

                                                    JSONArray imageUrls = response.getJSONArray("imageURLs");

                                                    if (imageUrls.length() > 0) {
                                                        eventFirstImageUrl = imageUrls.getString(0);
                                                    } else {
                                                        eventFirstImageUrl = null;
                                                    }

                                                } catch (JSONException e) {
                                                    ExLog.e(activity, TAG, "Error getting data from call on URL: "
                                                            + getEventUrl + ", Error: " + e.getMessage());
                                                    displayAPIErrorRecommendRetry();
                                                    return;
                                                }

                                                activity.showProgressDialog(R.string.please_wait,
                                                        R.string.getting_competitor);

                                                HttpGetRequest getCompetitorRequest;
                                                try {
                                                    getCompetitorRequest = new HttpGetRequest(
                                                            new URL(getCompetitorUrl), activity);
                                                    NetworkHelper.getInstance(activity)
                                                            .executeHttpJsonRequestAsnchronously(getCompetitorRequest,
                                                                    new NetworkHelper.NetworkHelperSuccessListener() {

                                                                        @Override
                                                                        public void performAction(JSONObject response) {
                                                                            activity.dismissProgressDialog();

                                                                            final String competitorName;
                                                                            final String competitorId;
                                                                            final String competitorSailId;
                                                                            final String competitorNationality;
                                                                            final String competitorCountryCode;

                                                                            try {
                                                                                competitorName = response
                                                                                        .getString("name");
                                                                                competitorId = response.getString("id");
                                                                                competitorSailId = response
                                                                                        .getString("sailID");
                                                                                competitorNationality = response
                                                                                        .getString("nationality");
                                                                                competitorCountryCode = response
                                                                                        .getString("countryCode");
                                                                            } catch (JSONException e) {
                                                                                ExLog.e(activity,
                                                                                        TAG,
                                                                                        "Error getting data from call on URL: "
                                                                                                + getCompetitorUrl
                                                                                                + ", Error: "
                                                                                                + e.getMessage());
                                                                                displayAPIErrorRecommendRetry();
                                                                                return;
                                                                            }
                                                                            CheckinData data = new CheckinData();
                                                                            data.competitorName = competitorName;
                                                                            data.competitorId = competitorId;
                                                                            data.competitorSailId = competitorSailId;
                                                                            data.competitorNationality = competitorNationality;
                                                                            data.competitorCountryCode = competitorCountryCode;
                                                                            data.eventId = eventId;
                                                                            data.eventName = eventName;
                                                                            data.eventStartDateStr = eventStartDateStr;
                                                                            data.eventEndDateStr = eventEndDateStr;
                                                                            data.eventFirstImageUrl = eventFirstImageUrl;
                                                                            data.eventServerUrl = hostWithPort;
                                                                            data.checkinURL = checkinURLStr;
                                                                            data.leaderboardName = leaderboardName;
                                                                            data.deviceUid = deviceUuid
                                                                                    .getStringRepresentation();
                                                                            try {
                                                                                data.setCheckinDigestFromString(uriStr);
                                                                                setCheckinData(data);
                                                                            } catch (UnsupportedEncodingException e) {
                                                                                ExLog.e(activity,
                                                                                        TAG,
                                                                                        "Failed to get generate digest of qr-code string ("
                                                                                                + uriStr + "). "
                                                                                                + e.getMessage());
                                                                                activity.dismissProgressDialog();
                                                                                displayAPIErrorRecommendRetry();
                                                                                return;
                                                                            } catch (NoSuchAlgorithmException e) {
                                                                                ExLog.e(activity,
                                                                                        TAG,
                                                                                        "Failed to get generate digest of qr-code string ("
                                                                                                + uriStr + "). "
                                                                                                + e.getMessage());
                                                                                activity.dismissProgressDialog();
                                                                                displayAPIErrorRecommendRetry();
                                                                                return;
                                                                            }
                                                                        }
                                                                    }, new NetworkHelper.NetworkHelperFailureListener() {
                                                                        @Override
                                                                        public void performAction(NetworkHelper.NetworkHelperError e) {
                                                                            ExLog.e(activity, TAG,
                                                                                    "Failed to get competitor from API: "
                                                                                            + e.getMessage());
                                                                            activity.dismissProgressDialog();
                                                                            displayAPIErrorRecommendRetry();
                                                                            return;
                                                                        }
                                                                    });

                                                } catch (MalformedURLException e2) {
                                                    ExLog.e(activity, TAG,
                                                            "Error: Failed to perform checking due to a MalformedURLException: "
                                                                    + e2.getMessage());
                                                }
                                            }
                                        }, new NetworkHelper.NetworkHelperFailureListener() {

                                            @Override
                                            public void performAction(NetworkHelper.NetworkHelperError e) {
                                                ExLog.e(activity, TAG,
                                                        "Failed to get leaderboard from API: " + e.getMessage());
                                                activity.dismissProgressDialog();
                                                displayAPIErrorRecommendRetry();
                                                return;
                                            }
                                        });
                            } catch (MalformedURLException e1) {
                                ExLog.e(activity,
                                        TAG,
                                        "Error: Failed to perform checking due to a MalformedURLException: "
                                                + e1.getMessage());
                            }
                        }
                    }, new NetworkHelper.NetworkHelperFailureListener() {

                        @Override
                        public void performAction(NetworkHelper.NetworkHelperError e) {
                            ExLog.e(activity, TAG, "Failed to get event from API: " + e.getMessage());
                            activity.dismissProgressDialog();
                            displayAPIErrorRecommendRetry();
                        }
                    });

        } catch (MalformedURLException e) {
            ExLog.e(activity, TAG,
                    "Error: Failed to perform checking due to a MalformedURLException: " + e.getMessage());
        }
    }
    public void setCheckinData(CheckinData data){
        checkinData = data;
        if(getCheckinData() != null) {
            activity.onCheckinDataAvailable(getCheckinData());
        }
    }

    public CheckinData getCheckinData(){
        return checkinData;
    }

    /**
     * Shows a pop-up-dialog that informs the user than an API-call has failed and recommends a retry.
     */
    private void displayAPIErrorRecommendRetry() {
        AlertDialog.Builder builder = new AlertDialog.Builder(activity);
        builder.setMessage(activity.getString(R.string.notify_user_api_call_failed));
        builder.setCancelable(true);
        builder.setPositiveButton(activity.getString(R.string.ok), new DialogInterface.OnClickListener() {

            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.cancel();
            }

        });
        AlertDialog alert = builder.create();
        alert.show();
    }

    public interface CheckinDataHandler{
        public void onCheckinDataAvailable(CheckinData data);
    }
}
