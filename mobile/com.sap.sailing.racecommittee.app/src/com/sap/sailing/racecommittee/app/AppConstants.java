package com.sap.sailing.racecommittee.app;

import com.sap.sailing.racecommittee.app.ui.fragments.raceinfo.SetStartTimeRaceFragment;

public class AppConstants {
    // Intent extra fields
    public final static String COURSE_AREA_UUID_KEY = "courseUuid";
    public final static String RACE_ID_KEY = "raceUuid";
    public final static String SERVICE_UNIQUE_ID = "serviceUID";
    public final static String STARTPROCEDURE_SPECIFIC_EVENT_ID = "startProcedureSpecificEventId";
    public final static String EXTRAS_RACE_STATE_EVENT = "raceStateEvent";
    public final static String FLAG_KEY = "raceFlag";
    public final static String EXTRAS_WIND_FIX = "windfix";

    public final static String DARK_THEME = "0";
    public final static String LIGHT_THEME = "1";
    
    public final static String INTENT_ACTION_REGISTER_RACE = "com.sap.sailing.racecommittee.app.action.registerRace";
    public final static String INTENT_ACTION_CLEAR_RACES = "com.sap.sailing.racecommittee.app.action.clearRaces";
    public final static String INTENT_ACTION_ALARM_ACTION = "com.sap.sailing.racecommittee.app.action.alarmAction";
    public final static String INTENT_ACTION_START_PROCEDURE_SPECIFIC_ACTION = "com.sap.sailing.racecommittee.app.action.startProcedureSpecificAction";

    // Login activity
    public final static String EventIdTag = "EventId";

    public static final String RESET_TIME_FRAGMENT_IS_RESET = SetStartTimeRaceFragment.class.getName() + ".isReset";

    public static final int DefaultStartTimeMinuteOffset = 10;
}
