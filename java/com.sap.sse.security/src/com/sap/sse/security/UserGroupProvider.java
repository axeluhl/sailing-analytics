package com.sap.sse.security;

import com.sap.sse.security.shared.SecurityUser;
import com.sap.sse.security.shared.UserGroup;

public interface UserGroupProvider {
    Iterable<UserGroup> getUserGroupsOfUser(SecurityUser user);
}
