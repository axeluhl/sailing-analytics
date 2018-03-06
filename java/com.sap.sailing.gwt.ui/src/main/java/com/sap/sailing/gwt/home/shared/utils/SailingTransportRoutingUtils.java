package com.sap.sailing.gwt.home.shared.utils;

import java.util.UUID;

import com.google.gwt.http.client.URL;

public class SailingTransportRoutingUtils {
    private static final String EVENT_PREFIX ="event/";
    private static final String SERIES_PREFIX ="series/";
    private static final String LEADERBOARDNAME_PREFIX ="/leaderboard/";
    
    public static String pathForEvent(UUID eventId) {
        return new StringBuilder(EVENT_PREFIX).append(eventId.toString()).toString();
    }
    public static String pathForSeries(UUID seriesId) {
        return new StringBuilder(SERIES_PREFIX).append(seriesId.toString()).toString();
    }

    public static String pathForEvent(UUID eventId, String leaderboardname) {
        return new StringBuilder(EVENT_PREFIX).append(eventId.toString()).append(LEADERBOARDNAME_PREFIX).append(URL.encodePathSegment(leaderboardname)).toString();
    }

}
