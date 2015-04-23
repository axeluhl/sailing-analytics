package com.sap.sailing.gwt.home.client.app;

import com.google.gwt.core.client.GWT;
import com.google.gwt.place.shared.Place;
import com.google.gwt.place.shared.PlaceController;
import com.google.web.bindery.event.shared.EventBus;
import com.sap.sailing.gwt.home.client.HomeService;
import com.sap.sailing.gwt.home.client.HomeServiceAsync;
import com.sap.sailing.gwt.home.client.place.start.StartPlace;
import com.sap.sailing.gwt.ui.client.MediaService;
import com.sap.sailing.gwt.ui.client.MediaServiceAsync;
import com.sap.sailing.gwt.ui.client.SailingService;
import com.sap.sailing.gwt.ui.client.SailingServiceAsync;
import com.sap.sse.security.ui.client.SecureClientFactoryImpl;


public abstract class AbstractApplicationClientFactory extends SecureClientFactoryImpl implements ApplicationClientFactory {
    private final SailingServiceAsync sailingService;
    private final HomeServiceAsync homeService;
    private final MediaServiceAsync mediaService;
    private final HomePlacesNavigator navigator;

    public AbstractApplicationClientFactory(ApplicationTopLevelView root, EventBus eventBus, PlaceController placeController) {
        super(root, eventBus, placeController);
        navigator = new HomePlacesNavigator(placeController);
        sailingService = GWT.create(SailingService.class);
        homeService = GWT.create(HomeService.class);
        mediaService = GWT.create(MediaService.class);
    }
    
    @Override
    public Place getDefaultPlace() {
        return new StartPlace();
    }

    @Override
    public SailingServiceAsync getSailingService() {
        return sailingService;
    }

    @Override
    public HomeServiceAsync getHomeService() {
        return homeService;
    }

    @Override
    public MediaServiceAsync getMediaService() {
        return mediaService;
    }

    @Override
    public HomePlacesNavigator getHomePlacesNavigator() {
        return navigator;
    }
}
