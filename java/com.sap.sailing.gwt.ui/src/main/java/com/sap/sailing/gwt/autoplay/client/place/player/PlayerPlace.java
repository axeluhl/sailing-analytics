package com.sap.sailing.gwt.autoplay.client.place.player;

import com.google.gwt.place.shared.PlaceTokenizer;
import com.sap.sailing.gwt.autoplay.client.shared.leaderboard.LeaderboardWithHeaderPerspectiveLifecycle;
import com.sap.sailing.gwt.autoplay.client.shared.leaderboard.LeaderboardWithHeaderPerspectiveSettings;
import com.sap.sailing.gwt.common.client.AbstractBasePlace;
import com.sap.sailing.gwt.ui.raceboard.RaceBoardPerspectiveLifecycle;
import com.sap.sailing.gwt.ui.raceboard.RaceBoardPerspectiveSettings;
import com.sap.sse.gwt.client.shared.perspective.PerspectiveLifecycleWithAllSettings;

public class PlayerPlace extends AbstractBasePlace {
    private final AutoPlayerConfiguration playerConfiguration;
    
    private final PerspectiveLifecycleWithAllSettings<LeaderboardWithHeaderPerspectiveLifecycle, LeaderboardWithHeaderPerspectiveSettings> leaderboardPerspectiveLifecycleWithAllSettings;
    private final PerspectiveLifecycleWithAllSettings<RaceBoardPerspectiveLifecycle, RaceBoardPerspectiveSettings> raceboardPerspectiveLifecycleWithAllSettings;
    
    public PlayerPlace(String url) {
        super(url);
        
        String eventUuidAsString = getParameter(AutoPlayerConfiguration.PARAM_EVENTID);
        String leaderboardName= getParameter(AutoPlayerConfiguration.PARAM_LEADEROARD_NAME);
        Boolean fullscreen = Boolean.valueOf(getParameter(AutoPlayerConfiguration.PARAM_FULLSCREEN));
        Integer timeToSwitchBeforeRaceStartInSeconds = Integer.valueOf(getParameter(AutoPlayerConfiguration.PARAM_TIME_TO_SWITCH_BEFORE_RACE_START));
        
        this.playerConfiguration = new AutoPlayerConfiguration(eventUuidAsString, leaderboardName, fullscreen, timeToSwitchBeforeRaceStartInSeconds);
        this.leaderboardPerspectiveLifecycleWithAllSettings = null;
        this.raceboardPerspectiveLifecycleWithAllSettings = null;
    }

    public PlayerPlace(AutoPlayerConfiguration playerConfiguration,
            PerspectiveLifecycleWithAllSettings<LeaderboardWithHeaderPerspectiveLifecycle, LeaderboardWithHeaderPerspectiveSettings> leaderboardPerspectiveLifecycleWithAllSettings,
            PerspectiveLifecycleWithAllSettings<RaceBoardPerspectiveLifecycle, RaceBoardPerspectiveSettings> raceboardPerspectiveLifecycleWithAllSettings) {
        super(AutoPlayerConfiguration.PARAM_EVENTID, playerConfiguration.getEventUidAsString(),
                AutoPlayerConfiguration.PARAM_FULLSCREEN, String.valueOf(playerConfiguration.isFullscreenMode()), 
                AutoPlayerConfiguration.PARAM_LEADEROARD_NAME, playerConfiguration.getLeaderboardName(),
               AutoPlayerConfiguration.PARAM_TIME_TO_SWITCH_BEFORE_RACE_START, String.valueOf(playerConfiguration.getTimeToSwitchBeforeRaceStartInSeconds()));
        this.playerConfiguration = playerConfiguration;
        this.leaderboardPerspectiveLifecycleWithAllSettings = leaderboardPerspectiveLifecycleWithAllSettings;
        this.raceboardPerspectiveLifecycleWithAllSettings = raceboardPerspectiveLifecycleWithAllSettings;
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

    public PerspectiveLifecycleWithAllSettings<LeaderboardWithHeaderPerspectiveLifecycle, LeaderboardWithHeaderPerspectiveSettings> getLeaderboardPerspectiveLifecycleWithAllSettings() {
        return leaderboardPerspectiveLifecycleWithAllSettings;
    }

    public PerspectiveLifecycleWithAllSettings<RaceBoardPerspectiveLifecycle, RaceBoardPerspectiveSettings> getRaceboardPerspectiveLifecycleWithAllSettings() {
        return raceboardPerspectiveLifecycleWithAllSettings;
    }
}
