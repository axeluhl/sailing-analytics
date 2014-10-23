package com.sap.sailing.gwt.home.client.app;

import com.google.gwt.place.shared.Place;
import com.sap.sailing.gwt.home.client.place.aboutus.AboutUsPlace;
import com.sap.sailing.gwt.home.client.place.contact.ContactPlace;
import com.sap.sailing.gwt.home.client.place.event.EventPlace;
import com.sap.sailing.gwt.home.client.place.events.EventsPlace;
import com.sap.sailing.gwt.home.client.place.leaderboard.LeaderboardPlace;
import com.sap.sailing.gwt.home.client.place.searchresult.SearchResultPlace;
import com.sap.sailing.gwt.home.client.place.solutions.SolutionsPlace;
import com.sap.sailing.gwt.home.client.place.sponsoring.SponsoringPlace;
import com.sap.sailing.gwt.home.client.place.start.StartPlace;


public interface PlaceNavigator {
    <T extends Place> void goToPlace(PlaceNavigation<T> placeNavigation);

    PlaceNavigation<StartPlace> getHomeNavigation();
    PlaceNavigation<EventsPlace> getEventsNavigation();
    PlaceNavigation<SolutionsPlace> getSolutionsNavigation();
    PlaceNavigation<SponsoringPlace> getSponsoringNavigation();
    PlaceNavigation<AboutUsPlace> getAboutUsNavigation();
    PlaceNavigation<ContactPlace> getContactNavigation();

    PlaceNavigation<EventPlace> getEventNavigation(String eventUuidAsString, String baseUrl, boolean isOnRemoteServer);
    PlaceNavigation<EventPlace> getRegattaNavigation(String eventUuidAsString, String leaderboardIdAsNameString, String baseUrl, boolean isOnRemoteServer);
    PlaceNavigation<LeaderboardPlace> getLeaderboardNavigation(String eventUuidAsString, String leaderboardIdAsNameString, String baseUrl, boolean isOnRemoteServer);
    PlaceNavigation<SearchResultPlace> getSearchResultNavigation(String searchQuery);

//    void goToEvent(String eventUuidAsString, String baseUrl, boolean isOnRemoteServer);
//    void goToRegattaOfEvent(String eventUuidAsString, String leaderboardIdAsNameString, String baseUrl, boolean isOnRemoteServer);
//    void goToLeaderboard(String eventUuidAsString, String leaderboardIdAsNameString, String baseUrl, boolean isOnRemoteServer);
//    void goToSearchResult(String searchQuery);
}
