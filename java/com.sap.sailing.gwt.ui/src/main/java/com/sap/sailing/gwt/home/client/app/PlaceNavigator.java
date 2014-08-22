package com.sap.sailing.gwt.home.client.app;

public interface PlaceNavigator {
    void goToHome();
    void goToEvent(String eventUuidAsString, String baseUrl, boolean isOnRemoteServer);
    void goToRegattaOfEvent(String eventUuidAsString, String leaderboardIdAsNameString, String baseUrl, boolean isOnRemoteServer);
    void goToLeaderboard(String eventUuidAsString, String leaderboardIdAsNameString, String baseUrl, boolean isOnRemoteServer);
    void goToSearchResult(String searchQuery);
    void goToEvents();
    void goToAboutUs();
    void goToContact();
    void goToSolutions();
    void goToSponsoring();
}
