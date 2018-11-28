package com.sap.sse.security;

import com.sap.sse.security.shared.User;
import com.sap.sse.security.shared.UserGroup;

public interface UserGroupProvider {
    Iterable<UserGroup> getUserGroupsOfUser(User user);
}
