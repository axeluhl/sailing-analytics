package com.sap.sailing.gwt.home.shared.app;

import com.google.gwt.core.client.GWT;
import com.google.gwt.place.shared.Place;
import com.google.gwt.place.shared.PlaceHistoryMapper;
import com.google.gwt.user.client.Window;
import com.sap.sailing.gwt.home.desktop.app.DesktopPlacesNavigator;
import com.sap.sailing.gwt.home.mobile.app.AbstractPlaceNavigator;

public class PlaceNavigation<T extends Place> {
    private final PlaceNavigator placeNavigator;

    private final PlaceHistoryMapper mapper;
    private final T destinationPlace;
    private final String baseUrl;
    private final boolean isDestinationOnRemoteServer;

    public PlaceNavigation(T destinationPlace, PlaceNavigator placeNavigator, PlaceHistoryMapper mapper) {
        this.placeNavigator = placeNavigator;
        this.destinationPlace = destinationPlace;
        this.mapper = mapper;
        String locationURL = getLocationURL();
        this.isDestinationOnRemoteServer = !(isLocationOnLocalhostOrDevServer(locationURL) || isLocationOnDefaultSapSailingServer(locationURL));
        this.baseUrl = isDestinationOnRemoteServer ? AbstractPlaceNavigator.DEFAULT_SAPSAILING_SERVER_URL : locationURL;
    }

    public PlaceNavigation(String baseUrl, T destinationPlace, boolean isDestinationOnRemoteServer,
            PlaceNavigator placeNavigator, PlaceHistoryMapper mapper) {
        this.mapper = mapper;
        this.destinationPlace = destinationPlace;
        this.isDestinationOnRemoteServer = isDestinationOnRemoteServer;
        this.placeNavigator = placeNavigator;
        this.baseUrl = isDestinationOnRemoteServer ? baseUrl : getLocationURL();
    }

    public String getTargetUrl() {
        return buildPlaceUrl();
    }

    public String getHistoryUrl() {
        String placeUrl = buildPlaceUrl();
        if (placeUrl.startsWith("#")) {
            placeUrl = placeUrl.substring(1, placeUrl.length());
        }
        return placeUrl;
    }

    public Place getPlace() {
        return destinationPlace;
    }

    private String buildPlaceUrl() {
        String url = "";
        if (isRemotePlace()) {
            url = baseUrl + "/gwt/Home.html";
            if (!GWT.isProdMode()) {
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
        return "#" + mapper.getToken(destinationPlace);

    }

    private boolean isLocationOnDefaultSapSailingServer(String urlToCheck) {
        return urlToCheck.contains(DesktopPlacesNavigator.DEFAULT_SAPSAILING_SERVER);
    }

    private boolean isLocationOnLocalhostOrDevServer(String urlToCheck) {
        return urlToCheck.contains("localhost") || urlToCheck.contains("127.0.0.1")
                || urlToCheck.contains(DesktopPlacesNavigator.DEFAULT_SAPSAILING_DEV_SERVER);
    }

    private String getLocationURL() {
        return Window.Location.getProtocol() + "//" + Window.Location.getHostName() + ":" + Window.Location.getPort();
    }

    public void goToPlace() {
        placeNavigator.goToPlace(this);
    }
}
