package com.sap.sailing.gwt.autoplay.client.app;

import com.sap.sailing.gwt.autoplay.client.places.startclassic.old.PlayerClientFactory;

public interface AutoPlayClientFactory<P extends PlaceNavigator<?>> extends PlayerClientFactory {
    P getPlaceNavigator();
}
