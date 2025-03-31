package com.sap.sailing.shared.server.operations;

import java.util.UUID;

import com.sap.sailing.shared.server.impl.ReplicatingSharedSailingData;

public class RecordUsageForMarkTemplateOperation implements SharedSailingDataOperation<Void> {
    private static final long serialVersionUID = -2911190856463977956L;
    protected final UUID markTemplateId;
    protected final UUID markPropertiesId;

    public RecordUsageForMarkTemplateOperation(UUID markTemplateId, UUID markPropertiesId) {
        this.markTemplateId = markTemplateId;
        this.markPropertiesId = markPropertiesId;
    }

    @Override
    public Void internalApplyTo(ReplicatingSharedSailingData toState) throws Exception {
        toState.internalRecordUsage(markTemplateId, markPropertiesId);
        return null;
    }
}
