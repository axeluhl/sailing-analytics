package com.sap.sailing.gwt.ui.client;

import java.util.Map;

import com.sap.sse.gwt.client.AbstractEntryPointLinkFactory;

public class EntryPointLinkFactory extends AbstractEntryPointLinkFactory {
    public static String createRaceBoardLink(Map<String, String> parameters) {
        return createEntryPointLink("/gwt/RaceBoard.html", parameters);
    }

    public static String createLeaderboardLink(Map<String, String> parameters) {
        return createEntryPointLink("/gwt/Leaderboard.html", parameters);
    }
    
    public static String createLeaderboardGroupLink(Map<String, String> parameters) {
        return createEntryPointLink("/gwt/Spectator.html", parameters);
    }
    
    public static String createEventLink(Map<String, String> parameters, String eventId) {
        return createEntryPointLink("/gwt/Home.html#EventPlace:eventId="+eventId, parameters);
    }
    
    public static String createRegattaOverviewLink(Map<String, String> parameters) {
        return createEntryPointLink("/gwt/RegattaOverview.html", parameters);
    }
    
    public static String createDashboardLink(Map<String, String> parameters) {
        return createEntryPointLink("/dashboards/RibDashboard.html", parameters);
    }
}
