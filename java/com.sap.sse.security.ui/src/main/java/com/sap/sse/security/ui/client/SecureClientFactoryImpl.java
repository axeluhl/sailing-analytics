package com.sap.sse.security.ui.client;

import com.google.gwt.core.client.GWT;
import com.google.gwt.event.shared.SimpleEventBus;
import com.google.gwt.place.shared.PlaceController;
import com.google.gwt.user.client.rpc.ServiceDefTarget;
import com.google.web.bindery.event.shared.EventBus;
import com.sap.sse.gwt.client.EntryPointHelper;
import com.sap.sse.gwt.client.mvp.ClientFactoryImpl;
import com.sap.sse.gwt.client.mvp.TopLevelView;

public abstract class SecureClientFactoryImpl extends ClientFactoryImpl {
    private UserService userService;
    private UserManagementServiceAsync userManagementService;

    public SecureClientFactoryImpl(TopLevelView root) {
        this(root, new SimpleEventBus());
    }
    
    protected SecureClientFactoryImpl(TopLevelView root, EventBus eventBus) {
        this(root, eventBus, new PlaceController(eventBus));
    }
    
    protected SecureClientFactoryImpl(TopLevelView root, EventBus eventBus, PlaceController placeController) {
        super(root, eventBus, placeController);
        
        userManagementService = GWT.create(UserManagementService.class);
        EntryPointHelper.registerASyncService((ServiceDefTarget) userManagementService, com.sap.sse.security.ui.client.RemoteServiceMappingConstants.userManagementServiceRemotePath);
        userService = new UserService(userManagementService);
    }

    public UserManagementServiceAsync getUserManagementService() {
        return userManagementService;
    }
    
    public UserService getUserService() {
        return userService;
    }
}
