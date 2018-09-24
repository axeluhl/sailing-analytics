package com.sap.sailing.domain.common.security;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import com.sap.sse.security.shared.HasPermissions;
import com.sap.sse.security.shared.QualifiedObjectIdentifier;
import com.sap.sse.security.shared.WildcardPermission;
import com.sap.sse.security.shared.impl.QualifiedObjectIdentifierImpl;
import com.sap.sse.security.shared.impl.WildcardPermissionEncoder;

public enum Permission implements HasPermissions {
    // AdminConsole permissions
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
    DETAIL_TIMER, // TODO this is not a valid "HasPermission" instance; it's more an operation the user may be granted on objects of the TimePanel type
    
    // back-end permissions
    EVENT,
    REGATTA,
    LEADERBOARD,
    LEADERBOARD_GROUP,
    TRACKED_RACE,
    DATA_MINING,
    SERVER, // represents the logical server which may consist of a master and multiple replicas and has a unique server name 
    ;
    
    private static Set<Permission> adminConsolePermissions = new HashSet<>(Arrays.asList(
            MANAGE_LEADERBOARDS, MANAGE_LEADERBOARD_GROUPS,
            MANAGE_MEDIA, MANAGE_DEVICE_CONFIGURATION,
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
    public String getStringPermission(HasPermissions.Action... modes) {
        final String result;
        if (modes==null || modes.length==0) {
            result = name();
        } else {
            final StringBuilder modesString = new StringBuilder();
            boolean first = true;
            for (HasPermissions.Action mode : modes) {
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
    public WildcardPermission getPermission(HasPermissions.Action... modes) {
        return new WildcardPermission(getStringPermission(modes));
    }

    // TODO once we can use Java8 here, move this up into a "default" method on the Permission interface
    @Override
    public String getStringPermissionForObjects(HasPermissions.Action mode, String... typeRelativeObjectIdentifiers) {
        final WildcardPermissionEncoder permissionEncoder = new WildcardPermissionEncoder();
        final StringBuilder result = new StringBuilder(getStringPermission(mode));
        if (typeRelativeObjectIdentifiers!=null && typeRelativeObjectIdentifiers.length>0) {
            result.append(':');
            boolean first = true;
            for (String typeRelativeObjectIdentifier : typeRelativeObjectIdentifiers) {
                if (first) {
                    first = false;
                } else {
                    result.append(',');
                }
                result.append(permissionEncoder.encodeAsPermissionPart(typeRelativeObjectIdentifier));
            }
        }
        return result.toString();
    }
    
    @Override
    public QualifiedObjectIdentifier getQualifiedObjectIdentifier(String typeRelativeObjectIdentifier) {
        return new QualifiedObjectIdentifierImpl(name(), typeRelativeObjectIdentifier);
    }

    @Override
    public WildcardPermission getPermissionForObjects(HasPermissions.Action mode, String... objectIdentifiers) {
        return new WildcardPermission(getStringPermissionForObjects(mode, objectIdentifiers));
    }
}
