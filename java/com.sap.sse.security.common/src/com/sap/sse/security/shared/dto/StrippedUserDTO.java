package com.sap.sse.security.shared.dto;

import com.sap.sse.security.shared.WildcardPermission;
import com.sap.sse.security.shared.impl.SecurityUserImpl;

public class StrippedUserDTO extends SecurityUserImpl<RoleDefinitionDTO, RoleDTO, UserGroupDTO> {
    private static final long serialVersionUID = 1L;

    @Deprecated
    private StrippedUserDTO() {
        super(null);
    }

    public StrippedUserDTO(String name, Iterable<RoleDTO> roles, Iterable<WildcardPermission> permissions) {
        super(name, roles, permissions);
    }

    public StrippedUserDTO(String name) {
        super(name);
    }
}
