package com.sap.sailing.gwt.home.client.app;

import com.google.gwt.place.shared.PlaceController;
import com.sap.sailing.gwt.home.client.place.aboutus.AboutUsPlace;
import com.sap.sailing.gwt.home.client.place.contact.ContactPlace;
import com.sap.sailing.gwt.home.client.place.event.EventPlace;
import com.sap.sailing.gwt.home.client.place.event.EventPlace.EventNavigationTabs;
import com.sap.sailing.gwt.home.client.place.events.EventsPlace;
import com.sap.sailing.gwt.home.client.place.regatta.RegattaPlace;
import com.sap.sailing.gwt.home.client.place.regatta.RegattaPlace.RegattaNavigationTabs;
import com.sap.sailing.gwt.home.client.place.searchresult.SearchResultPlace;
import com.sap.sailing.gwt.home.client.place.series.SeriesPlace;
import com.sap.sailing.gwt.home.client.place.series.SeriesPlace.SeriesNavigationTabs;
import com.sap.sailing.gwt.home.client.place.solutions.SolutionsPlace;
import com.sap.sailing.gwt.home.client.place.solutions.SolutionsPlace.SolutionsNavigationTabs;
import com.sap.sailing.gwt.home.client.place.sponsoring.SponsoringPlace;
import com.sap.sailing.gwt.home.client.place.start.StartPlace;

public class HomePlacesNavigator extends AbstractPlaceNavigator {

    protected HomePlacesNavigator(PlaceController placeController) {
        super(placeController);
    }

    public PlaceNavigation<StartPlace> getHomeNavigation() {
        return createGlobalPlaceNavigation(new StartPlace(), new StartPlace.Tokenizer());
    }

    public PlaceNavigation<EventsPlace> getEventsNavigation() {
        return createGlobalPlaceNavigation(new EventsPlace(), new EventsPlace.Tokenizer());
    }

    public PlaceNavigation<SolutionsPlace> getSolutionsNavigation(SolutionsNavigationTabs navigationTab) {
        return createLocalPlaceNavigation(new SolutionsPlace(navigationTab), new SolutionsPlace.Tokenizer());
    }

    public PlaceNavigation<SponsoringPlace> getSponsoringNavigation() {
        return createGlobalPlaceNavigation(new SponsoringPlace(), new SponsoringPlace.Tokenizer());
    }

    public PlaceNavigation<AboutUsPlace> getAboutUsNavigation() {
        return createGlobalPlaceNavigation(new AboutUsPlace(), new AboutUsPlace.Tokenizer());
    }

    public PlaceNavigation<ContactPlace> getContactNavigation() {
        return createGlobalPlaceNavigation(new ContactPlace(), new ContactPlace.Tokenizer());
    }

    public PlaceNavigation<EventPlace> getEventNavigation(String eventUuidAsString, String baseUrl, boolean isOnRemoteServer) {
        EventPlace eventPlace = new EventPlace(eventUuidAsString, EventNavigationTabs.Regattas, null);
        return createPlaceNavigation(baseUrl, isOnRemoteServer, eventPlace, new EventPlace.Tokenizer());
    }

    public PlaceNavigation<EventPlace> getRegattaNavigation(String eventUuidAsString, String leaderboardIdAsNameString, String baseUrl, boolean isOnRemoteServer) {
        EventPlace eventPlace = new EventPlace(eventUuidAsString, EventNavigationTabs.Regatta, leaderboardIdAsNameString);
        return createPlaceNavigation(baseUrl, isOnRemoteServer, eventPlace, new EventPlace.Tokenizer());
    }

    /** this place will be merged into the common regatta view as tab later on */
    public PlaceNavigation<RegattaPlace> getRegattaAnalyticsNavigation(String eventUuidAsString, RegattaNavigationTabs navigationTab, String leaderboardIdAsNameString, String baseUrl, boolean isOnRemoteServer) {
        RegattaPlace regattaPlace = new RegattaPlace(eventUuidAsString, navigationTab, leaderboardIdAsNameString, true, true);
        return createPlaceNavigation(baseUrl, isOnRemoteServer, regattaPlace, new RegattaPlace.Tokenizer());
    }

    /** this place will be merged into the common series event view as tab later on */
    public PlaceNavigation<SeriesPlace> getSeriesAnalyticsNavigation(String eventUuidAsString, SeriesNavigationTabs navigationTab, String leaderboardIdAsNameString, String baseUrl, boolean isOnRemoteServer) {
        SeriesPlace seriesPlace = new SeriesPlace(eventUuidAsString, navigationTab, leaderboardIdAsNameString, true, true);
        return createPlaceNavigation(baseUrl, isOnRemoteServer, seriesPlace, new SeriesPlace.Tokenizer());
    }

    public PlaceNavigation<SearchResultPlace> getSearchResultNavigation(String searchQuery) {
        return createGlobalPlaceNavigation(new SearchResultPlace(searchQuery), new SearchResultPlace.Tokenizer());
    }
}
