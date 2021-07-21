package com.sap.sailing.domain.common.subscription;

import java.io.Serializable;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.sap.sse.security.shared.subscription.SubscriptionPlan;
import com.sap.sse.security.shared.subscription.SubscriptionPlanRole;

/**
 * Payment subscription plans. A subscription plan has a name, a {@link String}-based ID, and a set of
 * {@link SubscriptionPlanRole roles} it grants to a subscribing user. These roles can specify how they are to be
 * qualified when assigned, regarding user and group qualifications. See
 * {@link SubscriptionPlanRole.GroupQualificationMode} and {@link SubscriptionPlanRole.UserQualificationMode} for
 * more details.
 * 
 */
public class SailingSubscriptionPlan extends SubscriptionPlan{
    private static final long serialVersionUID = 2563619370274543312L;
    private static final Map<String, SubscriptionPlan> plansById = new HashMap<>();
    
    private SailingSubscriptionPlan(String id, String name, SubscriptionPlanRole[] roles, List<String> features, String price) {
        super(id, name, roles, features, price);
        plansById.put(id, this);
    }
    
    public static final SubscriptionPlan PRO = new SailingSubscriptionPlan ("pro", "PRO", new SubscriptionPlanRole[] {
            new SubscriptionPlanRole(StreamletViewerRole.ROLE_ID)},
            convertPermissionsIterable(StreamletViewerRole.getInstance().getPermissions()), "99$");
    
    public static Map<Serializable, SubscriptionPlan> getAllInstances(){
        return Collections.unmodifiableMap(plansById);
    }
}
