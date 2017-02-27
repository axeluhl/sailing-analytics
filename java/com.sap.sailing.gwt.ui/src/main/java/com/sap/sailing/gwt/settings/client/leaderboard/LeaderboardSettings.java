package com.sap.sailing.gwt.settings.client.leaderboard;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import com.sap.sailing.domain.common.DetailType;
import com.sap.sailing.gwt.settings.client.settingtypes.converter.DetailTypeStringToEnumConverter;
import com.sap.sailing.gwt.settings.client.settingtypes.converter.RaceColumnSelectionStrategiesStringToEnumConverter;
import com.sap.sse.common.Util;
import com.sap.sse.common.settings.generic.AbstractGenericSerializableSettings;
import com.sap.sse.common.settings.generic.BooleanSetting;
import com.sap.sse.common.settings.generic.EnumListSetting;
import com.sap.sse.common.settings.generic.EnumSetting;
import com.sap.sse.common.settings.generic.IntegerSetting;
import com.sap.sse.common.settings.generic.LongSetting;
import com.sap.sse.common.settings.generic.StringListSetting;
import com.sap.sse.common.settings.generic.StringSetting;

/**
 * Settings for the {@link LeaderboardPanel} component. If you change here, please also visit
 * {@link LeaderboardSettingsDialogComponent} to make the setting editable, and edit {@link LeaderboardUrlSettings}
 * for URL generation and parsing.
 * 
 * @author Axel Uhl (d043530)
 *
 */
public class LeaderboardSettings extends AbstractGenericSerializableSettings {
    private static final long serialVersionUID = 2625004077963291333L;
    
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
    private StringListSetting namesOfRaceColumnsToShow;

    /**
     * Only one of {@link #namesOfRaceColumnsToShow} and {@link #namesOfRacesToShow} must be non-<code>null</code>.
     * Only valid when the {@link #activeRaceColumnSelectionStrategy} is set to EXPLIZIT
     */
    private StringListSetting namesOfRacesToShow;

    /**
     * Only valid when the {@link #activeRaceColumnSelectionStrategy} is set to LAST_N
     */
    private IntegerSetting numberOfLastRacesToShow;

    private EnumListSetting<DetailType> maneuverDetailsToShow;
    private EnumListSetting<DetailType> legDetailsToShow;
    private EnumListSetting<DetailType> raceDetailsToShow;
    private EnumListSetting<DetailType> overallDetailsToShow;
    private boolean autoExpandPreSelectedRace = false;
    private LongSetting delayBetweenAutoAdvancesInMilliseconds;
    private BooleanSetting updateUponPlayStateChange;
    
    /**
     * There are two ways to select race columns.
     * Either you select races from the list of all races or you select the last N races.
     */
    public static enum RaceColumnSelectionStrategies { EXPLICIT, LAST_N; }
    
    private EnumSetting<RaceColumnSelectionStrategies> activeRaceColumnSelectionStrategy;
    
    /**
     * An optional sort column; if <code>null</code>, the leaderboard sorting won't be touched when updating the settings.
     * Otherwise, the leaderboard will be sorted by the race column (ascending if {@link #sortAscending}, descending otherwise.
     */
    private StringSetting nameOfRaceToSort;
    private BooleanSetting sortAscending;
    
    /**
     * Shows scores sum'd up for each race column
     */
    private BooleanSetting showAddedScores;
    
    private BooleanSetting showCompetitorSailIdColumn;
    private BooleanSetting showCompetitorFullNameColumn;
    
    /**
     * Show a column with total number of races completed
     */
    private BooleanSetting showOverallColumnWithNumberOfRacesCompletedPerCompetitor;
    
