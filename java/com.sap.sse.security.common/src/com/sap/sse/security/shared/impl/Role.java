package com.sap.sse.security.shared.impl;

import com.sap.sse.security.shared.AbstractRole;
import com.sap.sse.security.shared.RoleDefinition;

public class Role extends AbstractRole<RoleDefinition, UserGroup, User> {
    private static final long serialVersionUID = 1L;

    public Role(RoleDefinition roleDefinition, UserGroup qualifiedForTenant, User qualifiedForUser, Boolean transitive) {
        super(roleDefinition, qualifiedForTenant, qualifiedForUser, transitive);
    }

    public Role(RoleDefinition roleDefinition, Boolean transitive) {
        super(roleDefinition, transitive);
    }
    
    /**
     * If {@link #transitive} is {@code null} on this instance, this method replaces this de-serialized object with one
     * that has {@link #transitive} set to the default of {@code true}.
     */
    private Object readResolve() {
        final Role result;
        if (this.transitive == null) {
            result = new Role(roleDefinition, qualifiedForTenant, qualifiedForUser, /* transitive */ true);
        } else {
            result = this;
        }
        return result;
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
