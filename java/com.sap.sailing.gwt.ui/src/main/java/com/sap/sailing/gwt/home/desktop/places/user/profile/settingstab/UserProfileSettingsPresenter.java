package com.sap.sailing.gwt.home.desktop.places.user.profile.settingstab;

import com.sap.sailing.gwt.home.desktop.places.user.profile.UserProfileClientFactory;
import com.sap.sailing.gwt.home.desktop.places.user.profile.UserProfileView;
import com.sap.sailing.gwt.home.shared.places.user.profile.settings.UserSettingsPresenter;
import com.sap.sailing.gwt.home.shared.places.user.profile.settings.UserSettingsView;
import com.sap.sse.security.ui.authentication.app.AuthenticationContext;

public class UserProfileSettingsPresenter implements UserProfileSettingsView.Presenter {

    private final UserProfileSettingsView view;
    private final UserProfileView.Presenter userProfilePresenter;
    private final UserSettingsView.Presenter userSettingsPresenter;
            
    public UserProfileSettingsPresenter(final UserProfileSettingsView view,
            final UserProfileView.Presenter userProfilePresenter) {
        this.view = view;
        this.userProfilePresenter = userProfilePresenter;
        this.userSettingsPresenter = new UserSettingsPresenter<UserProfileClientFactory>(
                userProfilePresenter.getClientFactory());
        view.setPresenter(this);
    }
    
    @Override
    public UserSettingsView.Presenter getUserSettingsPresenter() {
        return userSettingsPresenter;
    }
    
    @Override
    public void setAuthenticationContext(AuthenticationContext authenticationContext) {
        view.getDecorator().setAuthenticationContext(authenticationContext);
        if (authenticationContext.isLoggedIn()) {
            userSettingsPresenter.loadData();
        }
    }
    
    @Override
    public void doTriggerLoginForm() {
        userProfilePresenter.doTriggerLoginForm();
    }
}
