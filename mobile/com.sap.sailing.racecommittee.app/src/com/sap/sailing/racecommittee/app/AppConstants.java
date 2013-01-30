package com.sap.sailing.racecommittee.app;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;


public class AppConstants {
	
	public final static String ApplicationVersion = "0.7 - Gloria";
	
	public final static int EXTENDED_STARTPHASE_CLASS_DISPLAYED_MINUTES_BEFORE_START = 8;
	public final static int EXTENDED_STARTPHASE_CLASS_DISPLAYED_MILLISECONDS_BEFORE_START = 
			EXTENDED_STARTPHASE_CLASS_DISPLAYED_MINUTES_BEFORE_START * 60 * 1000;

	public final static int STARTPHASE_CLASS_DISPLAYED_MINUTES_BEFORE_START = 5;
	public final static int STARTPHASE_CLASS_DISPLAYED_MILLISECONDS_BEFORE_START = STARTPHASE_CLASS_DISPLAYED_MINUTES_BEFORE_START * 60 * 1000;
	public final static int STARTPHASE_STARTMODE_DISPLAYED_MINUTES_BEFORE_START = 4;
	public final static int STARTPHASE_STARTMODE_DISPLAYED_MILLISECONDS_BEFORE_START = STARTPHASE_STARTMODE_DISPLAYED_MINUTES_BEFORE_START * 60 * 1000;
	public final static int STARTPHASE_STARTMODE_REMOVED_MINUTES_BEFORE_START = 1;
	public final static int STARTPHASE_STARTMODE_REMOVED_MILLISECONDS_BEFORE_START = STARTPHASE_STARTMODE_REMOVED_MINUTES_BEFORE_START * 60 * 1000;
	public final static int STARTPHASE_CLASS_REMOVED_MINUTES_BEFORE_START = 0;
	public final static int STARTPHASE_CLASS_REMOVED_MILLISECONDS_BEFORE_START = STARTPHASE_CLASS_REMOVED_MINUTES_BEFORE_START;
	
	public final static int STARTPHASE_ESSAP_REMOVED_MINUTES_BEFORE_START = 4;
	public final static int STARTPHASE_ESSAP_REMOVED_MILLISECONDS_BEFORE_START = STARTPHASE_ESSAP_REMOVED_MINUTES_BEFORE_START * 60 * 1000;
	public final static int STARTPHASE_ESSTHREE_DISPLAYED_MINUTES_BEFORE_START = 3;
	public final static int STARTPHASE_ESSTHREE_DISPLAYED_MILLISECONDS_BEFORE_START = STARTPHASE_ESSTHREE_DISPLAYED_MINUTES_BEFORE_START * 60 * 1000;
	public final static int STARTPHASE_ESSTWO_DISPLAYED_MINUTES_BEFORE_START = 2;
	public final static int STARTPHASE_ESSTWO_DISPLAYED_MILLISECONDS_BEFORE_START = STARTPHASE_ESSTWO_DISPLAYED_MINUTES_BEFORE_START * 60 * 1000;
	public final static int STARTPHASE_ESSONE_DISPLAYED_MINUTES_BEFORE_START = 1;
	public final static int STARTPHASE_ESSONE_DISPLAYED_MILLISECONDS_BEFORE_START = STARTPHASE_ESSONE_DISPLAYED_MINUTES_BEFORE_START * 60 * 1000;
	public final static int STARTPHASE_ESSONE_REMOVED_MINUTES_BEFORE_START = 0;
	public final static int STARTPHASE_ESSONE_REMOVED_MILLISECONDS_BEFORE_START = STARTPHASE_ESSONE_REMOVED_MINUTES_BEFORE_START;
	
	public static final int INDIVIDUAL_RECALL_TIME_MINUTES = 4;
	public static final int INDIVIDUAL_RECALL_TIME_MILLISECONDS = INDIVIDUAL_RECALL_TIME_MINUTES * 60 * 1000;
	
	public static final int DEFAULT_GATE_TIME_MINUTES = 3;
	public static final int DEFAULT_GATE_TIME_MILLISECONDS = DEFAULT_GATE_TIME_MINUTES * 60 * 1000;
	
	// -1 if not removed automatically
	//public static int GOLF_FLAG_REMOVE_TIME_MILLISECONDS = (8 * 60 * 1000) + 60000;
	
	
	// Fired by the alarm manager - racing service reacts to them
	public final static String ACTION_RACE_CLASSFLAG_UP = "com.sap.sailing.racecommittee.app.action.classFlagUp";
	public final static String ACTION_RACE_STARTMODE_UP = "com.sap.sailing.racecommittee.app.action.startModeFlagUp";
	public final static String ACTION_RACE_STARTMODE_DOWN = "com.sap.sailing.racecommittee.app.action.startModeFlagDown";
	public final static String ACTION_RACE_CLASSFLAG_DOWN = "com.sap.sailing.racecommittee.app.action.startRace";
	
	public final static String ACTION_RACE_XRAYFLAG_DOWN = "com.sap.sailing.racecommittee.app.action.xrayFlagDown";
	public final static String ACTION_RACE_GOLFFLAG_DOWN = "com.sap.sailing.racecommittee.app.action.golfFlagDown";
	
	public final static String ACTION_RACE_ESS_APFLAG_DOWN = "com.sap.sailing.racecommittee.app.action.apFlagDown";
	public final static String ACTION_RACE_ESS_THREE_UP = "com.sap.sailing.racecommittee.app.action.essThreeFlagUp";
	public final static String ACTION_RACE_ESS_TWO_UP = "com.sap.sailing.racecommittee.app.action.essTwoFlagUp";
	public final static String ACTION_RACE_ESS_ONE_UP = "com.sap.sailing.racecommittee.app.action.essOneFlagUp";
	public final static String ACTION_RACE_ESS_ONE_DOWN = "com.sap.sailing.racecommittee.app.action.essOneFlagDown";
	
	// Communication between the Connectivity watcher and the event sender
	public final static String SAVE_INTENT_ACTION = "com.sap.sailing.racecommittee.app.action.saveIntent";
	public final static String SEND_SAVED_INTENTS_ACTION = "com.sap.sailing.racecommittee.app.action.sendSavedIntents";
	public final static String REGISTER_RACE_ACTION = "com.sap.sailing.racecommittee.app.action.registerRace";
	public final static String SEND_EVENT_ACTION = "com.sap.sailing.racecommittee.app.action.sendEvent";
	
	// Intent extra fields
	public final static String COURSE_AREA_UUID_KEY = "courseUuid";
	public final static String RACE_UUID_KEY = "raceUuid";
	public final static String SERVICE_UNIQUE_ID = "serviceUID";
	public final static String RACING_EVENT_TIME = "racingEventTime";
	public final static String EXTRAS_JSON_KEY = "json";
	public final static String EXTRAS_URL = "url";
	public final static String FLAG_KEY = "raceFlag";
	
	public final static String IS_SET_FRAGMENT = "isSetFragment";
	public final static String logFolder = "/racecommittee";
	
	// Login activity
	public final static String EventIdTag = "EventId";
	
	public static int CONNECTION_TIMEOUT = 10000;
	
	private final static String PREFERENCE_SERVICE_URL = "webserviceUrlPref";
	private final static String PREFERENCE_SENDING_ACTIVE = "sendingActivePref";
	private final static String PREFERENCE_RACE_FINISHING_TIME_FRAME = "edittextRaceTimeFrameAfterFinish";
	
	
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
