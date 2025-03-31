package com.sap.sse.security.datamining.data;

import com.sap.sse.common.Duration;
import com.sap.sse.datamining.annotations.Connector;
import com.sap.sse.datamining.annotations.Dimension;
import com.sap.sse.datamining.annotations.Statistic;
import com.sap.sse.security.shared.subscription.Subscription;

/**
 * Equality is based on {@link Subscription#getSubscriptionId() subscription ID}.
 * 
 * @author Axel Uhl (d043530)
 *
 */
public interface HasSubscriptionContext {
    @Connector(messageKey = "User", scanForStatistics = false)
    HasUserContext getUserContext();
    
    @Dimension(messageKey = "PlanID")
    String getPlanID();

    @Dimension(messageKey = "CustomerID")
    String getCustomerId();

    @Dimension(messageKey = "CurrencyCode")
    String getCurrencyCode();

    @Dimension(messageKey = "ProviderName")
    String getProviderName();

    @Dimension(messageKey = "SubscriptionStatus")
    String getSubscriptionStatus();
    
    @Statistic(messageKey = "DurationUntilExpiration")
    Duration getDurationUntilExpiration();
}
