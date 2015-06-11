package com.sap.sailing.gwt.home.mobile.app;

import com.google.gwt.place.shared.PlaceController;
import com.sap.sailing.gwt.home.client.place.event.AbstractEventPlace;
import com.sap.sailing.gwt.home.client.place.event.EventDefaultPlace;
import com.sap.sailing.gwt.home.client.place.events.EventsPlace;
import com.sap.sailing.gwt.home.client.place.start.StartPlace;
import com.sap.sailing.gwt.home.client.place.whatsnew.WhatsNewPlace;
import com.sap.sailing.gwt.home.client.place.whatsnew.WhatsNewPlace.WhatsNewNavigationTabs;
import com.sap.sailing.gwt.home.shared.app.PlaceNavigation;

public class MobilePlacesNavigator extends AbstractPlaceNavigator {

    protected MobilePlacesNavigator(PlaceController placeController) {
        super(placeController);
    }
    public PlaceNavigation<StartPlace> getHomeNavigation() {
        return createGlobalPlaceNavigation(new StartPlace());
    }

    public PlaceNavigation<EventsPlace> getEventsNavigation() {
        return createGlobalPlaceNavigation(new EventsPlace());
    }

    public PlaceNavigation<WhatsNewPlace> getWhatsNewNavigation(WhatsNewNavigationTabs navigationTab) {
        return createLocalPlaceNavigation(new WhatsNewPlace(navigationTab));
    }

    public PlaceNavigation<EventDefaultPlace> getEventNavigation(String eventUuidAsString, String baseUrl, boolean isOnRemoteServer) {
        EventDefaultPlace eventPlace = new EventDefaultPlace(eventUuidAsString);
        return createPlaceNavigation(baseUrl, isOnRemoteServer, eventPlace);
    }
    
    public <P extends AbstractEventPlace> PlaceNavigation<P> getEventNavigation(P place, String baseUrl,
            boolean isOnRemoteServer) {
        return createPlaceNavigation(baseUrl, isOnRemoteServer, place);
    }

}
