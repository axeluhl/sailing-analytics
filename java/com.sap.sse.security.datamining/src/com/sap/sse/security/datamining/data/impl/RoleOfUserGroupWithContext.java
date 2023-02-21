package com.sap.sse.security.datamining.data.impl;

import com.sap.sse.security.datamining.data.HasRoleOfUserGroupContext;
import com.sap.sse.security.datamining.data.HasUserGroupContext;
import com.sap.sse.security.shared.RoleDefinition;

/**
 * Equality is based on the {@link #getRoleDefinition() role definition} and the {@link #isForAll() "for all"} flag.
 * 
 * @author Axel Uhl (d043530)
 *
 */
public class RoleOfUserGroupWithContext implements HasRoleOfUserGroupContext {
    private final HasUserGroupContext userGroupContext;
    
    private final RoleDefinition roleDefinition;
    
    private final boolean forAll;
    
    public RoleOfUserGroupWithContext(HasUserGroupContext userGroupContext, RoleDefinition roleDefinition, boolean forAll) {
        this.userGroupContext = userGroupContext;
        this.roleDefinition = roleDefinition;
        this.forAll = forAll;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + (forAll ? 1231 : 1237);
        result = prime * result + ((roleDefinition == null) ? 0 : roleDefinition.hashCode());
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        RoleOfUserGroupWithContext other = (RoleOfUserGroupWithContext) obj;
        if (forAll != other.forAll)
            return false;
        if (roleDefinition == null) {
            if (other.roleDefinition != null)
                return false;
        } else if (!roleDefinition.equals(other.roleDefinition))
            return false;
        return true;
    }

    @Override
    public HasUserGroupContext getUserGroupContext() {
        return userGroupContext;
    }

    private RoleDefinition getRoleDefinition() {
        return roleDefinition;
    }
    
    @Override
    public String getRoleName() {
        return getRoleDefinition().getName();
    }

    @Override
    public boolean isForAll() {
        return forAll;
    }

    @Override
    public int getNumberOfPermissions() {
        return getRoleDefinition().getPermissions().size();
    }
}
