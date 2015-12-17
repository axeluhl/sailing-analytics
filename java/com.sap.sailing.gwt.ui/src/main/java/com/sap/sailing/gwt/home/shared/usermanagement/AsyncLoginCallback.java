package com.sap.sailing.gwt.home.shared.usermanagement;

import com.google.gwt.event.shared.EventBus;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.sap.sailing.gwt.home.shared.app.ClientFactoryWithUserManagementService;
import com.sap.sailing.gwt.home.shared.usermanagement.UserManagementPlaceManagementController.SignInSuccessfulEvent;
import com.sap.sse.security.ui.client.i18n.StringMessages;
import com.sap.sse.security.ui.shared.SuccessInfo;

public class AsyncLoginCallback implements AsyncCallback<SuccessInfo> {
    
    private final ClientFactoryWithUserManagementService clientFactory;
    private final ErrorMessageView view;
    private final EventBus eventBus;
    
    public AsyncLoginCallback(ClientFactoryWithUserManagementService clientFactory, ErrorMessageView view,
            EventBus eventBus) {
        this.clientFactory = clientFactory;
        this.view = view;
        this.eventBus = eventBus;
    }

    @Override
    public void onSuccess(SuccessInfo result) {
        if (result.isSuccessful()) {
            clientFactory.didLogin(result.getUserDTO());
            eventBus.fireEvent(new SignInSuccessfulEvent());
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
