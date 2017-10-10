package com.sap.sse.security.ui.client.component;

import java.util.HashMap;

import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.sap.sse.security.ui.client.EntryPointLinkFactory;
import com.sap.sse.security.ui.client.UserManagementServiceAsync;
import com.sap.sse.security.ui.client.component.UserManagementPanel.UserCreatedEventHandler;
import com.sap.sse.security.ui.client.i18n.StringMessages;
import com.sap.sse.security.ui.shared.UserDTO;

/**
 * Produces username, e-mail and password as the dialog's result.
 * 
 * @author Axel Uhl (D043530)
 *
 */
public class CreateUserDialog extends AbstractUserDialog {
    public CreateUserDialog(final StringMessages stringMessages,
            final UserManagementServiceAsync userManagementService, final Iterable<UserCreatedEventHandler> handlers) {
        super(stringMessages, stringMessages.createUser(), userManagementService, /* start with no user */null,
                new DialogCallback<UserData>() {
                    @Override
                    public void ok(UserData usernameEmailPassword) {
                        userManagementService
                                .createSimpleUser(usernameEmailPassword.getUsername(),
                                        usernameEmailPassword.getEmail(), usernameEmailPassword.getPassword(),
                                        /* fullName */ null, /* company */ null, /* TODO locale */ null,
                                        EntryPointLinkFactory.createEmailValidationLink(new HashMap<String, String>()), new AsyncCallback<UserDTO>() {
                                            @Override
                                            public void onSuccess(UserDTO result) {
                                                for (UserCreatedEventHandler handler : handlers) {
                                                    handler.onUserCreated(result);
                                                }
                                            }

                                            @Override
                                            public void onFailure(Throwable caught) {
                                                Window.alert(stringMessages.couldNotCreateUser(caught.getMessage()));
                                            }
                                        });
                    }

                    @Override
                    public void cancel() {
                    }
                });
    }
}
