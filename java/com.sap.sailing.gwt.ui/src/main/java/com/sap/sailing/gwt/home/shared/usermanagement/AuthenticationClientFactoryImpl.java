package com.sap.sailing.gwt.home.shared.usermanagement;

import com.sap.sailing.gwt.home.shared.places.user.confirmation.ConfirmationView;
import com.sap.sailing.gwt.home.shared.places.user.confirmation.ConfirmationViewImpl;
import com.sap.sailing.gwt.home.shared.usermanagement.create.CreateAccountView;
import com.sap.sailing.gwt.home.shared.usermanagement.create.CreateAccountViewImpl;
import com.sap.sailing.gwt.home.shared.usermanagement.info.LoggedInUserInfoView;
import com.sap.sailing.gwt.home.shared.usermanagement.info.LoggedInUserInfoViewImpl;
import com.sap.sailing.gwt.home.shared.usermanagement.recovery.PasswordRecoveryView;
import com.sap.sailing.gwt.home.shared.usermanagement.recovery.PasswordRecoveryViewImpl;
import com.sap.sailing.gwt.home.shared.usermanagement.signin.SignInView;
import com.sap.sailing.gwt.home.shared.usermanagement.signin.SignInViewImpl;
import com.sap.sse.security.ui.client.i18n.StringMessages;
import com.sap.sse.security.ui.client.usermanagement.UserManagementSharedResources;

public class AuthenticationClientFactoryImpl implements AuthenticationClientFactory {
    
    private final UserManagementSharedResources resources;

    public AuthenticationClientFactoryImpl(UserManagementSharedResources resources) {
        this.resources = resources;
    }

    @Override
    public SignInView createSignInView() {
        return new SignInViewImpl();
    }

    @Override
    public CreateAccountView createCreateAccountView() {
        return new CreateAccountViewImpl();
    }

    @Override
    public PasswordRecoveryView createPasswordRecoveryView() {
        return new PasswordRecoveryViewImpl();
    }

    @Override
    public LoggedInUserInfoView createLoggedInUserInfoView() {
        return new LoggedInUserInfoViewImpl();
    }

    @Override
    public ConfirmationView createConfirmationView() {
        return new ConfirmationViewImpl(StringMessages.INSTANCE.accountConfirmation());
    }

}
