package com.sap.sse.security.ui.shared;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.google.gwt.user.client.rpc.IsSerializable;

public class UserDTO implements IsSerializable {

    
    private String name;
    private Map<String , AccountDTO> accounts = new HashMap<String, AccountDTO>();
    
    private List<String> roles = new ArrayList<>();

    public UserDTO() {
    }

    public UserDTO(String name, AccountDTO... accounts) {
        this.name = name;
//        for (AccountDTO account : accounts){
//            this.accounts.put(account.getAccountType(), account);
//        }
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

    public Map<String, AccountDTO> getAccounts() {
        return accounts;
    }

    public void setAccounts(Map<String, AccountDTO> accounts) {
        this.accounts = accounts;
    }
    
    
}
