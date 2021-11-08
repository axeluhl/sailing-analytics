package com.sap.sse.security.ui.shared;

import com.sap.sse.security.shared.dto.AccountDTO;

public class UsernamePasswordAccountDTO extends AccountDTO {
    private static final long serialVersionUID = 1L;

    private static final String LABEL = "Simple";
    
    private String name;
    
    private String saltedPassword;
    
    private byte[] salt;
    
    public UsernamePasswordAccountDTO() {
        super(LABEL);
    }

    public UsernamePasswordAccountDTO(String name, String saltedPassword, byte[] salt) {
        super(LABEL);
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
