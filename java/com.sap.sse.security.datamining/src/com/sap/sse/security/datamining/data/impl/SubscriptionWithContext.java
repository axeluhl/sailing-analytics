package com.sap.sse.security.datamining.data.impl;

import com.sap.sse.common.Duration;
import com.sap.sse.common.TimePoint;
import com.sap.sse.common.Util;
import com.sap.sse.security.datamining.data.HasSubscriptionContext;
import com.sap.sse.security.datamining.data.HasUserContext;
import com.sap.sse.security.shared.subscription.Subscription;

public class SubscriptionWithContext implements HasSubscriptionContext {
    private final HasUserContext userContext;
    
    private final Subscription subscription;
    
    
    public SubscriptionWithContext(HasUserContext userContext, Subscription subscription) {
        super();
        this.userContext = userContext;
        this.subscription = subscription;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((subscription == null) ? 0 : subscription.getSubscriptionId() == null ? 0 : subscription.getSubscriptionId().hashCode());
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        SubscriptionWithContext other = (SubscriptionWithContext) obj;
        if (subscription == null) {
            if (other.subscription != null)
                return false;
        } else if (!Util.equalsWithNull(subscription.getSubscriptionId(), other.subscription.getSubscriptionId()))
            return false;
        return true;
    }

    @Override
    public HasUserContext getUserContext() {
        return userContext;
    }

    @Override
    public String getPlanID() {
        return subscription.getPlanId();
    }

    @Override
    public String getCustomerId() {
        return subscription.getCustomerId();
    }

    @Override
    public String getCurrencyCode() {
        return subscription.getCurrencyCode();
    }

    @Override
    public String getProviderName() {
        return subscription.getProviderName();
    }

    @Override
    public String getSubscriptionStatus() {
        return subscription.getSubscriptionStatus();
    }

    @Override
    public Duration getDurationUntilExpiration() {
        return TimePoint.now().until(subscription.getCurrentTermEnd());
    }
}
