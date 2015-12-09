package com.sap.sailing.gwt.autoplay.client.app;

import com.google.gwt.place.shared.PlaceController;
import com.sap.sailing.gwt.autoplay.client.place.player.AutoPlayerConfiguration;
import com.sap.sailing.gwt.autoplay.client.place.player.PlayerPlace;
import com.sap.sailing.gwt.autoplay.client.place.start.StartPlace;
import com.sap.sailing.gwt.ui.leaderboard.LeaderboardPerspectiveSettings;
import com.sap.sailing.gwt.ui.raceboard.RaceBoardPerspectiveSettings;
import com.sap.sse.common.Util.Pair;
import com.sap.sse.gwt.client.shared.components.CompositeSettings;

public class PlaceNavigatorImpl implements PlaceNavigator {
    private final PlaceController placeController;
    
    protected PlaceNavigatorImpl(PlaceController placeController) {
        super();
        this.placeController = placeController;
    }

    @Override
    public void goToStart() {
        placeController.goTo(new StartPlace()); 
    }
    
    @Override
    public void goToPlayer(AutoPlayerConfiguration playerConfig, Pair<LeaderboardPerspectiveSettings, CompositeSettings> leaderboardPerspectiveSettings,
            Pair<RaceBoardPerspectiveSettings, CompositeSettings> raceboardPerspectiveSettings) {
        PlayerPlace playerPlace = new PlayerPlace(playerConfig, leaderboardPerspectiveSettings, raceboardPerspectiveSettings);
        placeController.goTo(playerPlace); 
    }

}
