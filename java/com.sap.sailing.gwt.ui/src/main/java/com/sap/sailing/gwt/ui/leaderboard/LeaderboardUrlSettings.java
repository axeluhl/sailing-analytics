package com.sap.sailing.gwt.ui.leaderboard;

import com.sap.sse.common.settings.AbstractSettings;

public class LeaderboardUrlSettings extends AbstractSettings {
    public static final String PARAM_LEADERBOARD_GROUP_NAME = "leaderboardGroupName";
    public static final String PARAM_EVENT_ID = "eventId";
    public static final String PARAM_EMBEDDED = "embedded";
    public static final String PARAM_HIDE_TOOLBAR = "hideToolbar";
    public static final String PARAM_SHOW_RACE_DETAILS = "showRaceDetails";
    public static final String PARAM_RACE_NAME = "raceName";
    public static final String PARAM_RACE_DETAIL = "raceDetail";
    public static final String PARAM_OVERALL_DETAIL = "overallDetail";
    public static final String PARAM_LEG_DETAIL = "legDetail";
    public static final String PARAM_MANEUVER_DETAIL = "maneuverDetail";
    public static final String PARAM_AUTO_EXPAND_PRESELECTED_RACE = "autoExpandPreselectedRace";
    public static final String PARAM_AUTO_EXPAND_LAST_RACE_COLUMN = "autoExpandLastRaceColumn";
    public static final String PARAM_REGATTA_NAME = "regattaName";
    public static final String PARAM_REFRESH_INTERVAL_MILLIS = "refreshIntervalMillis";
    public static final String PARAM_SHOW_CHARTS = "showCharts";
    public static final String PARAM_CHART_DETAIL = "chartDetail";
    public static final String PARAM_SHOW_OVERALL_LEADERBOARD = "showOverallLeaderboard";
    public static final String PARAM_SHOW_SERIES_LEADERBOARDS = "showSeriesLeaderboards";
    public static final String PARAM_SHOW_ADDED_SCORES = "showAddedScores";
    public static final String PARAM_SHOW_TIME_ON_TIME_FACTOR = "showTimeOnTimeFactor";
    public static final String PARAM_SHOW_TIME_ON_DISTANCE_ALLOWANCE = "showTimeOnDistanceAllowance";
    public static final String PARAM_SHOW_OVERALL_COLUMN_WITH_NUMBER_OF_RACES_COMPLETED = "showNumberOfRacesCompleted";
    
    /**
     * Parameter to control the visibility of the competitor name columns. The following
     * values are supported: SailId and Name. If the value is empty then none of the two columns are displayed.
     * If the value can not be read then both are displayed.
     */
    public static final String PARAM_SHOW_COMPETITOR_NAME_COLUMNS="showCompetitorNameColumns";
    
    public static final String COMPETITOR_NAME_COLUMN_SAIL_ID = "SailId";
    public static final String COMPETITOR_NAME_COLUMN_FULL_NAME = "Name";
    
    /**
     * Parameter to support scaling the complete page by a given factor. This works by either using the
     * CSS3 zoom property or by applying scale operation to the body element. This comes in handy
     * when having to deal with screens that have high resolutions and that can't be controlled manually.
     * It is also a very simple method of adapting the viewport to a tv resolution. This parameter works
     * with value from 0.0 to 10.0 where 1.0 denotes the unchanged level (100%).
     */
    public static final String PARAM_ZOOM_TO = "zoomTo";
    
    /**
     * Lets the client choose a different race column selection which displays only up to the last N races with N being the integer
     * number specified by the parameter.
     */
    public static final String PARAM_NAME_LAST_N = "lastN";
}

