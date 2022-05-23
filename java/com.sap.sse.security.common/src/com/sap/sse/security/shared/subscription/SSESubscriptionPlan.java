package com.sap.sse.security.shared.subscription;

import java.io.Serializable;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/**
 * Payment subscription plans. A subscription plan has a name, a {@link String}-based ID, and a set of
 * {@link SubscriptionPlanRole roles} it grants to a subscribing user. These roles can specify how they are to be
 * qualified when assigned, regarding user and group qualifications. See
 * {@link SubscriptionPlanRole.GroupQualificationMode} and {@link SubscriptionPlanRole.UserQualificationMode} for
 * more details.
 * 
 * @author Tu Tran
 */
public class SSESubscriptionPlan extends SubscriptionPlan {
    private static final long serialVersionUID = 9061666338780737555L;
    private static final Map<String, SSESubscriptionPlan> plansById = new HashMap<String, SSESubscriptionPlan>();

    private SSESubscriptionPlan(String id, HashSet<SubscriptionPrice> prices, Set<PlanCategory> planCategories,
            Boolean isOneTimePlan, PlanGroup group, SubscriptionPlanRole... roles) {
        super(id, prices, planCategories, isOneTimePlan, group, roles);
        plansById.put(id, this);
    }
    
    public static Map<Serializable, SubscriptionPlan> getAllInstances(){
        return Collections.unmodifiableMap(plansById);
    }

}
