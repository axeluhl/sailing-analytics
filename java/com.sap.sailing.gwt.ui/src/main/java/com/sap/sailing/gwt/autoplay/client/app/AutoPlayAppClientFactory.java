package com.sap.sailing.gwt.autoplay.client.app;

import com.sap.sailing.gwt.autoplay.client.place.player.PlayerClientFactory;
import com.sap.sailing.gwt.autoplay.client.place.start.StartClientFactory;

public interface AutoPlayAppClientFactory extends StartClientFactory, PlayerClientFactory {
    PlaceNavigator getPlaceNavigator();
}
