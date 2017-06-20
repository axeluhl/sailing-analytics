package com.sap.sailing.gwt.settings.client.leaderboard;

import java.util.ArrayList;
import java.util.List;

import com.sap.sailing.domain.common.DetailType;
import com.sap.sailing.gwt.settings.client.settingtypes.converter.DetailTypeStringToEnumConverter;
import com.sap.sailing.gwt.settings.client.settingtypes.converter.RaceColumnSelectionStrategiesStringToEnumConverter;
import com.sap.sailing.gwt.ui.leaderboard.LeaderboardEntryPoint;
import com.sap.sailing.gwt.ui.leaderboard.LeaderboardPanel;
import com.sap.sse.common.Util;
import com.sap.sse.common.settings.generic.AbstractGenericSerializableSettings;
import com.sap.sse.common.settings.generic.BooleanSetting;
import com.sap.sse.common.settings.generic.EnumLinkedHashSetSetting;
import com.sap.sse.common.settings.generic.EnumSetting;
import com.sap.sse.common.settings.generic.IntegerSetting;
import com.sap.sse.common.settings.generic.LongSetting;
import com.sap.sse.common.settings.generic.StringSetSetting;
import com.sap.sse.common.settings.generic.StringSetting;
import com.sap.sse.common.settings.util.SettingsDefaultValuesUtils;

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
    
    /**
     * Only one of {@link #namesOfRaceColumnsToShow} and {@link #namesOfRacesToShow} must be non-<code>null</code>.
     * Only valid when the {@link #activeRaceColumnSelectionStrategy} is set to EXPLIZIT
     */
    private StringSetSetting namesOfRaceColumnsToShow;

    /**
     * Only one of {@link #namesOfRaceColumnsToShow} and {@link #namesOfRacesToShow} must be non-<code>null</code>.
     * Only valid when the {@link #activeRaceColumnSelectionStrategy} is set to EXPLIZIT
     */
    private StringSetSetting namesOfRacesToShow;

    /**
     * Only valid when the {@link #activeRaceColumnSelectionStrategy} is set to LAST_N
     */
    private IntegerSetting numberOfLastRacesToShow;

    private EnumLinkedHashSetSetting<DetailType> maneuverDetailsToShow;
    private EnumLinkedHashSetSetting<DetailType> legDetailsToShow;
    private EnumLinkedHashSetSetting<DetailType> raceDetailsToShow;
    private EnumLinkedHashSetSetting<DetailType> overallDetailsToShow;
    private boolean autoExpandPreSelectedRace = false;
    private LongSetting delayBetweenAutoAdvancesInMilliseconds;
    private BooleanSetting updateUponPlayStateChange;
    private BooleanSetting isShowCompetitorNationality;
    
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
        isShowCompetitorNationality = new BooleanSetting("showCompetitorNationality", this, false);
        namesOfRaceColumnsToShow = new StringSetSetting("namesOfRaceColumnsToShow", this);
        namesOfRacesToShow = new StringSetSetting("namesOfRacesToShow", this, null);
        numberOfLastRacesToShow = new IntegerSetting("numberOfLastRacesToShow", this, null);
        List<DetailType> maneuverDetails = new ArrayList<DetailType>();
        maneuverDetails.add(DetailType.TACK);
        maneuverDetails.add(DetailType.JIBE);
        maneuverDetails.add(DetailType.PENALTY_CIRCLE);
        maneuverDetailsToShow = new EnumLinkedHashSetSetting<>("maneuverDetailsToShow", this, maneuverDetails, new DetailTypeStringToEnumConverter());
        List<DetailType> legDetails = new ArrayList<DetailType>();
        legDetails.add(DetailType.DISTANCE_TRAVELED);
        legDetails.add(DetailType.AVERAGE_SPEED_OVER_GROUND_IN_KNOTS);
        legDetails.add(DetailType.RANK_GAIN);
        legDetailsToShow = new EnumLinkedHashSetSetting<>("legDetailsToShow", this, legDetails, new DetailTypeStringToEnumConverter());
        List<DetailType> raceDetails = new ArrayList<DetailType>();
        raceDetails.add(DetailType.DISPLAY_LEGS);
        raceDetailsToShow = new EnumLinkedHashSetSetting<>("raceDetailsToShow", this, raceDetails, new DetailTypeStringToEnumConverter());
        List<DetailType> overallDetails = new ArrayList<>();
        overallDetails.add(DetailType.REGATTA_RANK);
        overallDetailsToShow = new EnumLinkedHashSetSetting<>("overallDetailsToShow", this, overallDetails, new DetailTypeStringToEnumConverter());
        delayBetweenAutoAdvancesInMilliseconds = new LongSetting("delayBetweenAutoAdvancesInMilliseconds", this, LeaderboardEntryPoint.DEFAULT_REFRESH_INTERVAL_MILLIS);
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
    
    /**
     * @param raceColumnsToShow <code>null</code> means don't modify the list of races shown
     */
    public LeaderboardSettings(List<DetailType> maneuverDetailsToShow, List<DetailType> legDetailsToShow,
            List<DetailType> raceDetailsToShow, List<DetailType> overallDetailsToShow,
            List<String> namesOfRaceColumnsToShow, List<String> namesOfRacesToShow, Integer numberOfLastRacesToShow,
            boolean autoExpandPreSelectedRace, Long delayBetweenAutoAdvancesInMilliseconds, String nameOfRaceToSort,
            boolean sortAscending, boolean updateUponPlayStateChange, RaceColumnSelectionStrategies activeRaceColumnSelectionStrategy,
            boolean showAddedScores, boolean showOverallColumnWithNumberOfRacesCompletedPerCompetitor,
            boolean showCompetitorSailIdColumn, boolean showCompetitorFullNameColumn,
            boolean isCompetitorNationalityColumnVisible) {
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
        this.isShowCompetitorNationality.setValue(isCompetitorNationalityColumnVisible);
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
    
    public boolean isShowCompetitorNationality() {
        return isShowCompetitorNationality.getValue();
    }

    public LeaderboardSettings overrideDefaultsForNamesOfRaceColumns(List<String> namesOfRaceColumns) {
        LeaderboardSettings newSettings = new LeaderboardSettings();
        newSettings.legDetailsToShow.setValues(this.getLegDetailsToShow());
        newSettings.raceDetailsToShow.setValues(this.getRaceDetailsToShow());
        newSettings.overallDetailsToShow.setValues(this.getOverallDetailsToShow());
        newSettings.numberOfLastRacesToShow.setValue(this.getNumberOfLastRacesToShow());
        newSettings.activeRaceColumnSelectionStrategy.setValue(this.getActiveRaceColumnSelectionStrategy());
        newSettings.autoExpandPreSelectedRace = this.isAutoExpandPreSelectedRace();
        newSettings.delayBetweenAutoAdvancesInMilliseconds.setValue(this.getDelayBetweenAutoAdvancesInMilliseconds());
        newSettings.maneuverDetailsToShow.setValues(this.getManeuverDetailsToShow());
        newSettings.nameOfRaceToSort.setValue(this.getNameOfRaceToSort());
        newSettings.sortAscending.setValue(this.isSortAscending());
        newSettings.updateUponPlayStateChange.setValue(this.isUpdateUponPlayStateChange());
        newSettings.showAddedScores.setValue(this.isShowAddedScores());
        newSettings.showCompetitorSailIdColumn.setValue(this.isShowCompetitorSailIdColumn());
        newSettings.showCompetitorFullNameColumn.setValue(this.isShowCompetitorFullNameColumn());
        newSettings.showOverallColumnWithNumberOfRacesCompletedPerCompetitor.setValue(this.isShowOverallColumnWithNumberOfRacesCompletedPerCompetitor());
        newSettings.isShowCompetitorNationality.setValue(this.isShowCompetitorNationality());
        if(namesOfRaceColumns != null && !namesOfRaceColumns.isEmpty()) {
            newSettings.namesOfRacesToShow.setValues(null);
        }
        newSettings.namesOfRaceColumnsToShow.setValues(this.getNamesOfRaceColumnsToShow());
        SettingsDefaultValuesUtils.keepDefaults(this, newSettings);
        if(namesOfRaceColumns != null && !namesOfRaceColumns.isEmpty()) {
            newSettings.namesOfRacesToShow.setDefaultValues(null);
        }
        newSettings.namesOfRaceColumnsToShow.setDefaultValues(namesOfRaceColumns);
        return newSettings;
    }
    
}
