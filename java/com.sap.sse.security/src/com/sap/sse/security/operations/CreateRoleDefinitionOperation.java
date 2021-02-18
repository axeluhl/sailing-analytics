package com.sap.sse.security.operations;

import java.util.UUID;

import com.sap.sse.security.impl.ReplicableSecurityService;
import com.sap.sse.security.shared.RoleDefinition;

public class CreateRoleDefinitionOperation implements SecurityOperation<RoleDefinition> {
    private static final long serialVersionUID = 4931617817218964817L;
    protected final UUID roleDefinitionId;
    protected final String name;
    protected final boolean transitive;

    public CreateRoleDefinitionOperation(UUID roleDefinitionId, String name, boolean transitive) {
        this.roleDefinitionId = roleDefinitionId;
        this.name = name;
        this.transitive = transitive;
    }

    @Override
    public RoleDefinition internalApplyTo(ReplicableSecurityService toState) throws Exception {
        return toState.internalCreateRoleDefinition(roleDefinitionId, name, transitive);
    }
}
