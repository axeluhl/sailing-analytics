package com.sap.sailing.gwt.home.mobile.places.user.profile;

import com.google.gwt.user.client.ui.IsWidget;
import com.sap.sailing.gwt.home.mobile.partials.userdetails.UserDetails;
import com.sap.sailing.gwt.home.shared.app.UserManagementContext;
import com.sap.sailing.gwt.home.shared.usermanagement.decorator.NotLoggedInPresenter;

public interface UserProfileDetailsView extends IsWidget {
    
    void setUserManagementContext(UserManagementContext userManagementContext); 

    public interface Presenter extends UserDetails.Presenter, NotLoggedInPresenter {
    }
}

