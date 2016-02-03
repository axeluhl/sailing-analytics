package com.sap.sailing.domain.common.security;

import java.util.Collections;

import com.sap.sse.security.shared.DefaultRoles;
import com.sap.sse.security.shared.PermissionsForRoleProvider;

public class SailingPermissionsForRoleProvider implements PermissionsForRoleProvider {
    public static final SailingPermissionsForRoleProvider INSTANCE = new SailingPermissionsForRoleProvider();
    
    @Override
    public Iterable<String> getPermissions(String role) {
        final Iterable<String> result;
        if (DefaultRoles.ADMIN.getRolename().equals(role)) {
            return Collections.<String>singletonList("*");
        } else if (Roles.eventmanager.getRolename().equals(role)) {
            return Collections.<String>singletonList(Permission.MANAGE_MEDIA.getStringPermission());
        } else if (Roles.mediaeditor.getRolename().equals(role)) {
            return Collections.<String>singletonList(Permission.MANAGE_MEDIA.getStringPermission());
        } else {
            result = Collections.emptyList();
        }
        return result;
    }
}
