package com.sap.sse.security.shared.impl;

import com.sap.sse.security.shared.UserGroupDTO;
import com.sap.sse.security.shared.WildcardPermission;

public class StrippedUserDTO extends SecurityUserImpl<RoleDTO, UserGroupDTO> {
    private static final long serialVersionUID = 1L;

    public StrippedUserDTO(String name, Iterable<RoleDTO> roles, Iterable<WildcardPermission> permissions) {
        super(name, roles, permissions);
    }

    public StrippedUserDTO(String name) {
        super(name);
    }

}
