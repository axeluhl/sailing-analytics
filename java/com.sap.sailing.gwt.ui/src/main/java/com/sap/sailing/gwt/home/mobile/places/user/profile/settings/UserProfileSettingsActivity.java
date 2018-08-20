package com.sap.sailing.gwt.home.mobile.places.user.profile.settings;

import com.google.gwt.event.shared.EventBus;
import com.google.gwt.user.client.ui.AcceptsOneWidget;
import com.sap.sailing.gwt.home.mobile.app.MobileApplicationClientFactory;
import com.sap.sailing.gwt.home.mobile.places.user.profile.AbstractUserProfileActivity;
import com.sap.sailing.gwt.home.shared.places.user.profile.settings.UserProfileSettingsPlace;
import com.sap.sailing.gwt.home.shared.places.user.profile.settings.UserSettingsPresenter;
import com.sap.sailing.gwt.home.shared.places.user.profile.settings.UserSettingsView;
import com.sap.sse.security.ui.authentication.AuthenticationContextEvent;

public class UserProfileSettingsActivity extends AbstractUserProfileActivity implements UserProfileSettingsView.Presenter {

    private final UserSettingsView.Presenter userSettingsPresenter;
    private final UserProfileSettingsView currentView;
    
    public UserProfileSettingsActivity(UserProfileSettingsPlace place,
            MobileApplicationClientFactory clientFactory) {
        super(clientFactory);
        this.userSettingsPresenter = new UserSettingsPresenter<MobileApplicationClientFactory>(clientFactory);
        this.currentView = new UserProfileSettingsViewImpl(this);
    }

    @Override
    public void start(final AcceptsOneWidget panel, final EventBus eventBus) {
        panel.setWidget(currentView);
        currentView.setAuthenticationContext(clientFactory.getAuthenticationManager().getAuthenticationContext());
        eventBus.addHandler(AuthenticationContextEvent.TYPE, new AuthenticationContextEvent.Handler() {
            @Override
            public void onUserChangeEvent(AuthenticationContextEvent event) {
                currentView.setAuthenticationContext(event.getCtx());
            }
        });
        if (clientFactory.getAuthenticationManager().getAuthenticationContext().isLoggedIn()) {
            userSettingsPresenter.loadData();
        }
    }
    
    @Override
    public UserSettingsView.Presenter getUserSettingsPresenter() {
        return userSettingsPresenter;
    }
    
}
