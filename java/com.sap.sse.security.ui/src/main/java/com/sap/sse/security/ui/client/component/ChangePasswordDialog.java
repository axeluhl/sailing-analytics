package com.sap.sse.security.ui.client.component;

import com.google.gwt.user.client.ui.Widget;
import com.sap.sse.gwt.client.dialog.DataEntryDialog;
import com.sap.sse.security.ui.client.StringMessages;
import com.sap.sse.security.ui.shared.UserDTO;

public class ChangePasswordDialog extends DataEntryDialog<String> {
    public ChangePasswordDialog(StringMessages stringMessages, UserDTO user,
            com.sap.sse.gwt.client.dialog.DataEntryDialog.DialogCallback<String> callback) {
        super(stringMessages.changePassword(), stringMessages.changePassword(), stringMessages.ok(), stringMessages.cancel(),
                new DataEntryDialog.Validator<String>() {
                    @Override
                    public String getErrorMessage(String valueToValidate) {
                        // TODO Auto-generated method stub
                        return null;
                    }
                }, /*animationEnabled*/true, callback);
    }

    
    @Override
    protected Widget getAdditionalWidget() {
        return super.getAdditionalWidget();
    }


    @Override
    protected String getResult() {
        // TODO Auto-generated method stub
        return null;
    }

}
