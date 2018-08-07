package com.sap.sailing.gwt.home.desktop.places.user.profile.sailorprofiletab;

import com.sap.sailing.gwt.home.desktop.places.user.profile.UserProfileClientFactory;
import com.sap.sailing.gwt.home.desktop.places.user.profile.UserProfileView;
import com.sap.sailing.gwt.home.desktop.places.user.profile.sailorprofiletab.wrapper.SailorProfileWrapperView;
import com.sap.sailing.gwt.home.shared.places.user.profile.sailorprofile.SharedSailorProfilePresenter;
import com.sap.sailing.gwt.home.shared.places.user.profile.sailorprofile.SharedSailorProfileView;
import com.sap.sailing.gwt.home.shared.places.user.profile.sailorprofile.SharedSailorProfileView.Presenter;
import com.sap.sse.security.ui.authentication.app.AuthenticationContext;

public class SailorProfilePresenterImpl implements SailorProfileWrapperView.Presenter {

    private final SailorProfileWrapperView wrapperView;
    private final UserProfileView.Presenter userProfilePresenter;
    private final SharedSailorProfileView.Presenter sharedSailorProfilePresenter;

    public SailorProfilePresenterImpl(final SailorProfileView view, final SailorProfileWrapperView wrapperView,
            final UserProfileView.Presenter userProfilePresenter) {
        this.wrapperView = wrapperView;
        this.userProfilePresenter = userProfilePresenter;
        this.sharedSailorProfilePresenter = new SharedSailorProfilePresenter<UserProfileClientFactory>(
                userProfilePresenter.getClientFactory());
        view.setPresenter(this);
    }

    @Override
    public Presenter getSharedSailorProfilePresenter() {
        return sharedSailorProfilePresenter;
    }

    @Override
    public void setAuthenticationContext(AuthenticationContext authenticationContext) {
        wrapperView.getDecorator().setAuthenticationContext(authenticationContext);
        if (authenticationContext.isLoggedIn()) {
            sharedSailorProfilePresenter.loadPreferences();
        }
    }

    @Override
    public void doTriggerLoginForm() {
        userProfilePresenter.doTriggerLoginForm();
    }

}
