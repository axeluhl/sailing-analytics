package com.sap.sailing.gwt.ui.adminconsole;

import com.google.gwt.place.shared.PlaceController;
import com.google.web.bindery.event.shared.EventBus;
import com.google.web.bindery.event.shared.SimpleEventBus;
import com.sap.sse.gwt.client.DefaultErrorReporter;
import com.sap.sse.gwt.client.ErrorReporter;
import com.sap.sse.gwt.client.StringMessages;
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
}
