package com.sap.sailing.gwt.home.desktop.places.user.profile.subscriptiontab;

import com.sap.sailing.gwt.home.desktop.places.user.profile.UserProfileClientFactory;
import com.sap.sailing.gwt.home.desktop.places.user.profile.UserProfileView;
import com.sap.sailing.gwt.home.shared.places.user.profile.subscription.UserSubscriptionPresenter;
import com.sap.sailing.gwt.home.shared.places.user.profile.subscription.UserSubscriptionView;
import com.sap.sailing.gwt.home.shared.places.user.profile.subscription.UserSubscriptionView.Presenter;
import com.sap.sse.security.ui.authentication.app.AuthenticationContext;

public class UserProfileSubscriptionPresenter implements UserProfileSubscriptionView.Presenter {
    
    private final UserProfileSubscriptionView view;
    private final UserProfileView.Presenter userProfilePresenter;
    private final UserSubscriptionView.Presenter userSubscriptionPresenter;
    
    public UserProfileSubscriptionPresenter(final UserProfileSubscriptionView view,
            final UserProfileView.Presenter userProfilePresenter) {
       this.view = view;
       this.userProfilePresenter = userProfilePresenter;
       this.userSubscriptionPresenter = new UserSubscriptionPresenter<UserProfileClientFactory>(
               userProfilePresenter.getClientFactory());
       view.setPresenter(this);
    }

    @Override
    public void doTriggerLoginForm() {
        userProfilePresenter.doTriggerLoginForm();
    }

    @Override
    public void setAuthenticationContext(AuthenticationContext authenticationContext) {
        view.getDecorator().setAuthenticationContext(authenticationContext);
        if (authenticationContext.isLoggedIn()) {
            userSubscriptionPresenter.init();
            userSubscriptionPresenter.loadSubscription();
        }
    }

    @Override
    public Presenter getUserSubscriptionPresenter() {
        return this.userSubscriptionPresenter;
    }

}
