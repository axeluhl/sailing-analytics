package com.sap.sailing.gwt.autoplay.client.app;

import com.google.gwt.place.shared.PlaceController;

public class AutoplayNavigatorImpl implements AutoPlayPlaceNavigator {

    private PlaceController placeController;

    public AutoplayNavigatorImpl(PlaceController placeController) {
        super();
        this.placeController = placeController;
    }



    @Override
    public void goToPlayer(String serializedSettings, AutoPlayClientFactory cf) {
        cf.startRootNode(serializedSettings);
    }

}
