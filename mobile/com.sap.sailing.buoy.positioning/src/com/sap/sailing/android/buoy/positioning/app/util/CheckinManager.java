package com.sap.sailing.android.buoy.positioning.app.util;

import java.io.UnsupportedEncodingException;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLEncoder;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.json.simple.parser.ParseException;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.net.Uri;
import android.util.Log;
import android.widget.Toast;

import com.sap.sailing.android.buoy.positioning.app.R;
import com.sap.sailing.android.buoy.positioning.app.valueobjects.CheckinData;
import com.sap.sailing.android.buoy.positioning.app.valueobjects.MarkInfo;
import com.sap.sailing.android.buoy.positioning.app.valueobjects.MarkPingInfo;
import com.sap.sailing.android.shared.data.AbstractCheckinData;
import com.sap.sailing.android.shared.data.http.HttpGetRequest;
import com.sap.sailing.android.shared.logging.ExLog;
import com.sap.sailing.android.shared.ui.activities.CheckinDataActivity;
import com.sap.sailing.android.shared.util.JsonHelper;
import com.sap.sailing.android.shared.util.NetworkHelper;
import com.sap.sailing.android.shared.util.NetworkHelper.NetworkHelperSuccessListener;
import com.sap.sailing.android.shared.util.UniqueDeviceUuid;
import com.sap.sailing.domain.common.racelog.tracking.DeviceMappingConstants;
import com.sap.sailing.domain.common.tracking.GPSFix;
import com.sap.sailing.domain.racelogtracking.DeviceIdentifier;
import com.sap.sailing.domain.racelogtracking.impl.SmartphoneUUIDIdentifierImpl;
import com.sap.sailing.server.gateway.deserialization.JsonDeserializationException;
import com.sap.sailing.server.gateway.deserialization.coursedata.impl.MarkJsonDeserializer;
import com.sap.sailing.server.gateway.deserialization.impl.FlatGPSFixJsonDeserializer;
import com.sap.sailing.server.gateway.serialization.coursedata.impl.MarkJsonSerializer;
import com.sap.sailing.server.gateway.serialization.impl.FlatGPSFixJsonSerializer;
import com.sap.sailing.server.gateway.serialization.impl.MarkJsonSerializerWithPosition;

public class CheckinManager {

    private final static String TAG = CheckinManager.class.getName();
    private AbstractCheckinData checkinData;
    private CheckinDataActivity activity;
    private Context mContext;
    private AppPreferences prefs;
    private String url;
    private DataChangedListner dataChangedListner;

    public CheckinManager(String url, Context context){
        this.url = url;
        mContext = context;
        prefs = new AppPreferences(context);
    }

    public CheckinManager(String url, CheckinDataActivity activity) {
        this.activity = activity;
        this.url = url;
        mContext = activity;
        prefs = new AppPreferences(mContext);
    }

    public void callServerAndGenerateCheckinData() {
        Uri uri = Uri.parse(url);
        String scheme = uri.getScheme();
        final URLData urlData = extractRequestParametersFromUri(uri, scheme);
        if (urlData == null) {
            setCheckinData(null);
            return;
        }
        if (activity != null) {
            activity.showProgressDialog(R.string.please_wait, R.string.getting_marks);
        }
        try {
            HttpGetRequest getLeaderboardRequest = new HttpGetRequest(new URL(urlData.getLeaderboardUrl), mContext);
            getLeaderBoardFromServer(urlData, getLeaderboardRequest);
        } catch (MalformedURLException e) {
            ExLog.e(mContext, TAG,
                    "Error: Failed to perform checking due to a MalformedURLException: " + e.getMessage());
        }
    }

    private URLData extractRequestParametersFromUri(Uri uri, String scheme) {
        assert uri != null;
        URLData urlData = new URLData();
        urlData.uriStr = uri.toString();
        urlData.server = scheme + "://" + uri.getHost();
        urlData.port = uri.getPort();
        urlData.hostWithPort = urlData.server + (urlData.port == -1 ? "" : (":" + urlData.port));
        String leaderboardNameFromQR = "";
        try {
            leaderboardNameFromQR = URLEncoder.encode(
                    uri.getQueryParameter(DeviceMappingConstants.URL_LEADERBOARD_NAME), "UTF-8").replace("+", "%20");
        } catch (UnsupportedEncodingException e) {
            ExLog.e(mContext, TAG, "Failed to encode leaderboard name: " + e.getMessage());
        } catch (NullPointerException e) {
            ExLog.e(mContext, TAG, "Invalid Barcode (no leaderboard-name set): " + e.getMessage());
            Toast.makeText(mContext, mContext.getString(R.string.error_invalid_qr_code), Toast.LENGTH_LONG).show();
            urlData = null;
        }
        if (urlData != null) {
            urlData.leaderboardName = leaderboardNameFromQR;

            urlData.deviceUuid = new SmartphoneUUIDIdentifierImpl(UUID.fromString(UniqueDeviceUuid
                    .getUniqueId(mContext)));

            urlData.getLeaderboardUrl = urlData.hostWithPort + prefs.getServerLeaderboardPath(urlData.leaderboardName);
        }
        return urlData;
    }

