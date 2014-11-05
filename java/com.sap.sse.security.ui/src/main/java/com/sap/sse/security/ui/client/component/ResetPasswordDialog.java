package com.sap.sse.security.ui.client.component;

import com.google.gwt.user.client.ui.Grid;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.Widget;
import com.sap.sse.security.ui.client.i18n.StringMessages;
import com.sap.sse.security.ui.shared.UserDTO;
import com.sap.sse.security.ui.shared.UserManagementServiceAsync;

public class ResetPasswordDialog extends AbstractUserDialog {
    public ResetPasswordDialog(StringMessages stringMessages, UserManagementServiceAsync userManagementService, UserDTO user,
            com.sap.sse.gwt.client.dialog.DataEntryDialog.DialogCallback<UserData> callback) {
        super(stringMessages, stringMessages.forgotPassword(), stringMessages.enterUsernameOrEmail(),
                userManagementService, user, callback);
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
