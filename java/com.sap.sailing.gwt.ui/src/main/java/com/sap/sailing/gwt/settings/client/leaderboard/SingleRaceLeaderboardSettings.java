package com.sap.sailing.gwt.settings.client.leaderboard;

import java.util.Collection;

import com.sap.sailing.domain.common.DetailType;

public class SingleRaceLeaderboardSettings extends LeaderboardSettings {
    private static final long serialVersionUID = 2891220120957743158L;

    public SingleRaceLeaderboardSettings() {
        super();
    }

    public SingleRaceLeaderboardSettings(Collection<DetailType> maneuverDetailsToShow,
            Collection<DetailType> legDetailsToShow, Collection<DetailType> raceDetailsToShow,
            Collection<DetailType> overallDetailsToShow, Long delayBetweenAutoAdvancesInMilliseconds,
            boolean showAddedScores,
            boolean showOverallColumnWithNumberOfRacesCompletedPerCompetitor, boolean showCompetitorSailIdColumn,
            boolean showCompetitorFullNameColumn, boolean isCompetitorNationalityColumnVisible) {
        super(maneuverDetailsToShow, legDetailsToShow, raceDetailsToShow, overallDetailsToShow,
                delayBetweenAutoAdvancesInMilliseconds, showAddedScores,
                showOverallColumnWithNumberOfRacesCompletedPerCompetitor, showCompetitorSailIdColumn,
                showCompetitorFullNameColumn, isCompetitorNationalityColumnVisible);
    }
}
