package com.sap.sailing.gwt.home.shared.app;

import com.sap.sse.security.ui.shared.UserDTO;

public interface UserManagementContext {
    boolean isLoggedIn();

    UserDTO getCurrentUser();
}
