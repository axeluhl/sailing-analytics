package com.sap.sse.security.operations;

import com.sap.sse.replication.OperationWithResult;
import com.sap.sse.security.impl.ReplicableSecurityService;
import com.sap.sse.security.shared.RoleDefinition;

public class UpdateRoleDefinitionOperation implements OperationWithResult<ReplicableSecurityService, Void> {
    private static final long serialVersionUID = 1L;

    protected final RoleDefinition roleDefinitionWithNewProperties;

    public UpdateRoleDefinitionOperation(RoleDefinition roleDefinitionWithNewProperties) {
        this.roleDefinitionWithNewProperties = roleDefinitionWithNewProperties;
    }

    @Override
    public Void internalApplyTo(ReplicableSecurityService toState) throws Exception {
        toState.internalUpdateRoleDefinition(roleDefinitionWithNewProperties);
        return null;
    }
}
