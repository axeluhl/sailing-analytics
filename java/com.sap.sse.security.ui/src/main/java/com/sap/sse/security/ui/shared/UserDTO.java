package com.sap.sse.security.ui.shared;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import com.google.gwt.user.client.rpc.IsSerializable;

public class UserDTO implements IsSerializable {

    
    private String name;
    private String accountType;
    
    private List<String> roles = new ArrayList<>();
    
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

    public List<String> getRoles() {
        return roles;
    }

    public void addRoles(Collection<String> roles) {
        this.roles.addAll(roles);
    }
    
    
}
