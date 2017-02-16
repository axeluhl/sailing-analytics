package com.sap.sailing.gwt.autoplay.client.place.player;

import com.google.gwt.place.shared.PlaceTokenizer;
import com.sap.sailing.gwt.common.client.AbstractBasePlace;

public class PlayerPlace extends AbstractBasePlace {
    private final AutoPlayerConfiguration playerConfiguration;
    
    public PlayerPlace(String url) {
        super(url);
        
        String eventUuidAsString = getParameter(AutoPlayerConfiguration.PARAM_EVENTID);
        String leaderboardName= getParameter(AutoPlayerConfiguration.PARAM_LEADEROARD_NAME);
        Boolean fullscreen = Boolean.valueOf(getParameter(AutoPlayerConfiguration.PARAM_FULLSCREEN));
        Integer timeToSwitchBeforeRaceStartInSeconds = Integer.valueOf(getParameter(AutoPlayerConfiguration.PARAM_TIME_TO_SWITCH_BEFORE_RACE_START));
        
        this.playerConfiguration = new AutoPlayerConfiguration(eventUuidAsString, leaderboardName, fullscreen, timeToSwitchBeforeRaceStartInSeconds);
        
        //these are currently regenerated in the PlayerActivity, based on the historydata. Once the settings are properly stored, this should be replaced.
    }

    public PlayerPlace(AutoPlayerConfiguration playerConfiguration) {
        super(AutoPlayerConfiguration.PARAM_EVENTID, playerConfiguration.getEventUidAsString(),
                AutoPlayerConfiguration.PARAM_FULLSCREEN, String.valueOf(playerConfiguration.isFullscreenMode()), 
                AutoPlayerConfiguration.PARAM_LEADEROARD_NAME, playerConfiguration.getLeaderboardName(),
               AutoPlayerConfiguration.PARAM_TIME_TO_SWITCH_BEFORE_RACE_START, String.valueOf(playerConfiguration.getTimeToSwitchBeforeRaceStartInSeconds()));
        this.playerConfiguration = playerConfiguration;
    }

    public AutoPlayerConfiguration getConfiguration() {
        return this.playerConfiguration;
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
