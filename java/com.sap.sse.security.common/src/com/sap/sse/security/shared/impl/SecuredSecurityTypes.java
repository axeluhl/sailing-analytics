package com.sap.sse.security.shared.impl;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import com.sap.sse.security.shared.HasPermissions;
import com.sap.sse.security.shared.RoleDefinition;

/**
 * The basic types of logical objects provided by the security bundle that themselves have permissions governing how
 * users may deal with them.
 * 
 * @author Axel Uhl (d043530)
 *
 */
public class SecuredSecurityTypes extends HasPermissionsImpl {
    private static final long serialVersionUID = -5052828472297142038L;
    private static Set<HasPermissions> allInstances = new HashSet<>();
    
    public SecuredSecurityTypes(String logicalTypeName, Action... availableActions) {
        super(logicalTypeName, availableActions);
        allInstances.add(this);
    }

    public SecuredSecurityTypes(String logicalTypeName) {
        super(logicalTypeName);
        allInstances.add(this);
    }

    public static Iterable<HasPermissions> getAllInstances() {
        return Collections.unmodifiableSet(allInstances);
    }

    public static enum PublicReadableActions implements Action {
        READ_PUBLIC;

        public static final Action[] READ_AND_READ_PUBLIC_ACTIONS = new Action[] { READ_PUBLIC, DefaultActions.READ };
    };


    public static enum UserActions implements Action {
        /** Update a user's password without knowing the old password. */
        FORCE_OVERWRITE_PASSWORD
    };
    /**
     * type-relative identifier is the {@link User#getName() username}.
     */
    public static final HasPermissions USER = new SecuredSecurityTypes("USER", DefaultActions
            .plus(UserActions.FORCE_OVERWRITE_PASSWORD, PublicReadableActions.READ_PUBLIC));

    /**
     * type-relative identifier is the {@link RoleDefinition#getId() role ID's} string representation
     */
    public static final HasPermissions ROLE_DEFINITION = new SecuredSecurityTypes("ROLE_DEFINITION");
    
    /**
     * type-relative identifier is obtainable via {@link PermissionAndRoleAssociation#getId()}
     */
    public static final HasPermissions ROLE_ASSOCIATION = new SecuredSecurityTypes("ROLE_ASSOCIATION");

    /**
     * type-relative identifier is obtainable via {@link PermissionAndRoleAssociation#getId()}
     */
    public static final HasPermissions PERMISSION_ASSOCIATION = new SecuredSecurityTypes("PERMISSION_ASSOCIATION");

    /**
     * type-relative identifier is the {@link UserGroupImpl#getId() group ID's} string representation
     */
    public static final HasPermissions USER_GROUP = new SecuredSecurityTypes("USER_GROUP");

    public static enum ServerActions implements Action {
        CONFIGURE_FILE_STORAGE,
        CONFIGURE_LOCAL_SERVER,
        CONFIGURE_REMOTE_INSTANCES,
        IMPORT_MASTER_DATA,
        CREATE_OBJECT,

        /**
         * This permission is used to check READ-permission on different things. For that the object type to determine
         * the permission strings is String (e.g. server name, DataRetrieverChainDefinitionDTO.name,
         * RetrieverChainDefinition. name, QueryIdentifier, ...)
         */
        DATA_MINING,

        CAN_IMPORT_MASTERDATA,
        /**
         * Secures replication actions on the master side.
         */
        REPLICATE,
        /**
         * Secures replication actions on the replica side.
         */
        START_REPLICATION,
        /**
         * Secures the replication information provided through ReplicationServlet as well as AdminConsole.
         */
        READ_REPLICATOR;

        private static final Action[] ALL_ACTIONS = new Action[] { CONFIGURE_FILE_STORAGE, CONFIGURE_LOCAL_SERVER,
                CONFIGURE_REMOTE_INSTANCES, IMPORT_MASTER_DATA, CREATE_OBJECT, CAN_IMPORT_MASTERDATA, DATA_MINING,
                REPLICATE, START_REPLICATION, READ_REPLICATOR,
                DefaultActions.CHANGE_OWNERSHIP, DefaultActions.CHANGE_ACL, DefaultActions.UPDATE };
    }

    /**
     * represents the logical server which may consist of a master and multiple replicas and has a unique server name;
     * type-relative identifier is the server name
     */
    public static final HasPermissions SERVER = new SecuredSecurityTypes("SERVER", ServerActions.ALL_ACTIONS);
}
