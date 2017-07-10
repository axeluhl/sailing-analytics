package com.sap.sailing.gwt.settings.client.utils;

import com.sap.sailing.gwt.settings.client.leaderboard.LeaderboardContextDefinition;
import com.sap.sailing.gwt.settings.client.raceboard.RaceboardContextDefinition;
import com.sap.sailing.gwt.settings.client.regattaoverview.RegattaOverviewContextDefinition;
import com.sap.sse.security.ui.settings.StoredSettingsLocation;

/**
 * Factory for {@link StoredSettingsLocation}s which are used in Sailing Analytics.
 * 
 * @author Vladislav Chumak
 *
 */
public class StoredSettingsLocationFactory {

    private StoredSettingsLocationFactory() {
    }

    private static final String RACEBOARD = "Raceboard";
    private static final String LEADERBOARD = "Leaderboard";
    private static final String REGATTA_OVERVIEW = "RegattaOverview";
    private static final String EVENT_REGATTA_LEADERBOARD = "EventRegattaLeaderboard";
    private static final String SERIES_OVERALL_LEADERBOARD = "SeriesOverallLeaderboard";
    private static final String SERIES_REGATTA_LEADERBOARDS = "SeriesRegattaLeaderboards";

    public static final StoredSettingsLocation createStoredSettingsLocatorForRaceBoard(
            RaceboardContextDefinition raceboardContextDefinition, String raceBoardModeName) {
        String userSettingsIdPart = RACEBOARD;
        if(raceBoardModeName != null) {
            userSettingsIdPart += "." + raceBoardModeName;
        }
        String documentSettingsIdPart = StoredSettingsLocation.buildDocumentSettingsIdPart(raceboardContextDefinition.getRegattaName(),
                raceboardContextDefinition.getRaceName(), raceboardContextDefinition.getLeaderboardName());

        return new SailingStoredSettingsLocation(userSettingsIdPart, documentSettingsIdPart);
    }

    public static final StoredSettingsLocation createStoredSettingsLocatorForLeaderboard(
            LeaderboardContextDefinition leaderboardContextDefinition) {
        return new SailingStoredSettingsLocation(LEADERBOARD,
                StoredSettingsLocation.buildDocumentSettingsIdPart(leaderboardContextDefinition.getLeaderboardName()));
    }

    public static final StoredSettingsLocation createStoredSettingsLocatorForRegattaOverview(
            RegattaOverviewContextDefinition regattaOverviewContextDefinition) {
        return new SailingStoredSettingsLocation(REGATTA_OVERVIEW,
                StoredSettingsLocation.buildDocumentSettingsIdPart(regattaOverviewContextDefinition.getEvent().toString()));
    }
    
    public static final StoredSettingsLocation createStoredSettingsLocatorForEventRegattaLeaderboard(String leaderboardName) {
        return new SailingStoredSettingsLocation(EVENT_REGATTA_LEADERBOARD,
                StoredSettingsLocation.buildDocumentSettingsIdPart(leaderboardName));
    }
    
    public static final StoredSettingsLocation createStoredSettingsLocatorForSeriesOverallLeaderboard(String leaderboardName) {
        return new SailingStoredSettingsLocation(SERIES_OVERALL_LEADERBOARD,
                StoredSettingsLocation.buildDocumentSettingsIdPart(leaderboardName));
    }
    
    public static final StoredSettingsLocation createStoredSettingsLocatorForSeriesRegattaLeaderboards(String leaderboardName) {
        return new SailingStoredSettingsLocation(SERIES_REGATTA_LEADERBOARDS,
                StoredSettingsLocation.buildDocumentSettingsIdPart(leaderboardName));
    }

}