    private void getLeaderBoardFromServer(final URLData urlData, HttpGetRequest getLeaderboardRequest) {
        NetworkHelper.getInstance(mContext).executeHttpJsonRequestAsync(getLeaderboardRequest,
                new NetworkHelper.NetworkHelperSuccessListener() {
                    @Override
                    public void performAction(JSONObject response) {
                        final String leaderboardName;
                        try {
                            leaderboardName = response.getString("name");
                            urlData.getMarkUrl = urlData.hostWithPort + prefs.getServerMarkPath(leaderboardName);
                        } catch (JSONException e) {
                            ExLog.e(mContext, TAG, "Error getting data from call on URL: " + urlData.getLeaderboardUrl
                                    + ", Error: " + e.getMessage());
                            if (activity != null) {
                                activity.dismissProgressDialog();
                                displayAPIErrorRecommendRetry();
                            }
                            return;
                        }

                        HttpGetRequest getMarksRequest;
                        try {
                            getMarksRequest = new HttpGetRequest(new URL(urlData.getMarkUrl), mContext);
                            getMarksFromServer(leaderboardName, getMarksRequest, urlData);
                        } catch (MalformedURLException e1) {
                            ExLog.e(mContext, TAG, "Error: Failed to perform checking due to a MalformedURLException: " + e1.getMessage());
                        }
                    }
                }, new NetworkHelper.NetworkHelperFailureListener() {
                    @Override
                    public void performAction(NetworkHelper.NetworkHelperError e) {
                        ExLog.e(mContext, TAG, "Failed to get event from API: " + e.getMessage());
                        if (activity != null) {
                            activity.dismissProgressDialog();
                            displayAPIErrorRecommendRetry();
                        }
                    }
                });
    }

    private void getMarksFromServer(final String leaderboardName, HttpGetRequest getMarksRequest, final URLData urlData) {
        NetworkHelper.getInstance(mContext).executeHttpJsonRequestAsync(getMarksRequest,
                new NetworkHelperSuccessListener() {

                    @Override
                    public void performAction(JSONObject response) {
                        try {
                            JSONArray markArray = response.getJSONArray("marks");
                            String checkinDigest = generateCheckindigest(urlData.uriStr);
                            List<MarkInfo> marks = new ArrayList<>();
                            List<MarkPingInfo> pings = new ArrayList<>();
                            for (int i = 0; i < markArray.length(); i++) {
                                JSONObject jsonMark = (JSONObject) markArray.get(i);
                                org.json.simple.JSONObject simpleMark;
                                simpleMark = JsonHelper.convertToSimple(jsonMark);
                                MarkJsonDeserializer markDeserializer = new MarkJsonDeserializer();
                                MarkInfo mark = MarkInfo.create(markDeserializer.deserialize(simpleMark));
                                mark.setCheckinDigest(checkinDigest);
                                mark.setClassName(jsonMark.getString(MarkJsonSerializer.FIELD_CLASS));
                                if (jsonMark.has(MarkJsonSerializerWithPosition.FIELD_POSITION)) {
                                    if (!jsonMark.get(MarkJsonSerializerWithPosition.FIELD_POSITION).equals(null)) {
                                        JSONObject positionJson = jsonMark.getJSONObject(MarkJsonSerializerWithPosition.FIELD_POSITION);
                                        MarkPingInfo ping = new MarkPingInfo();
                                        FlatGPSFixJsonDeserializer deserializer = new FlatGPSFixJsonDeserializer();
                                        org.json.simple.JSONObject simplePosition;
                                        simplePosition = JsonHelper.convertToSimple(positionJson);
                                        GPSFix gpsFix = deserializer.deserialize(simplePosition);
                                        // Latitude and longitude are stored as string values for SQLite database
                                        ping.setLatitude("" + gpsFix.getPosition().getLatDeg());
                                        ping.setLongitude("" + gpsFix.getPosition().getLngDeg());
                                        // Timestamp is simply stored as int for SQLite database
                                        ping.setTimestamp((int)gpsFix.getTimePoint().asMillis());
                                        ping.setAccuracy(positionJson.getDouble(FlatGPSFixJsonSerializer.FIELD_ACCURACY));
                                        ping.setMarkId(mark.getId());
                                        pings.add(ping);
                                    }
                                }
                                marks.add(mark);
                            }
                            urlData.marks = marks;
                            urlData.pings = pings;
                            saveCheckinDataAndNotifyListeners(urlData, leaderboardName);

                        } catch (JSONException | ParseException | JsonDeserializationException e) {
                            ExLog.e(mContext, TAG, "Error getting data from call on URL: " + urlData.getMarkUrl
                                    + ", Error: " + e.getMessage());
                            if (activity != null) {
                                activity.dismissProgressDialog();
                                displayAPIErrorRecommendRetry();
                            }
                            return;
                        }

                    }
                }, new NetworkHelper.NetworkHelperFailureListener() {

                    @Override
                    public void performAction(NetworkHelper.NetworkHelperError e) {
                        ExLog.e(mContext, TAG, "Failed to get marks from API: " + e.getMessage());
                        if (activity != null) {
                            activity.dismissProgressDialog();
                            displayAPIErrorRecommendRetry();
                        }
                    }
                });
    }

