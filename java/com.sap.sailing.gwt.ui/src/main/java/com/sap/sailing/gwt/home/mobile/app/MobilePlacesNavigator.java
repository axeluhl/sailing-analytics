package com.sap.sailing.gwt.home.mobile.app;

import java.util.List;

import com.google.gwt.place.shared.PlaceController;
import com.sap.sailing.gwt.home.communication.event.news.NewsEntryDTO;
import com.sap.sailing.gwt.home.mobile.places.event.latestnews.LatestNewsPlace;
import com.sap.sailing.gwt.home.mobile.places.user.authentication.AuthenticationPlace;
import com.sap.sailing.gwt.home.shared.app.HomePlacesNavigator;
import com.sap.sailing.gwt.home.shared.app.PlaceNavigation;
import com.sap.sailing.gwt.home.shared.places.event.EventContext;

public class MobilePlacesNavigator extends HomePlacesNavigator {

    protected MobilePlacesNavigator(PlaceController placeController, boolean isStandaloneServer) {
        super(placeController, isStandaloneServer);
    }

    public PlaceNavigation<LatestNewsPlace> getEventLastestNewsNavigation(EventContext ctx,
            List<NewsEntryDTO> newsEntries, String baseUrl, boolean isOnRemoteServer) {
        LatestNewsPlace place = new LatestNewsPlace(ctx, newsEntries);
        return createPlaceNavigation(baseUrl, isOnRemoteServer, place);
    }

    public PlaceNavigation<AuthenticationPlace> getSignInNavigation(boolean registration) {
        return createLocalPlaceNavigation(new AuthenticationPlace(registration));
    }
    
}
