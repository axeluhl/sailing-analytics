package com.sap.sailing.gwt.ui.adminconsole;

import com.google.gwt.user.client.ui.Grid;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.TextBox;
import com.google.gwt.user.client.ui.VerticalPanel;
import com.google.gwt.user.client.ui.Widget;
import com.sap.sailing.gwt.ui.client.StringMessages;
import com.sap.sse.gwt.client.dialog.DataEntryDialog;

public class RegistrationLinkWithQRCodeDialog extends DataEntryDialog<RegistrationLinkWithQRCode> {

    private StringMessages stringMessages;
    private TextBox secretTextBox;

    private RegistrationLinkWithQRCode registrationLinkWithQRCode;

    public RegistrationLinkWithQRCodeDialog(final StringMessages stringMessages,
            RegistrationLinkWithQRCode registrationLinkWithQRCode,
            DialogCallback<RegistrationLinkWithQRCode> callback) {
        super(stringMessages.registrationLinkDialog(), stringMessages.explainRegistrationLinkDialog(),
                stringMessages.ok(), stringMessages.cancel(), null, true, callback);
        this.stringMessages = stringMessages;
        this.registrationLinkWithQRCode = registrationLinkWithQRCode == null ? new RegistrationLinkWithQRCode()
                : registrationLinkWithQRCode;
        secretTextBox = createTextBox(registrationLinkWithQRCode.getSecret(), 20);
    }

    @Override
    protected RegistrationLinkWithQRCode getResult() {
        registrationLinkWithQRCode.setSecret(secretTextBox.getValue());
        return registrationLinkWithQRCode;
    }

    @Override
    protected Widget getAdditionalWidget() {
        final VerticalPanel panel = new VerticalPanel();
        Widget additionalWidget = super.getAdditionalWidget();
        if (additionalWidget != null) {
            panel.add(additionalWidget);
        }
        Grid formGrid = new Grid(1, 2);
        panel.add(formGrid);

        formGrid.setWidget(0, 0, new Label(stringMessages.registrationLinkSecret() + ":"));
        formGrid.setWidget(0, 1, secretTextBox);

        return panel;
    }

}
