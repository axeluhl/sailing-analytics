package com.sap.sailing.gwt.home.mobile.app;

import com.google.gwt.core.client.GWT;
import com.google.gwt.place.shared.Place;
import com.google.gwt.place.shared.PlaceController;
import com.sap.sailing.gwt.ui.client.HomeService;
import com.sap.sailing.gwt.ui.client.HomeServiceAsync;
import com.sap.sailing.gwt.ui.client.MediaService;
import com.sap.sailing.gwt.ui.client.MediaServiceAsync;

/**
 * Initial very simple client factory
 * 
 * @author pgtaboada
 *
 */
public class ApplicationContext {
    private final HomeServiceAsync homeService;
    private final MediaServiceAsync mediaService;
    private final PlaceController placeController;

    public ApplicationContext(PlaceController placeController) {
        this.placeController = placeController;
        this.homeService = GWT.create(HomeService.class);
        this.mediaService = GWT.create(MediaService.class);
    }

    public <T extends Place> void goToPlace(T place) {
        placeController.goTo(place);
    }

    public HomeServiceAsync getHomeService() {
        return homeService;
    }

    public MediaServiceAsync getMediaService() {
        return mediaService;
    }

}
