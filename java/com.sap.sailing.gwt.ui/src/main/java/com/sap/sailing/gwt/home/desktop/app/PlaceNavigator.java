package com.sap.sailing.gwt.home.desktop.app;

import com.google.gwt.place.shared.Place;


public interface PlaceNavigator {
    <T extends Place> void goToPlace(PlaceNavigation<T> placeNavigation);
}
