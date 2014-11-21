package com.sap.sse.security.shared;

import java.io.Serializable;

public interface Account extends Serializable {

    public AccountType getAccountType();
    
    public enum AccountType {
        USERNAME_PASSWORD("Username and Password"), SOCIAL_USER("Social User");
        
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
