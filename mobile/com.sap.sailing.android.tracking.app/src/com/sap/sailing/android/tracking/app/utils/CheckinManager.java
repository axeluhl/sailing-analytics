package com.sap.sailing.android.tracking.app.utils;

import java.io.UnsupportedEncodingException;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLEncoder;
import java.security.NoSuchAlgorithmException;
import java.util.Set;
import java.util.UUID;

import org.json.JSONException;
import org.json.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import com.sap.sailing.android.shared.data.http.HttpGetRequest;
import com.sap.sailing.android.shared.logging.ExLog;
import com.sap.sailing.android.shared.ui.activities.CheckinDataActivity;
import com.sap.sailing.android.shared.util.JsonHelper;
import com.sap.sailing.android.shared.util.NetworkHelper;
import com.sap.sailing.android.shared.util.UniqueDeviceUuid;
import com.sap.sailing.android.tracking.app.R;
import com.sap.sailing.android.tracking.app.valueobjects.CheckinData;
import com.sap.sailing.android.tracking.app.valueobjects.CompetitorCheckinData;
import com.sap.sailing.android.tracking.app.valueobjects.CompetitorUrlData;
import com.sap.sailing.android.tracking.app.valueobjects.MarkCheckinData;
import com.sap.sailing.android.tracking.app.valueobjects.MarkUrlData;
import com.sap.sailing.android.tracking.app.valueobjects.UrlData;
import com.sap.sailing.domain.abstractlog.race.RaceLog;
import com.sap.sailing.domain.abstractlog.race.SimpleRaceLogIdentifier;
import com.sap.sailing.domain.abstractlog.race.analyzing.impl.RaceLogResolver;
import com.sap.sailing.domain.base.EventBase;
import com.sap.sailing.domain.base.Mark;
import com.sap.sailing.domain.base.impl.SharedDomainFactoryImpl;
import com.sap.sailing.domain.common.racelog.tracking.DeviceMappingConstants;
import com.sap.sailing.domain.common.tracking.impl.CompetitorJsonConstants;
import com.sap.sailing.domain.racelogtracking.impl.SmartphoneUUIDIdentifierImpl;
import com.sap.sailing.server.gateway.deserialization.JsonDeserializationException;
import com.sap.sailing.server.gateway.deserialization.coursedata.impl.MarkDeserializer;
import com.sap.sailing.server.gateway.deserialization.impl.CourseAreaJsonDeserializer;
import com.sap.sailing.server.gateway.deserialization.impl.EventBaseJsonDeserializer;
import com.sap.sailing.server.gateway.deserialization.impl.LeaderboardGroupBaseJsonDeserializer;
import com.sap.sailing.server.gateway.deserialization.impl.VenueJsonDeserializer;
import com.sap.sse.common.Util;
import com.sap.sse.shared.media.ImageDescriptor;

import android.content.DialogInterface;
import android.net.Uri;
import android.support.v7.app.AlertDialog;
import android.widget.Toast;

public class CheckinManager {

    private final static String TAG = CheckinManager.class.getName();
    private CheckinData checkinData;
    private CheckinDataActivity<CheckinData> activity;
    private AppPreferences prefs;
    private UrlData urlData;
    private String url;
    private boolean update;

    public CheckinManager(String url, CheckinDataActivity<CheckinData> activity, boolean update) {
        this.activity = activity;
        this.url = url;
        prefs = new AppPreferences(activity);
        this.update = update;
    }

    public void callServerAndGenerateCheckinData() {
        Uri uri = Uri.parse(url);
        String scheme = uri.getScheme();

        urlData = extractRequestParametersFromUri(uri, scheme);
        if (urlData == null) {
            setCheckinData(null);
            return;
        }

        activity.showProgressDialog(R.string.please_wait, R.string.getting_event);

        try {
            HttpGetRequest getLeaderboardRequest = new HttpGetRequest(new URL(urlData.leaderboardUrl), activity);
            getLeaderBoardFromServer(getLeaderboardRequest);

        } catch (MalformedURLException e) {
            ExLog.e(activity, TAG, "Error: Failed to perform checking due to a MalformedURLException: " + e.getMessage());
            handleApiError();
        }
    }

