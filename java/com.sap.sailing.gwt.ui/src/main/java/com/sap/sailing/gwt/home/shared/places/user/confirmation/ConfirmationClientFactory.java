package com.sap.sailing.gwt.home.shared.places.user.confirmation;

import com.sap.sailing.gwt.home.shared.usermanagement.app.UserManagementClientFactory;
import com.sap.sailing.gwt.ui.client.refresh.ErrorAndBusyClientFactory;

public interface ConfirmationClientFactory extends UserManagementClientFactory, ErrorAndBusyClientFactory {
    
    ConfirmationView createAccountConfirmationView(String message);
}
