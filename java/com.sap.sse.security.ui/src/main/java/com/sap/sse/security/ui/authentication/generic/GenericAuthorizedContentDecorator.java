package com.sap.sse.security.ui.authentication.generic;

import com.google.web.bindery.event.shared.HandlerRegistration;
import com.sap.sse.security.ui.authentication.AuthenticationContextEvent;
import com.sap.sse.security.ui.authentication.decorator.AuthorizedContentDecorator;
import com.sap.sse.security.ui.authentication.decorator.FlyoutBasedAuthorizedContentDecorator;

/**
 * {@link AuthorizedContentDecorator} that integrates with {@link GenericAuthentication}.
 */
public class GenericAuthorizedContentDecorator extends FlyoutBasedAuthorizedContentDecorator {

    private final GenericAuthentication genericAuthentication;
    private HandlerRegistration handlerRegistration;

    public GenericAuthorizedContentDecorator(GenericAuthentication genericAuthentication) {
        super(genericAuthentication.getEventBus(), new GenericNotLoggedInView());
        this.genericAuthentication = genericAuthentication;
        
    }
    
    @Override
    protected void onLoad() {
        super.onLoad();
        setAuthenticationContext(genericAuthentication.getAuthenticationManager().getAuthenticationContext());
        handlerRegistration = genericAuthentication.getEventBus().addHandler(AuthenticationContextEvent.TYPE, 
                new AuthenticationContextEvent.Handler() {
            @Override
            public void onUserChangeEvent(AuthenticationContextEvent event) {
                setAuthenticationContext(event.getCtx());
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
