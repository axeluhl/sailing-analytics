package com.sap.sse.security.operations;

import com.sap.sse.replication.OperationWithResult;
import com.sap.sse.security.impl.ReplicableSecurityService;
import com.sap.sse.security.shared.WildcardPermission;

public class AddPermissionForUserOperation implements OperationWithResult<ReplicableSecurityService, Void> {
    private static final long serialVersionUID = 1L;

    protected final String username;
    protected final WildcardPermission permissionToAdd;

    public AddPermissionForUserOperation(String username, WildcardPermission permissionToAdd) {
        this.username = username;
        this.permissionToAdd = permissionToAdd;
    }

    @Override
    public Void internalApplyTo(ReplicableSecurityService toState) throws Exception {
        toState.internalAddPermissionForUser(username, permissionToAdd);
        return null;
    }
}
