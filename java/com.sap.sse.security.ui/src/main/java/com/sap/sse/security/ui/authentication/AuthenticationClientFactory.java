package com.sap.sse.security.ui.authentication;

import com.google.gwt.activity.shared.Activity;
import com.sap.sse.security.ui.authentication.confirm.ConfirmationInfoView;
import com.sap.sse.security.ui.authentication.create.CreateAccountView;
import com.sap.sse.security.ui.authentication.info.LoggedInUserInfoView;
import com.sap.sse.security.ui.authentication.recover.PasswordRecoveryView;
import com.sap.sse.security.ui.authentication.signin.SignInView;

/**
 * Client factory interface for {@link Activity activities} within the authentication management providing access to the
 * {@link AuthenticationManager} and factory methods for creation of the various views.
 * 
 * @see WithAuthenticationManager
 */
public interface AuthenticationClientFactory extends WithAuthenticationManager {
    
    /**
     * @return a {@link SignInView} instance
     */
    SignInView createSignInView();
    
    /**
     * @return a {@link CreateAccountView} instance
     */
    CreateAccountView createCreateAccountView();
    
    /**
     * @return a {@link PasswordRecoveryView} instance
     */
    PasswordRecoveryView createPasswordRecoveryView();
    
    /**
     * @return a {@link LoggedInUserInfoView} instance
     */
    LoggedInUserInfoView createLoggedInUserInfoView();
    
    /**
     * @return a {@link ConfirmationInfoView} instance
     */
    ConfirmationInfoView createConfirmationInfoView();
    
}
