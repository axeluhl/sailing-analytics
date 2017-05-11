package com.sap.sailing.gwt.autoplay.client.app;

import com.google.gwt.place.shared.PlaceController;

public class AutoplayNavigatorImpl implements AutoPlayPlaceNavigator {

    public AutoplayNavigatorImpl(PlaceController placeController) {
        super();
    }



    @Override
    public void goToPlayer(String serializedSettings, AutoPlayClientFactory cf) {
        cf.startRootNode(serializedSettings);
    }

}
