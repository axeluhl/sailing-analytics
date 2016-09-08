package com.sap.sailing.gwt.autoplay.client.place.player;


public class AutoPlayerConfiguration {
    public final static String PARAM_EVENTID = "eventId"; 
    public final static String PARAM_FULLSCREEN = "fullscreen";
    public final static String PARAM_LEADEROARD_NAME = "leaderboardName"; 
    public final static String PARAM_TIME_TO_SWITCH_BEFORE_RACE_START = "timeToSwitchBeforeRaceStart"; 
    
    private String eventUidAsString;
    private String leaderboardName;
    private boolean fullscreenMode;
    private long timeToSwitchBeforeRaceStartInSeconds;

    public AutoPlayerConfiguration(String eventUidAsString, String leaderboardName, boolean fullscreenMode, long timeToSwitchBeforeRaceStartInSeconds) {
        this.eventUidAsString = eventUidAsString;
        this.leaderboardName = leaderboardName;
        this.fullscreenMode = fullscreenMode;
        this.timeToSwitchBeforeRaceStartInSeconds = timeToSwitchBeforeRaceStartInSeconds;
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

    public long getTimeToSwitchBeforeRaceStartInSeconds() {
        return timeToSwitchBeforeRaceStartInSeconds;
    }
}
