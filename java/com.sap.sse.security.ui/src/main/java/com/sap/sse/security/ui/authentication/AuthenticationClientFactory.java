package com.sap.sse.security.ui.authentication;

import com.sap.sse.security.ui.authentication.confirm.ConfirmationInfoView;
import com.sap.sse.security.ui.authentication.create.CreateAccountView;
import com.sap.sse.security.ui.authentication.info.LoggedInUserInfoView;
import com.sap.sse.security.ui.authentication.recover.PasswordRecoveryView;
import com.sap.sse.security.ui.authentication.signin.SignInView;
import com.sap.sse.security.ui.client.UserManagementServiceAsync;

public interface AuthenticationClientFactory extends WithAuthenticationManager {
    
    UserManagementServiceAsync getUserManagementService();

    SignInView createSignInView();
    
    CreateAccountView createCreateAccountView();
    
    PasswordRecoveryView createPasswordRecoveryView();
    
    LoggedInUserInfoView createLoggedInUserInfoView();
    
    ConfirmationInfoView createConfirmationInfoView();
    
    // ContextObject
}
