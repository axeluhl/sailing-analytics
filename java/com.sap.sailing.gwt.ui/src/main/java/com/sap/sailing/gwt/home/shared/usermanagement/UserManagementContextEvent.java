package com.sap.sailing.gwt.home.shared.usermanagement;

import com.google.gwt.event.shared.EventHandler;
import com.google.gwt.event.shared.GwtEvent;
import com.sap.sailing.gwt.home.shared.app.UserManagementContext;

public class UserManagementContextEvent extends GwtEvent<UserManagementContextEvent.Handler> {
    public static final Type<Handler> TYPE = new Type<UserManagementContextEvent.Handler>();

    private final UserManagementContext ctx;

    public interface Handler extends EventHandler {
        void onUserChangeEvent(UserManagementContextEvent event);
    }

    public UserManagementContextEvent(UserManagementContext ctx) {
        this.ctx = ctx;
    }

    public UserManagementContext getCtx() {
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
