package com.sap.sailing.domain.common.security;

import java.util.ArrayList;
import java.util.Collections;

import com.sap.sse.security.shared.AbstractRoles;
import com.sap.sse.security.shared.DefaultRoles;
import com.sap.sse.security.shared.PermissionsForRoleProvider;
import com.sap.sse.security.shared.RolePermissionModel;

public class SailingPermissionsForRoleProvider implements PermissionsForRoleProvider {
    public static final SailingPermissionsForRoleProvider INSTANCE = new SailingPermissionsForRoleProvider();
    
    @Override
    public Iterable<String> getPermissions(String role, RolePermissionModel rolePermissionModel) {
        final Iterable<String> result;
        if (rolePermissionModel == null) {
            if (DefaultRoles.ADMIN.getRolename().equals(role)) {
                result = DefaultRoles.ADMIN.getPermissions();
            } else if (AbstractRoles.eventmanager.getRolename().equals(role)) {
                result = asList(
                        // RaceBoard:
                        Permission.MANAGE_MEDIA,
                        Permission.MANAGE_MARK_PASSINGS,
                        Permission.MANAGE_MARK_POSITIONS,
                        
                        // AdminConsole:
                        Permission.MANAGE_ALL_COMPETITORS,
                        Permission.MANAGE_COURSE_LAYOUT,
                        Permission.MANAGE_DEVICE_CONFIGURATION,
                        Permission.MANAGE_EVENTS,
                        Permission.MANAGE_IGTIMI_ACCOUNTS,
                        Permission.MANAGE_LEADERBOARD_GROUPS,
                        Permission.MANAGE_LEADERBOARDS,
                        Permission.MANAGE_LEADERBOARD_RESULTS,
                        Permission.MANAGE_MEDIA,
                        Permission.MANAGE_RACELOG_TRACKING,
                        Permission.MANAGE_REGATTAS,
                        Permission.MANAGE_RESULT_IMPORT_URLS,
                        Permission.MANAGE_STRUCTURE_IMPORT_URLS,
                        Permission.MANAGE_TRACKED_RACES,
                        Permission.MANAGE_WIND,
                        
                        // back-end:
                        Permission.EVENT,
                        Permission.REGATTA,
                        Permission.LEADERBOARD,
                        Permission.LEADERBOARD_GROUP
                        );
            } else if (AbstractRoles.mediaeditor.getRolename().equals(role)) {
                result = asList(Permission.MANAGE_MEDIA);
            } else if (AbstractRoles.moderator.getRolename().equals(role)) {
                result = asList(Permission.CAN_REPLAY_DURING_LIVE_RACES);
            } else {
                result = Collections.emptyList();
            }
        } else {
            result = null; // TODO
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
