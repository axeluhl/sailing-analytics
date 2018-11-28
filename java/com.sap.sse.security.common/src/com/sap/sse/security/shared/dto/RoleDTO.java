package com.sap.sse.security.shared.dto;

import com.sap.sse.security.shared.AbstractRole;

public class RoleDTO extends AbstractRole<RoleDefinitionDTO, UserGroupDTO, StrippedUserDTO> {
    private static final long serialVersionUID = 1L;

    @Deprecated
    private RoleDTO() {
        super(null);
    }

    public RoleDTO(RoleDefinitionDTO roleDefinition, UserGroupDTO qualifiedForTenant,
            StrippedUserDTO qualifiedForUser) {
        super(roleDefinition, qualifiedForTenant, qualifiedForUser);
    }

    public RoleDTO(RoleDefinitionDTO roleDefinition) {
        super(roleDefinition);
    }

}
