package com.sap.sse.security.userstore.shared;

import java.util.HashSet;
import java.util.Set;

public abstract class User {
    
    protected String name;
    
    protected Set<String> roles = new HashSet<>();

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
    
    public Set<String> getRoles() {
        return roles;
    }
    
    public void addRole(String role){
        roles.add(role);
    }
    
    public void removeRole(String role){
        roles.remove(role);
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
