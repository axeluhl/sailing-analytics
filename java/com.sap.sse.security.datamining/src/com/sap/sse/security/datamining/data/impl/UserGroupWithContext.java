package com.sap.sse.security.datamining.data.impl;

import com.sap.sse.security.SecurityService;
import com.sap.sse.security.datamining.data.HasUserGroupContext;
import com.sap.sse.security.shared.impl.UserGroup;

/**
 * Equality is based on the {@link #getUserGroup() user group}.
 * 
 * @author Axel Uhl (d043530)
 *
 */
public class UserGroupWithContext implements HasUserGroupContext {
    private final UserGroup userGroup;
    private final SecurityService securityService;
    
    public UserGroupWithContext(UserGroup userGroup, SecurityService securityService) {
        this.userGroup = userGroup;
        this.securityService = securityService;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((userGroup == null) ? 0 : userGroup.hashCode());
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
        UserGroupWithContext other = (UserGroupWithContext) obj;
        if (userGroup == null) {
            if (other.userGroup != null)
                return false;
        } else if (!userGroup.equals(other.userGroup))
            return false;
        return true;
    }

    @Override
    public UserGroup getUserGroup() {
        return userGroup;
    }

    @Override
    public SecurityService getSecurityService() {
        return securityService;
    }

}
