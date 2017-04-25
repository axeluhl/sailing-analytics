package com.sap.sailing.gwt.autoplay.client.app;

import com.sap.sailing.gwt.autoplay.client.places.startclassic.old.PlayerClientFactory;
import com.sap.sailing.gwt.autoplay.client.places.startclassic.old.StartClientFactory;

public interface AutoPlayClientFactory<P extends PlaceNavigator> extends StartClientFactory, PlayerClientFactory {
    P getPlaceNavigator();
}
