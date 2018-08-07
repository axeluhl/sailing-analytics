package com.sap.sailing.gwt.home.desktop.places.user.profile.sailorprofiletab.details;

import com.sap.sailing.gwt.home.desktop.places.user.profile.sailorprofiletab.wrapper.SailorProfileOverviewWrapperView;
import com.sap.sse.security.ui.authentication.app.NeedsAuthenticationContext;

public interface SailorProfileDetailsView {

    void setPresenter(SailorProfileOverviewWrapperView.Presenter presenter);

    NeedsAuthenticationContext getDecorator();

}
