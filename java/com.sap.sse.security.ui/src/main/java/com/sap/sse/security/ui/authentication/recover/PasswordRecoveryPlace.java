package com.sap.sse.security.ui.authentication.recover;

import com.sap.sse.security.ui.authentication.AbstractAuthenticationPlace;
import com.sap.sse.security.ui.client.i18n.StringMessages;

public class PasswordRecoveryPlace extends AbstractAuthenticationPlace {
    
    @Override
    public String getHeaderText() {
        return StringMessages.INSTANCE.forgotPassword();
    }

}
