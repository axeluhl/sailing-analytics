package com.sap.sailing.gwt.home.mobile.places.user.profile;

import com.google.gwt.activity.shared.AbstractActivity;
import com.google.gwt.event.shared.EventBus;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.AcceptsOneWidget;
import com.sap.sailing.gwt.home.mobile.app.MobileApplicationClientFactory;
import com.sap.sailing.gwt.home.shared.places.user.profile.AbstractUserProfilePlace;
import com.sap.sailing.gwt.home.shared.usermanagement.AuthenticationContextEvent;
import com.sap.sse.security.shared.UserManagementException;
import com.sap.sse.security.ui.client.component.NewAccountValidator;
import com.sap.sse.security.ui.client.i18n.StringMessages;

public class UserProfileDetailsActivity extends AbstractActivity implements UserProfileDetailsView.Presenter {

    private final MobileApplicationClientFactory clientFactory;
    
    private final StringMessages i18n_sec = StringMessages.INSTANCE;
    private final NewAccountValidator validator = new NewAccountValidator(i18n_sec);
    
    private final UserProfileDetailsView currentView = new UserProfileDetailsViewImpl(this);
    
    public UserProfileDetailsActivity(AbstractUserProfilePlace place, MobileApplicationClientFactory clientFactory) {
        this.clientFactory = clientFactory;
    }

    @Override
    public void start(final AcceptsOneWidget panel, final EventBus eventBus) {
        panel.setWidget(currentView);
        currentView.setUserManagementContext(clientFactory.getAuthenticationManager().getAuthenticationContext());
        eventBus.addHandler(AuthenticationContextEvent.TYPE, new AuthenticationContextEvent.Handler() {
            @Override
            public void onUserChangeEvent(AuthenticationContextEvent event) {
                currentView.setUserManagementContext(event.getCtx());
            }
        });
    }
    
    @Override
    public void doTriggerLoginForm() {
        clientFactory.getNavigator().getSignInNavigation().goToPlace();
    }
    
    @Override
    public void handleSaveChangesRequest(String fullName, String company) {
        final String username = clientFactory.getAuthenticationManager().getAuthenticationContext().getCurrentUser().getName();
        clientFactory.getUserManagementService().updateUserProperties(username, fullName, company,
                new AsyncCallback<Void>() {
            @Override
            public void onSuccess(Void result) {
                clientFactory.getAuthenticationManager().refreshUser();
                Window.alert(i18n_sec.successfullyUpdatedUserProperties(username));
            }
            
            @Override
            public void onFailure(Throwable caught) {
                Window.alert(i18n_sec.errorUpdatingUserProperties(caught.getMessage()));
            }
        });
    }
    
    @Override
    public void handleEmailChangeRequest(final String email) {
        final String username = clientFactory.getAuthenticationManager().getAuthenticationContext().getCurrentUser().getName();
        final String url = Window.Location.createUrlBuilder()
                .setHash(clientFactory.getNavigator().getMailVerifiedConfirmationNavigation().getTargetUrl())
                .buildString();
        clientFactory.getUserManagementService().updateSimpleUserEmail(username, email, url,
                new AsyncCallback<Void>() {
                    @Override
                    public void onSuccess(Void result) {
                        Window.alert(i18n_sec.successfullyUpdatedEmail(username, email));
                    }
                    
                    @Override
                    public void onFailure(Throwable caught) {
                        Window.alert(i18n_sec.errorUpdatingEmail(caught.getMessage()));
                    }
                });
    }
    
    @Override
    public void handlePasswordChangeRequest(String oldPassword, String newPassword, String newPasswordConfirmation) {
        final String username = clientFactory.getAuthenticationManager().getAuthenticationContext().getCurrentUser().getName();
        String errorMessage = validator.validateUsernameAndPassword(username, newPassword, newPasswordConfirmation);
        if (errorMessage != null && !errorMessage.isEmpty()) {
            Window.alert(errorMessage);
            return;
        }
        clientFactory.getUserManagementService().updateSimpleUserPassword(username, oldPassword, null, newPassword, 
                new AsyncCallback<Void>() {
                    @Override
                    public void onFailure(Throwable caught) {
                        if (caught instanceof UserManagementException) {
                            String message = ((UserManagementException) caught).getMessage();
                            if (UserManagementException.PASSWORD_DOES_NOT_MEET_REQUIREMENTS.equals(message)) {
                                Window.alert(i18n_sec.passwordDoesNotMeetRequirements());
                            } else if (UserManagementException.INVALID_CREDENTIALS.equals(message)) {
                                Window.alert(i18n_sec.invalidCredentials());
                            } else {
                                Window.alert(i18n_sec.errorChangingPassword(caught.getMessage()));
                            }
                        } else {
                            Window.alert(i18n_sec.errorChangingPassword(caught.getMessage()));
                        }
                    }

                    @Override
                    public void onSuccess(Void result) {
                        Window.alert(i18n_sec.passwordSuccessfullyChanged());
                        currentView.setUserManagementContext(clientFactory.getAuthenticationManager().getAuthenticationContext());
                    }
                });
    }
}
