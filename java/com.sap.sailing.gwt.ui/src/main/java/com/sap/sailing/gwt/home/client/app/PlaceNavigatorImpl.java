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
    private final static String SAPSAILING_DOMAIN = "www.sapsailing.com"; 
    
    protected PlaceNavigatorImpl(PlaceController placeController) {
        super();
        this.placeController = placeController;
    }

    @Override
    public void goToHome() {
        gotoPlace(new StartPlace(), new StartPlace.Tokenizer());
    }

    @Override
    public void goToEvent(String eventUuidAsString, String baseUrl) {
        EventPlace eventPlace = new EventPlace(eventUuidAsString);
        if(isLocationOnLocalhost() || baseUrl.contains(SAPSAILING_DOMAIN)) {
            placeController.goTo(eventPlace);
        } else {
            String remoteEventUrl = buildRemotePlaceUrl(baseUrl, eventPlace, new EventPlace.Tokenizer());
            Window.Location.replace(remoteEventUrl);
        }
    }

    @Override
    public void goToRegattaOfEvent(String eventUuidAsString, String leaderboardIdAsNameString, String baseUrl) {
        EventPlace eventPlace = new EventPlace(eventUuidAsString, leaderboardIdAsNameString);
        if(isLocationOnLocalhost() || baseUrl.contains(SAPSAILING_DOMAIN)) {
            placeController.goTo(eventPlace);
        } else {
            String remoteEventUrl = buildRemotePlaceUrl(baseUrl, eventPlace, new EventPlace.Tokenizer());
            Window.Location.replace(remoteEventUrl);
        }
    }

    @Override
    public void goToSearchResult(String searchQuery) {
        gotoPlace(new SearchResultPlace(searchQuery), new SearchResultPlace.Tokenizer());
    }

    @Override
    public void goToEvents() {
        gotoPlace(new EventsPlace(), new EventsPlace.Tokenizer());
    }

    @Override
    public void goToAboutUs() {
        gotoPlace(new AboutUsPlace(), new AboutUsPlace.Tokenizer());
    }

    @Override
    public void goToContact() {
        gotoPlace(new ContactPlace(), new ContactPlace.Tokenizer());
    }

    @Override
    public void goToSolutions() {
        gotoPlace(new SolutionsPlace(), new SolutionsPlace.Tokenizer());
    }

    @Override
    public void goToSponsoring() {
        gotoPlace(new SponsoringPlace(), new SponsoringPlace.Tokenizer());
    }

    private <T extends Place> void gotoPlace(T destinationPlace, PlaceTokenizer<T> tokenizer) {
        if(isLocationOnLocalhost() || isLocationOnSapSailingCom()) {
            placeController.goTo(destinationPlace); 
        } else {
            String homeUrl = buildRemotePlaceUrl("http://" + SAPSAILING_DOMAIN, destinationPlace, tokenizer);
            Window.Location.replace(homeUrl);
        }
    }
    
    private  <T extends Place> String buildRemotePlaceUrl(String baseUrl, T destinationPlace, PlaceTokenizer<T> tokenizer) {
        return baseUrl + "/gwt/Home.html#" + destinationPlace.getClass().getSimpleName() + ":" + tokenizer.getToken(destinationPlace);
    }
    
    private boolean isLocationOnSapSailingCom() {
        return Window.Location.getHostName().contains(SAPSAILING_DOMAIN);
    }

    private boolean isLocationOnLocalhost() {
        return Window.Location.getHostName().contains("localhost") || Window.Location.getHostName().contains("127.0.0.1");
    }
}
