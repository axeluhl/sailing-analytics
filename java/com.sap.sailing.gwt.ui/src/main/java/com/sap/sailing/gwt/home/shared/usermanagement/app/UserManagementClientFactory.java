package com.sap.sailing.gwt.home.shared.usermanagement.app;

import com.sap.sse.security.ui.client.UserManagementServiceAsync;
import com.sap.sse.security.ui.shared.UserDTO;

public interface UserManagementClientFactory {
    
    UserManagementServiceAsync getUserManagement();
    void didLogin(UserDTO user);
    void didLogout();
}