    @Override
    protected void addChildSettings() {
        namesOfRaceColumnsToShow = new StringListSetting("namesOfRaceColumnsToShow", this);
        namesOfRacesToShow = new StringListSetting("namesOfRacesToShow", this, null);
        numberOfLastRacesToShow = new IntegerSetting("numberOfLastRacesToShow", this, null);
        List<DetailType> maneuverDetails = new ArrayList<DetailType>();
        maneuverDetails.add(DetailType.TACK);
        maneuverDetails.add(DetailType.JIBE);
        maneuverDetails.add(DetailType.PENALTY_CIRCLE);
        maneuverDetailsToShow = new EnumListSetting<>("maneuverDetailsToShow", this, maneuverDetails, new DetailTypeStringToEnumConverter());
        List<DetailType> legDetails = new ArrayList<DetailType>();
        legDetails.add(DetailType.DISTANCE_TRAVELED);
        legDetails.add(DetailType.AVERAGE_SPEED_OVER_GROUND_IN_KNOTS);
        legDetails.add(DetailType.RANK_GAIN);
        legDetailsToShow = new EnumListSetting<>("legDetailsToShow", this, legDetails, new DetailTypeStringToEnumConverter());
        List<DetailType> raceDetails = new ArrayList<DetailType>();
        raceDetails.add(DetailType.DISPLAY_LEGS);
        raceDetailsToShow = new EnumListSetting<>("raceDetailsToShow", this, raceDetails, new DetailTypeStringToEnumConverter());
        List<DetailType> overallDetails = new ArrayList<>();
        overallDetails.add(DetailType.REGATTA_RANK);
        overallDetailsToShow = new EnumListSetting<>("overallDetailsToShow", this, overallDetails, new DetailTypeStringToEnumConverter());
        delayBetweenAutoAdvancesInMilliseconds = new LongSetting("delayBetweenAutoAdvancesInMilliseconds", this, null);
        activeRaceColumnSelectionStrategy = new EnumSetting<>("activeRaceColumnSelectionStrategy", this, RaceColumnSelectionStrategies.EXPLICIT, new RaceColumnSelectionStrategiesStringToEnumConverter());
        nameOfRaceToSort = new StringSetting("nameOfRaceToSort", this, null);
        updateUponPlayStateChange = new BooleanSetting("updateUponPlayStateChange", this, true);
        sortAscending = new BooleanSetting("sortAscending", this, true);
        showAddedScores = new BooleanSetting("showAddedScores", this, false);
        showCompetitorSailIdColumn = new BooleanSetting("showCompetitorSailIdColumn", this, true);
        showCompetitorFullNameColumn = new BooleanSetting("showCompetitorFullNameColumn", this, true);
        showOverallColumnWithNumberOfRacesCompletedPerCompetitor = new BooleanSetting("showOverallColumnWithNumberOfRacesCompletedPerCompetitor", this, false);
    }
    
    public LeaderboardSettings() {
    }
    
    public LeaderboardSettings(Iterable<String> namesOfRaceColumnsToShow) {
        this.namesOfRaceColumnsToShow.setValues(namesOfRaceColumnsToShow);
    }
    
    public LeaderboardSettings(Iterable<String> namesOfRaceColumnsToShow, Long delayBetweenAutoAdvancesInMilliseconds) {
        this.namesOfRaceColumnsToShow.setValues(namesOfRaceColumnsToShow);
        this.delayBetweenAutoAdvancesInMilliseconds.setValue(delayBetweenAutoAdvancesInMilliseconds);
    }
    
    public LeaderboardSettings(Long delayBetweenAutoAdvancesInMilliseconds, Integer numberOfLastRacesToShow,
            RaceColumnSelectionStrategies activeRaceColumnSelectionStrategy) {
        this.delayBetweenAutoAdvancesInMilliseconds.setValue(delayBetweenAutoAdvancesInMilliseconds);
        this.numberOfLastRacesToShow.setValue(numberOfLastRacesToShow);
        this.activeRaceColumnSelectionStrategy.setValue(activeRaceColumnSelectionStrategy);
    }
    
    public LeaderboardSettings(List<String> namesOfRaceColumnsToShow, List<String> namesOfRacesToShow, List<DetailType> overallDetailsToShow,
            String nameOfRaceToSort, boolean autoExpandPreSelectedRace,
            boolean showCompetitorSailIdColumn, boolean showCompetitorFullNameColumn) {
        this.namesOfRaceColumnsToShow.setValues(namesOfRaceColumnsToShow);
        this.namesOfRacesToShow.setValues(namesOfRacesToShow);
        this.overallDetailsToShow.setValues(overallDetailsToShow);
        this.nameOfRaceToSort.setValue(nameOfRaceToSort);
        this.autoExpandPreSelectedRace = autoExpandPreSelectedRace;
        this.showCompetitorSailIdColumn.setValue(showCompetitorSailIdColumn);
        this.showCompetitorFullNameColumn.setValue(showCompetitorFullNameColumn);
    }
    
