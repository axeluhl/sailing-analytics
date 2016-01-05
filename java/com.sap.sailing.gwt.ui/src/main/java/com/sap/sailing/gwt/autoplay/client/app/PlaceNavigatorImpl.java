package com.sap.sailing.gwt.autoplay.client.app;

import com.google.gwt.place.shared.PlaceController;
import com.sap.sailing.gwt.autoplay.client.place.player.AutoPlayerConfiguration;
import com.sap.sailing.gwt.autoplay.client.place.player.LeaderboardWithHeaderPerspective;
import com.sap.sailing.gwt.autoplay.client.place.player.PlayerPlace;
import com.sap.sailing.gwt.autoplay.client.place.start.StartPlace;
import com.sap.sailing.gwt.ui.raceboard.ProxyRaceBoardPerspective;

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
    public void goToPlayer(AutoPlayerConfiguration playerConfig, LeaderboardWithHeaderPerspective leaderboardWithHeaderPerspective, 
            ProxyRaceBoardPerspective raceboardPerspective) {
        PlayerPlace playerPlace = new PlayerPlace(playerConfig, leaderboardWithHeaderPerspective, raceboardPerspective);
        placeController.goTo(playerPlace); 
    }

}
