package com.sap.sailing.gwt.ui.adminconsole;

import com.google.gwt.core.client.GWT;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.http.client.URL;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.Grid;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.TextBox;
import com.google.gwt.user.client.ui.VerticalPanel;
import com.google.gwt.user.client.ui.Widget;
import com.sap.sailing.domain.common.BranchIOConstants;
import com.sap.sailing.gwt.ui.client.StringMessages;
import com.sap.sailing.gwt.ui.common.client.RandomString;
import com.sap.sse.gwt.client.dialog.DataEntryDialog;

public class RegistrationLinkWithQRCodeDialog extends DataEntryDialog<RegistrationLinkWithQRCode> {

    private final boolean editMode;
    private final String regattaName;
    
    private final StringMessages stringMessages;
    private final TextBox secretTextBox;
    private Button generateSecretButton;
    private final Label urlLabel;

    private RegistrationLinkWithQRCode registrationLinkWithQRCode;

    public RegistrationLinkWithQRCodeDialog(final StringMessages stringMessages, String regattaName,
            RegistrationLinkWithQRCode registrationLinkWithQRCode, boolean editMode,
            DialogCallback<RegistrationLinkWithQRCode> callback) {
        super(stringMessages.registrationLinkDialog(), stringMessages.explainRegistrationLinkDialog(),
                stringMessages.ok(), stringMessages.cancel(), null, true, callback);
        this.stringMessages = stringMessages;
        this.editMode = editMode;
        this.regattaName = regattaName;
        this.registrationLinkWithQRCode = registrationLinkWithQRCode == null ? new RegistrationLinkWithQRCode()
                : registrationLinkWithQRCode;
        secretTextBox = createTextBox(registrationLinkWithQRCode.getSecret(), 30);
        secretTextBox.ensureDebugId("SecretTextBox");
        secretTextBox.setEnabled(editMode);
        if (editMode) {
            generateSecretButton = new Button("generate", new ClickHandler() {
                
                @Override
                public void onClick(ClickEvent event) {
                    final String randomString = RandomString.createRandomSecret(20);
                    registrationLinkWithQRCode.setSecret(randomString);
                    secretTextBox.setText(randomString);
                }
            });
            generateSecretButton.ensureDebugId("GenerateSecretButton");
        }
        urlLabel = new Label("http://url.com");
        urlLabel.ensureDebugId("RegistrationLinkUrl");
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
        final Grid formGrid = new Grid(1, 3);
        panel.add(formGrid);

        formGrid.setWidget(0, 0, new Label(stringMessages.registrationLinkSecret() + ":"));
        formGrid.setWidget(0, 1, secretTextBox);
        if (generateSecretButton != null) {
            formGrid.setWidget(0, 2, generateSecretButton);
        }
        
        if (!editMode) {
            panel.add(urlLabel);
            
            updateDisplayWidgets();
        }

        return panel;
    }
    
    private void updateDisplayWidgets() {
        String baseUrl = GWT.getHostPageBaseURL();
        if (baseUrl.endsWith("/")) {
            baseUrl = baseUrl.substring(0, baseUrl.lastIndexOf("/"));
        }
        String url = baseUrl.substring(0, baseUrl.lastIndexOf("/")) + "/sailingserver/api/v1/regattas/"
                + URL.encodeQueryString(regattaName) + "/competitors/createandadd?" + "regatta_name="
                + URL.encodeQueryString(regattaName) + "&secret="
                + URL.encodeQueryString(secretTextBox.getValue());
        String deeplinkUrl = BranchIOConstants.OPEN_REGATTA_APP_BRANCHIO + "?"
                + BranchIOConstants.OPEN_REGATTA_APP_BRANCHIO_PATH + "=" + URL.encodeQueryString(url);
        urlLabel.setText(deeplinkUrl);
    }

}
