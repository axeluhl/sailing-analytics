package com.sap.sailing.gwt.home.shared.usermanagement;

import com.google.gwt.event.shared.EventHandler;
import com.google.gwt.event.shared.GwtEvent;

public class UserManagementRequestEvent extends GwtEvent<UserManagementRequestEvent.Handler> {
    public static final Type<Handler> TYPE = new Type<UserManagementRequestEvent.Handler>();

    public interface Handler extends EventHandler {
        void onUserManagementRequestEvent(UserManagementRequestEvent event);
    }
    
    private final boolean login;

    public UserManagementRequestEvent() {
        this(true);
    }
    
    public UserManagementRequestEvent(boolean login) {
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
