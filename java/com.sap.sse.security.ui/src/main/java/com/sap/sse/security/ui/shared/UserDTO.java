package com.sap.sse.security.ui.shared;

import com.google.gwt.user.client.rpc.IsSerializable;

public class UserDTO implements IsSerializable {

    
    private String name;
    private String accountType;
    
    public String getAccountType() {
        return accountType;
    }

    public void setAccountType(String accountType) {
        this.accountType = accountType;
    }

    public UserDTO() {
    }

    public UserDTO(String name, String accountType) {
        super();
        this.name = name;
        this.accountType = accountType;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }
    
    
}
