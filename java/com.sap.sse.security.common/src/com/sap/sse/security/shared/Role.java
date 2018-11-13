package com.sap.sse.security.shared;

import java.util.Set;

import com.sap.sse.common.Named;

/**
 * A {@link Role} is an instantiation of a {@link RoleDefinition}. In case a {@link RoleDefinition} is not
 * {@link #getQualifiedForTenant() qualified by a tenant} or {@link #getQualifiedForUser() user}, the {@link Role} may
 * serve as its own {@link RoleDefinition} object as it doesn't have to exist for different combinations of
 * {@link UserGroup} and/or {@link SecurityUser} parameters. Otherwise, {@link Role} objects will carry the necessary
 * actual {@link UserGroup} / {@link SecurityUser} parameter objects which then help to decide whether the parameterized
 * role implies its permissions for a given object.
 * <p>
 * 
 * A {@link Role} does not carry an ID of its own.
 * <p>
 * 
 * Equality ({@link #equals(Object)} and {@link #hashCode()} are expected to be based on the
 * {@link RoleDefinition#getId() ID} of the {@link #getRoleDefinition() role definition} on which this role is based,
 * plus the {@link #getQualifiedForTenant() tenant qualifier's} {@link UserGroup#getId() ID} and the
 * {@link #getQualifiedForUser()} field's {@link SecurityUser#getName() username}.
 * 
 * @author Axel Uhl (d043530)
 *
 */
public interface Role extends Named {
    RoleDefinition getRoleDefinition();
    
    Set<WildcardPermission> getPermissions();

    /**
     * @return the {@link UserGroup} that has to be the {@link Ownership#getTenantOwner() tenant owner} of an object in
     *         order for this role's {@link #getPermissions() permissions} to be granted to the user having this role;
     *         or {@code null} in case this role's {@link RoleDefinition} is not {@link #isTenantQualified() qualified
     *         by a tenant argument}.
     */
    UserGroup getQualifiedForTenant();
    
    /**
     * @return the {@link SecurityUser user} that has to be the {@link Ownership#getUserOwner() owning user} of an
     *         object in order for this role's {@link #getPermissions() permissions} to be granted to the user having
     *         this role; or {@code null} in case this role's {@link RoleDefinition} is not {@link #isUserQualified()
     *         qualified by a user argument}.
     */
    SecurityUser getQualifiedForUser();

    /**
     * @return the {@link #getQualifiedForTenant()} and {@link #getQualificationAsOwnership()} as {@link Ownership}
     */
    Ownership getQualificationAsOwnership();
}
