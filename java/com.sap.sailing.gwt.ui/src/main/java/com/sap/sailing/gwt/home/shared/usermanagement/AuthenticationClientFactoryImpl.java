package com.sap.sailing.gwt.home.shared.usermanagement;

import com.sap.sse.security.ui.authentication.AuthenticationClientFactory;
import com.sap.sse.security.ui.authentication.AuthenticationManager;
import com.sap.sse.security.ui.authentication.AuthenticationManagerImpl;
import com.sap.sse.security.ui.authentication.UserManagementSharedResources;
import com.sap.sse.security.ui.authentication.confirm.ConfirmationInfoView;
import com.sap.sse.security.ui.authentication.confirm.ConfirmationInfoViewImpl;
import com.sap.sse.security.ui.authentication.create.CreateAccountView;
import com.sap.sse.security.ui.authentication.create.CreateAccountViewImpl;
import com.sap.sse.security.ui.authentication.info.LoggedInUserInfoView;
import com.sap.sse.security.ui.authentication.info.LoggedInUserInfoViewImpl;
import com.sap.sse.security.ui.authentication.recover.PasswordRecoveryView;
import com.sap.sse.security.ui.authentication.recover.PasswordRecoveryViewImpl;
import com.sap.sse.security.ui.authentication.signin.SignInView;
import com.sap.sse.security.ui.authentication.signin.SignInViewImpl;
import com.sap.sse.security.ui.client.UserManagementServiceAsync;

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
