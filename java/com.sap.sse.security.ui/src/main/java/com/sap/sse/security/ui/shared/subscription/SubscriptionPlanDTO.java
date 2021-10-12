package com.sap.sse.security.ui.shared.subscription;

import java.util.HashSet;
import java.util.Set;

import com.google.gwt.user.client.rpc.IsSerializable;
import com.sap.sse.security.shared.StringMessagesKey;
import com.sap.sse.security.shared.subscription.SubscriptionPrice;
import com.sap.sse.security.ui.client.subscription.SubscriptionService;

/**
 * SubscriptionPlan data transfer object {@link SubscriptionService#getAllSubscriptionPlans()}
 */
public class SubscriptionPlanDTO implements IsSerializable {
    private static final long serialVersionUID = -1990028347487353679L;
    private String id;
    private StringMessagesKey nameMessageKey;
    private StringMessagesKey descMessageKey;
    private HashSet<StringMessagesKey> featureIds;
    private HashSet<SubscriptionPrice> prices = new HashSet<SubscriptionPrice>();
    private String error;

    /**
     * For GWT Serialization only
     */
    @Deprecated
    public SubscriptionPlanDTO() {
    }

    public SubscriptionPlanDTO(String id, StringMessagesKey nameMessageKey, StringMessagesKey descMessageKey,
            Set<StringMessagesKey> features, Set<SubscriptionPrice> prices, String error) {
        this.id = id;
        this.nameMessageKey = nameMessageKey;
        this.descMessageKey = descMessageKey;
        this.featureIds = new HashSet<StringMessagesKey>(features);
        this.prices = new HashSet<SubscriptionPrice>(prices);
        this.error = error;
    }

    public static long getSerialversionuid() {
        return serialVersionUID;
    }

    public String getId() {
        return id;
    }

    public StringMessagesKey getNameMessageKey() {
        return nameMessageKey;
    }

    public StringMessagesKey getDescMessageKey() {
        return descMessageKey;
    }

    public void setDescMessageKey(StringMessagesKey descMessageKey) {
        this.descMessageKey = descMessageKey;
    }

    public Set<StringMessagesKey> getFeatures() {
        return featureIds;
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
