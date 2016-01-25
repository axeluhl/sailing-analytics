package com.sap.sailing.gwt.home.shared.usermanagement.create;

import com.google.gwt.activity.shared.AbstractActivity;
import com.google.gwt.event.shared.EventBus;
import com.google.gwt.place.shared.PlaceController;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.AcceptsOneWidget;
import com.sap.sailing.gwt.home.shared.places.user.confirmation.ConfirmationPlace;
import com.sap.sailing.gwt.home.shared.places.user.confirmation.ConfirmationPlace.Action;
import com.sap.sailing.gwt.home.shared.usermanagement.AsyncLoginCallback;
import com.sap.sailing.gwt.home.shared.usermanagement.AuthenticationClientFactory;
import com.sap.sailing.gwt.home.shared.usermanagement.UserManagementPlaceManagementController;
import com.sap.sailing.gwt.home.shared.usermanagement.signin.SignInPlace;
import com.sap.sse.security.shared.UserManagementException;
import com.sap.sse.security.ui.client.component.NewAccountValidator;
import com.sap.sse.security.ui.client.i18n.StringMessages;
import com.sap.sse.security.ui.shared.UserDTO;

public class CreateAccountActivity extends AbstractActivity implements CreateAccountView.Presenter {

    private final AuthenticationClientFactory clientFactory;
    private final PlaceController placeController;
    private final CreateAccountView view;
    
    private final StringMessages i18n_sec = StringMessages.INSTANCE;
    private final NewAccountValidator validator = new NewAccountValidator(i18n_sec);
    private final UserManagementPlaceManagementController.Callback callback;

    public CreateAccountActivity(CreateAccountView view, AuthenticationClientFactory clientFactory,
            UserManagementPlaceManagementController.Callback callback, PlaceController placeController) {
        this.view = view;
        this.clientFactory = clientFactory;
        this.callback = callback;
        this.placeController = placeController;
    }

    @Override
    public void start(AcceptsOneWidget panel, EventBus eventBus) {
        view.setPresenter(this);
        panel.setWidget(view);
    }
    
    @Override
    public void createAccount(final String username, final String fullName, final String company, String email,
            final String password, String passwordConfirmation) {
        String errorMessage = validator.validateUsernameAndPassword(username, password, passwordConfirmation);
        if (errorMessage != null && !errorMessage.isEmpty()) {
            view.setErrorMessage(errorMessage);
            return;
        }
        
        clientFactory.getUserManagementService().createSimpleUser(username, email, password, fullName, company,
                callback.getCreateConfirmationUrl(), new AsyncCallback<UserDTO>() {
            @Override
            public void onSuccess(final UserDTO result) {
                clientFactory.getUserManagementService().login(result.getName(), password, 
                        new AsyncLoginCallback(clientFactory.getAuthenticationManager(), view, callback, false));
                placeController.goTo(new ConfirmationPlace(Action.ACCOUNT_CREATED, username));
            }
            
            @Override
            public void onFailure(Throwable caught) {
                if (caught instanceof UserManagementException) {
                    if (UserManagementException.USER_ALREADY_EXISTS.equals(((UserManagementException) caught).getMessage())) {
                        Window.alert(i18n_sec.userAlreadyExists(username));
                    }
                } else {
                    Window.alert(i18n_sec.errorCreatingUser(username, caught.getMessage()));
                }
            }
        });
    }

    @Override
    public void signIn() {
        placeController.goTo(new SignInPlace());
    }

}
