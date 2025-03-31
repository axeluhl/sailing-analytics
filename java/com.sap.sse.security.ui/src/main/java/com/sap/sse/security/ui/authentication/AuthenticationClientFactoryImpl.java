package com.sap.sse.security.ui.authentication;

import com.sap.sse.gwt.common.CommonSharedResources;
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

/**
 * Default implementation of {@link AuthenticationClientFactory} using the given {@link AuthenticationManager} and
 * creating view implementations which use the given {@link CommonSharedResources} for styling issues.
 */
public class AuthenticationClientFactoryImpl implements AuthenticationClientFactory {
    
    private final AuthenticationManager authenticationManager;
    private final CommonSharedResources resources;

    /**
     * Creates a new {@link AuthenticationClientFactoryImpl} instance using the given manager and resources.
     * 
     * @param authenticationManager
     *            the {@link AuthenticationManager} to use
     * @param resources
     *            the {@link CommonSharedResources} to use
     */
    public AuthenticationClientFactoryImpl(AuthenticationManager authenticationManager,
            CommonSharedResources resources) {
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
    
}
