package com.sap.sse.security.datamining.data.impl;

import com.sap.sse.security.datamining.data.HasRoleOfUserInUserGroupContext;
import com.sap.sse.security.datamining.data.HasUserInUserGroupContext;
import com.sap.sse.security.shared.impl.Role;

/**
 * Equality is based on the {@link #getRole role}.
 * 
 * @author Axel Uhl (d043530)
 *
 */
public class RoleOfUserInUserGroupWithContext extends AbstractRoleOfUserWithContext implements HasRoleOfUserInUserGroupContext {
    private final HasUserInUserGroupContext userInUserGroupContext;
    
    public RoleOfUserInUserGroupWithContext(HasUserInUserGroupContext userInUserGroupContext, Role role) {
        super(role);
        this.userInUserGroupContext = userInUserGroupContext;
    }

    @Override
    public HasUserInUserGroupContext getUserInUserGroupContext() {
        return userInUserGroupContext;
    }
}
