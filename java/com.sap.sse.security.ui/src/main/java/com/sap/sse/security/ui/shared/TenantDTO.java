package com.sap.sse.security.ui.shared;

import java.util.Set;

public class TenantDTO extends UserGroupDTO {
    TenantDTO() {} // just for serialization
    
    public TenantDTO(String name, AccessControlListDTO acl, Set<String> usernames) {
        super(name, acl, usernames);
    }
}