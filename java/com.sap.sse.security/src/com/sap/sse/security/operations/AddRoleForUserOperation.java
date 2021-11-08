package com.sap.sse.security.operations;

import java.io.ObjectStreamException;
import java.util.UUID;

import com.sap.sse.security.impl.ReplicableSecurityService;

public class AddRoleForUserOperation implements SecurityOperation<Void> {
    private static final long serialVersionUID = 3440561732671958186L;
    protected final String username;
    protected final UUID roleDefinitionId;
    protected final UUID idOfTenantQualifyingRole;
    protected final String nameOfUserQualifyingRole;
    protected final Boolean transitive;

    public AddRoleForUserOperation(String username, UUID roleDefinitionId, UUID idOfTenantQualifyingRole,
            String nameOfUserQualifyingRole, Boolean transitive) {
        this.username = username;
        this.roleDefinitionId = roleDefinitionId;
        this.idOfTenantQualifyingRole = idOfTenantQualifyingRole;
        this.nameOfUserQualifyingRole = nameOfUserQualifyingRole;
        this.transitive = transitive;
    }

    /**
     * If {@link #transitive} is {@code null} on this instance, this method replaces this de-serialized object with one
     * that has {@link #transitive} set to the default of {@code true}.
     */
    private Object readResolve() throws ObjectStreamException {
        final AddRoleForUserOperation result;
        if (this.transitive == null) {
            result = new AddRoleForUserOperation(username, roleDefinitionId, idOfTenantQualifyingRole, nameOfUserQualifyingRole, /* transitive */ true);
        } else {
            result = this;
        }
        return result;
    }
    
    @Override
    public Void internalApplyTo(ReplicableSecurityService toState) throws Exception {
        toState.internalAddRoleForUser(username, roleDefinitionId, idOfTenantQualifyingRole, nameOfUserQualifyingRole, transitive);
        return null;
    }
}
