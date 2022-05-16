package com.sap.sse.security.ui.client.subscription;

import com.sap.sse.security.ui.shared.subscription.SubscriptionListDTO;

/**
 * View to display an overview of user subscriptions including information like plan, subscription status, etc. as well
 * as the possibility to cancel currently active subscription if desired.
 */
public interface SubscriptionsView {

    /**
     * Update the view with subscription data loaded from back-end
     *
     * @param subscriptions
     *            {@link SubscriptionListDTO transfer object} containing information about subscriptions of current user
     */
    void updateView(SubscriptionListDTO subscriptions);

}
