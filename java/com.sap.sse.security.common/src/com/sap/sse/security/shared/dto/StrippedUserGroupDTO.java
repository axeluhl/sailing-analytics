package com.sap.sse.security.shared.dto;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import com.sap.sse.security.shared.SecurityUserGroupImpl;

public class StrippedUserGroupDTO extends SecurityUserGroupImpl<StrippedRoleDefinitionDTO> {
    private static final long serialVersionUID = 1L;

    @Deprecated
    // GWT serializer only
    StrippedUserGroupDTO() {
        super(null, null, new HashMap<StrippedRoleDefinitionDTO, Boolean>());
    }
    
    public StrippedUserGroupDTO(UUID id, String name) {
        this(id, name, new HashMap<StrippedRoleDefinitionDTO, Boolean>());
    }

    public StrippedUserGroupDTO(UUID id, String name, Map<StrippedRoleDefinitionDTO, Boolean> roleDefinitionMap) {
        super(id, name, roleDefinitionMap);
    }

}
