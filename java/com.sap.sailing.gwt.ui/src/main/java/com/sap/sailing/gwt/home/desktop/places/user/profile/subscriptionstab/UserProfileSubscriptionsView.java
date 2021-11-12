package com.sap.sailing.gwt.home.desktop.places.user.profile.subscriptionstab;

import com.google.gwt.user.client.ui.IsWidget;
import com.sap.sailing.gwt.home.shared.places.user.profile.subscriptions.UserSubscriptionsView;
import com.sap.sse.security.ui.authentication.app.NeedsAuthenticationContext;
import com.sap.sse.security.ui.authentication.decorator.NotLoggedInPresenter;

/**
 * User profile subscription view interface
 * 
 * @author Tu Tran
 */
public interface UserProfileSubscriptionsView extends IsWidget {

    void setPresenter(Presenter presenter);

    NeedsAuthenticationContext getDecorator();

    public interface Presenter extends NotLoggedInPresenter, NeedsAuthenticationContext {
        UserSubscriptionsView.Presenter getUserSubscriptionPresenter();
    }
}
