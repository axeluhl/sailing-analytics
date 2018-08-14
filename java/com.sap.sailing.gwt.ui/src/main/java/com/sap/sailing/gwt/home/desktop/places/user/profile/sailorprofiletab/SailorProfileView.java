package com.sap.sailing.gwt.home.desktop.places.user.profile.sailorprofiletab;

import com.google.gwt.user.client.ui.IsWidget;
import com.sap.sse.security.ui.authentication.app.NeedsAuthenticationContext;

public interface SailorProfileView extends IsWidget {

    void setPresenter(SailingProfileOverviewPresenter presenter);

    NeedsAuthenticationContext getAuthenticationContext();

}
