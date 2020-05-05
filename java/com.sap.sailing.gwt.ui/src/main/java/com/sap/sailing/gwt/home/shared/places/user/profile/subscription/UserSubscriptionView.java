package com.sap.sailing.gwt.home.shared.places.user.profile.subscription;

import com.google.gwt.user.client.ui.IsWidget;

public interface UserSubscriptionView extends IsWidget {
    public interface Presenter {
        public void init();
        public void loadSubscription();
        public void openCheckout();
        public void setView(UserSubscriptionView view);
    }
}
