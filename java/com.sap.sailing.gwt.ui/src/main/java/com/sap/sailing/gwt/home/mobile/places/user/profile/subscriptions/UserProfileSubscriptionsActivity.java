package com.sap.sailing.gwt.home.mobile.places.user.profile.subscriptions;

import com.google.gwt.event.shared.EventBus;
import com.google.gwt.user.client.ui.AcceptsOneWidget;
import com.sap.sailing.gwt.home.mobile.app.MobileApplicationClientFactory;
import com.sap.sailing.gwt.home.mobile.places.user.profile.AbstractUserProfileActivity;
import com.sap.sailing.gwt.home.shared.places.user.profile.subscriptions.UserProfileSubscriptionsPlace;
import com.sap.sailing.gwt.home.shared.places.user.profile.subscriptions.UserSubscriptionsPresenter;
import com.sap.sailing.gwt.home.shared.places.user.profile.subscriptions.UserSubscriptionsView;
import com.sap.sse.security.ui.authentication.AuthenticationContextEvent;

public class UserProfileSubscriptionsActivity extends AbstractUserProfileActivity
        implements UserProfileSubscriptionsView.Presenter {

    private final UserSubscriptionsView.Presenter userSubscriptionsPresenter;
    private final UserProfileSubscriptionsView currentView;

    public UserProfileSubscriptionsActivity(final UserProfileSubscriptionsPlace place,
            final MobileApplicationClientFactory clientFactory) {
        super(clientFactory);
        this.userSubscriptionsPresenter = new UserSubscriptionsPresenter<>(clientFactory,
                clientFactory.getNavigator().getSubscriptionsNavigation());
        this.currentView = new UserProfileSubscriptionsViewImpl(this);
    }

    @Override
    public void start(final AcceptsOneWidget panel, final EventBus eventBus) {
        panel.setWidget(currentView);
        currentView.setAuthenticationContext(clientFactory.getAuthenticationManager().getAuthenticationContext());
        eventBus.addHandler(AuthenticationContextEvent.TYPE,
                event -> currentView.setAuthenticationContext(event.getCtx()));
        if (clientFactory.getAuthenticationManager().getAuthenticationContext().isLoggedIn()) {
            userSubscriptionsPresenter.loadSubscription();
        }
    }

    @Override
    public UserSubscriptionsView.Presenter getUserSubscriptionsPresenter() {
        return userSubscriptionsPresenter;
    }

}
