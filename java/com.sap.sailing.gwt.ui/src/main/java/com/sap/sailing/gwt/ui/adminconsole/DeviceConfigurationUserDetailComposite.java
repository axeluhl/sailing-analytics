package com.sap.sailing.gwt.ui.adminconsole;

import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.DialogBox;
import com.google.gwt.user.client.ui.Grid;
import com.google.gwt.user.client.ui.HorizontalPanel;
import com.sap.sailing.gwt.ui.client.SailingServiceAsync;
import com.sap.sailing.gwt.ui.client.StringMessages;
import com.sap.sse.gwt.client.ErrorReporter;
import com.sap.sse.gwt.client.Notification;
import com.sap.sse.gwt.client.Notification.NotificationType;
import com.sap.sse.gwt.client.async.MarkedAsyncCallback;
import com.sap.sse.security.shared.dto.UserDTO;
import com.sap.sse.security.ui.client.UserService;

public class DeviceConfigurationUserDetailComposite extends DeviceConfigurationDetailComposite {

    private final UserService userService;

    public DeviceConfigurationUserDetailComposite(SailingServiceAsync sailingService, UserService userService,
            ErrorReporter errorReporter, StringMessages stringMessages, DeviceConfigurationCloneListener listener) {
        super(sailingService, errorReporter, stringMessages, listener);
        this.userService = userService;
    }
    
    protected void setupIdentifier(Grid grid, int gridRow) {
        super.setupIdentifier(grid, gridRow);
        
        HorizontalPanel panel = new HorizontalPanel();
        panel.add(identifierBox);
        
        Button qrCodeButton = new Button(stringMessages.qrSync());
        qrCodeButton.addClickHandler(new ClickHandler() {
            @Override
            public void onClick(ClickEvent event) {
                if (identifierBox.getValue() == null || identifierBox.getValue().isEmpty()) {
                    Notification.notify(stringMessages.thereIsNoIdentifierSet(), NotificationType.ERROR);
                } else {
                    UserDTO currentUser = userService.getCurrentUser();
                    if (currentUser == null) {
                        createAndShowDialogForAccessToken(/* accessToken */ null);
                    } else {
                        userService.getUserManagementService().getOrCreateAccessToken(currentUser.getName(),
                                new MarkedAsyncCallback<>(new AsyncCallback<String>() {
                            @Override
                            public void onFailure(Throwable caught) {
                                errorReporter.reportError(stringMessages.couldNotObtainAccessTokenForUser(caught.getMessage()), /* silentMode */ true);
                            }
    
                            @Override
                            public void onSuccess(String accessToken) {
                                createAndShowDialogForAccessToken(accessToken);
                            }
                        }));
                    }
                }
            }
        });
        panel.add(qrCodeButton);
        grid.setWidget(gridRow, 1, panel);
    }
    
    private void createAndShowDialogForAccessToken(String accessToken) {
        final DialogBox dialog = new DeviceConfigurationQRIdentifierDialog(identifierBox.getValue(), stringMessages, accessToken);
        dialog.show();
        dialog.center();
    }
}
