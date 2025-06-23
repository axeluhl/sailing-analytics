package com.sap.sailing.gwt.ui.adminconsole;

import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.Focusable;
import com.google.gwt.user.client.ui.HorizontalPanel;
import com.google.gwt.user.client.ui.TextBox;
import com.google.gwt.user.client.ui.Widget;
import com.sap.sailing.gwt.ui.client.SailingServiceWriteAsync;
import com.sap.sailing.gwt.ui.client.StringMessages;
import com.sap.sse.gwt.client.Notification;
import com.sap.sse.gwt.client.Notification.NotificationType;
import com.sap.sse.gwt.client.dialog.DataEntryDialog;
import com.sap.sse.gwt.client.dialog.DialogUtils;

public class FreestyleCommandDialog extends DataEntryDialog<Void> {
    private final TextBox commandTextBox;
    private final StringMessages stringMessages;
    private final SailingServiceWriteAsync sailingServiceWrite;
    private final String deviceSerialNumber;

    public FreestyleCommandDialog(StringMessages stringMessages, SailingServiceWriteAsync sailingServiceWrite, String deviceSerialNumber) {
        super(stringMessages.sendCommandsTo(deviceSerialNumber),
                stringMessages.sendCommandsToDescription(deviceSerialNumber), StringMessages.INSTANCE.close(),
                /* cancelButtonName */ null, /* validator */ null, /* animationEnabled */ true, /* callback */ null);
        this.stringMessages = stringMessages;
        this.sailingServiceWrite = sailingServiceWrite;
        this.deviceSerialNumber = deviceSerialNumber;
        commandTextBox = new TextBox();
        commandTextBox.setVisibleLength(30);
        DialogUtils.linkEscapeToButton(getOkButton(), commandTextBox);
    }
    
    
    @Override
    protected Focusable getInitialFocusWidget() {
        return commandTextBox;
    }

    @Override
    protected Widget getAdditionalWidget() {
        final Button sendButton = new Button(stringMessages.send());
        sendButton.addClickHandler(e->sendCommand(commandTextBox.getValue()));
        DialogUtils.linkEnterToButton(sendButton, commandTextBox);
        final HorizontalPanel panel = new HorizontalPanel();
        panel.add(commandTextBox);
        panel.add(sendButton);
        return panel;
    }

    private void sendCommand(String command) {
        sailingServiceWrite.sendIgtimiCommand(deviceSerialNumber, command, new AsyncCallback<Boolean>() {
            @Override
            public void onSuccess(Boolean foundDevice) {
                if (foundDevice) {
                    Notification.notify(stringMessages.ok(), NotificationType.SUCCESS);
                } else {
                    Notification.notify(stringMessages.noLiveConnectionFoundForIgtimiDevice(deviceSerialNumber), NotificationType.WARNING);
                }
            }

            @Override
            public void onFailure(Throwable caught) {
                Notification.notify(caught.getMessage(), NotificationType.ERROR);
            }
        });
    }


    @Override
    protected Void getResult() {
        return null;
    }

}
