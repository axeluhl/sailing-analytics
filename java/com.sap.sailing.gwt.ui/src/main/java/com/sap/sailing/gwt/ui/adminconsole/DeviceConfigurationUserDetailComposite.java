package com.sap.sailing.gwt.ui.adminconsole;

import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.DialogBox;
import com.google.gwt.user.client.ui.Grid;
import com.google.gwt.user.client.ui.HorizontalPanel;
import com.sap.sailing.gwt.ui.client.SailingServiceAsync;
import com.sap.sailing.gwt.ui.client.StringMessages;
import com.sap.sse.gwt.client.ErrorReporter;

public class DeviceConfigurationUserDetailComposite extends DeviceConfigurationDetailComposite {

    public DeviceConfigurationUserDetailComposite(SailingServiceAsync sailingService, ErrorReporter errorReporter,
            StringMessages stringMessages, DeviceConfigurationCloneListener listener) {
        super(sailingService, errorReporter, stringMessages, listener);
    }
    
    protected void setupIdentifier(Grid grid, int gridRow) {
        super.setupIdentifier(grid, gridRow);
        
        HorizontalPanel panel = new HorizontalPanel();
        panel.add(identifierBox);
        
        Button qrCodeButton = new Button("QR-Sync");
        qrCodeButton.addClickHandler(new ClickHandler() {
            @Override
            public void onClick(ClickEvent event) {
                if (identifierBox.getValue() == null || identifierBox.getValue().isEmpty()) {
                    Window.alert("There is no identifier set.");
                } else {
                    DialogBox dialog = new DeviceConfigurationQRIdentifierDialog(identifierBox.getValue(), stringMessages);
                    dialog.show();
                    dialog.center();
                }
            }
        });
        panel.add(qrCodeButton);
        grid.setWidget(gridRow, 1, panel);
    }
}
