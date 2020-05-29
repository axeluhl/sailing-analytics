package com.sap.sse.security.shared;

import java.util.HashMap;
import java.util.Map;

/**
 * Class hold system subscription plans and method for access the plans
 * 
 * @author Tu Tran
 */
public class SubscriptionPlanHolder {

    private static final Map<String, SubscriptionPlan> planMap = new HashMap<String, SubscriptionPlan>();

    static {
        for (SubscriptionPlan plan : SubscriptionPlan.values()) {
            planMap.put(plan.getId(), plan);
        }
    }

    private static final SubscriptionPlanHolder instance = new SubscriptionPlanHolder();

    public static SubscriptionPlanHolder getInstance() {
        return instance;
    }

    protected SubscriptionPlanHolder() {
    }

    public SubscriptionPlan getPlan(String planId) {
        return planMap.get(planId);
    }

    public SubscriptionPlan[] getPlanList() {
        return SubscriptionPlan.values();
    }
}
