package com.sap.sse.security.shared;

import java.util.Map;
import java.util.UUID;

import com.sap.sse.security.shared.impl.Ownership;

public interface SecurityUserGroup<RD extends RoleDefinition> {

    String getName();

    UUID getId();

    /**
     * Roles whose {@link RoleDefinition#getPermissions() permissions} are considered granted when accessing objects
     * {@link Ownership#getTenantOwner() owned by this group}. If the value to which a role definition maps is
     * {@code true}, the role's permissions are applied regardless of the user performing the access, only requiring the
     * object access to be owned by this group; otherwise, the role's permissions are only granted if the object
     * accessed is owned by this group and the user performing the access belongs to this group.
     */
    Map<RD, Boolean> getRoleDefinitionMap();

}