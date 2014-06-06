package com.sap.sse.security.ui.shared;

public class UsernamePasswordAccountDTO extends AccountDTO {

    private String name;
    
    private String saltedPassword;
    
    private byte[] salt;
    
    public UsernamePasswordAccountDTO() {
        // TODO Auto-generated constructor stub
    }

    public UsernamePasswordAccountDTO(String name, String saltedPassword, byte[] salt) {
        super();
        this.name = name;
        this.saltedPassword = saltedPassword;
        this.salt = salt;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
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
    
    
}
