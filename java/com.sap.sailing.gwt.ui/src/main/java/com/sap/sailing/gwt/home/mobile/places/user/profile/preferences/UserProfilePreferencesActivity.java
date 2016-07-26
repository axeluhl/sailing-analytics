package com.sap.sailing.gwt.home.mobile.places.user.profile.preferences;

import com.google.gwt.activity.shared.AbstractActivity;
import com.google.gwt.event.shared.EventBus;
import com.google.gwt.user.client.ui.AcceptsOneWidget;
import com.sap.sailing.gwt.home.mobile.app.MobileApplicationClientFactory;
import com.sap.sailing.gwt.home.shared.app.PlaceNavigation;
import com.sap.sailing.gwt.home.shared.places.user.profile.AbstractUserProfilePlace;
import com.sap.sailing.gwt.home.shared.places.user.profile.preferences.UserPreferencesPresenter;
import com.sap.sailing.gwt.home.shared.places.user.profile.preferences.UserPreferencesView;
import com.sap.sailing.gwt.home.shared.places.user.profile.preferences.UserPreferencesView.Presenter;
import com.sap.sailing.gwt.home.shared.places.user.profile.preferences.UserProfilePreferencesPlace;
import com.sap.sse.security.ui.authentication.AuthenticationContextEvent;

public class UserProfilePreferencesActivity extends AbstractActivity implements UserProfilePreferencesView.Presenter {

    private final MobileApplicationClientFactory clientFactory;
    private final UserPreferencesView.Presenter userPreferencesPresenter;
    private final UserProfilePreferencesView currentView;
    
    public UserProfilePreferencesActivity(UserProfilePreferencesPlace place,
            MobileApplicationClientFactory clientFactory) {
        this.clientFactory = clientFactory;
        this.userPreferencesPresenter = new UserPreferencesPresenter<MobileApplicationClientFactory>(clientFactory);
        this.currentView = new UserProfilePreferencesViewImpl(this);
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
    
    @Override
    public void doTriggerLoginForm() {
        clientFactory.getNavigator().getSignInNavigation().goToPlace();
    }
    
    @Override
    public PlaceNavigation<? extends AbstractUserProfilePlace> getUserProfileNavigation() {
        return clientFactory.getNavigator().getUserProfileNavigation();
    }
    
    @Override
    public PlaceNavigation<? extends AbstractUserProfilePlace> getUserPreferencesNavigation() {
        return clientFactory.getNavigator().getUserPreferencesNavigation();
    }
}
