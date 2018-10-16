package com.sap.sailing.gwt.ui.adminconsole;

import java.util.stream.Collectors;
import java.util.stream.Stream;

import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.user.client.ui.Button;
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
    private Button generateSecretButton;

    private RegistrationLinkWithQRCode registrationLinkWithQRCode;

    public RegistrationLinkWithQRCodeDialog(final StringMessages stringMessages,
            RegistrationLinkWithQRCode registrationLinkWithQRCode,
            DialogCallback<RegistrationLinkWithQRCode> callback) {
        super(stringMessages.registrationLinkDialog(), stringMessages.explainRegistrationLinkDialog(),
                stringMessages.ok(), stringMessages.cancel(), null, true, callback);
        this.stringMessages = stringMessages;
        this.registrationLinkWithQRCode = registrationLinkWithQRCode == null ? new RegistrationLinkWithQRCode()
                : registrationLinkWithQRCode;
        secretTextBox = createTextBox(registrationLinkWithQRCode.getSecret(), 30);
        
        generateSecretButton = new Button("generate", new ClickHandler() {
            
            @Override
            public void onClick(ClickEvent event) {
                final String randomString = createRandomSecret(20);
                registrationLinkWithQRCode.setSecret(randomString);
                secretTextBox.setText(randomString);
            }
        });
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
        Grid formGrid = new Grid(1, 3);
        panel.add(formGrid);

        formGrid.setWidget(0, 0, new Label(stringMessages.registrationLinkSecret() + ":"));
        formGrid.setWidget(0, 1, secretTextBox);
        formGrid.setWidget(0, 2, generateSecretButton);

        return panel;
    }

    private static String createRandomSecret(int length) {
        String randomString = Stream.generate(Math::random).map(r -> (int) (r * 100))
                .filter(i -> (i > 47 && i < 58 || i > 64 && i < 90)).limit(length)
                .map(i -> (String) String.valueOf((char) i.intValue())).collect(Collectors.joining());
        return randomString;
    }

}
