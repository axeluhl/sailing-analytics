package com.sap.sailing.gwt.home.shared.usermanagement;

import com.google.gwt.event.shared.EventHandler;
import com.google.gwt.event.shared.GwtEvent;
import com.sap.sse.security.ui.shared.UserDTO;

public class UserChangeEvent extends GwtEvent<UserChangeEvent.Handler> {
    public static final Type<Handler> TYPE = new Type<UserChangeEvent.Handler>();

    private final UserDTO currentUser;

    public interface Handler extends EventHandler {
        void onUserChangeEvent(UserChangeEvent event);
    }

    public UserChangeEvent(UserDTO currentUser) {
        this.currentUser = currentUser;
    }

    public UserDTO getCurrentUser() {
        return currentUser;
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
