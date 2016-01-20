package com.sap.sailing.gwt.autoplay.client.place.player;

import com.google.gwt.place.shared.PlaceTokenizer;
import com.sap.sailing.gwt.common.client.AbstractBasePlace;
import com.sap.sailing.gwt.ui.leaderboard.LeaderboardPerspectiveSettings;
import com.sap.sailing.gwt.ui.raceboard.RaceBoardPerspectiveSettings;
import com.sap.sse.gwt.client.shared.components.ComponentLifecycleAndSettings;
import com.sap.sse.gwt.client.shared.perspective.PerspectiveLifecycleAndComponentSettings;

public class PlayerPlace extends AbstractBasePlace {
    private final AutoPlayerConfiguration playerConfiguration;
    
    private final ComponentLifecycleAndSettings<LeaderboardPerspectiveSettings> leaderboardPerspectiveLifecycleAndSettings;
    private final ComponentLifecycleAndSettings<RaceBoardPerspectiveSettings> raceboardPerspectiveLifecycleAndSettings;
    
    private final PerspectiveLifecycleAndComponentSettings leaderboardPerspectiveComponentLifecyclesAndSettings;
    private final PerspectiveLifecycleAndComponentSettings raceboardPerspectiveComponentLifecyclesAndSettings;
    
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
            ComponentLifecycleAndSettings<LeaderboardPerspectiveSettings> leaderboardPerspectiveLifecycleAndSettings, 
            ComponentLifecycleAndSettings<RaceBoardPerspectiveSettings> raceboardPerspectiveLifecycleAndSettings,
            PerspectiveLifecycleAndComponentSettings leaderboardPerspectiveComponentLifecyclesAndSettings,
            PerspectiveLifecycleAndComponentSettings raceboardPerspectiveComponentLifecyclesAndSettings) {
        super(AutoPlayerConfiguration.PARAM_EVENTID, playerConfiguration.getEventUidAsString(),
                AutoPlayerConfiguration.PARAM_FULLSCREEN, String.valueOf(playerConfiguration.isFullscreenMode()), 
                AutoPlayerConfiguration.PARAM_LEADEROARD_NAME, playerConfiguration.getLeaderboardName(),
               AutoPlayerConfiguration.PARAM_TIME_TO_SWITCH_BEFORE_RACE_START, String.valueOf(playerConfiguration.getTimeToSwitchBeforeRaceStartInSeconds()));
        this.playerConfiguration = playerConfiguration;
        this.leaderboardPerspectiveLifecycleAndSettings = leaderboardPerspectiveLifecycleAndSettings;
        this.raceboardPerspectiveLifecycleAndSettings = raceboardPerspectiveLifecycleAndSettings;
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

    public ComponentLifecycleAndSettings<LeaderboardPerspectiveSettings> getLeaderboardPerspectiveLifecycleAndSettings() {
        return leaderboardPerspectiveLifecycleAndSettings;
    }

    public ComponentLifecycleAndSettings<RaceBoardPerspectiveSettings> getRaceboardPerspectiveLifecycleAndSettings() {
        return raceboardPerspectiveLifecycleAndSettings;
    }

    public PerspectiveLifecycleAndComponentSettings getLeaderboardPerspectiveComponentLifecyclesAndSettings() {
        return leaderboardPerspectiveComponentLifecyclesAndSettings;
    }

    public PerspectiveLifecycleAndComponentSettings getRaceboardPerspectiveComponentLifecyclesAndSettings() {
        return raceboardPerspectiveComponentLifecyclesAndSettings;
    }
}
