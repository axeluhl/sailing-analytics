package com.sap.sse.security.userstore.shared;

public abstract class User {
    
    protected String name;
    
    public User(String name) {
        super();
        this.name = name;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }
    
    public abstract AccountType getAccountType();
    
    public enum AccountType {
        SIMPLE("Simple"), SOCIAL_USER("Social User");
        
        private String name;

        private AccountType(String name) {
            this.name = name;
        }

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }
        
    }

}
