package com.sap.sailing.gwt.home.shared.places.user.profile.subscription;

import com.google.gwt.user.client.ui.IsWidget;
import com.sap.sailing.gwt.ui.shared.subscription.SubscriptionDTO;

public interface UserSubscriptionView extends IsWidget {
    public void onStartLoadSubscription();
    public void updateView(SubscriptionDTO subscription);
    public void onCloseCheckoutModal();
    
    public interface Presenter {
        public void init();
        public void loadSubscription();
        public void openCheckout();
        public void setView(UserSubscriptionView view);
    }
}
