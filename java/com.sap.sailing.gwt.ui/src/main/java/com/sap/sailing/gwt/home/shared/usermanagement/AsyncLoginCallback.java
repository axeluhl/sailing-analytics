package com.sap.sailing.gwt.home.shared.usermanagement;

import com.google.gwt.user.client.rpc.AsyncCallback;
import com.sap.sailing.gwt.home.shared.usermanagement.UserManagementPlaceManagementController.Callback;
import com.sap.sailing.gwt.home.shared.usermanagement.app.UserManagementClientFactory;
import com.sap.sse.security.ui.client.i18n.StringMessages;
import com.sap.sse.security.ui.shared.SuccessInfo;

public class AsyncLoginCallback implements AsyncCallback<SuccessInfo> {
    
    private final UserManagementClientFactory clientFactory;
    private final ErrorMessageView view;
    private final Callback callback;
    private final boolean fireSignInSuccessfulEvent;
    
    public AsyncLoginCallback(UserManagementClientFactory clientFactory, ErrorMessageView view,
            UserManagementPlaceManagementController.Callback callback, boolean fireSignInSuccessfulEvent) {
        this.clientFactory = clientFactory;
        this.view = view;
        this.callback = callback;
        this.fireSignInSuccessfulEvent = fireSignInSuccessfulEvent;
    }

    @Override
    public void onSuccess(SuccessInfo result) {
        if (result.isSuccessful()) {
            clientFactory.didLogin(result.getUserDTO());
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
