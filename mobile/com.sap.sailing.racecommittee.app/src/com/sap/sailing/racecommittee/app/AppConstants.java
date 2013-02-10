package com.sap.sailing.racecommittee.app;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

import com.sap.sailing.racecommittee.app.ui.fragments.raceinfo.SetTimeRaceFragment;


public class AppConstants {
	
	public final static String ApplicationVersion = "2.0 Beta 1 - Jumbotron";
	
	// Communication between the Connectivity watcher and the event sender
	public final static String SAVE_INTENT_ACTION = "com.sap.sailing.racecommittee.app.action.saveIntent";
	public final static String SEND_SAVED_INTENTS_ACTION = "com.sap.sailing.racecommittee.app.action.sendSavedIntents";
	public final static String REGISTER_RACE_ACTION = "com.sap.sailing.racecommittee.app.action.registerRace";
	public final static String SEND_EVENT_ACTION = "com.sap.sailing.racecommittee.app.action.sendEvent";
	
	// Intent extra fields
	public final static String COURSE_AREA_UUID_KEY = "courseUuid";
	public final static String RACE_ID_KEY = "raceUuid";
	public final static String SERVICE_UNIQUE_ID = "serviceUID";
	public final static String RACING_EVENT_TIME = "racingEventTime";
	public final static String EXTRAS_JSON_KEY = "json";
	public final static String EXTRAS_URL = "url";
	public final static String FLAG_KEY = "raceFlag";
	
	public final static String LogFolder = "/racecommittee";
	
	// Login activity
	public final static String EventIdTag = "EventId";
	
	public static int CONNECTION_TIMEOUT = 10000;
	
	private final static String PREFERENCE_SERVICE_URL = "webserviceUrlPref";
	private final static String PREFERENCE_SENDING_ACTIVE = "sendingActivePref";
	private final static String PREFERENCE_RACE_FINISHING_TIME_FRAME = "edittextRaceTimeFrameAfterFinish";

	public static final String RESET_TIME_FRAGMENT_IS_RESET = SetTimeRaceFragment.class.getName() + ".isReset";
	
	public static final int DefaultStartTimeMinuteOffset = 10;
	
	
	public static String getURL(Context context) {
		SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(context);
		return sp.getString(PREFERENCE_SERVICE_URL, "http://racecommitteedev.sapsailing.com/racecommittee/service");
		//return sp.getString(PREFERENCE_SERVICE_URL, "http://192.168.1.100:8890/racecommittee/service");
	}
	
	public static boolean isSendingActive(Context context) {
		SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(context);
		return sp.getBoolean(PREFERENCE_SENDING_ACTIVE, false);
	}
	
	public static void setSendingActive(Context context, boolean activate) {
		SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(context);
		sp.edit().putBoolean(PREFERENCE_SENDING_ACTIVE, activate).apply();
	}
	
	public static long getRaceFinishingTimeFrame(Context context) {
		SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(context);
		String result = sp.getString(PREFERENCE_RACE_FINISHING_TIME_FRAME, "30");
		long resultLong = Long.parseLong(result);
		return (resultLong <= 0 ? 0 : resultLong * 60 * 1000);
	}
	
}
