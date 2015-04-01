package com.sap.sailing.gwt.home.client.app;

import com.google.gwt.place.shared.PlaceController;
import com.sap.sailing.gwt.home.client.place.aboutus.AboutUsPlace;
import com.sap.sailing.gwt.home.client.place.contact.ContactPlace;
import com.sap.sailing.gwt.home.client.place.event.AbstractEventPlace;
import com.sap.sailing.gwt.home.client.place.event.EventDefaultPlace;
import com.sap.sailing.gwt.home.client.place.event.multiregatta.tabs.MultiregattaRegattasPlace;
import com.sap.sailing.gwt.home.client.place.event.regatta.AbstractEventRegattaPlace;
import com.sap.sailing.gwt.home.client.place.event.regatta.tabs.RegattaCompetitorAnalyticsPlace;
import com.sap.sailing.gwt.home.client.place.event.regatta.tabs.RegattaLeaderboardPlace;
import com.sap.sailing.gwt.home.client.place.event.regatta.tabs.RegattaOverviewPlace;
import com.sap.sailing.gwt.home.client.place.events.EventsPlace;
import com.sap.sailing.gwt.home.client.place.fakeseries.SeriesDefaultPlace;
import com.sap.sailing.gwt.home.client.place.searchresult.SearchResultPlace;
import com.sap.sailing.gwt.home.client.place.solutions.SolutionsPlace;
import com.sap.sailing.gwt.home.client.place.solutions.SolutionsPlace.SolutionsNavigationTabs;
import com.sap.sailing.gwt.home.client.place.sponsoring.SponsoringPlace;
import com.sap.sailing.gwt.home.client.place.start.StartPlace;
import com.sap.sailing.gwt.home.client.place.whatsnew.WhatsNewPlace;
import com.sap.sailing.gwt.home.client.place.whatsnew.WhatsNewPlace.WhatsNewNavigationTabs;

public class HomePlacesNavigator extends AbstractPlaceNavigator {

    protected HomePlacesNavigator(PlaceController placeController) {
        super(placeController);
    }

    public PlaceNavigation<StartPlace> getHomeNavigation() {
        return createGlobalPlaceNavigation(new StartPlace());
    }

    public PlaceNavigation<EventsPlace> getEventsNavigation() {
        return createGlobalPlaceNavigation(new EventsPlace());
    }

    public PlaceNavigation<SolutionsPlace> getSolutionsNavigation(SolutionsNavigationTabs navigationTab) {
        return createLocalPlaceNavigation(new SolutionsPlace(navigationTab));
    }

    public PlaceNavigation<WhatsNewPlace> getWhatsNewNavigation(WhatsNewNavigationTabs navigationTab) {
        return createLocalPlaceNavigation(new WhatsNewPlace(navigationTab));
    }

    public PlaceNavigation<SponsoringPlace> getSponsoringNavigation() {
        return createGlobalPlaceNavigation(new SponsoringPlace());
    }

    public PlaceNavigation<AboutUsPlace> getAboutUsNavigation() {
        return createGlobalPlaceNavigation(new AboutUsPlace());
    }

    public PlaceNavigation<ContactPlace> getContactNavigation() {
        return createGlobalPlaceNavigation(new ContactPlace());
    }
    
    public PlaceNavigation<MultiregattaRegattasPlace> getEventRegattasNavigation(String eventUuidAsString, String baseUrl, boolean isOnRemoteServer) {
        MultiregattaRegattasPlace eventPlace = new MultiregattaRegattasPlace(eventUuidAsString);
        return createPlaceNavigation(baseUrl, isOnRemoteServer, eventPlace);
    }
    
    public PlaceNavigation<SeriesDefaultPlace> getEventSeriesNavigation(String seriesId, String baseUrl, boolean isOnRemoteServer) {
        SeriesDefaultPlace place = new SeriesDefaultPlace(seriesId);
        return createPlaceNavigation(baseUrl, isOnRemoteServer, place);
    }

    public PlaceNavigation<EventDefaultPlace> getEventNavigation(String eventUuidAsString, String baseUrl, boolean isOnRemoteServer) {
        EventDefaultPlace eventPlace = new EventDefaultPlace(eventUuidAsString);
        return createPlaceNavigation(baseUrl, isOnRemoteServer, eventPlace);
    }
    
    public <P extends AbstractEventPlace> PlaceNavigation<P> getEventNavigation(P place, String baseUrl,
            boolean isOnRemoteServer) {
        return createPlaceNavigation(baseUrl, isOnRemoteServer, place);
    }

    @SuppressWarnings({ "unchecked", "rawtypes" })
    public PlaceNavigation<AbstractEventRegattaPlace> getRegattaNavigation(String eventUuidAsString, String leaderboardIdAsNameString, String baseUrl, boolean isOnRemoteServer) {
        RegattaOverviewPlace eventPlace = new RegattaOverviewPlace(eventUuidAsString, leaderboardIdAsNameString);
        return (PlaceNavigation) createPlaceNavigation(baseUrl, isOnRemoteServer, eventPlace);
    }

    public PlaceNavigation<RegattaCompetitorAnalyticsPlace> getCompetitorAnalyticsNavigation(String eventUuidAsString, String regattaId, String baseUrl, boolean isOnRemoteServer) {
        RegattaCompetitorAnalyticsPlace regattaPlace = new RegattaCompetitorAnalyticsPlace(eventUuidAsString, regattaId);
        return createPlaceNavigation(baseUrl, isOnRemoteServer, regattaPlace);
    }
    
    public PlaceNavigation<RegattaLeaderboardPlace> getLeaderboardNavigation(String eventUuidAsString, String regattaId, String baseUrl, boolean isOnRemoteServer) {
        RegattaLeaderboardPlace regattaPlace = new RegattaLeaderboardPlace(eventUuidAsString, regattaId);
        return createPlaceNavigation(baseUrl, isOnRemoteServer, regattaPlace);
    }

    public PlaceNavigation<SearchResultPlace> getSearchResultNavigation(String searchQuery) {
        return createGlobalPlaceNavigation(new SearchResultPlace(searchQuery));
    }
}
