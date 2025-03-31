package com.sap.sailing.gwt.home.shared.usermanagement;

import com.sap.sailing.gwt.home.shared.app.PlaceNavigation;
import com.sap.sailing.gwt.home.shared.places.user.profile.AbstractUserProfilePlace;
import com.sap.sse.security.ui.authentication.AuthenticationCallback;

public class AuthenticationCallbackImpl implements AuthenticationCallback {

    private final PlaceNavigation<? extends AbstractUserProfilePlace> userProfileNavigation;
    private final Runnable signInSuccessfulNavigation;

    public AuthenticationCallbackImpl(PlaceNavigation<? extends AbstractUserProfilePlace> userProfileNavigation,
            Runnable signInSuccessfulNavigation) {
        this.signInSuccessfulNavigation = signInSuccessfulNavigation;
        this.userProfileNavigation = userProfileNavigation;
    }

    @Override
    public void handleUserProfileNavigation() {
        userProfileNavigation.goToPlace();
    }
    
    @Override
    public void handleSignInSuccess() {
        signInSuccessfulNavigation.run();
    }

}
