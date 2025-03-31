package com.sap.sailing.gwt.home.shared.places.user.confirmation;

import com.sap.sailing.gwt.ui.client.refresh.ErrorAndBusyClientFactory;
import com.sap.sse.security.ui.client.WithSecurity;

public interface ConfirmationClientFactory extends WithSecurity, ErrorAndBusyClientFactory {
    
    ConfirmationView createConfirmationView();
}
