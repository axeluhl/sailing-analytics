package com.sap.sse.security.operations;

import java.util.UUID;

import com.sap.sse.security.impl.ReplicableSecurityService;

public class AddUserToUserGroupOperation implements SecurityOperation<Void> {
    private static final long serialVersionUID = 299626075988002751L;
    protected final UUID groupId;
    protected final String username;

    public AddUserToUserGroupOperation(UUID groupId, String username) {
        this.groupId = groupId;
        this.username = username;
    }

    @Override
    public Void internalApplyTo(ReplicableSecurityService toState) throws Exception {
        toState.internalAddUserToUserGroup(groupId, username);
        return null;
    }
}
