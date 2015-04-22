package com.sap.sailing.android.tracking.app.utils;

public class BackendHelper {

    public static String getEventDataUrl(String server, int port, String eventId, String competitorId) {
        return String.format("%s?event_id=%s&competitor_id=%s", server + ":" + port, eventId, competitorId);
    }

    public static String getUrl(String server, String eventId, String competitorId, String path) {
        return String.format("%s/event/%s/competitor/%s/%s", server, eventId, competitorId, path);
    }

}
