package com.sap.sailing.gwt.home.desktop.places.user.profile.sailorprofiletab;

import com.sap.sailing.gwt.home.shared.places.user.profile.sailorprofile.SharedSailorProfileView;
import com.sap.sailing.gwt.ui.client.FlagImageResolver;
import com.sap.sse.gwt.client.mvp.ClientFactory;
import com.sap.sse.security.ui.authentication.app.NeedsAuthenticationContext;
import com.sap.sse.security.ui.authentication.decorator.NotLoggedInPresenter;

public interface SailingProfileOverviewPresenter extends NotLoggedInPresenter, NeedsAuthenticationContext {
    SharedSailorProfileView.Presenter getSharedSailorProfilePresenter();

    ClientFactory getClientFactory();

    FlagImageResolver getFlagImageResolver();
}
