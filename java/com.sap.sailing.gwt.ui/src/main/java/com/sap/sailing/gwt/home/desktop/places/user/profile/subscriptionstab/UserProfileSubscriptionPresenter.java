package com.sap.sailing.gwt.home.desktop.places.user.profile.subscriptionstab;

import com.sap.sailing.gwt.home.desktop.app.DesktopPlacesNavigator;
import com.sap.sailing.gwt.home.desktop.places.user.profile.UserProfileView;
import com.sap.sailing.gwt.home.shared.places.user.profile.subscriptions.UserSubscriptionsPresenter;
import com.sap.sailing.gwt.home.shared.places.user.profile.subscriptions.UserSubscriptionsView;
import com.sap.sailing.gwt.home.shared.places.user.profile.subscriptions.UserSubscriptionsView.Presenter;
import com.sap.sse.security.ui.authentication.app.AuthenticationContext;

/**
 * Implementation presenter for {@link UserProfileSubscriptionsView}
 *
 * @author Tu Tran
 */
public class UserProfileSubscriptionPresenter implements UserProfileSubscriptionsView.Presenter {

    private final UserProfileSubscriptionsView view;
    private final UserProfileView.Presenter userProfilePresenter;
    private final UserSubscriptionsView.Presenter userSubscriptionPresenter;

    public UserProfileSubscriptionPresenter(final DesktopPlacesNavigator homePlacesNavigator,
            final UserProfileSubscriptionsView view, final UserProfileView.Presenter userProfilePresenter) {
        this.view = view;
        this.userProfilePresenter = userProfilePresenter;
        this.userSubscriptionPresenter = new UserSubscriptionsPresenter<>(userProfilePresenter.getClientFactory(),
                homePlacesNavigator.getSubscriptionsNavigation());
        this.userSubscriptionPresenter.init();
        view.setPresenter(this);
    }

    @Override
    public void doTriggerLoginForm() {
        userProfilePresenter.doTriggerLoginForm();
    }

    @Override
    public void setAuthenticationContext(final AuthenticationContext authenticationContext) {
        view.getDecorator().setAuthenticationContext(authenticationContext);
        if (authenticationContext.isLoggedIn()) {
            userSubscriptionPresenter.loadSubscription();
        }
    }

    @Override
    public Presenter getUserSubscriptionPresenter() {
        return this.userSubscriptionPresenter;
    }

}
