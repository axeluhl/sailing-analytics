package com.sap.sailing.racecommittee.app;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

import com.sap.sailing.racecommittee.app.ui.fragments.raceinfo.SetStartTimeRaceFragment;

public class AppConstants {

    public static final boolean IS_DATA_OFFLINE = false;

    public final static String ApplicationVersion = "2.0 Beta 1 - Jumbotron";

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

    private final static String PREFERENCE_SERVICE_URL = "webserviceUrlPref";
    private final static String PREFERENCE_SENDING_ACTIVE = "sendingActivePref";
    private final static String PREFERENCE_RACE_FINISHING_TIME_FRAME = "edittextRaceTimeFrameAfterFinish";

    public static final String RESET_TIME_FRAGMENT_IS_RESET = SetStartTimeRaceFragment.class.getName() + ".isReset";

    public static final int DefaultStartTimeMinuteOffset = 10;

    public static String getServerBaseURL(Context context) {
        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(context);
        return sp.getString(PREFERENCE_SERVICE_URL, "http://localhost:8888");
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
