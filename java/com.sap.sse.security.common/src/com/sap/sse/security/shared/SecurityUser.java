package com.sap.sse.security.shared;

import com.sap.sse.common.Named;
import com.sap.sse.common.NamedWithID;
import com.sap.sse.common.WithID;

/**
 * Represents a user of the application. Only a small subset of the attributes are exposed by this interface.
 * The {@link Named#getName() name} is the ID for this user; usually a nickname or short name. Implements the
 * {@link WithID} key, so {@link WithID#getId()} does return the result of {@link #getName()}.
 * 
 * @author Axel Uhl (d043530)
 *
 */
public interface SecurityUser extends NamedWithID {
    Tenant getDefaultTenant();

    boolean hasPermission(WildcardPermission permission);
    
    /**
     * Returns the "raw" permissions explicitly set for this user. This does not include permissions
     * inferred by any {@link PermissionsForRoleProvider} for the {@link #getRoles() roles} that this
     * user has. Use {@link #getAllPermissions(PermissionsForRoleProvider)} for that.
     */
    Iterable<WildcardPermission> getPermissions();

    boolean hasRole(Role role);

    Iterable<Role> getRoles();
}
