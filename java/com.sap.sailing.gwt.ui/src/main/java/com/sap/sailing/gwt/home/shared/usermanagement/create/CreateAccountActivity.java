package com.sap.sailing.gwt.home.shared.usermanagement.create;

import java.util.HashMap;

import com.google.gwt.activity.shared.AbstractActivity;
import com.google.gwt.event.shared.EventBus;
import com.google.gwt.place.shared.PlaceController;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.AcceptsOneWidget;
import com.sap.sailing.gwt.home.shared.app.ClientFactoryWithUserManagementService;
import com.sap.sailing.gwt.home.shared.usermanagement.AsyncLoginCallback;
import com.sap.sailing.gwt.home.shared.usermanagement.signin.SignInPlace;
import com.sap.sse.security.ui.client.EntryPointLinkFactory;
import com.sap.sse.security.ui.client.component.NewAccountValidator;
import com.sap.sse.security.ui.client.i18n.StringMessages;
import com.sap.sse.security.ui.shared.UserDTO;

public class CreateAccountActivity extends AbstractActivity implements CreateAccountView.Presenter {

    private final ClientFactoryWithUserManagementService clientFactory;
    private final PlaceController placeController;
    private final CreateAccountView view = new CreateAccountViewImpl();
    
    private final StringMessages i18n_sec = StringMessages.INSTANCE;
    private final NewAccountValidator validator = new NewAccountValidator(i18n_sec);

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
        String errorMessage = validator.validateUsernameAndPassword(username, password, passwordConfirmation);
        if (errorMessage != null && !errorMessage.isEmpty()) {
            view.setErrorMessage(errorMessage);
            return;
        }
        clientFactory.getUserManagement().createSimpleUser(username, email, password, 
                EntryPointLinkFactory.createEmailValidationLink(new HashMap<String, String>()), 
                new AsyncCallback<UserDTO>() {
            @Override
            public void onSuccess(UserDTO result) {
                clientFactory.getUserManagement().login(result.getName(), password, 
                        new AsyncLoginCallback(clientFactory, placeController, view));
            }
            
            @Override
            public void onFailure(Throwable caught) {
                view.setErrorMessage("Error occured! Creating account failed.");
            }
        });
    }

    @Override
    public void signIn() {
        placeController.goTo(new SignInPlace());
    }

}
