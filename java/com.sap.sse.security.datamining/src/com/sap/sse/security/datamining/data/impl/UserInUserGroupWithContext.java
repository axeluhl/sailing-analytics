package com.sap.sse.security.datamining.data.impl;

import com.sap.sse.security.datamining.data.HasUserGroupContext;
import com.sap.sse.security.datamining.data.HasUserInUserGroupContext;
import com.sap.sse.security.shared.impl.User;

/**
 * Equality is based on the {@link #getUser() user}.
 * 
 * @author Axel Uhl (d043530)
 *
 */
public class UserInUserGroupWithContext implements HasUserInUserGroupContext {
    private final HasUserGroupContext userGroupContext;
    private final User user;
    
    public UserInUserGroupWithContext(HasUserGroupContext userGroupContext, User user) {
        super();
        this.userGroupContext = userGroupContext;
        this.user = user;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((user == null) ? 0 : user.hashCode());
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
        UserInUserGroupWithContext other = (UserInUserGroupWithContext) obj;
        if (user == null) {
            if (other.user != null)
                return false;
        } else if (!user.equals(other.user))
            return false;
        return true;
    }

    @Override
    public HasUserGroupContext getUserGroupContext() {
        return userGroupContext;
    }

    @Override
    public User getUser() {
        return user;
    }
}
