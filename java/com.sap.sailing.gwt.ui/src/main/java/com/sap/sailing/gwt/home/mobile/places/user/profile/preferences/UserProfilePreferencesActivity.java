package com.sap.sailing.gwt.home.mobile.places.user.profile.preferences;

import com.google.gwt.event.shared.EventBus;
import com.google.gwt.user.client.ui.AcceptsOneWidget;
import com.sap.sailing.gwt.home.mobile.app.MobileApplicationClientFactory;
import com.sap.sailing.gwt.home.mobile.places.user.profile.AbstractUserProfileActivity;
import com.sap.sailing.gwt.home.shared.places.user.profile.preferences.UserPreferencesPresenter;
import com.sap.sailing.gwt.home.shared.places.user.profile.preferences.UserPreferencesView;
import com.sap.sailing.gwt.home.shared.places.user.profile.preferences.UserPreferencesView.Presenter;
import com.sap.sailing.gwt.ui.client.FlagImageResolver;
import com.sap.sailing.gwt.home.shared.places.user.profile.preferences.UserProfilePreferencesPlace;
import com.sap.sse.security.ui.authentication.AuthenticationContextEvent;

public class UserProfilePreferencesActivity extends AbstractUserProfileActivity implements UserProfilePreferencesView.Presenter {

    private final UserPreferencesView.Presenter userPreferencesPresenter;
    private final UserProfilePreferencesView currentView;
    
    public UserProfilePreferencesActivity(UserProfilePreferencesPlace place,
            MobileApplicationClientFactory clientFactory, FlagImageResolver flagImageResolver) {
        super(clientFactory);
        this.userPreferencesPresenter = new UserPreferencesPresenter<MobileApplicationClientFactory>(clientFactory);
        this.currentView = new UserProfilePreferencesViewImpl(this, flagImageResolver);
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
            userPreferencesPresenter.loadPreferences();
        }
    }
    
    @Override
    public Presenter getUserPreferencesPresenter() {
        return userPreferencesPresenter;
    }
    
}
