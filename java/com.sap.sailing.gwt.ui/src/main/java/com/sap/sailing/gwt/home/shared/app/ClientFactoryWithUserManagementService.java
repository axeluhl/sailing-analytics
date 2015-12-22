package com.sap.sailing.gwt.home.shared.app;

import com.sap.sse.security.ui.client.UserManagementServiceAsync;
import com.sap.sse.security.ui.shared.UserDTO;

public interface ClientFactoryWithUserManagementService {
    UserManagementServiceAsync getUserManagement();
    void didLogin(UserDTO user);
}
