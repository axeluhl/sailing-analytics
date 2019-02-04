package com.sap.sse.security.shared.impl;

import java.util.Map;

import com.sap.sse.security.shared.RoleDefinition;
import com.sap.sse.security.shared.SecurityUserGroup;
import com.sap.sse.security.shared.WithQualifiedObjectIdentifier;

public interface UserGroup extends SecurityUserGroup<RoleDefinition>, WithQualifiedObjectIdentifier {

    Iterable<User> getUsers();

    void add(User user);

    void remove(User user);

    boolean contains(User user);

    Map<RoleDefinition, Boolean> getRoleDefinitionMap();

    void put(RoleDefinition roleDefinition, boolean forAll);

    void remove(RoleDefinition roleDefinition);

    boolean isRoleAssociated(RoleDefinition roleDefinition);

    /**
     * Returns {@code true}/{@code false} if the given RoleDefinition is associated to this UserGroup, {@code null}
     * otherwise.
     */
    Boolean getRoleAssociation(RoleDefinition roleDefinition);

}
