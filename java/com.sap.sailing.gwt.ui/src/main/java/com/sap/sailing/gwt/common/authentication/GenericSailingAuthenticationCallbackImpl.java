package com.sap.sailing.gwt.common.authentication;

import com.google.gwt.user.client.Window;
import com.sap.sse.security.ui.authentication.AuthenticationCallback;
import com.sap.sse.security.ui.authentication.WrappedPlaceManagementController;
import com.sap.sse.security.ui.authentication.info.LoggedInUserInfoPlace;

public class GenericSailingAuthenticationCallbackImpl implements AuthenticationCallback {
    
    private final WrappedPlaceManagementController controller;
    
    public GenericSailingAuthenticationCallbackImpl(WrappedPlaceManagementController controller) {
        this.controller = controller;
    }

    @Override
    public void handleSignInSuccess() {
        controller.goTo(new LoggedInUserInfoPlace());
    }

    @Override
    public void handleUserProfileNavigation() {
        Window.open(SailingAuthenticationEntryPointLinkFactory.createUserProfileLink(), "_blank", "");
    }

}
