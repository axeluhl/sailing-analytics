package com.sap.sailing.gwt.autoplay.client.place.player;

import com.google.gwt.place.shared.PlaceTokenizer;
import com.sap.sailing.gwt.common.client.AbstractBasePlace;
import com.sap.sailing.gwt.ui.raceboard.ProxyRaceBoardPerspective;

public class PlayerPlace extends AbstractBasePlace {
    private final AutoPlayerConfiguration playerConfiguration;
    
    private final LeaderboardWithHeaderPerspective leaderboardWithHeaderPerspective; 
    private final ProxyRaceBoardPerspective raceboardPerspective;
    
    public PlayerPlace(String url) {
        super(url);
        
        String eventUuidAsString = getParameter(AutoPlayerConfiguration.PARAM_EVENTID);
        String leaderboardName= getParameter(AutoPlayerConfiguration.PARAM_LEADEROARD_NAME);
        Boolean fullscreen = Boolean.valueOf(getParameter(AutoPlayerConfiguration.PARAM_FULLSCREEN));
        Integer timeToSwitchBeforeRaceStartInSeconds = Integer.valueOf(getParameter(AutoPlayerConfiguration.PARAM_TIME_TO_SWITCH_BEFORE_RACE_START));
        
        this.playerConfiguration = new AutoPlayerConfiguration(eventUuidAsString, leaderboardName, fullscreen, timeToSwitchBeforeRaceStartInSeconds);
        this.leaderboardWithHeaderPerspective = null;
        this.raceboardPerspective = null;
    }

    public PlayerPlace(AutoPlayerConfiguration playerConfiguration, LeaderboardWithHeaderPerspective leaderboardWithHeaderPerspective, 
            ProxyRaceBoardPerspective raceboardPerspective) {
        super(AutoPlayerConfiguration.PARAM_EVENTID, playerConfiguration.getEventUidAsString(),
                AutoPlayerConfiguration.PARAM_FULLSCREEN, String.valueOf(playerConfiguration.isFullscreenMode()), 
                AutoPlayerConfiguration.PARAM_LEADEROARD_NAME, playerConfiguration.getLeaderboardName(),
               AutoPlayerConfiguration.PARAM_TIME_TO_SWITCH_BEFORE_RACE_START, String.valueOf(playerConfiguration.getTimeToSwitchBeforeRaceStartInSeconds()));
        this.playerConfiguration = playerConfiguration;
        this.leaderboardWithHeaderPerspective = leaderboardWithHeaderPerspective;
        this.raceboardPerspective = raceboardPerspective;
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

    public LeaderboardWithHeaderPerspective getLeaderboardWithHeaderPerspective() {
        return leaderboardWithHeaderPerspective;
    }

    public ProxyRaceBoardPerspective getRaceboardPerspective() {
        return raceboardPerspective;
    }
}
