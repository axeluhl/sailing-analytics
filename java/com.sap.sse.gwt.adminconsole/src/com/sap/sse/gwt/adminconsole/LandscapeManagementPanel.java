package com.sap.sse.gwt.adminconsole;

import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.VerticalPanel;
import com.sap.sse.gwt.client.ErrorReporter;
import com.sap.sse.security.ui.client.UserService;

public class LandscapeManagementPanel extends VerticalPanel {
    public LandscapeManagementPanel(StringMessages instance, UserService userService,
            AdminConsoleTableResources tableResources, ErrorReporter errorReporter) {
        add(new Label("This is the Landscape Management Panel"));
        // TODO Auto-generated constructor stub
    }
}
