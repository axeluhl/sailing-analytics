package com.sap.sailing.gwt.common.authentication;

import com.google.web.bindery.event.shared.HandlerRegistration;
import com.sap.sse.security.ui.authentication.AuthenticationContextEvent;
import com.sap.sse.security.ui.authentication.decorator.FlyoutBasedAuthorizedContentDecorator;

public class GenericAuthorizedContentDecorator extends FlyoutBasedAuthorizedContentDecorator {

    private final GenericSailingAuthentication genericSailingAuthentication;
    private HandlerRegistration handlerRegistration;

    public GenericAuthorizedContentDecorator(GenericSailingAuthentication genericSailingAuthentication) {
        super(genericSailingAuthentication.getEventBus(), new GenericNotLoggedInView());
        this.genericSailingAuthentication = genericSailingAuthentication;
        
    }
    
    @Override
    protected void onLoad() {
        super.onLoad();
        setUserManagementContext(genericSailingAuthentication.getAuthenticationManager().getAuthenticationContext());
        handlerRegistration = genericSailingAuthentication.getEventBus().addHandler(AuthenticationContextEvent.TYPE, new AuthenticationContextEvent.Handler() {
            @Override
            public void onUserChangeEvent(AuthenticationContextEvent event) {
                setUserManagementContext(event.getCtx());
            }
        });
    }

    @Override
    protected void onUnload() {
        super.onUnload();
        handlerRegistration.removeHandler();
        handlerRegistration = null;
    }
}
