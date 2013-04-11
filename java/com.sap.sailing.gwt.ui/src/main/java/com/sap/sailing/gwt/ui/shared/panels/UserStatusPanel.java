package com.sap.sailing.gwt.ui.shared.panels;

import com.google.gwt.dom.client.Style;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.Widget;
import com.sap.sailing.gwt.ui.client.ErrorReporter;
import com.sap.sailing.gwt.ui.client.UserManagementServiceAsync;
import com.sap.sailing.gwt.ui.shared.UserDTO;

public class UserStatusPanel extends FlowPanel {

    private UserDTO user;

    private final Label userNameLabel;

    private final Label userNameText;

    private final Label userRolesText;

    private final Button logoutButton;

    public UserStatusPanel(final UserManagementServiceAsync userManagementService, final ErrorReporter errorReporter) {
        super();
        
        userNameLabel = new Label("User:");
        userNameText = new Label("");
        userRolesText = new Label("");
        user = null;

        logoutButton = new Button("Logout");
        
        userManagementService.getUser(new AsyncCallback<UserDTO>() {
            @Override
            public void onFailure(Throwable caught) {
                errorReporter.reportError("Could not read user: " + caught.getMessage());
            }

            @Override
            public void onSuccess(UserDTO result) {
                updateUser(result);
            }
        });
        
        logoutButton.addClickHandler(new ClickHandler() {
            @Override
            public void onClick(ClickEvent click) {
                userManagementService.logoutUser(
                        new AsyncCallback<Void>() {
                    @Override
                    public void onFailure(Throwable caught) {
                        errorReporter.reportError("Error during logout of the user: " + caught.getMessage());
                    }

                    @Override
                    public void onSuccess(Void result) {
                        errorReporter.reportError("Basic authentication does not support a server side 'Logout'. Please close the browser to logout.");
//                        Window.Location.reload();
                    }
                });
            }
        });
        
        addFloatingWidget(userNameLabel);
        addFloatingWidget(userNameText);
        addFloatingWidget(userRolesText);
        add(logoutButton);
        
        updateUser(user);
    }

    private void addFloatingWidget(Widget w) { 
        w.getElement().getStyle().setFloat(Style.Float.LEFT);
        w.getElement().getStyle().setPadding(3, Style.Unit.PX);
        add(w);
    }
    
    private void updateUser(UserDTO newUser) {
        this.user = newUser;
        
        if(user != null) {
            userNameText.setText(user.principalName);
            userRolesText.setText(user.roles.toString());
            logoutButton.setEnabled(false);
        } else {
            userNameText.setText("Unknown");
            userRolesText.setText("Unknown");
            logoutButton.setEnabled(false);
        }
    }
}
