package com.sap.sse.landscape.common.shared;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import com.sap.sse.security.shared.HasPermissions;
import com.sap.sse.security.shared.impl.HasPermissionsImpl;
import com.sap.sse.security.shared.impl.SecuredSecurityTypes;

/**
 * The basic types of logical objects provided by the landscape bundle that themselves have permissions governing how
 * users may deal with them.
 * 
 * @author Axel Uhl (d043530)
 *
 */
public class SecuredLandscapeTypes extends HasPermissionsImpl {
    private static final long serialVersionUID = -5052828472297142038L;
    private static Set<HasPermissions> allInstances = new HashSet<>();
    
    public SecuredLandscapeTypes(String logicalTypeName, Action... availableActions) {
        super(logicalTypeName, availableActions);
        allInstances.add(this);
    }

    public SecuredLandscapeTypes(String logicalTypeName) {
        super(logicalTypeName);
        allInstances.add(this);
    }

    public static Iterable<HasPermissions> getAllInstances() {
        return Collections.unmodifiableSet(allInstances);
    }

    /**
     * The type-relative identifier consists of the region ID and the key name.
     */
    public static final HasPermissions SSH_KEY = new SecuredLandscapeTypes("SSH_KEY");
    
    public static enum LandscapeActions implements Action {
        MANAGE;

        private static final Action[] ALL_ACTIONS = new Action[] { MANAGE,
                DefaultActions.CHANGE_OWNERSHIP, DefaultActions.CHANGE_ACL };
    }
    
    public static final HasPermissions LANDSCAPE = new SecuredSecurityTypes("LANDSCAPE", LandscapeActions.ALL_ACTIONS);
}

