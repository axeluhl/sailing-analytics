package com.sap.sailing.gwt.home.mobile.places;

import java.util.UUID;

import com.sap.sailing.gwt.home.shared.app.PlaceNavigation;

public interface SeriesEventOverviewNavigationProvider {
    
    PlaceNavigation<?> getSeriesEventOverviewNavigation(UUID eventId);

}
