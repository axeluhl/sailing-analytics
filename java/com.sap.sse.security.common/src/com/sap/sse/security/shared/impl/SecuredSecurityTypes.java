package com.sap.sse.security.shared.impl;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import com.sap.sse.security.shared.HasPermissions;
import com.sap.sse.security.shared.IdentifierStrategy;
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
    
    public SecuredSecurityTypes(String logicalTypeName, IdentifierStrategy identifierStrategy, Action... availableActions) {
        super(logicalTypeName, identifierStrategy, availableActions);
        allInstances.add(this);
    }

    public SecuredSecurityTypes(String logicalTypeName, IdentifierStrategy identifierStrategy) {
        super(logicalTypeName, identifierStrategy);
        allInstances.add(this);
    }

    public static Iterable<HasPermissions> getAllInstances() {
        return Collections.unmodifiableSet(allInstances);
    }

    public static enum UserActions implements Action { GRANT_PERMISSION, REVOKE_PERMISSION };
    /**
     * type-relative identifier is the {@link User#getName() username}.
     */
    public static final HasPermissions USER = new SecuredSecurityTypes("USER", IdentifierStrategy.NAMED, DefaultActions.plus(UserActions.GRANT_PERMISSION, UserActions.REVOKE_PERMISSION));

    /**
     * type-relative identifier is the {@link RoleDefinition#getId() role ID's} string representation
     */
    public static final HasPermissions ROLE_DEFINITION = new SecuredSecurityTypes("ROLE_DEFINITION", IdentifierStrategy.ID);
    
    /**
     * type-relative identifier is the {@link UserGroupImpl#getId() group ID's} string representation
     */
    public static final HasPermissions USER_GROUP = new SecuredSecurityTypes("USER_GROUP", IdentifierStrategy.ID);

    public static enum ServerActions implements Action {
        CONFIGURE_FILE_STORAGE,
        CONFIGURE_LOCAL_SERVER,
        CONFIGURE_REMOTE_INSTANCES,
        IMPORT_MASTER_DATA,
        CREATE_OBJECT
    }

    /**
     * represents the logical server which may consist of a master and multiple replicas and has a unique server name;
     * type-relative identifier is the server name
     */
    public static final HasPermissions SERVER = new SecuredSecurityTypes("SERVER", IdentifierStrategy.SERVER, ServerActions.values());

}
