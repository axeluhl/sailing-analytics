package com.sap.sailing.gwt.home.shared.usermanagement.recovery;

import com.sap.sailing.gwt.home.shared.usermanagement.AbstractAuthenticationPlace;
import com.sap.sse.security.ui.client.i18n.StringMessages;

public class PasswordRecoveryPlace extends AbstractAuthenticationPlace {
    
    @Override
    public String getLocationTitle() {
        return StringMessages.INSTANCE.forgotPassword();
    }

}
