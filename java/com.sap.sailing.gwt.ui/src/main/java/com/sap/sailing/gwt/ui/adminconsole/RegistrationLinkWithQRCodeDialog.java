package com.sap.sailing.gwt.ui.adminconsole;

import java.util.HashMap;
import java.util.Map;

import com.google.gwt.core.client.GWT;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.http.client.URL;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.Anchor;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.CaptionPanel;
import com.google.gwt.user.client.ui.Grid;
import com.google.gwt.user.client.ui.Image;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.TextBox;
import com.google.gwt.user.client.ui.VerticalPanel;
import com.google.gwt.user.client.ui.Widget;
import com.sap.sailing.domain.common.BranchIO;
import com.sap.sailing.domain.common.BranchIOConstants;
import com.sap.sailing.gwt.ui.client.SailingServiceAsync;
import com.sap.sailing.gwt.ui.client.StringMessages;
import com.sap.sailing.gwt.ui.common.client.RandomString;
import com.sap.sse.gwt.client.dialog.DataEntryDialog;

public class RegistrationLinkWithQRCodeDialog extends DataEntryDialog<RegistrationLinkWithQRCode> {

    private final boolean editMode;
    private final String regattaName;

    private final SailingServiceAsync sailingService;
    private final StringMessages stringMessages;
    private final CaptionPanel secretPanel;
    private final VerticalPanel secretPanelContent;
    private final Label secretExplainLabel;
    private final CaptionPanel registrationLinkPanel;
    private final VerticalPanel registrationLinkPanelContent;
    private final Label registrationLinkExplain;
    private final CaptionPanel barcodePanel;
    private final VerticalPanel barcodePanelContent;
    private final Label barcodeExplainLabel;
    private final TextBox secretTextBox;
    private Button generateSecretButton;
    private final TextBox urlTextBox;
    private final Image qrCodeImage;

    private RegistrationLinkWithQRCode registrationLinkWithQRCode;

    public RegistrationLinkWithQRCodeDialog(final SailingServiceAsync sailingService,
            final StringMessages stringMessages, String regattaName,
            RegistrationLinkWithQRCode registrationLinkWithQRCode, boolean editMode,
            DialogCallback<RegistrationLinkWithQRCode> callback) {
        super(stringMessages.registrationLinkDialog(), stringMessages.explainRegistrationLinkDialog(),
                stringMessages.ok(), stringMessages.cancel(), null, true, callback);
        this.sailingService = sailingService;
        this.stringMessages = stringMessages;
        this.editMode = editMode;
        this.regattaName = regattaName;
        this.registrationLinkWithQRCode = registrationLinkWithQRCode == null ? new RegistrationLinkWithQRCode()
                : registrationLinkWithQRCode;

        secretPanel = new CaptionPanel(stringMessages.registrationLinkSecret());
        secretPanelContent = new VerticalPanel();
        secretPanel.add(secretPanelContent);
        secretExplainLabel = new Label(stringMessages.registrationLinkSecretExplain());
        secretPanelContent.add(secretExplainLabel);

        registrationLinkPanel = new CaptionPanel(stringMessages.registrationLinkUrl());
        registrationLinkPanelContent = new VerticalPanel();
        registrationLinkPanel.add(registrationLinkPanelContent);
        registrationLinkExplain = new Label(stringMessages.registrationLinkUrlExplain());
        registrationLinkPanelContent.add(registrationLinkExplain);
        urlTextBox = createTextBox("URL", 100);
        urlTextBox.ensureDebugId("RegistrationLinkUrl");
        registrationLinkPanelContent.add(urlTextBox);
        Anchor copyAnchor = new Anchor(stringMessages.registrationLinkUrlCopy());
        copyAnchor.addClickHandler(new ClickHandler() {

            @Override
            public void onClick(ClickEvent event) {
                urlTextBox.setFocus(true);
                urlTextBox.selectAll();
                copyToClipboard();
            }
        });
        registrationLinkPanelContent.add(copyAnchor);

        barcodePanel = new CaptionPanel(stringMessages.registrationLinkDialogQrcode());
        barcodePanelContent = new VerticalPanel();
        barcodePanel.add(barcodePanelContent);
        barcodeExplainLabel = new Label(stringMessages.registrationLinkDialogQrcodeExplain());
        barcodePanelContent.add(barcodeExplainLabel);
        qrCodeImage = new Image();
        qrCodeImage.ensureDebugId("OpenRegattaRegistrationLinkQrCode");
        barcodePanelContent.add(qrCodeImage);

        secretTextBox = createTextBox(registrationLinkWithQRCode.getSecret(), 30);
        secretTextBox.ensureDebugId("SecretTextBox");
        secretTextBox.setEnabled(editMode);
        if (editMode) {
            generateSecretButton = new Button(stringMessages.registrationLinkSecretGenerate(), new ClickHandler() {

                @Override
                public void onClick(ClickEvent event) {
                    final String randomString = RandomString.createRandomSecret(20);
                    registrationLinkWithQRCode.setSecret(randomString);
                    secretTextBox.setText(randomString);
                }
            });
            generateSecretButton.ensureDebugId("GenerateSecretButton");
        }
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
        final VerticalPanel dialogPanel = new VerticalPanel();
        panel.add(dialogPanel);
        dialogPanel.add(secretPanel);

        final Grid secretPanelFormGrid = new Grid(1, 3);
        secretPanelContent.add(secretPanelFormGrid);
        secretPanelFormGrid.setWidget(0, 0, new Label(stringMessages.registrationLinkSecret() + ":"));
        secretPanelFormGrid.setWidget(0, 1, secretTextBox);
        if (generateSecretButton != null) {
            secretPanelFormGrid.setWidget(0, 2, generateSecretButton);
        }

        if (!editMode) {
            dialogPanel.add(registrationLinkPanel);
            dialogPanel.add(barcodePanel);
            updateDisplayWidgets();
        } else {
            registrationLinkPanel.setVisible(false);
            barcodePanel.setVisible(false);
        }

        return panel;
    }

    private void updateDisplayWidgets() {
        String baseUrl = GWT.getHostPageBaseURL();
        if (baseUrl.endsWith("/")) {
            baseUrl = baseUrl.substring(0, baseUrl.lastIndexOf("/"));
            baseUrl = baseUrl.substring(0, baseUrl.indexOf("/gwt"));
        }
        Map<String, String> parameters = new HashMap<String, String>();
        parameters.put("regatta_name", regattaName);
        parameters.put("secret", secretTextBox.getValue());
        parameters.put("server", baseUrl);
        String deeplinkUrl = BranchIO.generateLink(BranchIOConstants.OPEN_REGATTA_APP_BRANCHIO, parameters,
                URL::encodeQueryString);
        urlTextBox.setText(deeplinkUrl);
        sailingService.openRegattaRegistrationQrCode(deeplinkUrl, new AsyncCallback<String>() {

            @Override
            public void onFailure(Throwable caught) {
                GWT.log("Qrcode generation failed: ", caught);
            }

            @Override
            public void onSuccess(String result) {
                GWT.log("Qrcode generated for url: " + deeplinkUrl);
                qrCodeImage.setUrl("data:image/png;base64, " + result);
            }

        });
    }

    private static native boolean copyToClipboard() /*-{
        return $doc.execCommand('copy');
    }-*/;

}
