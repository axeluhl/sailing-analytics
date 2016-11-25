package com.sap.sse.security.ui.shared;

import java.util.HashSet;
import java.util.Set;

import com.sap.sse.common.Util;

import com.google.gwt.user.client.rpc.IsSerializable;

public class TenantDTO implements IsSerializable {
    private String name;
    private String owner;
    private Set<String> usernames;
    
    TenantDTO() {} // for serialization only
    
    public TenantDTO(String name, String owner, Iterable<String> usernames) {
        this.name = name;
        this.owner = owner;
        this.usernames = new HashSet<>();
        Util.addAll(usernames, this.usernames);
    }
    
    public String getName() {
        return name;
    }
    
    public String getOwner() {
        return owner;
    }
    
    public Set<String> getUsernames() {
        return usernames;
    }
}