package com.sap.sse.security.shared.dto;

import java.io.Serializable;
import java.util.HashSet;
import java.util.Set;

import com.sap.sse.common.Util;

public class RolesAndPermissionsForUserDTO implements Serializable{
    private static final long serialVersionUID = -7896556571617598329L;
    private Set<RoleWithSecurityDTO> roles = new HashSet<>();
    private Set<WildcardPermissionWithSecurityDTO> permissions = new HashSet<>();

    @Deprecated //gwt only
    RolesAndPermissionsForUserDTO() {
    }

    public RolesAndPermissionsForUserDTO(Iterable<RoleWithSecurityDTO> roles,
            Iterable<WildcardPermissionWithSecurityDTO> permissions) {
        Util.addAll(roles, this.roles);
        Util.addAll(permissions, this.permissions);
    }

    public Set<RoleWithSecurityDTO> getRoles() {
        return roles;
    }

    public void setRoles(Set<RoleWithSecurityDTO> roles) {
        this.roles = roles;
    }

    public Set<WildcardPermissionWithSecurityDTO> getPermissions() {
        return permissions;
    }

    public void setPermissions(Set<WildcardPermissionWithSecurityDTO> permissions) {
        this.permissions = permissions;
    }

}
