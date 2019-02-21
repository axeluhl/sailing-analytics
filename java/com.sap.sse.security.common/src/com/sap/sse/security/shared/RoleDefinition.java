package com.sap.sse.security.shared;

import java.util.Set;
import java.util.UUID;

import com.sap.sse.common.NamedWithID;
import com.sap.sse.common.Renamable;
import com.sap.sse.security.shared.impl.Ownership;
import com.sap.sse.security.shared.impl.Role;

/**
 * A role definition provides an ID, a (changeable) name and a set of permissions. As such, it represents a group of
 * permissions that are frequently assigned together to users. For example, if an administrator at an event usually
 * required create/read/update/delete permissions on a typical set of objects, those permissions can be grouped in a
 * role definition and then be assigned to a user by simply instantiating a {@link Role} from this
 * {@link RoleDefinition} and assigning the {@link Role} to the user.
 * <p>
 * 
 * During instantiating, a {@link Role} may optionally declare a {@link Tenant} and/or {@link SecurityUser} parameter to
 * qualify the range of objects for which it implies its permissions. With no such qualifications, the role grants its
 * permissions to the user to which the role is assigned for <em>all</em> objects. If a {@link Tenant} and/or
 * {@link SecurityUser} parameter is {@link AbstractRole#RoleImpl(RoleDefinition, Tenant, SecurityUser) declared}, the
 * object's {@link Ownership owning} {@link Ownership#getTenantOwner() tenant} and/or {@link Ownership#getUserOwner()
 * user} owner must match the actual {@link Tenant}/{@link SecurityUser} object(s) used when instantiating the
 * {@link Role} from this {@link RoleDefinition} for the user to obtain the permissions from this role definition. As
 * different parameterizations are possible for the same group of permissions, multiple {@link Role}s may be
 * instantiated from a single {@link RoleDefinition}.
 * <p>
 * 
 * Equality ({@link #equals(Object)}) and {@link #hashCode()} are expected to be based solely on the {@link #getId() ID}.
 * 
 * @author Axel Uhl (d043530)
 *
 */
public interface RoleDefinition extends NamedWithID, Renamable, WithQualifiedObjectIdentifier {
    Set<WildcardPermission> getPermissions();
    
    @Override
    UUID getId();

    void setPermissions(Iterable<WildcardPermission> permissions);
}
