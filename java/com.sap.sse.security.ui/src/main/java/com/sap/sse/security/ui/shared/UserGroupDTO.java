package com.sap.sse.security.ui.shared;

import java.util.Set;
import java.util.UUID;

import com.google.gwt.user.client.rpc.IsSerializable;
import com.sap.sse.security.shared.UserGroup;

public class UserGroupDTO implements UserGroup, IsSerializable {
    private static final long serialVersionUID = -436112808617164216L;
    
    private UUID id;
    private String name;
    private AccessControlListDTO acl;
    private OwnerDTO owner;
    private Set<String> usernames;
    
    UserGroupDTO() {} // just for serialization
    
    public UserGroupDTO(UUID id, String name, AccessControlListDTO acl, OwnerDTO owner, Set<String> usernames) {
        this.id = id;
        this.name = name;
        this.acl = acl;
        this.owner = owner;
        this.usernames = usernames;
    }
    
    public UUID getId() {
        return id;
    }
    
    public String getName() {
        return name;
    }
    
    public AccessControlListDTO getAccessControlList() {
        return acl;
    }
    
    public OwnerDTO getOwner() {
        return owner;
    }
    
    public boolean contains(String username) {
        return usernames.contains(username);
    }
    
    public Set<String> getUsernames() {
        return usernames;
    }

    @Override
    public void add(String user) {
        // TODO Remove in next refactoring
    }

    @Override
    public void remove(String user) {
        // TODO Remove in next refactoring
    }
}
