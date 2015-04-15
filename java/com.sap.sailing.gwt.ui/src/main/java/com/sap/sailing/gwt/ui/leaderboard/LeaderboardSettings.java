package com.sap.sailing.gwt.ui.leaderboard;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import com.sap.sailing.domain.common.DetailType;
import com.sap.sse.common.settings.Settings;

/**
 * Settings for the {@link LeaderboardPanel} component. If you change here, please also visit
 * {@link LeaderboardSettingsDialogComponent} to make the setting editable, and edit {@link LeaderboardUrlSettings}
 * for URL generation and parsing.
 * 
 * @author Axel Uhl (d043530)
 *
 */
public class LeaderboardSettings implements Settings {
    public static final String PARAM_LEADERBOARD_GROUP_NAME = "leaderboardGroupName";
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
    public static final String PARAM_SHOW_OVERALL_COLUMN_WITH_NUMBER_OF_RACES_COMPLETED = "showNumberOfRacesCompleted";
    
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


    /**
     * Only one of {@link #namesOfRaceColumnsToShow} and {@link #namesOfRacesToShow} must be non-<code>null</code>.
     * Only valid when the {@link #activeRaceColumnSelectionStrategy} is set to EXPLIZIT
     */
    private final List<String> namesOfRaceColumnsToShow;

    /**
     * Only one of {@link #namesOfRaceColumnsToShow} and {@link #namesOfRacesToShow} must be non-<code>null</code>.
     * Only valid when the {@link #activeRaceColumnSelectionStrategy} is set to EXPLIZIT
     */
    private final List<String> namesOfRacesToShow;

    /**
     * Only valid when the {@link #activeRaceColumnSelectionStrategy} is set to LAST_N
     */
    private final Integer numberOfLastRacesToShow;

    private final List<DetailType> maneuverDetailsToShow;
    private final List<DetailType> legDetailsToShow;
    private final List<DetailType> raceDetailsToShow;
    private final List<DetailType> overallDetailsToShow;
    private final boolean autoExpandPreSelectedRace;
    private final Long delayBetweenAutoAdvancesInMilliseconds;
    private final boolean updateUponPlayStateChange;
    
    /**
     * There are two ways to select race columns.
     * Either you select races from the list of all races or you select the last N races.
     */
    public static enum RaceColumnSelectionStrategies { EXPLICIT, LAST_N; }
    private final RaceColumnSelectionStrategies activeRaceColumnSelectionStrategy;
    
    /**
     * An optional sort column; if <code>null</code>, the leaderboard sorting won't be touched when updating the settings.
     * Otherwise, the leaderboard will be sorted by the race column (ascending if {@link #sortAscending}, descending otherwise.
     */
    private final String nameOfRaceToSort;
    private final boolean sortAscending;
    
    /**
     * Shows scores sum'd up for each race column
     */
    private final boolean showAddedScores;
    
    private final boolean showCompetitorSailIdColumn;
    private final boolean showCompetitorFullNameColumn;
    
    /**
     * Show a column with total number of races completed
     */
    private final boolean showOverallColumnWithNumberOfRacesCompletedPerCompetitor;
    
    /**
     * @param raceColumnsToShow <code>null</code> means don't modify the list of races shown
     */
    public LeaderboardSettings(List<DetailType> meneuverDetailsToShow, List<DetailType> legDetailsToShow,
            List<DetailType> raceDetailsToShow, List<DetailType> overallDetailsToShow,
            List<String> namesOfRaceColumnsToShow, List<String> namesOfRacesToShow, Integer numberOfLastRacesToShow,
            boolean autoExpandPreSelectedRace, Long delayBetweenAutoAdvancesInMilliseconds, String nameOfRaceToSort,
            boolean sortAscending, boolean updateUponPlayStateChange, RaceColumnSelectionStrategies activeRaceColumnSelectionStrategy,
            boolean showAddedScores, boolean showOverallColumnWithNumberOfRacesCompletedPerCompetitor,
            boolean showCompetitorSailIdColumn, boolean showCompetitorFullNameColumn) {
        if (namesOfRacesToShow != null && namesOfRaceColumnsToShow != null) {
            throw new IllegalArgumentException("You can identify races either only by their race or by their column names, not both");
        }
        this.legDetailsToShow = legDetailsToShow;
        this.raceDetailsToShow = raceDetailsToShow;
        this.overallDetailsToShow = overallDetailsToShow;
        this.namesOfRacesToShow = namesOfRacesToShow;
        this.namesOfRaceColumnsToShow = namesOfRaceColumnsToShow;
        this.numberOfLastRacesToShow = numberOfLastRacesToShow;
        this.activeRaceColumnSelectionStrategy = activeRaceColumnSelectionStrategy;
        this.autoExpandPreSelectedRace = autoExpandPreSelectedRace;
        this.delayBetweenAutoAdvancesInMilliseconds = delayBetweenAutoAdvancesInMilliseconds;
        this.maneuverDetailsToShow = meneuverDetailsToShow;
        this.nameOfRaceToSort = nameOfRaceToSort;
        this.sortAscending = sortAscending;
        this.updateUponPlayStateChange = updateUponPlayStateChange;
        this.showAddedScores = showAddedScores;
        this.showCompetitorSailIdColumn = showCompetitorSailIdColumn;
        this.showCompetitorFullNameColumn = showCompetitorFullNameColumn;
        this.showOverallColumnWithNumberOfRacesCompletedPerCompetitor = showOverallColumnWithNumberOfRacesCompletedPerCompetitor;
    }
  
