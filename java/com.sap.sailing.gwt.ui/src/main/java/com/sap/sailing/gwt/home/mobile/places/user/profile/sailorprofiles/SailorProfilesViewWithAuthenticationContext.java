package com.sap.sailing.gwt.home.mobile.places.user.profile.sailorprofiles;

import com.sap.sailing.gwt.home.desktop.places.user.profile.sailorprofiletab.SailorProfileView;
import com.sap.sse.security.ui.authentication.app.AuthenticationContext;

public interface SailorProfilesViewWithAuthenticationContext extends SailorProfileView {

    void setAuthenticationContext(AuthenticationContext authenticationContext);

}
