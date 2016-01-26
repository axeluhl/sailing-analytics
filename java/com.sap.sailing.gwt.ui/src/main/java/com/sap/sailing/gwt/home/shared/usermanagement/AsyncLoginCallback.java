package com.sap.sailing.gwt.home.shared.usermanagement;

import com.google.gwt.user.client.rpc.AsyncCallback;
import com.sap.sailing.gwt.home.shared.usermanagement.AuthenticationPlaceManagementController.Callback;
import com.sap.sse.security.ui.authentication.AuthenticationManager;
import com.sap.sse.security.ui.client.i18n.StringMessages;
import com.sap.sse.security.ui.shared.SuccessInfo;

public class AsyncLoginCallback implements AsyncCallback<SuccessInfo> {
    
    private final AuthenticationManager authenticationManager;
    private final ErrorMessageView view;
    private final Callback callback;
    private final boolean fireSignInSuccessfulEvent;
    
    public AsyncLoginCallback(AuthenticationManager authenticationManager, ErrorMessageView view,
            AuthenticationPlaceManagementController.Callback callback, boolean fireSignInSuccessfulEvent) {
        this.authenticationManager = authenticationManager;
        this.view = view;
        this.callback = callback;
        this.fireSignInSuccessfulEvent = fireSignInSuccessfulEvent;
    }

    @Override
    public void onSuccess(SuccessInfo result) {
        if (result.isSuccessful()) {
            authenticationManager.didLogin(result.getUserDTO());
            if (fireSignInSuccessfulEvent) {
                callback.handleSignInSuccess();
            }
        } else {
            if (SuccessInfo.FAILED_TO_LOGIN.equals(result.getMessage())) {
                view.setErrorMessage(StringMessages.INSTANCE.failedToSignIn());
            } else {
                view.setErrorMessage(result.getMessage());
            }
        }
    }
    
    @Override
    public void onFailure(Throwable caught) {
        view.setErrorMessage(StringMessages.INSTANCE.failedToSignIn());
    }

}
