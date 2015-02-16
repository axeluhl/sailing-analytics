package com.sap.sailing.gwt.ui.client.shared.security;

import java.util.Collections;

import com.sap.sse.security.shared.DefaultRoles;
import com.sap.sse.security.shared.PermissionsForRoleProvider;

public class SailingPermissionsForRoleProvider implements PermissionsForRoleProvider {
    @Override
    public Iterable<String> getPermissions(String role) {
        final Iterable<String> result;
        if (DefaultRoles.ADMIN.getRolename().equals(role)) {
            return Collections.<String>singletonList("*");
        } else {
            result = Collections.emptyList();
        }
        return result;
    }
}
