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
     * The tenant to use as {@link Ownership#getTenantOwner() tenant owner} of new objects created by this user
     */
    UserGroup getDefaultTenant();

    /**
     * Checks whether this user has the {@code permission} requested. For this, the {@link #getRoles() roles}
     * and {@link #getPermissions() permissions} are checked; however, since this method does not accept
     * {@link Ownership} or {@link AccessControlList} parameters, no further inferences are made.
     */
    boolean hasPermission(WildcardPermission permission);
    
    /**
     * Checks whether this user has the {@code permission} requested. For this, the {@link #getRoles() roles} and
     * {@link #getPermissions() permissions} are checked, furthermore if this user is the
     * {@link Ownership#getUserOwner() user owner} as per the {@code ownership} information, the permission will be
     * granted because users have all rights to the objects they own. Furthermore, tenant and user parameterized roles
     * will be applied based on the {@code ownership} information. No {@link AccessControlList} rules are applied here.
     */
    boolean hasPermission(WildcardPermission permission, Ownership ownership);
    
    /**
     * Checks whether this user has the {@code permission} requested. For this, the {@link #getRoles() roles} and
     * {@link #getPermissions() permissions} are checked, furthermore if this user is the
     * {@link Ownership#getUserOwner() user owner} as per the {@code ownership} information, the permission will be
     * granted because users have all rights to the objects they own. Furthermore, tenant and user parameterized roles
     * will be applied based on the {@code ownership} information. If the user belongs to one or more groups
     * ({@code groupsThisUserIsPartOf}) and a non-{@code null} {@code acl} is provided, the access control list
     * permissions are applied accordingly.
     */
    boolean hasPermission(WildcardPermission permission, Ownership ownership, Iterable<UserGroup> groupsThisUserIsPartOf, AccessControlList acl);
    
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
