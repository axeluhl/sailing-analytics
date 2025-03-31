package com.sap.sse.security.shared.dto;

import com.sap.sse.security.shared.AbstractRole;

public class RoleDTO extends AbstractRole<StrippedRoleDefinitionDTO, StrippedUserGroupDTO, StrippedUserDTO> {
    private static final long serialVersionUID = 6135613835601810753L;

    @Deprecated // gwt serialisation only
    RoleDTO() {
    }

    public RoleDTO(StrippedRoleDefinitionDTO roleDefinition, StrippedUserGroupDTO qualifiedForTenant,
            StrippedUserDTO qualifiedForUser, boolean transitive) {
        super(roleDefinition, qualifiedForTenant, qualifiedForUser, transitive);
    }

    public RoleDTO(StrippedRoleDefinitionDTO roleDefinition, boolean transitive) {
        super(roleDefinition, transitive);
    }

}
