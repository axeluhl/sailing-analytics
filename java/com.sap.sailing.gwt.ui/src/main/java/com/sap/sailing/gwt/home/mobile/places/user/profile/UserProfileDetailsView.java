package com.sap.sailing.gwt.home.mobile.places.user.profile;

import com.google.gwt.user.client.ui.IsWidget;
import com.sap.sailing.gwt.home.mobile.partials.userdetails.UserDetails;
import com.sap.sailing.gwt.home.shared.usermanagement.decorator.NotLoggedInPresenter;
import com.sap.sse.security.ui.authentication.app.AuthenticationContext;

public interface UserProfileDetailsView extends IsWidget {
    
    void setUserManagementContext(AuthenticationContext userManagementContext); 

    public interface Presenter extends UserDetails.Presenter, NotLoggedInPresenter {
    }
}

