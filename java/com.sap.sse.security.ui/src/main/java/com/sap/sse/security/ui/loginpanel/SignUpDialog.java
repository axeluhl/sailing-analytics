package com.sap.sse.security.ui.loginpanel;

import com.sap.sse.security.ui.client.UserManagementServiceAsync;
import com.sap.sse.security.ui.client.component.AbstractUserDialog;
import com.sap.sse.security.ui.client.i18n.StringMessages;

public class SignUpDialog extends AbstractUserDialog {
    public SignUpDialog(StringMessages stringMessages, UserManagementServiceAsync userManagementService, DialogCallback<UserData> callback) {
        super(stringMessages, stringMessages.signUp(), userManagementService, /* user */ null, callback);
    }
}
