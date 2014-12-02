package com.sap.sse.security.shared;

import com.sap.sse.common.Named;


public class UsernamePasswordAccount implements Named, Account {
    private static final long serialVersionUID = 5986653173215900637L;
    private String name;
    private String saltedPassword;
    private byte[] salt;
    
    public UsernamePasswordAccount(String name, String saltedPassword, byte[] salt) {
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

    public byte[] getSalt() {
        return salt;
    }

    public void setSalt(byte[] salt) {
        this.salt = salt;
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public AccountType getAccountType() {
        return AccountType.USERNAME_PASSWORD;
    }
}
