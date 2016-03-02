package com.sap.sailing.gwt.home.desktop.places.user.profile.detailstab;

import com.google.gwt.user.client.ui.IsWidget;
import com.sap.sse.security.ui.authentication.app.AuthenticationContext;
import com.sap.sse.security.ui.authentication.app.NeedsAuthenticationContext;
import com.sap.sse.security.ui.userprofile.shared.userdetails.UserDetailsView;

public interface UserProfileDetailsView extends IsWidget {

    void setPresenter(Presenter presenter);
    
    UserDetailsView getUserDetailsView();
    
    NeedsAuthenticationContext getDecorator();

    public interface Presenter {
        void setAuthenticationContext(AuthenticationContext userManagementContext);
    }
}