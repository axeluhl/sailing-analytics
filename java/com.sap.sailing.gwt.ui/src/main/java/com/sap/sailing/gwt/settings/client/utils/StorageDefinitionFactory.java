package com.sap.sailing.gwt.settings.client.utils;

import com.sap.sailing.gwt.settings.client.leaderboard.LeaderboardContextDefinition;
import com.sap.sailing.gwt.settings.client.raceboard.RaceboardContextDefinition;
import com.sap.sailing.gwt.settings.client.regattaoverview.RegattaOverviewContextDefinition;
import com.sap.sse.security.ui.settings.StorageDefinition;

/**
 * Factory for {@link StorageDefinition}s which are used in Sailing Analytics.
 * 
 * @author Vladislav Chumak
 *
 */
public class StorageDefinitionFactory {

    private StorageDefinitionFactory() {
    }

    private static final String RACEBOARD = "Raceboard";
    private static final String LEADERBOARD = "Leaderboard";
    private static final String REGATTA_OVERVIEW = "RegattaOverview";
    private static final String EVENT_REGATTA_LEADERBOARD = "EventRegattaLeaderboard";
    private static final String SERIES_OVERALL_LEADERBOARD = "SeriesOverallLeaderboard";
    private static final String SERIES_REGATTA_LEADERBOARDS = "SeriesRegattaLeaderboards";

    public static final StorageDefinition createStorageDefinitionForRaceBoard(
            RaceboardContextDefinition raceboardContextDefinition) {
        String globalDefinitionId = RACEBOARD;
        String contextDefinitionId = StorageDefinition.buildContextDefinitionId(raceboardContextDefinition.getRegattaName(),
                raceboardContextDefinition.getRaceName(), raceboardContextDefinition.getLeaderboardName());

        return new StorageDefinition(globalDefinitionId, contextDefinitionId);
    }

    public static final StorageDefinition createStorageDefinitionForLeaderboard(
            LeaderboardContextDefinition leaderboardContextDefinition) {
        return new StorageDefinition(LEADERBOARD,
                StorageDefinition.buildContextDefinitionId(leaderboardContextDefinition.getLeaderboardName()));
    }

    public static final StorageDefinition createStorageDefinitionForRegattaOverview(
            RegattaOverviewContextDefinition regattaOverviewContextDefinition) {
        return new StorageDefinition(REGATTA_OVERVIEW,
                StorageDefinition.buildContextDefinitionId(regattaOverviewContextDefinition.getEvent().toString()));
    }
    
    public static final StorageDefinition createStorageDefinitionForEventRegattaLeaderboard(String leaderboardName) {
        return new StorageDefinition(EVENT_REGATTA_LEADERBOARD,
                StorageDefinition.buildContextDefinitionId(leaderboardName));
    }
    
    public static final StorageDefinition createStorageDefinitionForSeriesOverallLeaderboard(String leaderboardName) {
        return new StorageDefinition(SERIES_OVERALL_LEADERBOARD,
                StorageDefinition.buildContextDefinitionId(leaderboardName));
    }
    
    public static final StorageDefinition createStorageDefinitionForSeriesRegattaLeaderboards(String leaderboardName) {
        return new StorageDefinition(SERIES_REGATTA_LEADERBOARDS,
                StorageDefinition.buildContextDefinitionId(leaderboardName));
    }

}
