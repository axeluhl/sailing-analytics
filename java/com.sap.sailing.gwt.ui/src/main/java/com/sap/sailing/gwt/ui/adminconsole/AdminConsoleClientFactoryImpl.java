package com.sap.sailing.gwt.ui.adminconsole;

import static com.sap.sse.common.HttpRequestHeaderConstants.HEADER_FORWARD_TO_MASTER;

import com.google.gwt.core.client.GWT;
import com.google.gwt.place.shared.PlaceController;
import com.google.gwt.user.client.rpc.ServiceDefTarget;
import com.google.gwt.user.client.ui.AcceptsOneWidget;
import com.google.gwt.user.client.ui.Widget;
import com.google.web.bindery.event.shared.EventBus;
import com.google.web.bindery.event.shared.SimpleEventBus;
import com.sap.sailing.gwt.ui.adminconsole.places.AdminConsoleTopLevelView;
import com.sap.sailing.gwt.ui.client.MediaServiceWrite;
import com.sap.sailing.gwt.ui.client.MediaServiceWriteAsync;
import com.sap.sailing.gwt.ui.client.RemoteServiceMappingConstants;
import com.sap.sailing.gwt.ui.client.SailingServiceWriteAsync;
import com.sap.sse.gwt.client.DefaultErrorReporter;
import com.sap.sse.gwt.client.EntryPointHelper;
import com.sap.sse.gwt.client.ErrorReporter;
import com.sap.sse.gwt.client.StringMessages;
import com.sap.sse.gwt.client.mvp.TopLevelView;
import com.sap.sse.security.ui.client.DefaultWithSecurityImpl;
import com.sap.sse.security.ui.client.UserManagementServiceAsync;
import com.sap.sse.security.ui.client.UserManagementWriteServiceAsync;
import com.sap.sse.security.ui.client.UserService;
import com.sap.sse.security.ui.client.WithSecurity;

public class AdminConsoleClientFactoryImpl implements AdminConsoleClientFactory {

    private final WithSecurity securityProvider = new DefaultWithSecurityImpl();
    private final EventBus eventBus = new SimpleEventBus();
    private final PlaceController placeController = new PlaceController(eventBus);
    private final ErrorReporter errorReporter = new DefaultErrorReporter<StringMessages>(StringMessages.INSTANCE);
    private final MediaServiceWriteAsync mediaServiceWrite = GWT.create(MediaServiceWrite.class);
    private SailingServiceWriteAsync sailingService;
    private TopLevelView topLevelView;
    
    public AdminConsoleClientFactoryImpl() {
    }
    
    public AdminConsoleClientFactoryImpl(final SailingServiceWriteAsync sailingService) {
        this.sailingService = sailingService;
        EntryPointHelper.registerASyncService((ServiceDefTarget) mediaServiceWrite, RemoteServiceMappingConstants.mediaServiceRemotePath, HEADER_FORWARD_TO_MASTER);
        topLevelView = new AdminConsoleTopLevelView(eventBus);
    }
    
    @Override
    public EventBus getEventBus() {
        return eventBus;
    }

    @Override
    public PlaceController getPlaceController() {
        return placeController;
    }

    @Override
    public ErrorReporter getErrorReporter() {
        return errorReporter;
    }
    
    @Override
    public UserManagementServiceAsync getUserManagementService() {
        return securityProvider.getUserManagementService();
    }

    @Override
    public UserManagementWriteServiceAsync getUserManagementWriteService() {
        return securityProvider.getUserManagementWriteService();
    }

    @Override
    public UserService getUserService() {
        return securityProvider.getUserService();
    }

    @Override
    public SailingServiceWriteAsync getSailingService() {
        return sailingService;
    }

    @Override
    public MediaServiceWriteAsync getMediaServiceWrite() {
        return mediaServiceWrite;
    }
    
    @Override
    public TopLevelView getTopLevelView() {
        return topLevelView;
    }

    @Override
    public Widget getRoot() {
        return topLevelView.asWidget();
    }
    
    @Override
    public AcceptsOneWidget getContent() {
        return topLevelView.getContent();
    }
}
