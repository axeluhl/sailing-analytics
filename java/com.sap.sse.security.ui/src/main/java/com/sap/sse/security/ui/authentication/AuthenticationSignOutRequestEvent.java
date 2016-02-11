package com.sap.sse.security.ui.authentication;

import com.google.gwt.event.shared.EventHandler;
import com.google.gwt.event.shared.GwtEvent;

public class AuthenticationSignOutRequestEvent extends GwtEvent<AuthenticationSignOutRequestEvent.Handler> {
    public static final Type<Handler> TYPE = new Type<AuthenticationSignOutRequestEvent.Handler>();

    public interface Handler extends EventHandler {
        void onUserManagementSignOutRequestEvent(AuthenticationSignOutRequestEvent event);
    }
    

    @Override
    public Type<Handler> getAssociatedType() {
        return TYPE;
    }

    @Override
    protected void dispatch(Handler handler) {
        handler.onUserManagementSignOutRequestEvent(this);
    }
}