    /**
     * @param raceColumnsToShow <code>null</code> means don't modify the list of races shown
     */
    public LeaderboardSettings(List<DetailType> maneuverDetailsToShow, List<DetailType> legDetailsToShow,
            List<DetailType> raceDetailsToShow, List<DetailType> overallDetailsToShow,
            List<String> namesOfRaceColumnsToShow, List<String> namesOfRacesToShow, Integer numberOfLastRacesToShow,
            boolean autoExpandPreSelectedRace, Long delayBetweenAutoAdvancesInMilliseconds, String nameOfRaceToSort,
            boolean sortAscending, boolean updateUponPlayStateChange, RaceColumnSelectionStrategies activeRaceColumnSelectionStrategy,
            boolean showAddedScores, boolean showOverallColumnWithNumberOfRacesCompletedPerCompetitor,
            boolean showCompetitorSailIdColumn, boolean showCompetitorFullNameColumn) {
        if (namesOfRacesToShow != null && namesOfRaceColumnsToShow != null) {
            throw new IllegalArgumentException("You can identify races either only by their race or by their column names, not both");
        }
        this.legDetailsToShow.setValues(legDetailsToShow);
        this.raceDetailsToShow.setValues(raceDetailsToShow);
        this.overallDetailsToShow.setValues(overallDetailsToShow);
        this.namesOfRacesToShow.setValues(namesOfRacesToShow);
        this.namesOfRaceColumnsToShow.setValues(namesOfRaceColumnsToShow);
        this.numberOfLastRacesToShow.setValue(numberOfLastRacesToShow);
        this.activeRaceColumnSelectionStrategy.setValue(activeRaceColumnSelectionStrategy);
        this.autoExpandPreSelectedRace = autoExpandPreSelectedRace;
        this.delayBetweenAutoAdvancesInMilliseconds.setValue(delayBetweenAutoAdvancesInMilliseconds);
        this.maneuverDetailsToShow.setValues(maneuverDetailsToShow);
        this.nameOfRaceToSort.setValue(nameOfRaceToSort);
        this.sortAscending.setValue(sortAscending);
        this.updateUponPlayStateChange.setValue(updateUponPlayStateChange);
        this.showAddedScores.setValue(showAddedScores);
        this.showCompetitorSailIdColumn.setValue(showCompetitorSailIdColumn);
        this.showCompetitorFullNameColumn.setValue(showCompetitorFullNameColumn);
        this.showOverallColumnWithNumberOfRacesCompletedPerCompetitor.setValue(showOverallColumnWithNumberOfRacesCompletedPerCompetitor);
    }
  
    /**
     * A live collection that reflects the current state of the settings of a leaderboard panel
     */
    public List<DetailType> getManeuverDetailsToShow() {
        return Util.createList(maneuverDetailsToShow.getValues());
    }

    /**
     * A live collection that reflects the current state of the settings of a leaderboard panel
     */
    public List<DetailType> getLegDetailsToShow() {
        return Util.createList(legDetailsToShow.getValues());
    }

    /**
     * A live collection that reflects the current state of the settings of a leaderboard panel
     */
    public List<DetailType> getRaceDetailsToShow() {
        return Util.createList(raceDetailsToShow.getValues());
    }
    
    /**
     * A live collection that reflects the current state of the settings of a leaderboard panel
     */
    public List<DetailType> getOverallDetailsToShow() {
        return Util.createList(overallDetailsToShow.getValues());
    }
    
    /**
     * If <code>null</code>, this is to mean that the race columns should not be modified by
     * {@link LeaderboardPanel#updateSettings(LeaderboardSettings)}. Otherwise a
     * live collection that reflects the current state of the settings of a leaderboard panel
     */
    public List<String> getNamesOfRaceColumnsToShow() {
        return activeRaceColumnSelectionStrategy.getValue() == RaceColumnSelectionStrategies.EXPLICIT ? (namesOfRaceColumnsToShow.isValuesEmpty() ? null : Util.createList(namesOfRaceColumnsToShow.getValues())) : null;
    }

    /**
     * If <code>null</code>, this is to mean that the race columns should not be modified by
     * {@link LeaderboardPanel#updateSettings(LeaderboardSettings)}. Otherwise
     * a live collection that reflects the current state of the settings of a leaderboard panel
     */
    public List<String> getNamesOfRacesToShow() {
        return activeRaceColumnSelectionStrategy.getValue() == RaceColumnSelectionStrategies.EXPLICIT ? (namesOfRacesToShow.isValuesEmpty() ? null : Util.createList(namesOfRacesToShow.getValues())) : null;
    }
    
    /**
     * If <code>null</code>, this is to mean that the race columns should not be modified by
     * {@link LeaderboardPanel#updateSettings(LeaderboardSettings)}.
     */
    public Integer getNumberOfLastRacesToShow() {
        return activeRaceColumnSelectionStrategy.getValue() == RaceColumnSelectionStrategies.LAST_N ? numberOfLastRacesToShow.getValue() : null;
    }

