package com.sap.sse.security.operations;

import java.util.UUID;

import com.sap.sse.replication.OperationWithResult;
import com.sap.sse.security.impl.ReplicableSecurityService;

public class CreateUserGroupOperation implements OperationWithResult<ReplicableSecurityService, Void> {
    private static final long serialVersionUID = 1L;

    protected final UUID id;
    protected final String name;

    public CreateUserGroupOperation(UUID id, String name) {
        this.id = id;
        this.name = name;
    }

    @Override
    public Void internalApplyTo(ReplicableSecurityService toState) throws Exception {
        toState.internalCreateUserGroup(id, name);
        return null;
    }
}
