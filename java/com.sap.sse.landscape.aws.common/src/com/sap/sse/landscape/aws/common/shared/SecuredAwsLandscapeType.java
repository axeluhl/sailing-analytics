package com.sap.sse.landscape.aws.common.shared;

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
    private static final Set<SecuredAwsLandscapeType> allInstances = new HashSet<>();
    
    public SecuredAwsLandscapeType(String logicalTypeName, Action... availableActions) {
        super(logicalTypeName, availableActions);
        allInstances.add(this);
    }
    
    public SecuredAwsLandscapeType(String logicalTypeName) {
        super(logicalTypeName);
        allInstances.add(this);
    }
    
    public static Iterable<SecuredAwsLandscapeType> getAllInstances() {
        return Collections.unmodifiableSet(allInstances);
    }

    public static final HasPermissions REGION = new SecuredAwsLandscapeType("REGION");
    public static final HasPermissions INSTANCE = new SecuredAwsLandscapeType("INSTANCE");
    public static final HasPermissions LOAD_BALANCER = new SecuredAwsLandscapeType("LOAD_BALANCER");
}

