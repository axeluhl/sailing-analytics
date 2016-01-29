package com.sap.sailing.gwt.autoplay.client.place.player;

import com.google.gwt.place.shared.PlaceTokenizer;
import com.sap.sailing.gwt.common.client.AbstractBasePlace;
import com.sap.sailing.gwt.ui.leaderboard.LeaderboardWithHeaderPerspectiveSettings;
import com.sap.sailing.gwt.ui.raceboard.RaceBoardPerspectiveSettings;
import com.sap.sse.gwt.client.shared.perspective.PerspectiveLifecycleAndComponentSettings;
import com.sap.sse.gwt.client.shared.perspective.PerspectiveLifecycleAndSettings;

public class PlayerPlace extends AbstractBasePlace {
    private final AutoPlayerConfiguration playerConfiguration;
    
    private final PerspectiveLifecycleAndSettings<LeaderboardWithHeaderPerspectiveLifecycle, LeaderboardWithHeaderPerspectiveSettings> leaderboardPerspectiveLifecycleAndSettings;
    private final PerspectiveLifecycleAndSettings<RaceBoardPerspectiveLifecycle, RaceBoardPerspectiveSettings> raceboardPerspectiveLifecycleAndSettings;
    
    private final PerspectiveLifecycleAndComponentSettings<LeaderboardWithHeaderPerspectiveLifecycle> leaderboardPerspectiveComponentLifecyclesAndSettings;
    private final PerspectiveLifecycleAndComponentSettings<RaceBoardPerspectiveLifecycle> raceboardPerspectiveComponentLifecyclesAndSettings;
    
    public PlayerPlace(String url) {
        super(url);
        
        String eventUuidAsString = getParameter(AutoPlayerConfiguration.PARAM_EVENTID);
        String leaderboardName= getParameter(AutoPlayerConfiguration.PARAM_LEADEROARD_NAME);
        Boolean fullscreen = Boolean.valueOf(getParameter(AutoPlayerConfiguration.PARAM_FULLSCREEN));
        Integer timeToSwitchBeforeRaceStartInSeconds = Integer.valueOf(getParameter(AutoPlayerConfiguration.PARAM_TIME_TO_SWITCH_BEFORE_RACE_START));
        
        this.playerConfiguration = new AutoPlayerConfiguration(eventUuidAsString, leaderboardName, fullscreen, timeToSwitchBeforeRaceStartInSeconds);
        this.leaderboardPerspectiveLifecycleAndSettings = null;
        this.raceboardPerspectiveLifecycleAndSettings = null;
        this.leaderboardPerspectiveComponentLifecyclesAndSettings = null;
        this.raceboardPerspectiveComponentLifecyclesAndSettings = null;
    }

    public PlayerPlace(AutoPlayerConfiguration playerConfiguration,
            PerspectiveLifecycleAndSettings<LeaderboardWithHeaderPerspectiveLifecycle, LeaderboardWithHeaderPerspectiveSettings> leaderboardPerspectiveLifecyleAndSettings, 
            PerspectiveLifecycleAndSettings<RaceBoardPerspectiveLifecycle, RaceBoardPerspectiveSettings> raceboardPerspectiveLifecyleAndSettings,
            PerspectiveLifecycleAndComponentSettings<LeaderboardWithHeaderPerspectiveLifecycle> leaderboardPerspectiveComponentLifecyclesAndSettings,
            PerspectiveLifecycleAndComponentSettings<RaceBoardPerspectiveLifecycle> raceboardPerspectiveComponentLifecyclesAndSettings) {
        super(AutoPlayerConfiguration.PARAM_EVENTID, playerConfiguration.getEventUidAsString(),
                AutoPlayerConfiguration.PARAM_FULLSCREEN, String.valueOf(playerConfiguration.isFullscreenMode()), 
                AutoPlayerConfiguration.PARAM_LEADEROARD_NAME, playerConfiguration.getLeaderboardName(),
               AutoPlayerConfiguration.PARAM_TIME_TO_SWITCH_BEFORE_RACE_START, String.valueOf(playerConfiguration.getTimeToSwitchBeforeRaceStartInSeconds()));
        this.playerConfiguration = playerConfiguration;
        this.leaderboardPerspectiveLifecycleAndSettings = leaderboardPerspectiveLifecyleAndSettings;
        this.raceboardPerspectiveLifecycleAndSettings = raceboardPerspectiveLifecyleAndSettings;
        this.leaderboardPerspectiveComponentLifecyclesAndSettings = leaderboardPerspectiveComponentLifecyclesAndSettings;
        this.raceboardPerspectiveComponentLifecyclesAndSettings = raceboardPerspectiveComponentLifecyclesAndSettings;
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

    public PerspectiveLifecycleAndSettings<LeaderboardWithHeaderPerspectiveLifecycle, LeaderboardWithHeaderPerspectiveSettings> getLeaderboardPerspectiveLifecycleAndSettings() {
        return leaderboardPerspectiveLifecycleAndSettings;
    }

    public PerspectiveLifecycleAndSettings<RaceBoardPerspectiveLifecycle, RaceBoardPerspectiveSettings> getRaceboardPerspectiveLifecycleAndSettings() {
        return raceboardPerspectiveLifecycleAndSettings;
    }

    public PerspectiveLifecycleAndComponentSettings<LeaderboardWithHeaderPerspectiveLifecycle> getLeaderboardPerspectiveComponentLifecyclesAndSettings() {
        return leaderboardPerspectiveComponentLifecyclesAndSettings;
    }

    public PerspectiveLifecycleAndComponentSettings<RaceBoardPerspectiveLifecycle> getRaceboardPerspectiveComponentLifecyclesAndSettings() {
        return raceboardPerspectiveComponentLifecyclesAndSettings;
    }
}
