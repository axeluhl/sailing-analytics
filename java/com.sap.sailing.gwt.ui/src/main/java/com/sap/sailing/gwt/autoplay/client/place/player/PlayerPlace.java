package com.sap.sailing.gwt.autoplay.client.place.player;

import com.google.gwt.place.shared.PlaceTokenizer;
import com.sap.sailing.gwt.common.client.AbstractBasePlace;

public class PlayerPlace extends AbstractBasePlace {
    // general parameters
    private final String eventUuidAsString;
    private final boolean fullscreen;
    
    public final static String PARAM_EVENTID = "eventId"; 
    public final static String PARAM_FULLSCREEN = "fullscreen";
    
    // leaderboard parameters
    private final String leaderboardIdAsNameString;
    private final String leaderboardZoom;
    
    public final static String PARAM_LEADEROARD_NAME = "leaderboardName"; 
    public final static String PARAM_LEADEROARD_ZOOM = "leaderboardZoom"; 

    // raceboard parameters
    private String raceboardAutoSelectMedia;  

    public final static String PARAM_RACEBOARD_AUTOSELECT_MEDIA = "autoSelectMedia"; 
    
    public PlayerPlace(String url) {
        super(url);
        eventUuidAsString = getParameter(PARAM_EVENTID);
        fullscreen = Boolean.valueOf(getParameter(PARAM_FULLSCREEN));

        leaderboardIdAsNameString = getParameter(PARAM_LEADEROARD_NAME);
        leaderboardZoom = getParameter(PARAM_LEADEROARD_ZOOM);
        
        raceboardAutoSelectMedia = getParameter(PARAM_RACEBOARD_AUTOSELECT_MEDIA);
    }

    public PlayerPlace(String eventUuidAsString, boolean fullscreen,
            String leaderboardIdAsNameString, String leaderboardZoomAsString, String raceboardAutoSelectMedia) {
        super(PARAM_EVENTID, eventUuidAsString, PARAM_FULLSCREEN, String.valueOf(fullscreen), 
                PARAM_LEADEROARD_NAME, leaderboardIdAsNameString, PARAM_LEADEROARD_ZOOM, leaderboardZoomAsString,
                PARAM_RACEBOARD_AUTOSELECT_MEDIA, raceboardAutoSelectMedia);
        this.eventUuidAsString = eventUuidAsString;
        this.fullscreen = fullscreen;
        this.leaderboardIdAsNameString = leaderboardIdAsNameString;
        this.leaderboardZoom = leaderboardZoomAsString;
        this.raceboardAutoSelectMedia = raceboardAutoSelectMedia; 
    }

    public String getEventUuidAsString() {
        return eventUuidAsString;
    }

    public String getLeaderboardIdAsNameString() {
        return leaderboardIdAsNameString;
    }

    public String getLeaderboardZoom() {
        return leaderboardZoom;
    }
    
    public String getRaceboardAutoSelectMedia() {
        return raceboardAutoSelectMedia;
    }

    public boolean isFullscreen() {
        return fullscreen;
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
