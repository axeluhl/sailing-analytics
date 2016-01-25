package com.sap.sailing.gwt.home.shared.usermanagement;

import com.sap.sailing.gwt.home.shared.usermanagement.confirm.ConfirmationInfoView;
import com.sap.sailing.gwt.home.shared.usermanagement.create.CreateAccountView;
import com.sap.sailing.gwt.home.shared.usermanagement.info.LoggedInUserInfoView;
import com.sap.sailing.gwt.home.shared.usermanagement.recovery.PasswordRecoveryView;
import com.sap.sailing.gwt.home.shared.usermanagement.signin.SignInView;
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
