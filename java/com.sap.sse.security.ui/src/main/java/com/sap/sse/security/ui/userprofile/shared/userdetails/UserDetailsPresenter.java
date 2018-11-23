package com.sap.sse.security.ui.userprofile.shared.userdetails;

import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.sap.sse.gwt.client.Notification;
import com.sap.sse.gwt.client.Notification.NotificationType;
import com.sap.sse.security.shared.UserManagementException;
import com.sap.sse.security.ui.authentication.AuthenticationManager;
import com.sap.sse.security.ui.authentication.app.AuthenticationContext;
import com.sap.sse.security.ui.client.UserManagementServiceAsync;
import com.sap.sse.security.ui.client.component.NewAccountValidator;
import com.sap.sse.security.ui.client.i18n.StringMessages;
import com.sap.sse.security.ui.shared.UserDTO;

/**
 * Default Presenter implementation for {@link UserDetailsView}.
 *
 */
public class UserDetailsPresenter implements AbstractUserDetails.Presenter {
    
    private final StringMessages i18n_sec = StringMessages.INSTANCE;
    private final NewAccountValidator validator = new NewAccountValidator(i18n_sec);
    
    private final AuthenticationManager authenticationManager;

    private final UserManagementServiceAsync userManagementService;
    private final String mailVerifiedConfirmationUrlToken;
    private final UserDetailsView view;

    public UserDetailsPresenter(UserDetailsView view, AuthenticationManager authenticationManager, UserManagementServiceAsync userManagementService, String mailVerifiedConfirmationUrlToken) {
        this.view = view;
        this.authenticationManager = authenticationManager;
        this.userManagementService = userManagementService;
        this.mailVerifiedConfirmationUrlToken = mailVerifiedConfirmationUrlToken;
        view.setPresenter(this);
        if (authenticationManager.getAuthenticationContext().isLoggedIn()) {
            view.setUser(authenticationManager.getAuthenticationContext().getCurrentUser());
        }
    }
    
    public void setAuthenticationContext(AuthenticationContext authenticationContext) {
        view.setUser(authenticationContext.getCurrentUser());
    }

    @Override
    public void handleSaveChangesRequest(String fullName, String company, String locale, String defaultTenantIdAsString) {
        authenticationManager.updateUserProperties(fullName, company, locale, defaultTenantIdAsString,
                new AsyncCallback<UserDTO>() {
            @Override
            public void onSuccess(UserDTO result) {
                Notification.notify(i18n_sec.successfullyUpdatedUserProperties(
                        authenticationManager.getAuthenticationContext().getCurrentUser().getName()),
                        NotificationType.INFO);
            }
            
            @Override
            public void onFailure(Throwable caught) {
                Notification.notify(i18n_sec.errorUpdatingUserProperties(caught.getMessage()), NotificationType.ERROR);
            }
        });
    }

    @Override
    public void handleEmailChangeRequest(final String email) {
        final String username = authenticationManager.getAuthenticationContext().getCurrentUser().getName();
        final String url = Window.Location.createUrlBuilder()
                .setHash(mailVerifiedConfirmationUrlToken).buildString();
        userManagementService.updateSimpleUserEmail(username, email, url,
                new AsyncCallback<Void>() {
                    @Override
                    public void onSuccess(Void result) {
                        Notification.notify(i18n_sec.successfullyUpdatedEmail(username, email), NotificationType.INFO);
                    }
                    
                    @Override
                    public void onFailure(Throwable caught) {
                        Notification.notify(i18n_sec.errorUpdatingEmail(caught.getMessage()), NotificationType.ERROR);
                    }
                });
    }
    
    @Override
    public void handlePasswordChangeRequest(String oldPassword, String newPassword, String newPasswordConfirmation) {
        final String username = authenticationManager.getAuthenticationContext().getCurrentUser().getName();
        String errorMessage = validator.validateUsernameAndPassword(username, newPassword, newPasswordConfirmation);
        if (errorMessage != null && !errorMessage.isEmpty()) {
            Notification.notify(errorMessage, NotificationType.ERROR);
            return;
        }
        userManagementService.updateSimpleUserPassword(username, oldPassword, null, newPassword, 
                new AsyncCallback<Void>() {
                    @Override
                    public void onFailure(Throwable caught) {
                        if (caught instanceof UserManagementException) {
                            String message = ((UserManagementException) caught).getMessage();
                            if (UserManagementException.PASSWORD_DOES_NOT_MEET_REQUIREMENTS.equals(message)) {
                                Notification.notify(i18n_sec.passwordDoesNotMeetRequirements(), NotificationType.ERROR);
                            } else if (UserManagementException.INVALID_CREDENTIALS.equals(message)) {
                                Notification.notify(i18n_sec.invalidCredentials(), NotificationType.ERROR);
                            } else {
                                Notification.notify(i18n_sec.errorChangingPassword(caught.getMessage()), NotificationType.ERROR);
                            }
                        } else {
                            Notification.notify(i18n_sec.errorChangingPassword(caught.getMessage()), NotificationType.ERROR);
                        }
                    }

                    @Override
                    public void onSuccess(Void result) {
                        Notification.notify(i18n_sec.passwordSuccessfullyChanged(), NotificationType.SUCCESS);
                        view.clearPasswordFields();
                    }
                });
    }
}
