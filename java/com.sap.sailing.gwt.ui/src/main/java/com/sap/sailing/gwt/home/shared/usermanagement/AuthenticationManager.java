package com.sap.sailing.gwt.home.shared.usermanagement;

import com.sap.sailing.gwt.home.shared.usermanagement.app.UserManagementContext;
import com.sap.sse.security.ui.shared.UserDTO;

public interface AuthenticationManager {
    
    UserManagementContext getAuthenticationContext();
    void didLogin(UserDTO user);
    void didLogout();
    void refreshUser();
}
