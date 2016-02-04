package com.sap.sailing.gwt.common.authentication;

import com.google.gwt.user.client.Window;
import com.sap.sse.security.ui.authentication.AuthenticationCallback;
import com.sap.sse.security.ui.authentication.WrappedPlaceManagementController;
import com.sap.sse.security.ui.authentication.info.LoggedInUserInfoPlace;

class GenericSailingAuthenticationCallbackImpl implements AuthenticationCallback {
    
    private WrappedPlaceManagementController controller;
    
    public void setController(WrappedPlaceManagementController controller) {
        this.controller = controller;
    }
    
    @Override
    public void handleSignInSuccess() {
        controller.goTo(new LoggedInUserInfoPlace());
    }

    @Override
    public void handleUserProfileNavigation() {
        Window.open(SailingAuthenticationEntryPointLinkFactory.INSTANCE.createUserProfileLink(), "_blank", "");
    }

}
