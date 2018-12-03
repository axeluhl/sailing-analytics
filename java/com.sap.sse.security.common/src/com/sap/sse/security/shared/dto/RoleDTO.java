package com.sap.sse.security.shared.dto;

import com.sap.sse.security.shared.AbstractRole;

public class RoleDTO extends AbstractRole<RoleDefinitionDTO, StrippedUserGroupDTO, StrippedUserDTO> {
    private static final long serialVersionUID = 1L;

    @Deprecated // gwt serialisation only
    RoleDTO() {
    }

    public RoleDTO(RoleDefinitionDTO roleDefinition, StrippedUserGroupDTO qualifiedForTenant,
            StrippedUserDTO qualifiedForUser) {
        super(roleDefinition, qualifiedForTenant, qualifiedForUser);
    }

    public RoleDTO(RoleDefinitionDTO roleDefinition) {
        super(roleDefinition);
    }

}
