package com.sap.sailing.landscape.ui.client;

import com.google.gwt.user.client.ui.Anchor;
import com.google.gwt.user.client.ui.Grid;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.TextArea;
import com.google.gwt.user.client.ui.Widget;
import com.sap.sailing.landscape.ui.client.i18n.StringMessages;
import com.sap.sse.common.Base64Utils;
import com.sap.sse.gwt.client.dialog.DataEntryDialog;

public class SshKeyDisplayAndDownloadDialog extends DataEntryDialog<Void> {
    private final TextArea publicKeyTextArea;
    private final TextArea encryptedPrivateKeyTextArea;
    private final Anchor publicKeyDownloadLink;
    private final Anchor encryptedPrivateKeyDownloadLink;
    private final StringMessages stringMessages;
    
    public SshKeyDisplayAndDownloadDialog(String keyName, byte[] publicKey, byte[] encryptedPrivateKey, StringMessages stringMessages) {
        super(stringMessages.sshKeys(), stringMessages.sshKeys(), stringMessages.close(), /* cancelButtonName */ null, /* validator */ null, /* callback */ null);
        this.stringMessages = stringMessages;
        publicKeyTextArea = new TextArea();
        publicKeyTextArea.setSize("40em", "6em");
        publicKeyTextArea.setText(new String(publicKey));
        encryptedPrivateKeyTextArea = new TextArea();
        encryptedPrivateKeyTextArea.setSize("40em", "6em");
        encryptedPrivateKeyTextArea.setText(new String(encryptedPrivateKey));
        publicKeyDownloadLink = new Anchor(stringMessages.download());
        publicKeyDownloadLink.getElement().setAttribute("download", keyName+".pub");
        publicKeyDownloadLink.setHref("data:application/octet-stream;charset=utf-8;base64,"+Base64Utils.toBase64(publicKey));
        encryptedPrivateKeyDownloadLink = new Anchor(stringMessages.download());
        encryptedPrivateKeyDownloadLink.getElement().setAttribute("download", keyName);
        encryptedPrivateKeyDownloadLink.setHref("data:application/octet-stream;charset=utf-8;base64,"+Base64Utils.toBase64(encryptedPrivateKey));
    }

    @Override
    protected Widget getAdditionalWidget() {
        final Grid result = new Grid(4, 2);
        int row=0;
        result.setWidget(row++, 0, new Label(stringMessages.publicKey()));
        result.setWidget(row, 0, publicKeyTextArea);
        result.setWidget(row++, 1, publicKeyDownloadLink);
        result.setWidget(row++, 0, new Label(stringMessages.encryptedPrivateKey()));
        result.setWidget(row, 0, encryptedPrivateKeyTextArea);
        result.setWidget(row++, 1, encryptedPrivateKeyDownloadLink);
        return result;
    }

    @Override
    protected Void getResult() {
        return null;
    }
}
