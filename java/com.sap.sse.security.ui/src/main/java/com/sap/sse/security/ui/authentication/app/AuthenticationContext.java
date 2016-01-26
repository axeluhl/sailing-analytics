package com.sap.sse.security.ui.authentication.app;

import com.sap.sse.security.ui.shared.UserDTO;

public interface AuthenticationContext {
    
    boolean isLoggedIn();

    UserDTO getCurrentUser();
    
    String getUserTitle();
    
    String getUserSubtitle();
}
