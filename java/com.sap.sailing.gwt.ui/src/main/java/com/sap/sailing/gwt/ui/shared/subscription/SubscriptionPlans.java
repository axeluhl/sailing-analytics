package com.sap.sailing.gwt.ui.shared.subscription;

import java.util.HashMap;
import java.util.Map;

public class SubscriptionPlans {
    public static Plan PREMIUM = new Plan("premium", "Premium");
    
    private static Map<String, Plan> plans = null;
    
    public static Plan getPlan(String planId) {
        if (plans == null) {
            plans = new HashMap<String, SubscriptionPlans.Plan>();
            plans.put(PREMIUM.id, PREMIUM);
        }
        
        return plans.get(planId);
    }
    
    public static class Plan {
        private String name;
        private String id;
        
        public Plan(String id, String name) {
            this.name = name;
            this.id = id;
        }

        public String getName() {
            return name;
        }

        public String getId() {
            return id;
        }
    }
}
