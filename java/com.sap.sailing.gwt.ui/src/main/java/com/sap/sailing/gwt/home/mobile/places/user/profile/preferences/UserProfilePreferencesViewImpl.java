package com.sap.sailing.gwt.home.mobile.places.user.profile.preferences;

import com.sap.sailing.gwt.home.mobile.places.user.profile.AbstractUserProfileView;
import com.sap.sailing.gwt.home.shared.places.user.profile.preferences.UserPreferences;
import com.sap.sailing.gwt.home.shared.places.user.profile.preferences.UserPreferencesView;
import com.sap.sailing.gwt.ui.client.FlagImageResolver;

public class UserProfilePreferencesViewImpl extends AbstractUserProfileView implements UserProfilePreferencesView {

    private final UserPreferencesView userPreferencesView;
    
    public UserProfilePreferencesViewImpl(UserProfilePreferencesView.Presenter presenter, FlagImageResolver flagImageResolver) {
        super(presenter);
        this.userPreferencesView = new UserPreferences(presenter.getUserPreferencesPresenter(), flagImageResolver);
        this.userPreferencesView.setEdgeToEdge(true);
        this.setViewContent(userPreferencesView);
    }
    
}
