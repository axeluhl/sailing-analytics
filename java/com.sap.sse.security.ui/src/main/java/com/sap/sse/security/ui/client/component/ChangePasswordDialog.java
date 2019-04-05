package com.sap.sse.security.ui.client.component;

import com.google.gwt.user.client.ui.FocusWidget;
import com.sap.sse.security.shared.dto.UserDTO;
import com.sap.sse.security.ui.client.UserManagementServiceAsync;
import com.sap.sse.security.ui.client.i18n.StringMessages;

public class ChangePasswordDialog extends AbstractUserDialog {
    public ChangePasswordDialog(StringMessages stringMessages, UserManagementServiceAsync userManagementService, UserDTO user,
            com.sap.sse.gwt.client.dialog.DataEntryDialog.DialogCallback<UserData> callback) {
        super(stringMessages, stringMessages.changePassword(), userManagementService, user, callback, false);
        getNameBox().setEnabled(false);
        getEmailBox().setEnabled(false);
    }

    @Override
    protected FocusWidget getInitialFocusWidget() {
        return getPwBox();
    }
}
