package com.sap.sailing.gwt.ui.shared.subscription;

/**
 * Payment subscription plan
 * 
 * @author Tu Tran
 */
public enum SubscriptionPlan {
    STARTER("starter", "Starter"), PREMIUM("premium", "Premium");

    private String name;
    private String id;

    SubscriptionPlan(String id, String name) {
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
