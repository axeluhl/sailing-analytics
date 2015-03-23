package com.sap.sailing.android.buoy.positioning.app.util;

import java.io.UnsupportedEncodingException;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLEncoder;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.net.Uri;
import android.widget.Toast;

import com.sap.sailing.android.buoy.positioning.app.R;
import com.sap.sailing.android.buoy.positioning.app.valueobjects.CheckinData;
import com.sap.sailing.android.buoy.positioning.app.valueobjects.MarkInfo;
import com.sap.sailing.android.shared.data.AbstractCheckinData;
import com.sap.sailing.android.shared.data.http.HttpGetRequest;
import com.sap.sailing.android.shared.logging.ExLog;
import com.sap.sailing.android.shared.ui.activities.CheckinDataActivity;
import com.sap.sailing.android.shared.util.NetworkHelper;
import com.sap.sailing.android.shared.util.NetworkHelper.NetworkHelperSuccessListener;
import com.sap.sailing.android.shared.util.UniqueDeviceUuid;
import com.sap.sailing.domain.common.racelog.tracking.DeviceMappingConstants;
import com.sap.sailing.domain.racelogtracking.DeviceIdentifier;
import com.sap.sailing.domain.racelogtracking.impl.SmartphoneUUIDIdentifierImpl;

public class CheckinManager {

    private final static String TAG = CheckinManager.class.getName();
    private AbstractCheckinData checkinData;
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
            setCheckinData(null);
            return;
        }

        activity.showProgressDialog(R.string.please_wait, R.string.getting_marks);

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
            urlData.leaderboardName = leaderboardNameFromQR;

            urlData.deviceUuid = new SmartphoneUUIDIdentifierImpl(UUID.fromString(UniqueDeviceUuid
                    .getUniqueId(activity)));

            urlData.getLeaderboardUrl = urlData.hostWithPort + prefs.getServerLeaderboardPath(urlData.leaderboardName);
        }
        return urlData;
    }

    private void getLeaderBoardFromServer(final URLData urlData, HttpGetRequest getLeaderboardRequest) {
        NetworkHelper.getInstance(activity).executeHttpJsonRequestAsnchronously(getLeaderboardRequest,
                new NetworkHelper.NetworkHelperSuccessListener() {

                    @Override
                    public void performAction(JSONObject response) {
                        // TODO Auto-generated method stub


                        final String leaderboardName;

                        try {
                            leaderboardName = response.getString("name");
                            urlData.getMarkUrl = urlData.hostWithPort + prefs.getServerMarkPath(leaderboardName);
                        } catch (JSONException e) {
                            ExLog.e(activity, TAG, "Error getting data from call on URL: " + urlData.getLeaderboardUrl
                                    + ", Error: " + e.getMessage());
                            activity.dismissProgressDialog();
                            displayAPIErrorRecommendRetry();
                            return;
                        }

                        HttpGetRequest getMarksRequest;
                        try {
                            getMarksRequest = new HttpGetRequest(new URL(urlData.getMarkUrl), activity);
                            getMarksFromServer(leaderboardName, getMarksRequest, urlData);
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
    
	private void getMarksFromServer(final String leaderboardName,
			HttpGetRequest getMarksRequest, final URLData urlData) {
		NetworkHelper.getInstance(activity).executeHttpJsonRequestAsnchronously(getMarksRequest, new NetworkHelperSuccessListener() {
			
			@Override
			public void performAction(JSONObject response) {
				try {
                    JSONArray markArray = response.getJSONArray("");
                    List<MarkInfo> marks = new ArrayList<MarkInfo>();
                    for(int i = 0; i< markArray.length(); i++){
                    	JSONObject jsonMark = (JSONObject) markArray.get(i);
                    	MarkInfo mark = new MarkInfo();
                    	mark.setClassName(jsonMark.getString("@class"));
                    	mark.setName(jsonMark.getString("name"));
                    	mark.setId(jsonMark.getString("id"));
                    	mark.setType(jsonMark.getString("type"));
                    	marks.add(mark);
                    }
                    urlData.marks = marks;
                    saveCheckinDataAndNotifyListeners(urlData, leaderboardName);

                } catch (JSONException e) {
                    ExLog.e(activity, TAG, "Error getting data from call on URL: " + urlData.getMarkUrl
                            + ", Error: " + e.getMessage());
                    activity.dismissProgressDialog();
                    displayAPIErrorRecommendRetry();
                    return;
                }
				
			}
		}, new NetworkHelper.NetworkHelperFailureListener() {
			
			@Override
            public void performAction(NetworkHelper.NetworkHelperError e) {
                ExLog.e(activity, TAG, "Failed to get marks from API: " + e.getMessage());
                activity.dismissProgressDialog();
                displayAPIErrorRecommendRetry();
            }
		});
	}

    private void saveCheckinDataAndNotifyListeners(URLData urlData, String leaderboardName) {
        CheckinData data = new CheckinData();
        data.leaderboardName = leaderboardName;
        data.deviceUid = urlData.deviceUuid
                .getStringRepresentation();
        data.uriString = urlData.uriStr;
        try {
            data.setCheckinDigestFromString(urlData.uriStr);
            activity.dismissProgressDialog();
            setCheckinData(data);
        } catch (UnsupportedEncodingException e) {
            ExLog.e(activity,
                    TAG,
                    "Failed to get generate digest of qr-code string ("
                            + urlData.uriStr + "). "
                            + e.getMessage());
            activity.dismissProgressDialog();
            displayAPIErrorRecommendRetry();
        } catch (NoSuchAlgorithmException e) {
            ExLog.e(activity,
                    TAG,
                    "Failed to get generate digest of qr-code string ("
                            + urlData.uriStr + "). "
                            + e.getMessage());
            activity.dismissProgressDialog();
            displayAPIErrorRecommendRetry();
        }
    }

    public void setCheckinData(AbstractCheckinData data){
        checkinData = data;
        activity.onCheckinDataAvailable(getCheckinData());
    }

    public AbstractCheckinData getCheckinData(){
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
        setCheckinData(null);
    }

    public interface CheckinDataHandler{
    }

    private class URLData{
        public String uriStr;
        public String server;
        public int port;
        public String hostWithPort;
        public String checkinURLStr;
        public String eventId;
        public List<MarkInfo> marks;
        public String leaderboardName;
        public DeviceIdentifier deviceUuid;
        public String getMarkUrl;
        public String getLeaderboardUrl;

        public URLData(){

        }
    }
}
