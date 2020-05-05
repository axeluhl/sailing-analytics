package com.sap.sailing.gwt.ui.shared.subscription;

public class SubscriptionPlans {
    public static Plan PREMIUM = new Plan("premium", "Premium");
    
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
