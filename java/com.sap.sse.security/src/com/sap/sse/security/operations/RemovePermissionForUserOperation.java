package com.sap.sse.security.operations;

import com.sap.sse.replication.OperationWithResult;
import com.sap.sse.security.impl.ReplicableSecurityService;
import com.sap.sse.security.shared.WildcardPermission;

public class RemovePermissionForUserOperation implements OperationWithResult<ReplicableSecurityService, Void> {
    private static final long serialVersionUID = 1L;

    protected final String username;
    protected final WildcardPermission permissionToRemove;

    public RemovePermissionForUserOperation(String username, WildcardPermission permissionToRemove) {
        this.username = username;
        this.permissionToRemove = permissionToRemove;
    }

    @Override
    public Void internalApplyTo(ReplicableSecurityService toState) throws Exception {
        toState.internalRemovePermissionForUser(username, permissionToRemove);
        return null;
    }
}
