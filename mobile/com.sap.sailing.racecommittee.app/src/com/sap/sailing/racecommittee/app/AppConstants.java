package com.sap.sailing.racecommittee.app;

public class AppConstants {

    // TODO replace it later with BuildConfig.APPLICATION_ID (Eclipse didn't know it)
    private final static String PACKAGE_NAME = "com.sap.sailing.racecommittee.app";

    private static final String PREFIX_ACTION = PACKAGE_NAME + ".action.";
    private static final String PREFIX_EXTRA = PACKAGE_NAME + ".extra.";

    public static final String EXTRA_DEFAULT = PREFIX_ACTION + "DEFAULT";

    // Intent extra fields
    public final static String EXTRA_COURSE_UUID = PREFIX_EXTRA + "COURSE_UUID";
    public final static String EXTRA_RACE_ID = PREFIX_EXTRA + "RACE_UUID";
    public final static String EXTRA_TIME_POINT_MILLIS = PREFIX_EXTRA + "TIME_POINT_MILLIS";
    public final static String EXTRA_EVENT_NAME = PREFIX_EXTRA + "EVENT_NAME";

    public final static String KEY_RACE_FLAG = "RACE_FLAG";

    public final static String AUTHOR_TYPE_OFFICER_VESSEL = "Race Officer on Vessel";
    public final static String AUTHOR_TYPE_SHORE_CONTROL = "Shore Control";
    public final static String AUTHOR_TYPE_VIEWER = "Viewer";
    public final static String AUTHOR_TYPE_SUPERUSER = "Super User";

    public final static String ACTION_RESET = PREFIX_ACTION + "RESET";
    public final static String ACTION_REGISTER_RACE = PREFIX_ACTION + "REGISTER_RACE";
    public final static String ACTION_UNREGISTER_RACE = PREFIX_ACTION + "UNREGISTER_RACE";
    public final static String ACTION_CLEAR_RACES = PREFIX_ACTION + "CLEAR_RACES";
    public final static String ACTION_ALARM_ACTION = PREFIX_ACTION + "ALARM_ACTION";
    public final static String ACTION_RELOAD_RACES = PREFIX_ACTION + "RELOAD_RACES";

    public final static String ACTION_POLLING_STOP = PREFIX_ACTION + "STOP_POLLING";
    public final static String ACTION_POLLING_RACE_ADD = PREFIX_ACTION + "ADD_RACE";
    public final static String ACTION_POLLING_RACE_REMOVE = PREFIX_ACTION + "REMOVE_RACE";
    public final static String ACTION_POLLING_POLL = PREFIX_ACTION + "POLL";

    // Login activity
    public final static String EXTRA_EVENT_ID = PREFIX_EXTRA + "EVENT_ID";

    // Inner process events
    public final static String ACTION_TOGGLE = PREFIX_ACTION + "TOGGLE";
    public final static String ACTION_ON_LIFECYCLE = PREFIX_ACTION + "ON";
    public final static String ACTION_EXTRA_FORCED = PREFIX_EXTRA + "FORCED";

    // Lifecycle events as extra
    public final static String ACTION_EXTRA_LIFECYCLE = PREFIX_EXTRA + "LIFECYCLE";
    public final static String ACTION_EXTRA_START = PREFIX_ACTION + "START";
    public final static String ACTION_EXTRA_STOP = PREFIX_EXTRA + "STOP";

