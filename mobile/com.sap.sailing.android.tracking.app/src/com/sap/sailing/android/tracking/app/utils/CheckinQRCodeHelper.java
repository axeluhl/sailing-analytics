package com.sap.sailing.android.tracking.app.utils;

import org.json.JSONException;
import org.json.JSONObject;

public class CheckinQRCodeHelper {
	public static final String LEADERBOARD_NAME = "leaderboard_name";
	public static final String COMPETITOR_ID = "competitor_id";
	public static final String EVENT_ID = "event_id";
	
	public static JSONObject getCheckinJson(String competitorId, String deviceUuid, String pushDeviceId, long fromMillis) throws JSONException
	{
		JSONObject jsonObject = new JSONObject();
		jsonObject.put("competitorId", competitorId);
		jsonObject.put("deviceType", "android");
		jsonObject.put("deviceUuid", deviceUuid);
		jsonObject.put("pushDeviceId", pushDeviceId);
		jsonObject.put("fromMillis", fromMillis);
		return jsonObject;
	}
}
