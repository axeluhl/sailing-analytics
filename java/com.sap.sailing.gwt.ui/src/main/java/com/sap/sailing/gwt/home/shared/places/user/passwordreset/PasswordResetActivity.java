package com.sap.sailing.gwt.home.shared.places.user.passwordreset;

import com.google.gwt.activity.shared.AbstractActivity;
import com.google.gwt.event.shared.EventBus;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.AcceptsOneWidget;
import com.sap.sse.security.shared.UserManagementException;
import com.sap.sse.security.ui.client.component.NewAccountValidator;
import com.sap.sse.security.ui.client.i18n.StringMessages;

public class PasswordResetActivity extends AbstractActivity implements PasswordResetView.Presenter {

    private final PasswordResetClientFactory clientFactory;
    private final PasswordResetView view;
    private final StringMessages i18n_sec = StringMessages.INSTANCE;
    private final ResetPasswordFormValues values;

    public PasswordResetActivity(PasswordResetPlace place, PasswordResetClientFactory clientFactory) {
        this.clientFactory = clientFactory;
        this.view = clientFactory.createPasswordResetView();
        this.values = new ResetPasswordFormValues(place.getName(), place.getResetSecret());
    }

    @Override
    public void start(final AcceptsOneWidget panel, EventBus eventBus) {
        view.setPresenter(this);
        panel.setWidget(view.asWidget());
        values.validate();
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
    public void resetPassword() {
        if (values.validate()) {
            clientFactory.getUserManagementService().updateSimpleUserPassword(values.username, null,
                    values.resetSecret, values.password, new AsyncCallback<Void>() {
                @Override
                public void onFailure(Throwable caught) {
                    if (caught instanceof UserManagementException) {
                        String message = ((UserManagementException) caught).getMessage();
                        if (UserManagementException.PASSWORD_DOES_NOT_MEET_REQUIREMENTS.equals(message)) {
                            view.setErrorMessage(i18n_sec.passwordDoesNotMeetRequirements());
                        } else if (UserManagementException.INVALID_CREDENTIALS.equals(message)) {
                            view.setErrorMessage(i18n_sec.invalidCredentials());
                        } else {
                            view.setErrorMessage(i18n_sec.errorChangingPassword(caught.getMessage()));
                        }
                    } else {
                        view.setErrorMessage(i18n_sec.errorChangingPassword(caught.getMessage()));
                    }
                }
                
                @Override
                public void onSuccess(Void result) {
                    clientFactory.getPasswordResettedConfirmationNavigation(values.username).goToPlace();
                }
            });
        }
    }
    
    private class ResetPasswordFormValues {
        private final NewAccountValidator validator = new ResetPasswordAccountValidator(i18n_sec);
        private final String username, resetSecret;
        private String password, passwordConfirmation;
        
        private ResetPasswordFormValues(String username, String resetSecret) {
            this.username = username;
            this.resetSecret = resetSecret;
        }

        private boolean validate() {
            String errorMessage = validator.validateUsernameAndPassword(username, password, passwordConfirmation);
            boolean isValid = errorMessage == null || errorMessage.isEmpty();
            view.setErrorMessage(isValid ? null : errorMessage);
            view.getChangePasswordControl().setEnabled(isValid);
            return isValid;
        }
    }
}
