package com.sap.sailing.gwt.home.desktop.places.user.profile.sailorprofiletab.details;

import com.sap.sailing.gwt.home.desktop.places.user.profile.sailorprofiletab.SailorProfileView;
import com.sap.sse.security.ui.authentication.app.NeedsAuthenticationContext;

public interface SailorProfileDetailsView extends SailorProfileView {

    NeedsAuthenticationContext getDecorator();

}
