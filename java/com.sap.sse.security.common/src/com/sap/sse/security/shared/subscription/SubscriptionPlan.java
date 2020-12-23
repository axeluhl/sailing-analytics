package com.sap.sse.security.shared.subscription;

import java.util.HashMap;
import java.util.Map;

import com.sap.sse.security.shared.PredefinedRoles;

/**
 * Payment subscription plan
 * 
 * @author Tu Tran
 */
public enum SubscriptionPlan {
    STARTER("starter", "Starter", new SubscriptionPlanRole[] {
            new SubscriptionPlanRole(PredefinedRoles.spectator.getId(),
                    SubscriptionPlanRole.GroupQualificationMode.DEFAULT_QUALIFIED_USER_TENANT,
                    SubscriptionPlanRole.UserQualificationMode.SUBSCRIBING_USER),
            new SubscriptionPlanRole(PredefinedRoles.mediaeditor.getId(),
                    SubscriptionPlanRole.GroupQualificationMode.DEFAULT_QUALIFIED_USER_TENANT,
                    SubscriptionPlanRole.UserQualificationMode.SUBSCRIBING_USER) }),
    PREMIUM("premium", "Premium", new SubscriptionPlanRole[] {
            new SubscriptionPlanRole(PredefinedRoles.spectator.getId(),
                    SubscriptionPlanRole.GroupQualificationMode.DEFAULT_QUALIFIED_USER_TENANT,
                    SubscriptionPlanRole.UserQualificationMode.SUBSCRIBING_USER),
            new SubscriptionPlanRole(PredefinedRoles.moderator.getId(),
                    SubscriptionPlanRole.GroupQualificationMode.SUBSCRIBING_USER_DEFAULT_TENANT,
                    SubscriptionPlanRole.UserQualificationMode.NONE) });

    /**
     * Plan name
     */
    private String name;

    /**
     * Plan id
     */
    private String id;

    /**
     * Roles assigned for this plan, if user subscribe to the plan then the user will be assigned these roles
     */
    private SubscriptionPlanRole[] roles;
    
    private static final Map<String, SubscriptionPlan> plansById = new HashMap<String, SubscriptionPlan>();
    
    static {
        for (final SubscriptionPlan plan : values()) {
            plansById.put(plan.getId(), plan);
        }
    }
    
    public static SubscriptionPlan getPlan(String planId) {
        return plansById.get(planId);
    }

    SubscriptionPlan(String id, String name, SubscriptionPlanRole[] roles) {
        this.name = name;
        this.id = id;
        this.roles = roles;
    }

    public String getName() {
        return name;
    }

    public String getId() {
        return id;
    }

    public SubscriptionPlanRole[] getRoles() {
        return this.roles;
    }
}
