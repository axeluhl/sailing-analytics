package com.sap.sailing.gwt.home.shared.places.user.passwordreset;

import com.sap.sailing.gwt.home.shared.app.PlaceNavigation;
import com.sap.sailing.gwt.home.shared.places.user.confirmation.ConfirmationPlace;
import com.sap.sailing.gwt.ui.client.refresh.ErrorAndBusyClientFactory;
import com.sap.sse.security.ui.client.WithSecurity;

public interface PasswordResetClientFactory extends WithSecurity, ErrorAndBusyClientFactory {
    
    PasswordResetView createPasswordResetView();
    
    PlaceNavigation<ConfirmationPlace> getPasswordResettedConfirmationNavigation(String username);
}
