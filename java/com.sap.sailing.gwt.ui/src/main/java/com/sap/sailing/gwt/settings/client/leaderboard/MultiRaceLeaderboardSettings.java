package com.sap.sailing.gwt.settings.client.leaderboard;

import java.util.Collection;
import java.util.List;

import com.sap.sailing.domain.common.DetailType;
import com.sap.sailing.gwt.ui.leaderboard.LeaderboardPanel;
import com.sap.sse.common.Util;
import com.sap.sse.common.settings.generic.IntegerSetting;
import com.sap.sse.common.settings.generic.StringSetSetting;
import com.sap.sse.common.settings.util.SettingsDefaultValuesUtils;

public class MultiRaceLeaderboardSettings extends LeaderboardSettings {
    private static final long serialVersionUID = -3445146715292390755L;

    /**
     * Only one of {@link #namesOfRaceColumnsToShow} and {@link #namesOfRacesToShow} must be non-<code>null</code>.
     * Only valid when the {@link #activeRaceColumnSelectionStrategy} is set to EXPLIZIT
     */
    protected StringSetSetting namesOfRaceColumnsToShow;

    /**
     * Only one of {@link #namesOfRaceColumnsToShow} and {@link #namesOfRacesToShow} must be non-<code>null</code>.
     * Only valid when the {@link #activeRaceColumnSelectionStrategy} is set to EXPLIZIT
     */
    protected StringSetSetting namesOfRacesToShow;

    /**
     * Only valid when the {@link #activeRaceColumnSelectionStrategy} is set to LAST_N
     */
    protected IntegerSetting numberOfLastRacesToShow;

    
    public MultiRaceLeaderboardSettings() {
        super();
    }

    public MultiRaceLeaderboardSettings(Collection<DetailType> maneuverDetailsToShow,
            Collection<DetailType> legDetailsToShow, Collection<DetailType> raceDetailsToShow,
            Collection<DetailType> overallDetailsToShow, List<String> namesOfRaceColumnsToShow,
            Integer numberOfLastRacesToShow,
            Long delayBetweenAutoAdvancesInMilliseconds, String nameOfRaceToSort, boolean sortAscending,
            boolean updateUponPlayStateChange, RaceColumnSelectionStrategies activeRaceColumnSelectionStrategy,
            boolean showAddedScores, boolean showOverallColumnWithNumberOfRacesCompletedPerCompetitor,
            boolean showCompetitorSailIdColumn, boolean showCompetitorFullNameColumn,
            boolean isCompetitorNationalityColumnVisible) {
        super(maneuverDetailsToShow, legDetailsToShow, raceDetailsToShow, overallDetailsToShow, delayBetweenAutoAdvancesInMilliseconds,
                nameOfRaceToSort, sortAscending, updateUponPlayStateChange, activeRaceColumnSelectionStrategy, showAddedScores,
                showOverallColumnWithNumberOfRacesCompletedPerCompetitor, showCompetitorSailIdColumn,
                showCompetitorFullNameColumn, isCompetitorNationalityColumnVisible);
        
        this.namesOfRaceColumnsToShow.setValues(namesOfRaceColumnsToShow);
        this.numberOfLastRacesToShow.setValue(numberOfLastRacesToShow);
        if (namesOfRacesToShow != null && namesOfRaceColumnsToShow != null) {
            throw new IllegalArgumentException("You can identify races either only by their race or by their column names, not both");
        }
    }

    public MultiRaceLeaderboardSettings(Iterable<String> namesOfRaceColumnsToShow) {
       this.namesOfRaceColumnsToShow.setValues(namesOfRaceColumnsToShow);
    }
    
    public LeaderboardSettings overrideDefaultsForNamesOfRaceColumns(List<String> namesOfRaceColumns) {
        MultiRaceLeaderboardSettings newSettings = new MultiRaceLeaderboardSettings();
        newSettings.legDetailsToShow.setValues(this.getLegDetailsToShow());
        newSettings.raceDetailsToShow.setValues(this.getRaceDetailsToShow());
        newSettings.overallDetailsToShow.setValues(this.getOverallDetailsToShow());
        newSettings.numberOfLastRacesToShow.setValue(this.getNumberOfLastRacesToShow());
        newSettings.activeRaceColumnSelectionStrategy.setValue(this.getActiveRaceColumnSelectionStrategy());
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
    
    @Override
    protected void addChildSettings() {
        super.addChildSettings();
        namesOfRaceColumnsToShow = new StringSetSetting("namesOfRaceColumnsToShow", this);
        namesOfRacesToShow = new StringSetSetting("namesOfRacesToShow", this, null);
        numberOfLastRacesToShow = new IntegerSetting("numberOfLastRacesToShow", this, null);
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

}
