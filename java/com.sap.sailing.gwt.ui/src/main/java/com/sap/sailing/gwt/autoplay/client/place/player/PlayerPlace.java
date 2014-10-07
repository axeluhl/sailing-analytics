package com.sap.sailing.gwt.autoplay.client.place.player;

import com.google.gwt.place.shared.PlaceTokenizer;
import com.sap.sailing.gwt.common.client.AbstractBasePlace;

public class PlayerPlace extends AbstractBasePlace {
    private final String eventUuidAsString;
    private final String leaderboardIdAsNameString;
    private final String leaderboardZoomAsString;
    
    private final static String PARAM_EVENTID = "eventId"; 
    private final static String PARAM_LEADEROARD_NAME = "leaderboardName"; 
    private final static String PARAM_LEADEROARD_ZOOM = "leaderboardZoom"; 
    
    public PlayerPlace(String url) {
        super(url);
        eventUuidAsString = getParameter(PARAM_EVENTID);
        leaderboardIdAsNameString = getParameter(PARAM_LEADEROARD_NAME);
        leaderboardZoomAsString = getParameter(PARAM_LEADEROARD_ZOOM);
    }

    public PlayerPlace(String eventUuidAsString, String leaderboardIdAsNameString, String leaderboardZoomAsString) {
        super(PARAM_EVENTID, eventUuidAsString, PARAM_LEADEROARD_NAME, leaderboardIdAsNameString, PARAM_LEADEROARD_ZOOM, leaderboardZoomAsString);
        this.eventUuidAsString = eventUuidAsString;
        this.leaderboardIdAsNameString = leaderboardIdAsNameString;
        this.leaderboardZoomAsString = leaderboardZoomAsString;
    }

    public String getEventUuidAsString() {
        return eventUuidAsString;
    }

    public String getLeaderboardIdAsNameString() {
        return leaderboardIdAsNameString;
    }

    public String getLeaderboardZoomAsString() {
        return leaderboardZoomAsString;
    }

    public static class Tokenizer implements PlaceTokenizer<PlayerPlace> {
        @Override
        public String getToken(PlayerPlace place) {
            return place.getParametersAsToken();
        }

        @Override
        public PlayerPlace getPlace(String url) {
            return new PlayerPlace(url);
        }
    }
}
