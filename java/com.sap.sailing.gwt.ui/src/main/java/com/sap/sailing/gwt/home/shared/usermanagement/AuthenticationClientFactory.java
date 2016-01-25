package com.sap.sailing.gwt.home.shared.usermanagement;

import com.sap.sailing.gwt.home.shared.places.user.confirmation.ConfirmationView;
import com.sap.sailing.gwt.home.shared.usermanagement.create.CreateAccountView;
import com.sap.sailing.gwt.home.shared.usermanagement.info.LoggedInUserInfoView;
import com.sap.sailing.gwt.home.shared.usermanagement.recovery.PasswordRecoveryView;
import com.sap.sailing.gwt.home.shared.usermanagement.signin.SignInView;

public interface AuthenticationClientFactory extends WithAuthenticationManager {

    SignInView createSignInView();
    
    CreateAccountView createCreateAccountView();
    
    PasswordRecoveryView createPasswordRecoveryView();
    
    LoggedInUserInfoView createLoggedInUserInfoView();
    
    ConfirmationView createConfirmationView();
    
    // ContextObject
}