    public List<DetailType> getManeuverDetailsToShow() {
        return maneuverDetailsToShow;
    }

    public List<DetailType> getLegDetailsToShow() {
        return legDetailsToShow;
    }

    public List<DetailType> getRaceDetailsToShow() {
        return raceDetailsToShow;
    }
    
    public List<DetailType> getOverallDetailsToShow() {
        return overallDetailsToShow;
    }
    
    /**
     * If <code>null</code>, this is to mean that the race columns should not be modified by
     * {@link LeaderboardPanel#updateSettings(LeaderboardSettings)}.
     */
    public List<String> getNamesOfRaceColumnsToShow() {
        return activeRaceColumnSelectionStrategy == RaceColumnSelectionStrategies.EXPLICIT ? namesOfRaceColumnsToShow : null;
    }

    /**
     * If <code>null</code>, this is to mean that the race columns should not be modified by
     * {@link LeaderboardPanel#updateSettings(LeaderboardSettings)}.
     */
    public List<String> getNamesOfRacesToShow() {
        return activeRaceColumnSelectionStrategy == RaceColumnSelectionStrategies.EXPLICIT ? namesOfRacesToShow : null;
    }
    
    /**
     * If <code>null</code>, this is to mean that the race columns should not be modified by
     * {@link LeaderboardPanel#updateSettings(LeaderboardSettings)}.
     */
    public Integer getNumberOfLastRacesToShow() {
        return activeRaceColumnSelectionStrategy == RaceColumnSelectionStrategies.LAST_N ? numberOfLastRacesToShow : null;
    }

    public boolean isAutoExpandPreSelectedRace() {
        return autoExpandPreSelectedRace;
    }

    /**
     * @return if <code>null</code>, leave refresh interval alone (don't change in
     *         {@link LeaderboardPanel#updateSettings(LeaderboardSettings)}
     */
    public Long getDelayBetweenAutoAdvancesInMilliseconds() {
        return delayBetweenAutoAdvancesInMilliseconds;
    }

    public String getNameOfRaceToSort() {
        return nameOfRaceToSort;
    }

    public boolean isSortAscending() {
        return sortAscending;
    }

    /**
     * If <code>true</code>, an update of the settings will behave like a manual settings update, meaning that
     * the settings won't automatically be replaced / adjusted when the play state changes.
     */
    public boolean isUpdateUponPlayStateChange() {
        return updateUponPlayStateChange;
    }

    public RaceColumnSelectionStrategies getActiveRaceColumnSelectionStrategy() {
        return activeRaceColumnSelectionStrategy;
    }

    public boolean isShowAddedScores() {
        return showAddedScores;
    }
    
    public boolean isShowOverallColumnWithNumberOfRacesCompletedPerCompetitor() {
        return showOverallColumnWithNumberOfRacesCompletedPerCompetitor;
    }
    
