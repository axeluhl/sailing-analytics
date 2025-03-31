package com.sap.sse.security.impl;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

import org.osgi.util.tracker.ServiceTracker;

import com.sap.sse.security.shared.SubscriptionPlanProvider;
import com.sap.sse.security.shared.subscription.SubscriptionPlan;

public class OSGISubscriptionPlanProvider implements SubscriptionPlanProvider {
    
    private final ServiceTracker<SubscriptionPlanProvider, SubscriptionPlanProvider> subscriptionPlanServiceTracker;

    public OSGISubscriptionPlanProvider(ServiceTracker<SubscriptionPlanProvider, SubscriptionPlanProvider> subscriptionPlanServiceTracker) {
        this.subscriptionPlanServiceTracker = subscriptionPlanServiceTracker;
    }

    @Override
    public Map<Serializable, SubscriptionPlan> getAllSubscriptionPlans() {
        Map<Serializable, SubscriptionPlan> result = new HashMap<>();
        for (SubscriptionPlanProvider provider : subscriptionPlanServiceTracker.getServices(new SubscriptionPlanProvider[1])) {
            result.putAll(provider.getAllSubscriptionPlans());
        }
        return result;
    }

}
