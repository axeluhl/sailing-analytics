package com.sap.sse.security.shared.impl;

import com.sap.sse.security.shared.AbstractRole;
import com.sap.sse.security.shared.RoleDefinition;

public class Role extends AbstractRole<RoleDefinition, UserGroup, User> {
    private static final long serialVersionUID = 5730465771729662605L;

    public Role(RoleDefinition roleDefinition, UserGroup qualifiedForTenant, User qualifiedForUser, boolean transitive) {
        super(roleDefinition, qualifiedForTenant, qualifiedForUser, transitive);
    }

    public Role(RoleDefinition roleDefinition, boolean transitive) {
        super(roleDefinition, transitive);
    }

    public Ownership getQualificationAsOwnership() {
        final Ownership result;
        if (qualifiedForTenant != null || qualifiedForUser != null) {
            result = new Ownership(qualifiedForUser, qualifiedForTenant);
        } else {
            result = null;
        }
        return result;
    }
}
