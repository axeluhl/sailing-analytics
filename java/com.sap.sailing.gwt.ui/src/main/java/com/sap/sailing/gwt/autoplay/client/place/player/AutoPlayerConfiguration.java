package com.sap.sailing.gwt.autoplay.client.place.player;


public class AutoPlayerConfiguration {
    public final static String PARAM_EVENTID = "eventId"; 
    public final static String PARAM_FULLSCREEN = "fullscreen";
    public final static String PARAM_LEADEROARD_NAME = "leaderboardName"; 
    
    private String eventUidAsString;
    private String leaderboardName;
    private boolean fullscreenMode;

    public AutoPlayerConfiguration(String eventUidAsString, String leaderboardName, boolean fullscreenMode) {
        this.eventUidAsString = eventUidAsString;
        this.leaderboardName = leaderboardName;
        this.fullscreenMode = fullscreenMode;
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
}
