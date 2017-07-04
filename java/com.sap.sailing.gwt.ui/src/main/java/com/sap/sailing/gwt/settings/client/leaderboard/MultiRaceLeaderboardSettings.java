package com.sap.sailing.gwt.settings.client.leaderboard;

import java.util.Collection;
import java.util.List;

import com.sap.sailing.domain.common.DetailType;
import com.sap.sse.common.settings.util.SettingsDefaultValuesUtils;

public class MultiRaceLeaderboardSettings extends LeaderboardSettings {

    public MultiRaceLeaderboardSettings() {
        super();
    }

    public MultiRaceLeaderboardSettings(Collection<DetailType> maneuverDetailsToShow,
            Collection<DetailType> legDetailsToShow, Collection<DetailType> raceDetailsToShow,
            Collection<DetailType> overallDetailsToShow, List<String> namesOfRaceColumnsToShow,
            List<String> namesOfRacesToShow, Integer numberOfLastRacesToShow, boolean autoExpandPreSelectedRace,
            Long delayBetweenAutoAdvancesInMilliseconds, String nameOfRaceToSort, boolean sortAscending,
            boolean updateUponPlayStateChange, RaceColumnSelectionStrategies activeRaceColumnSelectionStrategy,
            boolean showAddedScores, boolean showOverallColumnWithNumberOfRacesCompletedPerCompetitor,
            boolean showCompetitorSailIdColumn, boolean showCompetitorFullNameColumn,
            boolean isCompetitorNationalityColumnVisible) {
        super(maneuverDetailsToShow, legDetailsToShow, raceDetailsToShow, overallDetailsToShow, namesOfRaceColumnsToShow,
                namesOfRacesToShow, numberOfLastRacesToShow, autoExpandPreSelectedRace, delayBetweenAutoAdvancesInMilliseconds,
                nameOfRaceToSort, sortAscending, updateUponPlayStateChange, activeRaceColumnSelectionStrategy, showAddedScores,
                showOverallColumnWithNumberOfRacesCompletedPerCompetitor, showCompetitorSailIdColumn,
                showCompetitorFullNameColumn, isCompetitorNationalityColumnVisible);
    }

    public MultiRaceLeaderboardSettings(Iterable<String> namesOfRaceColumnsToShow) {
        super(namesOfRaceColumnsToShow);
    }
    
    public LeaderboardSettings overrideDefaultsForNamesOfRaceColumns(List<String> namesOfRaceColumns) {
        LeaderboardSettings newSettings = new MultiRaceLeaderboardSettings();
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
