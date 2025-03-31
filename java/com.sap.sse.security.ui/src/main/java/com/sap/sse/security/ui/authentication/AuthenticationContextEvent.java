package com.sap.sse.security.ui.authentication;

import com.google.gwt.event.shared.EventHandler;
import com.google.gwt.event.shared.GwtEvent;
import com.sap.sse.security.ui.authentication.app.AuthenticationContext;

/**
 * Event which is fired if the {@link AuthenticationContext} changes, usually after user login or logout.  
 */
public class AuthenticationContextEvent extends GwtEvent<AuthenticationContextEvent.Handler> {
    public static final Type<Handler> TYPE = new Type<AuthenticationContextEvent.Handler>();

    private final AuthenticationContext ctx;

    /**
     * Handler for {@link AuthenticationContextEvent}s.
     */
    public interface Handler extends EventHandler {
        /**
         * Called when a {@link AuthenticationContextEvent} is fired.
         * 
         * @param event
         *            the {@link AuthenticationContextEvent} that was fired
         */
        void onUserChangeEvent(AuthenticationContextEvent event);
    }

    /**
     * Creates a new {@link AuthenticationContextEvent} containing the given {@link AuthenticationContext}.
     * 
     * @param ctx
     *            the new {@link AuthenticationContext}
     */
    public AuthenticationContextEvent(AuthenticationContext ctx) {
        this.ctx = ctx;
    }

    /**
     * Getter for the contained {@link AuthenticationContext}.
     * 
     * @return the current {@link AuthenticationContext} instance
     */
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
