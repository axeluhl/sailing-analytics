package com.sap.sse.security.ui.userprofile.shared.userdetails;

import java.util.Collection;

import com.google.gwt.core.shared.GWT;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.ListBox;
import com.sap.sse.common.Util;
import com.sap.sse.security.shared.UserGroup;
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
        authenticationManager.updateUserProperties(fullName, company, locale, new AsyncCallback<UserDTO>() {
            @Override
            public void onSuccess(UserDTO result) {
                Window.alert(i18n_sec.successfullyUpdatedUserProperties(
                        authenticationManager.getAuthenticationContext().getCurrentUser().getName()));
            }
            
            @Override
            public void onFailure(Throwable caught) {
                Window.alert(i18n_sec.errorUpdatingUserProperties(caught.getMessage()));
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
        final String username = authenticationManager.getAuthenticationContext().getCurrentUser().getName();
        String errorMessage = validator.validateUsernameAndPassword(username, newPassword, newPasswordConfirmation);
        if (errorMessage != null && !errorMessage.isEmpty()) {
            Window.alert(errorMessage);
            return;
        }
        userManagementService.updateSimpleUserPassword(username, oldPassword, null, newPassword, 
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
                        view.clearPasswordFields();
                    }
                });
    }

    @Override
    public void fillTenants(ListBox tenantListBox) {
        userManagementService.getUserGroups(new AsyncCallback<Collection<UserGroup>>() {
            @Override
            public void onFailure(Throwable caught) {
                GWT.log("Error fetching tenants: "+caught.getMessage());
            }

            @Override
            public void onSuccess(Collection<UserGroup> result) {
                final String oldSelectedTenantIdAsString = tenantListBox.getSelectedValue();
                tenantListBox.clear();
                for (final UserGroup tenant : result) {
                    tenantListBox.addItem(tenant.getName(), tenant.getId().toString());
                    if (Util.equalsWithNull(tenant.getId().toString(), oldSelectedTenantIdAsString)) {
                        tenantListBox.setSelectedIndex(tenantListBox.getItemCount()-1);
                    }
                }
            }
        });
    }

}
