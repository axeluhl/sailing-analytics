package com.sap.sse.security.shared.impl;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import com.sap.sse.security.shared.HasPermissions;

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

    public static final HasPermissions USER = new SecuredSecurityTypes("USER");
    public static final HasPermissions USER_GROUP = new SecuredSecurityTypes("USER_GROUP");
}
