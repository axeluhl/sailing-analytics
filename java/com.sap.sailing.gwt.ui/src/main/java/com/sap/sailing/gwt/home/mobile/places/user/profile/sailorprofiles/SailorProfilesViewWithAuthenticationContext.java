package com.sap.sailing.gwt.home.mobile.places.user.profile.sailorprofiles;

import com.sap.sailing.gwt.home.shared.places.user.profile.sailorprofile.view.SailorProfileView;
import com.sap.sse.security.ui.authentication.app.AuthenticationContext;

public interface SailorProfilesViewWithAuthenticationContext extends SailorProfileView {

    void setAuthenticationContext(AuthenticationContext authenticationContext);

}
