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

    public CheckinManager(String url, CheckinDataActivity activity){
        this.activity = activity;
        this.url = url;
        prefs = new AppPreferences(activity);
    }

    public void callServerAndGenerateCheckinData(){
        Uri uri = Uri.parse(url);

        // TODO: assuming scheme is http, is this valid?
        String scheme = uri.getScheme();
        if (scheme != "http" && scheme != "https") {
            scheme = "http";
        }

        final URLData urlData = extractRequestParametersFromUri(uri, scheme);
        if (urlData == null)
        {
            return;
        }

        activity.showProgressDialog(R.string.please_wait, R.string.getting_leaderboard);

        try {
            HttpGetRequest getLeaderboardRequest = new HttpGetRequest(new URL(urlData.getLeaderboardUrl), activity);
            getLeaderBoardFromServer(urlData, getLeaderboardRequest);

        } catch (MalformedURLException e) {
            ExLog.e(activity, TAG,
                    "Error: Failed to perform checking due to a MalformedURLException: " + e.getMessage());
        }
    }

    private URLData extractRequestParametersFromUri(Uri uri, String scheme) {
        URLData urlData = new URLData();
        urlData.uriStr = uri.toString();
        urlData.server = scheme + "://" + uri.getHost();
        urlData.port = (uri.getPort() == -1) ? 80 : uri.getPort();
        urlData.hostWithPort = urlData.server + ":" + urlData.port;

        String leaderboardNameFromQR = "";
        try {
            leaderboardNameFromQR = URLEncoder.encode(uri.getQueryParameter(DeviceMappingConstants.URL_LEADERBOARD_NAME), "UTF-8")
                    .replace("+", "%20");
        } catch (UnsupportedEncodingException e) {
            ExLog.e(activity, TAG, "Failed to encode leaderboard name: " + e.getMessage());
        } catch (NullPointerException e) {
            ExLog.e(activity, TAG, "Invalid Barcode (no leaderboard-name set): " + e.getMessage());
            Toast.makeText(activity, activity.getString(R.string.error_invalid_qr_code), Toast.LENGTH_LONG).show();
            urlData = null;
        }
        if(urlData != null) {
            urlData.competitorId = uri.getQueryParameter(DeviceMappingConstants.URL_COMPETITOR_ID_AS_STRING);
            urlData.checkinURLStr = urlData.hostWithPort
                    + prefs.getServerCheckinPath().replace("{leaderboard-name}", leaderboardNameFromQR);
            urlData.eventId = uri.getQueryParameter(DeviceMappingConstants.URL_EVENT_ID);
            urlData.leaderboardName = leaderboardNameFromQR;

            urlData.deviceUuid = new SmartphoneUUIDIdentifierImpl(UUID.fromString(UniqueDeviceUuid
                    .getUniqueId(activity)));

            urlData.getEventUrl = urlData.hostWithPort + prefs.getServerEventPath(urlData.eventId);
            urlData.getLeaderboardUrl = urlData.hostWithPort + prefs.getServerLeaderboardPath(urlData.leaderboardName);
            urlData.getCompetitorUrl = urlData.hostWithPort + prefs.getServerCompetitorPath(urlData.competitorId);
        }
        return urlData;
    }

    private void getLeaderBoardFromServer(final URLData urlData, HttpGetRequest getLeaderboardRequest) {
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
                            ExLog.e(activity, TAG, "Error getting data from call on URL: " + urlData.getLeaderboardUrl
                                    + ", Error: " + e.getMessage());
                            activity.dismissProgressDialog();
                            displayAPIErrorRecommendRetry();
                            return;
                        }

                        activity.showProgressDialog(R.string.please_wait, R.string.getting_event);

                        HttpGetRequest getEventRequest;
                        try {
                            getEventRequest = new HttpGetRequest(new URL(urlData.getEventUrl), activity);
                            getEventFromServer(leaderboardName, getEventRequest, urlData);
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
    }

    private void getEventFromServer(final String leaderboardName, HttpGetRequest getEventRequest, final URLData urlData) {
        NetworkHelper.getInstance(activity).executeHttpJsonRequestAsnchronously(
                getEventRequest, new NetworkHelper.NetworkHelperSuccessListener() {

                    @Override
                    public void performAction(JSONObject response) {
                        activity.dismissProgressDialog();

                        try {
                            urlData.eventId = response.getString("id");
                            urlData.eventName = response.getString("name");
                            urlData.eventStartDateStr = response.getString("startDate");
                            urlData.eventEndDateStr = response.getString("endDate");

                            JSONArray imageUrls = response.getJSONArray("imageURLs");

                            if (imageUrls.length() > 0) {
                                urlData.eventFirstImageUrl = imageUrls.getString(0);
                            } else {
                                urlData.eventFirstImageUrl = null;
                            }

                        } catch (JSONException e) {
                            ExLog.e(activity, TAG, "Error getting data from call on URL: "
                                    + urlData.getEventUrl + ", Error: " + e.getMessage());
                            displayAPIErrorRecommendRetry();
                            return;
                        }

                        activity.showProgressDialog(R.string.please_wait,
                                R.string.getting_competitor);

                        HttpGetRequest getCompetitorRequest;
                        try {
                            getCompetitorRequest = new HttpGetRequest(
                                    new URL(urlData.getCompetitorUrl), activity);
                            getCompetitorFromServer(getCompetitorRequest, urlData, leaderboardName);

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
    }

    private void getCompetitorFromServer(HttpGetRequest getCompetitorRequest, final URLData urlData, final String leaderboardName) {
        NetworkHelper.getInstance(activity)
                .executeHttpJsonRequestAsnchronously(getCompetitorRequest,
                        new NetworkHelper.NetworkHelperSuccessListener() {

                            @Override
                            public void performAction(JSONObject response) {
                                activity.dismissProgressDialog();

                                try {
                                    urlData.competitorName = response
                                            .getString("name");
                                    urlData.competitorId = response.getString("id");
                                    urlData.competitorSailId = response
                                            .getString("sailID");
                                    urlData.competitorNationality = response
                                            .getString("nationality");
                                    urlData.competitorCountryCode = response
                                            .getString("countryCode");
                                } catch (JSONException e) {
                                    ExLog.e(activity,
                                            TAG,
                                            "Error getting data from call on URL: "
                                                    + urlData.getCompetitorUrl
                                                    + ", Error: "
                                                    + e.getMessage());
                                    displayAPIErrorRecommendRetry();
                                    return;
                                }
                                saveCheckinDataAndNotifyListeners(urlData, leaderboardName);
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
    }

    private void saveCheckinDataAndNotifyListeners(URLData urlData, String leaderboardName) {
        CheckinData data = new CheckinData();
        data.competitorName = urlData.competitorName;
        data.competitorId = urlData.competitorId;
        data.competitorSailId = urlData.competitorSailId;
        data.competitorNationality = urlData.competitorNationality;
        data.competitorCountryCode = urlData.competitorCountryCode;
        data.eventId = urlData.eventId;
        data.eventName = urlData.eventName;
        data.eventStartDateStr = urlData.eventStartDateStr;
        data.eventEndDateStr = urlData.eventEndDateStr;
        data.eventFirstImageUrl = urlData.eventFirstImageUrl;
        data.eventServerUrl = urlData.hostWithPort;
        data.checkinURL = urlData.checkinURLStr;
        data.leaderboardName = leaderboardName;
        data.deviceUid = urlData.deviceUuid
                .getStringRepresentation();
        try {
            data.setCheckinDigestFromString(urlData.uriStr);
            setCheckinData(data);
        } catch (UnsupportedEncodingException e) {
            ExLog.e(activity,
                    TAG,
                    "Failed to get generate digest of qr-code string ("
                            + urlData.uriStr + "). "
                            + e.getMessage());
            activity.dismissProgressDialog();
            displayAPIErrorRecommendRetry();
            return;
        } catch (NoSuchAlgorithmException e) {
            ExLog.e(activity,
                    TAG,
                    "Failed to get generate digest of qr-code string ("
                            + urlData.uriStr + "). "
                            + e.getMessage());
            activity.dismissProgressDialog();
            displayAPIErrorRecommendRetry();
            return;
        }
    }

    public void setCheckinData(CheckinData data){
        if(data != null)
        {
            checkinData = data;
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

    private class URLData{
        public String uriStr;
        public String server;
        public int port;
        public String hostWithPort;
        public String competitorId;
        public String checkinURLStr;
        public String eventId;
        public String leaderboardName;
        public DeviceIdentifier deviceUuid;
        public String getEventUrl;
        public String getLeaderboardUrl;
        public String getCompetitorUrl;
        public String competitorName;
        public String eventName;
        public String competitorSailId;
        public String eventStartDateStr;
        public String eventEndDateStr;
        public String eventFirstImageUrl;
        public String competitorNationality;
        public String competitorCountryCode;

        public URLData(){

        }
    }
}
