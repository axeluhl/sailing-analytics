package com.sap.sse.security.operations;

import java.util.UUID;

import com.sap.sse.replication.OperationWithResult;
import com.sap.sse.security.impl.ReplicableSecurityService;

public class SetDefaultTenantForServerForUserOperation implements OperationWithResult<ReplicableSecurityService, Void> {
    private static final long serialVersionUID = 1L;

    protected final String username;
    protected final UUID defaultTenantId;
    protected final String serverName;

    public SetDefaultTenantForServerForUserOperation(String username, UUID defaultTenantId, String serverName) {
        this.username = username;
        this.defaultTenantId = defaultTenantId;
        this.serverName = serverName;
    }

    @Override
    public Void internalApplyTo(ReplicableSecurityService toState) throws Exception {
        toState.internalSetDefaultTenantForServerForUser(username, defaultTenantId, serverName);
        return null;
    }
}
