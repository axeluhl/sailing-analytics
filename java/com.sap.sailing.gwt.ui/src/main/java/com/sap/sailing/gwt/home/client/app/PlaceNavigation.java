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
    
    public PlaceNavigation(T destinationPlace, PlaceTokenizer<T> tokenizer) {
        this.destinationPlace = destinationPlace;
        this.tokenizer = tokenizer;
        String locationURL = getLocationURL();
        Window.alert(locationURL);
        this.isDestinationOnRemoteServer = !(isLocationOnLocalhost(locationURL) || isLocationOnDefaultSapSailingServer(locationURL));
        if(isDestinationOnRemoteServer) {
            this.baseUrl = AbstractPlaceNavigator.DEFAULT_SAPSAILING_SERVER_URL; 
        } else {
            this.baseUrl = locationURL;
        }
    }

    public PlaceNavigation(String baseUrl, T destinationPlace, PlaceTokenizer<T> tokenizer, boolean isDestinationOnRemoteServer) {
        this.destinationPlace = destinationPlace;
        this.tokenizer = tokenizer;
        this.baseUrl = baseUrl;
        this.isDestinationOnRemoteServer = isDestinationOnRemoteServer;
    }
    
    public void gotoPlace() {
    }

    public String getTargetUrl() {
        return buildPlaceUrl();
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
}
