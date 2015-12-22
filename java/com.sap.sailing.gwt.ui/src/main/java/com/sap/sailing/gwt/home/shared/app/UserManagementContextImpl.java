package com.sap.sailing.gwt.home.shared.app;

import java.util.ArrayList;

import com.sap.sse.security.ui.shared.AccountDTO;
import com.sap.sse.security.ui.shared.UserDTO;

public class UserManagementContextImpl implements UserManagementContext {

    private final UserDTO currentUser;
    private final static UserDTO ANONYMOUS = new UserDTO("Anonymous", "", false, new ArrayList<AccountDTO>(),
            new ArrayList<String>(), new ArrayList<String>());

    public UserManagementContextImpl() {
        this.currentUser = ANONYMOUS;
    }

    public UserManagementContextImpl(UserDTO currentUser) {
        if (currentUser == null) {
            this.currentUser = ANONYMOUS;
        } else {
            this.currentUser = currentUser;
        }
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
