package com.sap.sailing.gwt.autoplay.client.app;

import static com.sap.sailing.gwt.ui.client.RemoteServiceMappingConstants.mediaServiceRemotePath;
import static com.sap.sailing.gwt.ui.client.RemoteServiceMappingConstants.mediaServiceWriteRemotePath;
import static com.sap.sse.gwt.shared.RpcConstants.HEADER_FORWARD_TO_MASTER;
import static com.sap.sse.gwt.shared.RpcConstants.HEADER_FORWARD_TO_REPLICA;

import java.util.HashMap;
import java.util.Map;

import com.google.gwt.core.client.GWT;
import com.google.gwt.place.shared.Place;
import com.google.gwt.place.shared.PlaceController;
import com.google.gwt.user.client.rpc.ServiceDefTarget;
import com.google.web.bindery.event.shared.EventBus;
import com.sap.sailing.domain.common.security.SecuredDomainType;
import com.sap.sailing.gwt.autoplay.client.places.autoplaystart.AutoPlayStartPlace;
import com.sap.sailing.gwt.common.communication.routing.ProvidesLeaderboardRouting;
import com.sap.sailing.gwt.ui.client.MediaService;
import com.sap.sailing.gwt.ui.client.MediaServiceAsync;
import com.sap.sailing.gwt.ui.client.MediaServiceWrite;
import com.sap.sailing.gwt.ui.client.MediaServiceWriteAsync;
import com.sap.sailing.gwt.ui.client.RemoteServiceMappingConstants;
import com.sap.sailing.gwt.ui.client.SailingServiceAsync;
import com.sap.sailing.gwt.ui.client.SailingServiceHelper;
import com.sap.sailing.gwt.ui.client.SailingServiceWriteAsync;
import com.sap.sse.gwt.client.EntryPointHelper;
import com.sap.sse.security.ui.client.SecureClientFactoryImpl;

public abstract class AutoPlayClientFactoryBase
        extends SecureClientFactoryImpl<ApplicationTopLevelView> implements AutoPlayClientFactory {
    private final SailingServiceAsync sailingService;
    private final SailingServiceWriteAsync sailingServiceWrite;
    private final MediaServiceAsync mediaService;
    private final MediaServiceWriteAsync mediaServiceWrite;
    private final AutoPlayPlaceNavigator navigator;

    private final Map<String, SailingServiceAsync> services = new HashMap<>();
    
    public AutoPlayClientFactoryBase(ApplicationTopLevelView root, EventBus eventBus, PlaceController placeController,
            AutoPlayPlaceNavigator navigator) {
        super(root, eventBus, placeController);
        this.navigator = navigator;
        sailingService = SailingServiceHelper.createSailingServiceInstance();
        sailingServiceWrite = SailingServiceHelper.createSailingServiceWriteInstance();
        mediaService = GWT.create(MediaService.class);
        mediaServiceWrite = GWT.create(MediaServiceWrite.class);
        EntryPointHelper.registerASyncService((ServiceDefTarget) sailingService,
                RemoteServiceMappingConstants.sailingServiceRemotePath);
        EntryPointHelper.registerASyncService((ServiceDefTarget) mediaService, mediaServiceRemotePath,
                HEADER_FORWARD_TO_REPLICA);
        EntryPointHelper.registerASyncService((ServiceDefTarget) mediaServiceWrite, mediaServiceWriteRemotePath,
                HEADER_FORWARD_TO_MASTER);
        getUserService().addKnownHasPermissions(SecuredDomainType.getAllInstances());
    }

    @Override
    public Place getDefaultPlace() {
        return new AutoPlayStartPlace();
    }

    @Override
    public SailingServiceAsync getSailingService() {
        if (isConfigured()) {
            return getSailingService(() -> getAutoPlayCtxSignalError().getContextDefinition().getLeaderboardName());
        } 
        return sailingService;
    }
    
    @Override
    public SailingServiceAsync getSailingService(ProvidesLeaderboardRouting routingProvider) {
        if (routingProvider == null) {
            return sailingService;
        } else {            
            SailingServiceAsync sailingServiceAsync = services.get(routingProvider.routingSuffixPath());
            if (sailingServiceAsync == null) {
                sailingServiceAsync = SailingServiceHelper.createSailingServiceInstance(routingProvider);
                services.put(routingProvider.routingSuffixPath(), sailingServiceAsync);
            }
            return sailingServiceAsync;
        }
    }
  
    @Override
    public SailingServiceWriteAsync getSailingServiceWrite() {
        return sailingServiceWrite;
    }

    @Override
    public MediaServiceAsync getMediaService() {
        return mediaService;
    }
    
    public MediaServiceWriteAsync getMediaServiceWrite() {
        return mediaServiceWrite;
    }

    @Override
    public AutoPlayPlaceNavigator getPlaceNavigator() {
        return navigator;
    }
}
