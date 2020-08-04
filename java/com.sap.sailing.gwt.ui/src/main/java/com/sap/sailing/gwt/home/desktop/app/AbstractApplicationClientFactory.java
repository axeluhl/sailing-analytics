package com.sap.sailing.gwt.home.desktop.app;

import static com.sap.sailing.gwt.ui.client.RemoteServiceMappingConstants.mediaServiceRemotePath;
import static com.sap.sailing.gwt.ui.client.RemoteServiceMappingConstants.mediaServiceWriteRemotePath;
import static com.sap.sse.gwt.shared.RpcConstants.HEADER_FORWARD_TO_MASTER;
import static com.sap.sse.gwt.shared.RpcConstants.HEADER_FORWARD_TO_REPLICA;

import com.google.gwt.core.client.GWT;
import com.google.gwt.place.shared.Place;
import com.google.gwt.place.shared.PlaceController;
import com.google.gwt.user.client.rpc.ServiceDefTarget;
import com.google.web.bindery.event.shared.EventBus;
import com.sap.sailing.domain.common.security.SecuredDomainType;
import com.sap.sailing.gwt.common.communication.routing.ProvidesLeaderboardRouting;
import com.sap.sailing.gwt.home.shared.places.start.StartPlace;
import com.sap.sailing.gwt.ui.client.MediaService;
import com.sap.sailing.gwt.ui.client.MediaServiceAsync;
import com.sap.sailing.gwt.ui.client.MediaServiceWrite;
import com.sap.sailing.gwt.ui.client.MediaServiceWriteAsync;
import com.sap.sailing.gwt.ui.client.SailingServiceAsync;
import com.sap.sailing.gwt.ui.client.SailingServiceHelper;
import com.sap.sailing.gwt.ui.client.subscription.chargebee.SubscriptionService;
import com.sap.sailing.gwt.ui.client.subscription.chargebee.SubscriptionServiceAsync;
import com.sap.sse.gwt.client.EntryPointHelper;
import com.sap.sse.security.ui.client.SecureClientFactoryImpl;

public abstract class AbstractApplicationClientFactory<ATLV extends ApplicationTopLevelView<?>>
        extends SecureClientFactoryImpl<ATLV> implements DesktopClientFactory {
    private final SailingServiceAsync sailingService;
    private final MediaServiceAsync mediaService;
    private final MediaServiceWriteAsync mediaServiceWrite;
    private final DesktopPlacesNavigator navigator;
    private final SubscriptionServiceAsync subscriptionService;

    public AbstractApplicationClientFactory(ATLV root, EventBus eventBus, PlaceController placeController,
            final DesktopPlacesNavigator navigator) {
        super(root, eventBus, placeController);
        this.navigator = navigator;
        sailingService = SailingServiceHelper.createSailingServiceInstance();
        mediaService = GWT.create(MediaService.class);
        subscriptionService = GWT.create(SubscriptionService.class);
        EntryPointHelper.registerASyncService((ServiceDefTarget) subscriptionService,
                RemoteServiceMappingConstants.subscriptionServiceRemotePath);
        EntryPointHelper.registerASyncService((ServiceDefTarget) mediaService, mediaServiceRemotePath,
                HEADER_FORWARD_TO_REPLICA);
        mediaServiceWrite = GWT.create(MediaServiceWrite.class);
        EntryPointHelper.registerASyncService((ServiceDefTarget) mediaServiceWrite, mediaServiceWriteRemotePath,
                HEADER_FORWARD_TO_MASTER);
        getUserService().addKnownHasPermissions(SecuredDomainType.getAllInstances());
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
    public SailingServiceAsync getSailingService(ProvidesLeaderboardRouting routingProvider) {
        if (routingProvider == null) {
            return sailingService;
        } else {
            return SailingServiceHelper.createSailingServiceInstance(routingProvider);
        }
    }

    @Override
    public MediaServiceAsync getMediaService() {
        return mediaService;
    }

    @Override
    public MediaServiceWriteAsync getMediaServiceWrite() {
        return mediaServiceWrite;
    }

    @Override
    public DesktopPlacesNavigator getHomePlacesNavigator() {
        return navigator;
    }

    @Override
    public SubscriptionServiceAsync getSubscriptionService() {
        return subscriptionService;
    }
}
