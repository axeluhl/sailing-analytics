package com.sap.sailing.gwt.home.mobile.places.user.profile.settings;

import com.sap.sailing.gwt.home.mobile.places.user.profile.AbstractUserProfileView;
import com.sap.sailing.gwt.home.shared.places.user.profile.settings.UserSettingsView;

public class UserProfileSettingsViewImpl extends AbstractUserProfileView implements UserProfileSettingsView {

    private final UserSettingsView userPreferencesView;
    
    public UserProfileSettingsViewImpl(UserProfileSettingsView.Presenter presenter) {
        super(presenter);
        this.userPreferencesView = new UserSettings(presenter.getUserSettingsPresenter());
        this.setViewContent(userPreferencesView);
    }
    
}
