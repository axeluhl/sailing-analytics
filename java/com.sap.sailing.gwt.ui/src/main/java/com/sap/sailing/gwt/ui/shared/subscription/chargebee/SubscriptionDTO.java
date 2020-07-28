package com.sap.sailing.gwt.ui.shared.subscription.chargebee;

import com.google.gwt.user.client.rpc.IsSerializable;
import com.sap.sailing.gwt.ui.client.subscription.chargebee.SubscriptionService;

/**
 * User subscription data transfer object {@link SubscriptionService#getSubscription()}
 * 
 * @author Tu Tran
 */
public class SubscriptionDTO implements IsSerializable {

    /**
     * User's subscriptions
     */
    private SubscriptionItem[] subscriptionItems;

    /**
     * Error message
     */
    private String error;

    public SubscriptionDTO() {
    }

    public SubscriptionDTO(SubscriptionItem[] subscriptionItems, String error) {
        this.subscriptionItems = subscriptionItems;
        this.error = error;
    }

    public SubscriptionItem[] getSubscriptionItems() {
        return subscriptionItems;
    }

    public String getError() {
        return error;
    }

    public void setSubscriptionItems(SubscriptionItem[] subscriptionItems) {
        this.subscriptionItems = subscriptionItems;
    }

    public void setError(String error) {
        this.error = error;
    }
}
