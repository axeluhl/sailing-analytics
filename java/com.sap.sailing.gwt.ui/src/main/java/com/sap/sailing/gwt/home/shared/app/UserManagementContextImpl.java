package com.sap.sailing.gwt.home.shared.app;

import com.sap.sse.security.ui.shared.UserDTO;

public class UserManagementContextImpl implements UserManagementContext {

    private final UserDTO currentUser;

    public UserManagementContextImpl() {
        this.currentUser = null;
    }

    public UserManagementContextImpl(UserDTO currentUser) {
        this.currentUser = currentUser;
    }

    @Override
    public boolean isLoggedIn() {
        return (currentUser != null);
    }

    @Override
    public UserDTO getCurrentUser() {
        return currentUser;
    }

}
