package com.sap.sailing.gwt.home.shared.places.user.profile.subscription;

import com.google.gwt.user.client.ui.IsWidget;
import com.sap.sailing.gwt.ui.shared.subscription.SubscriptionDTO;

public interface UserSubscriptionView extends IsWidget {
    /**
     * This is called on start loading subscription data
     */
    public void onStartLoadSubscription();

    /**
     * Call to update the view with subscription data returned from backend
     * 
     * @param subscription
     */
    public void updateView(SubscriptionDTO subscription);

    /**
     * Called on Chargebee checkout modal is closed
     */
    public void onCloseCheckoutModal();

    /**
     * Called on openning Chargebee checkout modal has errors
     * 
     * @param error
     */
    public void onOpenCheckoutError(String error);

    /**
     * Presenter for {@link UserSubscriptionView}
     * 
     * @author tutran
     */
    public interface Presenter {
        /**
         * Init Chargebee
         */
        public void init();

        /**
         * Load user's subscription data
         */
        public void loadSubscription();

        /**
         * Open Chargebee checkout modal from which user can create new subscription or change one if user is already in
         * a subscription plan
         * 
         * @param planId
         *            Id of plan user want to subscribe to
         */
        public void openCheckout(String planId);

        public void setView(UserSubscriptionView view);

        /**
         * Request to cancel current user's subscription
         */
        public void cancelSubscription();
    }
}
