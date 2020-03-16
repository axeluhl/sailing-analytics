package com.sap.sse.security.operations;

import java.util.UUID;

import com.sap.sse.replication.OperationWithResult;
import com.sap.sse.security.impl.ReplicableSecurityService;
import com.sap.sse.security.shared.RoleDefinition;

public class CreateRoleDefinitionOperation implements OperationWithResult<ReplicableSecurityService, RoleDefinition> {
    private static final long serialVersionUID = 1L;

    protected final UUID roleDefinitionId;
    protected final String name;

    public CreateRoleDefinitionOperation(UUID roleDefinitionId, String name) {
        this.roleDefinitionId = roleDefinitionId;
        this.name = name;
    }

    @Override
    public RoleDefinition internalApplyTo(ReplicableSecurityService toState) throws Exception {
        return toState.internalCreateRoleDefinition(roleDefinitionId, name);
    }
}
