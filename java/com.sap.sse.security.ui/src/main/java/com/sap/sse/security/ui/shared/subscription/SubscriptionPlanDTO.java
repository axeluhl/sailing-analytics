package com.sap.sse.security.ui.shared.subscription;

import java.math.BigDecimal;
import java.util.HashSet;
import java.util.Set;

import com.google.gwt.user.client.rpc.IsSerializable;
import com.sap.sse.security.shared.StringMessagesKey;
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
    private BigDecimal price;
    private String error;

    /**
     * For GWT Serialization only
     */
    @Deprecated
    public SubscriptionPlanDTO() {
    }

    public SubscriptionPlanDTO(String id, StringMessagesKey nameMessageKey, StringMessagesKey descMessageKey,
            BigDecimal price, Set<StringMessagesKey> features, String error) {
        this.id = id;
        this.nameMessageKey = nameMessageKey;
        this.setDescMessageKey(descMessageKey);
        this.featureIds = new HashSet<StringMessagesKey>(features);
        this.price = price;
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

    public BigDecimal getPrice() {
        return price;
    }

    public String getError() {
        return error;
    }

}
