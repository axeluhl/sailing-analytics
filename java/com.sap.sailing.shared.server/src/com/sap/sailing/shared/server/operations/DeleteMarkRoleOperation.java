package com.sap.sailing.shared.server.operations;

import com.sap.sailing.shared.server.impl.ReplicatingSharedSailingData;

import java.util.UUID;

public class DeleteMarkRoleOperation implements SharedSailingDataOperation<Void> {
    private static final long serialVersionUID = 6069375751925606420L;
    private final UUID markRoleUUID;

    public DeleteMarkRoleOperation(UUID markRoleUUID) {
        this.markRoleUUID = markRoleUUID;
    }

    @Override
    public Void internalApplyTo(ReplicatingSharedSailingData toState) throws Exception {
        toState.internalDeleteMarkRole(markRoleUUID);
        return null;
    }
}
