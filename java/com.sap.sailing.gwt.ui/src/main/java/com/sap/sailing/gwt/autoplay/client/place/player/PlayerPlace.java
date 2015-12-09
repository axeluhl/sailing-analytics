package com.sap.sailing.gwt.autoplay.client.place.player;

import com.google.gwt.place.shared.PlaceTokenizer;
import com.sap.sailing.gwt.common.client.AbstractBasePlace;
import com.sap.sailing.gwt.ui.leaderboard.LeaderboardPerspectiveSettings;
import com.sap.sailing.gwt.ui.raceboard.RaceBoardPerspectiveSettings;
import com.sap.sse.common.Util.Pair;
import com.sap.sse.gwt.client.shared.components.CompositeSettings;

public class PlayerPlace extends AbstractBasePlace {
    private final AutoPlayerConfiguration playerConfiguration;
    
    private final Pair<LeaderboardPerspectiveSettings, CompositeSettings> leaderboardPerspectiveSettings;
    private final Pair<RaceBoardPerspectiveSettings, CompositeSettings> raceboardPerspectiveSettings;
    
    public PlayerPlace(String url) {
        super(url);
        
        String eventUuidAsString = getParameter(AutoPlayerConfiguration.PARAM_EVENTID);
        String leaderboardName= getParameter(AutoPlayerConfiguration.PARAM_LEADEROARD_NAME);
        Boolean fullscreen = Boolean.valueOf(getParameter(AutoPlayerConfiguration.PARAM_FULLSCREEN));
        
        this.playerConfiguration = new AutoPlayerConfiguration(eventUuidAsString, leaderboardName, fullscreen);
        this.leaderboardPerspectiveSettings = null;
        this.raceboardPerspectiveSettings = null;
    }

    public PlayerPlace(AutoPlayerConfiguration playerConfiguration, Pair<LeaderboardPerspectiveSettings, CompositeSettings> leaderboardPerspectiveSettings,
            Pair<RaceBoardPerspectiveSettings, CompositeSettings> raceboardPerspectiveSettings) {
        super(AutoPlayerConfiguration.PARAM_EVENTID, playerConfiguration.getEventUidAsString(),
                AutoPlayerConfiguration.PARAM_FULLSCREEN, String.valueOf(playerConfiguration.isFullscreenMode()), 
                AutoPlayerConfiguration.PARAM_LEADEROARD_NAME, playerConfiguration.getLeaderboardName());
        this.playerConfiguration = playerConfiguration;
        this.leaderboardPerspectiveSettings = leaderboardPerspectiveSettings;
        this.raceboardPerspectiveSettings = raceboardPerspectiveSettings;
    }

    public AutoPlayerConfiguration getConfiguration() {
        return this.playerConfiguration;
    }

    public Pair<LeaderboardPerspectiveSettings, CompositeSettings> getLeaderboardPerspectiveSettings() {
        return leaderboardPerspectiveSettings;
    }

    public Pair<RaceBoardPerspectiveSettings, CompositeSettings> getRaceboardPerspectiveSettings() {
        return raceboardPerspectiveSettings;
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
