package com.sap.sailing.gwt.settings.client.utils;

import com.sap.sailing.gwt.settings.client.leaderboard.LeaderboardContextDefinition;
import com.sap.sailing.gwt.settings.client.raceboard.RaceboardContextDefinition;
import com.sap.sailing.gwt.settings.client.regattaoverview.RegattaOverviewContextDefinition;
import com.sap.sse.security.ui.settings.StoredSettingsLocator;

/**
 * Factory for {@link StoredSettingsLocator}s which are used in Sailing Analytics.
 * 
 * @author Vladislav Chumak
 *
 */
public class StoredSettingsLocatorFactory {

    private StoredSettingsLocatorFactory() {
    }

    private static final String RACEBOARD = "Raceboard";
    private static final String LEADERBOARD = "Leaderboard";
    private static final String REGATTA_OVERVIEW = "RegattaOverview";
    private static final String EVENT_REGATTA_LEADERBOARD = "EventRegattaLeaderboard";
    private static final String SERIES_OVERALL_LEADERBOARD = "SeriesOverallLeaderboard";
    private static final String SERIES_REGATTA_LEADERBOARDS = "SeriesRegattaLeaderboards";

    public static final StoredSettingsLocator createStoredSettingsLocatorForRaceBoard(
            RaceboardContextDefinition raceboardContextDefinition) {
        String userSettingsIdPart = RACEBOARD;
        String documentSettingsIdPart = StoredSettingsLocator.buildDocumentSettingsIdPart(raceboardContextDefinition.getRegattaName(),
                raceboardContextDefinition.getRaceName(), raceboardContextDefinition.getLeaderboardName());

        return new StoredSettingsLocator(userSettingsIdPart, documentSettingsIdPart);
    }

    public static final StoredSettingsLocator createStoredSettingsLocatorForLeaderboard(
            LeaderboardContextDefinition leaderboardContextDefinition) {
        return new StoredSettingsLocator(LEADERBOARD,
                StoredSettingsLocator.buildDocumentSettingsIdPart(leaderboardContextDefinition.getLeaderboardName()));
    }

    public static final StoredSettingsLocator createStoredSettingsLocatorForRegattaOverview(
            RegattaOverviewContextDefinition regattaOverviewContextDefinition) {
        return new StoredSettingsLocator(REGATTA_OVERVIEW,
                StoredSettingsLocator.buildDocumentSettingsIdPart(regattaOverviewContextDefinition.getEvent().toString()));
    }
    
    public static final StoredSettingsLocator createStoredSettingsLocatorForEventRegattaLeaderboard(String leaderboardName) {
        return new StoredSettingsLocator(EVENT_REGATTA_LEADERBOARD,
                StoredSettingsLocator.buildDocumentSettingsIdPart(leaderboardName));
    }
    
    public static final StoredSettingsLocator createStoredSettingsLocatorForSeriesOverallLeaderboard(String leaderboardName) {
        return new StoredSettingsLocator(SERIES_OVERALL_LEADERBOARD,
                StoredSettingsLocator.buildDocumentSettingsIdPart(leaderboardName));
    }
    
    public static final StoredSettingsLocator createStoredSettingsLocatorForSeriesRegattaLeaderboards(String leaderboardName) {
        return new StoredSettingsLocator(SERIES_REGATTA_LEADERBOARDS,
                StoredSettingsLocator.buildDocumentSettingsIdPart(leaderboardName));
    }

}
