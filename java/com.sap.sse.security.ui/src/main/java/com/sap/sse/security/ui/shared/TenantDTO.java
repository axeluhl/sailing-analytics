package com.sap.sse.security.ui.shared;

import java.util.Set;
import java.util.UUID;

public class TenantDTO extends UserGroupDTO {
    TenantDTO() {} // just for serialization
    
    public TenantDTO(UUID id, String name, AccessControlListDTO acl, OwnerDTO ownership, Set<String> usernames) {
        super(id, name, acl, ownership, usernames);
    }
}