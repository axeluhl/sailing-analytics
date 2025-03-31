package com.sap.sailing.gwt.home.mobile.places.user.profile.subscriptions;

import com.sap.sailing.gwt.home.mobile.places.user.profile.AbstractUserProfileView;
import com.sap.sailing.gwt.home.shared.places.user.profile.subscriptions.UserSubscriptionsView;

public class UserProfileSubscriptionsViewImpl extends AbstractUserProfileView implements UserProfileSubscriptionsView {

    private final UserSubscriptionsView userSubscriptionsView;

    public UserProfileSubscriptionsViewImpl(final UserProfileSubscriptionsView.Presenter presenter) {
        super(presenter);
        this.userSubscriptionsView = new UserSubscriptions(presenter.getUserSubscriptionsPresenter());
        this.setViewContent(userSubscriptionsView);
    }

}
