package com.sap.sailing.gwt.home.mobile.app;

import java.util.List;

import com.google.gwt.place.shared.PlaceController;
import com.sap.sailing.gwt.home.communication.event.news.NewsEntryDTO;
import com.sap.sailing.gwt.home.desktop.places.aboutus.AboutUsPlace;
import com.sap.sailing.gwt.home.desktop.places.contact.ContactPlace;
import com.sap.sailing.gwt.home.desktop.places.event.regatta.overviewtab.RegattaOverviewPlace;
import com.sap.sailing.gwt.home.desktop.places.whatsnew.WhatsNewPlace;
import com.sap.sailing.gwt.home.desktop.places.whatsnew.WhatsNewPlace.WhatsNewNavigationTabs;
import com.sap.sailing.gwt.home.mobile.places.event.latestnews.LatestNewsPlace;
import com.sap.sailing.gwt.home.shared.app.AbstractPlaceNavigator;
import com.sap.sailing.gwt.home.shared.app.PlaceNavigation;
import com.sap.sailing.gwt.home.shared.places.event.AbstractEventPlace;
import com.sap.sailing.gwt.home.shared.places.event.EventContext;
import com.sap.sailing.gwt.home.shared.places.event.EventDefaultPlace;
import com.sap.sailing.gwt.home.shared.places.events.EventsPlace;
import com.sap.sailing.gwt.home.shared.places.fakeseries.AbstractSeriesPlace;
import com.sap.sailing.gwt.home.shared.places.fakeseries.SeriesDefaultPlace;
import com.sap.sailing.gwt.home.shared.places.solutions.SolutionsPlace;
import com.sap.sailing.gwt.home.shared.places.solutions.SolutionsPlace.SolutionsNavigationTabs;
import com.sap.sailing.gwt.home.shared.places.start.StartPlace;

public class MobilePlacesNavigator extends AbstractPlaceNavigator {

    protected MobilePlacesNavigator(PlaceController placeController, boolean isStandaloneServer) {
        super(placeController, isStandaloneServer);
    }

    public PlaceNavigation<StartPlace> getHomeNavigation() {
        return createGlobalPlaceNavigation(new StartPlace());
    }

    public PlaceNavigation<EventsPlace> getEventsNavigation() {
        return createGlobalPlaceNavigation(new EventsPlace());
    }

    public PlaceNavigation<EventDefaultPlace> getEventNavigation(String eventUuidAsString, String baseUrl,
            boolean isOnRemoteServer) {
        EventDefaultPlace eventPlace = new EventDefaultPlace(eventUuidAsString);
        return createPlaceNavigation(baseUrl, isOnRemoteServer, eventPlace);
    }

    public PlaceNavigation<SeriesDefaultPlace> getEventSeriesNavigation(String seriesId, String baseUrl,
            boolean isOnRemoteServer) {
        SeriesDefaultPlace place = new SeriesDefaultPlace(seriesId);
        return createPlaceNavigation(baseUrl, isOnRemoteServer, place);
    }
    
    public <P extends AbstractSeriesPlace> PlaceNavigation<P> getSeriesNavigation(P place, String baseUrl,
            boolean isOnRemoteServer) {
        return createPlaceNavigation(baseUrl, isOnRemoteServer, place);
    }

    public <P extends AbstractEventPlace> PlaceNavigation<P> getEventNavigation(P place, String baseUrl,
            boolean isOnRemoteServer) {
        return createPlaceNavigation(baseUrl, isOnRemoteServer, place);
    }

    public PlaceNavigation<LatestNewsPlace> getEventLastestNewsNavigation(EventContext ctx, List<NewsEntryDTO> newsEntries,
            String baseUrl, boolean isOnRemoteServer) {
        LatestNewsPlace place = new LatestNewsPlace(ctx, newsEntries);
        return createPlaceNavigation(baseUrl, isOnRemoteServer, place);
    }

    // NOT MOBILE PLACES

    public PlaceNavigation<SolutionsPlace> getSolutionsNavigation() {
        return createLocalPlaceNavigation(new SolutionsPlace(SolutionsNavigationTabs.SailingAnalytics));
    }

    public PlaceNavigation<WhatsNewPlace> getWhatsNewNavigation(WhatsNewNavigationTabs navigationTab) {
        return createLocalPlaceNavigation(new WhatsNewPlace(navigationTab));
    }

    public PlaceNavigation<AboutUsPlace> getAboutUsNavigation() {
        return createGlobalPlaceNavigation(new AboutUsPlace());
    }

    public PlaceNavigation<ContactPlace> getContactNavigation() {
        return createGlobalPlaceNavigation(new ContactPlace());
    }

    public PlaceNavigation<?> getRegattaOverviewNavigation(String eventId, String leaderboardName, String baseUrl,
            boolean isOnRemoteServer) {
        return createPlaceNavigation(baseUrl, isOnRemoteServer, new RegattaOverviewPlace(eventId, leaderboardName));
    }
}
