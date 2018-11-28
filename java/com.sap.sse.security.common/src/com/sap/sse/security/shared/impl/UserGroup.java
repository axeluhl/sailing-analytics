package com.sap.sse.security.shared.impl;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

import com.sap.sse.security.shared.AbstractUserGroup;

public class UserGroup extends AbstractUserGroup<User> {
    private static final long serialVersionUID = 1L;

    public UserGroup(Set<User> users, UUID id, String name) {
        super(users, id, name);
    }

    public UserGroup(UUID id, String name) {
        super(new HashSet<User>(), id, name);
    }

}
