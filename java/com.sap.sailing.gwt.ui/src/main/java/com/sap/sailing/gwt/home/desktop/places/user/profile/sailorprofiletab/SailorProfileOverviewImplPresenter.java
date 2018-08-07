package com.sap.sailing.gwt.home.desktop.places.user.profile.sailorprofiletab;

import com.sap.sailing.gwt.home.desktop.places.user.profile.UserProfileClientFactory;
import com.sap.sailing.gwt.home.desktop.places.user.profile.UserProfileView;
import com.sap.sailing.gwt.home.shared.places.user.profile.sailorprofile.SharedSailorProfilePresenter;
import com.sap.sailing.gwt.home.shared.places.user.profile.sailorprofile.SharedSailorProfileView;
import com.sap.sailing.gwt.home.shared.places.user.profile.sailorprofile.SharedSailorProfileView.Presenter;
import com.sap.sse.gwt.client.mvp.ClientFactory;
import com.sap.sse.security.ui.authentication.app.AuthenticationContext;

public class SailorProfileOverviewImplPresenter implements SailingProfileOverviewPresenter {

    private final SailorProfileView view;
    private final UserProfileView.Presenter userProfilePresenter;
    private final SharedSailorProfileView.Presenter sharedSailorProfilePresenter;

    public SailorProfileOverviewImplPresenter(final SailorProfileView view,
            final UserProfileView.Presenter userProfilePresenter) {
        this.view = view;
        this.userProfilePresenter = userProfilePresenter;
        this.sharedSailorProfilePresenter = new SharedSailorProfilePresenter<UserProfileClientFactory>(
                userProfilePresenter.getClientFactory());
        view.setPresenter(this);
    }

    @Override
    public ClientFactory getClientFactory() {
        return userProfilePresenter.getClientFactory();
    }

    @Override
    public Presenter getSharedSailorProfilePresenter() {
        return sharedSailorProfilePresenter;
    }

    @Override
    public void setAuthenticationContext(AuthenticationContext authenticationContext) {
        if (authenticationContext.isLoggedIn() && view instanceof SailorProfileOverview) {
            ((SailorProfileOverview) view)
                    .setProfileList(sharedSailorProfilePresenter.getDataProvider().loadSailorProfiles());
        }
    }

    @Override
    public void doTriggerLoginForm() {
        userProfilePresenter.doTriggerLoginForm();
    }

}
