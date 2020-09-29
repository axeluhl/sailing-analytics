package com.sap.sse.security.ui.client.component;

import java.util.HashMap;

import com.google.gwt.user.client.rpc.AsyncCallback;
import com.sap.sse.gwt.client.Notification;
import com.sap.sse.gwt.client.Notification.NotificationType;
import com.sap.sse.security.shared.dto.UserDTO;
import com.sap.sse.security.ui.client.EntryPointLinkFactory;
import com.sap.sse.security.ui.client.UserManagementWriteServiceAsync;
import com.sap.sse.security.ui.client.UserService;
import com.sap.sse.security.ui.client.i18n.StringMessages;
import com.sap.sse.security.ui.client.usermanagement.UserManagementPanel.UserCreatedEventHandler;

/**
 * Produces username, e-mail and password as the dialog's result.
 * 
 * @author Axel Uhl (D043530)
 *
 */
// TODO: Add input fields for full name, company and locale information!
public class CreateUserDialog extends AbstractUserDialog {
    public CreateUserDialog(final StringMessages stringMessages,
            final UserManagementWriteServiceAsync userManagementWriteService, final Iterable<UserCreatedEventHandler> handlers,
            final UserService userService) {
        super(stringMessages, stringMessages.createUser(), userManagementWriteService, /* start with no user */null,
                new DialogCallback<UserData>() {
                    @Override
                    public void ok(UserData usernameEmailPassword) {
                        userManagementWriteService
                                .createSimpleUser(usernameEmailPassword.getUsername(),
                                        usernameEmailPassword.getEmail(), usernameEmailPassword.getPassword(),
                                        /* TOOD fullName */ null, /* TODO company */ null, /* TODO locale */ null,
                                        EntryPointLinkFactory.createEmailValidationLink(new HashMap<String, String>()),
                                        new AsyncCallback<UserDTO>() {
                                            @Override
                                            public void onSuccess(UserDTO result) {
                                                for (UserCreatedEventHandler handler : handlers) {
                                                    handler.onUserCreated(result);
                                                }
                                            }

                                            @Override
                                            public void onFailure(Throwable caught) {
                                                Notification.notify(stringMessages.couldNotCreateUser(caught.getMessage()), NotificationType.ERROR);
                                            }
                                        });
                    }

                    @Override
                    public void cancel() {
                    }
                });
        this.ensureDebugId(this.getClass().getSimpleName());
    }
}
