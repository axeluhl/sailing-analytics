package com.sap.sse.security.ui.authentication.signin;

import com.sap.sse.security.ui.authentication.AbstractAuthenticationPlace;
import com.sap.sse.security.ui.client.i18n.StringMessages;

public class SignInPlace extends AbstractAuthenticationPlace {
    
    @Override
    public String getHeaderText() {
        return StringMessages.INSTANCE.signIn();
    }
    
}
