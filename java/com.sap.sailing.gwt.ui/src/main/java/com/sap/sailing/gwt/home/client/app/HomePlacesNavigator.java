package com.sap.sailing.gwt.home.client.app;

import com.google.gwt.place.shared.PlaceController;
import com.sap.sailing.gwt.home.client.place.aboutus.AboutUsPlace;
import com.sap.sailing.gwt.home.client.place.contact.ContactPlace;
import com.sap.sailing.gwt.home.client.place.event.EventPlace;
import com.sap.sailing.gwt.home.client.place.event.EventPlace.EventNavigationTabs;
import com.sap.sailing.gwt.home.client.place.events.EventsPlace;
import com.sap.sailing.gwt.home.client.place.leaderboard.LeaderboardPlace;
import com.sap.sailing.gwt.home.client.place.searchresult.SearchResultPlace;
import com.sap.sailing.gwt.home.client.place.solutions.SolutionsPlace;
import com.sap.sailing.gwt.home.client.place.sponsoring.SponsoringPlace;
import com.sap.sailing.gwt.home.client.place.start.StartPlace;

public class HomePlacesNavigator extends AbstractPlaceNavigator {

    protected HomePlacesNavigator(PlaceController placeController) {
        super(placeController);
    }

    public PlaceNavigation<StartPlace> getHomeNavigation() {
        return createPlaceNavigation(getLocationURL(), new StartPlace(), new StartPlace.Tokenizer());
    }

    public PlaceNavigation<EventsPlace> getEventsNavigation() {
        return createPlaceNavigation(getLocationURL(), new EventsPlace(), new EventsPlace.Tokenizer());
    }

    public PlaceNavigation<SolutionsPlace> getSolutionsNavigation() {
        return createPlaceNavigation(getLocationURL(), new SolutionsPlace(), new SolutionsPlace.Tokenizer());
    }

    public PlaceNavigation<SponsoringPlace> getSponsoringNavigation() {
        return createPlaceNavigation(getLocationURL(), new SponsoringPlace(), new SponsoringPlace.Tokenizer());
    }

    public PlaceNavigation<AboutUsPlace> getAboutUsNavigation() {
        return createPlaceNavigation(getLocationURL(), new AboutUsPlace(), new AboutUsPlace.Tokenizer());
    }

    public PlaceNavigation<ContactPlace> getContactNavigation() {
        return createPlaceNavigation(getLocationURL(), new ContactPlace(), new ContactPlace.Tokenizer());
    }

    public PlaceNavigation<EventPlace> getEventNavigation(String eventUuidAsString, String baseUrl, boolean isOnRemoteServer) {
        EventPlace eventPlace = new EventPlace(eventUuidAsString, EventNavigationTabs.Regattas, null);
        return createPlaceNavigation(baseUrl, isOnRemoteServer, eventPlace, new EventPlace.Tokenizer());
    }

    public PlaceNavigation<EventPlace> getRegattaNavigation(String eventUuidAsString, String leaderboardIdAsNameString, String baseUrl, boolean isOnRemoteServer) {
        EventPlace eventPlace = new EventPlace(eventUuidAsString, EventNavigationTabs.Regatta, leaderboardIdAsNameString);
        return createPlaceNavigation(baseUrl, isOnRemoteServer, eventPlace, new EventPlace.Tokenizer());
    }

    public PlaceNavigation<LeaderboardPlace> getLeaderboardNavigation(String eventUuidAsString, String leaderboardIdAsNameString, String baseUrl, boolean isOnRemoteServer) {
        LeaderboardPlace leaderboardPlace = new LeaderboardPlace(eventUuidAsString, leaderboardIdAsNameString, true, true);
        return createPlaceNavigation(baseUrl, isOnRemoteServer, leaderboardPlace, new LeaderboardPlace.Tokenizer());
    }

    public PlaceNavigation<SearchResultPlace> getSearchResultNavigation(String searchQuery) {
        return createPlaceNavigation(getLocationURL(), new SearchResultPlace(searchQuery), new SearchResultPlace.Tokenizer());
    }
}
