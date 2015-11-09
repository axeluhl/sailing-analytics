package com.sap.sailing.gwt.autoplay.client.app;

import com.google.gwt.core.client.GWT;
import com.google.gwt.place.shared.Place;
import com.google.gwt.place.shared.PlaceController;
import com.google.gwt.user.client.rpc.ServiceDefTarget;
import com.google.web.bindery.event.shared.EventBus;
import com.sap.sailing.gwt.autoplay.client.place.start.StartPlace;
import com.sap.sailing.gwt.ui.client.MediaService;
import com.sap.sailing.gwt.ui.client.MediaServiceAsync;
import com.sap.sailing.gwt.ui.client.RemoteServiceMappingConstants;
import com.sap.sailing.gwt.ui.client.SailingService;
import com.sap.sailing.gwt.ui.client.SailingServiceAsync;
import com.sap.sse.gwt.client.EntryPointHelper;
import com.sap.sse.security.ui.client.SecureClientFactoryImpl;

public abstract class AbstractApplicationClientFactory extends SecureClientFactoryImpl<ApplicationTopLevelView> implements AutoPlayAppClientFactory {
    private final SailingServiceAsync sailingService;
    private final MediaServiceAsync mediaService;
    private final PlaceNavigator navigator;

    public AbstractApplicationClientFactory(ApplicationTopLevelView root, EventBus eventBus, PlaceController placeController, PlaceNavigator navigator) {
        super(root, eventBus, placeController);
        this.navigator = navigator;
        sailingService = GWT.create(SailingService.class);
        mediaService = GWT.create(MediaService.class);
        EntryPointHelper.registerASyncService((ServiceDefTarget) sailingService, RemoteServiceMappingConstants.sailingServiceRemotePath);
        EntryPointHelper.registerASyncService((ServiceDefTarget) mediaService, RemoteServiceMappingConstants.mediaServiceRemotePath);
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
    public MediaServiceAsync getMediaService() {
        return mediaService;
    }

    @Override
    public PlaceNavigator getPlaceNavigator() {
        return navigator;
    }
}
