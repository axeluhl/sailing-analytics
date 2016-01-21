package com.sap.sailing.gwt.home.shared.usermanagement.app;

import com.sap.sailing.gwt.home.shared.places.user.confirmation.ConfirmationClientFactory;
import com.sap.sailing.gwt.home.shared.usermanagement.WithAuthenticationManager;
import com.sap.sse.gwt.client.mvp.ClientFactory;
import com.sap.sse.security.ui.client.UserManagementServiceAsync;

public interface UserManagementClientFactory extends ClientFactory, ConfirmationClientFactory, WithAuthenticationManager {
    
    UserManagementServiceAsync getUserManagement();
}