    public boolean isAutoExpandPreSelectedRace() {
        return autoExpandPreSelectedRace;
    }

    /**
     * @return if <code>null</code>, leave refresh interval alone (don't change in
     *         {@link LeaderboardPanel#updateSettings(LeaderboardSettings)}
     */
    public Long getDelayBetweenAutoAdvancesInMilliseconds() {
        return delayBetweenAutoAdvancesInMilliseconds.getValue();
    }

    public String getNameOfRaceToSort() {
        return nameOfRaceToSort.getValue();
    }

    public boolean isSortAscending() {
        return sortAscending.getValue();
    }

    /**
     * If <code>true</code>, an update of the settings will behave like a manual settings update, meaning that
     * the settings won't automatically be replaced / adjusted when the play state changes.
     */
    public boolean isUpdateUponPlayStateChange() {
        return updateUponPlayStateChange.getValue();
    }

    public RaceColumnSelectionStrategies getActiveRaceColumnSelectionStrategy() {
        return activeRaceColumnSelectionStrategy.getValue();
    }

    public boolean isShowAddedScores() {
        return showAddedScores.getValue();
    }
    
    public boolean isShowOverallColumnWithNumberOfRacesCompletedPerCompetitor() {
        return showOverallColumnWithNumberOfRacesCompletedPerCompetitor.getValue();
    }
    
    public boolean isShowCompetitorSailIdColumn() {
        return showCompetitorSailIdColumn.getValue();
    }
    
    public boolean isShowCompetitorFullNameColumn() {
        return showCompetitorFullNameColumn.getValue();
    }
    
    void overrideDefaultValues(LeaderboardSettings newDefaults) {
        autoExpandPreSelectedRace = newDefaults.autoExpandPreSelectedRace;
        boolean namesOfRaceColumnsToShowWasDefault = Util.equals(namesOfRaceColumnsToShow.getValues(), namesOfRaceColumnsToShow.getDefaultValues());
        namesOfRaceColumnsToShow.setDefaultValues(newDefaults.namesOfRaceColumnsToShow.getValues());
        namesOfRacesToShow.setDefaultValues(newDefaults.namesOfRacesToShow.getValues());
        if(getNamesOfRaceColumnsToShow() != null && getNamesOfRacesToShow() != null) {
            if(namesOfRaceColumnsToShowWasDefault) {
                namesOfRaceColumnsToShow.setValues(null);
            } else {
                namesOfRacesToShow.setValues(null);
            }
        }
        numberOfLastRacesToShow.setDefaultValue(newDefaults.numberOfLastRacesToShow.getValue());
        maneuverDetailsToShow.setDefaultValues(newDefaults.maneuverDetailsToShow.getValues());
        legDetailsToShow.setDefaultValues(newDefaults.legDetailsToShow.getValues());
        raceDetailsToShow.setDefaultValues(newDefaults.raceDetailsToShow.getValues());
        overallDetailsToShow.setDefaultValues(newDefaults.overallDetailsToShow.getValues());
        delayBetweenAutoAdvancesInMilliseconds.setDefaultValue(newDefaults.delayBetweenAutoAdvancesInMilliseconds.getValue());
        activeRaceColumnSelectionStrategy.setDefaultValue(newDefaults.activeRaceColumnSelectionStrategy.getValue());
        nameOfRaceToSort.setDefaultValue(newDefaults.nameOfRaceToSort.getValue());
        updateUponPlayStateChange.setDefaultValue(newDefaults.updateUponPlayStateChange.getValue());
        sortAscending.setDefaultValue(newDefaults.sortAscending.getValue());
        showAddedScores.setDefaultValue(newDefaults.showAddedScores.getValue());
        showCompetitorSailIdColumn.setDefaultValue(newDefaults.showCompetitorSailIdColumn.getValue());
        showCompetitorFullNameColumn.setDefaultValue(newDefaults.showCompetitorFullNameColumn.getValue());
        showOverallColumnWithNumberOfRacesCompletedPerCompetitor.setDefaultValue(newDefaults.showOverallColumnWithNumberOfRacesCompletedPerCompetitor.getValue());
    }
    
