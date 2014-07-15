package com.sap.sailing.gwt.home.client.app;

public interface PlaceNavigator {
    void goToHome();
    void goToEvent(String eventUuidAsString, String baseUrl);
    void goToRegattaOfEvent(String eventUuidAsString, String regattaIdAsNameString, String baseUrl);
    void goToSearchResult(String searchQuery);
    void goToEvents();
    void goToAboutUs();
    void goToContact();
    void goToSolutions();
    void goToSponsoring();
}
