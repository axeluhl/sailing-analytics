package com.sap.sse.security.shared.dto;

import java.util.UUID;

import com.sap.sse.security.shared.RoleDefinitionImpl;
import com.sap.sse.security.shared.WildcardPermission;

public class StrippedRoleDefinitionDTO extends RoleDefinitionImpl {
    
    private static final long serialVersionUID = -3340211553071045099L;

    @Deprecated
    StrippedRoleDefinitionDTO() {} // for GWT serialization only
    
    public StrippedRoleDefinitionDTO(UUID id, String name, Iterable<WildcardPermission> permissions) {
        super(id, name, permissions);
    }
}
