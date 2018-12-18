package com.sap.sse.security.shared.dto;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import com.sap.sse.common.Util;
import com.sap.sse.security.shared.WildcardPermission;
import com.sap.sse.security.shared.impl.SecurityUserImpl;

public class StrippedUserDTO extends SecurityUserImpl<RoleDefinitionDTO, RoleDTO, StrippedUserGroupDTO> {
    private static final long serialVersionUID = 1L;
    
    private Set<RoleDTO> roles;
    
    @Deprecated
    private StrippedUserDTO() {
        super(null);
    }

    public StrippedUserDTO(String name, Iterable<RoleDTO> roles, Iterable<WildcardPermission> permissions) {
        super(name, permissions);
        roles = new HashSet<>();
        Util.addAll(roles, this.getRolesInternal());
    }

    public StrippedUserDTO(String name) {
        this(name, Collections.<RoleDTO>emptySet(), Collections.<WildcardPermission>emptySet());
    }
    
    @Override
    protected Set<RoleDTO> getRolesInternal() {
        return roles;
    }
}
