package com.sap.sse.security.datamining.data.impl;

import com.sap.sse.security.datamining.data.HasRoleOfUserContext;
import com.sap.sse.security.datamining.data.HasUserContext;
import com.sap.sse.security.shared.impl.Role;

/**
 * Equality is based on the {@link #getRole role}.
 * 
 * @author Axel Uhl (d043530)
 *
 */
public class RoleOfUserWithContext extends AbstractRoleOfUserWithContext implements HasRoleOfUserContext {
    private final HasUserContext userContext;
    
    public RoleOfUserWithContext(HasUserContext userContext, Role role) {
        super(role);
        this.userContext = userContext;
    }

    @Override
    public HasUserContext getUserContext() {
        return userContext;
    }
}
