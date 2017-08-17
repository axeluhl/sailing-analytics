package com.sap.sse.security.ui.authentication.generic.sapheader;

import com.google.gwt.user.client.Window;
import com.sap.sse.security.ui.authentication.AuthenticationManager;
import com.sap.sse.security.ui.authentication.generic.GenericAuthenticationLinkFactory;
import com.sap.sse.security.ui.authentication.login.LoginHintPopup;

public class FixedLoginHintPopup extends LoginHintPopup {

    public FixedLoginHintPopup(AuthenticationManager authenticationManager, GenericAuthenticationLinkFactory linkFactory) {
        super(authenticationManager, () -> Window.open(linkFactory.createMoreInfoAboutLoginLink(), "_blank", ""));
        this.addStyleName(SAPHeaderWithAuthenticationResources.INSTANCE.css().usermanagement_view());
        this.addStyleName(SAPHeaderWithAuthenticationResources.INSTANCE.css().fixed());
    }
}
