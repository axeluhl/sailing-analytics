package com.sap.sse.security.datamining.data.impl;

import com.sap.sse.security.datamining.data.HasRoleContext;
import com.sap.sse.security.shared.impl.Role;

/**
 * Equality is based on the {@link #getRole role}.
 * 
 * @author Axel Uhl (d043530)
 *
 */
public abstract class AbstractRoleOfUserWithContext implements HasRoleContext {
    private final Role role;
    
    public AbstractRoleOfUserWithContext(Role role) {
        this.role = role;
    }

    @Override
    public Role getRole() {
        return role;
    }
}
