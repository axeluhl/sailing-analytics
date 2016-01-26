package com.sap.sailing.gwt.home.shared.usermanagement;

import com.google.gwt.user.client.Window;
import com.sap.sailing.gwt.home.shared.app.PlaceNavigation;
import com.sap.sailing.gwt.home.shared.places.user.confirmation.ConfirmationPlace;
import com.sap.sailing.gwt.home.shared.places.user.passwordreset.PasswordResetPlace;
import com.sap.sailing.gwt.home.shared.places.user.profile.AbstractUserProfilePlace;
import com.sap.sse.security.ui.authentication.AuthenticationCallback;

public class AuthenticationCallbackImpl implements AuthenticationCallback {

    private final String createConfirmationUrl;
    private final String passwordResetUrl;
    private final PlaceNavigation<? extends AbstractUserProfilePlace> userProfileNavigation;
    private final Runnable signInSuccessfulNavigation;

    public AuthenticationCallbackImpl(PlaceNavigation<ConfirmationPlace> createConfirmationNavigation,
            PlaceNavigation<PasswordResetPlace> passwordResetPlaceNavigation,
            PlaceNavigation<? extends AbstractUserProfilePlace> userProfileNavigation,
            Runnable signInSuccessfulNavigation) {
        this.signInSuccessfulNavigation = signInSuccessfulNavigation;
        this.createConfirmationUrl = createUrl(createConfirmationNavigation);
        this.passwordResetUrl = createUrl(passwordResetPlaceNavigation);
        this.userProfileNavigation = userProfileNavigation;
    }

    private String createUrl(PlaceNavigation<?> placeNavigation) {
        return Window.Location.createUrlBuilder().setHash(placeNavigation.getTargetUrl()).buildString();
    }

    @Override
    public String getCreateConfirmationUrl() {
        return createConfirmationUrl;
    }

    @Override
    public String getPasswordResetUrl() {
        return passwordResetUrl;
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
