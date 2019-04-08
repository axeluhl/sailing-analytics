package com.sap.sailing.gwt.ui.adminconsole;

import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.http.client.URL;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.DialogBox;
import com.google.gwt.user.client.ui.HorizontalPanel;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.TextBox;
import com.google.gwt.user.client.ui.VerticalPanel;
import com.sap.sailing.domain.common.impl.DeviceConfigurationQRCodeUtils;
import com.sap.sailing.gwt.ui.client.StringMessages;
import com.sap.sse.gwt.client.Notification;
import com.sap.sse.gwt.client.Notification.NotificationType;

public class DeviceConfigurationQRIdentifierDialog extends DialogBox {
    public static final String rcAppApkPath = "/apps/com.sap.sailing.racecommittee.app.apk";
    private static final int qrCodeSize = 320;

    private class DeviceConfigurationQRIdentifierWidget extends BaseQRIdentifierWidget {
        private final TextBox identifierBox;
        private final StringMessages stringMessages;
        private final String accessToken;
        
        public DeviceConfigurationQRIdentifierWidget(String identifier, String accessToken, StringMessages stringMessages) {
            super(qrCodeSize, stringMessages);
            this.accessToken = accessToken;
            identifierBox = new TextBox();
            identifierBox.setValue(identifier);
            identifierBox.setReadOnly(true);
            identifierBox.setVisibleLength(40);
            inputGrid.resize(2, 2);
            inputGrid.setWidget(1, 0, new Label("Identifier:"));
            inputGrid.setWidget(1, 1, identifierBox);
            this.stringMessages = stringMessages;
        }
        
        @Override
        protected String generateEncodedQRCodeContent() {
            if (identifierBox.getValue().contains("#")) {
                Notification.notify(stringMessages.notCapableOfGeneratingACodeForIdentifier(), NotificationType.ERROR);
            } else if (!identifierBox.getValue().isEmpty() && !serverBox.getValue().isEmpty()) {
                String apkUrl = getServerUrlWithoutFinalSlash() + rcAppApkPath;
                return DeviceConfigurationQRCodeUtils.composeQRContent(URL.encodeQueryString(identifierBox.getValue()), apkUrl, URL.encodeQueryString(accessToken));
            }
            return null;
        }   
    }
    
    private final DeviceConfigurationQRIdentifierWidget widget;
    
    public DeviceConfigurationQRIdentifierDialog(String identifier, StringMessages stringMessages, String accessToken) {
        widget = new DeviceConfigurationQRIdentifierWidget(identifier, accessToken, stringMessages);
        Button exitButton = new Button(stringMessages.close());
        exitButton.addClickHandler(new ClickHandler() {
            @Override
            public void onClick(ClickEvent event) {
                hide();
            }
        });
        
        HorizontalPanel actionPanel = new HorizontalPanel();
        actionPanel.add(exitButton);
        
        VerticalPanel panel = new VerticalPanel();
        panel.add(widget);
        panel.add(actionPanel);
        setWidget(panel);
    }
    
    @Override
    protected void onAttach() {
        super.onAttach();
        widget.generateQRCode();
    }   
}
