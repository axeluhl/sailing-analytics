package com.sap.sailing.gwt.home.shared.places.user;

import com.sap.sailing.gwt.home.shared.places.user.confirmation.ConfirmationClientFactory;
import com.sap.sse.gwt.client.mvp.ClientFactory;
import com.sap.sse.security.ui.client.UserManagementServiceAsync;

public interface UserManagementClientFactory extends ClientFactory, ConfirmationClientFactory {
    
    UserManagementServiceAsync getUserManagement();
}
