package com.sap.sailing.gwt.ui.client;

import java.util.Map;
import java.util.Map.Entry;

import com.google.gwt.user.client.Window;

public class EntryPointLinkFactory {
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

    private static String createEntryPointLink(String baseLink, Map<String, String> parameters) {
        String debugParam = Window.Location.getParameter("gwt.codesvr");
        String localeParam = Window.Location.getParameter("locale");
        String link = baseLink;
        int i = 1;
        for(Entry<String, String> entry: parameters.entrySet()) {
            link += i == 1 ? "?" : "&";
            link += entry.getKey() + "=" + entry.getValue();
            i++;
        }
        if (debugParam != null && !debugParam.isEmpty()) {
            link += i == 1 ? "?" : "&";
            link += "gwt.codesvr=" + debugParam;
        }
        if (localeParam != null && !localeParam.isEmpty()) {
            link += i == 1 ? "?" : "&";
            link += "locale=" + localeParam;
        }
        return URLEncoder.encode(link);
    }
}