    private UrlData extractRequestParametersFromUri(Uri uri, String scheme) {
        UrlData urlData = null;
        String server = scheme + "://" + uri.getHost();
        int port = uri.getPort();
        Exception exception = null;
        try {
            String leaderboardNameFromQR = URLEncoder.encode(uri.getQueryParameter(DeviceMappingConstants.URL_LEADERBOARD_NAME), "UTF-8")
                .replace("+", "%20");
            Set<String> parameterNames = uri.getQueryParameterNames();
            if (parameterNames.contains(DeviceMappingConstants.URL_COMPETITOR_ID_AS_STRING)) {
                CompetitorUrlData competitorUrlData = new CompetitorUrlData(server, port);
                competitorUrlData.competitorId = uri.getQueryParameter(DeviceMappingConstants.URL_COMPETITOR_ID_AS_STRING);
                competitorUrlData.competitorUrl = competitorUrlData.hostWithPort + prefs.getServerCompetitorPath(competitorUrlData.competitorId);
                urlData = competitorUrlData;
            } else if (parameterNames.contains(DeviceMappingConstants.URL_MARK_ID_AS_STRING)) {
                MarkUrlData markUrlData = new MarkUrlData(server, port);
                markUrlData.setMarkId(uri.getQueryParameter(DeviceMappingConstants.URL_MARK_ID_AS_STRING));
                markUrlData.setMarkUrl(markUrlData.hostWithPort + prefs.getServerMarkPath(leaderboardNameFromQR, markUrlData.getMarkId()));
                urlData = markUrlData;
            } else {
                ExLog.e(activity, TAG, "Neither mark nor competitor checkin");
                exception = new Exception();
            }
            if (urlData != null) {
                urlData.uriStr = uri.toString();
                urlData.checkinURLStr = urlData.hostWithPort + prefs.getServerCheckinPath().replace("{leaderboard-name}", leaderboardNameFromQR);
                urlData.eventId = uri.getQueryParameter(DeviceMappingConstants.URL_EVENT_ID);
                urlData.leaderboardName = leaderboardNameFromQR;
                urlData.deviceUuid = new SmartphoneUUIDIdentifierImpl(UUID.fromString(UniqueDeviceUuid.getUniqueId(activity)));
                urlData.eventUrl = urlData.hostWithPort + prefs.getServerEventPath(urlData.eventId);
                urlData.leaderboardUrl = urlData.hostWithPort + prefs.getServerLeaderboardPath(urlData.leaderboardName);
            }

        } catch (UnsupportedEncodingException e) {
            ExLog.e(activity, TAG, "Failed to encode leaderboard name: " + e.getMessage());
            exception = e;
        } catch (NullPointerException e){
            ExLog.e(activity, TAG, "Invalid Barcode (no leaderboard-name set or missing parameter): " + e.getMessage());
            exception = e;
        }
        if (exception != null) {
            Toast.makeText(activity, activity.getString(R.string.error_invalid_qr_code), Toast.LENGTH_LONG).show();
            prefs.setLastScannedQRCode(null);
            urlData = null;
        }
        return urlData;
    }

    private void getLeaderBoardFromServer(HttpGetRequest getLeaderboardRequest) {
        NetworkHelper.getInstance(activity)
            .executeHttpJsonRequestAsync(getLeaderboardRequest, new NetworkHelper.NetworkHelperSuccessListener() {
                @Override
                public void performAction(JSONObject response) {
                    final String leaderboardDisplayName = response.optString("displayName", urlData.leaderboardName);
                    HttpGetRequest getEventRequest;
                    try {
                        getEventRequest = new HttpGetRequest(new URL(urlData.eventUrl), activity);
                        getEventFromServer(getEventRequest, leaderboardDisplayName);
                    } catch (MalformedURLException e1) {
                        ExLog.e(activity, TAG, "Error: Failed to perform checking due to a MalformedURLException: " + e1.getMessage());
                        handleApiError();
                    }
                }
            }, new NetworkHelper.NetworkHelperFailureListener() {
                @Override
                public void performAction(NetworkHelper.NetworkHelperError e) {
                    ExLog.e(activity, TAG, "Failed to get event from API: " + e.getMessage());
                    handleApiError();
                }
            });
    }

