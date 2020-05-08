package com.sap.sse.security.operations;

import com.sap.sse.security.impl.ReplicableSecurityService;
import com.sap.sse.security.shared.WildcardPermission;

public class RemovePermissionForUserOperation implements SecurityOperation<Void> {
    private static final long serialVersionUID = 3069373737677863363L;
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
