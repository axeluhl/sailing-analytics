package com.sap.sailing.gwt.ui.client.shared.panels;

import com.google.gwt.dom.client.Style;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.Widget;

public class UserStatusPanel extends FlowPanel {

    private final Label userNameLabel;

    private final Label userNameText;

    private final Label userRolesText;
    
    public UserStatusPanel() {
        super();
        userNameLabel = new Label("User:");
        userNameText = new Label("");
        userRolesText = new Label("");
        addFloatingWidget(userNameLabel);
        addFloatingWidget(userNameText);
        addFloatingWidget(userRolesText);
        updateUser();
    }

    private void addFloatingWidget(Widget w) { 
        w.getElement().getStyle().setFloat(Style.Float.LEFT);
        w.getElement().getStyle().setPadding(3, Style.Unit.PX);
        add(w);
    }
    
    private void updateUser() {
//        if(user != null) {
//            userNameText.setText(user.principalName);
//            userRolesText.setText(user.roles.toString());
//        } else {
//            userNameText.setText("Unknown");
//            userRolesText.setText("Unknown");
//        }
//    }
    }
}
