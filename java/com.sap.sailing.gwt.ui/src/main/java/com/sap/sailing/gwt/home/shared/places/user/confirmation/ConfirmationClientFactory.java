package com.sap.sailing.gwt.home.shared.places.user.confirmation;

import com.sap.sailing.gwt.ui.client.refresh.ErrorAndBusyClientFactory;

public interface ConfirmationClientFactory extends ErrorAndBusyClientFactory {
    
    ConfirmationView createAccountConfirmationView();
}
