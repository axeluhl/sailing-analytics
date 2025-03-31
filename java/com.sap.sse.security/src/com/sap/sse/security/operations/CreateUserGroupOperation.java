package com.sap.sse.security.operations;

import java.util.UUID;

import com.sap.sse.security.impl.ReplicableSecurityService;

public class CreateUserGroupOperation implements SecurityOperation<Void> {
    private static final long serialVersionUID = 9151578637763285168L;
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
