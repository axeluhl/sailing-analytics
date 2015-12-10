package com.sap.sailing.gwt.home.shared.usermanagement;

import com.google.gwt.place.shared.PlaceController;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.sap.sailing.gwt.home.shared.app.ClientFactoryWithUserManagementService;
import com.sap.sailing.gwt.home.shared.usermanagement.info.LoggedInUserInfoPlace;
import com.sap.sse.security.ui.client.i18n.StringMessages;
import com.sap.sse.security.ui.shared.SuccessInfo;

public class AsyncLoginCallback implements AsyncCallback<SuccessInfo> {
    
    private final ClientFactoryWithUserManagementService clientFactory;
    private final PlaceController placeController;
    private final ErrorMessageView view;
    
    public AsyncLoginCallback(ClientFactoryWithUserManagementService clientFactory,
            PlaceController placeController, ErrorMessageView view) {
        this.clientFactory = clientFactory;
        this.placeController = placeController;
        this.view = view;
    }

    @Override
    public void onSuccess(SuccessInfo result) {
        if (result.isSuccessful()) {
            clientFactory.didLogin(result.getUserDTO());
            placeController.goTo(new LoggedInUserInfoPlace());
        } else {
            view.setErrorMessage(result.getMessage());
        }
    }
    
    @Override
    public void onFailure(Throwable caught) {
        view.setErrorMessage(StringMessages.INSTANCE.failedToSignIn());
    }

}
