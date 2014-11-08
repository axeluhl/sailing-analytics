package com.sap.sailing.gwt.home.client.app;

import com.google.gwt.place.shared.Place;
import com.google.gwt.place.shared.PlaceController;
import com.google.gwt.place.shared.PlaceTokenizer;
import com.google.gwt.user.client.History;
import com.google.gwt.user.client.Window;

public abstract class AbstractPlaceNavigator implements PlaceNavigator {
    protected final PlaceController placeController;
    public final static String DEFAULT_SAPSAILING_SERVER = "www.sapsailing.com"; 
    public final static String DEFAULT_SAPSAILING_SERVER_URL = "http://" + DEFAULT_SAPSAILING_SERVER;  

    protected AbstractPlaceNavigator(PlaceController placeController) {
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

    protected <T extends Place> PlaceNavigation<T> createGlobalPlaceNavigation(T destinationPlace, PlaceTokenizer<T> tokenizer) {
        return new PlaceNavigation<T>(destinationPlace, tokenizer);
    }

    protected <T extends Place> PlaceNavigation<T> createPlaceNavigation(String baseUrl, boolean isOnRemoteServer, T destinationPlace, PlaceTokenizer<T> tokenizer) {
        return new PlaceNavigation<T>(baseUrl, destinationPlace, tokenizer, isOnRemoteServer);
    }
    
    public <T extends Place> void pushPlaceToHistoryStack(T destinationPlace, PlaceTokenizer<T> tokenizer) {
        String placeHistoryToken = destinationPlace.getClass().getSimpleName() + ":" + tokenizer.getToken(destinationPlace);
        History.newItem(placeHistoryToken, false);
    }
}
