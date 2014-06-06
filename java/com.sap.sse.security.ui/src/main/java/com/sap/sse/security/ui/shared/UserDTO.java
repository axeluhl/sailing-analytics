package com.sap.sse.security.ui.shared;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import com.google.gwt.user.client.rpc.IsSerializable;

public class UserDTO implements IsSerializable {

    
    private String name;
    private List<AccountDTO> accounts = new ArrayList<AccountDTO>();
    
    private List<String> roles = new ArrayList<>();

    public UserDTO() {
    }

    public UserDTO(String name, List<AccountDTO> accounts) {
        this.name = name;
        this.accounts = accounts;
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

    public List<AccountDTO> getAccounts() {
        return accounts;
    }

    public void setAccounts(List<AccountDTO> accounts) {
        this.accounts = accounts;
    }
    
    
}
