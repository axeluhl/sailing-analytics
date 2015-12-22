package com.sap.sailing.gwt.home.shared.usermanagement.app;

import com.sap.sailing.gwt.home.shared.places.user.confirmation.ConfirmationClientFactory;
import com.sap.sse.gwt.client.mvp.ClientFactory;
import com.sap.sse.security.ui.client.UserManagementServiceAsync;
import com.sap.sse.security.ui.shared.UserDTO;

public interface UserManagementClientFactory extends ClientFactory, ClientFactoryWithUserManagementContext, ConfirmationClientFactory {
    
    UserManagementServiceAsync getUserManagement();
    void didLogin(UserDTO user);
    void didLogout();
}
