package com.sap.sailing.gwt.ui.server.security;

import java.util.Collections;

import org.apache.shiro.authz.Permission;
import org.apache.shiro.authz.permission.WildcardPermission;

import com.sap.sse.security.PermissionsForRoleProvider;
import com.sap.sse.security.shared.DefaultRoles;

public class SailingPermissionsForRoleProvider implements PermissionsForRoleProvider {
    @Override
    public Iterable<Permission> getPermissions(String role) {
        final Iterable<Permission> result;
        if (DefaultRoles.ADMIN.getRolename().equals(role)) {
            return Collections.<Permission>singletonList(new WildcardPermission("*"));
        } else {
            result = Collections.emptyList();
        }
        return result;
    }

}
