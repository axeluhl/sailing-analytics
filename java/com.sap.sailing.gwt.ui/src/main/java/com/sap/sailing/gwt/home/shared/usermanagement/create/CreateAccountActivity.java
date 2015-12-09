package com.sap.sailing.gwt.home.shared.usermanagement.create;

import java.util.HashMap;

import com.google.gwt.activity.shared.AbstractActivity;
import com.google.gwt.event.shared.EventBus;
import com.google.gwt.place.shared.PlaceController;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.AcceptsOneWidget;
import com.sap.sailing.gwt.home.shared.app.ClientFactoryWithUserManagementService;
import com.sap.sailing.gwt.home.shared.usermanagement.info.LoggedInUserInfoPlace;
import com.sap.sailing.gwt.home.shared.usermanagement.signin.SigInPlace;
import com.sap.sse.security.ui.client.EntryPointLinkFactory;
import com.sap.sse.security.ui.shared.SuccessInfo;
import com.sap.sse.security.ui.shared.UserDTO;

public class CreateAccountActivity extends AbstractActivity implements CreateAccountView.Presenter {

    private final ClientFactoryWithUserManagementService clientFactory;
    private final PlaceController placeController;
    private final CreateAccountView view = new CreateAccountViewImpl();

    public CreateAccountActivity(ClientFactoryWithUserManagementService clientFactory, PlaceController placeController) {
        this.clientFactory = clientFactory;
        this.placeController = placeController;
    }

    @Override
    public void start(AcceptsOneWidget panel, EventBus eventBus) {
        view.setPresenter(this);
        panel.setWidget(view);
    }
    
    @Override
    public void createAccount(String username, String email, final String password, String passwordConfirmation) {
        clientFactory.getUserManagement().createSimpleUser(username, email, password, 
                EntryPointLinkFactory.createEmailValidationLink(new HashMap<String, String>()), 
                new AsyncCallback<UserDTO>() {
            @Override
            public void onSuccess(UserDTO result) {
                clientFactory.getUserManagement().login(result.getName(), password, 
                        new AsyncCallback<SuccessInfo>() {
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
                                // TODO
                                view.setErrorMessage("TODO - login failed");
                            }
                        });
            }
            
            @Override
            public void onFailure(Throwable caught) {
                // TODO
                view.setErrorMessage("TODO - create account failed");
            }
        });
    }

    @Override
    public void signIn() {
        placeController.goTo(new SigInPlace());
    }

}
