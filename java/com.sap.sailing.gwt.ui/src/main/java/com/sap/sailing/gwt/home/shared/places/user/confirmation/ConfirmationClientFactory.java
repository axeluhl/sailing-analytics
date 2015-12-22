package com.sap.sailing.gwt.home.shared.places.user.confirmation;

import com.sap.sailing.gwt.home.shared.usermanagement.app.ClientFactoryWithUserManagementService;
import com.sap.sailing.gwt.ui.client.refresh.ErrorAndBusyClientFactory;

public interface ConfirmationClientFactory extends ClientFactoryWithUserManagementService, ErrorAndBusyClientFactory {
    
    ConfirmationView createAccountConfirmationView(String message);
}
