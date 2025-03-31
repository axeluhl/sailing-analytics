package com.sap.sse.security.ui.authentication;

import com.google.gwt.event.shared.EventHandler;
import com.google.gwt.event.shared.GwtEvent;

/**
 * Event to be fired to trigger logout. This makes it possible for UI elements to request a logout without further
 * integration with the authentication framework.
 */
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
