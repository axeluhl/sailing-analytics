package com.sap.sailing.android.tracking.app.utils;

import com.sap.sailing.domain.common.tracking.FlatSmartphoneUuidAndGPSFixMovingJsonSerializer;
import org.json.JSONException;
import org.json.JSONObject;

public class CheckinHelper {
    public static JSONObject getCheckinJson(String competitorId, String deviceUuid, String pushDeviceId, long fromMillis)
            throws JSONException {
        JSONObject jsonObject = new JSONObject();
        jsonObject.put("competitorId", competitorId);
        jsonObject.put("deviceType", "android");
        jsonObject.put(FlatSmartphoneUuidAndGPSFixMovingJsonSerializer.DEVICE_UUID, deviceUuid);
        jsonObject.put("pushDeviceId", pushDeviceId);
        jsonObject.put("fromMillis", fromMillis);
        return jsonObject;
    }
}
