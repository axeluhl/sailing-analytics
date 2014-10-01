package com.sap.sse.security.ui.shared;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import com.google.gwt.user.client.rpc.IsSerializable;

public class UserDTO implements IsSerializable {
    private String name;
    private String email;
    private List<AccountDTO> accounts;
    private List<String> roles = new ArrayList<>();

    UserDTO() {} // for serialization only

    public UserDTO(String name, String email, List<AccountDTO> accounts) {
        this.name = name;
        this.email = email;
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

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }
    
    
}
