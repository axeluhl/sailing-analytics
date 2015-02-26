package com.sap.sailing.domain.common.security;

import java.util.Arrays;
import java.util.Collections;

import com.sap.sse.security.shared.DefaultRoles;
import com.sap.sse.security.shared.PermissionsForRoleProvider;

public class SailingPermissionsForRoleProvider implements PermissionsForRoleProvider {
    public static final SailingPermissionsForRoleProvider INSTANCE = new SailingPermissionsForRoleProvider();
    
    @Override
    public Iterable<String> getPermissions(String role) {
        final Iterable<String> result;
        if (DefaultRoles.ADMIN.getRolename().equals(role)) {
            result = Collections.<String>singletonList("*");
        } else if (Roles.eventmanager.getRolename().equals(role)) {
            result = Arrays.asList(
                    // AdminConsole:
                    Permission.MANAGE_ALL_COMPETITORS.getStringPermission(),
                    Permission.MANAGE_COURSE_LAYOUT.getStringPermission(),
                    Permission.MANAGE_DEVICE_CONFIGURATION.getStringPermission(),
                    Permission.MANAGE_EVENTS.getStringPermission(),
                    Permission.MANAGE_IGTIMI_ACCOUNTS.getStringPermission(),
                    Permission.MANAGE_LEADERBOARD_GROUPS.getStringPermission(),
                    Permission.MANAGE_LEADERBOARDS.getStringPermission(),
                    Permission.MANAGE_MEDIA.getStringPermission(),
                    Permission.MANAGE_RACELOG_TRACKING.getStringPermission(),
                    Permission.MANAGE_REGATTAS.getStringPermission(),
                    Permission.MANAGE_RESULT_IMPORT_URLS.getStringPermission(),
                    Permission.MANAGE_STRUCTURE_IMPORT_URLS.getStringPermission(),
                    Permission.MANAGE_TRACKED_RACES.getStringPermission(),
                    Permission.MANAGE_WIND.getStringPermission(),
                    
                    // back-end:
                    Permission.EVENT.getStringPermission(),
                    Permission.REGATTA.getStringPermission(),
                    Permission.LEADERBOARD.getStringPermission(),
                    Permission.LEADERBOARD_GROUP.getStringPermission()
                    );
        } else {
            result = Collections.emptyList();
        }
        return result;
    }
}