    private void saveCheckinDataAndNotifyListeners(URLData urlData, String leaderboardName) {
        CheckinData data = new CheckinData();
        data.serverWithPort = urlData.hostWithPort;
        data.leaderboardName = leaderboardName;
        data.marks = urlData.marks;
        data.pings = urlData.pings;
        data.deviceUid = urlData.deviceUuid.getStringRepresentation();
        data.uriString = urlData.uriStr;
        try {
            data.setCheckinDigestFromString(urlData.uriStr);
            if (activity != null) {
                activity.dismissProgressDialog();
            }
            setCheckinData(data);
        } catch (UnsupportedEncodingException e) {
            ExLog.e(mContext, TAG,
                    "Failed to get generate digest of qr-code string (" + urlData.uriStr + "). " + e.getMessage());
            if (activity != null) {
                activity.dismissProgressDialog();
                displayAPIErrorRecommendRetry();
            }
        } catch (NoSuchAlgorithmException e) {
            ExLog.e(mContext, TAG,
                    "Failed to get generate digest of qr-code string (" + urlData.uriStr + "). " + e.getMessage());
            if (activity != null) {
                activity.dismissProgressDialog();
                displayAPIErrorRecommendRetry();
            }
        }
    }

    public void setCheckinData(AbstractCheckinData data) {
        checkinData = data;
        if (activity != null) {
            activity.onCheckinDataAvailable(getCheckinData());
        }
        else if (dataChangedListner != null){
            dataChangedListner.handleData(data);
        }
    }

    public interface DataChangedListner{
        void handleData(AbstractCheckinData data);
    }

    public void setDataChangedListner(DataChangedListner listner){
        dataChangedListner = listner;
    }

    public AbstractCheckinData getCheckinData() {
        return checkinData;
    }

    /**
     * Shows a pop-up-dialog that informs the user than an API-call has failed and recommends a retry.
     */
    private void displayAPIErrorRecommendRetry() {
        AlertDialog.Builder builder = new AlertDialog.Builder(mContext);
        builder.setMessage(mContext.getString(R.string.notify_user_api_call_failed));
        builder.setCancelable(true);
        builder.setPositiveButton(mContext.getString(R.string.ok), new DialogInterface.OnClickListener() {

            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.cancel();
            }

        });
        AlertDialog alert = builder.create();
        alert.show();
        setCheckinData(null);
    }

    private String generateCheckindigest(String url) {
        String checkinDigest = "";
        try {
            MessageDigest md = MessageDigest.getInstance("SHA-256");
            md.update(url.getBytes("UTF-8"));
            byte[] digest = md.digest();
            StringBuffer buf = new StringBuffer();
            for (byte byt : digest) {
                buf.append(Integer.toString((byt & 0xff) + 0x100, 16).substring(1));
                checkinDigest = buf.toString();
            }
        } catch (UnsupportedEncodingException e) {
            Log.e(TAG, "Exception trying to generate check-in digest", e);
        } catch (NoSuchAlgorithmException e) {
            Log.e(TAG, "Exception trying to generate check-in digest", e);
        }
        return checkinDigest;
    }

    private class URLData {
        public String uriStr;
        public String server;
        public int port;
        public String hostWithPort;
        public List<MarkInfo> marks;
        public List<MarkPingInfo> pings;
        public String leaderboardName;
        public DeviceIdentifier deviceUuid;
        public String getMarkUrl;
        public String getLeaderboardUrl;

        public URLData() {

        }
    }
}
