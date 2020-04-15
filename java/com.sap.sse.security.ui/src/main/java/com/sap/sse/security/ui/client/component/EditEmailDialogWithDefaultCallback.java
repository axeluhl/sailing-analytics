package com.sap.sse.security.ui.client.component;

import java.util.HashMap;

import com.google.gwt.user.client.rpc.AsyncCallback;
import com.sap.sse.gwt.client.Notification;
import com.sap.sse.gwt.client.Notification.NotificationType;
import com.sap.sse.gwt.client.async.MarkedAsyncCallback;
import com.sap.sse.security.shared.dto.UserDTO;
import com.sap.sse.security.ui.client.EntryPointLinkFactory;
import com.sap.sse.security.ui.client.UserManagementServiceAsync;
import com.sap.sse.security.ui.client.i18n.StringMessages;

/**
 * Forwards the e-mail update request to a {@link UserManagementServiceAsync} and uses notifications to show success or
 * failure of the server-side update after calling back to an optional additional callback that can be provided to the
 * constructor.
 * 
 * @author Axel Uhl (D043530)
 *
 */
public class EditEmailDialogWithDefaultCallback extends EditEmailDialog {
    public EditEmailDialogWithDefaultCallback(final StringMessages stringMessages,
            final UserManagementServiceAsync userManagementService, UserDTO user) {
        this(stringMessages, userManagementService, user, /* additional callback */ null);
    }
    
    public EditEmailDialogWithDefaultCallback(final StringMessages stringMessages,
            final UserManagementServiceAsync userManagementService, UserDTO user,
            final AsyncCallback<UserData> callback) {
        super(stringMessages, userManagementService, user, new DialogCallback<UserData>() {
            @Override
            public void ok(final UserData userData) {
                userManagementService.updateSimpleUserEmail(userData.getUsername(), userData.getEmail(),
                        EntryPointLinkFactory.createEmailValidationLink(new HashMap<String, String>()),
                        new MarkedAsyncCallback<Void>(
                        new AsyncCallback<Void>() {
                            @Override
                            public void onFailure(Throwable caught) {
                                if (callback != null) {
                                    callback.onFailure(caught);
                                }
                                Notification.notify(stringMessages.errorUpdatingEmail(caught.getMessage()), NotificationType.ERROR);
                            }

                            @Override
                            public void onSuccess(Void result) {
                                if (callback != null) {
                                    callback.onSuccess(userData);
                                }
                                        Notification.notify(stringMessages.successfullyUpdatedEmail(
                                                userData.getUsername(), userData.getEmail()), NotificationType.SUCCESS);
                            }
                        }));
            }
            @Override public void cancel() {}
        });
    }

}
