package com.sap.sailing.gwt.settings.client.utils;

import com.sap.sailing.gwt.settings.client.leaderboard.LeaderboardContextDefinition;
import com.sap.sailing.gwt.settings.client.raceboard.RaceboardContextDefinition;
import com.sap.sailing.gwt.settings.client.regattaoverview.RegattaOverviewContextDefinition;
import com.sap.sse.security.ui.settings.StorageDefinitionId;

public class StorageDefinitionIdFactory {

    private StorageDefinitionIdFactory() {
    }

    private static final String RACEBOARD = "Raceboard";
    private static final String LEADERBOARD = "Leaderboard";
    private static final String REGATTA_OVERVIEW = "RegattaOverview";
    private static final String EVENT_REGATTA_LEADERBOARD = "EventRegattaLeaderboard";
    private static final String SERIES_OVERALL_LEADERBOARD = "SeriesOverallLeaderboard";
    private static final String SERIES_REGATTA_LEADERBOARDS = "SeriesRegattaLeaderboards";

    public static final StorageDefinitionId createStorageDefinitionIdForRaceBoard(
            RaceboardContextDefinition raceboardContextDefinition) {
        String globalDefinitionId = RACEBOARD;
        if (raceboardContextDefinition.getMode() != null) {
            globalDefinitionId += "." + raceboardContextDefinition.getMode().toString();
        }

        String contextDefinitionId = StorageDefinitionId.buildContextDefinitionId(raceboardContextDefinition.getRegattaName(),
                raceboardContextDefinition.getRaceName(), raceboardContextDefinition.getLeaderboardName());

        return new StorageDefinitionId(globalDefinitionId, contextDefinitionId);
    }

    public static final StorageDefinitionId createStorageDefinitionIdForLeaderboard(
            LeaderboardContextDefinition leaderboardContextDefinition) {
        return new StorageDefinitionId(LEADERBOARD,
                StorageDefinitionId.buildContextDefinitionId(leaderboardContextDefinition.getLeaderboardName()));
    }

    public static final StorageDefinitionId createStorageDefinitionIdForRegattaOverview(
            RegattaOverviewContextDefinition regattaOverviewContextDefinition) {
        return new StorageDefinitionId(REGATTA_OVERVIEW,
                StorageDefinitionId.buildContextDefinitionId(regattaOverviewContextDefinition.getEvent().toString()));
    }
    
    public static final StorageDefinitionId createStorageDefinitionIdForEventRegattaLeaderboard(String leaderboardName) {
        return new StorageDefinitionId(EVENT_REGATTA_LEADERBOARD,
                StorageDefinitionId.buildContextDefinitionId(leaderboardName));
    }
    
    public static final StorageDefinitionId createStorageDefinitionIdForSeriesOverallLeaderboard(String leaderboardName) {
        return new StorageDefinitionId(SERIES_OVERALL_LEADERBOARD,
                StorageDefinitionId.buildContextDefinitionId(leaderboardName));
    }
    
    public static final StorageDefinitionId createStorageDefinitionIdForSeriesRegattaLeaderboards(String leaderboardName) {
        return new StorageDefinitionId(SERIES_REGATTA_LEADERBOARDS,
                StorageDefinitionId.buildContextDefinitionId(leaderboardName));
    }

}
