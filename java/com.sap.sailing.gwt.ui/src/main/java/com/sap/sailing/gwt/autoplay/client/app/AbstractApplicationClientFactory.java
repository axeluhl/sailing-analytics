package com.sap.sailing.gwt.autoplay.client.app;

import com.google.gwt.core.client.GWT;
import com.google.gwt.place.shared.Place;
import com.google.gwt.place.shared.PlaceController;
import com.google.gwt.user.client.rpc.ServiceDefTarget;
import com.google.web.bindery.event.shared.EventBus;
import com.sap.sailing.gwt.autoplay.client.place.start.StartPlace;
import com.sap.sailing.gwt.ui.client.MediaService;
import com.sap.sailing.gwt.ui.client.MediaServiceAsync;
import com.sap.sailing.gwt.ui.client.SailingService;
import com.sap.sailing.gwt.ui.client.SailingServiceAsync;
import com.sap.sse.gwt.client.EntryPointHelper;
import com.sap.sse.gwt.client.mvp.ClientFactoryImpl;
import com.sap.sse.security.ui.client.UserManagementService;
import com.sap.sse.security.ui.client.UserManagementServiceAsync;
import com.sap.sse.security.ui.client.UserService;


public abstract class AbstractApplicationClientFactory extends ClientFactoryImpl implements AutoPlayAppClientFactory {
    private final SailingServiceAsync sailingService;
    private final MediaServiceAsync mediaService;
    private final UserManagementServiceAsync userManagementService;
    private final UserService userService;
    private final PlaceNavigator navigator;

    public AbstractApplicationClientFactory(ApplicationTopLevelView root, EventBus eventBus, PlaceController placeController) {
        super(root, eventBus, placeController);
        navigator = new PlaceNavigatorImpl(placeController);
        sailingService = GWT.create(SailingService.class);
        mediaService = GWT.create(MediaService.class);
        userManagementService = GWT.create(UserManagementService.class);
        EntryPointHelper.registerASyncService((ServiceDefTarget) getUserManagementService(),
                com.sap.sse.security.ui.client.RemoteServiceMappingConstants.userManagementServiceRemotePath);
        userService = new UserService(userManagementService);
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

    @Override
    public UserManagementServiceAsync getUserManagementService() {
        return userManagementService;
    }

    @Override
    public UserService getUserService() {
        return userService;
    }
    
}
