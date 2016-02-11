package com.sap.sailing.gwt.home.mobile.places.user.profile;

import com.google.gwt.user.client.ui.IsWidget;
import com.sap.sailing.gwt.home.shared.partials.userdetails.UserDetailsView;
import com.sap.sse.security.ui.authentication.app.AuthenticationContext;
import com.sap.sse.security.ui.authentication.decorator.NotLoggedInPresenter;

public interface UserProfileDetailsView extends IsWidget {
    
    void setUserManagementContext(AuthenticationContext userManagementContext); 
    
    UserDetailsView getUserDetailsView();

    public interface Presenter extends NotLoggedInPresenter {
    }
}

