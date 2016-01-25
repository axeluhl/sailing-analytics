package com.sap.sailing.gwt.home.shared.usermanagement;

import com.google.gwt.event.shared.EventHandler;
import com.google.gwt.event.shared.GwtEvent;

public class AuthenticationRequestEvent extends GwtEvent<AuthenticationRequestEvent.Handler> {
    public static final Type<Handler> TYPE = new Type<AuthenticationRequestEvent.Handler>();

    public interface Handler extends EventHandler {
        void onUserManagementRequestEvent(AuthenticationRequestEvent event);
    }
    
    private final boolean login;

    public AuthenticationRequestEvent() {
        this(true);
    }
    
    public AuthenticationRequestEvent(boolean login) {
        super();
        this.login = login;
    }
    
    public boolean isLogin() {
        return login;
    }
    
    @Override
    public Type<Handler> getAssociatedType() {
        return TYPE;
    }

    @Override
    protected void dispatch(Handler handler) {
        handler.onUserManagementRequestEvent(this);
    }
}
