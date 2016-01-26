package com.sap.sse.security.ui.authentication;

import com.sap.sse.security.ui.authentication.app.AuthenticationContext;
import com.sap.sse.security.ui.shared.UserDTO;

public interface AuthenticationManager {
    
    AuthenticationContext getAuthenticationContext();
    void didLogin(UserDTO user);
    void didLogout();
    void refreshUser();
}
