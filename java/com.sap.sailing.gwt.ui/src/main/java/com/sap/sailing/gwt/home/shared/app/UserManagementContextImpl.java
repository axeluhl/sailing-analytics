package com.sap.sailing.gwt.home.shared.app;

import com.sap.sse.security.ui.shared.UserDTO;

public class UserManagementContextImpl implements UserManagementContext {

    private final UserDTO currentUser;
    private final static UserDTO ANONYMOUS = new UserDTO("Anonymous", "", false, null, null, null);

    public UserManagementContextImpl() {
        this.currentUser = ANONYMOUS;
    }

    public UserManagementContextImpl(UserDTO currentUser) {
        this.currentUser = currentUser;
    }

    @Override
    public boolean isLoggedIn() {
        return (currentUser != ANONYMOUS);
    }

    @Override
    public UserDTO getCurrentUser() {
        return currentUser;
    }

}
