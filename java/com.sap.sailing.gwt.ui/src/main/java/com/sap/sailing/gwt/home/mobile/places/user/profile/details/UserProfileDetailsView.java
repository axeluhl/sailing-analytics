package com.sap.sailing.gwt.home.mobile.places.user.profile.details;

import com.sap.sailing.gwt.home.mobile.places.user.profile.UserProfileViewBase;
import com.sap.sse.security.ui.userprofile.shared.userdetails.UserDetailsView;

public interface UserProfileDetailsView extends UserProfileViewBase {
    
    UserDetailsView getUserDetailsView();

    public interface Presenter extends UserProfileViewBase.Presenter {
    }
}