    void setValues(LeaderboardSettings settingsWithCustomValues) {
        this.legDetailsToShow.setValues(settingsWithCustomValues.getLegDetailsToShow());
        this.raceDetailsToShow.setValues(settingsWithCustomValues.getRaceDetailsToShow());
        this.overallDetailsToShow.setValues(settingsWithCustomValues.getOverallDetailsToShow());
        this.namesOfRacesToShow.setValues(settingsWithCustomValues.getNamesOfRacesToShow());
        this.namesOfRaceColumnsToShow.setValues(settingsWithCustomValues.getNamesOfRaceColumnsToShow());
        this.numberOfLastRacesToShow.setValue(settingsWithCustomValues.getNumberOfLastRacesToShow());
        this.activeRaceColumnSelectionStrategy.setValue(settingsWithCustomValues.getActiveRaceColumnSelectionStrategy());
        this.autoExpandPreSelectedRace = settingsWithCustomValues.isAutoExpandPreSelectedRace();
        this.delayBetweenAutoAdvancesInMilliseconds.setValue(settingsWithCustomValues.getDelayBetweenAutoAdvancesInMilliseconds());
        this.maneuverDetailsToShow.setValues(settingsWithCustomValues.getManeuverDetailsToShow());
        this.nameOfRaceToSort.setValue(settingsWithCustomValues.getNameOfRaceToSort());
        this.sortAscending.setValue(settingsWithCustomValues.isSortAscending());
        this.updateUponPlayStateChange.setValue(settingsWithCustomValues.isUpdateUponPlayStateChange());
        this.showAddedScores.setValue(settingsWithCustomValues.isShowAddedScores());
        this.showCompetitorSailIdColumn.setValue(settingsWithCustomValues.isShowCompetitorSailIdColumn());
        this.showCompetitorFullNameColumn.setValue(settingsWithCustomValues.isShowCompetitorFullNameColumn());
        this.showOverallColumnWithNumberOfRacesCompletedPerCompetitor.setValue(settingsWithCustomValues.isShowOverallColumnWithNumberOfRacesCompletedPerCompetitor());
    }
    
    public LeaderboardSettings getDefaultSettings() {
        LeaderboardSettings leaderboardSettings = new LeaderboardSettings();
        leaderboardSettings.legDetailsToShow.setDefaultValues(legDetailsToShow.getDefaultValues());
        leaderboardSettings.raceDetailsToShow.setDefaultValues(raceDetailsToShow.getDefaultValues());
        leaderboardSettings.overallDetailsToShow.setDefaultValues(overallDetailsToShow.getDefaultValues());
        leaderboardSettings.namesOfRacesToShow.setDefaultValues(namesOfRacesToShow.getDefaultValues());
        leaderboardSettings.namesOfRaceColumnsToShow.setDefaultValues(namesOfRaceColumnsToShow.getDefaultValues());
        leaderboardSettings.numberOfLastRacesToShow.setDefaultValue(numberOfLastRacesToShow.getDefaultValue());
        leaderboardSettings.activeRaceColumnSelectionStrategy.setDefaultValue(activeRaceColumnSelectionStrategy.getDefaultValue());
        leaderboardSettings.autoExpandPreSelectedRace = autoExpandPreSelectedRace;
        leaderboardSettings.delayBetweenAutoAdvancesInMilliseconds.setDefaultValue(delayBetweenAutoAdvancesInMilliseconds.getDefaultValue());
        leaderboardSettings.maneuverDetailsToShow.setDefaultValues(maneuverDetailsToShow.getDefaultValues());
        leaderboardSettings.nameOfRaceToSort.setDefaultValue(nameOfRaceToSort.getDefaultValue());
        leaderboardSettings.sortAscending.setDefaultValue(sortAscending.getDefaultValue());
        leaderboardSettings.updateUponPlayStateChange.setDefaultValue(updateUponPlayStateChange.getDefaultValue());
        leaderboardSettings.showAddedScores.setDefaultValue(showAddedScores.getDefaultValue());
        leaderboardSettings.showCompetitorSailIdColumn.setDefaultValue(showCompetitorSailIdColumn.getDefaultValue());
        leaderboardSettings.showCompetitorFullNameColumn.setDefaultValue(showCompetitorFullNameColumn.getDefaultValue());
        leaderboardSettings.showOverallColumnWithNumberOfRacesCompletedPerCompetitor.setDefaultValue(showOverallColumnWithNumberOfRacesCompletedPerCompetitor.getDefaultValue());
        return leaderboardSettings;
    }
    
    public static LeaderboardSettings createDefaultSettings(Collection<String> defaultNamesOfRaceColumnsToShow) {
        LeaderboardSettings leaderboardSettings = new LeaderboardSettings();
        leaderboardSettings.namesOfRaceColumnsToShow.setDefaultValues(defaultNamesOfRaceColumnsToShow);
        
        return leaderboardSettings;
    }
}
