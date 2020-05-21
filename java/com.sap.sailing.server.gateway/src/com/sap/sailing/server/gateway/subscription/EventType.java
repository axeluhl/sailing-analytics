package com.sap.sailing.server.gateway.subscription;

public enum EventType {
    CUSTOMER_DELETED("customer_deleted"),
    SUBSCRIPTION_DELETED("subscription_deleted"),
    SUBSCRIPTION_CREATED("subscription_created"),
    SUBSCRIPTION_CHANGED("subscription_changed"),
    SUBSCRIPTION_CANCELLED("subscription_cancelled"),
    PAYMENT_SUCCEEDED("payment_succeeded"),
    PAYMENT_FAILED("payment_failed"),
    SUBSCRIPTION_ACTIVATED("subscription_activated");
    
    private String name;
    
    EventType(String name) {
        this.name = name;
    }
    
    public String getName() {
        return name;
    }
}
