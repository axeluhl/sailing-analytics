package com.sap.sailing.shared.server.operations;

import java.util.UUID;

import com.sap.sailing.shared.server.impl.ReplicatingSharedSailingData;

public class DeleteMarkPropertiesOperation implements SharedSailingDataOperation<Void> {
    private static final long serialVersionUID = -3972957992707082281L;
    protected final UUID markPropertiesUUID;

    public DeleteMarkPropertiesOperation(UUID markPropertiesUUID) {
        this.markPropertiesUUID = markPropertiesUUID;
    }

    @Override
    public Void internalApplyTo(ReplicatingSharedSailingData toState) throws Exception {
        toState.internalDeleteMarkProperties(markPropertiesUUID);
        return null;
    }
}
