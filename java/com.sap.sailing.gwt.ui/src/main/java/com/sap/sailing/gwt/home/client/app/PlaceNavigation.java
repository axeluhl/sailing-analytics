package com.sap.sailing.gwt.home.client.app;

import com.google.gwt.core.client.GWT;
import com.google.gwt.place.shared.Place;
import com.google.gwt.place.shared.PlaceTokenizer;
import com.google.gwt.user.client.Window;

public class PlaceNavigation<T extends Place> {
    private final T destinationPlace;
    private final PlaceTokenizer<T> tokenizer;
    private final String baseUrl;
    private final boolean isDestinationOnRemoteServer;
    private final PlaceNavigator placeNavigator;
    
    public PlaceNavigation(T destinationPlace, PlaceTokenizer<T> tokenizer, PlaceNavigator placeNavigator) {
        this.destinationPlace = destinationPlace;
        this.tokenizer = tokenizer;
        this.placeNavigator = placeNavigator;
        String locationURL = getLocationURL();
        this.isDestinationOnRemoteServer = !(isLocationOnLocalhost(locationURL) || isLocationOnDefaultSapSailingServer(locationURL));
        this.baseUrl = isDestinationOnRemoteServer ? AbstractPlaceNavigator.DEFAULT_SAPSAILING_SERVER_URL : locationURL; 
    }

    public PlaceNavigation(String baseUrl, T destinationPlace, PlaceTokenizer<T> tokenizer, boolean isDestinationOnRemoteServer, PlaceNavigator placeNavigator) {
        this.destinationPlace = destinationPlace;
        this.tokenizer = tokenizer;
        this.isDestinationOnRemoteServer = isDestinationOnRemoteServer;
        this.placeNavigator = placeNavigator;
        this.baseUrl = isDestinationOnRemoteServer ? baseUrl : getLocationURL(); 
    }

    public String getTargetUrl() {
        return buildPlaceUrl();
    }

    public String getHistoryUrl() {
        String placeUrl = buildPlaceUrl();
        if(placeUrl.startsWith("#")) {
            placeUrl = placeUrl.substring(1, placeUrl.length());
        }
        return placeUrl;
    }

    public Place getPlace() {
        return destinationPlace;
    }

    private String buildPlaceUrl() {
        String url = "";
        if(isRemotePlace()) {
            url = baseUrl + "/gwt/Home.html";
            if(!GWT.isProdMode()) {
                url += "?gwt.codesvr=127.0.0.1:9997"; 
            }
            url += getPlaceToken();
        } else {
            url = getPlaceToken();
        }
        return url;
    }
    
    public boolean isRemotePlace() {
        return isDestinationOnRemoteServer;
    }

    private String getPlaceToken() {
        return "#" + destinationPlace.getClass().getSimpleName() + ":" + tokenizer.getToken(destinationPlace);
    }

    private boolean isLocationOnDefaultSapSailingServer(String urlToCheck) {
        return urlToCheck.contains(HomePlacesNavigator.DEFAULT_SAPSAILING_SERVER);
    }

    private boolean isLocationOnLocalhost(String urlToCheck) {
        return urlToCheck.contains("localhost") || urlToCheck.contains("127.0.0.1");
    }
    
    private String getLocationURL() {
        return Window.Location.getProtocol() + "//" + Window.Location.getHostName() + ":" + Window.Location.getPort();
    }
    
    public void goToPlace() {
        placeNavigator.goToPlace(this);
    }
}
