package com.sap.sse.security.shared.impl;

import com.sap.sse.security.shared.AbstractRole;
import com.sap.sse.security.shared.RoleDefinition;
import com.sap.sse.security.shared.UserGroupDTO;

public class RoleDTO extends AbstractRole<UserGroupDTO, StrippedUserDTO> {
    private static final long serialVersionUID = 1L;

    public RoleDTO(RoleDefinition roleDefinition, UserGroupDTO qualifiedForTenant, StrippedUserDTO qualifiedForUser) {
        super(roleDefinition, qualifiedForTenant, qualifiedForUser);
    }

    public RoleDTO(RoleDefinition roleDefinition) {
        super(roleDefinition);
    }

}
