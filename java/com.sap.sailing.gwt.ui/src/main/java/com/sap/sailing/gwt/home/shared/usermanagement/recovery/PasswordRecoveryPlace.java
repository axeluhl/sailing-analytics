package com.sap.sailing.gwt.home.shared.usermanagement.recovery;

import com.sap.sailing.gwt.home.shared.app.HasMobileVersion;
import com.sap.sailing.gwt.home.shared.usermanagement.AbstractUserManagementPlace;
import com.sap.sse.security.ui.client.i18n.StringMessages;

public class PasswordRecoveryPlace extends AbstractUserManagementPlace implements HasMobileVersion {
    
    @Override
    public String getLocationTitle() {
        return StringMessages.INSTANCE.forgotPassword();
    }

}
