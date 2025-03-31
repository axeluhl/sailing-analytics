package com.sap.sailing.gwt.home.mobile.places;

import java.util.UUID;

import com.sap.sailing.gwt.home.shared.app.PlaceNavigation;

public interface SeriesEventLeaderboardOverviewNavigationProvider {
    
    PlaceNavigation<?> getSeriesEventLeaderboardOverviewNavigation(UUID eventId, String leaderboardName);

}
