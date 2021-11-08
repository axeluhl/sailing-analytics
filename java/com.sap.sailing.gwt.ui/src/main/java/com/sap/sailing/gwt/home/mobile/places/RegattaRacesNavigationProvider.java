package com.sap.sailing.gwt.home.mobile.places;

import com.sap.sailing.gwt.home.shared.app.PlaceNavigation;

public interface RegattaRacesNavigationProvider {
    
    PlaceNavigation<?> getRegattaRacesNavigation(String regattaId);

    PlaceNavigation<?> getRegattaRacesNavigation(String regattaId, String seriesName);
}
