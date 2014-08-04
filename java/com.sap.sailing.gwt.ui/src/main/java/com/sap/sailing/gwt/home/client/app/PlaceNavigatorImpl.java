package com.sap.sailing.gwt.home.client.app;

import com.google.gwt.place.shared.Place;
import com.google.gwt.place.shared.PlaceController;
import com.google.gwt.place.shared.PlaceTokenizer;
import com.google.gwt.user.client.Window;
import com.sap.sailing.gwt.home.client.place.aboutus.AboutUsPlace;
import com.sap.sailing.gwt.home.client.place.contact.ContactPlace;
import com.sap.sailing.gwt.home.client.place.event.EventPlace;
import com.sap.sailing.gwt.home.client.place.events.EventsPlace;
import com.sap.sailing.gwt.home.client.place.searchresult.SearchResultPlace;
import com.sap.sailing.gwt.home.client.place.solutions.SolutionsPlace;
import com.sap.sailing.gwt.home.client.place.sponsoring.SponsoringPlace;
import com.sap.sailing.gwt.home.client.place.start.StartPlace;

public class PlaceNavigatorImpl implements PlaceNavigator {
    private final PlaceController placeController;
    private final static String DEFAULT_SAPSAILING_SERVER = "dev.sapsailing.com"; // www.sapsailing.com 
    
    protected PlaceNavigatorImpl(PlaceController placeController) {
        super();
        this.placeController = placeController;
    }

    @Override
    public void goToHome() {
        gotoPlace(getLocationURL(), new StartPlace(), new StartPlace.Tokenizer());
    }

    @Override
    public void goToEvent(String eventUuidAsString, String baseUrl, boolean isOnRemoteServer) {
        EventPlace eventPlace = new EventPlace(eventUuidAsString, null);
        gotoPlace(baseUrl, isOnRemoteServer, eventPlace, new EventPlace.Tokenizer());
    }

    @Override
    public void goToRegattaOfEvent(String eventUuidAsString, String leaderboardIdAsNameString, String baseUrl, boolean isOnRemoteServer) {
        EventPlace eventPlace = new EventPlace(eventUuidAsString, leaderboardIdAsNameString);
        gotoPlace(baseUrl, isOnRemoteServer, eventPlace, new EventPlace.Tokenizer());
    }

    @Override
    public void goToSearchResult(String searchQuery) {
        gotoPlace(getLocationURL(), new SearchResultPlace(searchQuery), new SearchResultPlace.Tokenizer());
    }

    @Override
    public void goToEvents() {
        gotoPlace(getLocationURL(), new EventsPlace(), new EventsPlace.Tokenizer());
    }

    @Override
    public void goToAboutUs() {
        gotoPlace(new AboutUsPlace());
    }

    @Override
    public void goToContact() {
        gotoPlace(new ContactPlace());
    }

    @Override
    public void goToSolutions() {
        gotoPlace(new SolutionsPlace());
    }

    @Override
    public void goToSponsoring() {
        gotoPlace(new SponsoringPlace());
    }

    private <T extends Place> void gotoPlace(T destinationPlace) {
        placeController.goTo(destinationPlace); 
    }

    private <T extends Place> void gotoPlace(String baseUrl, T destinationPlace, PlaceTokenizer<T> tokenizer) {
        if(isLocationOnLocalhost(baseUrl) || isLocationOnDefaultSapSailingServer(baseUrl)) {
            placeController.goTo(destinationPlace); 
        } else {
            String homeUrl = buildRemotePlaceUrl(DEFAULT_SAPSAILING_SERVER, destinationPlace, tokenizer);
            Window.Location.replace(homeUrl);
        }
    }

    private <T extends Place> void gotoPlace(String baseUrl, boolean isOnRemoteServer, T destinationPlace, PlaceTokenizer<T> tokenizer) {
        if(isLocationOnLocalhost(baseUrl) || !isOnRemoteServer) {
            placeController.goTo(destinationPlace); 
        } else {
            String homeUrl = buildRemotePlaceUrl(baseUrl, destinationPlace, tokenizer);
            Window.Location.replace(homeUrl);
        }
    }

    private String getLocationURL() {
        return Window.Location.getProtocol() + "//" + Window.Location.getHostName();
    }
    
    private  <T extends Place> String buildRemotePlaceUrl(String baseUrl, T destinationPlace, PlaceTokenizer<T> tokenizer) {
        return baseUrl + "/gwt/Home.html#" + destinationPlace.getClass().getSimpleName() + ":" + tokenizer.getToken(destinationPlace);
    }

    private boolean isLocationOnDefaultSapSailingServer(String urlToCheck) {
        return urlToCheck.contains(DEFAULT_SAPSAILING_SERVER);
    }

    private boolean isLocationOnLocalhost(String urlToCheck) {
        return urlToCheck.contains("localhost") || urlToCheck.contains("127.0.0.1");
    }
}
