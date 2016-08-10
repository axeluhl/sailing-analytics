package com.sap.sailing.gwt.home.mobile.app;

import java.util.List;

import com.google.gwt.place.shared.PlaceController;
import com.sap.sailing.gwt.home.communication.event.news.NewsEntryDTO;
import com.sap.sailing.gwt.home.desktop.places.event.regatta.overviewtab.RegattaOverviewPlace;
import com.sap.sailing.gwt.home.mobile.places.event.latestnews.LatestNewsPlace;
import com.sap.sailing.gwt.home.mobile.places.user.authentication.AuthenticationPlace;
import com.sap.sailing.gwt.home.shared.app.HomePlacesNavigator;
import com.sap.sailing.gwt.home.shared.app.PlaceNavigation;
import com.sap.sailing.gwt.home.shared.places.event.EventContext;

public class MobilePlacesNavigator extends HomePlacesNavigator {

    protected MobilePlacesNavigator(PlaceController placeController, boolean isStandaloneServer) {
        super(placeController, isStandaloneServer);
    }

    public PlaceNavigation<LatestNewsPlace> getEventLastestNewsNavigation(EventContext ctx, List<NewsEntryDTO> newsEntries,
            String baseUrl, boolean isOnRemoteServer) {
        LatestNewsPlace place = new LatestNewsPlace(ctx, newsEntries);
        return createPlaceNavigation(baseUrl, isOnRemoteServer, place);
    }

    // NOT MOBILE PLACES

    public PlaceNavigation<?> getRegattaOverviewNavigation(String eventId, String leaderboardName, String baseUrl,
            boolean isOnRemoteServer) {
        return createPlaceNavigation(baseUrl, isOnRemoteServer, new RegattaOverviewPlace(eventId, leaderboardName));
    }
    
    public PlaceNavigation<AuthenticationPlace> getSignInNavigation() {
        return createLocalPlaceNavigation(new AuthenticationPlace());
    }
}