    public final static String ACTION_TOGGLE_PROCEDURE = PREFIX_ACTION + "TOGGLE_PROCEDURE";
    public final static String ACTION_TOGGLE_PROCEDURE_MORE_MODE = PREFIX_ACTION + "TOGGLE_PROCEDURE_MORE_MODE";
    public final static String ACTION_TOGGLE_PROCEDURE_MORE_PATHFINDER = PREFIX_ACTION + "TOGGLE_PROCEDURE_MORE_PATHFINDER";
    public final static String ACTION_TOGGLE_PROCEDURE_MORE_TIMING = PREFIX_ACTION + "TOGGLE_PROCEDURE_MORE_TIMING";
    public final static String ACTION_TOGGLE_FACTOR = PREFIX_ACTION + "TOGGLE_FACTOR";
    public final static String ACTION_TOGGLE_COURSE = PREFIX_ACTION + "TOGGLE_COURSE";
    public final static String ACTION_TOGGLE_WIND = PREFIX_ACTION + "TOGGLE_WIND";
    public final static String ACTION_TOGGLE_TIME = PREFIX_ACTION + "TOGGLE_TIME";
    public final static String ACTION_TOGGLE_POSTPONE = PREFIX_ACTION + "TOGGLE_POSTPONE";
    public final static String ACTION_TOGGLE_ABANDON = PREFIX_ACTION + "TOGGLE_ABANDON";
    public final static String ACTION_TOGGLE_RECALL = PREFIX_ACTION + "TOGGLE_RECALL";
    public final static String ACTION_TOGGLE_BLUE_FIRST = PREFIX_ACTION + "TOGGLE_MORE";
    public final static String ACTION_TOGGLE_BLUE_LAST = PREFIX_ACTION + "TOGGLE_BLUE_LAST";
    public final static String ACTION_TOGGLE_COMPETITOR = PREFIX_ACTION + "TOGGLE_COMPETITOR";

    public final static String ACTION_TOGGLE_REPLAY = PREFIX_ACTION + "TOGGLE_REPLAY";
    public final static String ACTION_TOGGLE_PHOTOS = PREFIX_ACTION + "TOGGLE_PHOTOS";
    public final static String ACTION_TOGGLE_LIST = PREFIX_ACTION + "TOGGLE_LIST";

    public final static String ACTION_TOGGLE_EVENT = PREFIX_ACTION + "TOGGLE_EVENT";
    public final static String ACTION_TOGGLE_AREA = PREFIX_ACTION + "TOGGLE_AREA";
    public final static String ACTION_TOGGLE_POSITION = PREFIX_ACTION + "TOGGLE_POSITION";

    public final static String ACTION_TIME_HIDE = PREFIX_ACTION + "TIME_HIDE";
    public final static String ACTION_TIME_SHOW = PREFIX_ACTION + "TIME_SHOW";

    public final static String ACTION_CHECK_LOGIN = PREFIX_ACTION + "CHECK_LOGIN";
    public final static String ACTION_SHOW_ONBOARDING = PREFIX_ACTION + "SHOW_ONBOARDING";
    public final static String ACTION_SHOW_LOGIN = PREFIX_ACTION + "SHOW_LOGIN";
    public final static String ACTION_VALID_DATA = PREFIX_ACTION + "VALID_DATA";
    public final static String ACTION_SHOW_WELCOME = PREFIX_ACTION + "SHOW_WELCOME";

    // clears all toggle buttons
    public final static String ACTION_CLEAR_TOGGLE = PREFIX_ACTION + "CLEAR_TOGGLE";

    public final static String ACTION_UPDATE_SCREEN = PREFIX_ACTION + "UPDATE_SCREEN";
    public final static String ACTION_SHOW_MAIN_CONTENT = PREFIX_ACTION + "SHOW_MAIN";
    public final static String ACTION_SHOW_SUMMARY_CONTENT = PREFIX_ACTION + "SHOW_SUMMARY";

    /**
     * As extra this intent expects the String obtained for a FilterableRace / ManagedRace using
     * {@code new RaceGroupSeries(race.getRaceGroup(), race.getSeries()).getDisplayName()}.
     */
    public final static String ACTION_SHOW_PROTEST = PREFIX_ACTION + "SHOW_PROTEST";
    public final static String ACTION_REMOVE_PROTEST = PREFIX_ACTION + "REMOVE_PROTEST";

    public final static String ACTION_IS_TRACKING = PREFIX_ACTION + "IS_TRACKING";
    public final static String EXTRA_IS_TRACKING = PREFIX_EXTRA + "IS_TRACKING";

    public final static String GWT_MAP_AND_WIND_CHART_HTML = "gwt/EmbeddedMapAndWindChart.html";
}
