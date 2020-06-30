package com.sap.sse.security.operations;

import java.util.UUID;

import com.sap.sse.security.impl.ReplicableSecurityService;

public class AddRoleForUserOperation implements SecurityOperation<Void> {
    private static final long serialVersionUID = 3440561732671958186L;
    protected final String username;
    protected final UUID roleDefinitionId;
    protected final UUID idOfTenantQualifyingRole;
    protected final String nameOfUserQualifyingRole;

    public AddRoleForUserOperation(String username, UUID roleDefinitionId, UUID idOfTenantQualifyingRole,
            String nameOfUserQualifyingRole) {
        this.username = username;
        this.roleDefinitionId = roleDefinitionId;
        this.idOfTenantQualifyingRole = idOfTenantQualifyingRole;
        this.nameOfUserQualifyingRole = nameOfUserQualifyingRole;
    }

    @Override
    public Void internalApplyTo(ReplicableSecurityService toState) throws Exception {
        toState.internalAddRoleForUser(username, roleDefinitionId, idOfTenantQualifyingRole, nameOfUserQualifyingRole);
        return null;
    }
}
