package com.sap.sse.security.shared.impl;

import com.sap.sse.security.shared.AbstractRole;
import com.sap.sse.security.shared.RoleDefinition;

public class Role extends AbstractRole<RoleDefinition, UserGroup, User> {
    private static final long serialVersionUID = 1L;

    public Role(RoleDefinition roleDefinition, UserGroup qualifiedForTenant, User qualifiedForUser) {
        super(roleDefinition, qualifiedForTenant, qualifiedForUser);
    }

    public Role(RoleDefinition roleDefinition) {
        super(roleDefinition);
    }

    public Ownership getQualificationAsOwnership() {
        if (qualifiedForTenant != null || qualifiedForUser != null) {
            return new Ownership(qualifiedForUser, qualifiedForTenant);
        }
        return null;
    }

}
