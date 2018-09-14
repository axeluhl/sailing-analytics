package com.sap.sailing.domain.common.security;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import com.sap.sse.security.shared.WildcardPermission;
import com.sap.sse.security.shared.impl.WildcardPermissionEncoder;

public enum Permission implements com.sap.sse.security.shared.Permission {
    // AdminConsole permissions
    MANAGE_EVENTS,
    MANAGE_PAIRING_LISTS,
    MANAGE_REGATTAS,
    MANAGE_TRACKED_RACES,
    MANAGE_RACELOG_TRACKING,
    MANAGE_ALL_COMPETITORS,
    MANAGE_ALL_BOATS,
    MANAGE_COURSE_LAYOUT,
    MANAGE_WIND,
    MANAGE_IGTIMI_ACCOUNTS,
    MANAGE_EXPEDITION_DEVICE_CONFIGURATIONS,
    MANAGE_LEADERBOARDS,
    MANAGE_LEADERBOARD_RESULTS,
    MANAGE_LEADERBOARD_GROUPS,
    MANAGE_RESULT_IMPORT_URLS,
    MANAGE_STRUCTURE_IMPORT_URLS,
    MANAGE_MEDIA,
    MANAGE_SAILING_SERVER_INSTANCES,
    MANAGE_LOCAL_SERVER_INSTANCE,
    MANAGE_REPLICATION,
    MANAGE_MASTERDATA_IMPORT,
    MANAGE_DEVICE_CONFIGURATION,
    MANAGE_USERS,
    MANAGE_ROLES,
    MANAGE_FILE_STORAGE,
    MANAGE_MARK_PASSINGS,
    MANAGE_MARK_POSITIONS,
    CAN_REPLAY_DURING_LIVE_RACES,
    DETAIL_TIMER,
    
    // back-end permissions
    EVENT,
    REGATTA,
    LEADERBOARD,
    LEADERBOARD_GROUP,
    TRACKED_RACE,
    DATA_MINING,
    ;
    
    private static Set<Permission> adminConsolePermissions = new HashSet<>(Arrays.asList(
            MANAGE_EVENTS, MANAGE_ALL_COMPETITORS, MANAGE_ALL_BOATS, MANAGE_REGATTAS, MANAGE_LEADERBOARDS, MANAGE_LEADERBOARD_GROUPS,
            MANAGE_COURSE_LAYOUT, MANAGE_WIND, MANAGE_MEDIA, MANAGE_DEVICE_CONFIGURATION, MANAGE_TRACKED_RACES,
            MANAGE_IGTIMI_ACCOUNTS, MANAGE_EXPEDITION_DEVICE_CONFIGURATIONS, MANAGE_RESULT_IMPORT_URLS,
            MANAGE_STRUCTURE_IMPORT_URLS, MANAGE_REPLICATION, MANAGE_MASTERDATA_IMPORT, MANAGE_SAILING_SERVER_INSTANCES,
            MANAGE_LOCAL_SERVER_INSTANCE, MANAGE_USERS, MANAGE_ROLES, MANAGE_FILE_STORAGE));
    
    /**
     * Tells whether {@code permission} is one that describes the permission necessary to view
     * an AdminConsole permission.
     */
    public static boolean isAdminConsolePermission(Permission permission) {
        return adminConsolePermissions.contains(permission);
    }
    
    public static Iterable<Permission> getAdminConsolePermissions() {
        return Collections.unmodifiableSet(adminConsolePermissions);
    }

    // TODO once we can use Java8 here, move this up into a "default" method on the Permission interface
    @Override
    public String getStringPermission(com.sap.sse.security.shared.Permission.Mode... modes) {
        final String result;
        if (modes==null || modes.length==0) {
            result = name();
        } else {
            final StringBuilder modesString = new StringBuilder();
            boolean first = true;
            for (com.sap.sse.security.shared.Permission.Mode mode : modes) {
                if (first) {
                    first = false;
                } else {
                    modesString.append(',');
                }
                modesString.append(mode.getStringPermission());
            }
            result = name()+":"+modesString.toString();
        }
        return result;
    }

    @Override
    public WildcardPermission getPermission(com.sap.sse.security.shared.Permission.Mode... modes) {
        return new WildcardPermission(getStringPermission(modes), /* case sensitive */ true);
    }

    // TODO once we can use Java8 here, move this up into a "default" method on the Permission interface
    @Override
    public String getStringPermissionForObjects(com.sap.sse.security.shared.Permission.Mode mode, String... objectIdentifiers) {
        final WildcardPermissionEncoder permissionEncoder = new WildcardPermissionEncoder();
        final StringBuilder result = new StringBuilder(getStringPermission(mode));
        if (objectIdentifiers!=null && objectIdentifiers.length>0) {
            result.append(':');
            boolean first = true;
            for (String objectIdentifier : objectIdentifiers) {
                if (first) {
                    first = false;
                } else {
                    result.append(',');
                }
                result.append(permissionEncoder.encodeAsPermissionPart(getQualifiedObjectIdentifier(objectIdentifier)));
            }
        }
        return result.toString();
    }
    
    @Override
    public String getQualifiedObjectIdentifier(String objectIdentifier) {
        return name()+QUALIFIER_SEPARATOR+objectIdentifier;
    }

    @Override
    public WildcardPermission getPermissionForObjects(com.sap.sse.security.shared.Permission.Mode mode, String... objectIdentifiers) {
        return new WildcardPermission(getStringPermissionForObjects(mode, objectIdentifiers), /* case sensitive */ true);
    }
}
