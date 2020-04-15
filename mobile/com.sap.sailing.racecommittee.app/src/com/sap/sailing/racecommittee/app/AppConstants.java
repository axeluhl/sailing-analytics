package com.sap.sailing.racecommittee.app;

public class AppConstants {

    // TODO replace it later with BuildConfig.APPLICATION_ID (Eclipse didn't know it)
    private final static String PACKAGE_NAME = "com.sap.sailing.racecommittee.app";

    // Intent extra fields
    public final static String COURSE_AREA_UUID_KEY = "courseUuid";
    public final static String INTENT_EXTRA_RACE_ID = PACKAGE_NAME + ".raceUuid";
    public final static String INTENT_EXTRA_TIMEPOINT_MILLIS = PACKAGE_NAME + ".timePoint.millis";
    public final static String INTENT_EXTRA_EVENTNAME = PACKAGE_NAME + ".eventName";
    public final static String FLAG_KEY = PACKAGE_NAME + ".raceFlag";

    public final static String AUTHOR_TYPE_OFFICER_VESSEL = "Race Officer on Vessel";
    public final static String AUTHOR_TYPE_SHORE_CONTROL = "Shore Control";
    public final static String AUTHOR_TYPE_VIEWER = "Viewer";
    public final static String AUTHOR_TYPE_SUPERUSER = "Super User";

    public final static String DARK_THEME = "dark";
    public final static String LIGHT_THEME = "light";

    private final static String INTENT_ACTION = PACKAGE_NAME + ".action";
    public final static String INTENT_ACTION_RESET = INTENT_ACTION + ".reset";
    public final static String INTENT_ACTION_CLEAR_RACES = INTENT_ACTION + ".clearRaces";
    public final static String INTENT_ACTION_CLEANUP_RACES = INTENT_ACTION + ".cleanupRaces";
    public final static String INTENT_ACTION_ALARM_ACTION = INTENT_ACTION + ".alarmAction";
    public final static String INTENT_ACTION_RELOAD_RACES = INTENT_ACTION + ".reloadRaces";
    public final static String INTENT_ACTION_START_PROCEDURE_SPECIFIC_ACTION = INTENT_ACTION
            + ".startProcedureSpecificAction";

    public final static String INTENT_ACTION_POLLING_STOP = ".stopPolling";
    public final static String INTENT_ACTION_POLLING_RACE_ADD = ".addRace";
    public final static String INTENT_ACTION_POLLING_RACE_REMOVE = ".removeRace";
    public final static String INTENT_ACTION_POLLING_POLL = ".poll";

    // Login activity
    public final static String EventIdTag = "EventId";

    public static final int DefaultStartTimeMinuteOffset = 10;

    // Inner process events
    public final static String INTENT_ACTION_TOGGLE = PACKAGE_NAME + ".action.toggle";
    public final static String INTENT_ACTION_ON_LIFECYCLE = PACKAGE_NAME + ".action.on";
    public final static String INTENT_ACTION_EXTRA = PACKAGE_NAME + ".action.extra";
    public final static String INTENT_ACTION_EXTRA_FORCED = INTENT_ACTION_EXTRA + ".forced";

    // Lifecycle events as extra
    public final static String INTENT_ACTION_EXTRA_LIFECYCLE = INTENT_ACTION_EXTRA + ".lifecycle";
    public final static String INTENT_ACTION_EXTRA_START = "start";
    public final static String INTENT_ACTION_EXTRA_STOP = "stop";

    public final static String INTENT_ACTION_TOGGLE_PROCEDURE = "procedure";
    public final static String INTENT_ACTION_TOGGLE_PROCEDURE_MORE_MODE = "more_mode";
    public final static String INTENT_ACTION_TOGGLE_PROCEDURE_MORE_PATHFINDER = "more_pathfinder";
    public final static String INTENT_ACTION_TOGGLE_PROCEDURE_MORE_TIMING = "more_timing";
    public final static String INTENT_ACTION_TOGGLE_FACTOR = "factor";
    public final static String INTENT_ACTION_TOGGLE_COURSE = "course";
    public final static String INTENT_ACTION_TOGGLE_WIND = "wind";
    public final static String INTENT_ACTION_TOGGLE_TIME = "time";
    public final static String INTENT_ACTION_TOGGLE_POSTPONE = "postpone";
    public final static String INTENT_ACTION_TOGGLE_ABANDON = "abandon";
    public final static String INTENT_ACTION_TOGGLE_RECALL = "recall";
    public final static String INTENT_ACTION_TOGGLE_BLUE_FIRST = "more";
    public final static String INTENT_ACTION_TOGGLE_BLUE_LAST = "blue_last";
    public final static String INTENT_ACTION_TOGGLE_COMPETITOR = "competitor";

    public final static String INTENT_ACTION_TOGGLE_REPLAY = "replay";
    public final static String INTENT_ACTION_TOGGLE_PHOTOS = "photos";
    public final static String INTENT_ACTION_TOGGLE_LIST = "list";

    public final static String INTENT_ACTION_TOGGLE_EVENT = "event";
    public final static String INTENT_ACTION_TOGGLE_AREA = "area";
    public final static String INTENT_ACTION_TOGGLE_POSITION = "position";

    private final static String INTENT_ACTION_TIME = PACKAGE_NAME + "action.time";
    public final static String INTENT_ACTION_TIME_HIDE = INTENT_ACTION_TIME + ".hide";
    public final static String INTENT_ACTION_TIME_SHOW = INTENT_ACTION_TIME + ".show";

    public final static String INTENT_ACTION_CHECK_LOGIN = "show_empty_screen";
    public final static String INTENT_ACTION_SHOW_ONBOARDING = "show_onboarding";
    public final static String INTENT_ACTION_SHOW_LOGIN = "show_login";
    public final static String INTENT_ACTION_VALID_DATA = "valid_data";
    public final static String INTENT_ACTION_SHOW_WELCOME = "show_welcome";

    // clears all toggle buttons
    public final static String INTENT_ACTION_CLEAR_TOGGLE = PACKAGE_NAME + ".action.toggle.clear";

    public final static String INTENT_ACTION_UPDATE_SCREEN = PACKAGE_NAME + ".action.update.screen";
    public final static String INTENT_ACTION_SHOW_MAIN_CONTENT = PACKAGE_NAME + ".action.show.main";
    public final static String INTENT_ACTION_SHOW_SUMMARY_CONTENT = PACKAGE_NAME + ".action.show.summary";

    /**
     * As extra this intent expects the String obtained for a FilterableRace / ManagedRace using
     * {@code new RaceGroupSeries(race.getRaceGroup(), race.getSeries()).getDisplayName()}.
     */
    public final static String INTENT_ACTION_SHOW_PROTEST = PACKAGE_NAME + ".action.show.protest";
    public final static String INTENT_ACTION_REMOVE_PROTEST = PACKAGE_NAME + ".action.remove.protest";

    public final static String INTENT_ACTION_IS_TRACKING = PACKAGE_NAME + "action.is.tracking";
    public final static String INTENT_ACTION_IS_TRACKING_EXTRA = PACKAGE_NAME + "action.is.tracking.extra";

    public final static String GWT_MAP_AND_WIND_CHART_HTML = "gwt/EmbeddedMapAndWindChart.html";
}
