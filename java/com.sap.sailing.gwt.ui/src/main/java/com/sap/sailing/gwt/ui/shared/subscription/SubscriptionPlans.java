package com.sap.sailing.gwt.ui.shared.subscription;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Class represents all subscription plans
 * 
 * @author tutran
 */
public class SubscriptionPlans {
    public static Plan STARTER = new Plan("starter", "Starter");
    public static Plan PREMIUM = new Plan("premium", "Premium");

    private static Map<String, Plan> planMap;
    private static List<Plan> planList;

    public static Plan getPlan(String planId) {
        if (planMap == null) {
            planMap = new HashMap<String, SubscriptionPlans.Plan>();
            planMap.put(STARTER.id, STARTER);
            planMap.put(PREMIUM.id, PREMIUM);
        }

        return planMap.get(planId);
    }

    public static List<Plan> getPlanList() {
        if (planList == null) {
            planList = new ArrayList<SubscriptionPlans.Plan>();
            planList.add(STARTER);
            planList.add(PREMIUM);
        }

        return planList;
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
