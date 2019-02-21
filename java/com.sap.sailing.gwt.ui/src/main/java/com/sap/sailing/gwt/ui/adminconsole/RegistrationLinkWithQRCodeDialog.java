package com.sap.sailing.gwt.ui.adminconsole;

import java.util.HashMap;
import java.util.Map;

import com.google.gwt.core.client.GWT;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.http.client.URL;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.Anchor;
import com.google.gwt.user.client.ui.CaptionPanel;
import com.google.gwt.user.client.ui.Image;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.TextBox;
import com.google.gwt.user.client.ui.VerticalPanel;
import com.google.gwt.user.client.ui.Widget;
import com.sap.sailing.domain.common.BranchIO;
import com.sap.sailing.domain.common.BranchIOConstants;
import com.sap.sailing.gwt.ui.client.SailingServiceAsync;
import com.sap.sailing.gwt.ui.client.StringMessages;
import com.sap.sse.gwt.client.dialog.DataEntryDialog;

public class RegistrationLinkWithQRCodeDialog extends DataEntryDialog<RegistrationLinkWithQRCode> {

    private final String regattaName;

    private final SailingServiceAsync sailingService;
    private final StringMessages stringMessages;
    private final CaptionPanel registrationLinkPanel;
    private final VerticalPanel registrationLinkPanelContent;
    private final Label registrationLinkExplain;
    private final CaptionPanel barcodePanel;
    private final VerticalPanel barcodePanelContent;
    private final Label barcodeExplainLabel;
    private final TextBox urlTextBox;
    private final Image qrCodeImage;
    private final String secret;

    private RegistrationLinkWithQRCode registrationLinkWithQRCode;

    public RegistrationLinkWithQRCodeDialog(final SailingServiceAsync sailingService,
            final StringMessages stringMessages, String regattaName,
            RegistrationLinkWithQRCode registrationLinkWithQRCode,
            DialogCallback<RegistrationLinkWithQRCode> callback, final String secret) {
        super(stringMessages.registrationLinkDialog(), stringMessages.explainRegistrationLinkDialog(),
                stringMessages.ok(), stringMessages.cancel(), null, true, callback);
        this.sailingService = sailingService;
        this.stringMessages = stringMessages;
        this.regattaName = regattaName;
        this.registrationLinkWithQRCode = registrationLinkWithQRCode == null ? new RegistrationLinkWithQRCode()
                : registrationLinkWithQRCode;


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
        this.secret = secret;
    }

    @Override
    protected RegistrationLinkWithQRCode getResult() {
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

        dialogPanel.add(registrationLinkPanel);
        dialogPanel.add(barcodePanel);
        updateDisplayWidgets();
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
        parameters.put("secret", secret);
        parameters.put("server", baseUrl);
        String deeplinkUrl = BranchIO.generateLink(BranchIOConstants.OPEN_REGATTA_2_APP_BRANCHIO, parameters,
                URL::encodeQueryString);
        urlTextBox.setText(deeplinkUrl);
        // FIXME: connect to back end to check if app version 1.0 or 2.0 are configured
        if (true) {
            getStatusLabel().setText(stringMessages.warningSailInsightVersion());
            getStatusLabel().setStyleName("errorLabel");
        }
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
