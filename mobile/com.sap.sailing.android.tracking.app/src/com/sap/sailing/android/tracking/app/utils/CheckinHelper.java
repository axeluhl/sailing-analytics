package com.sap.sailing.android.tracking.app.utils;

import org.json.JSONException;
import org.json.JSONObject;

import com.sap.sailing.domain.common.racelog.tracking.DeviceMappingConstants;

import android.support.annotation.NonNull;

public class CheckinHelper {

    @NonNull
    private static JSONObject getBaseCheckinJsom(String deviceUuid, String pushDeviceId, long fromMillis,
            JSONObject jsonObject) throws JSONException {
        jsonObject.put(DeviceMappingConstants.JSON_DEVICE_TYPE, "android");
        jsonObject.put(DeviceMappingConstants.JSON_DEVICE_UUID, deviceUuid);
        jsonObject.put(DeviceMappingConstants.JSON_PUSH_DEVICE_ID, pushDeviceId);
        jsonObject.put(DeviceMappingConstants.JSON_FROM_MILLIS, fromMillis);
        return jsonObject;
    }

    @NonNull
    public static JSONObject getCompetitorCheckinJson(String competitorId, String deviceUuid, String pushDeviceId,
            long fromMillis) throws JSONException {
        JSONObject jsonObject = new JSONObject();
        jsonObject.put(DeviceMappingConstants.JSON_COMPETITOR_ID_AS_STRING, competitorId);
        return getBaseCheckinJsom(deviceUuid, pushDeviceId, fromMillis, jsonObject);
    }

    @NonNull
    public static JSONObject getMarkCheckinJson(String markId, String deviceUuid, String pushDeviceId, long fromMillis)
            throws JSONException {
        JSONObject jsonObject = new JSONObject();
        jsonObject.put(DeviceMappingConstants.JSON_MARK_ID_AS_STRING, markId);
        return getBaseCheckinJsom(deviceUuid, pushDeviceId, fromMillis, jsonObject);
    }

    @NonNull
    public static JSONObject getBoatCheckinJson(String boatId, String deviceUuid, String pushDeviceId, long fromMillis)
            throws JSONException {
        JSONObject jsonObject = new JSONObject();
        jsonObject.put(DeviceMappingConstants.JSON_BOAT_ID_AS_STRING, boatId);
        return getBaseCheckinJsom(deviceUuid, pushDeviceId, fromMillis, jsonObject);
    }

}
