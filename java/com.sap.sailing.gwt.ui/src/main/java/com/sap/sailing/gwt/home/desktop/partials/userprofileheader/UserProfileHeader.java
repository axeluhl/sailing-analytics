package com.sap.sailing.gwt.home.desktop.partials.userprofileheader;

import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.Label;
import com.sap.sailing.gwt.home.shared.app.UserManagementContext;

public class UserProfileHeader extends Composite {
    
    public UserProfileHeader() {
        initWidget(new Label("TODO implement User Profile Header"));
    }

    public void setUserManagementContext(UserManagementContext userManagementContext) {
        if(userManagementContext.isLoggedIn()) {
            ((Label)getWidget()).setText(userManagementContext.getCurrentUser().getName());
        } else {
            ((Label)getWidget()).setText("TODO: anonymous");
        }
    }

}
