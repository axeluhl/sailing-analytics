package com.sap.sailing.gwt.home.shared.usermanagement.view;

import com.google.gwt.user.client.ui.AcceptsOneWidget;
import com.google.gwt.user.client.ui.IsWidget;

public interface UserManagementView extends AcceptsOneWidget, IsWidget {
    
    void setHeading(String heading);
    
}
