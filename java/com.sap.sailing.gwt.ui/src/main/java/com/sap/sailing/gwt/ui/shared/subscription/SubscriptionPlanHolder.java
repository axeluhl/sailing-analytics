package com.sap.sailing.gwt.ui.shared.subscription;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Class hold system subscription plans and method for access the plans
 * 
 * @author tutran
 */
public class SubscriptionPlanHolder {

    private static final Map<String, SubscriptionPlan> planMap = new HashMap<String, SubscriptionPlan>() {
        /**
         * 
         */
        private static final long serialVersionUID = -8686217706843873886L;

        {
            put(SubscriptionPlan.STARTER.getId(), SubscriptionPlan.STARTER);
            put(SubscriptionPlan.PREMIUM.getId(), SubscriptionPlan.PREMIUM);
        }
    };

    private static final List<SubscriptionPlan> planList = new ArrayList<SubscriptionPlan>() {
        /**
         * 
         */
        private static final long serialVersionUID = 1775492848916531255L;

        {
            add(SubscriptionPlan.STARTER);
            add(SubscriptionPlan.PREMIUM);
        }
    };

    private static final SubscriptionPlanHolder instance = new SubscriptionPlanHolder();

    public static SubscriptionPlanHolder getInstance() {
        return instance;
    }

    protected SubscriptionPlanHolder() {
    }

    public SubscriptionPlan getPlan(String planId) {
        return planMap.get(planId);
    }

    public List<SubscriptionPlan> getPlanList() {
        return planList;
    }
}
