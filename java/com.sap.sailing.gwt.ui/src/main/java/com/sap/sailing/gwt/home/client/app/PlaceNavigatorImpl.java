package com.sap.sailing.gwt.home.client.app;

import com.google.gwt.place.shared.Place;
import com.google.gwt.place.shared.PlaceController;
import com.google.gwt.place.shared.PlaceTokenizer;
import com.google.gwt.user.client.History;
import com.google.gwt.user.client.Window;
import com.sap.sailing.gwt.home.client.place.aboutus.AboutUsPlace;
import com.sap.sailing.gwt.home.client.place.contact.ContactPlace;
import com.sap.sailing.gwt.home.client.place.event.EventPlace;
import com.sap.sailing.gwt.home.client.place.event.EventPlace.NavigationTabs;
import com.sap.sailing.gwt.home.client.place.events.EventsPlace;
import com.sap.sailing.gwt.home.client.place.leaderboard.LeaderboardPlace;
import com.sap.sailing.gwt.home.client.place.searchresult.SearchResultPlace;
import com.sap.sailing.gwt.home.client.place.solutions.SolutionsPlace;
import com.sap.sailing.gwt.home.client.place.sponsoring.SponsoringPlace;
import com.sap.sailing.gwt.home.client.place.start.StartPlace;

public class PlaceNavigatorImpl implements PlaceNavigator {
    private final PlaceController placeController;
    public final static String DEFAULT_SAPSAILING_SERVER = "www.sapsailing.com"; 
    public final static String DEFAULT_SAPSAILING_SERVER_URL = "http://" + DEFAULT_SAPSAILING_SERVER;  

    protected PlaceNavigatorImpl(PlaceController placeController) {
        super();
        this.placeController = placeController;
    }

    public <T extends Place> void goToPlace(PlaceNavigation<T> placeNavigation) {
        if(placeNavigation.isRemotePlace()) {
            String destinationUrl = placeNavigation.getTargetUrl();
            History.newItem(History.getToken());
            Window.Location.replace(destinationUrl);
        } else {
            placeController.goTo(placeNavigation.getPlace()); 
        }
    }

    @Override
    public PlaceNavigation<StartPlace> getHomeNavigation() {
        return createPlaceNavigation(getLocationURL(), new StartPlace(), new StartPlace.Tokenizer());
    }

    @Override
    public PlaceNavigation<EventsPlace> getEventsNavigation() {
        return createPlaceNavigation(getLocationURL(), new EventsPlace(), new EventsPlace.Tokenizer());
    }

    @Override
    public PlaceNavigation<SolutionsPlace> getSolutionsNavigation() {
        return createPlaceNavigation(getLocationURL(), new SolutionsPlace(), new SolutionsPlace.Tokenizer());
    }

    @Override
    public PlaceNavigation<SponsoringPlace> getSponsoringNavigation() {
        return createPlaceNavigation(getLocationURL(), new SponsoringPlace(), new SponsoringPlace.Tokenizer());
    }

    @Override
    public PlaceNavigation<AboutUsPlace> getAboutUsNavigation() {
        return createPlaceNavigation(getLocationURL(), new AboutUsPlace(), new AboutUsPlace.Tokenizer());
    }

    @Override
    public PlaceNavigation<ContactPlace> getContactNavigation() {
        return createPlaceNavigation(getLocationURL(), new ContactPlace(), new ContactPlace.Tokenizer());
    }

    @Override
    public PlaceNavigation<EventPlace> getEventNavigation(String eventUuidAsString, String baseUrl, boolean isOnRemoteServer) {
        EventPlace eventPlace = new EventPlace(eventUuidAsString, NavigationTabs.Regattas, null);
        return createPlaceNavigation(baseUrl, isOnRemoteServer, eventPlace, new EventPlace.Tokenizer());
    }

    @Override
    public PlaceNavigation<EventPlace> getRegattaNavigation(String eventUuidAsString, String leaderboardIdAsNameString, String baseUrl, boolean isOnRemoteServer) {
        EventPlace eventPlace = new EventPlace(eventUuidAsString, NavigationTabs.Regatta, leaderboardIdAsNameString);
        return createPlaceNavigation(baseUrl, isOnRemoteServer, eventPlace, new EventPlace.Tokenizer());
    }

    @Override
    public PlaceNavigation<LeaderboardPlace> getLeaderboardNavigation(String eventUuidAsString, String leaderboardIdAsNameString, String baseUrl, boolean isOnRemoteServer) {
        LeaderboardPlace leaderboardPlace = new LeaderboardPlace(eventUuidAsString, leaderboardIdAsNameString, true, true);
        return createPlaceNavigation(baseUrl, isOnRemoteServer, leaderboardPlace, new LeaderboardPlace.Tokenizer());
    }

    @Override
    public PlaceNavigation<SearchResultPlace> getSearchResultNavigation(String searchQuery) {
        return createPlaceNavigation(getLocationURL(), new SearchResultPlace(searchQuery), new SearchResultPlace.Tokenizer());
    }

    private <T extends Place> PlaceNavigation<T> createPlaceNavigation(String baseUrl, T destinationPlace, PlaceTokenizer<T> tokenizer) {
        return new PlaceNavigation<T>(baseUrl, destinationPlace, tokenizer);
    }

    private <T extends Place> PlaceNavigation<T> createPlaceNavigation(String baseUrl, boolean isOnRemoteServer, T destinationPlace, PlaceTokenizer<T> tokenizer) {
        return new PlaceNavigation<T>(baseUrl, destinationPlace, tokenizer, isOnRemoteServer);
    }
    
    private String getLocationURL() {
        return Window.Location.getProtocol() + "//" + Window.Location.getHostName();
    }
}
