package com.sap.sailing.gwt.autoplay.client.app;

import com.google.gwt.core.client.GWT;
import com.google.gwt.place.shared.Place;
import com.google.gwt.place.shared.PlaceController;
import com.google.web.bindery.event.shared.EventBus;
import com.sap.sailing.gwt.autoplay.client.place.start.StartPlace;
import com.sap.sailing.gwt.ui.client.HomeService;
import com.sap.sailing.gwt.ui.client.HomeServiceAsync;
import com.sap.sailing.gwt.ui.client.MediaService;
import com.sap.sailing.gwt.ui.client.MediaServiceAsync;
import com.sap.sailing.gwt.ui.client.SailingService;
import com.sap.sailing.gwt.ui.client.SailingServiceAsync;
import com.sap.sse.security.ui.client.SecureClientFactoryImpl;

public abstract class AbstractApplicationClientFactory extends SecureClientFactoryImpl implements AutoPlayAppClientFactory {
    private final SailingServiceAsync sailingService;
    private final HomeServiceAsync homeService;
    private final MediaServiceAsync mediaService;
    private final PlaceNavigator navigator;

    public AbstractApplicationClientFactory(ApplicationTopLevelView root, EventBus eventBus, PlaceController placeController) {
        super(root, eventBus, placeController);
        navigator = new PlaceNavigatorImpl(placeController);
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
    public PlaceNavigator getPlaceNavigator() {
        return navigator;
    }
}
