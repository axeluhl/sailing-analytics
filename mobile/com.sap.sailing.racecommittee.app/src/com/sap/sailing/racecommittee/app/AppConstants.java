package com.sap.sailing.racecommittee.app;

import com.sap.sailing.racecommittee.app.ui.fragments.raceinfo.SetStartTimeRaceFragment;

public class AppConstants {

    //TODO replace it later with BuildConfig.APPLICATION_ID (Eclipse didn't know it)
    private final static String PACKAGE_NAME = "com.sap.sailing.racecommittee.app";

    // Intent extra fields
    public final static String COURSE_AREA_UUID_KEY = "courseUuid";
    public final static String RACE_ID_KEY = "raceUuid";
    public final static String SERVICE_UNIQUE_ID = "serviceUID";
    public final static String STARTPROCEDURE_SPECIFIC_EVENT_ID = "startProcedureSpecificEventId";
    public final static String EXTRAS_RACE_STATE_EVENT = "raceStateEvent";
    public final static String FLAG_KEY = "raceFlag";
    public final static String EXTRAS_WIND_FIX = "windfix";

    public final static String DARK_THEME = "dark";
    public final static String LIGHT_THEME = "light";
    
    public final static String INTENT_ACTION_REGISTER_RACE = "com.sap.sailing.racecommittee.app.action.registerRace";
    public final static String INTENT_ACTION_CLEAR_RACES = "com.sap.sailing.racecommittee.app.action.clearRaces";
    public final static String INTENT_ACTION_ALARM_ACTION = "com.sap.sailing.racecommittee.app.action.alarmAction";
    public final static String INTENT_ACTION_START_PROCEDURE_SPECIFIC_ACTION = "com.sap.sailing.racecommittee.app.action.startProcedureSpecificAction";

    // Login activity
    public final static String EventIdTag = "EventId";

    public static final String RESET_TIME_FRAGMENT_IS_RESET = SetStartTimeRaceFragment.class.getName() + ".isReset";

    public static final int DefaultStartTimeMinuteOffset = 10;

    // Inner process events
    public final static String INTENT_ACTION_TOGGLE = PACKAGE_NAME + ".action.toggle";

    public final static String INTENT_ACTION_EXTRA = "extra";
    public final static String INTENT_ACTION_TOGGLE_PROCEDURE = "procedure";
    public final static String INTENT_ACTION_TOGGLE_MODE = "mode";
    public final static String INTENT_ACTION_TOGGLE_COURSE = "course";
    public final static String INTENT_ACTION_TOGGLE_WIND = "wind";
    public final static String INTENT_ACTION_TOGGLE_TIME = "time";
    public final static String INTENT_ACTION_TOGGLE_POSTPONE = "postpone";
    public final static String INTENT_ACTION_TOGGLE_ABANDON = "abandon";
    public final static String INTENT_ACTION_TOGGLE_RECALL = "recall";
    public final static String INTENT_ACTION_TOGGLE_MORE = "more";

    private final static String INTENT_ACTION_TIME = PACKAGE_NAME + "action.time";
    public final static String INTENT_ACTION_TIME_HIDE = INTENT_ACTION_TIME + ".hide";
    public final static String INTENT_ACTION_TIME_SHOW = INTENT_ACTION_TIME + ".show";

    // clears all toggle buttons
    public final static String INTENT_ACTION_CLEAR_TOGGLE = PACKAGE_NAME + ".action.toggle.clear";

    public final static String INTENT_ACTION_SHOW_MAIN_CONTENT = PACKAGE_NAME + ".action.show.main";
}
