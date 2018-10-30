package com.sap.sailing.gwt.home.mobile.places.user.profile;

import com.google.gwt.user.client.ui.IsWidget;
import com.sap.sailing.gwt.home.shared.app.PlaceNavigation;
import com.sap.sailing.gwt.home.shared.places.user.profile.AbstractUserProfilePlace;
import com.sap.sse.security.ui.authentication.app.NeedsAuthenticationContext;
import com.sap.sse.security.ui.authentication.decorator.NotLoggedInPresenter;

public interface UserProfileViewBase extends IsWidget, NeedsAuthenticationContext {

    public interface Presenter extends NotLoggedInPresenter {
        PlaceNavigation<? extends AbstractUserProfilePlace> getUserProfileNavigation();
        PlaceNavigation<? extends AbstractUserProfilePlace> getUserPreferencesNavigation();
        PlaceNavigation<? extends AbstractUserProfilePlace> getUserSettingsNavigation();
        PlaceNavigation<? extends AbstractUserProfilePlace> getSailorProfilesNavigation();
    }
}
