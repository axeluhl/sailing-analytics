package com.sap.sse.security.shared;


public class UsernamePasswordAccount implements Account {

    private String name;
    private String saltedPassword;
    private Object salt;
    
    public UsernamePasswordAccount(String name, String saltedPassword, Object salt) {
        this.name = name;
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

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    @Override
    public AccountType getAccountType() {
        return AccountType.USERNAME_PASSWORD;
    }
}
