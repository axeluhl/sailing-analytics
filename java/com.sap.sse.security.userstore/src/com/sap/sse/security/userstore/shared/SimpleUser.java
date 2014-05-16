package com.sap.sse.security.userstore.shared;

public class SimpleUser extends User {

    private String saltedPassword;
    private Object salt;
    
    public SimpleUser(String name, String saltedPassword, Object salt) {
        super(name);
        this.saltedPassword = saltedPassword;
        this.salt = salt;
    }

    public String getSaltedPassword() {
        return saltedPassword;
    }

    public void setSaltedPassword(String saltedPassword) {
        this.saltedPassword = saltedPassword;
    }

    public Object getSalt() {
        return salt;
    }

    public void setSalt(Object salt) {
        this.salt = salt;
    }

    @Override
    public AccountType getAccountType() {
        return AccountType.SIMPLE;
    }
    
}
