package com.sap.sailing.gwt.autoplay.client.app;

import com.google.gwt.place.shared.PlaceController;
import com.sap.sailing.gwt.autoplay.client.place.player.PlayerPlace;
import com.sap.sailing.gwt.autoplay.client.place.start.StartPlace;

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
    public void goToPlayer(String eventUuidAsString, String leaderboardIdAsNameString, String leaderboardZoom) {
        PlayerPlace playerPlace = new PlayerPlace(eventUuidAsString, leaderboardIdAsNameString, String.valueOf(leaderboardZoom));
        placeController.goTo(playerPlace); 
    }

}