    private void getEventFromServer(HttpGetRequest getEventRequest, final String leaderboardDisplayName) {
        NetworkHelper.getInstance(activity).executeHttpJsonRequestAsync(getEventRequest, new NetworkHelper.NetworkHelperSuccessListener() {
            @Override
            public void performAction(JSONObject response) {
                EventBaseJsonDeserializer deserializer = new EventBaseJsonDeserializer(
                        new VenueJsonDeserializer(new CourseAreaJsonDeserializer(new SharedDomainFactoryImpl(
                                new RaceLogResolver() {
                                    @Override
                                    public RaceLog resolve(SimpleRaceLogIdentifier identifier) {
                                        return null;
                                    }
                                }))), new LeaderboardGroupBaseJsonDeserializer());
                try {
                    final EventBase event = deserializer.deserialize((org.json.simple.JSONObject) new JSONParser().parse(response.toString()));
                    urlData.eventId = event.getId().toString();
                    urlData.eventName = event.getName();
                    urlData.eventStartDateStr = (event.getStartDate() != null) ? "" + event.getStartDate().asMillis() : "0";
                    urlData.eventEndDateStr = (event.getEndDate() != null) ? "" + event.getEndDate().asMillis() : "0";
                    Iterable<ImageDescriptor> imageUrls = event.getImages();
                    if (!Util.isEmpty(imageUrls)) {
                        urlData.eventFirstImageUrl = imageUrls.iterator().next().getURL().toString();
                    } else {
                        urlData.eventFirstImageUrl = null;
                    }
                } catch (JsonDeserializationException | ParseException e) {
                    ExLog.e(activity, TAG, "Error getting data from call on URL: " + urlData.eventUrl + ", Error: " + e.getMessage());
                    handleApiError();
                    return;
                }
                if (urlData instanceof CompetitorUrlData) {
                    CompetitorUrlData competitorUrlData = (CompetitorUrlData) urlData;
                    try {
                        HttpGetRequest getCompetitorRequest = new HttpGetRequest(new URL(competitorUrlData.competitorUrl), activity);
                        getCompetitorFromServer(getCompetitorRequest, competitorUrlData, leaderboardDisplayName);
                    } catch (MalformedURLException e2) {
                        ExLog.e(activity, TAG, "Error: Failed to perform checking due to a MalformedURLException: " + e2.getMessage());
                        handleApiError();
                    }
                } else if (urlData instanceof MarkUrlData) {
                    MarkUrlData markUrlData = (MarkUrlData) urlData;
                    try {
                        HttpGetRequest getMarkRequest = new HttpGetRequest(new URL(markUrlData.getMarkUrl()), activity);
                        getMarkFromServer(getMarkRequest, markUrlData, leaderboardDisplayName);
                    } catch (MalformedURLException exception) {
                        ExLog.e(activity, TAG, "Error: Failed to perform checking due to a MalformedURLException: " + exception.getMessage());
                        handleApiError();
                    }
                }

            }
        }, new NetworkHelper.NetworkHelperFailureListener() {

            @Override
            public void performAction(NetworkHelper.NetworkHelperError e) {
                ExLog.e(activity, TAG, "Failed to get leaderboard from API: " + e.getMessage());
                handleApiError();
            }
        });
    }

    private void getCompetitorFromServer(HttpGetRequest getCompetitorRequest, final CompetitorUrlData urlData, final String leaderboardDisplayName) {
        NetworkHelper.getInstance(activity)
            .executeHttpJsonRequestAsync(getCompetitorRequest, new NetworkHelper.NetworkHelperSuccessListener() {
                @Override
                public void performAction(JSONObject response) {
                    activity.dismissProgressDialog();
                    try {
                        urlData.competitorName = response.getString(CompetitorJsonConstants.FIELD_NAME);
                        urlData.competitorId = response.getString(CompetitorJsonConstants.FIELD_ID);
                        urlData.competitorSailId = response.getString(CompetitorJsonConstants.FIELD_SAIL_ID);
                        urlData.competitorNationality = response.getString(CompetitorJsonConstants.FIELD_NATIONALITY);
                        urlData.competitorCountryCode = response.getString(CompetitorJsonConstants.FIELD_COUNTRY_CODE);
                        // TODO Bug 3358: get optional team image from CompetitorJsonConstants.FIELD_TEAM_IMAGE_URI
                    } catch (JSONException e) {
                        ExLog.e(activity, TAG, "Error getting data from call on URL: " + urlData.competitorUrl + ", Error: " + e.getMessage());
                        handleApiError();
                        return;
                    }
                    saveCheckinDataAndNotifyListeners(urlData, leaderboardDisplayName);
                }
            }, new NetworkHelper.NetworkHelperFailureListener() {
                @Override
                public void performAction(NetworkHelper.NetworkHelperError e) {
                    ExLog.e(activity, TAG, "Failed to get competitor from API: " + e.getMessage());
                    handleApiError();
                }
            });
    }

