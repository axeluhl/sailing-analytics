package com.sap.sailing.android.tracking.app.utils;

import org.json.JSONException;
import org.json.JSONObject;

public class CheckinHelper {
	public static final String LEADERBOARD_NAME = "leaderboardName";
	public static final String COMPETITOR_ID = "competitorId";
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
