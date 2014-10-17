package com.sap.sse.security.ui.client.component;

import com.google.gwt.user.client.ui.HorizontalPanel;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.TextBox;
import com.google.gwt.user.client.ui.Widget;
import com.sap.sse.gwt.client.dialog.DataEntryDialog;
import com.sap.sse.security.ui.client.StringMessages;
import com.sap.sse.security.ui.shared.UserDTO;

public class EditEmailDialog extends DataEntryDialog<String> {
    private final TextBox emailTextBox;
    private final StringMessages stringMessages;

    public EditEmailDialog(StringMessages stringMessages, UserDTO user, DialogCallback<String> callback) {
        super(stringMessages.editEmail(), stringMessages.editEmail(), stringMessages.ok(), stringMessages.cancel(),
                /* validator: anything is valid for an e-mail, even empty */ null, /* animationEnabled */ true, callback);
        this.stringMessages = stringMessages;
        emailTextBox = createTextBox(user.getEmail());
    }

    @Override
    protected Widget getAdditionalWidget() {
        final HorizontalPanel result = new HorizontalPanel();
        result.add(new Label(stringMessages.email()));
        result.add(emailTextBox);
        return result;
    }


    @Override
    protected String getResult() {
        return emailTextBox.getText();
    }

}
