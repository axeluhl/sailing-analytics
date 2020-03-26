package com.sap.sailing.shared.server.operations;

import java.util.UUID;

import com.sap.sailing.domain.coursetemplate.MarkRole;
import com.sap.sailing.shared.server.impl.ReplicatingSharedSailingData;

public class RecordUsageForMarkRoleOperation implements SharedSailingDataOperation<Void> {
    private static final long serialVersionUID = -7727649023935102006L;
    protected final UUID markPropertiesId;
    protected final MarkRole roleName;

    public RecordUsageForMarkRoleOperation(UUID markPropertiesId, MarkRole roleName) {
        this.markPropertiesId = markPropertiesId;
        this.roleName = roleName;
    }

    @Override
    public Void internalApplyTo(ReplicatingSharedSailingData toState) throws Exception {
        toState.internalRecordUsage(markPropertiesId, roleName);
        return null;
    }
}
