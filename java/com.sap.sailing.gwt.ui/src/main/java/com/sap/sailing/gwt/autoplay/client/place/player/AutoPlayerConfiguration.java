package com.sap.sailing.gwt.autoplay.client.place.player;


public class AutoPlayerConfiguration {
    public final static String PARAM_EVENTID = "eventId"; 
    public final static String PARAM_FULLSCREEN = "fullscreen";
    public final static String PARAM_LEADEROARD_NAME = "leaderboardName"; 
    public final static String PARAM_LEADEROARD_ZOOM = "leaderboardZoom"; 
    
    private String eventUidAsString;
    private String leaderboardName;
    private boolean fullscreenMode;
    private String leaderboardZoom;

    public AutoPlayerConfiguration(String eventUidAsString, String leaderboardName, boolean fullscreenMode,
            String leaderboardZoom) {
        this.eventUidAsString = eventUidAsString;
        this.leaderboardName = leaderboardName;
        this.fullscreenMode = fullscreenMode;
        this.leaderboardZoom = leaderboardZoom;
    }

    public String getEventUidAsString() {
        return eventUidAsString;
    }

    public String getLeaderboardName() {
        return leaderboardName;
    }

    public boolean isFullscreenMode() {
        return fullscreenMode;
    }

    public String getLeaderboardZoom() {
        return leaderboardZoom;
    }
}
