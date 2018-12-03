package com.sap.sse.security.shared.impl;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

import com.sap.sse.security.shared.AbstractUserGroupImpl;

public class UserGroupImpl extends AbstractUserGroupImpl<User> implements UserGroup {
    private static final long serialVersionUID = 1L;

    public UserGroupImpl(Set<User> users, UUID id, String name) {
        super(users, id, name);
    }

    public UserGroupImpl(UUID id, String name) {
        super(new HashSet<User>(), id, name);
    }

}
