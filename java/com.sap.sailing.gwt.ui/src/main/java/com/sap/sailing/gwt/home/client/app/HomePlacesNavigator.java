package com.sap.sailing.gwt.home.client.app;

import com.google.gwt.place.shared.PlaceController;
import com.google.gwt.place.shared.PlaceTokenizer;
import com.sap.sailing.gwt.home.client.place.aboutus.AboutUsPlace;
import com.sap.sailing.gwt.home.client.place.contact.ContactPlace;
import com.sap.sailing.gwt.home.client.place.event2.EventDefaultPlace;
import com.sap.sailing.gwt.home.client.place.event2.multiregatta.tabs.MultiregattaRegattasPlace;
import com.sap.sailing.gwt.home.client.place.event2.regatta.AbstractEventRegattaPlace;
import com.sap.sailing.gwt.home.client.place.event2.regatta.tabs.RegattaCompetitorAnalyticsPlace;
import com.sap.sailing.gwt.home.client.place.event2.regatta.tabs.RegattaLeaderboardPlace;
import com.sap.sailing.gwt.home.client.place.event2.regatta.tabs.RegattaRacesPlace;
import com.sap.sailing.gwt.home.client.place.events.EventsPlace;
import com.sap.sailing.gwt.home.client.place.searchresult.SearchResultPlace;
import com.sap.sailing.gwt.home.client.place.series.SeriesPlace;
import com.sap.sailing.gwt.home.client.place.series.SeriesPlace.SeriesNavigationTabs;
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
        return createGlobalPlaceNavigation(new StartPlace(), new StartPlace.Tokenizer());
    }

    public PlaceNavigation<EventsPlace> getEventsNavigation() {
        return createGlobalPlaceNavigation(new EventsPlace(), new EventsPlace.Tokenizer());
    }

    public PlaceNavigation<SolutionsPlace> getSolutionsNavigation(SolutionsNavigationTabs navigationTab) {
        return createLocalPlaceNavigation(new SolutionsPlace(navigationTab), new SolutionsPlace.Tokenizer());
    }

    public PlaceNavigation<WhatsNewPlace> getWhatsNewNavigation(WhatsNewNavigationTabs navigationTab) {
        return createLocalPlaceNavigation(new WhatsNewPlace(navigationTab), new WhatsNewPlace.Tokenizer());
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
    
    public PlaceNavigation<MultiregattaRegattasPlace> getEventRegattasNavigation(String eventUuidAsString, String baseUrl, boolean isOnRemoteServer) {
        MultiregattaRegattasPlace eventPlace = new MultiregattaRegattasPlace(eventUuidAsString);
        return createPlaceNavigation(baseUrl, isOnRemoteServer, eventPlace, new MultiregattaRegattasPlace.Tokenizer());
    }

    public PlaceNavigation<EventDefaultPlace> getEventNavigation(String eventUuidAsString, String baseUrl, boolean isOnRemoteServer) {
        EventDefaultPlace eventPlace = new EventDefaultPlace(eventUuidAsString);
        return createPlaceNavigation(baseUrl, isOnRemoteServer, eventPlace, new EventDefaultPlace.Tokenizer());
    }

    @SuppressWarnings({ "unchecked", "rawtypes" })
    public PlaceNavigation<AbstractEventRegattaPlace> getRegattaNavigation(String eventUuidAsString, String leaderboardIdAsNameString, String baseUrl, boolean isOnRemoteServer) {
        // TODO RegattaOverviewPlace not implemented yet
        RegattaRacesPlace eventPlace = new RegattaRacesPlace(eventUuidAsString, leaderboardIdAsNameString);
        return createPlaceNavigation(baseUrl, isOnRemoteServer, eventPlace, (PlaceTokenizer)new RegattaRacesPlace.Tokenizer());
    }

    public PlaceNavigation<RegattaCompetitorAnalyticsPlace> getCompetitorAnalyticsNavigation(String eventUuidAsString, String regattaId, String baseUrl, boolean isOnRemoteServer) {
        RegattaCompetitorAnalyticsPlace regattaPlace = new RegattaCompetitorAnalyticsPlace(eventUuidAsString, regattaId);
        return createPlaceNavigation(baseUrl, isOnRemoteServer, regattaPlace, new RegattaCompetitorAnalyticsPlace.Tokenizer());
    }
    
    public PlaceNavigation<RegattaLeaderboardPlace> getLeaderboardNavigation(String eventUuidAsString, String regattaId, String baseUrl, boolean isOnRemoteServer) {
        RegattaLeaderboardPlace regattaPlace = new RegattaLeaderboardPlace(eventUuidAsString, regattaId);
        return createPlaceNavigation(baseUrl, isOnRemoteServer, regattaPlace, new RegattaLeaderboardPlace.Tokenizer());
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
