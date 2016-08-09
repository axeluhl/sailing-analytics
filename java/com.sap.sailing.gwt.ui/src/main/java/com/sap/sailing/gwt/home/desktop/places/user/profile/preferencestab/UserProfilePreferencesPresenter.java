package com.sap.sailing.gwt.home.desktop.places.user.profile.preferencestab;

import com.sap.sailing.gwt.home.desktop.places.user.profile.UserProfileClientFactory;
import com.sap.sailing.gwt.home.desktop.places.user.profile.UserProfileView;
import com.sap.sailing.gwt.home.shared.places.user.profile.preferences.UserPreferencesPresenter;
import com.sap.sailing.gwt.home.shared.places.user.profile.preferences.UserPreferencesView;
import com.sap.sailing.gwt.home.shared.places.user.profile.preferences.UserPreferencesView.Presenter;
import com.sap.sse.security.ui.authentication.app.AuthenticationContext;

public class UserProfilePreferencesPresenter implements UserProfilePreferencesView.Presenter {

    private final UserProfilePreferencesView view;
    private final UserProfileView.Presenter userProfilePresenter;
    private final UserPreferencesView.Presenter userPreferencesPresenter;
            
    public UserProfilePreferencesPresenter(final UserProfilePreferencesView view,
            final UserProfileView.Presenter userProfilePresenter) {
        this.view = view;
        this.userProfilePresenter = userProfilePresenter;
        this.userPreferencesPresenter = new UserPreferencesPresenter<UserProfileClientFactory>(
                userProfilePresenter.getClientFactory());
        view.setPresenter(this);
    }
    
    @Override
    public Presenter getUserPreferencesPresenter() {
        return userPreferencesPresenter;
    }
    
    @Override
    public void setAuthenticationContext(AuthenticationContext authenticationContext) {
        view.getDecorator().setAuthenticationContext(authenticationContext);
        if (authenticationContext.isLoggedIn()) {
            userPreferencesPresenter.loadPreferences();
        }
    }
    
    @Override
    public void doTriggerLoginForm() {
        userProfilePresenter.doTriggerLoginForm();
    }
    
}
