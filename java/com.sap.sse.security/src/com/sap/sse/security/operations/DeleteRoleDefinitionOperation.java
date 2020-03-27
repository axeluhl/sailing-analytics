package com.sap.sse.security.operations;

import java.util.UUID;

import com.sap.sse.security.impl.ReplicableSecurityService;

public class DeleteRoleDefinitionOperation implements SecurityOperation<Void> {
    private static final long serialVersionUID = 2803534172168486392L;
    protected final UUID roleDefinitionId;

    public DeleteRoleDefinitionOperation(UUID roleDefinitionId) {
        this.roleDefinitionId = roleDefinitionId;
    }

    @Override
    public Void internalApplyTo(ReplicableSecurityService toState) throws Exception {
        toState.internalDeleteRoleDefinition(roleDefinitionId);
        return null;
    }
}
