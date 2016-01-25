package com.sap.sailing.gwt.home.shared.usermanagement;

import com.sap.sailing.gwt.home.shared.usermanagement.confirm.ConfirmationInfoView;
import com.sap.sailing.gwt.home.shared.usermanagement.confirm.ConfirmationInfoViewImpl;
import com.sap.sailing.gwt.home.shared.usermanagement.create.CreateAccountView;
import com.sap.sailing.gwt.home.shared.usermanagement.create.CreateAccountViewImpl;
import com.sap.sailing.gwt.home.shared.usermanagement.info.LoggedInUserInfoView;
import com.sap.sailing.gwt.home.shared.usermanagement.info.LoggedInUserInfoViewImpl;
import com.sap.sailing.gwt.home.shared.usermanagement.recovery.PasswordRecoveryView;
import com.sap.sailing.gwt.home.shared.usermanagement.recovery.PasswordRecoveryViewImpl;
import com.sap.sailing.gwt.home.shared.usermanagement.signin.SignInView;
import com.sap.sailing.gwt.home.shared.usermanagement.signin.SignInViewImpl;
import com.sap.sse.security.ui.client.UserManagementServiceAsync;
import com.sap.sse.security.ui.client.usermanagement.UserManagementSharedResources;

public class AuthenticationClientFactoryImpl implements AuthenticationClientFactory {
    
    private final AuthenticationManager authenticationManager;
    private final UserManagementSharedResources resources;

    public AuthenticationClientFactoryImpl(AuthenticationManager authenticationManager,
            UserManagementSharedResources resources) {
        this.authenticationManager = authenticationManager;
        this.resources = resources;
    }

    @Override
    public SignInView createSignInView() {
        return new SignInViewImpl(resources);
    }

    @Override
    public CreateAccountView createCreateAccountView() {
        return new CreateAccountViewImpl(resources);
    }

    @Override
    public PasswordRecoveryView createPasswordRecoveryView() {
        return new PasswordRecoveryViewImpl(resources);
    }

    @Override
    public LoggedInUserInfoView createLoggedInUserInfoView() {
        return new LoggedInUserInfoViewImpl(resources);
    }

    @Override
    public ConfirmationInfoView createConfirmationInfoView() {
        return new ConfirmationInfoViewImpl(resources);
    }
    
    @Override
    public AuthenticationManager getAuthenticationManager() {
        return authenticationManager;
    }
    
    @Override
    public UserManagementServiceAsync getUserManagementService() {
        return ((AuthenticationManagerImpl) authenticationManager).getUserManagementService();
    }

}
