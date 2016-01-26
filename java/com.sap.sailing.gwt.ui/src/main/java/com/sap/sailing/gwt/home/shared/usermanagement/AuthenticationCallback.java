package com.sap.sailing.gwt.home.shared.usermanagement;

import com.sap.sse.security.ui.authentication.create.CreateAccountView;
import com.sap.sse.security.ui.authentication.info.LoggedInUserInfoView;
import com.sap.sse.security.ui.authentication.recover.PasswordRecoveryView;

public interface AuthenticationCallback extends CreateAccountView.Presenter.Callback,
        PasswordRecoveryView.Presenter.Callback, LoggedInUserInfoView.Presenter.Callback {
    
    void handleSignInSuccess();
}