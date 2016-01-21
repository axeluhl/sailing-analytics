package com.sap.sailing.gwt.home.shared.usermanagement;

import com.google.gwt.event.shared.EventHandler;
import com.google.gwt.event.shared.GwtEvent;
import com.sap.sailing.gwt.home.shared.usermanagement.app.AuthenticationContext;

public class AuthenticationContextEvent extends GwtEvent<AuthenticationContextEvent.Handler> {
    public static final Type<Handler> TYPE = new Type<AuthenticationContextEvent.Handler>();

    private final AuthenticationContext ctx;

    public interface Handler extends EventHandler {
        void onUserChangeEvent(AuthenticationContextEvent event);
    }

    public AuthenticationContextEvent(AuthenticationContext ctx) {
        this.ctx = ctx;
    }

    public AuthenticationContext getCtx() {
        return ctx;
    }

    @Override
    public Type<Handler> getAssociatedType() {
        return TYPE;
    }

    @Override
    protected void dispatch(Handler handler) {
        handler.onUserChangeEvent(this);
    }
}
