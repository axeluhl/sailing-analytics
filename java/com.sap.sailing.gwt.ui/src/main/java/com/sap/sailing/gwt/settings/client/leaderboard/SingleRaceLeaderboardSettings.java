package com.sap.sailing.gwt.settings.client.leaderboard;

import java.util.Collection;
import java.util.List;

import com.sap.sailing.domain.common.DetailType;

public class SingleRaceLeaderboardSettings extends LeaderboardSettings {

    public SingleRaceLeaderboardSettings() {
        super();
        // TODO Auto-generated constructor stub
    }

    public SingleRaceLeaderboardSettings(Collection<DetailType> maneuverDetailsToShow,
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

    public SingleRaceLeaderboardSettings(Iterable<String> namesOfRaceColumnsToShow) {
        super(namesOfRaceColumnsToShow);
    }

}
