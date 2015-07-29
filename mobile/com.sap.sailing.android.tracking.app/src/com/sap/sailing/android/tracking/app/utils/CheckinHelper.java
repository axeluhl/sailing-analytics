package com.sap.sailing.android.tracking.app.utils;

import org.json.JSONException;
import org.json.JSONObject;

import com.sap.sailing.domain.common.racelog.tracking.DeviceMappingConstants;

public class CheckinHelper {
    public static JSONObject getCheckinJson(String competitorId, String deviceUuid, String pushDeviceId, long fromMillis)
            throws JSONException {
        JSONObject jsonObject = new JSONObject();
        jsonObject.put(DeviceMappingConstants.JSON_COMPETITOR_ID_AS_STRING, competitorId);
        jsonObject.put(DeviceMappingConstants.JSON_DEVICE_TYPE, "android");
        jsonObject.put(DeviceMappingConstants.JSON_DEVICE_UUID, deviceUuid);
        jsonObject.put(DeviceMappingConstants.JSON_PUSH_DEVICE_ID, pushDeviceId);
        jsonObject.put(DeviceMappingConstants.JSON_FROM_MILLIS, fromMillis);
        return jsonObject;
    }
}
