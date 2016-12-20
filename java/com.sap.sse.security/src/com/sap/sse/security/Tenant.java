package com.sap.sse.security;

import java.util.Set;

public class Tenant extends UserGroupImpl {
    private static final long serialVersionUID = -8831840409264252279L;

    public Tenant(UserGroup group) {
        super(group.getName(), group.getUsernames(), group.getAccessControlList());
    }
    
    public Tenant(String name, AccessControlList acl) {
        super(name, acl);
    }
    
    public Tenant(String name, Set<String> usernames, AccessControlList acl) {
        super(name, usernames, acl);
    }
}