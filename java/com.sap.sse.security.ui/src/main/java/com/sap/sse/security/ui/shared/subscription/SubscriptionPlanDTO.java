package com.sap.sse.security.ui.shared.subscription;

import java.util.HashSet;
import java.util.Set;

import com.google.gwt.user.client.rpc.IsSerializable;
import com.sap.sse.security.shared.subscription.SubscriptionPrice;
import com.sap.sse.security.ui.client.subscription.SubscriptionService;

/**
 * SubscriptionPlan data transfer object {@link SubscriptionService#getAllSubscriptionPlans()}
 */
public class SubscriptionPlanDTO implements IsSerializable {
    private static final String FEATURES_MESSAGE_KEY_SUFFX = "_features";
    private static final String NAME_MESSAGE_KEY_SUFFX = "_name";
    private static final String DESC_MESSAGE_KEY_SUFFX = "_description";
    private static final long serialVersionUID = -1990028347487353679L;
    private String id;
    private HashSet<SubscriptionPrice> prices = new HashSet<SubscriptionPrice>();
    private String error;
    private boolean isUserSubscribedToPlan;

    /**
     * For GWT Serialization only
     */
    @Deprecated
    public SubscriptionPlanDTO() {
    }

    public SubscriptionPlanDTO(String id, boolean isUserSubscribedToPlan, Set<SubscriptionPrice> prices, String error) {
        this.id = id;
        this.isUserSubscribedToPlan = isUserSubscribedToPlan;
        this.prices = new HashSet<SubscriptionPrice>(prices);
        this.error = error;
    }

    public static long getSerialversionuid() {
        return serialVersionUID;
    }

    public String getId() {
        return id;
    }

    public boolean isUserSubscribedToPlan() {
        return isUserSubscribedToPlan;
    }

    public void setUserSubscribedToPlan(boolean isUserSubscribedToPlan) {
        this.isUserSubscribedToPlan = isUserSubscribedToPlan;
    }

    public String getNameMessageKey() {
        return this.id + NAME_MESSAGE_KEY_SUFFX;
    }

    public String getDescMessageKey() {
        return this.id + DESC_MESSAGE_KEY_SUFFX;
    }

    public String getFeatureMessageKey() {
        return this.id + FEATURES_MESSAGE_KEY_SUFFX;
    }

    public String getError() {
        return error;
    }
    
    // TODO: Implement action information
    public boolean isCurrentUserSubscribed() {
        return false;
    }

    public HashSet<SubscriptionPrice> getPrices() {
        return prices;
    }

    public void setPrices(HashSet<SubscriptionPrice> prices) {
        this.prices = prices;
    }

    public void setError(String error) {
        this.error = error;
    }

}
