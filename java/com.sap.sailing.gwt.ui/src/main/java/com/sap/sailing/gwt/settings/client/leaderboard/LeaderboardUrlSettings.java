package com.sap.sailing.gwt.settings.client.leaderboard;

import com.sap.sse.common.settings.AbstractSettings;

public class LeaderboardUrlSettings extends AbstractSettings {
    public static final String PARAM_RACE_NAME = "raceName";
    public static final String PARAM_RACE_DETAIL = "raceDetail";
    public static final String PARAM_OVERALL_DETAIL = "overallDetail";
    public static final String PARAM_LEG_DETAIL = "legDetail";
    public static final String PARAM_MANEUVER_DETAIL = "maneuverDetail";
    public static final String PARAM_AUTO_EXPAND_PRESELECTED_RACE = "autoExpandPreselectedRace";
    public static final String PARAM_AUTO_EXPAND_LAST_RACE_COLUMN = "autoExpandLastRaceColumn";
    public static final String PARAM_REGATTA_NAME = "regattaName";
    public static final String PARAM_REFRESH_INTERVAL_MILLIS = "refreshIntervalMillis";
    public static final String PARAM_SHOW_ADDED_SCORES = "showAddedScores";
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
     * Lets the client choose a different race column selection which displays only up to the last N races with N being the integer
     * number specified by the parameter.
     */
    public static final String PARAM_NAME_LAST_N = "lastN";
}

