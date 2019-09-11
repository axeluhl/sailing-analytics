package com.sap.sse.security.ui.authentication.create;

import com.google.gwt.activity.shared.AbstractActivity;
import com.google.gwt.event.shared.EventBus;
import com.google.gwt.place.shared.PlaceController;
import com.google.gwt.user.client.ui.AcceptsOneWidget;
import com.sap.sse.security.shared.dto.UserDTO;
import com.sap.sse.security.ui.authentication.AuthenticationClientFactory;
import com.sap.sse.security.ui.authentication.AuthenticationManager.SuccessCallback;
import com.sap.sse.security.ui.authentication.confirm.ConfirmationInfoPlace;
import com.sap.sse.security.ui.authentication.confirm.ConfirmationInfoPlace.Action;
import com.sap.sse.security.ui.authentication.signin.SignInPlace;
import com.sap.sse.security.ui.client.component.NewAccountValidator;
import com.sap.sse.security.ui.client.i18n.StringMessages;
import com.sap.sse.security.ui.shared.SuccessInfo;

public class CreateAccountActivity extends AbstractActivity implements CreateAccountView.Presenter {

    private final AuthenticationClientFactory clientFactory;
    private final PlaceController placeController;
    private final CreateAccountView view;
    
    private final StringMessages i18n_sec = StringMessages.INSTANCE;
    private final CreateAccountFormValues values = new CreateAccountFormValues();

    public CreateAccountActivity(AuthenticationClientFactory clientFactory, PlaceController placeController) {
        this.clientFactory = clientFactory;
        this.placeController = placeController;
        this.view = clientFactory.createCreateAccountView();
    }

    @Override
    public void start(AcceptsOneWidget panel, EventBus eventBus) {
        view.setPresenter(this);
        panel.setWidget(view);
        values.validate();
    }
    
    @Override
    public void createAccount() {
        if (values.validate()) {
            clientFactory.getAuthenticationManager().createAccount(values.username, values.email, 
                    values.password, values.fullName, values.company, new SuccessCallback<UserDTO>() {
                @Override
                public void onSuccess(final UserDTO result) {
                    clientFactory.getAuthenticationManager().login(result.getName(), values.password, 
                            new LoginAfterCreatingAccountSuccessCallback());
                    placeController.goTo(new ConfirmationInfoPlace(Action.ACCOUNT_CREATED, result.getName()));
                }
            });
        }
    }
    
    @Override
    public void onChangeEmail(String newValue) {
        values.email = newValue;
    }
    
    @Override
    public void onChangeUsername(String newValue) {
        values.username = newValue;
        values.validate();
    }
    
    @Override
    public void onChangeFullName(String newValue) {
        values.fullName = newValue;
    }

    @Override
    public void onChangeCompany(String newValue) {
        values.company = newValue;
    }
    
    @Override
    public void onChangePassword(String newValue) {
        values.password = newValue;
        values.validate();
    }
    
    @Override
    public void onChangePasswordConfirmation(String newValue) {
        values.passwordConfirmation = newValue;
        values.validate();
    }
    
    @Override
    public void signIn() {
        placeController.goTo(new SignInPlace());
    }
    
    private class CreateAccountFormValues {
        private final NewAccountValidator validator = new NewAccountValidator(i18n_sec);
        private String username, fullName, company, email, password, passwordConfirmation;
        
        private boolean validate() {
            String errorMessage = validator.validateUsernameAndPassword(username, password, passwordConfirmation);
            boolean isValid = errorMessage == null || errorMessage.isEmpty();
            view.setErrorMessage(isValid ? null : errorMessage);
            view.getCreateAccountControl().setEnabled(isValid);
            return isValid;
        }
    }
    
    private class LoginAfterCreatingAccountSuccessCallback implements SuccessCallback<SuccessInfo> {
        @Override
        public void onSuccess(SuccessInfo result) {
        }
    }

}
