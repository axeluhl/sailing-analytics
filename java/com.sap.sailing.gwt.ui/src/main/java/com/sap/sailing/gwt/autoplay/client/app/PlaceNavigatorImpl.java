package com.sap.sailing.gwt.autoplay.client.app;

import java.util.Map;

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
    public void goToPlayer(String eventUuidAsString, boolean fullscreen, 
            Map<String, String> leaderboardParameters, Map<String, String> raceboardParameters) {
        PlayerPlace playerPlace = new PlayerPlace(eventUuidAsString, fullscreen, 
                leaderboardParameters.get(PlayerPlace.PARAM_LEADEROARD_NAME), 
                leaderboardParameters.get(PlayerPlace.PARAM_LEADEROARD_ZOOM),
                raceboardParameters.get(PlayerPlace.PARAM_RACEBOARD_AUTOSELECT_MEDIA));
        placeController.goTo(playerPlace); 
    }

}
