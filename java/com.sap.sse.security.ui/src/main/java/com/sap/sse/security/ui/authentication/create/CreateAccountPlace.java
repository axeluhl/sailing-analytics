package com.sap.sse.security.ui.authentication.create;

import com.sap.sse.security.ui.authentication.AbstractAuthenticationPlace;
import com.sap.sse.security.ui.client.i18n.StringMessages;

public class CreateAccountPlace extends AbstractAuthenticationPlace {

    @Override
    public String getHeaderText() {
        return StringMessages.INSTANCE.signUp();
    }
    
}
