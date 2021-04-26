package com.sap.sse.security.shared;

import java.io.Serializable;
import java.util.Map;

import com.sap.sse.security.shared.subscription.SubscriptionPlan;

@FunctionalInterface
public interface SubscriptionPlanProvider {
    Map<Serializable, SubscriptionPlan> getAllSubscriptionPlans();
}
