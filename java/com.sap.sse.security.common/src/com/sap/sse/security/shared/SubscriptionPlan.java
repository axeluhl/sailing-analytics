package com.sap.sse.security.shared;

import java.util.UUID;

/**
 * Payment subscription plan
 * 
 * @author Tu Tran
 */
public enum SubscriptionPlan {
    STARTER("starter", "Starter", new UUID[] { PredefinedRoles.spectator.getId() }), PREMIUM("premium", "Premium",
            new UUID[] { PredefinedRoles.moderator.getId() });

    private String name;
    private String id;

    /**
     * Roles assigned for this plan, if user subscribe to the plan then the user will be assigned these roles
     */
    private UUID[] roleDefinitionIds;

    SubscriptionPlan(String id, String name, UUID[] roleDefinitionIds) {
        this.name = name;
        this.id = id;
        this.roleDefinitionIds = roleDefinitionIds;
    }

    public String getName() {
        return name;
    }

    public String getId() {
        return id;
    }

    public UUID[] getRoleDefinitionIds() {
        return this.roleDefinitionIds;
    }
}
