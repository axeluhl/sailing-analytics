package com.sap.sailing.gwt.home.shared.places.user.passwordreset;

import com.sap.sailing.gwt.home.shared.app.ClientFactoryWithUserManagementService;
import com.sap.sailing.gwt.home.shared.app.PlaceNavigation;
import com.sap.sailing.gwt.home.shared.places.user.confirmation.ConfirmationPlace;
import com.sap.sailing.gwt.ui.client.refresh.ErrorAndBusyClientFactory;

public interface PasswordResetClientFactory extends ClientFactoryWithUserManagementService, ErrorAndBusyClientFactory {
    PasswordResetView createPasswordResetView();
    PlaceNavigation<ConfirmationPlace> getPasswordResttedConfirmationNavigation(String username);
}
