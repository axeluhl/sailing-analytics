package com.sap.sailing.shared.server.operations;

import com.sap.sailing.shared.server.impl.ReplicatingSharedSailingData;

import java.util.UUID;

public class DeleteMarkTemplateOperation implements SharedSailingDataOperation<Void> {
    private static final long serialVersionUID = 4157045245768162929L;
    private final UUID markTemplateUUID;

    public DeleteMarkTemplateOperation(UUID markTemplateUUID) {
        this.markTemplateUUID = markTemplateUUID;
    }

    @Override
    public Void internalApplyTo(ReplicatingSharedSailingData toState) throws Exception {
        toState.internalDeleteMarkTemplate(markTemplateUUID);
        return null;
    }
}
