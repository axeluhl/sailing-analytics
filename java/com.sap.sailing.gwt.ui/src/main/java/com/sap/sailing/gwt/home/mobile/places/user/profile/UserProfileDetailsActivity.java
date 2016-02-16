package com.sap.sailing.gwt.home.mobile.places.user.profile;

import com.google.gwt.activity.shared.AbstractActivity;
import com.google.gwt.event.shared.EventBus;
import com.google.gwt.user.client.ui.AcceptsOneWidget;
import com.sap.sailing.gwt.home.mobile.app.MobileApplicationClientFactory;
import com.sap.sailing.gwt.home.shared.partials.userdetails.UserDetailsPresenter;
import com.sap.sailing.gwt.home.shared.places.user.profile.AbstractUserProfilePlace;
import com.sap.sse.security.ui.authentication.AuthenticationContextEvent;

public class UserProfileDetailsActivity extends AbstractActivity implements UserProfileDetailsView.Presenter {

    private final MobileApplicationClientFactory clientFactory;
    
    private final UserProfileDetailsView currentView = new UserProfileDetailsViewImpl(this);

    private UserDetailsPresenter userDetailsPresenter;
    
    public UserProfileDetailsActivity(AbstractUserProfilePlace place, MobileApplicationClientFactory clientFactory) {
        this.clientFactory = clientFactory;
    }

    @Override
    public void start(final AcceptsOneWidget panel, final EventBus eventBus) {
        panel.setWidget(currentView);
        currentView.setAuthenticationContext(clientFactory.getAuthenticationManager().getAuthenticationContext());

        userDetailsPresenter = new UserDetailsPresenter(currentView.getUserDetailsView(),
                clientFactory.getAuthenticationManager(), clientFactory.getUserManagementService(), clientFactory
                        .getNavigator().getMailVerifiedConfirmationNavigation().getTargetUrl());
        
        eventBus.addHandler(AuthenticationContextEvent.TYPE, new AuthenticationContextEvent.Handler() {
            @Override
            public void onUserChangeEvent(AuthenticationContextEvent event) {
                currentView.setAuthenticationContext(event.getCtx());
                userDetailsPresenter.setAuthenticationContext(event.getCtx());
            }
        });
    }
    
    @Override
    public void doTriggerLoginForm() {
        clientFactory.getNavigator().getSignInNavigation().goToPlace();
    }
}