    private void getMarkFromServer(HttpGetRequest getMarkRequest, final MarkUrlData urlData, final String leaderboardDisplayName) {
        NetworkHelper.getInstance(activity)
            .executeHttpJsonRequestAsync(getMarkRequest, new NetworkHelper.NetworkHelperSuccessListener() {
                @Override
                public void performAction(JSONObject response) {
                    try {
                        org.json.simple.JSONObject simpleMark;
                        simpleMark = JsonHelper.convertToSimple(response);
                        MarkDeserializer markDeserializer = new MarkDeserializer(new SharedDomainFactoryImpl(null));
                        Mark mark = markDeserializer.deserialize(simpleMark);
                        urlData.setMark(mark);
                    } catch (ParseException|JsonDeserializationException e) {
                        ExLog.e(activity, TAG, "Failed to deserialize mark");
                        if (activity != null) {
                            activity.dismissProgressDialog();
                            displayAPIErrorRecommendRetry();
                        }
                        return;
                    }
                    saveCheckinDataAndNotifyListeners(urlData, leaderboardDisplayName);
                }
            }, new NetworkHelper.NetworkHelperFailureListener() {
                @Override
                public void performAction(NetworkHelper.NetworkHelperError e) {
                    ExLog.e(activity, TAG, "Failed to get mark from server: " + e.getMessage());
                    if (activity != null) {
                        activity.dismissProgressDialog();
                        displayAPIErrorRecommendRetry();
                    }
                }
            });
    }

    private void saveCheckinDataAndNotifyListeners(UrlData urlData, String leaderboardDisplayName) {
        CheckinData data;
        if (urlData instanceof CompetitorUrlData) {
            CompetitorUrlData competitorUrlData = (CompetitorUrlData) urlData;
            data = new CompetitorCheckinData(competitorUrlData, leaderboardDisplayName);
        } else {
            MarkUrlData markUrlData = (MarkUrlData) urlData;
            data = new MarkCheckinData(markUrlData, leaderboardDisplayName);
        }
        data.setUpdate(update);
        try {
            data.setCheckinDigestFromString(urlData.uriStr);
            activity.dismissProgressDialog();
            setCheckinData(data);
        } catch (UnsupportedEncodingException | NoSuchAlgorithmException e) {
            ExLog.e(activity, TAG, "Failed to get generate digest of qr-code string (" + urlData.uriStr + "). " + e.getMessage());
            handleApiError();
        }
    }

    public void setCheckinData(CheckinData data) {
        checkinData = data;
        activity.onCheckinDataAvailable(getCheckinData());
    }

    public CheckinData getCheckinData() {
        return checkinData;
    }

    private void handleApiError() {
        prefs.setLastScannedQRCode(null);
        activity.dismissProgressDialog();
        displayAPIErrorRecommendRetry();
        setCheckinData(null);
    }

    /**
     * Shows a pop-up-dialog that informs the user than an API-call has failed and recommends a retry.
     */
    private void displayAPIErrorRecommendRetry() {
        AlertDialog.Builder builder = new AlertDialog.Builder(activity, R.style.AppTheme_AlertDialog);
        builder.setMessage(activity.getString(R.string.notify_user_api_call_failed));
        builder.setCancelable(true);
        builder.setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {

            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.cancel();
            }

        });
        builder.show();
        setCheckinData(null);
    }

}
