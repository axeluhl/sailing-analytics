package com.sap.sse.security;

import com.sap.sse.security.shared.WildcardPermission;
import com.sap.sse.security.shared.impl.User;

/**
 * Part of an observer pattern implementation for the set of users who have a specific permission on a specific object.
 * See also {@link SecurityService#addPermissionChangeListener}.
 * 
 * @author Axel Uhl (D043530)
 *
 */
public interface PermissionChangeListener {
    void setOfUsersWithPermissionChanged(WildcardPermission permission, Iterable<User> usersNowHavingPermission);
}
