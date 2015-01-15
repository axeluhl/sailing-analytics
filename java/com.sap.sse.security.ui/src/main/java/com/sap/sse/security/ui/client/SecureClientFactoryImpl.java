package com.sap.sse.security.ui.client;

import com.google.gwt.event.shared.SimpleEventBus;
import com.google.gwt.place.shared.PlaceController;
import com.google.web.bindery.event.shared.EventBus;
import com.sap.sse.gwt.client.mvp.ClientFactoryImpl;
import com.sap.sse.gwt.client.mvp.TopLevelView;

/**
 * An implementation of a ClientFactory providing security services
 * @author Frank
 *
 */
public abstract class SecureClientFactoryImpl extends ClientFactoryImpl implements WithSecurity {
    private WithSecurity securityProvider;

    public SecureClientFactoryImpl(TopLevelView root) {
        this(root, new SimpleEventBus());
    }
    
    protected SecureClientFactoryImpl(TopLevelView root, EventBus eventBus) {
        this(root, eventBus, new PlaceController(eventBus));
    }
    
    protected SecureClientFactoryImpl(TopLevelView root, EventBus eventBus, PlaceController placeController) {
        super(root, eventBus, placeController);
        
        securityProvider = new DefaultWithSecurityImpl();
    }

    @Override
    public UserManagementServiceAsync getUserManagementService() {
        return securityProvider.getUserManagementService();
    }
    
    @Override
    public UserService getUserService() {
        return securityProvider.getUserService();
    }
}
