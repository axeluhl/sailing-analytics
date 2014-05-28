package com.sap.sailing.gwt.ui.client.shared.panels;

import com.google.gwt.dom.client.Style;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.Widget;
import com.sap.sailing.gwt.ui.client.ErrorReporter;

public class UserStatusPanel extends FlowPanel {

    private final Label userNameLabel;

    private final Label userNameText;

    private final Label userRolesText;
    
    public UserStatusPanel(final ErrorReporter errorReporter) {
        super();
        userNameLabel = new Label("User:");
        userNameText = new Label("");
        userRolesText = new Label("");
//        userManagementService.getUser(new AsyncCallback<UserDTO>() {
//            @Override
//            public void onFailure(Throwable caught) {
//                errorReporter.reportError("Could not read user: " + caught.getMessage());
//            }
//
//            @Override
//            public void onSuccess(UserDTO result) {
//                updateUser(result);
//            }
//        });
        addFloatingWidget(userNameLabel);
        addFloatingWidget(userNameText);
        addFloatingWidget(userRolesText);
//        updateUser(user);
    }

    private void addFloatingWidget(Widget w) { 
        w.getElement().getStyle().setFloat(Style.Float.LEFT);
        w.getElement().getStyle().setPadding(3, Style.Unit.PX);
        add(w);
    }
    
//    private void updateUser(UserDTO newUser) {
//        this.user = newUser;
//        
//        if(user != null) {
//            userNameText.setText(user.principalName);
//            userRolesText.setText(user.roles.toString());
//        } else {
//            userNameText.setText("Unknown");
//            userRolesText.setText("Unknown");
//        }
//    }
}
