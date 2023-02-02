package com.sap.sse.security.ui.shared.subscription;

import com.google.gwt.user.client.rpc.IsSerializable;
import com.sap.sse.security.ui.client.subscription.SubscriptionService;

/**
 * User subscription data transfer object {@link SubscriptionService#getSubscriptions()}
 * 
 * @author Tu Tran
 */
public class SubscriptionListDTO implements IsSerializable {
    /**
     * User's subscriptions
     */
    private SubscriptionDTO[] subscriptionItems;

    /**
     * Error message
     */
    private String error;

    /**
     * Only for GWT serialization
     */
    @Deprecated
    public SubscriptionListDTO() {
    }

    public SubscriptionListDTO(SubscriptionDTO[] subscriptionItems, String error) {
        this.subscriptionItems = subscriptionItems;
        this.error = error;
    }

    public SubscriptionDTO[] getSubscriptionItems() {
        return subscriptionItems;
    }

    public String getError() {
        return error;
    }

    public void setSubscriptionItems(SubscriptionDTO[] subscriptionItems) {
        this.subscriptionItems = subscriptionItems;
    }
}
