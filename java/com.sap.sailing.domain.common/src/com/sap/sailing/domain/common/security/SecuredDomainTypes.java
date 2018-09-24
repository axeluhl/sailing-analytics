package com.sap.sailing.domain.common.security;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import com.sap.sse.security.shared.HasPermissions;
import com.sap.sse.security.shared.impl.HasPermissionsImpl;

/**
 * Logical domain types in the "sailing" domain that require the user to have certain permissions
 * in order to use their actions. These types are defined here in the "common" bundle so that
 * the server as well as the client can check them.
 * 
 * @author Axel Uhl (d043530)
 *
 */
public class SecuredDomainTypes extends HasPermissionsImpl {
    private static final long serialVersionUID = -7072719056136061490L;
    private static final Set<HasPermissions> allInstances = new HashSet<>();
    
    public SecuredDomainTypes(String logicalTypeName, Action... availableActions) {
        super(logicalTypeName, availableActions);
        allInstances.add(this);
    }
    public SecuredDomainTypes(String logicalTypeName) {
        super(logicalTypeName);
        allInstances.add(this);
    }
    
    public static Iterable<HasPermissions> getAllInstances() {
        return Collections.unmodifiableSet(allInstances);
    }
    
    // AdminConsole permissions
    public static final HasPermissions MANAGE_IGTIMI_ACCOUNTS = new SecuredDomainTypes("MANAGE_IGTIMI_ACCOUNTS");
    public static final HasPermissions MANAGE_EXPEDITION_DEVICE_CONFIGURATIONS = new SecuredDomainTypes("MANAGE_EXPEDITION_DEVICE_CONFIGURATIONS");
    public static final HasPermissions MANAGE_LEADERBOARDS = new SecuredDomainTypes("MANAGE_LEADERBOARDS");
    public static final HasPermissions MANAGE_LEADERBOARD_RESULTS = new SecuredDomainTypes("MANAGE_LEADERBOARD_RESULTS");
    public static final HasPermissions MANAGE_LEADERBOARD_GROUPS = new SecuredDomainTypes("MANAGE_LEADERBOARD_GROUPS");
    public static final HasPermissions MANAGE_RESULT_IMPORT_URLS = new SecuredDomainTypes("MANAGE_RESULT_IMPORT_URLS");
    public static final HasPermissions MANAGE_STRUCTURE_IMPORT_URLS = new SecuredDomainTypes("MANAGE_STRUCTURE_IMPORT_URLS");
    public static final HasPermissions MANAGE_MEDIA = new SecuredDomainTypes("MANAGE_MEDIA");
    public static final HasPermissions MANAGE_SAILING_SERVER_INSTANCES = new SecuredDomainTypes("MANAGE_SAILING_SERVER_INSTANCES");
    public static final HasPermissions MANAGE_LOCAL_SERVER_INSTANCE = new SecuredDomainTypes("MANAGE_LOCAL_SERVER_INSTANCE");
    public static final HasPermissions MANAGE_REPLICATION = new SecuredDomainTypes("MANAGE_REPLICATION");
    public static final HasPermissions MANAGE_MASTERDATA_IMPORT = new SecuredDomainTypes("MANAGE_MASTERDATA_IMPORT");
    public static final HasPermissions MANAGE_USERS = new SecuredDomainTypes("MANAGE_USERS");
    public static final HasPermissions MANAGE_ROLES = new SecuredDomainTypes("MANAGE_ROLES");
    public static final HasPermissions MANAGE_FILE_STORAGE = new SecuredDomainTypes("MANAGE_FILE_STORAGE");
    public static final HasPermissions MANAGE_MARK_PASSINGS = new SecuredDomainTypes("MANAGE_MARK_PASSINGS");
    public static final HasPermissions MANAGE_MARK_POSITIONS = new SecuredDomainTypes("MANAGE_MARK_POSITIONS");
    public static final HasPermissions CAN_REPLAY_DURING_LIVE_RACES = new SecuredDomainTypes("CAN_REPLAY_DURING_LIVE_RACES");
    public static final HasPermissions DETAIL_TIMER = new SecuredDomainTypes("DETAIL_TIMER"); // TODO this is not a valid "HasPermission" instance; it's more an operation the user may be granted on objects of the TimePanel type
    
    // back-end permissions
    public static final HasPermissions EVENT = new SecuredDomainTypes("EVENT");
    public static final HasPermissions REGATTA = new SecuredDomainTypes("REGATTA");
    public static final HasPermissions LEADERBOARD = new SecuredDomainTypes("LEADERBOARD");
    public static final HasPermissions LEADERBOARD_GROUP = new SecuredDomainTypes("LEADERBOARD_GROUP");
    public static final HasPermissions TRACKED_RACE = new SecuredDomainTypes("TRACKED_RACE");
    public static final HasPermissions DATA_MINING = new SecuredDomainTypes("DATA_MINING");
    public static final HasPermissions RACE_MANAGER_APP_DEVICE_CONFIGURATION = new SecuredDomainTypes("RACE_MANAGER_APP_DEVICE_CONFIGURATION");
    public static final HasPermissions SERVER = new SecuredDomainTypes("SERVER"); // represents the logical server which may consist of a master and multiple replicas and has a unique server name 
}
