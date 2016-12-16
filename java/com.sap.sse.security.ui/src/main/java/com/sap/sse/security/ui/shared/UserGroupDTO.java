package com.sap.sse.security.ui.shared;

import java.util.Set;

import com.google.gwt.user.client.rpc.IsSerializable;

public class UserGroupDTO implements IsSerializable {
    private String name;
    private TenantDTO owner;
    private AccessControlListDTO acl;
    private Set<String> usernames;
    
    UserGroupDTO() {} // just for serialization
    
    public UserGroupDTO(String name, TenantDTO owner, AccessControlListDTO acl, Set<String> usernames) {
        this.name = name;
        this.owner = owner;
        this.acl = acl;
        this.usernames = usernames;
    }
    
    public String getName() {
        return name;
    }
    
    public TenantDTO getOwner() {
        return owner;
    }
    
    public AccessControlListDTO getAccessControlList() {
        return acl;
    }
    
    public boolean contains(String username) {
        return usernames.contains(username);
    }
    
    public Set<String> getUsernames() {
        return usernames;
    }
}
