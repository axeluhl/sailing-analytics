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
        String[] documentAndFragment = baseLink.split("#", 2);
        StringBuilder link = new StringBuilder(documentAndFragment[0]);
        int i = 1;
        for (Entry<String, String> entry: parameters.entrySet()) {
            link.append(i == 1 ? "?" : "&");
            link.append(entry.getKey() + "=" + replaceAmpersand(URLEncoder.encode(entry.getValue())));
            i++;
        }
        if (debugParam != null && !debugParam.isEmpty()) {
            link.append(i == 1 ? "?" : "&");
            link.append("gwt.codesvr=" + debugParam);
        }
        if (localeParam != null && !localeParam.isEmpty()) {
            link.append(i == 1 ? "?" : "&");
            link.append("locale=" + localeParam);
        }
        if (documentAndFragment.length > 1) {
            link.append('#');
            link.append(URLEncoder.encode(documentAndFragment[1])); // append the fragment following the "#" again
        }
        return link.toString();
    }
    
    private static String replaceAmpersand(String param) {
        return param.replaceAll("&", "%26");
    }
}
