package com.sap.sse.security.ui.loginpanel;

import com.sap.sse.security.ui.client.StringMessages;
import com.sap.sse.security.ui.client.component.AbstractUserDialog;
import com.sap.sse.security.ui.shared.UserManagementServiceAsync;

public class SignUpDialog extends AbstractUserDialog {
    public SignUpDialog(StringMessages stringMessages, UserManagementServiceAsync userManagementService, DialogCallback<UserData> callback) {
        super(stringMessages, stringMessages.signUp(), userManagementService, /* user */ null, callback);
    }
}
