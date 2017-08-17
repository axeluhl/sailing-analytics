package com.sap.sse.security.ui.authentication.generic.sapheader;

import com.sap.sse.security.ui.authentication.AuthenticationManager;
import com.sap.sse.security.ui.authentication.generic.GenericAuthenticationLinkFactory;

public class FixedLoginHintPopup extends GenericLoginHintPopup {

    public FixedLoginHintPopup(AuthenticationManager authenticationManager, GenericAuthenticationLinkFactory linkFactory) {
        super(authenticationManager, linkFactory);
        this.addStyleName(SAPHeaderWithAuthenticationResources.INSTANCE.css().fixed());
    }
}
