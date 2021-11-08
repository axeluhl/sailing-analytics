package com.sap.sailing.gwt.home.shared.places.user.profile.sailorprofile.view;

import com.google.gwt.user.client.ui.IsWidget;
import com.sap.sailing.gwt.home.desktop.places.user.profile.sailorprofiletab.SailingProfileOverviewPresenter;
import com.sap.sailing.gwt.home.desktop.places.user.profile.sailorprofiletab.details.SailorProfileDetailsView;
import com.sap.sse.security.ui.authentication.app.NeedsAuthenticationContext;

/**
 * top level interface for sailor profile views on mobile and desktop, abstracts both {@link SailorProfileOverview} and
 * {@link SailorProfileDetailsView} (desktop)
 */
public interface SailorProfileView extends IsWidget {

    void setPresenter(SailingProfileOverviewPresenter presenter);

    NeedsAuthenticationContext authentificationContextConsumer();

}
