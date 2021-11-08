package com.sap.sailing.gwt.ui.adminconsole.swisstiming;

public final class SwissTimingEventIdUrlUtil {
    private static final String EVENT_ID_PATTERN = "[a-z0-9]{8}-[a-z0-9]{4}-[a-z0-9]{4}-[a-z0-9]{4}-[a-z0-9]{12}";
    private static final String MANAGE2SAIL_API_BASE_URL = "http://manage2sail.com/api/public/links/event/";
    private static final String MANAGE2SAIL_API_ACCESS_TOKEN = "?accesstoken=bDAv8CwsTM94ujZ";
    private static final String MANAGE2SAIL_API_APPENDIX = "&mediaType=json&includeRaces=true";

    /**
     * Similar to {@link #updateUrlFromEventId} this function tries to extract a M2S event Id by looking at the given
     * url.
     * 
     * @return The event ID inferred from the Json Url.
     */
    public static String getEventIdFromUrl(String eventUrl) {
        if (eventUrl.matches("http://manage2sail.com/.*" + EVENT_ID_PATTERN + ".*")) {
            return eventUrl.replaceFirst(".*(" + EVENT_ID_PATTERN + ").*", "$1");
        }
        return null;
    }

    /**
     * This function tries to infer a valid JsonUrl for any input given that matches the pattern of an event Id from
     * M2S. If there is an event id detected the Json Url gets returned.The ID pattern is defined in
     * {@link eventIdPattern}.
     */
    public static String getUrlFromEventId(String eventIdText) {
        if (eventIdText.matches(".*" + EVENT_ID_PATTERN + ".*")) {
            final String inferredEventId = eventIdText.replaceFirst(".*(" + EVENT_ID_PATTERN + ").*", "$1");
            return MANAGE2SAIL_API_BASE_URL + inferredEventId + MANAGE2SAIL_API_ACCESS_TOKEN + MANAGE2SAIL_API_APPENDIX;
        }
        return null;
    }

}
