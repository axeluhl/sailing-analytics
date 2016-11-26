package com.sap.sse.security;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

import org.apache.shiro.authz.Permission;
import org.apache.shiro.authz.permission.WildcardPermission;

import com.sap.sse.security.shared.Role;

public class RolePermissionResolver implements org.apache.shiro.authz.permission.RolePermissionResolver {
    @Override
    public Collection<Permission> resolvePermissionsInRole(String rolename) {
        String[] split = rolename.split(":", 2);
        if (split.length < 3) {
            return null;
        }
        try {
            Set<Permission> permissions = new HashSet<>();
            for (String permissionString : new Role(split[0], split[1], split[2]).getPermissions()) {
                permissions.add(new WildcardPermission(permissionString));
            }
            return permissions;
        } catch (Role.EmptyTenantOrInstanceException e) {
            return null;
        }
    }
}