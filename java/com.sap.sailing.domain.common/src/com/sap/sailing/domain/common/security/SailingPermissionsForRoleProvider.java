package com.sap.sailing.domain.common.security;

import java.util.ArrayList;
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
            return asList(Permission.MANAGE_MEDIA, Permission.MANAGE_MARK_PASSINGS);
        } else if (Roles.mediaeditor.getRolename().equals(role)) {
            return Collections.<String>singletonList(Permission.MANAGE_MEDIA.getStringPermission());
        } else {
            result = Collections.emptyList();
        }
        return result;
    }
    
    private Iterable<String> asList(Permission... permissions) {
        ArrayList<String> list = new ArrayList<String>(permissions.length);
        for (Permission permission : permissions) {
            list.add(permission.getStringPermission());
        }
        return list;
    }
}
