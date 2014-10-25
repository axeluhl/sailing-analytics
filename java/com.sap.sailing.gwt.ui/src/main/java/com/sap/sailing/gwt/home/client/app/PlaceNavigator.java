package com.sap.sailing.gwt.home.client.app;

import com.google.gwt.place.shared.Place;


public interface PlaceNavigator {
    <T extends Place> void goToPlace(PlaceNavigation<T> placeNavigation);
}
