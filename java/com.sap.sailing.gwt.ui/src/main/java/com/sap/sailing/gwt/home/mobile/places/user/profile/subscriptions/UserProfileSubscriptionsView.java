package com.sap.sailing.gwt.home.mobile.places.user.profile.subscriptions;

import com.sap.sailing.gwt.home.mobile.places.user.profile.UserProfileViewBase;
import com.sap.sailing.gwt.home.shared.places.user.profile.subscriptions.UserSubscriptionsView;

public interface UserProfileSubscriptionsView extends UserProfileViewBase {

    public interface Presenter extends UserProfileViewBase.Presenter {
        UserSubscriptionsView.Presenter getUserSubscriptionsPresenter();
    }
}

