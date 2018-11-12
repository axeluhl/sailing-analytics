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
    /**
     * Returns the "raw" permissions explicitly set for this user. This does not include permissions
     * inferred by any {@link PermissionsForRoleProvider} for the {@link #getRoles() roles} that this
     * user has. Use {@link #getAllPermissions(PermissionsForRoleProvider)} for that.
     */
    Iterable<WildcardPermission> getPermissions();

    boolean hasRole(Role role);

    Iterable<Role> getRoles();

    /**
     * @return the groups this user is a member of; an implementation may not be able to reliably determine this, e.g.,
     *         if it represents a "stripped" copy of a user from which the group information has been removed. Other
     *         implementations may provide a "static" view on this data as it was when the object was created. Yet
     *         others may be able to dynamically query the user store for this information.
     */
    Iterable<UserGroup> getUserGroups();
}
