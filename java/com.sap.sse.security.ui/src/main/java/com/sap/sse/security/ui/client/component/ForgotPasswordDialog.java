package com.sap.sse.security.ui.client.component;

import java.util.HashMap;

import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.Grid;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.Widget;
import com.sap.sse.gwt.client.Notification;
import com.sap.sse.gwt.client.Notification.NotificationType;
import com.sap.sse.gwt.client.async.MarkedAsyncCallback;
import com.sap.sse.security.shared.UserManagementException;
import com.sap.sse.security.ui.client.EntryPointLinkFactory;
import com.sap.sse.security.ui.client.UserManagementServiceAsync;
import com.sap.sse.security.ui.client.i18n.StringMessages;
import com.sap.sse.security.ui.shared.UserDTO;

public class ForgotPasswordDialog extends AbstractUserDialog {
    public ForgotPasswordDialog(StringMessages stringMessages, UserManagementServiceAsync userManagementService, UserDTO user,
            com.sap.sse.gwt.client.dialog.DataEntryDialog.DialogCallback<UserData> callback) {
        super(stringMessages, stringMessages.forgotPassword(), stringMessages.enterUsernameOrEmail(),
                userManagementService, user, /* validator */ null, callback);
    }
    
    /**
     * Uses a default callback handler that triggers {@link UserManagementServiceAsync#resetPassword(String, String, String, AsyncCallback)} when
     * the user confirms the dialog.
     */
    public ForgotPasswordDialog(final StringMessages stringMessages, final UserManagementServiceAsync userManagementService, UserDTO user) {
        this(stringMessages, userManagementService, user, new DialogCallback<UserData>() {
            @Override
            public void ok(final UserData userData) {
                userManagementService.resetPassword(userData.getUsername(), userData.getEmail(),
                        EntryPointLinkFactory.createPasswordResetLink(new HashMap<String, String>()),
                        new MarkedAsyncCallback<Void>(new AsyncCallback<Void>() {
                    @Override
                    public void onFailure(Throwable caught) {
                        if (caught instanceof UserManagementException) {
                            if (UserManagementException.CANNOT_RESET_PASSWORD_WITHOUT_VALIDATED_EMAIL.equals(caught.getMessage())) {
                                Notification.notify(StringMessages.INSTANCE.cannotResetPasswordWithoutValidatedEmail(userData.getUsername()), NotificationType.ERROR);
                            } else {
                                Notification.notify(StringMessages.INSTANCE.errorDuringPasswordReset(caught.getMessage()), NotificationType.ERROR);
                            }
                        } else {
                            Notification.notify(StringMessages.INSTANCE.errorDuringPasswordReset(caught.getMessage()), NotificationType.ERROR);
                        }
                    }
    
                    @Override
                    public void onSuccess(Void result) {
                        final StringBuilder nameOrEmail = new StringBuilder();
                        if (userData.getUsername() != null && !userData.getUsername().isEmpty()) {
                            nameOrEmail.append(userData.getUsername());
                        }
                        if (userData.getEmail() != null && !userData.getEmail().isEmpty()) {
                            if (nameOrEmail.length() > 0) {
                                nameOrEmail.append(" / ");
                            }
                            nameOrEmail.append(userData.getEmail());
                        }
                        Notification.notify(StringMessages.INSTANCE.passwordResetLinkSent(nameOrEmail.toString()), NotificationType.INFO);
                    }
                }));
            }
    
            @Override public void cancel() {}
        });
    }

    @Override
    protected Widget getAdditionalWidget() {
        Grid result = new Grid(4, 2);
        result.setWidget(0, 0, new Label(getStringMessages().username()));
        result.setWidget(0, 1, getNameBox());
        result.setWidget(1, 0, new Label(getStringMessages().email()));
        result.setWidget(1, 1, getEmailBox());
        return result;
    }
}
