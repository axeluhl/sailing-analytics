package com.sap.sailing.gwt.home.shared.places.user.passwordreset;

import com.sap.sailing.gwt.home.shared.app.PlaceNavigation;
import com.sap.sailing.gwt.home.shared.places.user.confirmation.ConfirmationPlace;
import com.sap.sailing.gwt.home.shared.usermanagement.app.UserManagementClientFactory;
import com.sap.sailing.gwt.ui.client.refresh.ErrorAndBusyClientFactory;

public interface PasswordResetClientFactory extends UserManagementClientFactory, ErrorAndBusyClientFactory {
    PasswordResetView createPasswordResetView();
    PlaceNavigation<ConfirmationPlace> getPasswordResettedConfirmationNavigation(String username);
}
