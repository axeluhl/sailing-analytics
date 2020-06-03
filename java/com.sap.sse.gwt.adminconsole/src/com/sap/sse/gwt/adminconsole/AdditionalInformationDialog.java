package com.sap.sse.gwt.adminconsole;

import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.VerticalPanel;
import com.google.gwt.user.client.ui.Widget;
import com.sap.sse.gwt.client.ServerInfoDTO;
import com.sap.sse.gwt.client.dialog.DataEntryDialog;

public class AdditionalInformationDialog extends DataEntryDialog<String> {

    private final StringMessages stringMessages;
    private final ServerInfoDTO serverInfo;

    public AdditionalInformationDialog(String title, String message, String okButtonName, String cancelButtonName,
            Validator<String> validator, boolean animationEnabled, DialogCallback<String> callback,
            StringMessages stringMessages, ServerInfoDTO serverInfo) {
        super(title, message, okButtonName, cancelButtonName, validator, animationEnabled, callback);
        this.stringMessages = stringMessages;
        this.serverInfo = serverInfo;
    }

    @Override
    protected Widget getAdditionalWidget() {
        VerticalPanel mainPanel = new VerticalPanel();
        Label nameLabel = new Label(stringMessages.serverName(serverInfo.getName()));
        Label versionLabel = new Label(stringMessages.version(serverInfo.getBuildVersion()));
        mainPanel.add(nameLabel);
        mainPanel.add(versionLabel);
        return mainPanel;
    }

    @Override
    protected String getResult() {
        return null;
    }

}
