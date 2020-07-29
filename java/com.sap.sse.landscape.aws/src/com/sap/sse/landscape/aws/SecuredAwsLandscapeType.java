package com.sap.sse.landscape.aws;

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
public class SecuredAwsLandscapeType extends HasPermissionsImpl {
    private static final long serialVersionUID = -7072719056136061490L;
    private static final Set<HasPermissions> allInstances = new HashSet<>();
    
    public SecuredAwsLandscapeType(String logicalTypeName, Action... availableActions) {
        super(logicalTypeName, availableActions);
        allInstances.add(this);
    }
    
    public SecuredAwsLandscapeType(String logicalTypeName) {
        super(logicalTypeName);
        allInstances.add(this);
    }
    
    public static Iterable<HasPermissions> getAllInstances() {
        return Collections.unmodifiableSet(allInstances);
    }

    public static final HasPermissions LANDSCAPE = new SecuredAwsLandscapeType("LANDSCAPE", LandscapeActions.ALL_ACTIONS);
    public static final HasPermissions REGION = new SecuredAwsLandscapeType("REGION");
    public static final HasPermissions INSTANCE = new SecuredAwsLandscapeType("INSTANCE");
    public static final HasPermissions LOAD_BALANCER = new SecuredAwsLandscapeType("LOAD_BALANCER");
    public static final HasPermissions SSH_KEY_PAIR = new SecuredAwsLandscapeType("SSH_KEY_PAIR");
    
    public static enum LandscapeActions implements Action {
        DUMMY_READ_LANDSCAPE_ACTION, DUMMY_WRITE_LANDSCAPE_ACTION;

        private static final Action[] ALL_ACTIONS = DefaultActions.plus(DUMMY_READ_LANDSCAPE_ACTION);

        public static final Action[] MUTATION_ACTIONS = new Action[] { DUMMY_WRITE_LANDSCAPE_ACTION, DefaultActions.DELETE,
                DefaultActions.CREATE, DefaultActions.UPDATE, DefaultActions.CHANGE_OWNERSHIP,
                DefaultActions.CHANGE_ACL };
    }
}

