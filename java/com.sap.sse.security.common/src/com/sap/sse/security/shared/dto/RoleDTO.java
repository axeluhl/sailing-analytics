package com.sap.sse.security.shared.dto;

import com.sap.sse.security.shared.AbstractRole;

public class RoleDTO extends AbstractRole<StrippedRoleDefinitionDTO, StrippedUserGroupDTO, StrippedUserDTO> {
    private static final long serialVersionUID = 1L;

    @Deprecated // gwt serialisation only
    RoleDTO() {
    }

    public RoleDTO(StrippedRoleDefinitionDTO roleDefinition, StrippedUserGroupDTO qualifiedForTenant,
            StrippedUserDTO qualifiedForUser) {
        super(roleDefinition, qualifiedForTenant, qualifiedForUser);
    }

    public RoleDTO(StrippedRoleDefinitionDTO roleDefinition) {
        super(roleDefinition);
    }

}