    /**
     * Constructs {@link LeaderboardSettings} from the URL parameters found
     */
    public static LeaderboardSettings createLeaderboardSettingsFromURLParameters(Map<String, List<String>> parameterMap) {
        LeaderboardSettings result;
        Long refreshIntervalMillis = parameterMap.containsKey(PARAM_REFRESH_INTERVAL_MILLIS) ? Long
                .valueOf(parameterMap.get(PARAM_REFRESH_INTERVAL_MILLIS).get(0)) : null;
        RaceColumnSelectionStrategies raceColumnSelectionStrategy;
        final Integer numberOfLastRacesToShow;
        if (parameterMap.containsKey(PARAM_NAME_LAST_N)) {
            raceColumnSelectionStrategy = RaceColumnSelectionStrategies.LAST_N;
            numberOfLastRacesToShow = Integer.valueOf(parameterMap.get(PARAM_NAME_LAST_N).get(0));
        } else {
            raceColumnSelectionStrategy = RaceColumnSelectionStrategies.EXPLICIT;
            numberOfLastRacesToShow = null;
        }
        if (parameterMap.containsKey(PARAM_RACE_NAME) || parameterMap.containsKey(PARAM_RACE_DETAIL) ||
                parameterMap.containsKey(PARAM_LEG_DETAIL) || parameterMap.containsKey(PARAM_MANEUVER_DETAIL) ||
                parameterMap.containsKey(PARAM_OVERALL_DETAIL) || parameterMap.containsKey(PARAM_SHOW_ADDED_SCORES) ||
                parameterMap.containsKey(PARAM_SHOW_OVERALL_COLUMN_WITH_NUMBER_OF_RACES_COMPLETED)) {
            List<DetailType> maneuverDetails = getDetailTypeListFromParamValue(parameterMap.get(PARAM_MANEUVER_DETAIL));
            List<DetailType> raceDetails = getDetailTypeListFromParamValue(parameterMap.get(PARAM_RACE_DETAIL));
            List<DetailType> overallDetails = getDetailTypeListFromParamValue(parameterMap.get(PARAM_OVERALL_DETAIL));
            List<DetailType> legDetails = getDetailTypeListFromParamValue(parameterMap.get(PARAM_LEG_DETAIL));
            List<String> namesOfRacesToShow = getStringListFromParamValue(parameterMap.get(PARAM_RACE_NAME));
            boolean showAddedScores = parameterMap.containsKey(PARAM_SHOW_ADDED_SCORES) ? 
                    Boolean.valueOf(parameterMap.get(PARAM_SHOW_ADDED_SCORES).get(0)) : false;
            boolean showOverallColumnWithNumberOfRacesSailedPerCompetitor = parameterMap.containsKey(PARAM_SHOW_OVERALL_COLUMN_WITH_NUMBER_OF_RACES_COMPLETED) ?
                    Boolean.valueOf(parameterMap.get(PARAM_SHOW_OVERALL_COLUMN_WITH_NUMBER_OF_RACES_COMPLETED).get(0)) : false;
            boolean autoExpandPreSelectedRace = parameterMap.containsKey(PARAM_AUTO_EXPAND_PRESELECTED_RACE) ?
                    Boolean.valueOf(parameterMap.get(PARAM_AUTO_EXPAND_PRESELECTED_RACE).get(0)) :
                        (namesOfRacesToShow != null && namesOfRacesToShow.size() == 1);
            boolean showCompetitorSailIdColumn = true;
            boolean showCompetitorFullNameColumn = true;
            if (parameterMap.containsKey(LeaderboardUrlSettings.PARAM_SHOW_COMPETITOR_NAME_COLUMNS)) {
                 String value = parameterMap.get(LeaderboardUrlSettings.PARAM_SHOW_COMPETITOR_NAME_COLUMNS).get(0);
                 if (value.equals(LeaderboardUrlSettings.COMPETITOR_NAME_COLUMN_FULL_NAME)) {
                     showCompetitorSailIdColumn = false;
                 } else if (value.equals(LeaderboardUrlSettings.COMPETITOR_NAME_COLUMN_SAIL_ID)) {
                     showCompetitorFullNameColumn = false;
                 } else if (value.trim().equals("")) {
                     showCompetitorFullNameColumn = false;
                     showCompetitorSailIdColumn = false;
                 }
             }
             result = new LeaderboardSettings(maneuverDetails, legDetails, raceDetails, overallDetails,
                     /* namesOfRaceColumnsToShow */ null,
                     namesOfRacesToShow, numberOfLastRacesToShow,
                     autoExpandPreSelectedRace, refreshIntervalMillis, /* sort by column */ (namesOfRacesToShow != null && !namesOfRacesToShow.isEmpty()) ?
                                     namesOfRacesToShow.get(0) : null,
                             /* ascending */ true, /* updateUponPlayStateChange */ raceDetails.isEmpty() && legDetails.isEmpty(),
                                     raceColumnSelectionStrategy, showAddedScores, showOverallColumnWithNumberOfRacesSailedPerCompetitor,
                                     showCompetitorSailIdColumn, showCompetitorFullNameColumn);
        } else {
            final List<DetailType> overallDetails = Collections.singletonList(DetailType.REGATTA_RANK);
            result = LeaderboardSettingsFactory.getInstance().createNewDefaultSettings(null, null,
                    /* overallDetails */ overallDetails, null,
                    /* autoExpandFirstRace */false, refreshIntervalMillis, numberOfLastRacesToShow,
                    raceColumnSelectionStrategy);
        }
        return result;
    }

    private static List<DetailType> getDetailTypeListFromParamValue(List<String> list) {
        List<DetailType> result = new ArrayList<DetailType>();
        if (list != null) {
            for (String entry : list) {
                try {
                    result.add(DetailType.valueOf(entry));
                } catch (IllegalArgumentException e) {
                }
            }
        }
        return result;
    }

    private static List<String> getStringListFromParamValue(List<String> list) {
        List<String> result = new ArrayList<String>();
        if (list != null) {
            result.addAll(list);
        }
        return result;
    }
    
    public boolean isShowCompetitorSailIdColumn() {
        return showCompetitorSailIdColumn;
    }
    
    public boolean isShowCompetitorFullNameColumn() {
        return showCompetitorFullNameColumn;
    }
}
