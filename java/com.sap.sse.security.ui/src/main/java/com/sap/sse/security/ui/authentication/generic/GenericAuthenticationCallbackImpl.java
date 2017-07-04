package com.sap.sse.security.ui.authentication.generic;

import com.google.gwt.user.client.Window;
import com.sap.sse.security.ui.authentication.AuthenticationCallback;
import com.sap.sse.security.ui.authentication.WrappedPlaceManagementController;
import com.sap.sse.security.ui.authentication.info.LoggedInUserInfoPlace;

/**
 * Implementation of {@link AuthenticationCallback} to be used with {@link GenericAuthentication}.
 *
 * This implementation ensures that the logged in Info is shown inline in the flyout while the user profile page is
 * opened in a new tab/window using the link provided by the {@link GenericAuthenticationLinkFactory} given to the
 * constructor.
 */
class GenericAuthenticationCallbackImpl implements AuthenticationCallback {
    
    private final GenericAuthenticationLinkFactory linkFactory;
    private WrappedPlaceManagementController controller;
    
    GenericAuthenticationCallbackImpl(GenericAuthenticationLinkFactory linkFactory) {
        this.linkFactory = linkFactory;
    }

    public void setController(WrappedPlaceManagementController controller) {
        this.controller = controller;
    }
    
    @Override
    public void handleSignInSuccess() {
        controller.goTo(new LoggedInUserInfoPlace());
    }

    @Override
    public void handleUserProfileNavigation() {
        Window.open(linkFactory.createUserProfileLink(), "_blank", "");
    }

}
