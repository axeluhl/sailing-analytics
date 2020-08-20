package com.sap.sse.security.shared;

/**
 * Payment subscription plan
 * 
 * @author Tu Tran
 */
public enum SubscriptionPlan {
    STARTER("starter", "Starter",
            new SubscriptionPlanRole[] { new SubscriptionPlanRole(PredefinedRoles.spectator.getId(),
                    SubscriptionPlanRole.TenantQualification.DEFAULT_QUALIFIED_USER_TENANT,
                    SubscriptionPlanRole.UserQualification.USER) }), PREMIUM(
                            "premium", "Premium",
                            new SubscriptionPlanRole[] { new SubscriptionPlanRole(PredefinedRoles.moderator.getId(),
                                    SubscriptionPlanRole.TenantQualification.DEFAULT_SUBSCRIBED_USER_TENANT,
                                    SubscriptionPlanRole.UserQualification.NONE) }),;

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
