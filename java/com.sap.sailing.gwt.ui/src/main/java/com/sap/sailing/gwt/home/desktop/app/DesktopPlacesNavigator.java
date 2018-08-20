package com.sap.sailing.gwt.home.desktop.app;

import com.google.gwt.place.shared.PlaceController;
import com.sap.sailing.gwt.home.desktop.places.sponsoring.SponsoringPlace;
import com.sap.sailing.gwt.home.shared.app.HomePlacesNavigator;
import com.sap.sailing.gwt.home.shared.app.PlaceNavigation;

public class DesktopPlacesNavigator extends HomePlacesNavigator {

    protected DesktopPlacesNavigator(PlaceController placeController, boolean isStandaloneServer) {
        super(placeController, isStandaloneServer);
    }
    
    public PlaceNavigation<SponsoringPlace> getSponsoringNavigation() {
        return createGlobalPlaceNavigation(new SponsoringPlace());
    }
}
