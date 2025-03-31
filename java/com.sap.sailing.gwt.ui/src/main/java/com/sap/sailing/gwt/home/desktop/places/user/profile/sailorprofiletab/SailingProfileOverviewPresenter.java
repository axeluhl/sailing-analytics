package com.sap.sailing.gwt.home.desktop.places.user.profile.sailorprofiletab;

import java.util.UUID;

import com.sap.sailing.gwt.home.shared.places.user.profile.sailorprofile.ClientFactoryWithDispatchAndErrorAndUserService;
import com.sap.sailing.gwt.home.shared.places.user.profile.sailorprofile.EditSailorProfileDetailsView;
import com.sap.sailing.gwt.ui.client.FlagImageResolver;
import com.sap.sse.security.ui.authentication.app.NeedsAuthenticationContext;
import com.sap.sse.security.ui.authentication.decorator.NotLoggedInPresenter;

public interface SailingProfileOverviewPresenter extends NotLoggedInPresenter, NeedsAuthenticationContext {
    EditSailorProfileDetailsView.Presenter getSharedSailorProfilePresenter();

    ClientFactoryWithDispatchAndErrorAndUserService getClientFactory();

    FlagImageResolver getFlagImageResolver();

    void removeSailorProfile(UUID uuid);
}
