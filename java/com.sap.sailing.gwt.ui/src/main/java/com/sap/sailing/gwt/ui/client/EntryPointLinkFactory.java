package com.sap.sailing.gwt.ui.client;

import java.util.Collections;
import java.util.Map;

import com.sap.sse.gwt.client.AbstractEntryPointLinkFactory;

public class EntryPointLinkFactory extends AbstractEntryPointLinkFactory {
    public static final String LEADERBOARD_PATH = "/gwt/Leaderboard.html";
    public static final String AUTOPLAY_PATH = "/gwt/AutoPlay.html";

    public static String createSimulatorLink(Map<String, String> parameters) {
        return createEntryPointLink("/gwt/Simulator.html", parameters);
    }
    
    public static String createLeaderboardTabLink(String eventId, String regattaId) {
        return createEventRegattaTabLink(eventId, regattaId, "leaderboard");
    }
    
    public static String createRacesTabLink(String eventId, String leaderboardName) {
        return createEventRegattaTabLink(eventId, leaderboardName, "races");
    }
    
    private static String createEventRegattaTabLink(String eventId, String regattaId, String tabName) {
        return createEntryPointLink(
                "/gwt/Home.html", "/regatta/" + tabName + "/:eventId=" + eventId + "&regattaId=" + regattaId,
                Collections.<String, String>emptyMap());
    }
    
    public static String createLeaderboardGroupLink(Map<String, String> parameters) {
        return createEntryPointLink("/gwt/Spectator.html", parameters);
    }
    
    public static String createEventPlaceLink(String eventId, Map<String, String> parameters) {
        return createEntryPointLink("/gwt/Home.html", "/event/:eventId="+eventId, parameters);
    }
    
    public static String createDashboardLink(Map<String, String> parameters) {
        return createEntryPointLink("/dashboards/RibDashboard.html", parameters);
    }
    
    public static String createPairingListLink(Map<String, String> parameters) {
        return createEntryPointLink("/gwt/PairingList.html", parameters);
    }
}
